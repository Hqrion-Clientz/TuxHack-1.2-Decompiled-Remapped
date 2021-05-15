// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import nonapi.io.github.classgraph.json.JSONSerializer;
import nonapi.io.github.classgraph.utils.CollectionUtils;
import java.util.regex.Matcher;
import nonapi.io.github.classgraph.concurrency.AutoCloseableExecutorService;
import nonapi.io.github.classgraph.json.JSONDeserializer;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import nonapi.io.github.classgraph.utils.JarUtils;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import nonapi.io.github.classgraph.utils.FileUtils;
import java.nio.ByteBuffer;
import java.util.Set;
import java.lang.ref.WeakReference;
import nonapi.io.github.classgraph.utils.LogNode;
import java.util.concurrent.atomic.AtomicBoolean;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.fastzipfilereader.NestedJarHandler;
import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.io.Closeable;

public final class ScanResult implements Closeable, AutoCloseable
{
    private List<String> rawClasspathEltOrderStrs;
    private List<ClasspathElement> classpathOrder;
    private ResourceList allAcceptedResourcesCached;
    private final AtomicInteger getResourcesWithPathCallCount;
    private Map<String, ResourceList> pathToAcceptedResourcesCached;
    Map<String, ClassInfo> classNameToClassInfo;
    private Map<String, PackageInfo> packageNameToPackageInfo;
    private Map<String, ModuleInfo> moduleNameToModuleInfo;
    private Map<File, Long> fileToLastModified;
    private boolean isObtainedFromDeserialization;
    private ClassGraphClassLoader classGraphClassLoader;
    private ClassLoader[] classLoaderOrderRespectingParentDelegation;
    private NestedJarHandler nestedJarHandler;
    ScanSpec scanSpec;
    private final AtomicBoolean closed;
    private final LogNode topLevelLog;
    private final WeakReference<ScanResult> weakReference;
    private static Set<WeakReference<ScanResult>> nonClosedWeakReferences;
    private static final AtomicBoolean initialized;
    private static final String CURRENT_SERIALIZATION_FORMAT = "10";
    
    static void init() {
        if (!ScanResult.initialized.getAndSet(true)) {
            FileUtils.closeDirectByteBuffer(ByteBuffer.allocateDirect(32), null);
        }
    }
    
    ScanResult(final ScanSpec scanSpec, final List<ClasspathElement> classpathOrder, final List<String> rawClasspathEltOrderStrs, final ClassLoader[] classLoaderOrderRespectingParentDelegation, final Map<String, ClassInfo> classNameToClassInfo, final Map<String, PackageInfo> packageNameToPackageInfo, final Map<String, ModuleInfo> moduleNameToModuleInfo, final Map<File, Long> fileToLastModified, final NestedJarHandler nestedJarHandler, final LogNode topLevelLog) {
        this.getResourcesWithPathCallCount = new AtomicInteger();
        this.closed = new AtomicBoolean(false);
        this.scanSpec = scanSpec;
        this.rawClasspathEltOrderStrs = rawClasspathEltOrderStrs;
        this.classpathOrder = classpathOrder;
        this.classLoaderOrderRespectingParentDelegation = classLoaderOrderRespectingParentDelegation;
        this.fileToLastModified = fileToLastModified;
        this.classNameToClassInfo = classNameToClassInfo;
        this.packageNameToPackageInfo = packageNameToPackageInfo;
        this.moduleNameToModuleInfo = moduleNameToModuleInfo;
        this.nestedJarHandler = nestedJarHandler;
        this.topLevelLog = topLevelLog;
        if (classNameToClassInfo != null) {
            this.indexResourcesAndClassInfo();
        }
        if (classNameToClassInfo != null) {
            final Set<String> allRepeatableAnnotationNames = new HashSet<String>();
            for (final ClassInfo classInfo : classNameToClassInfo.values()) {
                if (classInfo.isAnnotation() && classInfo.annotationInfo != null) {
                    final AnnotationInfo repeatableMetaAnnotation = classInfo.annotationInfo.get("java.lang.annotation.Repeatable");
                    if (repeatableMetaAnnotation == null) {
                        continue;
                    }
                    final AnnotationParameterValueList vals = repeatableMetaAnnotation.getParameterValues();
                    if (vals.isEmpty()) {
                        continue;
                    }
                    final Object val = vals.getValue("value");
                    if (!(val instanceof AnnotationClassRef)) {
                        continue;
                    }
                    final AnnotationClassRef classRef = (AnnotationClassRef)val;
                    final String repeatableAnnotationName = classRef.getName();
                    if (repeatableAnnotationName == null) {
                        continue;
                    }
                    allRepeatableAnnotationNames.add(repeatableAnnotationName);
                }
            }
            if (!allRepeatableAnnotationNames.isEmpty()) {
                for (final ClassInfo classInfo : classNameToClassInfo.values()) {
                    classInfo.handleRepeatableAnnotations(allRepeatableAnnotationNames);
                }
            }
        }
        this.classGraphClassLoader = new ClassGraphClassLoader(this);
        this.weakReference = new WeakReference<ScanResult>(this);
        ScanResult.nonClosedWeakReferences.add(this.weakReference);
    }
    
