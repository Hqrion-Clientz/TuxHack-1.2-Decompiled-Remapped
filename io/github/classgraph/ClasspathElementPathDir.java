// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Objects;
import java.io.IOError;
import java.net.URI;
import java.io.File;
import java.util.List;
import nonapi.io.github.classgraph.utils.VersionFinder;
import java.util.Collections;
import java.util.ArrayList;
import java.io.InputStream;
import nonapi.io.github.classgraph.fileslice.Slice;
import nonapi.io.github.classgraph.fileslice.reader.ClassfileReader;
import java.nio.ByteBuffer;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import nonapi.io.github.classgraph.utils.FastPathResolver;
import java.util.concurrent.atomic.AtomicBoolean;
import nonapi.io.github.classgraph.fileslice.PathSlice;
import java.util.Iterator;
import java.nio.file.DirectoryStream;
import java.io.IOException;
import nonapi.io.github.classgraph.classpath.ClasspathOrder;
import java.nio.file.LinkOption;
import java.nio.file.Files;
import nonapi.io.github.classgraph.utils.FileUtils;
import nonapi.io.github.classgraph.classloaderhandler.ClassLoaderHandlerRegistry;
import nonapi.io.github.classgraph.utils.LogNode;
import nonapi.io.github.classgraph.concurrency.WorkQueue;
import java.util.HashSet;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.fastzipfilereader.NestedJarHandler;
import java.util.Set;
import java.nio.file.Path;

class ClasspathElementPathDir extends ClasspathElement
{
    private final Path classpathEltPath;
    private final Path packageRootPath;
    private final Set<Path> scannedCanonicalPaths;
    private final NestedJarHandler nestedJarHandler;
    
    ClasspathElementPathDir(final Path classpathEltPath, final String packageRoot, final ClassLoader classLoader, final NestedJarHandler nestedJarHandler, final ScanSpec scanSpec) {
        super(classLoader, scanSpec);
        this.scannedCanonicalPaths = new HashSet<Path>();
        this.classpathEltPath = classpathEltPath;
        this.packageRootPath = classpathEltPath.resolve(packageRoot);
        this.nestedJarHandler = nestedJarHandler;
    }
    
    @Override
    void open(final WorkQueue<Scanner.ClasspathEntryWorkUnit> workQueue, final LogNode log) {
        if (!this.scanSpec.scanDirs) {
            if (log != null) {
                this.log(this.classpathElementIdx, "Skipping classpath element, since dir scanning is disabled: " + this.classpathEltPath, log);
            }
            this.skipClasspathElement = true;
            return;
        }
        try {
            int childClasspathEntryIdx = 0;
            for (final String libDirPrefix : ClassLoaderHandlerRegistry.AUTOMATIC_LIB_DIR_PREFIXES) {
                final Path libDirPath = this.classpathEltPath.resolve(libDirPrefix);
                if (FileUtils.canReadAndIsDir(libDirPath)) {
                    try (final DirectoryStream<Path> stream = Files.newDirectoryStream(libDirPath)) {
                        for (final Path filePath : stream) {
                            if (Files.isRegularFile(filePath, new LinkOption[0]) && filePath.getFileName().endsWith(".jar")) {
                                if (log != null) {
                                    this.log(this.classpathElementIdx, "Found lib jar: " + filePath, log);
                                }
                                workQueue.addWorkUnit(new Scanner.ClasspathEntryWorkUnit(new ClasspathOrder.ClasspathElementAndClassLoader(filePath, this.classLoader), this, childClasspathEntryIdx++));
                            }
                        }
                    }
                    catch (IOException ex) {}
                }
            }
            if (this.packageRootPath.equals(this.classpathEltPath)) {
                for (final String packageRootPrefix : ClassLoaderHandlerRegistry.AUTOMATIC_PACKAGE_ROOT_PREFIXES) {
                    final Path packageRoot = this.classpathEltPath.resolve(packageRootPrefix);
                    if (FileUtils.canReadAndIsDir(packageRoot)) {
                        if (log != null) {
                            this.log(this.classpathElementIdx, "Found package root: " + packageRootPrefix, log);
                        }
                        workQueue.addWorkUnit(new Scanner.ClasspathEntryWorkUnit(new ClasspathOrder.ClasspathElementAndClassLoader(this.classpathEltPath, packageRootPrefix, this.classLoader), this, childClasspathEntryIdx++));
                    }
                }
            }
        }
        catch (SecurityException e) {
            if (log != null) {
                this.log(this.classpathElementIdx, "Skipping classpath element, since dir cannot be accessed: " + this.classpathEltPath, log);
            }
            this.skipClasspathElement = true;
        }
    }
    
