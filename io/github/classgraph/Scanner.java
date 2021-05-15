// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.concurrent.CancellationException;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.AbstractMap;
import java.util.concurrent.ExecutionException;
import java.util.HashSet;
import java.util.Queue;
import nonapi.io.github.classgraph.utils.CollectionUtils;
import java.util.Collection;
import java.util.Set;
import java.util.Iterator;
import nonapi.io.github.classgraph.classpath.ModuleFinder;
import nonapi.io.github.classgraph.concurrency.WorkQueue;
import java.util.ArrayList;
import nonapi.io.github.classgraph.concurrency.AutoCloseableExecutorService;
import java.io.FileNotFoundException;
import java.io.File;
import nonapi.io.github.classgraph.utils.FastPathResolver;
import nonapi.io.github.classgraph.utils.FileUtils;
import java.nio.file.Path;
import java.nio.file.FileSystemNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.net.URLDecoder;
import java.net.MalformedURLException;
import java.net.URL;
import nonapi.io.github.classgraph.utils.JarUtils;
import java.io.IOException;
import nonapi.io.github.classgraph.classpath.ClasspathOrder;
import nonapi.io.github.classgraph.concurrency.SingletonMap;
import java.util.Map;
import java.util.Comparator;
import java.util.List;
import nonapi.io.github.classgraph.classpath.ClasspathFinder;
import nonapi.io.github.classgraph.utils.LogNode;
import nonapi.io.github.classgraph.concurrency.InterruptionChecker;
import java.util.concurrent.ExecutorService;
import nonapi.io.github.classgraph.fastzipfilereader.NestedJarHandler;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import java.util.concurrent.Callable;

class Scanner implements Callable<ScanResult>
{
    private final ScanSpec scanSpec;
    public boolean performScan;
    private final NestedJarHandler nestedJarHandler;
    private final ExecutorService executorService;
    private final InterruptionChecker interruptionChecker;
    private final int numParallelTasks;
    private final ClassGraph.ScanResultProcessor scanResultProcessor;
    private final ClassGraph.FailureHandler failureHandler;
    private final LogNode topLevelLog;
    private final ClasspathFinder classpathFinder;
    private final List<ClasspathElementModule> moduleOrder;
    private final ClassLoader[] classLoaderOrderRespectingParentDelegation;
    private static final Comparator<Map.Entry<Integer, ClasspathElement>> INDEXED_CLASSPATH_ELEMENT_COMPARATOR;
    private final SingletonMap<ClasspathOrder.ClasspathElementAndClassLoader, ClasspathElement, IOException> classpathEntryToClasspathElementSingletonMap;
    
