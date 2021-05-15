// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classloaderhandler;

import nonapi.io.github.classgraph.utils.ReflectionUtils;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.classpath.ClasspathOrder;
import nonapi.io.github.classgraph.classpath.ClassLoaderOrder;
import nonapi.io.github.classgraph.utils.LogNode;

class WebsphereTraditionalClassLoaderHandler implements ClassLoaderHandler
{
    private WebsphereTraditionalClassLoaderHandler() {
    }
    
    public static boolean canHandle(final Class<?> classLoaderClass, final LogNode log) {
        return "com.ibm.ws.classloader.CompoundClassLoader".equals(classLoaderClass.getName()) || "com.ibm.ws.classloader.ProtectionClassLoader".equals(classLoaderClass.getName()) || "com.ibm.ws.bootstrap.ExtClassLoader".equals(classLoaderClass.getName());
    }
    
    public static void findClassLoaderOrder(final ClassLoader classLoader, final ClassLoaderOrder classLoaderOrder, final LogNode log) {
        classLoaderOrder.delegateTo(classLoader.getParent(), true, log);
        classLoaderOrder.add(classLoader, log);
    }
    
    public static void findClasspathOrder(final ClassLoader classLoader, final ClasspathOrder classpathOrder, final ScanSpec scanSpec, final LogNode log) {
        final String classpath = (String)ReflectionUtils.invokeMethod(classLoader, "getClassPath", false);
        classpathOrder.addClasspathPathStr(classpath, classLoader, scanSpec, log);
    }
}
