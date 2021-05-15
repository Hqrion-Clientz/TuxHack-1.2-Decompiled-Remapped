// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classloaderhandler;

import java.util.HashSet;
import java.lang.reflect.Array;
import nonapi.io.github.classgraph.utils.ReflectionUtils;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.classpath.ClasspathOrder;
import java.util.Set;
import nonapi.io.github.classgraph.classpath.ClassLoaderOrder;
import nonapi.io.github.classgraph.utils.LogNode;

class EquinoxClassLoaderHandler implements ClassLoaderHandler
{
    private static boolean alreadyReadSystemBundles;
    private static final String[] FIELD_NAMES;
    
    private EquinoxClassLoaderHandler() {
    }
    
    public static boolean canHandle(final Class<?> classLoaderClass, final LogNode log) {
        return "org.eclipse.osgi.internal.loader.EquinoxClassLoader".equals(classLoaderClass.getName());
    }
    
    public static void findClassLoaderOrder(final ClassLoader classLoader, final ClassLoaderOrder classLoaderOrder, final LogNode log) {
        classLoaderOrder.delegateTo(classLoader.getParent(), true, log);
        classLoaderOrder.add(classLoader, log);
    }
    
    private static void addBundleFile(final Object bundlefile, final Set<Object> path, final ClassLoader classLoader, final ClasspathOrder classpathOrderOut, final ScanSpec scanSpec, final LogNode log) {
        if (bundlefile != null && path.add(bundlefile)) {
            final Object baseFile = ReflectionUtils.getFieldVal(bundlefile, "basefile", false);
            if (baseFile != null) {
                boolean foundClassPathElement = false;
                for (final String fieldName : EquinoxClassLoaderHandler.FIELD_NAMES) {
                    final Object fieldVal = ReflectionUtils.getFieldVal(bundlefile, fieldName, false);
                    if (fieldVal != null) {
                        foundClassPathElement = true;
                        Object base = baseFile;
                        String sep = "/";
                        if (bundlefile.getClass().getName().equals("org.eclipse.osgi.storage.bundlefile.NestedDirBundleFile")) {
                            final Object baseBundleFile = ReflectionUtils.getFieldVal(bundlefile, "baseBundleFile", false);
                            if (baseBundleFile != null && baseBundleFile.getClass().getName().equals("org.eclipse.osgi.storage.bundlefile.ZipBundleFile")) {
                                base = baseBundleFile;
                                sep = "!/";
                            }
                        }
                        final String pathElement = base.toString() + sep + fieldVal.toString();
                        classpathOrderOut.addClasspathEntry(pathElement, classLoader, scanSpec, log);
                        break;
                    }
                }
                if (!foundClassPathElement) {
                    classpathOrderOut.addClasspathEntry(baseFile.toString(), classLoader, scanSpec, log);
                }
            }
            addBundleFile(ReflectionUtils.getFieldVal(bundlefile, "wrapped", false), path, classLoader, classpathOrderOut, scanSpec, log);
            addBundleFile(ReflectionUtils.getFieldVal(bundlefile, "next", false), path, classLoader, classpathOrderOut, scanSpec, log);
        }
    }
    
    private static void addClasspathEntries(final Object owner, final ClassLoader classLoader, final ClasspathOrder classpathOrderOut, final ScanSpec scanSpec, final LogNode log) {
        final Object entries = ReflectionUtils.getFieldVal(owner, "entries", false);
        if (entries != null) {
            for (int i = 0, n = Array.getLength(entries); i < n; ++i) {
                final Object entry = Array.get(entries, i);
                final Object bundlefile = ReflectionUtils.getFieldVal(entry, "bundlefile", false);
                addBundleFile(bundlefile, new HashSet<Object>(), classLoader, classpathOrderOut, scanSpec, log);
            }
        }
    }
    
    public static void findClasspathOrder(final ClassLoader classLoader, final ClasspathOrder classpathOrder, final ScanSpec scanSpec, final LogNode log) {
        final Object manager = ReflectionUtils.getFieldVal(classLoader, "manager", false);
        addClasspathEntries(manager, classLoader, classpathOrder, scanSpec, log);
        final Object fragments = ReflectionUtils.getFieldVal(manager, "fragments", false);
        if (fragments != null) {
            for (int f = 0, fragLength = Array.getLength(fragments); f < fragLength; ++f) {
                final Object fragment = Array.get(fragments, f);
                addClasspathEntries(fragment, classLoader, classpathOrder, scanSpec, log);
            }
        }
        if (!EquinoxClassLoaderHandler.alreadyReadSystemBundles) {
            final Object delegate = ReflectionUtils.getFieldVal(classLoader, "delegate", false);
            final Object container = ReflectionUtils.getFieldVal(delegate, "container", false);
            final Object storage = ReflectionUtils.getFieldVal(container, "storage", false);
            final Object moduleContainer = ReflectionUtils.getFieldVal(storage, "moduleContainer", false);
            final Object moduleDatabase = ReflectionUtils.getFieldVal(moduleContainer, "moduleDatabase", false);
            final Object modulesById = ReflectionUtils.getFieldVal(moduleDatabase, "modulesById", false);
            final Object module0 = ReflectionUtils.invokeMethod(modulesById, "get", Object.class, 0L, false);
            final Object bundle = ReflectionUtils.invokeMethod(module0, "getBundle", false);
            final Object bundleContext = ReflectionUtils.invokeMethod(bundle, "getBundleContext", false);
            final Object bundles = ReflectionUtils.invokeMethod(bundleContext, "getBundles", false);
            if (bundles != null) {
                for (int i = 0, n = Array.getLength(bundles); i < n; ++i) {
                    final Object equinoxBundle = Array.get(bundles, i);
                    final Object module2 = ReflectionUtils.getFieldVal(equinoxBundle, "module", false);
                    String location = (String)ReflectionUtils.getFieldVal(module2, "location", false);
                    if (location != null) {
                        final int fileIdx = location.indexOf("file:");
                        if (fileIdx >= 0) {
                            location = location.substring(fileIdx);
                            classpathOrder.addClasspathEntry(location, classLoader, scanSpec, log);
                        }
                    }
                }
            }
            EquinoxClassLoaderHandler.alreadyReadSystemBundles = true;
        }
    }
    
    static {
        FIELD_NAMES = new String[] { "cp", "nestedDirName" };
    }
}