    Scanner(final boolean performScan, final ScanSpec scanSpec, final ExecutorService executorService, final int numParallelTasks, final ClassGraph.ScanResultProcessor scanResultProcessor, final ClassGraph.FailureHandler failureHandler, final LogNode topLevelLog) throws InterruptedException {
        this.classpathEntryToClasspathElementSingletonMap = new SingletonMap<ClasspathOrder.ClasspathElementAndClassLoader, ClasspathElement, IOException>() {
            @Override
            public ClasspathElement newInstance(final ClasspathOrder.ClasspathElementAndClassLoader classpathEntry, final LogNode log) throws IOException, InterruptedException {
                Object classpathEntryObj = classpathEntry.classpathElementRoot;
                String dirOrPathPackageRoot;
                for (dirOrPathPackageRoot = classpathEntry.dirOrPathPackageRoot; dirOrPathPackageRoot.startsWith("/"); dirOrPathPackageRoot = dirOrPathPackageRoot.substring(1)) {}
                if (classpathEntryObj instanceof String) {
                    final String classpathEntryStr = (String)classpathEntryObj;
                    if (JarUtils.URL_SCHEME_PATTERN.matcher(classpathEntryStr).matches()) {
                        try {
                            classpathEntryObj = new URL(classpathEntryStr);
                        }
                        catch (MalformedURLException e4) {
                            throw new IOException("Malformed URL: " + classpathEntryStr);
                        }
                    }
                }
                String classpathEntryPathStr;
                if (classpathEntryObj instanceof URL) {
                    URL classpathEntryURL = (URL)classpathEntryObj;
                    String scheme = classpathEntryURL.getProtocol();
                    if ("jar".equals(scheme)) {
                        try {
                            classpathEntryURL = new URL(URLDecoder.decode(classpathEntryURL.toString(), "UTF-8").substring(4));
                            scheme = classpathEntryURL.getProtocol();
                        }
                        catch (MalformedURLException e) {
                            throw new IOException("Could not strip 'jar:' prefix from " + classpathEntryObj, e);
                        }
                    }
                    if ("file".equals(scheme)) {
                        classpathEntryPathStr = URLDecoder.decode(classpathEntryURL.getPath(), "UTF-8");
                    }
                    else {
                        if ("http".equals(scheme) || "https".equals(scheme)) {
                            return new ClasspathElementZip(classpathEntryURL, classpathEntry.classLoader, Scanner.this.nestedJarHandler, Scanner.this.scanSpec);
                        }
                        try {
                            final Path path = Paths.get(classpathEntryURL.toURI());
                            if (Files.isDirectory(path, new LinkOption[0])) {
                                return new ClasspathElementPathDir(path, dirOrPathPackageRoot, classpathEntry.classLoader, Scanner.this.nestedJarHandler, Scanner.this.scanSpec);
                            }
                        }
                        catch (URISyntaxException | IllegalArgumentException | SecurityException ex2) {
                            final Exception ex;
                            final Exception e2 = ex;
                            throw new IOException("Cannot handle URL " + classpathEntryURL + " : " + e2.getMessage());
                        }
                        catch (FileSystemNotFoundException ex3) {}
                        return new ClasspathElementZip(classpathEntryURL, classpathEntry.classLoader, Scanner.this.nestedJarHandler, Scanner.this.scanSpec);
                    }
                }
                else if (classpathEntryObj instanceof Path) {
                    final Path classpathEntryPath = (Path)classpathEntryObj;
                    final Path packageRootPath = classpathEntryPath.resolve(dirOrPathPackageRoot);
                    if (FileUtils.canReadAndIsFile(packageRootPath)) {
                        return new ClasspathElementZip(classpathEntryPath.toUri(), classpathEntry.classLoader, Scanner.this.nestedJarHandler, Scanner.this.scanSpec);
                    }
                    if (FileUtils.canReadAndIsDir(packageRootPath)) {
                        return new ClasspathElementPathDir((Path)classpathEntryObj, dirOrPathPackageRoot, classpathEntry.classLoader, Scanner.this.nestedJarHandler, Scanner.this.scanSpec);
                    }
                    throw new IOException("Path is not a directory or file: " + classpathEntryPath);
                }
                else {
                    classpathEntryPathStr = classpathEntryObj.toString();
                }
                final String pathNormalized = FastPathResolver.resolve(FileUtils.CURR_DIR_PATH, classpathEntryPathStr);
                final int plingIdx = pathNormalized.indexOf(33);
                final String pathToCanonicalize = (plingIdx < 0) ? pathNormalized : pathNormalized.substring(0, plingIdx);
                final File fileCanonicalized = new File(pathToCanonicalize).getCanonicalFile();
                if (!fileCanonicalized.exists()) {
                    throw new FileNotFoundException();
                }
                if (!FileUtils.canRead(fileCanonicalized)) {
                    throw new IOException("Cannot read file or directory");
                }
                boolean isJar = classpathEntryPathStr.regionMatches(true, 0, "jar:", 0, 4) || plingIdx > 0;
                if (fileCanonicalized.isFile()) {
                    isJar = true;
                }
                else {
                    if (!fileCanonicalized.isDirectory()) {
                        throw new IOException("Not a normal file or directory");
                    }
                    if (isJar) {
                        throw new IOException("Expected jar, found directory");
                    }
                }
                final String baseFileCanonicalPathNormalized = FastPathResolver.resolve(FileUtils.CURR_DIR_PATH, fileCanonicalized.getPath());
                final String canonicalPathNormalized = (plingIdx < 0) ? baseFileCanonicalPathNormalized : (baseFileCanonicalPathNormalized + pathNormalized.substring(plingIdx));
                if (!canonicalPathNormalized.equals(pathNormalized)) {
                    try {
                        return ((SingletonMap<ClasspathOrder.ClasspathElementAndClassLoader, ClasspathElement, E>)this).get(new ClasspathOrder.ClasspathElementAndClassLoader(canonicalPathNormalized, dirOrPathPackageRoot, classpathEntry.classLoader), log);
                    }
                    catch (NullSingletonException e3) {
                        throw new IOException("Cannot get classpath element for canonical path " + canonicalPathNormalized + " : " + e3);
                    }
                }
                return isJar ? new ClasspathElementZip(canonicalPathNormalized, classpathEntry.classLoader, Scanner.this.nestedJarHandler, Scanner.this.scanSpec) : new ClasspathElementFileDir(fileCanonicalized, dirOrPathPackageRoot, classpathEntry.classLoader, Scanner.this.nestedJarHandler, Scanner.this.scanSpec);
            }
        };
        this.scanSpec = scanSpec;
        this.performScan = performScan;
        scanSpec.sortPrefixes();
        scanSpec.log(topLevelLog);
        if (topLevelLog != null) {
            if (scanSpec.pathAcceptReject != null && scanSpec.packagePrefixAcceptReject.isSpecificallyAccepted("")) {
                topLevelLog.log("Note: There is no need to accept the root package (\"\") -- not accepting anything will have the same effect of causing all packages to be scanned");
            }
            topLevelLog.log("Number of worker threads: " + numParallelTasks);
        }
        this.executorService = executorService;
        this.interruptionChecker = ((executorService instanceof AutoCloseableExecutorService) ? ((AutoCloseableExecutorService)executorService).interruptionChecker : new InterruptionChecker());
        this.nestedJarHandler = new NestedJarHandler(scanSpec, this.interruptionChecker);
        this.numParallelTasks = numParallelTasks;
        this.scanResultProcessor = scanResultProcessor;
        this.failureHandler = failureHandler;
        this.topLevelLog = topLevelLog;
        final LogNode classpathFinderLog = (topLevelLog == null) ? null : topLevelLog.log("Finding classpath");
        this.classpathFinder = new ClasspathFinder(scanSpec, classpathFinderLog);
        this.classLoaderOrderRespectingParentDelegation = this.classpathFinder.getClassLoaderOrderRespectingParentDelegation();
        try {
            this.moduleOrder = new ArrayList<ClasspathElementModule>();
            final ModuleFinder moduleFinder = this.classpathFinder.getModuleFinder();
            if (moduleFinder != null) {
                final List<ModuleRef> systemModuleRefs = moduleFinder.getSystemModuleRefs();
                final ClassLoader defaultClassLoader = (this.classLoaderOrderRespectingParentDelegation != null && this.classLoaderOrderRespectingParentDelegation.length != 0) ? this.classLoaderOrderRespectingParentDelegation[0] : null;
                if (systemModuleRefs != null) {
                    for (final ModuleRef systemModuleRef : systemModuleRefs) {
                        final String moduleName = systemModuleRef.getName();
                        if ((scanSpec.enableSystemJarsAndModules && scanSpec.moduleAcceptReject.acceptAndRejectAreEmpty()) || scanSpec.moduleAcceptReject.isSpecificallyAcceptedAndNotRejected(moduleName)) {
                            final ClasspathElementModule classpathElementModule = new ClasspathElementModule(systemModuleRef, defaultClassLoader, this.nestedJarHandler.moduleRefToModuleReaderProxyRecyclerMap, scanSpec);
                            this.moduleOrder.add(classpathElementModule);
                            classpathElementModule.open(null, classpathFinderLog);
                        }
                        else {
                            if (classpathFinderLog == null) {
                                continue;
                            }
                            classpathFinderLog.log("Skipping non-accepted or rejected system module: " + moduleName);
                        }
                    }
                }
                final List<ModuleRef> nonSystemModuleRefs = moduleFinder.getNonSystemModuleRefs();
                if (nonSystemModuleRefs != null) {
                    for (final ModuleRef nonSystemModuleRef : nonSystemModuleRefs) {
                        String moduleName2 = nonSystemModuleRef.getName();
                        if (moduleName2 == null) {
                            moduleName2 = "";
                        }
                        if (scanSpec.moduleAcceptReject.isAcceptedAndNotRejected(moduleName2)) {
                            final ClasspathElementModule classpathElementModule2 = new ClasspathElementModule(nonSystemModuleRef, defaultClassLoader, this.nestedJarHandler.moduleRefToModuleReaderProxyRecyclerMap, scanSpec);
                            this.moduleOrder.add(classpathElementModule2);
                            classpathElementModule2.open(null, classpathFinderLog);
                        }
                        else {
                            if (classpathFinderLog == null) {
                                continue;
                            }
                            classpathFinderLog.log("Skipping non-accepted or rejected module: " + moduleName2);
                        }
                    }
                }
            }
        }
        catch (InterruptedException e) {
            this.nestedJarHandler.close(null);
            throw e;
        }
    }
    
