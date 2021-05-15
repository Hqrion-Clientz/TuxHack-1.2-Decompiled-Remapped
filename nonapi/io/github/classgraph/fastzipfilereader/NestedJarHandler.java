// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.fastzipfilereader;

import nonapi.io.github.classgraph.recycler.Resettable;
import java.util.Collection;
import java.util.ArrayList;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import nonapi.io.github.classgraph.fileslice.FileSlice;
import nonapi.io.github.classgraph.fileslice.ArraySlice;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.ZipException;
import java.util.zip.Inflater;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.net.HttpURLConnection;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Paths;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import nonapi.io.github.classgraph.utils.FileUtils;
import java.util.AbstractMap;
import nonapi.io.github.classgraph.utils.JarUtils;
import nonapi.io.github.classgraph.utils.FastPathResolver;
import nonapi.io.github.classgraph.utils.LogNode;
import nonapi.io.github.classgraph.concurrency.InterruptionChecker;
import java.util.concurrent.atomic.AtomicBoolean;
import nonapi.io.github.classgraph.fileslice.Slice;
import java.util.Set;
import io.github.classgraph.ModuleReaderProxy;
import nonapi.io.github.classgraph.recycler.Recycler;
import io.github.classgraph.ModuleRef;
import java.util.Map;
import java.io.IOException;
import java.io.File;
import nonapi.io.github.classgraph.concurrency.SingletonMap;
import nonapi.io.github.classgraph.scanspec.ScanSpec;

public class NestedJarHandler
{
    public final ScanSpec scanSpec;
    private SingletonMap<File, PhysicalZipFile, IOException> canonicalFileToPhysicalZipFileMap;
    private SingletonMap<FastZipEntry, ZipFileSlice, IOException> fastZipEntryToZipFileSliceMap;
    private SingletonMap<ZipFileSlice, LogicalZipFile, IOException> zipFileSliceToLogicalZipFileMap;
    public SingletonMap<String, Map.Entry<LogicalZipFile, String>, IOException> nestedPathToLogicalZipFileAndPackageRootMap;
    public SingletonMap<ModuleRef, Recycler<ModuleReaderProxy, IOException>, IOException> moduleRefToModuleReaderProxyRecyclerMap;
    private Recycler<RecyclableInflater, RuntimeException> inflaterRecycler;
    private Set<Slice> openSlices;
    private Set<File> tempFiles;
    public static final String TEMP_FILENAME_LEAF_SEPARATOR = "---";
    private final AtomicBoolean closed;
    public InterruptionChecker interruptionChecker;
    private static final int DEFAULT_BUFFER_SIZE = 16384;
    private static final int MAX_INITIAL_BUFFER_SIZE = 16777216;
    