    private void indexResourcesAndClassInfo() {
        final Collection<ClassInfo> allClassInfo = this.classNameToClassInfo.values();
        for (final ClassInfo classInfo : allClassInfo) {
            classInfo.setScanResult(this);
        }
        if (this.scanSpec.enableInterClassDependencies) {
            for (final ClassInfo ci : new ArrayList<ClassInfo>(this.classNameToClassInfo.values())) {
                final Set<ClassInfo> refdClassesFiltered = new HashSet<ClassInfo>();
                for (final ClassInfo refdClassInfo : ci.findReferencedClassInfo()) {
                    if (refdClassInfo != null && !ci.equals(refdClassInfo) && !refdClassInfo.getName().equals("java.lang.Object") && (!refdClassInfo.isExternalClass() || this.scanSpec.enableExternalClasses)) {
                        refdClassInfo.setScanResult(this);
                        refdClassesFiltered.add(refdClassInfo);
                    }
                }
                ci.setReferencedClasses(new ClassInfoList(refdClassesFiltered, true));
            }
        }
    }
    
    public List<File> getClasspathFiles() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        final List<File> classpathElementOrderFiles = new ArrayList<File>();
        for (final ClasspathElement classpathElement : this.classpathOrder) {
            final File file = classpathElement.getFile();
            if (file != null) {
                classpathElementOrderFiles.add(file);
            }
        }
        return classpathElementOrderFiles;
    }
    
    public String getClasspath() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        return JarUtils.pathElementsToPathStr(this.getClasspathFiles());
    }
    
    public List<URI> getClasspathURIs() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        final List<URI> classpathElementOrderURIs = new ArrayList<URI>();
        for (final ClasspathElement classpathElement : this.classpathOrder) {
            try {
                final URI uri = classpathElement.getURI();
                if (uri == null) {
                    continue;
                }
                classpathElementOrderURIs.add(uri);
            }
            catch (IllegalArgumentException ex) {}
        }
        return classpathElementOrderURIs;
    }
    
    public List<URL> getClasspathURLs() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        final List<URL> classpathElementOrderURLs = new ArrayList<URL>();
        for (final ClasspathElement classpathElement : this.classpathOrder) {
            try {
                final URI uri = classpathElement.getURI();
                if (uri == null) {
                    continue;
                }
                classpathElementOrderURLs.add(uri.toURL());
            }
            catch (IllegalArgumentException ex) {}
            catch (MalformedURLException ex2) {}
        }
        return classpathElementOrderURLs;
    }
    
    public List<ModuleRef> getModules() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        final List<ModuleRef> moduleRefs = new ArrayList<ModuleRef>();
        for (final ClasspathElement classpathElement : this.classpathOrder) {
            if (classpathElement instanceof ClasspathElementModule) {
                moduleRefs.add(((ClasspathElementModule)classpathElement).getModuleRef());
            }
        }
        return moduleRefs;
    }
    
    public ModulePathInfo getModulePathInfo() {
        return this.scanSpec.modulePathInfo;
    }
    
    public ResourceList getAllResources() {
        if (this.allAcceptedResourcesCached == null) {
            final ResourceList acceptedResourcesList = new ResourceList();
            for (final ClasspathElement classpathElt : this.classpathOrder) {
                acceptedResourcesList.addAll(classpathElt.acceptedResources);
            }
            this.allAcceptedResourcesCached = acceptedResourcesList;
        }
        return this.allAcceptedResourcesCached;
    }
    
    public Map<String, ResourceList> getAllResourcesAsMap() {
        if (this.pathToAcceptedResourcesCached == null) {
            final Map<String, ResourceList> pathToAcceptedResourceListMap = new HashMap<String, ResourceList>();
            for (final Resource res : this.getAllResources()) {
                ResourceList resList = pathToAcceptedResourceListMap.get(res.getPath());
                if (resList == null) {
                    pathToAcceptedResourceListMap.put(res.getPath(), resList = new ResourceList());
                }
                resList.add(res);
            }
            this.pathToAcceptedResourcesCached = pathToAcceptedResourceListMap;
        }
        return this.pathToAcceptedResourcesCached;
    }
    
    public ResourceList getResourcesWithPath(final String resourcePath) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        final String path = FileUtils.sanitizeEntryPath(resourcePath, true, true);
        if (this.getResourcesWithPathCallCount.incrementAndGet() > 3) {
            return this.getAllResourcesAsMap().get(path);
        }
        ResourceList matchingResources = null;
        for (final ClasspathElement classpathElt : this.classpathOrder) {
            for (final Resource res : classpathElt.acceptedResources) {
                if (res.getPath().equals(path)) {
                    if (matchingResources == null) {
                        matchingResources = new ResourceList();
                    }
                    matchingResources.add(res);
                }
            }
        }
        return (matchingResources == null) ? ResourceList.EMPTY_LIST : matchingResources;
    }
    
    public ResourceList getResourcesWithPathIgnoringAccept(final String resourcePath) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        final String path = FileUtils.sanitizeEntryPath(resourcePath, true, true);
        final ResourceList matchingResources = new ResourceList();
        for (final ClasspathElement classpathElt : this.classpathOrder) {
            final Resource matchingResource = classpathElt.getResource(path);
            if (matchingResource != null) {
                matchingResources.add(matchingResource);
            }
        }
        return matchingResources;
    }
    
    @Deprecated
    public ResourceList getResourcesWithPathIgnoringWhitelist(final String resourcePath) {
        return this.getResourcesWithPathIgnoringAccept(resourcePath);
    }
    
    public ResourceList getResourcesWithLeafName(final String leafName) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        final ResourceList allAcceptedResources = this.getAllResources();
        if (allAcceptedResources.isEmpty()) {
            return ResourceList.EMPTY_LIST;
        }
        final ResourceList filteredResources = new ResourceList();
        for (final Resource classpathResource : allAcceptedResources) {
            final String relativePath = classpathResource.getPath();
            final int lastSlashIdx = relativePath.lastIndexOf(47);
            if (relativePath.substring(lastSlashIdx + 1).equals(leafName)) {
                filteredResources.add(classpathResource);
            }
        }
        return filteredResources;
    }
    
    public ResourceList getResourcesWithExtension(final String extension) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        final ResourceList allAcceptedResources = this.getAllResources();
        if (allAcceptedResources.isEmpty()) {
            return ResourceList.EMPTY_LIST;
        }
        String bareExtension;
        for (bareExtension = extension; bareExtension.startsWith("."); bareExtension = bareExtension.substring(1)) {}
        final ResourceList filteredResources = new ResourceList();
        for (final Resource classpathResource : allAcceptedResources) {
            final String relativePath = classpathResource.getPath();
            final int lastSlashIdx = relativePath.lastIndexOf(47);
            final int lastDotIdx = relativePath.lastIndexOf(46);
            if (lastDotIdx > lastSlashIdx && relativePath.substring(lastDotIdx + 1).equalsIgnoreCase(bareExtension)) {
                filteredResources.add(classpathResource);
            }
        }
        return filteredResources;
    }
    
    public ResourceList getResourcesMatchingPattern(final Pattern pattern) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        final ResourceList allAcceptedResources = this.getAllResources();
        if (allAcceptedResources.isEmpty()) {
            return ResourceList.EMPTY_LIST;
        }
        final ResourceList filteredResources = new ResourceList();
        for (final Resource classpathResource : allAcceptedResources) {
            final String relativePath = classpathResource.getPath();
            if (pattern.matcher(relativePath).matches()) {
                filteredResources.add(classpathResource);
            }
        }
        return filteredResources;
    }
    
    public ModuleInfo getModuleInfo(final String moduleName) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        return this.moduleNameToModuleInfo.get(moduleName);
    }
    
    public ModuleInfoList getModuleInfo() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        return new ModuleInfoList(this.moduleNameToModuleInfo.values());
    }
    
    public PackageInfo getPackageInfo(final String packageName) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        return this.packageNameToPackageInfo.get(packageName);
    }
    
    public PackageInfoList getPackageInfo() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        return new PackageInfoList(this.packageNameToPackageInfo.values());
    }
    
    public Map<ClassInfo, ClassInfoList> getClassDependencyMap() {
        final Map<ClassInfo, ClassInfoList> map = new HashMap<ClassInfo, ClassInfoList>();
        for (final ClassInfo ci : this.getAllClasses()) {
            map.put(ci, ci.getClassDependencies());
        }
        return map;
    }
    
    public Map<ClassInfo, ClassInfoList> getReverseClassDependencyMap() {
        final Map<ClassInfo, Set<ClassInfo>> revMapSet = new HashMap<ClassInfo, Set<ClassInfo>>();
        for (final ClassInfo ci : this.getAllClasses()) {
            for (final ClassInfo dep : ci.getClassDependencies()) {
                Set<ClassInfo> set = revMapSet.get(dep);
                if (set == null) {
                    revMapSet.put(dep, set = new HashSet<ClassInfo>());
                }
                set.add(ci);
            }
        }
        final Map<ClassInfo, ClassInfoList> revMapList = new HashMap<ClassInfo, ClassInfoList>();
        for (final Map.Entry<ClassInfo, Set<ClassInfo>> ent : revMapSet.entrySet()) {
            revMapList.put(ent.getKey(), new ClassInfoList(ent.getValue(), true));
        }
        return revMapList;
    }
    
    public ClassInfo getClassInfo(final String className) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        return this.classNameToClassInfo.get(className);
    }
    
    public ClassInfoList getAllClasses() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        return ClassInfo.getAllClasses(this.classNameToClassInfo.values(), this.scanSpec);
    }
    
    public ClassInfoList getAllEnums() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        return ClassInfo.getAllEnums(this.classNameToClassInfo.values(), this.scanSpec);
    }
    
    public ClassInfoList getAllRecords() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        return ClassInfo.getAllRecords(this.classNameToClassInfo.values(), this.scanSpec);
    }
    
    public Map<String, ClassInfo> getAllClassesAsMap() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        return this.classNameToClassInfo;
    }
    
    public ClassInfoList getAllStandardClasses() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        return ClassInfo.getAllStandardClasses(this.classNameToClassInfo.values(), this.scanSpec);
    }
    
    public ClassInfoList getSubclasses(final String superclassName) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        if (superclassName.equals("java.lang.Object")) {
            return this.getAllStandardClasses();
        }
        final ClassInfo superclass = this.classNameToClassInfo.get(superclassName);
        return (superclass == null) ? ClassInfoList.EMPTY_LIST : superclass.getSubclasses();
    }
    
    public ClassInfoList getSuperclasses(final String subclassName) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        final ClassInfo subclass = this.classNameToClassInfo.get(subclassName);
        return (subclass == null) ? ClassInfoList.EMPTY_LIST : subclass.getSuperclasses();
    }
    
    public ClassInfoList getClassesWithMethodAnnotation(final String methodAnnotationName) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo || !this.scanSpec.enableMethodInfo || !this.scanSpec.enableAnnotationInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo(), #enableMethodInfo(), and #enableAnnotationInfo() before #scan()");
        }
        final ClassInfo classInfo = this.classNameToClassInfo.get(methodAnnotationName);
        return (classInfo == null) ? ClassInfoList.EMPTY_LIST : classInfo.getClassesWithMethodAnnotation();
    }
    
    public ClassInfoList getClassesWithMethodParameterAnnotation(final String methodParameterAnnotationName) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo || !this.scanSpec.enableMethodInfo || !this.scanSpec.enableAnnotationInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo(), #enableMethodInfo(), and #enableAnnotationInfo() before #scan()");
        }
        final ClassInfo classInfo = this.classNameToClassInfo.get(methodParameterAnnotationName);
        return (classInfo == null) ? ClassInfoList.EMPTY_LIST : classInfo.getClassesWithMethodParameterAnnotation();
    }
    
    public ClassInfoList getClassesWithFieldAnnotation(final String fieldAnnotationName) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo || !this.scanSpec.enableFieldInfo || !this.scanSpec.enableAnnotationInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo(), #enableFieldInfo(), and #enableAnnotationInfo() before #scan()");
        }
        final ClassInfo classInfo = this.classNameToClassInfo.get(fieldAnnotationName);
        return (classInfo == null) ? ClassInfoList.EMPTY_LIST : classInfo.getClassesWithFieldAnnotation();
    }
    
    public ClassInfoList getAllInterfaces() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        return ClassInfo.getAllImplementedInterfaceClasses(this.classNameToClassInfo.values(), this.scanSpec);
    }
    
    public ClassInfoList getInterfaces(final String className) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        final ClassInfo classInfo = this.classNameToClassInfo.get(className);
        return (classInfo == null) ? ClassInfoList.EMPTY_LIST : classInfo.getInterfaces();
    }
    
    public ClassInfoList getClassesImplementing(final String interfaceName) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        final ClassInfo classInfo = this.classNameToClassInfo.get(interfaceName);
        return (classInfo == null) ? ClassInfoList.EMPTY_LIST : classInfo.getClassesImplementing();
    }
    
    public ClassInfoList getAllAnnotations() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo || !this.scanSpec.enableAnnotationInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() and #enableAnnotationInfo() before #scan()");
        }
        return ClassInfo.getAllAnnotationClasses(this.classNameToClassInfo.values(), this.scanSpec);
    }
    
    public ClassInfoList getAllInterfacesAndAnnotations() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo || !this.scanSpec.enableAnnotationInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() and #enableAnnotationInfo() before #scan()");
        }
        return ClassInfo.getAllInterfacesOrAnnotationClasses(this.classNameToClassInfo.values(), this.scanSpec);
    }
    
    public ClassInfoList getClassesWithAnnotation(final String annotationName) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo || !this.scanSpec.enableAnnotationInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() and #enableAnnotationInfo() before #scan()");
        }
        final ClassInfo classInfo = this.classNameToClassInfo.get(annotationName);
        return (classInfo == null) ? ClassInfoList.EMPTY_LIST : classInfo.getClassesWithAnnotation();
    }
    
    public ClassInfoList getAnnotationsOnClass(final String className) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo || !this.scanSpec.enableAnnotationInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() and #enableAnnotationInfo() before #scan()");
        }
        final ClassInfo classInfo = this.classNameToClassInfo.get(className);
        return (classInfo == null) ? ClassInfoList.EMPTY_LIST : classInfo.getAnnotations();
    }
    
    public boolean classpathContentsModifiedSinceScan() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (this.fileToLastModified == null) {
            return true;
        }
        for (final Map.Entry<File, Long> ent : this.fileToLastModified.entrySet()) {
            if (ent.getKey().lastModified() != ent.getValue()) {
                return true;
            }
        }
        return false;
    }
    
    public long classpathContentsLastModifiedTime() {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        long maxLastModifiedTime = 0L;
        if (this.fileToLastModified != null) {
            final long currTime = System.currentTimeMillis();
            for (final long timestamp : this.fileToLastModified.values()) {
                if (timestamp > maxLastModifiedTime && timestamp < currTime) {
                    maxLastModifiedTime = timestamp;
                }
            }
        }
        return maxLastModifiedTime;
    }
    
    ClassLoader[] getClassLoaderOrderRespectingParentDelegation() {
        return this.classLoaderOrderRespectingParentDelegation;
    }
    
    public Class<?> loadClass(final String className, final boolean returnNullIfClassNotFound) throws IllegalArgumentException {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (className == null || className.isEmpty()) {
            throw new NullPointerException("className cannot be null or empty");
        }
        try {
            return Class.forName(className, this.scanSpec.initializeLoadedClasses, this.classGraphClassLoader);
        }
        catch (ClassNotFoundException | LinkageError ex) {
            final Throwable t;
            final Throwable e = t;
            if (returnNullIfClassNotFound) {
                return null;
            }
            throw new IllegalArgumentException("Could not load class " + className + " : " + e);
        }
    }
    
    public <T> Class<T> loadClass(final String className, final Class<T> superclassOrInterfaceType, final boolean returnNullIfClassNotFound) throws IllegalArgumentException {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (className == null || className.isEmpty()) {
            throw new NullPointerException("className cannot be null or empty");
        }
        if (superclassOrInterfaceType == null) {
            throw new NullPointerException("superclassOrInterfaceType parameter cannot be null");
        }
        Class<?> loadedClass;
        try {
            loadedClass = Class.forName(className, this.scanSpec.initializeLoadedClasses, this.classGraphClassLoader);
        }
        catch (ClassNotFoundException | LinkageError ex) {
            final Throwable t;
            final Throwable e = t;
            if (returnNullIfClassNotFound) {
                return null;
            }
            throw new IllegalArgumentException("Could not load class " + className + " : " + e);
        }
        if (loadedClass == null || superclassOrInterfaceType.isAssignableFrom(loadedClass)) {
            final Class<T> castClass = (Class<T>)loadedClass;
            return castClass;
        }
        if (returnNullIfClassNotFound) {
            return null;
        }
        throw new IllegalArgumentException("Loaded class " + loadedClass.getName() + " cannot be cast to " + superclassOrInterfaceType.getName());
    }
    
    public static ScanResult fromJSON(final String json) {
        final Matcher matcher = Pattern.compile("\\{[\\n\\r ]*\"format\"[ ]?:[ ]?\"([^\"]+)\"").matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("JSON is not in correct format");
        }
        if (!"10".equals(matcher.group(1))) {
            throw new IllegalArgumentException("JSON was serialized in a different format from the format used by the current version of ClassGraph -- please serialize and deserialize your ScanResult using the same version of ClassGraph");
        }
        final SerializationFormat deserialized = JSONDeserializer.deserializeObject(SerializationFormat.class, json);
        if (!deserialized.format.equals("10")) {
            throw new IllegalArgumentException("JSON was serialized by newer version of ClassGraph");
        }
        final ClassGraph classGraph = new ClassGraph();
        classGraph.scanSpec = deserialized.scanSpec;
        ScanResult scanResult;
        try (final AutoCloseableExecutorService executorService = new AutoCloseableExecutorService(ClassGraph.DEFAULT_NUM_WORKER_THREADS)) {
            scanResult = classGraph.getClasspathScanResult(executorService);
        }
        scanResult.rawClasspathEltOrderStrs = deserialized.classpath;
        scanResult.scanSpec = deserialized.scanSpec;
        scanResult.classNameToClassInfo = new HashMap<String, ClassInfo>();
        if (deserialized.classInfo != null) {
            for (final ClassInfo ci : deserialized.classInfo) {
                scanResult.classNameToClassInfo.put(ci.getName(), ci);
                ci.setScanResult(scanResult);
            }
        }
        scanResult.moduleNameToModuleInfo = new HashMap<String, ModuleInfo>();
        if (deserialized.moduleInfo != null) {
            for (final ModuleInfo mi : deserialized.moduleInfo) {
                scanResult.moduleNameToModuleInfo.put(mi.getName(), mi);
            }
        }
        scanResult.packageNameToPackageInfo = new HashMap<String, PackageInfo>();
        if (deserialized.packageInfo != null) {
            for (final PackageInfo pi : deserialized.packageInfo) {
                scanResult.packageNameToPackageInfo.put(pi.getName(), pi);
            }
        }
        scanResult.indexResourcesAndClassInfo();
        scanResult.isObtainedFromDeserialization = true;
        return scanResult;
    }
    
    public String toJSON(final int indentWidth) {
        if (this.closed.get()) {
            throw new IllegalArgumentException("Cannot use a ScanResult after it has been closed");
        }
        if (!this.scanSpec.enableClassInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableClassInfo() before #scan()");
        }
        final List<ClassInfo> allClassInfo = new ArrayList<ClassInfo>(this.classNameToClassInfo.values());
        CollectionUtils.sortIfNotEmpty(allClassInfo);
        final List<PackageInfo> allPackageInfo = new ArrayList<PackageInfo>(this.packageNameToPackageInfo.values());
        CollectionUtils.sortIfNotEmpty(allPackageInfo);
        final List<ModuleInfo> allModuleInfo = new ArrayList<ModuleInfo>(this.moduleNameToModuleInfo.values());
        CollectionUtils.sortIfNotEmpty(allModuleInfo);
        return JSONSerializer.serializeObject(new SerializationFormat("10", this.scanSpec, allClassInfo, allPackageInfo, allModuleInfo, this.rawClasspathEltOrderStrs), indentWidth, false);
    }
    
    public String toJSON() {
        return this.toJSON(0);
    }
    
    public boolean isObtainedFromDeserialization() {
        return this.isObtainedFromDeserialization;
    }
    
    @Override
    public void close() {
        if (!this.closed.getAndSet(true)) {
            ScanResult.nonClosedWeakReferences.remove(this.weakReference);
            if (this.classpathOrder != null) {
                this.classpathOrder.clear();
                this.classpathOrder = null;
            }
            if (this.allAcceptedResourcesCached != null) {
                for (final Resource classpathResource : this.allAcceptedResourcesCached) {
                    classpathResource.close();
                }
                this.allAcceptedResourcesCached.clear();
                this.allAcceptedResourcesCached = null;
            }
            if (this.pathToAcceptedResourcesCached != null) {
                this.pathToAcceptedResourcesCached.clear();
                this.pathToAcceptedResourcesCached = null;
            }
            this.classGraphClassLoader = null;
            if (this.classNameToClassInfo != null) {}
            if (this.packageNameToPackageInfo != null) {
                this.packageNameToPackageInfo.clear();
                this.packageNameToPackageInfo = null;
            }
            if (this.moduleNameToModuleInfo != null) {
                this.moduleNameToModuleInfo.clear();
                this.moduleNameToModuleInfo = null;
            }
            if (this.fileToLastModified != null) {
                this.fileToLastModified.clear();
                this.fileToLastModified = null;
            }
            if (this.nestedJarHandler != null) {
                this.nestedJarHandler.close(this.topLevelLog);
                this.nestedJarHandler = null;
            }
            this.classGraphClassLoader = null;
            this.classLoaderOrderRespectingParentDelegation = null;
            if (this.topLevelLog != null) {
                this.topLevelLog.flush();
            }
        }
    }
    
    public static void closeAll() {
        for (final WeakReference<ScanResult> nonClosedWeakReference : new ArrayList<WeakReference<ScanResult>>(ScanResult.nonClosedWeakReferences)) {
            final ScanResult scanResult = nonClosedWeakReference.get();
            if (scanResult != null) {
                scanResult.close();
            }
        }
    }
    
    static {
        ScanResult.nonClosedWeakReferences = Collections.newSetFromMap(new ConcurrentHashMap<WeakReference<ScanResult>, Boolean>());
        initialized = new AtomicBoolean(false);
    }
    
    private static class SerializationFormat
    {
        public String format;
        public ScanSpec scanSpec;
        public List<String> classpath;
        public List<ClassInfo> classInfo;
        public List<PackageInfo> packageInfo;
        public List<ModuleInfo> moduleInfo;
        
        public SerializationFormat() {
        }
        
        public SerializationFormat(final String serializationFormatStr, final ScanSpec scanSpec, final List<ClassInfo> classInfo, final List<PackageInfo> packageInfo, final List<ModuleInfo> moduleInfo, final List<String> classpath) {
            this.format = serializationFormatStr;
            this.scanSpec = scanSpec;
            this.classpath = classpath;
            this.classInfo = classInfo;
            this.packageInfo = packageInfo;
            this.moduleInfo = moduleInfo;
        }
    }
}
