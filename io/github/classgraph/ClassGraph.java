// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.net.URL;
import java.net.URI;
import java.io.File;
import java.util.List;
import nonapi.io.github.classgraph.concurrency.AutoCloseableExecutorService;
import nonapi.io.github.classgraph.concurrency.InterruptionChecker;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import nonapi.io.github.classgraph.classpath.SystemJarFinder;
import nonapi.io.github.classgraph.scanspec.AcceptReject;
import java.util.Iterator;
import nonapi.io.github.classgraph.utils.JarUtils;
import nonapi.io.github.classgraph.utils.VersionFinder;
import nonapi.io.github.classgraph.utils.LogNode;
import nonapi.io.github.classgraph.scanspec.ScanSpec;

public class ClassGraph
{
    ScanSpec scanSpec;
    static final int DEFAULT_NUM_WORKER_THREADS;
    private LogNode topLevelLog;
    
    public ClassGraph() {
        this.scanSpec = new ScanSpec();
        ScanResult.init();
    }
    
    public static String getVersion() {
        return VersionFinder.getVersion();
    }
    
    public ClassGraph verbose() {
        if (this.topLevelLog == null) {
            this.topLevelLog = new LogNode();
        }
        return this;
    }
    
    public ClassGraph verbose(final boolean verbose) {
        if (verbose) {
            this.verbose();
        }
        return this;
    }
    
    public ClassGraph enableAllInfo() {
        this.enableClassInfo();
        this.enableFieldInfo();
        this.enableMethodInfo();
        this.enableAnnotationInfo();
        this.enableStaticFinalFieldConstantInitializerValues();
        this.ignoreClassVisibility();
        this.ignoreFieldVisibility();
        this.ignoreMethodVisibility();
        return this;
    }
    
    public ClassGraph enableClassInfo() {
        this.scanSpec.enableClassInfo = true;
        return this;
    }
    
    public ClassGraph ignoreClassVisibility() {
        this.enableClassInfo();
        this.scanSpec.ignoreClassVisibility = true;
        return this;
    }
    
    public ClassGraph enableMethodInfo() {
        this.enableClassInfo();
        this.scanSpec.enableMethodInfo = true;
        return this;
    }
    
    public ClassGraph ignoreMethodVisibility() {
        this.enableClassInfo();
        this.enableMethodInfo();
        this.scanSpec.ignoreMethodVisibility = true;
        return this;
    }
    
    public ClassGraph enableFieldInfo() {
        this.enableClassInfo();
        this.scanSpec.enableFieldInfo = true;
        return this;
    }
    
    public ClassGraph ignoreFieldVisibility() {
        this.enableClassInfo();
        this.enableFieldInfo();
        this.scanSpec.ignoreFieldVisibility = true;
        return this;
    }
    
    public ClassGraph enableStaticFinalFieldConstantInitializerValues() {
        this.enableClassInfo();
        this.enableFieldInfo();
        this.scanSpec.enableStaticFinalFieldConstantInitializerValues = true;
        return this;
    }
    
    public ClassGraph enableAnnotationInfo() {
        this.enableClassInfo();
        this.scanSpec.enableAnnotationInfo = true;
        return this;
    }
    
    public ClassGraph enableInterClassDependencies() {
        this.enableClassInfo();
        this.enableFieldInfo();
        this.enableMethodInfo();
        this.enableAnnotationInfo();
        this.ignoreClassVisibility();
        this.ignoreFieldVisibility();
        this.ignoreMethodVisibility();
        this.scanSpec.enableInterClassDependencies = true;
        return this;
    }
    
    public ClassGraph disableRuntimeInvisibleAnnotations() {
        this.enableClassInfo();
        this.scanSpec.disableRuntimeInvisibleAnnotations = true;
        return this;
    }
    
    public ClassGraph disableJarScanning() {
        this.scanSpec.scanJars = false;
        return this;
    }
    
    public ClassGraph disableNestedJarScanning() {
        this.scanSpec.scanNestedJars = false;
        return this;
    }
    