    private static void findClasspathOrderRec(final ClasspathElement currClasspathElement, final Set<ClasspathElement> visitedClasspathElts, final List<ClasspathElement> order) {
        if (visitedClasspathElts.add(currClasspathElement)) {
            if (!currClasspathElement.skipClasspathElement) {
                order.add(currClasspathElement);
            }
            for (final ClasspathElement childClasspathElt : currClasspathElement.childClasspathElementsOrdered) {
                findClasspathOrderRec(childClasspathElt, visitedClasspathElts, order);
            }
        }
    }
    
    private static List<ClasspathElement> orderClasspathElements(final Collection<Map.Entry<Integer, ClasspathElement>> classpathEltsIndexed) {
        final List<Map.Entry<Integer, ClasspathElement>> classpathEltsIndexedOrdered = new ArrayList<Map.Entry<Integer, ClasspathElement>>(classpathEltsIndexed);
        CollectionUtils.sortIfNotEmpty(classpathEltsIndexedOrdered, Scanner.INDEXED_CLASSPATH_ELEMENT_COMPARATOR);
        final List<ClasspathElement> classpathEltsOrdered = new ArrayList<ClasspathElement>(classpathEltsIndexedOrdered.size());
        for (final Map.Entry<Integer, ClasspathElement> ent : classpathEltsIndexedOrdered) {
            classpathEltsOrdered.add(ent.getValue());
        }
        return classpathEltsOrdered;
    }
    