    public NestedJarHandler(final ScanSpec scanSpec, final InterruptionChecker interruptionChecker) {
        this.canonicalFileToPhysicalZipFileMap = new SingletonMap<File, PhysicalZipFile, IOException>() {
            @Override
            public PhysicalZipFile newInstance(final File canonicalFile, final LogNode log) throws IOException {
                return new PhysicalZipFile(canonicalFile, NestedJarHandler.this, log);
            }
        };
        this.fastZipEntryToZipFileSliceMap = new SingletonMap<FastZipEntry, ZipFileSlice, IOException>() {
            @Override
            public ZipFileSlice newInstance(final FastZipEntry childZipEntry, final LogNode log) throws IOException, InterruptedException {
                ZipFileSlice childZipEntrySlice;
                if (!childZipEntry.isDeflated) {
                    childZipEntrySlice = new ZipFileSlice(childZipEntry);
                }
                else {
                    if (log != null) {
                        log.log("Inflating nested zip entry: " + childZipEntry + " ; uncompressed size: " + childZipEntry.uncompressedSize);
                    }
                    final PhysicalZipFile physicalZipFile = new PhysicalZipFile(childZipEntry.getSlice().open(), (childZipEntry.uncompressedSize >= 0L && childZipEntry.uncompressedSize <= 2147483639L) ? ((long)(int)childZipEntry.uncompressedSize) : -1L, childZipEntry.entryName, NestedJarHandler.this, log);
                    childZipEntrySlice = new ZipFileSlice(physicalZipFile, childZipEntry);
                }
                return childZipEntrySlice;
            }
        };
        this.zipFileSliceToLogicalZipFileMap = new SingletonMap<ZipFileSlice, LogicalZipFile, IOException>() {
            @Override
            public LogicalZipFile newInstance(final ZipFileSlice zipFileSlice, final LogNode log) throws IOException, InterruptedException {
                return new LogicalZipFile(zipFileSlice, NestedJarHandler.this, log);
            }
        };
        this.nestedPathToLogicalZipFileAndPackageRootMap = new SingletonMap<String, Map.Entry<LogicalZipFile, String>, IOException>() {
            @Override
            public Map.Entry<LogicalZipFile, String> newInstance(final String nestedJarPathRaw, final LogNode log) throws IOException, InterruptedException {
                final String nestedJarPath = FastPathResolver.resolve(nestedJarPathRaw);
                final int lastPlingIdx = nestedJarPath.lastIndexOf(33);
                if (lastPlingIdx < 0) {
                    final boolean isURL = JarUtils.URL_SCHEME_PATTERN.matcher(nestedJarPath).matches();
                    PhysicalZipFile physicalZipFile;
                    if (isURL) {
                        final String scheme = nestedJarPath.substring(0, nestedJarPath.indexOf(58));
                        if (NestedJarHandler.this.scanSpec.allowedURLSchemes == null || !NestedJarHandler.this.scanSpec.allowedURLSchemes.contains(scheme)) {
                            throw new IOException("Scanning of URL scheme \"" + scheme + "\" has not been enabled -- cannot scan classpath element: " + nestedJarPath);
                        }
                        physicalZipFile = NestedJarHandler.this.downloadJarFromURL(nestedJarPath, log);
                    }
                    else {
                        try {
                            final File canonicalFile = new File(nestedJarPath).getCanonicalFile();
                            physicalZipFile = NestedJarHandler.this.canonicalFileToPhysicalZipFileMap.get(canonicalFile, log);
                        }
                        catch (NullSingletonException e) {
                            throw new IOException("Could not get PhysicalZipFile for path " + nestedJarPath + " : " + e);
                        }
                        catch (SecurityException e2) {
                            throw new IOException("Path component " + nestedJarPath + " could not be canonicalized: " + e2);
                        }
                    }
                    final ZipFileSlice topLevelSlice = new ZipFileSlice(physicalZipFile);
                    LogicalZipFile logicalZipFile;
                    try {
                        logicalZipFile = NestedJarHandler.this.zipFileSliceToLogicalZipFileMap.get(topLevelSlice, log);
                    }
                    catch (NullSingletonException e3) {
                        throw new IOException("Could not get toplevel slice " + topLevelSlice + " : " + e3);
                    }
                    return new AbstractMap.SimpleEntry<LogicalZipFile, String>(logicalZipFile, "");
                }
                final String parentPath = nestedJarPath.substring(0, lastPlingIdx);
                String childPath = nestedJarPath.substring(lastPlingIdx + 1);
                childPath = FileUtils.sanitizeEntryPath(childPath, true, true);
                Map.Entry<LogicalZipFile, String> parentLogicalZipFileAndPackageRoot;
                try {
                    parentLogicalZipFileAndPackageRoot = NestedJarHandler.this.nestedPathToLogicalZipFileAndPackageRootMap.get(parentPath, log);
                }
                catch (NullSingletonException e4) {
                    throw new IOException("Could not get parent logical zipfile " + parentPath + " : " + e4);
                }
                final LogicalZipFile parentLogicalZipFile = parentLogicalZipFileAndPackageRoot.getKey();
                boolean isDirectory = false;
                while (childPath.endsWith("/")) {
                    isDirectory = true;
                    childPath = childPath.substring(0, childPath.length() - 1);
                }
                FastZipEntry childZipEntry = null;
                if (!isDirectory) {
                    for (final FastZipEntry entry : parentLogicalZipFile.entries) {
                        if (entry.entryName.equals(childPath)) {
                            childZipEntry = entry;
                            break;
                        }
                    }
                }
                if (childZipEntry == null) {
                    final String childPathPrefix = childPath + "/";
                    for (final FastZipEntry entry2 : parentLogicalZipFile.entries) {
                        if (entry2.entryName.startsWith(childPathPrefix)) {
                            isDirectory = true;
                            break;
                        }
                    }
                }
                if (isDirectory) {
                    if (!childPath.isEmpty()) {
                        if (log != null) {
                            log.log("Path " + childPath + " in jarfile " + parentLogicalZipFile + " is a directory, not a file -- using as package root");
                        }
                        parentLogicalZipFile.classpathRoots.add(childPath);
                    }
                    return new AbstractMap.SimpleEntry<LogicalZipFile, String>(parentLogicalZipFile, childPath);
                }
                if (childZipEntry == null) {
                    throw new IOException("Path " + childPath + " does not exist in jarfile " + parentLogicalZipFile);
                }
                if (!NestedJarHandler.this.scanSpec.scanNestedJars) {
                    throw new IOException("Nested jar scanning is disabled -- skipping nested jar " + nestedJarPath);
                }
                ZipFileSlice childZipEntrySlice;
                try {
                    childZipEntrySlice = NestedJarHandler.this.fastZipEntryToZipFileSliceMap.get(childZipEntry, log);
                }
                catch (NullSingletonException e5) {
                    throw new IOException("Could not get child zip entry slice " + childZipEntry + " : " + e5);
                }
                final LogNode zipSliceLog = (log == null) ? null : log.log("Getting zipfile slice " + childZipEntrySlice + " for nested jar " + childZipEntry.entryName);
                LogicalZipFile childLogicalZipFile;
                try {
                    childLogicalZipFile = NestedJarHandler.this.zipFileSliceToLogicalZipFileMap.get(childZipEntrySlice, zipSliceLog);
                }
                catch (NullSingletonException e6) {
                    throw new IOException("Could not get child logical zipfile " + childZipEntrySlice + " : " + e6);
                }
                return new AbstractMap.SimpleEntry<LogicalZipFile, String>(childLogicalZipFile, "");
            }
        };
        this.moduleRefToModuleReaderProxyRecyclerMap = new SingletonMap<ModuleRef, Recycler<ModuleReaderProxy, IOException>, IOException>() {
            @Override
            public Recycler<ModuleReaderProxy, IOException> newInstance(final ModuleRef moduleRef, final LogNode ignored) {
                return new Recycler<ModuleReaderProxy, IOException>() {
                    @Override
                    public ModuleReaderProxy newInstance() throws IOException {
                        return moduleRef.open();
                    }
                };
            }
        };
        this.inflaterRecycler = new Recycler<RecyclableInflater, RuntimeException>() {
            @Override
            public RecyclableInflater newInstance() throws RuntimeException {
                return new RecyclableInflater();
            }
        };
        this.openSlices = Collections.newSetFromMap(new ConcurrentHashMap<Slice, Boolean>());
        this.tempFiles = Collections.newSetFromMap(new ConcurrentHashMap<File, Boolean>());
        this.closed = new AtomicBoolean(false);
        this.scanSpec = scanSpec;
        this.interruptionChecker = interruptionChecker;
    }
    