    public ClassGraph disableDirScanning() {
        this.scanSpec.scanDirs = false;
        return this;
    }
    
    public ClassGraph disableModuleScanning() {
        this.scanSpec.scanModules = false;
        return this;
    }
    
    public ClassGraph enableExternalClasses() {
        this.enableClassInfo();
        this.scanSpec.enableExternalClasses = true;
        return this;
    }
    
    public ClassGraph initializeLoadedClasses() {
        this.scanSpec.initializeLoadedClasses = true;
        return this;
    }
    
    public ClassGraph removeTemporaryFilesAfterScan() {
        this.scanSpec.removeTemporaryFilesAfterScan = true;
        return this;
    }
    
    public ClassGraph overrideClasspath(final String overrideClasspath) {
        if (overrideClasspath.isEmpty()) {
            throw new IllegalArgumentException("Can't override classpath with an empty path");
        }
        for (final String classpathElement : JarUtils.smartPathSplit(overrideClasspath, this.scanSpec)) {
            this.scanSpec.addClasspathOverride(classpathElement);
        }
        return this;
    }
    
    public ClassGraph overrideClasspath(final Iterable<?> overrideClasspathElements) {
        if (!overrideClasspathElements.iterator().hasNext()) {
            throw new IllegalArgumentException("Can't override classpath with an empty path");
        }
        for (final Object classpathElement : overrideClasspathElements) {
            this.scanSpec.addClasspathOverride(classpathElement);
        }
        return this;
    }
    
    public ClassGraph overrideClasspath(final Object... overrideClasspathElements) {
        if (overrideClasspathElements.length == 0) {
            throw new IllegalArgumentException("Can't override classpath with an empty path");
        }
        for (final Object classpathElement : overrideClasspathElements) {
            this.scanSpec.addClasspathOverride(classpathElement);
        }
        return this;
    }
    
    public ClassGraph filterClasspathElements(final ClasspathElementFilter classpathElementFilter) {
        this.scanSpec.filterClasspathElements(classpathElementFilter);
        return this;
    }
    
    public ClassGraph addClassLoader(final ClassLoader classLoader) {
        this.scanSpec.addClassLoader(classLoader);
        return this;
    }
    
    public ClassGraph overrideClassLoaders(final ClassLoader... overrideClassLoaders) {
        this.scanSpec.overrideClassLoaders(overrideClassLoaders);
        return this;
    }
    
    public ClassGraph ignoreParentClassLoaders() {
        this.scanSpec.ignoreParentClassLoaders = true;
        return this;
    }
    
    public ClassGraph addModuleLayer(final Object moduleLayer) {
        this.scanSpec.addModuleLayer(moduleLayer);
        return this;
    }
    
    public ClassGraph overrideModuleLayers(final Object... overrideModuleLayers) {
        this.scanSpec.overrideModuleLayers(overrideModuleLayers);
        return this;
    }
    
    public ClassGraph ignoreParentModuleLayers() {
        this.scanSpec.ignoreParentModuleLayers = true;
        return this;
    }
    
    public ClassGraph acceptPackages(final String... packageNames) {
        this.enableClassInfo();
        for (final String packageName : packageNames) {
            final String packageNameNormalized = AcceptReject.normalizePackageOrClassName(packageName);
            if (packageNameNormalized.startsWith("!") || packageNameNormalized.startsWith("-")) {
                throw new IllegalArgumentException("This style of accepting/rejecting is no longer supported: " + packageNameNormalized);
            }
            this.scanSpec.packageAcceptReject.addToAccept(packageNameNormalized);
            final String path = AcceptReject.packageNameToPath(packageNameNormalized);
            this.scanSpec.pathAcceptReject.addToAccept(path + "/");
            if (packageNameNormalized.isEmpty()) {
                this.scanSpec.pathAcceptReject.addToAccept("");
            }
            if (!packageNameNormalized.contains("*")) {
                if (packageNameNormalized.isEmpty()) {
                    this.scanSpec.packagePrefixAcceptReject.addToAccept("");
                    this.scanSpec.pathPrefixAcceptReject.addToAccept("");
                }
                else {
                    this.scanSpec.packagePrefixAcceptReject.addToAccept(packageNameNormalized + ".");
                    this.scanSpec.pathPrefixAcceptReject.addToAccept(path + "/");
                }
            }
        }
        return this;
    }
    