    private List<ClasspathElement> findClasspathOrder(final Set<ClasspathElement> uniqueClasspathElements, final Queue<Map.Entry<Integer, ClasspathElement>> toplevelClasspathEltsIndexed) {
        final List<ClasspathElement> toplevelClasspathEltsOrdered = orderClasspathElements(toplevelClasspathEltsIndexed);
        for (final ClasspathElement classpathElt : uniqueClasspathElements) {
            classpathElt.childClasspathElementsOrdered = orderClasspathElements(classpathElt.childClasspathElementsIndexed);
        }
        final Set<ClasspathElement> visitedClasspathElts = new HashSet<ClasspathElement>();
        final List<ClasspathElement> order = new ArrayList<ClasspathElement>();
        for (final ClasspathElement toplevelClasspathElt : toplevelClasspathEltsOrdered) {
            findClasspathOrderRec(toplevelClasspathElt, visitedClasspathElts, order);
        }
        return order;
    }
    
    private <W> void processWorkUnits(final Collection<W> workUnits, final LogNode log, final WorkQueue.WorkUnitProcessor<W> workUnitProcessor) throws InterruptedException, ExecutionException {
        WorkQueue.runWorkQueue(workUnits, this.executorService, this.interruptionChecker, this.numParallelTasks, log, workUnitProcessor);
        if (log != null) {
            log.addElapsedTime();
        }
        this.interruptionChecker.check();
    }
    
    private WorkQueue.WorkUnitProcessor<ClasspathEntryWorkUnit> newClasspathEntryWorkUnitProcessor(final Set<ClasspathElement> openedClasspathElementsSet, final Queue<Map.Entry<Integer, ClasspathElement>> toplevelClasspathEltOrder) {
        return new WorkQueue.WorkUnitProcessor<ClasspathEntryWorkUnit>() {
            @Override
            public void processWorkUnit(final ClasspathEntryWorkUnit workUnit, final WorkQueue<ClasspathEntryWorkUnit> workQueue, final LogNode log) throws InterruptedException {
                try {
                    ClasspathElement classpathElt;
                    try {
                        classpathElt = Scanner.this.classpathEntryToClasspathElementSingletonMap.get(workUnit.rawClasspathEntry, log);
                    }
                    catch (SingletonMap.NullSingletonException e) {
                        throw new IOException("Cannot get classpath element for classpath entry " + workUnit.rawClasspathEntry + " : " + e);
                    }
                    if (openedClasspathElementsSet.add(classpathElt)) {
                        final LogNode subLog = (log == null) ? null : log.log("Opening classpath element " + classpathElt);
                        classpathElt.open(workQueue, subLog);
                        final AbstractMap.SimpleEntry<Integer, ClasspathElement> classpathEltEntry = new AbstractMap.SimpleEntry<Integer, ClasspathElement>(workUnit.orderWithinParentClasspathElement, classpathElt);
                        if (workUnit.parentClasspathElement != null) {
                            workUnit.parentClasspathElement.childClasspathElementsIndexed.add(classpathEltEntry);
                        }
                        else {
                            toplevelClasspathEltOrder.add(classpathEltEntry);
                        }
                    }
                }
                catch (IOException | SecurityException ex2) {
                    final Exception ex;
                    final Exception e2 = ex;
                    if (log != null) {
                        log.log("Skipping invalid classpath element " + workUnit.rawClasspathEntry.classpathElementRoot + (workUnit.rawClasspathEntry.dirOrPathPackageRoot.isEmpty() ? "" : ("/" + workUnit.rawClasspathEntry.dirOrPathPackageRoot)) + " : " + e2);
                    }
                }
            }
        };
    }
    
