// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.net.URISyntaxException;
import nonapi.io.github.classgraph.utils.URLPathEncoder;
import java.net.URI;
import nonapi.io.github.classgraph.utils.JarUtils;
import java.io.File;
import nonapi.io.github.classgraph.utils.VersionFinder;
import java.nio.ByteBuffer;
import nonapi.io.github.classgraph.fileslice.reader.ClassfileReader;
import java.io.InputStream;
import java.nio.file.attribute.PosixFilePermission;
import java.util.concurrent.atomic.AtomicBoolean;
import nonapi.io.github.classgraph.fastzipfilereader.ZipFileSlice;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import nonapi.io.github.classgraph.classpath.ClasspathOrder;
import nonapi.io.github.classgraph.classloaderhandler.ClassLoaderHandlerRegistry;
import nonapi.io.github.classgraph.fastzipfilereader.FastZipEntry;
import nonapi.io.github.classgraph.concurrency.SingletonMap;
import java.io.IOException;
import java.util.Map;
import nonapi.io.github.classgraph.utils.FastPathResolver;
import nonapi.io.github.classgraph.utils.FileUtils;
import nonapi.io.github.classgraph.utils.LogNode;
import nonapi.io.github.classgraph.concurrency.WorkQueue;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.fastzipfilereader.NestedJarHandler;
import java.util.concurrent.ConcurrentHashMap;
import nonapi.io.github.classgraph.fastzipfilereader.LogicalZipFile;

class ClasspathElementZip extends ClasspathElement
{
    private final String rawPath;
    LogicalZipFile logicalZipFile;
    private String packageRootPrefix;
    private String zipFilePath;
    private final ConcurrentHashMap<String, Resource> relativePathToResource;
    private final NestedJarHandler nestedJarHandler;
    String moduleNameFromManifestFile;
    private String derivedAutomaticModuleName;
    
    ClasspathElementZip(final Object rawPathObj, final ClassLoader classLoader, final NestedJarHandler nestedJarHandler, final ScanSpec scanSpec) {
        super(classLoader, scanSpec);
        this.packageRootPrefix = "";
        this.relativePathToResource = new ConcurrentHashMap<String, Resource>();
        this.rawPath = rawPathObj.toString();
        this.zipFilePath = this.rawPath;
        this.nestedJarHandler = nestedJarHandler;
    }
    