    @Deprecated
    public ClassGraph whitelistPackages(final String... packageNames) {
        return this.acceptPackages(packageNames);
    }
    
    public ClassGraph acceptPaths(final String... paths) {
        for (final String path : paths) {
            final String pathNormalized = AcceptReject.normalizePath(path);
            final String packageName = AcceptReject.pathToPackageName(pathNormalized);
            this.scanSpec.packageAcceptReject.addToAccept(packageName);
            this.scanSpec.pathAcceptReject.addToAccept(pathNormalized + "/");
            if (pathNormalized.isEmpty()) {
                this.scanSpec.pathAcceptReject.addToAccept("");
            }
            if (!pathNormalized.contains("*")) {
                if (pathNormalized.isEmpty()) {
                    this.scanSpec.packagePrefixAcceptReject.addToAccept("");
                    this.scanSpec.pathPrefixAcceptReject.addToAccept("");
                }
                else {
                    this.scanSpec.packagePrefixAcceptReject.addToAccept(packageName + ".");
                    this.scanSpec.pathPrefixAcceptReject.addToAccept(pathNormalized + "/");
                }
            }
        }
        return this;
    }
    
    @Deprecated
    public ClassGraph whitelistPaths(final String... paths) {
        return this.acceptPaths(paths);
    }
    
    public ClassGraph acceptPackagesNonRecursive(final String... packageNames) {
        this.enableClassInfo();
        for (final String packageName : packageNames) {
            final String packageNameNormalized = AcceptReject.normalizePackageOrClassName(packageName);
            if (packageNameNormalized.contains("*")) {
                throw new IllegalArgumentException("Cannot use a glob wildcard here: " + packageNameNormalized);
            }
            this.scanSpec.packageAcceptReject.addToAccept(packageNameNormalized);
            this.scanSpec.pathAcceptReject.addToAccept(AcceptReject.packageNameToPath(packageNameNormalized) + "/");
            if (packageNameNormalized.isEmpty()) {
                this.scanSpec.pathAcceptReject.addToAccept("");
            }
        }
        return this;
    }
    
    @Deprecated
    public ClassGraph whitelistPackagesNonRecursive(final String... packageNames) {
        return this.acceptPackagesNonRecursive(packageNames);
    }
    
    public ClassGraph acceptPathsNonRecursive(final String... paths) {
        for (final String path : paths) {
            if (path.contains("*")) {
                throw new IllegalArgumentException("Cannot use a glob wildcard here: " + path);
            }
            final String pathNormalized = AcceptReject.normalizePath(path);
            this.scanSpec.packageAcceptReject.addToAccept(AcceptReject.pathToPackageName(pathNormalized));
            this.scanSpec.pathAcceptReject.addToAccept(pathNormalized + "/");
            if (pathNormalized.isEmpty()) {
                this.scanSpec.pathAcceptReject.addToAccept("");
            }
        }
        return this;
    }
    
    @Deprecated
    public ClassGraph whitelistPathsNonRecursive(final String... paths) {
        return this.acceptPathsNonRecursive(paths);
    }
    
    public ClassGraph rejectPackages(final String... packageNames) {
        this.enableClassInfo();
        for (final String packageName : packageNames) {
            final String packageNameNormalized = AcceptReject.normalizePackageOrClassName(packageName);
            if (packageNameNormalized.isEmpty()) {
                throw new IllegalArgumentException("Rejecting the root package (\"\") will cause nothing to be scanned");
            }
            this.scanSpec.packageAcceptReject.addToReject(packageNameNormalized);
            final String path = AcceptReject.packageNameToPath(packageNameNormalized);
            this.scanSpec.pathAcceptReject.addToReject(path + "/");
            if (!packageNameNormalized.contains("*")) {
                this.scanSpec.packagePrefixAcceptReject.addToReject(packageNameNormalized + ".");
                this.scanSpec.pathPrefixAcceptReject.addToReject(path + "/");
            }
        }
        return this;
    }
    