    private static String leafname(final String path) {
        return path.substring(path.lastIndexOf(47) + 1);
    }
    
    private String sanitizeFilename(final String filename) {
        return filename.replace('/', '_').replace('\\', '_').replace(':', '_').replace('?', '_').replace('&', '_').replace('=', '_').replace(' ', '_');
    }
    
    public File makeTempFile(final String filePathBase, final boolean onlyUseLeafname) throws IOException {
        final File tempFile = File.createTempFile("ClassGraph--", "---" + this.sanitizeFilename(onlyUseLeafname ? leafname(filePathBase) : filePathBase));
        tempFile.deleteOnExit();
        this.tempFiles.add(tempFile);
        return tempFile;
    }
    
    void removeTempFile(final File tempFile) throws IOException, SecurityException {
        if (this.tempFiles.contains(tempFile)) {
            try {
                Files.delete(tempFile.toPath());
            }
            finally {
                this.tempFiles.remove(tempFile);
            }
            return;
        }
        throw new IOException("Not a temp file: " + tempFile);
    }
    
    public void markSliceAsOpen(final Slice slice) throws IOException {
        this.openSlices.add(slice);
    }
    
    public void markSliceAsClosed(final Slice slice) {
        this.openSlices.remove(slice);
    }
    
    private PhysicalZipFile downloadJarFromURL(final String jarURL, final LogNode log) throws IOException, InterruptedException {
        URL url = null;
        try {
            url = new URL(jarURL);
        }
        catch (MalformedURLException e1) {
            try {
                url = new URI(jarURL).toURL();
            }
            catch (URISyntaxException e2) {
                throw new IOException("Could not parse URL: " + jarURL);
            }
        }
        final String scheme = url.getProtocol();
        if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
            try {
                final Path path = Paths.get(url.toURI());
                final FileSystem fs = path.getFileSystem();
                if (log != null) {
                    log.log("URL " + jarURL + " is backed by filesystem " + fs.getClass().getName());
                }
                return new PhysicalZipFile(path, this, log);
            }
            catch (URISyntaxException e3) {
                throw new IOException("Could not convert URL to URI: " + url);
            }
            catch (FileSystemNotFoundException ex) {}
        }
        final URLConnection conn = url.openConnection();
        HttpURLConnection httpConn = null;
        try {
            long contentLengthHint = -1L;
            if (conn instanceof HttpURLConnection) {
                httpConn = (HttpURLConnection)url.openConnection();
                contentLengthHint = httpConn.getContentLengthLong();
                if (contentLengthHint < -1L) {
                    contentLengthHint = -1L;
                }
            }
            else if (conn.getURL().getProtocol().equalsIgnoreCase("file")) {
                try {
                    final File file = new File(conn.getURL().toURI());
                    return new PhysicalZipFile(file, this, log);
                }
                catch (URISyntaxException ex2) {}
            }
            final LogNode subLog = (log == null) ? null : log.log("Downloading jar from URL " + jarURL);
            try (final InputStream inputStream = conn.getInputStream()) {
                final PhysicalZipFile physicalZipFile = new PhysicalZipFile(inputStream, contentLengthHint, jarURL, this, subLog);
                if (subLog != null) {
                    subLog.addElapsedTime();
                    subLog.log("***** Note that it is time-consuming to scan jars at non-\"file:\" URLs, the URL must be opened (possibly after an http(s) fetch) for every scan, and the same URL must also be separately opened by the ClassLoader *****");
                }
                return physicalZipFile;
            }
            catch (MalformedURLException e4) {
                throw new IOException("Malformed URL: " + jarURL);
            }
        }
        finally {
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
    }
    
