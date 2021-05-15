// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classloaderhandler;

import nonapi.io.github.classgraph.utils.ReflectionUtils;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.classpath.ClasspathOrder;
import nonapi.io.github.classgraph.classpath.ClassLoaderOrder;
import nonapi.io.github.classgraph.utils.LogNode;

class FallbackClassLoaderHandler implements ClassLoaderHandler
{
    private FallbackClassLoaderHandler() {
    }
    
    public static boolean canHandle(final Class<?> classLoaderClass, final LogNode log) {
        return true;
    }
    
    public static void findClassLoaderOrder(final ClassLoader classLoader, final ClassLoaderOrder classLoaderOrder, final LogNode log) {
        classLoaderOrder.delegateTo(classLoader.getParent(), true, log);
        classLoaderOrder.add(classLoader, log);
    }
    
    public static void findClasspathOrder(final ClassLoader classLoader, final ClasspathOrder classpathOrder, final ScanSpec scanSpec, final LogNode log) {
        boolean valid = false;
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "getClassPath", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "getClasspath", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "classpath", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "classPath", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "cp", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.getFieldVal(classLoader, "classpath", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.getFieldVal(classLoader, "classPath", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.getFieldVal(classLoader, "cp", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "getPath", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "getPaths", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "path", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "paths", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.getFieldVal(classLoader, "paths", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.getFieldVal(classLoader, "paths", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "getDir", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "getDirs", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "dir", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "dirs", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.getFieldVal(classLoader, "dir", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.getFieldVal(classLoader, "dirs", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "getFile", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "getFiles", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "file", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "files", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.getFieldVal(classLoader, "file", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.getFieldVal(classLoader, "files", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "getJar", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "getJars", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "jar", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "jars", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.getFieldVal(classLoader, "jar", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.getFieldVal(classLoader, "jars", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "getURL", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "getURLs", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "getUrl", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "getUrls", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "url", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.invokeMethod(classLoader, "urls", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.getFieldVal(classLoader, "url", false), classLoader, scanSpec, log);
        valid |= classpathOrder.addClasspathEntryObject(ReflectionUtils.getFieldVal(classLoader, "urls", false), classLoader, scanSpec, log);
        if (log != null) {
            log.log("FallbackClassLoaderHandler " + (valid ? "found" : "did not find") + " classpath entries in unknown ClassLoader " + classLoader);
        }
    }
}
