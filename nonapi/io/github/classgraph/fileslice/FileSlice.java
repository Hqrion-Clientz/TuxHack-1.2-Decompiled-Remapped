// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.fileslice;

import java.io.InputStream;
import nonapi.io.github.classgraph.fileslice.reader.RandomAccessByteBufferReader;
import nonapi.io.github.classgraph.fileslice.reader.RandomAccessFileChannelReader;
import nonapi.io.github.classgraph.fileslice.reader.RandomAccessReader;
import java.io.IOException;
import nonapi.io.github.classgraph.utils.FileUtils;
import nonapi.io.github.classgraph.utils.LogNode;
import nonapi.io.github.classgraph.fastzipfilereader.NestedJarHandler;
import java.util.concurrent.atomic.AtomicBoolean;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.io.RandomAccessFile;
import java.io.File;

public class FileSlice extends Slice
{
    public final File file;
    public RandomAccessFile raf;
    private final long fileLength;
    private FileChannel fileChannel;
    private ByteBuffer backingByteBuffer;
    private final boolean isTopLevelFileSlice;
    private final AtomicBoolean isClosed;
    
    private FileSlice(final FileSlice parentSlice, final long offset, final long length, final boolean isDeflatedZipEntry, final long inflatedLengthHint, final NestedJarHandler nestedJarHandler) {
        super(parentSlice, offset, length, isDeflatedZipEntry, inflatedLengthHint, nestedJarHandler);
        this.isClosed = new AtomicBoolean();
        this.file = parentSlice.file;
        this.raf = parentSlice.raf;
        this.fileChannel = parentSlice.fileChannel;
        this.fileLength = parentSlice.fileLength;
        this.isTopLevelFileSlice = false;
        if (parentSlice.backingByteBuffer != null) {
            (this.backingByteBuffer = parentSlice.backingByteBuffer.duplicate()).position((int)this.sliceStartPos);
            this.backingByteBuffer.limit((int)(this.sliceStartPos + this.sliceLength));
        }
    }
    
    public FileSlice(final File file, final boolean isDeflatedZipEntry, final long inflatedLengthHint, final NestedJarHandler nestedJarHandler, final LogNode log) throws IOException {
        super(file.length(), isDeflatedZipEntry, inflatedLengthHint, nestedJarHandler);
        this.isClosed = new AtomicBoolean();
        FileUtils.checkCanReadAndIsFile(file);
        this.file = file;
        this.raf = new RandomAccessFile(file, "r");
        this.fileChannel = this.raf.getChannel();
        this.fileLength = file.length();
        this.isTopLevelFileSlice = true;
        Label_0180: {
            if (nestedJarHandler.scanSpec.enableMemoryMapping) {
                try {
                    this.backingByteBuffer = this.fileChannel.map(FileChannel.MapMode.READ_ONLY, 0L, this.fileLength);
                }
                catch (IOException | OutOfMemoryError ex) {
                    final Throwable t;
                    final Throwable e = t;
                    System.gc();
                    System.runFinalization();
                    try {
                        this.backingByteBuffer = this.fileChannel.map(FileChannel.MapMode.READ_ONLY, 0L, this.fileLength);
                    }
                    catch (IOException | OutOfMemoryError ex2) {
                        final Throwable t2;
                        final Throwable e2 = t2;
                        if (log == null) {
                            break Label_0180;
                        }
                        log.log("File " + file + " cannot be memory mapped: " + e2 + " (using RandomAccessFile API instead)");
                    }
                }
            }
        }
        nestedJarHandler.markSliceAsOpen(this);
    }
    
    public FileSlice(final File file, final NestedJarHandler nestedJarHandler, final LogNode log) throws IOException {
        this(file, false, 0L, nestedJarHandler, log);
    }
    
    @Override
    public Slice slice(final long offset, final long length, final boolean isDeflatedZipEntry, final long inflatedLengthHint) {
        if (this.isDeflatedZipEntry) {
            throw new IllegalArgumentException("Cannot slice a deflated zip entry");
        }
        return new FileSlice(this, offset, length, isDeflatedZipEntry, inflatedLengthHint, this.nestedJarHandler);
    }
    
    @Override
    public RandomAccessReader randomAccessReader() {
        if (this.backingByteBuffer == null) {
            return new RandomAccessFileChannelReader(this.fileChannel, this.sliceStartPos, this.sliceLength);
        }
        return new RandomAccessByteBufferReader(this.backingByteBuffer, this.sliceStartPos, this.sliceLength);
    }
    
    @Override
    public byte[] load() throws IOException {
        if (this.isDeflatedZipEntry) {
            if (this.inflatedLengthHint > 2147483639L) {
                throw new IOException("Uncompressed size is larger than 2GB");
            }
            try (final InputStream inputStream = this.open()) {
                return NestedJarHandler.readAllBytesAsArray(inputStream, this.inflatedLengthHint);
            }
        }
        if (this.sliceLength > 2147483639L) {
            throw new IOException("File is larger than 2GB");
        }
        final RandomAccessReader reader = this.randomAccessReader();
        final byte[] content = new byte[(int)this.sliceLength];
        if (reader.read(0L, content, 0, content.length) < content.length) {
            throw new IOException("File is truncated");
        }
        return content;
    }
    
    @Override
    public ByteBuffer read() throws IOException {
        if (this.isDeflatedZipEntry) {
            if (this.inflatedLengthHint > 2147483639L) {
                throw new IOException("Uncompressed size is larger than 2GB");
            }
            return ByteBuffer.wrap(this.load());
        }
        else {
            if (this.backingByteBuffer != null) {
                return this.backingByteBuffer.duplicate();
            }
            if (this.sliceLength > 2147483639L) {
                throw new IOException("File is larger than 2GB");
            }
            return ByteBuffer.wrap(this.load());
        }
    }
    
    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public void close() {
        if (!this.isClosed.getAndSet(true)) {
            if (this.isTopLevelFileSlice && this.backingByteBuffer != null) {
                FileUtils.closeDirectByteBuffer(this.backingByteBuffer, null);
            }
            this.backingByteBuffer = null;
            this.fileChannel = null;
            try {
                this.raf.close();
            }
            catch (IOException ex) {}
            this.raf = null;
            this.nestedJarHandler.markSliceAsClosed(this);
        }
    }
}