    @Override
    void open(final WorkQueue<Scanner.ClasspathEntryWorkUnit> workQueue, final LogNode log) throws InterruptedException {
        if (!this.scanSpec.scanJars) {
            if (log != null) {
                this.log(this.classpathElementIdx, "Skipping classpath element, since jar scanning is disabled: " + this.rawPath, log);
            }
            this.skipClasspathElement = true;
            return;
        }
        final LogNode subLog = (log == null) ? null : this.log(this.classpathElementIdx, "Opening jar: " + this.rawPath, log);
        final int plingIdx = this.rawPath.indexOf(33);
        final String outermostZipFilePathResolved = FastPathResolver.resolve(FileUtils.CURR_DIR_PATH, (plingIdx < 0) ? this.rawPath : this.rawPath.substring(0, plingIdx));
        if (!this.scanSpec.jarAcceptReject.isAcceptedAndNotRejected(outermostZipFilePathResolved)) {
            if (subLog != null) {
                subLog.log("Skipping jarfile that is rejected or not accepted: " + this.rawPath);
            }
            this.skipClasspathElement = true;
            return;
        }
        try {
            Map.Entry<LogicalZipFile, String> logicalZipFileAndPackageRoot;
            try {
                logicalZipFileAndPackageRoot = this.nestedJarHandler.nestedPathToLogicalZipFileAndPackageRootMap.get(this.rawPath, subLog);
            }
            catch (SingletonMap.NullSingletonException e) {
                throw new IOException("Could not get logical zipfile " + this.rawPath + " : " + e);
            }
            this.logicalZipFile = logicalZipFileAndPackageRoot.getKey();
            if (this.logicalZipFile == null) {
                throw new IOException("Logical zipfile was null");
            }
            this.zipFilePath = FastPathResolver.resolve(FileUtils.CURR_DIR_PATH, this.logicalZipFile.getPath());
            final String packageRoot = logicalZipFileAndPackageRoot.getValue();
            if (!packageRoot.isEmpty()) {
                this.packageRootPrefix = packageRoot + "/";
            }
        }
        catch (IOException | IllegalArgumentException ex2) {
            final Exception ex;
            final Exception e2 = ex;
            if (subLog != null) {
                subLog.log("Could not open jarfile " + this.rawPath + " : " + e2);
            }
            this.skipClasspathElement = true;
            return;
        }
        if (!this.scanSpec.enableSystemJarsAndModules && this.logicalZipFile.isJREJar) {
            if (subLog != null) {
                subLog.log("Ignoring JRE jar: " + this.rawPath);
            }
            this.skipClasspathElement = true;
            return;
        }
        if (!this.logicalZipFile.isAcceptedAndNotRejected(this.scanSpec.jarAcceptReject)) {
            if (subLog != null) {
                subLog.log("Skipping jarfile that is rejected or not accepted: " + this.rawPath);
            }
            this.skipClasspathElement = true;
            return;
        }
        int childClasspathEntryIdx = 0;
        if (this.scanSpec.scanNestedJars) {
            for (final FastZipEntry zipEntry : this.logicalZipFile.entries) {
                for (final String libDirPrefix : ClassLoaderHandlerRegistry.AUTOMATIC_LIB_DIR_PREFIXES) {
                    if (zipEntry.entryNameUnversioned.startsWith(libDirPrefix) && zipEntry.entryNameUnversioned.endsWith(".jar")) {
                        final String entryPath = zipEntry.getPath();
                        if (subLog != null) {
                            subLog.log("Found nested lib jar: " + entryPath);
                        }
                        workQueue.addWorkUnit(new Scanner.ClasspathEntryWorkUnit(new ClasspathOrder.ClasspathElementAndClassLoader(entryPath, this.classLoader), this, childClasspathEntryIdx++));
                        break;
                    }
                }
            }
        }
        final Set<String> scheduledChildClasspathElements = new HashSet<String>();
        scheduledChildClasspathElements.add(this.rawPath);
        if (this.logicalZipFile.classPathManifestEntryValue != null) {
            final String jarParentDir = FileUtils.getParentDirPath(this.logicalZipFile.getPathWithinParentZipFileSlice());
            for (final String childClassPathEltPathRelative : this.logicalZipFile.classPathManifestEntryValue.split(" ")) {
                if (!childClassPathEltPathRelative.isEmpty()) {
                    final String childClassPathEltPath = FastPathResolver.resolve(jarParentDir, childClassPathEltPathRelative);
                    final ZipFileSlice parentZipFileSlice = this.logicalZipFile.getParentZipFileSlice();
                    final String childClassPathEltPathWithPrefix = (parentZipFileSlice == null) ? childClassPathEltPath : (parentZipFileSlice.getPath() + (childClassPathEltPath.startsWith("/") ? "!" : "!/") + childClassPathEltPath);
                    if (scheduledChildClasspathElements.add(childClassPathEltPathWithPrefix)) {
                        workQueue.addWorkUnit(new Scanner.ClasspathEntryWorkUnit(new ClasspathOrder.ClasspathElementAndClassLoader(childClassPathEltPathWithPrefix, this.classLoader), this, childClasspathEntryIdx++));
                    }
                }
            }
        }
        if (this.logicalZipFile.bundleClassPathManifestEntryValue != null) {
            final String zipFilePathPrefix = this.zipFilePath + "!/";
            for (String childBundlePath : this.logicalZipFile.bundleClassPathManifestEntryValue.split(",")) {
                while (childBundlePath.startsWith("/")) {
                    childBundlePath = childBundlePath.substring(1);
                }
                if (!childBundlePath.isEmpty() && !childBundlePath.equals(".")) {
                    final String childClassPathEltPath = zipFilePathPrefix + FileUtils.sanitizeEntryPath(childBundlePath, true, true);
                    if (scheduledChildClasspathElements.add(childClassPathEltPath)) {
                        workQueue.addWorkUnit(new Scanner.ClasspathEntryWorkUnit(new ClasspathOrder.ClasspathElementAndClassLoader(childClassPathEltPath, this.classLoader), this, childClasspathEntryIdx++));
                    }
                }
            }
        }
    }
    