    private Resource newResource(final Path resourcePath, final NestedJarHandler nestedJarHandler) {
        long length;
        try {
            length = Files.size(resourcePath);
        }
        catch (IOException | SecurityException ex2) {
            final Exception ex;
            final Exception e = ex;
            length = -1L;
        }
        return new Resource(this, length) {
            private PathSlice pathSlice;
            protected AtomicBoolean isOpen = new AtomicBoolean();
            
            @Override
            public String getPath() {
                String path;
                for (path = FastPathResolver.resolve(ClasspathElementPathDir.this.packageRootPath.relativize(resourcePath).toString()); path.startsWith("/"); path = path.substring(1)) {}
                return path;
            }
            
            @Override
            public String getPathRelativeToClasspathElement() {
                String path;
                for (path = FastPathResolver.resolve(ClasspathElementPathDir.this.classpathEltPath.relativize(resourcePath).toString()); path.startsWith("/"); path = path.substring(1)) {}
                return path;
            }
            
            @Override
            public long getLastModified() {
                try {
                    return resourcePath.toFile().lastModified();
                }
                catch (UnsupportedOperationException e) {
                    return 0L;
                }
            }
            
            @Override
            public Set<PosixFilePermission> getPosixFilePermissions() {
                Set<PosixFilePermission> posixFilePermissions = null;
                try {
                    posixFilePermissions = Files.readAttributes(resourcePath, PosixFileAttributes.class, new LinkOption[0]).permissions();
                }
                catch (UnsupportedOperationException ex) {}
                catch (IOException ex2) {}
                catch (SecurityException ex3) {}
                return posixFilePermissions;
            }
            
            @Override
            public ByteBuffer read() throws IOException {
                if (ClasspathElementPathDir.this.skipClasspathElement) {
                    throw new IOException("Parent directory could not be opened");
                }
                if (this.isOpen.getAndSet(true)) {
                    throw new IOException("Resource is already open -- cannot open it again without first calling close()");
                }
                this.pathSlice = new PathSlice(resourcePath, nestedJarHandler);
                this.length = this.pathSlice.sliceLength;
                return this.byteBuffer = this.pathSlice.read();
            }
            
            @Override
            ClassfileReader openClassfile() throws IOException {
                if (ClasspathElementPathDir.this.skipClasspathElement) {
                    throw new IOException("Parent directory could not be opened");
                }
                if (this.isOpen.getAndSet(true)) {
                    throw new IOException("Resource is already open -- cannot open it again without first calling close()");
                }
                this.pathSlice = new PathSlice(resourcePath, nestedJarHandler);
                this.length = this.pathSlice.sliceLength;
                return new ClassfileReader(this.pathSlice);
            }
            
            @Override
            public InputStream open() throws IOException {
                if (ClasspathElementPathDir.this.skipClasspathElement) {
                    throw new IOException("Parent directory could not be opened");
                }
                if (this.isOpen.getAndSet(true)) {
                    throw new IOException("Resource is already open -- cannot open it again without first calling close()");
                }
                this.pathSlice = new PathSlice(resourcePath, nestedJarHandler);
                this.inputStream = this.pathSlice.open();
                this.length = this.pathSlice.sliceLength;
                return this.inputStream;
            }
            
            @Override
            public byte[] load() throws IOException {
                this.read();
                try (final Resource res = this) {
                    this.pathSlice = new PathSlice(resourcePath, nestedJarHandler);
                    final byte[] bytes = this.pathSlice.load();
                    this.length = bytes.length;
                    return bytes;
                }
            }
            
            @Override
            public void close() {
                super.close();
                if (this.isOpen.getAndSet(false)) {
                    if (this.byteBuffer != null) {
                        this.byteBuffer = null;
                    }
                    if (this.pathSlice != null) {
                        this.pathSlice.close();
                        nestedJarHandler.markSliceAsClosed(this.pathSlice);
                        this.pathSlice = null;
                    }
                }
            }
        };
    }
    
