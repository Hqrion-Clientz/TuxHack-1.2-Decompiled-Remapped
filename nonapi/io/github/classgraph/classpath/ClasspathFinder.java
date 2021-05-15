// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classpath;

import java.util.List;
import java.util.Set;
import java.util.Iterator;
import nonapi.io.github.classgraph.utils.FastPathResolver;
import nonapi.io.github.classgraph.utils.FileUtils;
import nonapi.io.github.classgraph.utils.JarUtils;
import java.util.Map;
import java.util.ArrayList;
import nonapi.io.github.classgraph.classloaderhandler.ClassLoaderHandlerRegistry;
import nonapi.io.github.classgraph.utils.LogNode;
import nonapi.io.github.classgraph.scanspec.ScanSpec;

public class ClasspathFinder
{
    private final ClasspathOrder classpathOrder;
    private final ModuleFinder moduleFinder;
    private ClassLoader[] classLoaderOrderRespectingParentDelegation;
    
    public ClasspathOrder getClasspathOrder() {
        return this.classpathOrder;
    }
    
    public ModuleFinder getModuleFinder() {
        return this.moduleFinder;
    }
    
    public ClassLoader[] getClassLoaderOrderRespectingParentDelegation() {
        return this.classLoaderOrderRespectingParentDelegation;
    }
    