    private void findNestedClasspathElements(final List<AbstractMap.SimpleEntry<String, ClasspathElement>> classpathElts, final LogNode log) {
        CollectionUtils.sortIfNotEmpty(classpathElts, new Comparator<AbstractMap.SimpleEntry<String, ClasspathElement>>() {
            @Override
            public int compare(final AbstractMap.SimpleEntry<String, ClasspathElement> o1, final AbstractMap.SimpleEntry<String, ClasspathElement> o2) {
                return o1.getKey().compareTo((String)o2.getKey());
            }
        });
        for (int i = 0; i < classpathElts.size(); ++i) {
            final AbstractMap.SimpleEntry<String, ClasspathElement> ei = classpathElts.get(i);
            final String basePath = ei.getKey();
            final int basePathLen = basePath.length();
            for (int j = i + 1; j < classpathElts.size(); ++j) {
                final AbstractMap.SimpleEntry<String, ClasspathElement> ej = classpathElts.get(j);
                final String comparePath = ej.getKey();
                final int comparePathLen = comparePath.length();
                boolean foundNestedClasspathRoot = false;
                if (comparePath.startsWith(basePath) && comparePathLen > basePathLen) {
                    final char nextChar = comparePath.charAt(basePathLen);
                    if (nextChar == '/' || nextChar == '!') {
                        final String nestedClasspathRelativePath = comparePath.substring(basePathLen + 1);
                        if (nestedClasspathRelativePath.indexOf(33) < 0) {
                            foundNestedClasspathRoot = true;
                            final ClasspathElement baseElement = ei.getValue();
                            if (baseElement.nestedClasspathRootPrefixes == null) {
                                baseElement.nestedClasspathRootPrefixes = new ArrayList<String>();
                            }
                            baseElement.nestedClasspathRootPrefixes.add(nestedClasspathRelativePath + "/");
                            if (log != null) {
                                log.log(basePath + " is a prefix of the nested element " + comparePath);
                            }
                        }
                    }
                }
                if (!foundNestedClasspathRoot) {
                    break;
                }
            }
        }
    }
    
    private void preprocessClasspathElementsByType(final List<ClasspathElement> finalTraditionalClasspathEltOrder, final LogNode classpathFinderLog) {
        final List<AbstractMap.SimpleEntry<String, ClasspathElement>> classpathEltDirs = new ArrayList<AbstractMap.SimpleEntry<String, ClasspathElement>>();
        final List<AbstractMap.SimpleEntry<String, ClasspathElement>> classpathEltZips = new ArrayList<AbstractMap.SimpleEntry<String, ClasspathElement>>();
        for (final ClasspathElement classpathElt : finalTraditionalClasspathEltOrder) {
            if (classpathElt instanceof ClasspathElementFileDir) {
                classpathEltDirs.add(new AbstractMap.SimpleEntry<String, ClasspathElement>(((ClasspathElementFileDir)classpathElt).getFile().getPath(), classpathElt));
            }
            else {
                if (!(classpathElt instanceof ClasspathElementZip)) {
                    continue;
                }
                final ClasspathElementZip classpathEltZip = (ClasspathElementZip)classpathElt;
                classpathEltZips.add(new AbstractMap.SimpleEntry<String, ClasspathElement>(classpathEltZip.getZipFilePath(), classpathElt));
                if (classpathEltZip.logicalZipFile == null) {
                    continue;
                }
                if (classpathEltZip.logicalZipFile.addExportsManifestEntryValue != null) {
                    for (final String addExports : JarUtils.smartPathSplit(classpathEltZip.logicalZipFile.addExportsManifestEntryValue, ' ', this.scanSpec)) {
                        this.scanSpec.modulePathInfo.addExports.add(addExports + "=ALL-UNNAMED");
                    }
                }
                if (classpathEltZip.logicalZipFile.addOpensManifestEntryValue != null) {
                    for (final String addOpens : JarUtils.smartPathSplit(classpathEltZip.logicalZipFile.addOpensManifestEntryValue, ' ', this.scanSpec)) {
                        this.scanSpec.modulePathInfo.addOpens.add(addOpens + "=ALL-UNNAMED");
                    }
                }
                if (classpathEltZip.logicalZipFile.automaticModuleNameManifestEntryValue == null) {
                    continue;
                }
                classpathEltZip.moduleNameFromManifestFile = classpathEltZip.logicalZipFile.automaticModuleNameManifestEntryValue;
            }
        }
        this.findNestedClasspathElements(classpathEltDirs, classpathFinderLog);
        this.findNestedClasspathElements(classpathEltZips, classpathFinderLog);
    }
    
    private void maskClassfiles(final List<ClasspathElement> classpathElementOrder, final LogNode maskLog) {
        final Set<String> acceptedClasspathRelativePathsFound = new HashSet<String>();
        for (int classpathIdx = 0; classpathIdx < classpathElementOrder.size(); ++classpathIdx) {
            final ClasspathElement classpathElement = classpathElementOrder.get(classpathIdx);
            classpathElement.maskClassfiles(classpathIdx, acceptedClasspathRelativePathsFound, maskLog);
        }
        if (maskLog != null) {
            maskLog.addElapsedTime();
        }
    }
    