    private Resource newResource(final FastZipEntry zipEntry, final String pathRelativeToPackageRoot) {
        return new Resource(this, zipEntry.uncompressedSize) {
            protected AtomicBoolean isOpen = new AtomicBoolean();
            
            @Override
            public String getPath() {
                return pathRelativeToPackageRoot;
            }
            
            @Override
            public String getPathRelativeToClasspathElement() {
                return zipEntry.entryName;
            }
            
            @Override
            public long getLastModified() {
                return zipEntry.getLastModifiedTimeMillis();
            }
            
            @Override
            public Set<PosixFilePermission> getPosixFilePermissions() {
                final int fileAttributes = zipEntry.fileAttributes;
                Set<PosixFilePermission> perms;
                if (fileAttributes == 0) {
                    perms = null;
                }
                else {
                    perms = new HashSet<PosixFilePermission>();
                    if ((fileAttributes & 0x100) > 0) {
                        perms.add(PosixFilePermission.OWNER_READ);
                    }
                    if ((fileAttributes & 0x80) > 0) {
                        perms.add(PosixFilePermission.OWNER_WRITE);
                    }
                    if ((fileAttributes & 0x40) > 0) {
                        perms.add(PosixFilePermission.OWNER_EXECUTE);
                    }
                    if ((fileAttributes & 0x20) > 0) {
                        perms.add(PosixFilePermission.GROUP_READ);
                    }
                    if ((fileAttributes & 0x10) > 0) {
                        perms.add(PosixFilePermission.GROUP_WRITE);
                    }
                    if ((fileAttributes & 0x8) > 0) {
                        perms.add(PosixFilePermission.GROUP_EXECUTE);
                    }
                    if ((fileAttributes & 0x4) > 0) {
                        perms.add(PosixFilePermission.OTHERS_READ);
                    }
                    if ((fileAttributes & 0x2) > 0) {
                        perms.add(PosixFilePermission.OTHERS_WRITE);
                    }
                    if ((fileAttributes & 0x1) > 0) {
                        perms.add(PosixFilePermission.OTHERS_EXECUTE);
                    }
                }
                return perms;
            }
            
            @Override
            public InputStream open() throws IOException {
                if (ClasspathElementZip.this.skipClasspathElement) {
                    throw new IOException("Jarfile could not be opened");
                }
                if (this.isOpen.getAndSet(true)) {
                    throw new IOException("Resource is already open -- cannot open it again without first calling close()");
                }
                try {
                    this.inputStream = zipEntry.getSlice().open();
                    this.length = zipEntry.uncompressedSize;
                    return this.inputStream;
                }
                catch (IOException e) {
                    this.close();
                    throw e;
                }
            }
            
            @Override
            ClassfileReader openClassfile() throws IOException {
                return new ClassfileReader(this.open());
            }
            
            @Override
            public ByteBuffer read() throws IOException {
                if (ClasspathElementZip.this.skipClasspathElement) {
                    throw new IOException("Jarfile could not be opened");
                }
                if (this.isOpen.getAndSet(true)) {
                    throw new IOException("Resource is already open -- cannot open it again without first calling close()");
                }
                try {
                    this.byteBuffer = zipEntry.getSlice().read();
                    this.length = this.byteBuffer.remaining();
                    return this.byteBuffer;
                }
                catch (IOException e) {
                    this.close();
                    throw e;
                }
            }
            
            @Override
            public byte[] load() throws IOException {
                if (ClasspathElementZip.this.skipClasspathElement) {
                    throw new IOException("Jarfile could not be opened");
                }
                if (this.isOpen.getAndSet(true)) {
                    throw new IOException("Resource is already open -- cannot open it again without first calling close()");
                }
                try (final Resource res = this) {
                    final byte[] byteArray = zipEntry.getSlice().load();
                    this.length = byteArray.length;
                    return byteArray;
                }
            }
            
            @Override
            public void close() {
                super.close();
                if (this.isOpen.getAndSet(false) && this.byteBuffer != null) {
                    this.byteBuffer = null;
                }
            }
        };
    }
    