    @Deprecated
    public ClassGraph blacklistPackages(final String... packageNames) {
        return this.rejectPackages(packageNames);
    }
    
    public ClassGraph rejectPaths(final String... paths) {
        for (final String path : paths) {
            final String pathNormalized = AcceptReject.normalizePath(path);
            if (pathNormalized.isEmpty()) {
                throw new IllegalArgumentException("Rejecting the root package (\"\") will cause nothing to be scanned");
            }
            final String packageName = AcceptReject.pathToPackageName(pathNormalized);
            this.scanSpec.packageAcceptReject.addToReject(packageName);
            this.scanSpec.pathAcceptReject.addToReject(pathNormalized + "/");
            if (!pathNormalized.contains("*")) {
                this.scanSpec.packagePrefixAcceptReject.addToReject(packageName + ".");
                this.scanSpec.pathPrefixAcceptReject.addToReject(pathNormalized + "/");
            }
        }
        return this;
    }
    
    @Deprecated
    public ClassGraph blacklistPaths(final String... paths) {
        return this.rejectPaths(paths);
    }
    
    public ClassGraph acceptClasses(final String... classNames) {
        this.enableClassInfo();
        for (final String className : classNames) {
            if (className.contains("*")) {
                throw new IllegalArgumentException("Cannot use a glob wildcard here: " + className);
            }
            final String classNameNormalized = AcceptReject.normalizePackageOrClassName(className);
            this.scanSpec.classAcceptReject.addToAccept(classNameNormalized);
            this.scanSpec.classfilePathAcceptReject.addToAccept(AcceptReject.classNameToClassfilePath(classNameNormalized));
            final String packageName = PackageInfo.getParentPackageName(classNameNormalized);
            this.scanSpec.classPackageAcceptReject.addToAccept(packageName);
            this.scanSpec.classPackagePathAcceptReject.addToAccept(AcceptReject.packageNameToPath(packageName) + "/");
        }
        return this;
    }
    
    @Deprecated
    public ClassGraph whitelistClasses(final String... classNames) {
        return this.acceptClasses(classNames);
    }
    
    public ClassGraph rejectClasses(final String... classNames) {
        this.enableClassInfo();
        for (final String className : classNames) {
            if (className.contains("*")) {
                throw new IllegalArgumentException("Cannot use a glob wildcard here: " + className);
            }
            final String classNameNormalized = AcceptReject.normalizePackageOrClassName(className);
            this.scanSpec.classAcceptReject.addToReject(classNameNormalized);
            this.scanSpec.classfilePathAcceptReject.addToReject(AcceptReject.classNameToClassfilePath(classNameNormalized));
        }
        return this;
    }
    
    @Deprecated
    public ClassGraph blacklistClasses(final String... classNames) {
        return this.rejectClasses(classNames);
    }
    
    public ClassGraph acceptJars(final String... jarLeafNames) {
        for (final String jarLeafName : jarLeafNames) {
            final String leafName = JarUtils.leafName(jarLeafName);
            if (!leafName.equals(jarLeafName)) {
                throw new IllegalArgumentException("Can only accept jars by leafname: " + jarLeafName);
            }
            this.scanSpec.jarAcceptReject.addToAccept(leafName);
        }
        return this;
    }
    
    @Deprecated
    public ClassGraph whitelistJars(final String... jarLeafNames) {
        return this.acceptJars(jarLeafNames);
    }
    
    public ClassGraph rejectJars(final String... jarLeafNames) {
        for (final String jarLeafName : jarLeafNames) {
            final String leafName = JarUtils.leafName(jarLeafName);
            if (!leafName.equals(jarLeafName)) {
                throw new IllegalArgumentException("Can only reject jars by leafname: " + jarLeafName);
            }
            this.scanSpec.jarAcceptReject.addToReject(leafName);
        }
        return this;
    }
    