    private ScanResult performScan(final List<ClasspathElement> finalClasspathEltOrder, final List<String> finalClasspathEltOrderStrs, final ClassLoader[] classLoaderOrderRespectingParentDelegation) throws InterruptedException, ExecutionException {
        if (this.scanSpec.enableClassInfo) {
            this.maskClassfiles(finalClasspathEltOrder, (this.topLevelLog == null) ? null : this.topLevelLog.log("Masking classfiles"));
        }
        final Map<File, Long> fileToLastModified = new HashMap<File, Long>();
        for (final ClasspathElement classpathElement : finalClasspathEltOrder) {
            fileToLastModified.putAll(classpathElement.fileToLastModified);
        }
        final Map<String, ClassInfo> classNameToClassInfo = new ConcurrentHashMap<String, ClassInfo>();
        final Map<String, PackageInfo> packageNameToPackageInfo = new HashMap<String, PackageInfo>();
        final Map<String, ModuleInfo> moduleNameToModuleInfo = new HashMap<String, ModuleInfo>();
        if (this.scanSpec.enableClassInfo) {
            final List<ClassfileScanWorkUnit> classfileScanWorkItems = new ArrayList<ClassfileScanWorkUnit>();
            final Set<String> acceptedClassNamesFound = new HashSet<String>();
            for (final ClasspathElement classpathElement2 : finalClasspathEltOrder) {
                for (final Resource resource : classpathElement2.acceptedClassfileResources) {
                    final String className = JarUtils.classfilePathToClassName(resource.getPath());
                    if (!acceptedClassNamesFound.add(className) && !className.equals("module-info") && !className.equals("package-info") && !className.endsWith(".package-info")) {
                        throw new IllegalArgumentException("Class " + className + " should not have been scheduled more than once for scanning due to classpath masking -- please report this bug at: https://github.com/classgraph/classgraph/issues");
                    }
                    classfileScanWorkItems.add(new ClassfileScanWorkUnit(classpathElement2, resource, false));
                }
            }
            final Queue<Classfile> scannedClassfiles = new ConcurrentLinkedQueue<Classfile>();
            final ClassfileScannerWorkUnitProcessor classfileWorkUnitProcessor = new ClassfileScannerWorkUnitProcessor(this.scanSpec, finalClasspathEltOrder, Collections.unmodifiableSet((Set<? extends String>)acceptedClassNamesFound), scannedClassfiles);
            this.processWorkUnits(classfileScanWorkItems, (this.topLevelLog == null) ? null : this.topLevelLog.log("Scanning classfiles"), classfileWorkUnitProcessor);
            final LogNode linkLog = (this.topLevelLog == null) ? null : this.topLevelLog.log("Linking related classfiles");
            while (!scannedClassfiles.isEmpty()) {
                final Classfile c = scannedClassfiles.remove();
                c.link(classNameToClassInfo, packageNameToPackageInfo, moduleNameToModuleInfo);
            }
            if (linkLog != null) {
                linkLog.addElapsedTime();
            }
        }
        else if (this.topLevelLog != null) {
            this.topLevelLog.log("Classfile scanning is disabled");
        }
        return new ScanResult(this.scanSpec, finalClasspathEltOrder, finalClasspathEltOrderStrs, classLoaderOrderRespectingParentDelegation, classNameToClassInfo, packageNameToPackageInfo, moduleNameToModuleInfo, fileToLastModified, this.nestedJarHandler, this.topLevelLog);
    }
    
