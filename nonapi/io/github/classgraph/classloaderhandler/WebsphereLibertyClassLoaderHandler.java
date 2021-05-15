// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classloaderhandler;

import java.util.List;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.classpath.ClasspathOrder;
import java.util.Iterator;
import java.util.HashSet;
import java.io.File;
import java.util.Arrays;
import nonapi.io.github.classgraph.utils.ReflectionUtils;
import java.util.Collections;
import java.util.Collection;
import nonapi.io.github.classgraph.classpath.ClassLoaderOrder;
import nonapi.io.github.classgraph.utils.LogNode;

class WebsphereLibertyClassLoaderHandler implements ClassLoaderHandler
{
    private static final String PKG_PREFIX = "com.ibm.ws.classloading.internal.";
    private static final String IBM_APP_CLASS_LOADER = "com.ibm.ws.classloading.internal.AppClassLoader";
    private static final String IBM_THREAD_CONTEXT_CLASS_LOADER = "com.ibm.ws.classloading.internal.ThreadContextClassLoader";
    
    private WebsphereLibertyClassLoaderHandler() {
    }
    
    public static boolean canHandle(final Class<?> classLoaderClass, final LogNode log) {
        return "com.ibm.ws.classloading.internal.AppClassLoader".equals(classLoaderClass.getName()) || "com.ibm.ws.classloading.internal.ThreadContextClassLoader".equals(classLoaderClass.getName());
    }
    
    public static void findClassLoaderOrder(final ClassLoader classLoader, final ClassLoaderOrder classLoaderOrder, final LogNode log) {
        classLoaderOrder.delegateTo(classLoader.getParent(), true, log);
        classLoaderOrder.add(classLoader, log);
    }
    
    private static Collection<Object> getPaths(final Object containerClassLoader) {
        if (containerClassLoader == null) {
            return Collections.emptyList();
        }
        Collection<Object> urls = callGetUrls(containerClassLoader, "getContainerURLs");
        if (urls != null && !urls.isEmpty()) {
            return urls;
        }
        final Object container = ReflectionUtils.getFieldVal(containerClassLoader, "container", false);
        if (container == null) {
            return Collections.emptyList();
        }
        urls = callGetUrls(container, "getURLs");
        if (urls != null && !urls.isEmpty()) {
            return urls;
        }
        final Object delegate = ReflectionUtils.getFieldVal(container, "delegate", false);
        if (delegate == null) {
            return Collections.emptyList();
        }
        final String path = (String)ReflectionUtils.getFieldVal(delegate, "path", false);
        if (path != null && path.length() > 0) {
            return Arrays.asList(path);
        }
        final Object base = ReflectionUtils.getFieldVal(delegate, "base", false);
        if (base == null) {
            return Collections.emptyList();
        }
        final Object archiveFile = ReflectionUtils.getFieldVal(base, "archiveFile", false);
        if (archiveFile != null) {
            final File file = (File)archiveFile;
            return Arrays.asList(file.getAbsolutePath());
        }
        return Collections.emptyList();
    }
    
    private static Collection<Object> callGetUrls(final Object container, final String methodName) {
        if (container != null) {
            try {
                final Collection<Object> results = (Collection<Object>)ReflectionUtils.invokeMethod(container, methodName, false);
                if (results != null && !results.isEmpty()) {
                    final Collection<Object> allUrls = new HashSet<Object>();
                    for (final Object result : results) {
                        if (result instanceof Collection) {
                            for (final Object url : (Collection)result) {
                                if (url != null) {
                                    allUrls.add(url);
                                }
                            }
                        }
                        else {
                            if (result == null) {
                                continue;
                            }
                            allUrls.add(result);
                        }
                    }
                    return allUrls;
                }
            }
            catch (UnsupportedOperationException ex) {}
        }
        return Collections.emptyList();
    }
    
    public static void findClasspathOrder(final ClassLoader classLoader, final ClasspathOrder classpathOrder, final ScanSpec scanSpec, final LogNode log) {
        final Object appLoader = ReflectionUtils.getFieldVal(classLoader, "appLoader", false);
        Object smartClassPath;
        if (appLoader != null) {
            smartClassPath = ReflectionUtils.getFieldVal(appLoader, "smartClassPath", false);
        }
        else {
            smartClassPath = ReflectionUtils.getFieldVal(classLoader, "smartClassPath", false);
        }
        if (smartClassPath != null) {
            final Collection<Object> paths = callGetUrls(smartClassPath, "getClassPath");
            if (!paths.isEmpty()) {
                for (final Object path : paths) {
                    classpathOrder.addClasspathEntry(path, classLoader, scanSpec, log);
                }
            }
            else {
                final List<Object> classPathElements = (List<Object>)ReflectionUtils.getFieldVal(smartClassPath, "classPath", false);
                if (classPathElements != null && !classPathElements.isEmpty()) {
                    for (final Object classPathElement : classPathElements) {
                        final Collection<Object> subPaths = getPaths(classPathElement);
                        for (final Object path2 : subPaths) {
                            classpathOrder.addClasspathEntry(path2, classLoader, scanSpec, log);
                        }
                    }
                }
            }
        }
    }
}