    @Deprecated
    public ClassGraph blacklistJars(final String... jarLeafNames) {
        return this.rejectJars(jarLeafNames);
    }
    
    private void acceptOrRejectLibOrExtJars(final boolean accept, final String... jarLeafNames) {
        if (jarLeafNames.length == 0) {
            for (final String libOrExtJar : SystemJarFinder.getJreLibOrExtJars()) {
                this.acceptOrRejectLibOrExtJars(accept, JarUtils.leafName(libOrExtJar));
            }
        }
        else {
            for (final String jarLeafName : jarLeafNames) {
                final String leafName = JarUtils.leafName(jarLeafName);
                if (!leafName.equals(jarLeafName)) {
                    throw new IllegalArgumentException("Can only " + (accept ? "accept" : "reject") + " jars by leafname: " + jarLeafName);
                }
                if (jarLeafName.contains("*")) {
                    final Pattern pattern = AcceptReject.globToPattern(jarLeafName);
                    boolean found = false;
                    for (final String libOrExtJarPath : SystemJarFinder.getJreLibOrExtJars()) {
                        final String libOrExtJarLeafName = JarUtils.leafName(libOrExtJarPath);
                        if (pattern.matcher(libOrExtJarLeafName).matches()) {
                            if (!libOrExtJarLeafName.contains("*")) {
                                this.acceptOrRejectLibOrExtJars(accept, libOrExtJarLeafName);
                            }
                            found = true;
                        }
                    }
                    if (!found && this.topLevelLog != null) {
                        this.topLevelLog.log("Could not find lib or ext jar matching wildcard: " + jarLeafName);
                    }
                }
                else {
                    boolean found2 = false;
                    for (final String libOrExtJarPath2 : SystemJarFinder.getJreLibOrExtJars()) {
                        final String libOrExtJarLeafName2 = JarUtils.leafName(libOrExtJarPath2);
                        if (jarLeafName.equals(libOrExtJarLeafName2)) {
                            if (accept) {
                                this.scanSpec.libOrExtJarAcceptReject.addToAccept(jarLeafName);
                            }
                            else {
                                this.scanSpec.libOrExtJarAcceptReject.addToReject(jarLeafName);
                            }
                            if (this.topLevelLog != null) {
                                this.topLevelLog.log((accept ? "Accepting" : "Rejecting") + " lib or ext jar: " + libOrExtJarPath2);
                            }
                            found2 = true;
                            break;
                        }
                    }
                    if (!found2 && this.topLevelLog != null) {
                        this.topLevelLog.log("Could not find lib or ext jar: " + jarLeafName);
                    }
                }
            }
        }
    }
    
    public ClassGraph acceptLibOrExtJars(final String... jarLeafNames) {
        this.acceptOrRejectLibOrExtJars(true, jarLeafNames);
        return this;
    }
    
    @Deprecated
    public ClassGraph whitelistLibOrExtJars(final String... jarLeafNames) {
        return this.acceptLibOrExtJars(jarLeafNames);
    }
    
    public ClassGraph rejectLibOrExtJars(final String... jarLeafNames) {
        this.acceptOrRejectLibOrExtJars(false, jarLeafNames);
        return this;
    }
    
    @Deprecated
    public ClassGraph blacklistLibOrExtJars(final String... jarLeafNames) {
        return this.rejectLibOrExtJars(jarLeafNames);
    }
    
    public ClassGraph acceptModules(final String... moduleNames) {
        for (final String moduleName : moduleNames) {
            this.scanSpec.moduleAcceptReject.addToAccept(AcceptReject.normalizePackageOrClassName(moduleName));
        }
        return this;
    }
    
    @Deprecated
    public ClassGraph whitelistModules(final String... moduleNames) {
        return this.acceptModules(moduleNames);
    }
    