    @Override
    Resource getResource(final String relativePath) {
        final Path resourcePath = this.packageRootPath.resolve(relativePath);
        return FileUtils.canReadAndIsFile(resourcePath) ? this.newResource(resourcePath, this.nestedJarHandler) : null;
    }
    
    private void scanPathRecursively(final Path path, final LogNode log) {
        if (this.skipClasspathElement) {
            return;
        }
        try {
            final Path canonicalPath = path.toRealPath(new LinkOption[0]);
            if (!this.scannedCanonicalPaths.add(canonicalPath)) {
                if (log != null) {
                    log.log("Reached symlink cycle, stopping recursion: " + path);
                }
                return;
            }
        }
        catch (IOException | SecurityException ex3) {
            final Exception ex;
            final Exception e = ex;
            if (log != null) {
                log.log("Could not canonicalize path: " + path, e);
            }
            return;
        }
        String dirRelativePathStr;
        for (dirRelativePathStr = FastPathResolver.resolve(this.packageRootPath.relativize(path).toString()); dirRelativePathStr.startsWith("/"); dirRelativePathStr = dirRelativePathStr.substring(1)) {}
        if (!dirRelativePathStr.endsWith("/")) {
            dirRelativePathStr += "/";
        }
        final boolean isDefaultPackage = dirRelativePathStr.equals("/");
        if (this.nestedClasspathRootPrefixes != null && this.nestedClasspathRootPrefixes.contains(dirRelativePathStr)) {
            if (log != null) {
                log.log("Reached nested classpath root, stopping recursion to avoid duplicate scanning: " + dirRelativePathStr);
            }
            return;
        }
        if (dirRelativePathStr.startsWith("META-INF/versions/")) {
            if (log != null) {
                log.log("Found unexpected nested versioned entry in directory classpath element -- skipping: " + dirRelativePathStr);
            }
            return;
        }
        this.checkResourcePathAcceptReject(dirRelativePathStr, log);
        if (this.skipClasspathElement) {
            return;
        }
        final ScanSpec.ScanSpecPathMatch parentMatchStatus = this.scanSpec.dirAcceptMatchStatus(dirRelativePathStr);
        if (parentMatchStatus == ScanSpec.ScanSpecPathMatch.HAS_REJECTED_PATH_PREFIX) {
            if (log != null) {
                log.log("Reached rejected directory, stopping recursive scan: " + dirRelativePathStr);
            }
            return;
        }
        if (parentMatchStatus == ScanSpec.ScanSpecPathMatch.NOT_WITHIN_ACCEPTED_PATH) {
            return;
        }
        Path canonicalPath;
        final LogNode subLog = (log == null) ? null : log.log("1:" + canonicalPath, "Scanning Path: " + FastPathResolver.resolve(path.toString()) + (path.equals(canonicalPath) ? "" : (" ; canonical path: " + FastPathResolver.resolve(canonicalPath.toString()))));
        final List<Path> pathsInDir = new ArrayList<Path>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (final Path subPath : stream) {
                pathsInDir.add(subPath);
            }
        }
        catch (IOException | SecurityException ex4) {
            final Exception ex2;
            final Exception e2 = ex2;
            if (log != null) {
                log.log("Could not read directory " + path + " : " + e2.getMessage());
            }
            this.skipClasspathElement = true;
            return;
        }
        Collections.sort(pathsInDir);
        final boolean isModularJar = VersionFinder.JAVA_MAJOR_VERSION >= 9 && this.getModuleName() != null;
        if (parentMatchStatus != ScanSpec.ScanSpecPathMatch.ANCESTOR_OF_ACCEPTED_PATH) {
            for (final Path subPath2 : pathsInDir) {
                if (Files.isRegularFile(subPath2, new LinkOption[0])) {
                    final Path subPathRelative = this.classpathEltPath.relativize(subPath2);
                    final String subPathRelativeStr = subPathRelative.toString();
                    if (isModularJar && isDefaultPackage && subPathRelativeStr.endsWith(".class") && !subPathRelativeStr.equals("module-info.class")) {
                        continue;
                    }
                    this.checkResourcePathAcceptReject(subPathRelativeStr, subLog);
                    if (this.skipClasspathElement) {
                        return;
                    }
                    if (parentMatchStatus == ScanSpec.ScanSpecPathMatch.HAS_ACCEPTED_PATH_PREFIX || parentMatchStatus == ScanSpec.ScanSpecPathMatch.AT_ACCEPTED_PATH || (parentMatchStatus == ScanSpec.ScanSpecPathMatch.AT_ACCEPTED_CLASS_PACKAGE && this.scanSpec.classfileIsSpecificallyAccepted(subPathRelativeStr))) {
                        final Resource resource = this.newResource(subPath2, this.nestedJarHandler);
                        this.addAcceptedResource(resource, parentMatchStatus, false, subLog);
                        try {
                            this.fileToLastModified.put(subPath2.toFile(), subPath2.toFile().lastModified());
                        }
                        catch (UnsupportedOperationException ex5) {}
                    }
                    else {
                        if (subLog == null) {
                            continue;
                        }
                        subLog.log("Skipping non-accepted file: " + subPathRelative);
                    }
                }
            }
        }
        else if (this.scanSpec.enableClassInfo && dirRelativePathStr.equals("/")) {
            for (final Path subPath2 : pathsInDir) {
                if (subPath2.getFileName().toString().equals("module-info.class") && Files.isRegularFile(subPath2, new LinkOption[0])) {
                    final Resource resource2 = this.newResource(subPath2, this.nestedJarHandler);
                    this.addAcceptedResource(resource2, parentMatchStatus, true, subLog);
                    try {
                        this.fileToLastModified.put(subPath2.toFile(), subPath2.toFile().lastModified());
                    }
                    catch (UnsupportedOperationException ex6) {}
                    break;
                }
            }
        }
        for (final Path subPath2 : pathsInDir) {
            try {
                if (!Files.isDirectory(subPath2, new LinkOption[0])) {
                    continue;
                }
                this.scanPathRecursively(subPath2, subLog);
                if (this.skipClasspathElement) {
                    if (subLog != null) {
                        subLog.addElapsedTime();
                    }
                    return;
                }
                continue;
            }
            catch (SecurityException e3) {
                if (subLog == null) {
                    continue;
                }
                subLog.log("Could not read sub-directory " + subPath2 + " : " + e3.getMessage());
            }
        }
        if (subLog != null) {
            subLog.addElapsedTime();
        }
        try {
            final File file = path.toFile();
            this.fileToLastModified.put(file, file.lastModified());
        }
        catch (UnsupportedOperationException ex7) {}
    }
    
    @Override
    void scanPaths(final LogNode log) {
        if (this.skipClasspathElement) {
            return;
        }
        if (this.scanned.getAndSet(true)) {
            throw new IllegalArgumentException("Already scanned classpath element " + this.toString());
        }
        final LogNode subLog = (log == null) ? null : this.log(this.classpathElementIdx, "Scanning Path classpath element " + this.getURI(), log);
        this.scanPathRecursively(this.packageRootPath, subLog);
        this.finishScanPaths(subLog);
    }
    
    public String getModuleName() {
        return (this.moduleNameFromModuleDescriptor == null || this.moduleNameFromModuleDescriptor.isEmpty()) ? null : this.moduleNameFromModuleDescriptor;
    }
    
    public File getFile() {
        try {
            return this.classpathEltPath.toFile();
        }
        catch (UnsupportedOperationException e) {
            return null;
        }
    }
    
    @Override
    URI getURI() {
        return this.packageRootPath.toUri();
    }
    
    @Override
    public String toString() {
        try {
            return this.packageRootPath.toUri().toString();
        }
        catch (IOError | SecurityException ioError) {
            final Throwable t;
            final Throwable e = t;
            return this.packageRootPath.toString();
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.classpathEltPath, this.packageRootPath);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ClasspathElementPathDir)) {
            return false;
        }
        final ClasspathElementPathDir other = (ClasspathElementPathDir)obj;
        return Objects.equals(this.classpathEltPath, other.classpathEltPath) && Objects.equals(this.packageRootPath, other.packageRootPath);
    }
}