    public InputStream openInflaterInputStream(final InputStream rawInputStream) throws IOException {
        return new InputStream() {
            private final RecyclableInflater recyclableInflater = NestedJarHandler.this.inflaterRecycler.acquire();
            private final Inflater inflater = this.recyclableInflater.getInflater();
            private final AtomicBoolean closed = new AtomicBoolean();
            private final byte[] buf = new byte[8192];
            private static final int INFLATE_BUF_SIZE = 8192;
            
            @Override
            public int read() throws IOException {
                if (this.closed.get()) {
                    throw new IOException("Already closed");
                }
                if (this.inflater.finished()) {
                    return -1;
                }
                final int numDeflatedBytesRead = this.read(this.buf, 0, 1);
                if (numDeflatedBytesRead < 0) {
                    return -1;
                }
                return this.buf[0] & 0xFF;
            }
            
            @Override
            public int read(final byte[] outBuf, final int off, final int len) throws IOException {
                if (this.closed.get()) {
                    throw new IOException("Already closed");
                }
                if (len < 0) {
                    throw new IllegalArgumentException("len cannot be negative");
                }
                if (len == 0) {
                    return 0;
                }
                try {
                    int totInflatedBytes = 0;
                    while (!this.inflater.finished() && totInflatedBytes < len) {
                        final int numInflatedBytes = this.inflater.inflate(outBuf, off + totInflatedBytes, len - totInflatedBytes);
                        if (numInflatedBytes == 0) {
                            if (this.inflater.needsDictionary()) {
                                throw new IOException("Inflater needs preset dictionary");
                            }
                            if (!this.inflater.needsInput()) {
                                continue;
                            }
                            final int numRawBytesRead = rawInputStream.read(this.buf, 0, this.buf.length);
                            if (numRawBytesRead == -1) {
                                this.buf[0] = 0;
                                this.inflater.setInput(this.buf, 0, 1);
                            }
                            else {
                                this.inflater.setInput(this.buf, 0, numRawBytesRead);
                            }
                        }
                        else {
                            totInflatedBytes += numInflatedBytes;
                        }
                    }
                    if (totInflatedBytes == 0) {
                        return -1;
                    }
                    return totInflatedBytes;
                }
                catch (DataFormatException e) {
                    throw new ZipException((e.getMessage() != null) ? e.getMessage() : "Invalid deflated zip entry data");
                }
            }
            
            @Override
            public long skip(final long numToSkip) throws IOException {
                if (this.closed.get()) {
                    throw new IOException("Already closed");
                }
                if (numToSkip < 0L) {
                    throw new IllegalArgumentException("numToSkip cannot be negative");
                }
                if (numToSkip == 0L) {
                    return 0L;
                }
                if (this.inflater.finished()) {
                    return -1L;
                }
                long totBytesSkipped = 0L;
                while (true) {
                    final int readLen = (int)Math.min(numToSkip - totBytesSkipped, this.buf.length);
                    final int numBytesRead = this.read(this.buf, 0, readLen);
                    if (numBytesRead <= 0) {
                        break;
                    }
                    totBytesSkipped -= numBytesRead;
                }
                return totBytesSkipped;
            }
            
            @Override
            public int available() throws IOException {
                if (this.closed.get()) {
                    throw new IOException("Already closed");
                }
                return this.inflater.finished() ? 0 : 1;
            }
            
            @Override
            public synchronized void mark(final int readlimit) {
                throw new IllegalArgumentException("Not supported");
            }
            
            @Override
            public synchronized void reset() throws IOException {
                throw new IllegalArgumentException("Not supported");
            }
            
            @Override
            public boolean markSupported() {
                return false;
            }
            
            @Override
            public void close() {
                if (!this.closed.getAndSet(true)) {
                    try {
                        rawInputStream.close();
                    }
                    catch (IOException ex) {}
                    finally {
                        NestedJarHandler.this.inflaterRecycler.recycle(this.recyclableInflater);
                    }
                }
            }
        };
    }
    
