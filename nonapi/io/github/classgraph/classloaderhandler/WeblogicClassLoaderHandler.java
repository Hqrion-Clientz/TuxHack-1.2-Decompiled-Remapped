// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classloaderhandler;

import nonapi.io.github.classgraph.utils.ReflectionUtils;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.classpath.ClasspathOrder;
import nonapi.io.github.classgraph.classpath.ClassLoaderOrder;
import nonapi.io.github.classgraph.utils.LogNode;

class WeblogicClassLoaderHandler implements ClassLoaderHandler
{
    private WeblogicClassLoaderHandler() {
    }
    
    public static boolean canHandle(final Class<?> classLoaderClass, final LogNode log) {
        return "weblogic.utils.classloaders.ChangeAwareClassLoader".equals(classLoaderClass.getName()) || "weblogic.utils.classloaders.GenericClassLoader".equals(classLoaderClass.getName()) || "weblogic.utils.classloaders.FilteringClassLoader".equals(classLoaderClass.getName()) || "weblogic.servlet.jsp.JspClassLoader".equals(classLoaderClass.getName()) || "weblogic.servlet.jsp.TagFileClassLoader".equals(classLoaderClass.getName());
    }
    
    public static void findClassLoaderOrder(final ClassLoader classLoader, final ClassLoaderOrder classLoaderOrder, final LogNode log) {
        classLoaderOrder.delegateTo(classLoader.getParent(), true, log);
        classLoaderOrder.add(classLoader, log);
    }
    
    public static void findClasspathOrder(final ClassLoader classLoader, final ClasspathOrder classpathOrder, final ScanSpec scanSpec, final LogNode log) {
        classpathOrder.addClasspathPathStr((String)ReflectionUtils.invokeMethod(classLoader, "getFinderClassPath", false), classLoader, scanSpec, log);
        classpathOrder.addClasspathPathStr((String)ReflectionUtils.invokeMethod(classLoader, "getClassPath", false), classLoader, scanSpec, log);
    }
}