    private ScanResult openClasspathElementsThenScan() throws InterruptedException, ExecutionException {
        final List<ClasspathEntryWorkUnit> rawClasspathEntryWorkUnits = new ArrayList<ClasspathEntryWorkUnit>();
        for (final ClasspathOrder.ClasspathElementAndClassLoader rawClasspathEntry : this.classpathFinder.getClasspathOrder().getOrder()) {
            rawClasspathEntryWorkUnits.add(new ClasspathEntryWorkUnit(rawClasspathEntry, null, rawClasspathEntryWorkUnits.size()));
        }
        final Set<ClasspathElement> openedClasspathEltsSet = Collections.newSetFromMap(new ConcurrentHashMap<ClasspathElement, Boolean>());
        final Queue<Map.Entry<Integer, ClasspathElement>> toplevelClasspathEltOrder = new ConcurrentLinkedQueue<Map.Entry<Integer, ClasspathElement>>();
        this.processWorkUnits(rawClasspathEntryWorkUnits, (this.topLevelLog == null) ? null : this.topLevelLog.log("Opening classpath elements"), this.newClasspathEntryWorkUnitProcessor(openedClasspathEltsSet, toplevelClasspathEltOrder));
        final List<ClasspathElement> classpathEltOrder = this.findClasspathOrder(openedClasspathEltsSet, toplevelClasspathEltOrder);
        this.preprocessClasspathElementsByType(classpathEltOrder, (this.topLevelLog == null) ? null : this.topLevelLog.log("Finding nested classpath elements"));
        final LogNode classpathOrderLog = (this.topLevelLog == null) ? null : this.topLevelLog.log("Final classpath element order:");
        final int numElts = this.moduleOrder.size() + classpathEltOrder.size();
        final List<ClasspathElement> finalClasspathEltOrder = new ArrayList<ClasspathElement>(numElts);
        final List<String> finalClasspathEltOrderStrs = new ArrayList<String>(numElts);
        int classpathOrderIdx = 0;
        for (final ClasspathElementModule classpathElt : this.moduleOrder) {
            classpathElt.classpathElementIdx = classpathOrderIdx++;
            finalClasspathEltOrder.add(classpathElt);
            finalClasspathEltOrderStrs.add(classpathElt.toString());
            if (classpathOrderLog != null) {
                final ModuleRef moduleRef = classpathElt.getModuleRef();
                classpathOrderLog.log(moduleRef.toString());
            }
        }
        for (final ClasspathElement classpathElt2 : classpathEltOrder) {
            classpathElt2.classpathElementIdx = classpathOrderIdx++;
            finalClasspathEltOrder.add(classpathElt2);
            finalClasspathEltOrderStrs.add(classpathElt2.toString());
            if (classpathOrderLog != null) {
                classpathOrderLog.log(classpathElt2.toString());
            }
        }
        this.processWorkUnits(finalClasspathEltOrder, (this.topLevelLog == null) ? null : this.topLevelLog.log("Scanning classpath elements"), new WorkQueue.WorkUnitProcessor<ClasspathElement>() {
            @Override
            public void processWorkUnit(final ClasspathElement classpathElement, final WorkQueue<ClasspathElement> workQueueIgnored, final LogNode pathScanLog) throws InterruptedException {
                classpathElement.scanPaths(pathScanLog);
            }
        });
        List<ClasspathElement> finalClasspathEltOrderFiltered = finalClasspathEltOrder;
        if (!this.scanSpec.classpathElementResourcePathAcceptReject.acceptIsEmpty()) {
            finalClasspathEltOrderFiltered = new ArrayList<ClasspathElement>(finalClasspathEltOrder.size());
            for (final ClasspathElement classpathElement : finalClasspathEltOrder) {
                if (classpathElement.containsSpecificallyAcceptedClasspathElementResourcePath) {
                    finalClasspathEltOrderFiltered.add(classpathElement);
                }
            }
        }
        if (this.performScan) {
            return this.performScan(finalClasspathEltOrderFiltered, finalClasspathEltOrderStrs, this.classLoaderOrderRespectingParentDelegation);
        }
        if (this.topLevelLog != null) {
            this.topLevelLog.log("Only returning classpath elements (not performing a scan)");
        }
        return new ScanResult(this.scanSpec, finalClasspathEltOrderFiltered, finalClasspathEltOrderStrs, this.classLoaderOrderRespectingParentDelegation, null, null, null, null, this.nestedJarHandler, this.topLevelLog);
    }
    
    @Override
    public ScanResult call() throws InterruptedException, CancellationException, ExecutionException {
        ScanResult scanResult = null;
        final long scanStart = System.currentTimeMillis();
        boolean removeTemporaryFilesAfterScan = this.scanSpec.removeTemporaryFilesAfterScan;
        try {
            scanResult = this.openClasspathElementsThenScan();
            if (this.topLevelLog != null) {
                this.topLevelLog.log("~", String.format("Total time: %.3f sec", (System.currentTimeMillis() - scanStart) * 0.001));
                this.topLevelLog.flush();
            }
            if (this.scanResultProcessor != null) {
                try {
                    this.scanResultProcessor.processScanResult(scanResult);
                }
                finally {
                    scanResult.close();
                }
            }
        }
        catch (Throwable e) {
            if (this.topLevelLog != null) {
                this.topLevelLog.log("~", (e instanceof InterruptedException || e instanceof CancellationException) ? "Scan interrupted or canceled" : ((e instanceof ExecutionException || e instanceof RuntimeException) ? "Uncaught exception during scan" : e.getMessage()), InterruptionChecker.getCause(e));
                this.topLevelLog.flush();
            }
            removeTemporaryFilesAfterScan = true;
            this.interruptionChecker.interrupt();
            if (this.failureHandler == null) {
                throw e;
            }
            try {
                this.failureHandler.onFailure(e);
            }
            catch (Exception f) {
                if (this.topLevelLog != null) {
                    this.topLevelLog.log("~", "The failure handler threw an exception:", f);
                    this.topLevelLog.flush();
                }
                final ExecutionException failureHandlerException = new ExecutionException("Exception while calling failure handler", f);
                failureHandlerException.addSuppressed(e);
                throw failureHandlerException;
            }
        }
        finally {
            if (removeTemporaryFilesAfterScan) {
                this.nestedJarHandler.close(this.topLevelLog);
            }
        }
        return scanResult;
    }
    