    public Slice readAllBytesWithSpilloverToDisk(final InputStream inputStream, final String tempFileBaseName, final long inputStreamLengthHint, final LogNode log) throws IOException {
        try (final InputStream inptStream = inputStream) {
            if (inputStreamLengthHint <= this.scanSpec.maxBufferedJarRAMSize) {
                final int bufSize = (inputStreamLengthHint == -1L) ? this.scanSpec.maxBufferedJarRAMSize : ((inputStreamLengthHint == 0L) ? 16384 : Math.min((int)inputStreamLengthHint, this.scanSpec.maxBufferedJarRAMSize));
                byte[] buf;
                int bufLength;
                int bufBytesUsed;
                int bytesRead;
                for (buf = new byte[bufSize], bufLength = buf.length, bufBytesUsed = 0, bytesRead = 0; (bytesRead = inptStream.read(buf, bufBytesUsed, bufLength - bufBytesUsed)) > 0; bufBytesUsed += bytesRead) {}
                if (bytesRead == 0) {
                    final byte[] overflowBuf = { 0 };
                    final int overflowBufBytesUsed = inptStream.read(overflowBuf, 0, 1);
                    if (overflowBufBytesUsed == 1) {
                        return this.spillToDisk(inptStream, tempFileBaseName, buf, overflowBuf, log);
                    }
                }
                if (bufBytesUsed < buf.length) {
                    buf = Arrays.copyOf(buf, bufBytesUsed);
                }
                return new ArraySlice(buf, false, 0L, this);
            }
            return this.spillToDisk(inptStream, tempFileBaseName, null, null, log);
        }
    }
    