    public ClassGraph rejectModules(final String... moduleNames) {
        for (final String moduleName : moduleNames) {
            this.scanSpec.moduleAcceptReject.addToReject(AcceptReject.normalizePackageOrClassName(moduleName));
        }
        return this;
    }
    
    @Deprecated
    public ClassGraph blacklistModules(final String... moduleNames) {
        return this.rejectModules(moduleNames);
    }
    
    public ClassGraph acceptClasspathElementsContainingResourcePath(final String... resourcePaths) {
        for (final String resourcePath : resourcePaths) {
            final String resourcePathNormalized = AcceptReject.normalizePath(resourcePath);
            this.scanSpec.classpathElementResourcePathAcceptReject.addToAccept(resourcePathNormalized);
        }
        return this;
    }
    
    @Deprecated
    public ClassGraph whitelistClasspathElementsContainingResourcePath(final String... resourcePaths) {
        return this.acceptClasspathElementsContainingResourcePath(resourcePaths);
    }
    
    public ClassGraph rejectClasspathElementsContainingResourcePath(final String... resourcePaths) {
        for (final String resourcePath : resourcePaths) {
            final String resourcePathNormalized = AcceptReject.normalizePath(resourcePath);
            this.scanSpec.classpathElementResourcePathAcceptReject.addToReject(resourcePathNormalized);
        }
        return this;
    }
    
    @Deprecated
    public ClassGraph blacklistClasspathElementsContainingResourcePath(final String... resourcePaths) {
        return this.rejectClasspathElementsContainingResourcePath(resourcePaths);
    }
    
    public ClassGraph enableRemoteJarScanning() {
        this.scanSpec.enableURLScheme("http");
        this.scanSpec.enableURLScheme("https");
        return this;
    }
    
    public ClassGraph enableURLScheme(final String scheme) {
        this.scanSpec.enableURLScheme(scheme);
        return this;
    }
    
    public ClassGraph enableSystemJarsAndModules() {
        this.enableClassInfo();
        this.scanSpec.enableSystemJarsAndModules = true;
        return this;
    }
    
    public ClassGraph setMaxBufferedJarRAMSize(final int maxBufferedJarRAMSize) {
        this.scanSpec.maxBufferedJarRAMSize = maxBufferedJarRAMSize;
        return this;
    }
    
    public ClassGraph enableMemoryMapping() {
        this.scanSpec.enableMemoryMapping = true;
        return this;
    }
    
    public ClassGraph enableRealtimeLogging() {
        this.verbose();
        LogNode.logInRealtime(true);
        return this;
    }
    
