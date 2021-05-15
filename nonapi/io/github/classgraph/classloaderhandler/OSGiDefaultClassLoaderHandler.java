// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classloaderhandler;

import java.io.File;
import nonapi.io.github.classgraph.utils.ReflectionUtils;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.classpath.ClasspathOrder;
import nonapi.io.github.classgraph.classpath.ClassLoaderOrder;
import nonapi.io.github.classgraph.utils.LogNode;

class OSGiDefaultClassLoaderHandler implements ClassLoaderHandler
{
    private OSGiDefaultClassLoaderHandler() {
    }
    
    public static boolean canHandle(final Class<?> classLoaderClass, final LogNode log) {
        return "org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader".equals(classLoaderClass.getName());
    }
    
    public static void findClassLoaderOrder(final ClassLoader classLoader, final ClassLoaderOrder classLoaderOrder, final LogNode log) {
        classLoaderOrder.delegateTo(classLoader.getParent(), true, log);
        classLoaderOrder.add(classLoader, log);
    }
    
    public static void findClasspathOrder(final ClassLoader classLoader, final ClasspathOrder classpathOrder, final ScanSpec scanSpec, final LogNode log) {
        final Object classpathManager = ReflectionUtils.invokeMethod(classLoader, "getClasspathManager", false);
        final Object[] entries = (Object[])ReflectionUtils.getFieldVal(classpathManager, "entries", false);
        if (entries != null) {
            for (final Object entry : entries) {
                final Object bundleFile = ReflectionUtils.invokeMethod(entry, "getBundleFile", false);
                final File baseFile = (File)ReflectionUtils.invokeMethod(bundleFile, "getBaseFile", false);
                if (baseFile != null) {
                    classpathOrder.addClasspathEntry(baseFile.getPath(), classLoader, scanSpec, log);
                }
            }
        }
    }
}