    @Override
    Resource getResource(final String relativePath) {
        return this.relativePathToResource.get(relativePath);
    }
    
    @Override
    void scanPaths(final LogNode log) {
        if (this.logicalZipFile == null) {
            this.skipClasspathElement = true;
        }
        if (this.skipClasspathElement) {
            return;
        }
        if (this.scanned.getAndSet(true)) {
            throw new IllegalArgumentException("Already scanned classpath element " + this.getZipFilePath());
        }
        final LogNode subLog = (log == null) ? null : this.log(this.classpathElementIdx, "Scanning jarfile classpath element " + this.getZipFilePath(), log);
        boolean isModularJar = false;
        if (VersionFinder.JAVA_MAJOR_VERSION >= 9) {
            String moduleName = this.moduleNameFromModuleDescriptor;
            if (moduleName == null || moduleName.isEmpty()) {
                moduleName = this.moduleNameFromManifestFile;
            }
            if (moduleName != null && moduleName.isEmpty()) {
                moduleName = null;
            }
            if (moduleName != null) {
                isModularJar = true;
            }
        }
        Set<String> loggedNestedClasspathRootPrefixes = null;
        String prevParentRelativePath = null;
        ScanSpec.ScanSpecPathMatch prevParentMatchStatus = null;
        for (final FastZipEntry zipEntry : this.logicalZipFile.entries) {
            String relativePath = zipEntry.entryNameUnversioned;
            if (relativePath.startsWith("META-INF/versions/")) {
                if (subLog == null) {
                    continue;
                }
                if (VersionFinder.JAVA_MAJOR_VERSION < 9) {
                    subLog.log("Skipping versioned entry in jar, because JRE version " + VersionFinder.JAVA_MAJOR_VERSION + " does not support this: " + relativePath);
                }
                else {
                    subLog.log("Found unexpected versioned entry in jar (the jar's manifest file may be missing the \"Multi-Release\" key) -- skipping: " + relativePath);
                }
            }
            else {
                if (isModularJar && relativePath.indexOf(47) < 0 && relativePath.endsWith(".class") && !relativePath.equals("module-info.class")) {
                    continue;
                }
                if (this.nestedClasspathRootPrefixes != null) {
                    boolean reachedNestedRoot = false;
                    for (final String nestedClasspathRoot : this.nestedClasspathRootPrefixes) {
                        if (relativePath.startsWith(nestedClasspathRoot)) {
                            if (subLog != null) {
                                if (loggedNestedClasspathRootPrefixes == null) {
                                    loggedNestedClasspathRootPrefixes = new HashSet<String>();
                                }
                                if (loggedNestedClasspathRootPrefixes.add(nestedClasspathRoot)) {
                                    subLog.log("Reached nested classpath root, stopping recursion to avoid duplicate scanning: " + nestedClasspathRoot);
                                }
                            }
                            reachedNestedRoot = true;
                            break;
                        }
                    }
                    if (reachedNestedRoot) {
                        continue;
                    }
                }
                if (!this.packageRootPrefix.isEmpty() && !relativePath.startsWith(this.packageRootPrefix)) {
                    continue;
                }
                if (!this.packageRootPrefix.isEmpty()) {
                    relativePath = relativePath.substring(this.packageRootPrefix.length());
                }
                else {
                    for (int i = 0; i < ClassLoaderHandlerRegistry.AUTOMATIC_PACKAGE_ROOT_PREFIXES.length; ++i) {
                        if (relativePath.startsWith(ClassLoaderHandlerRegistry.AUTOMATIC_PACKAGE_ROOT_PREFIXES[i])) {
                            relativePath = relativePath.substring(ClassLoaderHandlerRegistry.AUTOMATIC_PACKAGE_ROOT_PREFIXES[i].length());
                        }
                    }
                }
                this.checkResourcePathAcceptReject(relativePath, log);
                if (this.skipClasspathElement) {
                    return;
                }
                final int lastSlashIdx = relativePath.lastIndexOf(47);
                final String parentRelativePath = (lastSlashIdx < 0) ? "/" : relativePath.substring(0, lastSlashIdx + 1);
                final boolean parentRelativePathChanged = !parentRelativePath.equals(prevParentRelativePath);
                final ScanSpec.ScanSpecPathMatch parentMatchStatus = parentRelativePathChanged ? this.scanSpec.dirAcceptMatchStatus(parentRelativePath) : prevParentMatchStatus;
                prevParentRelativePath = parentRelativePath;
                if ((prevParentMatchStatus = parentMatchStatus) == ScanSpec.ScanSpecPathMatch.HAS_REJECTED_PATH_PREFIX) {
                    if (subLog == null) {
                        continue;
                    }
                    subLog.log("Skipping rejected path: " + relativePath);
                }
                else {
                    final Resource resource = this.newResource(zipEntry, relativePath);
                    if (this.relativePathToResource.putIfAbsent(relativePath, resource) != null) {
                        continue;
                    }
                    if (parentMatchStatus == ScanSpec.ScanSpecPathMatch.HAS_ACCEPTED_PATH_PREFIX || parentMatchStatus == ScanSpec.ScanSpecPathMatch.AT_ACCEPTED_PATH || (parentMatchStatus == ScanSpec.ScanSpecPathMatch.AT_ACCEPTED_CLASS_PACKAGE && this.scanSpec.classfileIsSpecificallyAccepted(relativePath))) {
                        this.addAcceptedResource(resource, parentMatchStatus, false, subLog);
                    }
                    else {
                        if (!this.scanSpec.enableClassInfo || !relativePath.equals("module-info.class")) {
                            continue;
                        }
                        this.addAcceptedResource(resource, parentMatchStatus, true, subLog);
                    }
                }
            }
        }
        final File zipfile = this.getFile();
        if (zipfile != null) {
            this.fileToLastModified.put(zipfile, zipfile.lastModified());
        }
        this.finishScanPaths(subLog);
    }
    