    private FileSlice spillToDisk(final InputStream inputStream, final String tempFileBaseName, final byte[] buf, final byte[] overflowBuf, final LogNode log) throws IOException {
        File tempFile;
        try {
            tempFile = this.makeTempFile(tempFileBaseName, true);
        }
        catch (IOException e) {
            throw new IOException("Could not create temporary file: " + e.getMessage());
        }
        if (log != null) {
            log.log("Could not fit InputStream content into max RAM buffer size, saving to temporary file: " + tempFileBaseName + " -> " + tempFile);
        }
        try (final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            if (buf != null) {
                outputStream.write(buf);
                outputStream.write(overflowBuf);
            }
            final byte[] copyBuf = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(copyBuf, 0, copyBuf.length)) > 0) {
                outputStream.write(copyBuf, 0, bytesRead);
            }
        }
        return new FileSlice(tempFile, this, log);
    }
    
    public static byte[] readAllBytesAsArray(final InputStream inputStream, final long uncompressedLengthHint) throws IOException {
        if (uncompressedLengthHint > 2147483639L) {
            throw new IOException("InputStream is too large to read");
        }
        try (final InputStream inptStream = inputStream) {
            final int bufferSize = (uncompressedLengthHint < 1L) ? 16384 : Math.min((int)uncompressedLengthHint, 16777216);
            byte[] buf = new byte[bufferSize];
            int totBytesRead = 0;
            while (true) {
                final int bytesRead;
                if ((bytesRead = inptStream.read(buf, totBytesRead, buf.length - totBytesRead)) > 0) {
                    totBytesRead += bytesRead;
                }
                else {
                    if (bytesRead < 0) {
                        break;
                    }
                    final int extraByte = inptStream.read();
                    if (extraByte == -1) {
                        break;
                    }
                    if (buf.length == 2147483639) {
                        throw new IOException("InputStream too large to read into array");
                    }
                    buf = Arrays.copyOf(buf, (int)Math.min(buf.length * 2L, 2147483639L));
                    buf[totBytesRead++] = (byte)extraByte;
                }
            }
            return (totBytesRead == buf.length) ? buf : Arrays.copyOf(buf, totBytesRead);
        }
    }
    
    public void close(final LogNode log) {
        if (!this.closed.getAndSet(true)) {
            boolean interrupted = false;
            if (this.moduleRefToModuleReaderProxyRecyclerMap != null) {
                boolean completedWithoutInterruption = false;
                while (!completedWithoutInterruption) {
                    try {
                        for (final Recycler<ModuleReaderProxy, IOException> recycler : this.moduleRefToModuleReaderProxyRecyclerMap.values()) {
                            recycler.forceClose();
                        }
                        completedWithoutInterruption = true;
                    }
                    catch (InterruptedException e2) {
                        interrupted = true;
                    }
                }
                this.moduleRefToModuleReaderProxyRecyclerMap.clear();
                this.moduleRefToModuleReaderProxyRecyclerMap = null;
            }
            if (this.zipFileSliceToLogicalZipFileMap != null) {
                this.zipFileSliceToLogicalZipFileMap.clear();
                this.zipFileSliceToLogicalZipFileMap = null;
            }
            if (this.nestedPathToLogicalZipFileAndPackageRootMap != null) {
                this.nestedPathToLogicalZipFileAndPackageRootMap.clear();
                this.nestedPathToLogicalZipFileAndPackageRootMap = null;
            }
            if (this.canonicalFileToPhysicalZipFileMap != null) {
                this.canonicalFileToPhysicalZipFileMap.clear();
                this.canonicalFileToPhysicalZipFileMap = null;
            }
            if (this.fastZipEntryToZipFileSliceMap != null) {
                this.fastZipEntryToZipFileSliceMap.clear();
                this.fastZipEntryToZipFileSliceMap = null;
            }
            if (this.openSlices != null) {
                while (!this.openSlices.isEmpty()) {
                    for (final Slice slice : new ArrayList<Slice>(this.openSlices)) {
                        try {
                            slice.close();
                        }
                        catch (IOException ex2) {}
                        this.markSliceAsClosed(slice);
                    }
                }
                this.openSlices.clear();
                this.openSlices = null;
            }
            if (this.inflaterRecycler != null) {
                this.inflaterRecycler.forceClose();
                this.inflaterRecycler = null;
            }
            if (this.tempFiles != null) {
                final LogNode rmLog = (this.tempFiles.isEmpty() || log == null) ? null : log.log("Removing temporary files");
                while (!this.tempFiles.isEmpty()) {
                    for (final File tempFile : new ArrayList<File>(this.tempFiles)) {
                        try {
                            this.removeTempFile(tempFile);
                        }
                        catch (IOException | SecurityException ex3) {
                            final Exception ex;
                            final Exception e = ex;
                            if (rmLog == null) {
                                continue;
                            }
                            rmLog.log("Removing temporary file failed: " + tempFile);
                        }
                    }
                }
                this.tempFiles = null;
            }
            if (interrupted) {
                this.interruptionChecker.interrupt();
            }
        }
    }
    
    private static class RecyclableInflater implements Resettable, AutoCloseable
    {
        private final Inflater inflater;
        
        private RecyclableInflater() {
            this.inflater = new Inflater(true);
        }
        
        public Inflater getInflater() {
            return this.inflater;
        }
        
        @Override
        public void reset() {
            this.inflater.reset();
        }
        
        @Override
        public void close() {
            this.inflater.end();
        }
    }
}
