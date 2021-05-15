// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classloaderhandler;

import java.util.Iterator;
import java.io.File;
import java.util.List;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.classpath.ClasspathOrder;
import nonapi.io.github.classgraph.classpath.ClassLoaderOrder;
import nonapi.io.github.classgraph.utils.ReflectionUtils;
import nonapi.io.github.classgraph.utils.LogNode;

class TomcatWebappClassLoaderBaseHandler implements ClassLoaderHandler
{
    private TomcatWebappClassLoaderBaseHandler() {
    }
    
    public static boolean canHandle(final Class<?> classLoaderClass, final LogNode log) {
        return "org.apache.catalina.loader.WebappClassLoaderBase".equals(classLoaderClass.getName());
    }
    
    private static boolean isParentFirst(final ClassLoader classLoader) {
        final Object delegateObject = ReflectionUtils.getFieldVal(classLoader, "delegate", false);
        return delegateObject == null || (boolean)delegateObject;
    }
    
    public static void findClassLoaderOrder(final ClassLoader classLoader, final ClassLoaderOrder classLoaderOrder, final LogNode log) {
        final boolean isParentFirst = isParentFirst(classLoader);
        if (isParentFirst) {
            classLoaderOrder.delegateTo(classLoader.getParent(), true, log);
        }
        classLoaderOrder.add(classLoader, log);
        if (!isParentFirst) {
            classLoaderOrder.delegateTo(classLoader.getParent(), true, log);
        }
    }
    
    public static void findClasspathOrder(final ClassLoader classLoader, final ClasspathOrder classpathOrder, final ScanSpec scanSpec, final LogNode log) {
        final Object resources = ReflectionUtils.invokeMethod(classLoader, "getResources", false);
        final Object baseURLs = ReflectionUtils.invokeMethod(resources, "getBaseUrls", false);
        classpathOrder.addClasspathEntryObject(baseURLs, classLoader, scanSpec, log);
        final List<List<?>> allResources = (List<List<?>>)ReflectionUtils.getFieldVal(resources, "allResources", false);
        if (allResources != null) {
            for (final List<?> webResourceSetList : allResources) {
                for (final Object webResourceSet : webResourceSetList) {
                    if (webResourceSet != null) {
                        final File file = (File)ReflectionUtils.invokeMethod(webResourceSet, "getFileBase", false);
                        String base = (file == null) ? null : file.getPath();
                        if (base == null) {
                            base = (String)ReflectionUtils.invokeMethod(webResourceSet, "getBase", false);
                        }
                        if (base == null) {
                            base = (String)ReflectionUtils.invokeMethod(webResourceSet, "getBaseUrlString", false);
                        }
                        if (base == null) {
                            continue;
                        }
                        final String archivePath = (String)ReflectionUtils.getFieldVal(webResourceSet, "archivePath", false);
                        if (archivePath != null && !archivePath.isEmpty()) {
                            base = base + "!" + (archivePath.startsWith("/") ? archivePath : ("/" + archivePath));
                        }
                        final String className = webResourceSet.getClass().getName();
                        final boolean isJar = className.equals("java.org.apache.catalina.webresources.JarResourceSet") || className.equals("java.org.apache.catalina.webresources.JarWarResourceSet");
                        final String internalPath = (String)ReflectionUtils.invokeMethod(webResourceSet, "getInternalPath", false);
                        if (internalPath != null && !internalPath.isEmpty() && !internalPath.equals("/")) {
                            classpathOrder.addClasspathEntryObject(base + (isJar ? "!" : "") + (internalPath.startsWith("/") ? internalPath : ("/" + internalPath)), classLoader, scanSpec, log);
                        }
                        else {
                            classpathOrder.addClasspathEntryObject(base, classLoader, scanSpec, log);
                        }
                    }
                }
            }
        }
        final Object urls = ReflectionUtils.invokeMethod(classLoader, "getURLs", false);
        classpathOrder.addClasspathEntryObject(urls, classLoader, scanSpec, log);
    }
}