    public String getModuleName() {
        String moduleName = this.moduleNameFromModuleDescriptor;
        if (moduleName == null || moduleName.isEmpty()) {
            moduleName = this.moduleNameFromManifestFile;
        }
        if (moduleName == null || moduleName.isEmpty()) {
            if (this.derivedAutomaticModuleName == null) {
                this.derivedAutomaticModuleName = JarUtils.derivedAutomaticModuleName(this.zipFilePath);
            }
            moduleName = this.derivedAutomaticModuleName;
        }
        return (moduleName == null || moduleName.isEmpty()) ? null : moduleName;
    }
    
    String getZipFilePath() {
        return this.packageRootPrefix.isEmpty() ? this.zipFilePath : (this.zipFilePath + "!/" + this.packageRootPrefix.substring(0, this.packageRootPrefix.length() - 1));
    }
    
    @Override
    URI getURI() {
        try {
            return new URI(URLPathEncoder.normalizeURLPath(this.getZipFilePath()));
        }
        catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not form URI: " + e);
        }
    }
    
    @Override
    File getFile() {
        if (this.logicalZipFile != null) {
            return this.logicalZipFile.getPhysicalFile();
        }
        final int plingIdx = this.rawPath.indexOf(33);
        final String outermostZipFilePathResolved = FastPathResolver.resolve(FileUtils.CURR_DIR_PATH, (plingIdx < 0) ? this.rawPath : this.rawPath.substring(0, plingIdx));
        return new File(outermostZipFilePathResolved);
    }
    
    @Override
    public String toString() {
        return this.getZipFilePath();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ClasspathElementZip)) {
            return false;
        }
        final ClasspathElementZip other = (ClasspathElementZip)obj;
        return this.getZipFilePath().equals(other.getZipFilePath());
    }
    
    @Override
    public int hashCode() {
        return this.getZipFilePath().hashCode();
    }
}
