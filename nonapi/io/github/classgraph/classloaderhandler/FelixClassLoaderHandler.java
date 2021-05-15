// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classloaderhandler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import java.util.Set;
import nonapi.io.github.classgraph.classpath.ClasspathOrder;
import nonapi.io.github.classgraph.utils.ReflectionUtils;
import java.io.File;
import nonapi.io.github.classgraph.classpath.ClassLoaderOrder;
import nonapi.io.github.classgraph.utils.LogNode;

class FelixClassLoaderHandler implements ClassLoaderHandler
{
    private FelixClassLoaderHandler() {
    }
    
    public static boolean canHandle(final Class<?> classLoaderClass, final LogNode log) {
        return "org.apache.felix.framework.BundleWiringImpl$BundleClassLoaderJava5".equals(classLoaderClass.getName()) || "org.apache.felix.framework.BundleWiringImpl$BundleClassLoader".equals(classLoaderClass.getName());
    }
    
    public static void findClassLoaderOrder(final ClassLoader classLoader, final ClassLoaderOrder classLoaderOrder, final LogNode log) {
        classLoaderOrder.delegateTo(classLoader.getParent(), true, log);
        classLoaderOrder.add(classLoader, log);
    }
    
    private static File getContentLocation(final Object content) {
        return (File)ReflectionUtils.invokeMethod(content, "getFile", false);
    }
    
    private static void addBundle(final Object bundleWiring, final ClassLoader classLoader, final ClasspathOrder classpathOrderOut, final Set<Object> bundles, final ScanSpec scanSpec, final LogNode log) {
        bundles.add(bundleWiring);
        final Object revision = ReflectionUtils.invokeMethod(bundleWiring, "getRevision", false);
        final Object content = ReflectionUtils.invokeMethod(revision, "getContent", false);
        final File location = (content != null) ? getContentLocation(content) : null;
        if (location != null) {
            classpathOrderOut.addClasspathEntry(location, classLoader, scanSpec, log);
            final List<?> embeddedContent = (List<?>)ReflectionUtils.invokeMethod(revision, "getContentPath", false);
            if (embeddedContent != null) {
                for (final Object embedded : embeddedContent) {
                    if (embedded != content) {
                        final File embeddedLocation = (embedded != null) ? getContentLocation(embedded) : null;
                        if (embeddedLocation == null) {
                            continue;
                        }
                        classpathOrderOut.addClasspathEntry(embeddedLocation, classLoader, scanSpec, log);
                    }
                }
            }
        }
    }
    
    public static void findClasspathOrder(final ClassLoader classLoader, final ClasspathOrder classpathOrder, final ScanSpec scanSpec, final LogNode log) {
        final Set<Object> bundles = new HashSet<Object>();
        final Object bundleWiring = ReflectionUtils.getFieldVal(classLoader, "m_wiring", false);
        addBundle(bundleWiring, classLoader, classpathOrder, bundles, scanSpec, log);
        final List<?> requiredWires = (List<?>)ReflectionUtils.invokeMethod(bundleWiring, "getRequiredWires", String.class, null, false);
        if (requiredWires != null) {
            for (final Object wire : requiredWires) {
                final Object provider = ReflectionUtils.invokeMethod(wire, "getProviderWiring", false);
                if (!bundles.contains(provider)) {
                    addBundle(provider, classLoader, classpathOrder, bundles, scanSpec, log);
                }
            }
        }
    }
}
