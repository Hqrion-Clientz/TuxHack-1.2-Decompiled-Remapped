// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classloaderhandler;

import java.nio.file.Path;
import java.util.Iterator;
import nonapi.io.github.classgraph.utils.ReflectionUtils;
import java.util.Collection;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.classpath.ClasspathOrder;
import nonapi.io.github.classgraph.classpath.ClassLoaderOrder;
import nonapi.io.github.classgraph.utils.LogNode;

class QuarkusClassLoaderHandler implements ClassLoaderHandler
{
    private static final String RUNTIME_CLASSLOADER = "io.quarkus.runner.RuntimeClassLoader";
    private static final String QUARKUS_CLASSLOADER = "io.quarkus.bootstrap.classloading.QuarkusClassLoader";
    
    private QuarkusClassLoaderHandler() {
    }
    
    public static boolean canHandle(final Class<?> classLoaderClass, final LogNode log) {
        return "io.quarkus.runner.RuntimeClassLoader".equals(classLoaderClass.getName()) || "io.quarkus.bootstrap.classloading.QuarkusClassLoader".equals(classLoaderClass.getName());
    }
    
    public static void findClassLoaderOrder(final ClassLoader classLoader, final ClassLoaderOrder classLoaderOrder, final LogNode log) {
        classLoaderOrder.delegateTo(classLoader.getParent(), true, log);
        classLoaderOrder.add(classLoader, log);
    }
    
    public static void findClasspathOrder(final ClassLoader classLoader, final ClasspathOrder classpathOrder, final ScanSpec scanSpec, final LogNode log) {
        final String classLoaderName = classLoader.getClass().getName();
        if ("io.quarkus.runner.RuntimeClassLoader".equals(classLoaderName)) {
            findClasspathOrderForRuntimeClassloader(classLoader, classpathOrder, scanSpec, log);
        }
        else if ("io.quarkus.bootstrap.classloading.QuarkusClassLoader".equals(classLoaderName)) {
            findClasspathOrderForQuarkusClassloader(classLoader, classpathOrder, scanSpec, log);
        }
    }
    
    private static void findClasspathOrderForQuarkusClassloader(final ClassLoader classLoader, final ClasspathOrder classpathOrder, final ScanSpec scanSpec, final LogNode log) {
        for (final Object element : (Collection)ReflectionUtils.getFieldVal(classLoader, "elements", false)) {
            final String elementClassName = element.getClass().getName();
            if ("io.quarkus.bootstrap.classloading.JarClassPathElement".equals(elementClassName)) {
                classpathOrder.addClasspathEntry(ReflectionUtils.getFieldVal(element, "file", false), classLoader, scanSpec, log);
            }
            else {
                if (!"io.quarkus.bootstrap.classloading.DirectoryClassPathElement".equals(elementClassName)) {
                    continue;
                }
                classpathOrder.addClasspathEntry(ReflectionUtils.getFieldVal(element, "root", false), classLoader, scanSpec, log);
            }
        }
    }
    
    private static void findClasspathOrderForRuntimeClassloader(final ClassLoader classLoader, final ClasspathOrder classpathOrder, final ScanSpec scanSpec, final LogNode log) {
        final Collection<Path> applicationClassDirectories = (Collection<Path>)ReflectionUtils.getFieldVal(classLoader, "applicationClassDirectories", false);
        if (applicationClassDirectories != null) {
            for (final Path path : applicationClassDirectories) {
                classpathOrder.addClasspathEntryObject(path.toUri(), classLoader, scanSpec, log);
            }
        }
    }
}