    public ClasspathFinder(final ScanSpec scanSpec, final LogNode log) {
        final LogNode classpathFinderLog = (log == null) ? null : log.log("Finding classpath and modules");
        boolean scanModules;
        if (scanSpec.overrideClasspath != null) {
            scanModules = false;
        }
        else if (scanSpec.overrideClassLoaders != null) {
            scanModules = false;
            for (final ClassLoader classLoader : scanSpec.overrideClassLoaders) {
                final String classLoaderClassName = classLoader.getClass().getName();
                if (classLoaderClassName.equals("jdk.internal.loader.ClassLoaders$AppClassLoader")) {
                    scanModules = true;
                }
                else {
                    if (!classLoaderClassName.equals("jdk.internal.loader.ClassLoaders$PlatformClassLoader")) {
                        continue;
                    }
                    scanModules = true;
                    if (scanSpec.enableSystemJarsAndModules) {
                        continue;
                    }
                    if (classpathFinderLog != null) {
                        classpathFinderLog.log("overrideClassLoaders() was called with an instance of jdk.internal.loader.ClassLoaders$PlatformClassLoader, which is a system classloader, so enableSystemJarsAndModules() was called automatically");
                    }
                    scanSpec.enableSystemJarsAndModules = true;
                }
            }
        }
        else {
            scanModules = scanSpec.scanModules;
        }
        this.moduleFinder = (scanModules ? new ModuleFinder(CallStackReader.getClassContext(classpathFinderLog), scanSpec, classpathFinderLog) : null);
        this.classpathOrder = new ClasspathOrder(scanSpec);
        final ClassLoaderFinder classLoaderFinder = (scanSpec.overrideClasspath == null && scanSpec.overrideClassLoaders == null) ? new ClassLoaderFinder(scanSpec, classpathFinderLog) : null;
        final ClassLoader[] contextClassLoaders = (classLoaderFinder == null) ? new ClassLoader[0] : classLoaderFinder.getContextClassLoaders();
        final ClassLoader defaultClassLoader = (contextClassLoaders.length > 0) ? contextClassLoaders[0] : null;
        if (scanSpec.overrideClasspath != null) {
            if (scanSpec.overrideClassLoaders != null && classpathFinderLog != null) {
                classpathFinderLog.log("It is not possible to override both the classpath and the ClassLoaders -- ignoring the ClassLoader override");
            }
            final LogNode overrideLog = (classpathFinderLog == null) ? null : classpathFinderLog.log("Overriding classpath with: " + scanSpec.overrideClasspath);
            this.classpathOrder.addClasspathEntries(scanSpec.overrideClasspath, defaultClassLoader, scanSpec, overrideLog);
            if (overrideLog != null) {
                overrideLog.log("WARNING: when the classpath is overridden, there is no guarantee that the classes found by classpath scanning will be the same as the classes loaded by the context classloader");
            }
            this.classLoaderOrderRespectingParentDelegation = contextClassLoaders;
        }
        else if (scanSpec.overrideClassLoaders == null) {
            final String jreRtJar = SystemJarFinder.getJreRtJarPath();
            final LogNode systemJarsLog = (classpathFinderLog == null) ? null : classpathFinderLog.log("System jars:");
            if (jreRtJar != null) {
                if (scanSpec.enableSystemJarsAndModules) {
                    this.classpathOrder.addSystemClasspathEntry(jreRtJar, defaultClassLoader);
                    if (systemJarsLog != null) {
                        systemJarsLog.log("Found rt.jar: " + jreRtJar);
                    }
                }
                else if (systemJarsLog != null) {
                    systemJarsLog.log((scanSpec.enableSystemJarsAndModules ? "" : "Scanning disabled for rt.jar: ") + jreRtJar);
                }
            }
            final boolean scanAllLibOrExtJars = !scanSpec.libOrExtJarAcceptReject.acceptAndRejectAreEmpty();
            for (final String libOrExtJarPath : SystemJarFinder.getJreLibOrExtJars()) {
                if (scanAllLibOrExtJars || scanSpec.libOrExtJarAcceptReject.isSpecificallyAcceptedAndNotRejected(libOrExtJarPath)) {
                    this.classpathOrder.addSystemClasspathEntry(libOrExtJarPath, defaultClassLoader);
                    if (systemJarsLog == null) {
                        continue;
                    }
                    systemJarsLog.log("Found lib or ext jar: " + libOrExtJarPath);
                }
                else {
                    if (systemJarsLog == null) {
                        continue;
                    }
                    systemJarsLog.log("Scanning disabled for lib or ext jar: " + libOrExtJarPath);
                }
            }
        }
        if (scanSpec.overrideClasspath == null) {
            if (classpathFinderLog != null) {
                final LogNode classLoaderHandlerLog = classpathFinderLog.log("ClassLoaderHandlers:");
                for (final ClassLoaderHandlerRegistry.ClassLoaderHandlerRegistryEntry classLoaderHandlerEntry : ClassLoaderHandlerRegistry.CLASS_LOADER_HANDLERS) {
                    classLoaderHandlerLog.log(classLoaderHandlerEntry.classLoaderHandlerClass.getName());
                }
            }
            final LogNode classloaderOrderLog = (classpathFinderLog == null) ? null : classpathFinderLog.log("Finding unique classloaders in delegation order");
            final ClassLoaderOrder classLoaderOrder = new ClassLoaderOrder();
            final ClassLoader[] origClassLoaderOrder = (scanSpec.overrideClassLoaders != null) ? scanSpec.overrideClassLoaders.toArray(new ClassLoader[0]) : contextClassLoaders;
            if (origClassLoaderOrder != null) {
                for (final ClassLoader classLoader2 : origClassLoaderOrder) {
                    classLoaderOrder.delegateTo(classLoader2, false, classloaderOrderLog);
                }
            }
            final Set<ClassLoader> allParentClassLoaders = classLoaderOrder.getAllParentClassLoaders();
            final LogNode classloaderURLLog = (classpathFinderLog == null) ? null : classpathFinderLog.log("Obtaining URLs from classloaders in delegation order");
            final List<ClassLoader> finalClassLoaderOrder = new ArrayList<ClassLoader>();
            for (final Map.Entry<ClassLoader, ClassLoaderHandlerRegistry.ClassLoaderHandlerRegistryEntry> ent : classLoaderOrder.getClassLoaderOrder()) {
                final ClassLoader classLoader3 = ent.getKey();
                final ClassLoaderHandlerRegistry.ClassLoaderHandlerRegistryEntry classLoaderHandlerRegistryEntry = ent.getValue();
                if (!scanSpec.ignoreParentClassLoaders || !allParentClassLoaders.contains(classLoader3)) {
                    final LogNode classloaderHandlerLog = (classloaderURLLog == null) ? null : classloaderURLLog.log("Classloader " + classLoader3 + " is handled by " + classLoaderHandlerRegistryEntry.classLoaderHandlerClass.getName());
                    classLoaderHandlerRegistryEntry.findClasspathOrder(classLoader3, this.classpathOrder, scanSpec, classloaderHandlerLog);
                    finalClassLoaderOrder.add(classLoader3);
                }
                else {
                    if (classloaderURLLog == null) {
                        continue;
                    }
                    classloaderURLLog.log("Ignoring parent classloader " + classLoader3 + ", normally handled by " + classLoaderHandlerRegistryEntry.classLoaderHandlerClass.getName());
                }
            }
            this.classLoaderOrderRespectingParentDelegation = finalClassLoaderOrder.toArray(new ClassLoader[0]);
        }
        if ((!scanSpec.ignoreParentClassLoaders && scanSpec.overrideClassLoaders == null && scanSpec.overrideClasspath == null) || (this.moduleFinder != null && this.moduleFinder.forceScanJavaClassPath())) {
            final String[] pathElements = JarUtils.smartPathSplit(System.getProperty("java.class.path"), scanSpec);
            if (pathElements.length > 0) {
                final LogNode sysPropLog = (classpathFinderLog == null) ? null : classpathFinderLog.log("Getting classpath entries from java.class.path");
                for (final String pathElement : pathElements) {
                    final String pathElementResolved = FastPathResolver.resolve(FileUtils.CURR_DIR_PATH, pathElement);
                    this.classpathOrder.addClasspathEntry(pathElementResolved, defaultClassLoader, scanSpec, sysPropLog);
                }
            }
        }
    }
}