    public void scanAsync(final ExecutorService executorService, final int numParallelTasks, final ScanResultProcessor scanResultProcessor, final FailureHandler failureHandler) {
        if (scanResultProcessor == null) {
            throw new IllegalArgumentException("scanResultProcessor cannot be null");
        }
        if (failureHandler == null) {
            throw new IllegalArgumentException("failureHandler cannot be null");
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    new Scanner(true, ClassGraph.this.scanSpec, executorService, numParallelTasks, scanResultProcessor, failureHandler, ClassGraph.this.topLevelLog).call();
                }
                catch (InterruptedException | CancellationException | ExecutionException ex2) {
                    final Exception ex;
                    final Exception e = ex;
                    failureHandler.onFailure(e);
                }
            }
        });
    }
    
    private Future<ScanResult> scanAsync(final boolean performScan, final ExecutorService executorService, final int numParallelTasks) {
        try {
            return executorService.submit((Callable<ScanResult>)new Scanner(performScan, this.scanSpec, executorService, numParallelTasks, null, null, this.topLevelLog));
        }
        catch (InterruptedException e) {
            return executorService.submit((Callable<ScanResult>)new Callable<ScanResult>() {
                @Override
                public ScanResult call() throws Exception {
                    throw e;
                }
            });
        }
    }
    
    public Future<ScanResult> scanAsync(final ExecutorService executorService, final int numParallelTasks) {
        return this.scanAsync(true, executorService, numParallelTasks);
    }
    
    public ScanResult scan(final ExecutorService executorService, final int numParallelTasks) {
        try {
            final ScanResult scanResult = this.scanAsync(executorService, numParallelTasks).get();
            if (scanResult == null) {
                throw new NullPointerException();
            }
            return scanResult;
        }
        catch (InterruptedException | CancellationException ex2) {
            final Exception ex;
            final Exception e = ex;
            throw ClassGraphException.newClassGraphException("Scan interrupted", e);
        }
        catch (ExecutionException e2) {
            throw ClassGraphException.newClassGraphException("Uncaught exception during scan", InterruptionChecker.getCause(e2));
        }
    }
    
    public ScanResult scan(final int numThreads) {
        try (final AutoCloseableExecutorService executorService = new AutoCloseableExecutorService(numThreads)) {
            return this.scan(executorService, numThreads);
        }
    }
    
    public ScanResult scan() {
        return this.scan(ClassGraph.DEFAULT_NUM_WORKER_THREADS);
    }
    
    ScanResult getClasspathScanResult(final AutoCloseableExecutorService executorService) {
        try {
            final ScanResult scanResult = this.scanAsync(false, executorService, ClassGraph.DEFAULT_NUM_WORKER_THREADS).get();
            if (scanResult == null) {
                throw new NullPointerException();
            }
            return scanResult;
        }
        catch (InterruptedException | CancellationException ex2) {
            final Exception ex;
            final Exception e = ex;
            throw ClassGraphException.newClassGraphException("Scan interrupted", e);
        }
        catch (ExecutionException e2) {
            throw ClassGraphException.newClassGraphException("Uncaught exception during scan", InterruptionChecker.getCause(e2));
        }
    }
    
    public List<File> getClasspathFiles() {
        try (final AutoCloseableExecutorService executorService = new AutoCloseableExecutorService(ClassGraph.DEFAULT_NUM_WORKER_THREADS);
             final ScanResult scanResult = this.getClasspathScanResult(executorService)) {
            return scanResult.getClasspathFiles();
        }
    }
    
    public String getClasspath() {
        return JarUtils.pathElementsToPathStr(this.getClasspathFiles());
    }
    
    public List<URI> getClasspathURIs() {
        try (final AutoCloseableExecutorService executorService = new AutoCloseableExecutorService(ClassGraph.DEFAULT_NUM_WORKER_THREADS);
             final ScanResult scanResult = this.getClasspathScanResult(executorService)) {
            return scanResult.getClasspathURIs();
        }
    }
    
    public List<URL> getClasspathURLs() {
        try (final AutoCloseableExecutorService executorService = new AutoCloseableExecutorService(ClassGraph.DEFAULT_NUM_WORKER_THREADS);
             final ScanResult scanResult = this.getClasspathScanResult(executorService)) {
            return scanResult.getClasspathURLs();
        }
    }
    
    public List<ModuleRef> getModules() {
        try (final AutoCloseableExecutorService executorService = new AutoCloseableExecutorService(ClassGraph.DEFAULT_NUM_WORKER_THREADS);
             final ScanResult scanResult = this.getClasspathScanResult(executorService)) {
            return scanResult.getModules();
        }
    }
    
    public ModulePathInfo getModulePathInfo() {
        return this.scanSpec.modulePathInfo;
    }
    
    static {
        DEFAULT_NUM_WORKER_THREADS = Math.max(2, (int)Math.ceil(Math.min(4.0, Runtime.getRuntime().availableProcessors() * 0.75) + Runtime.getRuntime().availableProcessors() * 1.25));
    }
    
    @FunctionalInterface
    public interface FailureHandler
    {
        void onFailure(final Throwable p0);
    }
    
    @FunctionalInterface
    public interface ScanResultProcessor
    {
        void processScanResult(final ScanResult p0);
    }
    
    @FunctionalInterface
    public interface ClasspathElementFilter
    {
        boolean includeClasspathElement(final String p0);
    }
}