    static {
        INDEXED_CLASSPATH_ELEMENT_COMPARATOR = new Comparator<Map.Entry<Integer, ClasspathElement>>() {
            @Override
            public int compare(final Map.Entry<Integer, ClasspathElement> o1, final Map.Entry<Integer, ClasspathElement> o2) {
                return o1.getKey() - o2.getKey();
            }
        };
    }
    
    static class ClasspathEntryWorkUnit
    {
        private final ClasspathOrder.ClasspathElementAndClassLoader rawClasspathEntry;
        private final ClasspathElement parentClasspathElement;
        private final int orderWithinParentClasspathElement;
        
        public ClasspathEntryWorkUnit(final ClasspathOrder.ClasspathElementAndClassLoader rawClasspathEntry, final ClasspathElement parentClasspathElement, final int orderWithinParentClasspathElement) {
            this.rawClasspathEntry = rawClasspathEntry;
            this.parentClasspathElement = parentClasspathElement;
            this.orderWithinParentClasspathElement = orderWithinParentClasspathElement;
        }
    }
    
    static class ClassfileScanWorkUnit
    {
        private final ClasspathElement classpathElement;
        private final Resource classfileResource;
        private final boolean isExternalClass;
        
        ClassfileScanWorkUnit(final ClasspathElement classpathElement, final Resource classfileResource, final boolean isExternalClass) {
            this.classpathElement = classpathElement;
            this.classfileResource = classfileResource;
            this.isExternalClass = isExternalClass;
        }
    }
    
    private static class ClassfileScannerWorkUnitProcessor implements WorkQueue.WorkUnitProcessor<ClassfileScanWorkUnit>
    {
        private final ScanSpec scanSpec;
        private final List<ClasspathElement> classpathOrder;
        private final Set<String> acceptedClassNamesFound;
        private final Set<String> classNamesScheduledForExtendedScanning;
        private final Queue<Classfile> scannedClassfiles;
        private final ConcurrentHashMap<String, String> stringInternMap;
        
        public ClassfileScannerWorkUnitProcessor(final ScanSpec scanSpec, final List<ClasspathElement> classpathOrder, final Set<String> acceptedClassNamesFound, final Queue<Classfile> scannedClassfiles) {
            this.classNamesScheduledForExtendedScanning = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
            this.stringInternMap = new ConcurrentHashMap<String, String>();
            this.scanSpec = scanSpec;
            this.classpathOrder = classpathOrder;
            this.acceptedClassNamesFound = acceptedClassNamesFound;
            this.scannedClassfiles = scannedClassfiles;
        }
        
        @Override
        public void processWorkUnit(final ClassfileScanWorkUnit workUnit, final WorkQueue<ClassfileScanWorkUnit> workQueue, final LogNode log) throws InterruptedException {
            final LogNode subLog = (workUnit.classfileResource.scanLog == null) ? null : workUnit.classfileResource.scanLog.log(workUnit.classfileResource.getPath(), "Parsing classfile");
            try {
                final Classfile classfile = new Classfile(workUnit.classpathElement, this.classpathOrder, this.acceptedClassNamesFound, this.classNamesScheduledForExtendedScanning, workUnit.classfileResource.getPath(), workUnit.classfileResource, workUnit.isExternalClass, this.stringInternMap, workQueue, this.scanSpec, subLog);
                this.scannedClassfiles.add(classfile);
            }
            catch (Classfile.SkipClassException e) {
                if (subLog != null) {
                    subLog.log(workUnit.classfileResource.getPath(), "Skipping classfile: " + e.getMessage());
                }
            }
            catch (Classfile.ClassfileFormatException e2) {
                if (subLog != null) {
                    subLog.log(workUnit.classfileResource.getPath(), "Invalid classfile: " + e2.getMessage());
                }
            }
            catch (IOException e3) {
                if (subLog != null) {
                    subLog.log(workUnit.classfileResource.getPath(), "Could not read classfile: " + e3);
                }
            }
            finally {
                if (subLog != null) {
                    subLog.addElapsedTime();
                }
            }
        }
    }
}
