// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classloaderhandler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.lang.reflect.Array;
import java.util.Set;
import java.nio.file.Path;
import nonapi.io.github.classgraph.utils.FileUtils;
import java.io.File;
import nonapi.io.github.classgraph.utils.ReflectionUtils;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.classpath.ClasspathOrder;
import nonapi.io.github.classgraph.classpath.ClassLoaderOrder;
import nonapi.io.github.classgraph.utils.LogNode;

class JBossClassLoaderHandler implements ClassLoaderHandler
{
    private JBossClassLoaderHandler() {
    }
    
    public static boolean canHandle(final Class<?> classLoaderClass, final LogNode log) {
        return "org.jboss.modules.ModuleClassLoader".equals(classLoaderClass.getName());
    }
    
    public static void findClassLoaderOrder(final ClassLoader classLoader, final ClassLoaderOrder classLoaderOrder, final LogNode log) {
        classLoaderOrder.delegateTo(classLoader.getParent(), true, log);
        classLoaderOrder.add(classLoader, log);
    }
    
    private static void handleResourceLoader(final Object resourceLoader, final ClassLoader classLoader, final ClasspathOrder classpathOrderOut, final ScanSpec scanSpec, final LogNode log) {
        if (resourceLoader == null) {
            return;
        }
        final Object root = ReflectionUtils.getFieldVal(resourceLoader, "root", false);
        final File physicalFile = (File)ReflectionUtils.invokeMethod(root, "getPhysicalFile", false);
        String path = null;
        if (physicalFile != null) {
            final String name = (String)ReflectionUtils.invokeMethod(root, "getName", false);
            if (name != null) {
                final File file = new File(physicalFile.getParentFile(), name);
                if (FileUtils.canRead(file)) {
                    path = file.getAbsolutePath();
                }
                else {
                    path = physicalFile.getAbsolutePath();
                }
            }
            else {
                path = physicalFile.getAbsolutePath();
            }
        }
        else {
            path = (String)ReflectionUtils.invokeMethod(root, "getPathName", false);
            if (path == null) {
                final File file2 = (root instanceof Path) ? ((Path)root).toFile() : ((root instanceof File) ? ((File)root) : null);
                if (file2 != null) {
                    path = file2.getAbsolutePath();
                }
            }
        }
        if (path == null) {
            final File file2 = (File)ReflectionUtils.getFieldVal(resourceLoader, "fileOfJar", false);
            if (file2 != null) {
                path = file2.getAbsolutePath();
            }
        }
        if (path != null) {
            classpathOrderOut.addClasspathEntry(path, classLoader, scanSpec, log);
        }
        else if (log != null) {
            log.log("Could not determine classpath for ResourceLoader: " + resourceLoader);
        }
    }
    
    private static void handleRealModule(final Object module, final Set<Object> visitedModules, final ClassLoader classLoader, final ClasspathOrder classpathOrderOut, final ScanSpec scanSpec, final LogNode log) {
        if (!visitedModules.add(module)) {
            return;
        }
        ClassLoader moduleLoader = (ClassLoader)ReflectionUtils.invokeMethod(module, "getClassLoader", false);
        if (moduleLoader == null) {
            moduleLoader = classLoader;
        }
        final Object vfsResourceLoaders = ReflectionUtils.invokeMethod(moduleLoader, "getResourceLoaders", false);
        if (vfsResourceLoaders != null) {
            for (int i = 0, n = Array.getLength(vfsResourceLoaders); i < n; ++i) {
                final Object resourceLoader = Array.get(vfsResourceLoaders, i);
                handleResourceLoader(resourceLoader, moduleLoader, classpathOrderOut, scanSpec, log);
            }
        }
    }
    
    public static void findClasspathOrder(final ClassLoader classLoader, final ClasspathOrder classpathOrder, final ScanSpec scanSpec, final LogNode log) {
        final Object module = ReflectionUtils.invokeMethod(classLoader, "getModule", false);
        final Object callerModuleLoader = ReflectionUtils.invokeMethod(module, "getCallerModuleLoader", false);
        final Set<Object> visitedModules = new HashSet<Object>();
        final Map<Object, Object> moduleMap = (Map<Object, Object>)ReflectionUtils.getFieldVal(callerModuleLoader, "moduleMap", false);
        for (final Map.Entry<Object, Object> ent : moduleMap.entrySet()) {
            final Object val = ent.getValue();
            final Object realModule = ReflectionUtils.invokeMethod(val, "getModule", false);
            handleRealModule(realModule, visitedModules, classLoader, classpathOrder, scanSpec, log);
        }
        final Map<String, List<?>> pathsMap = (Map<String, List<?>>)ReflectionUtils.invokeMethod(module, "getPaths", false);
        for (final Map.Entry<String, List<?>> ent2 : pathsMap.entrySet()) {
            for (final Object localLoader : ent2.getValue()) {
                final Object moduleClassLoader = ReflectionUtils.getFieldVal(localLoader, "this$0", false);
                final Object realModule2 = ReflectionUtils.getFieldVal(moduleClassLoader, "module", false);
                handleRealModule(realModule2, visitedModules, classLoader, classpathOrder, scanSpec, log);
            }
        }
    }
}
