// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.fileslice;

import java.nio.ByteBuffer;
import java.io.InputStream;
import nonapi.io.github.classgraph.fileslice.reader.RandomAccessFileChannelReader;
import nonapi.io.github.classgraph.fileslice.reader.RandomAccessReader;
import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.nio.file.OpenOption;
import nonapi.io.github.classgraph.utils.FileUtils;
import nonapi.io.github.classgraph.fastzipfilereader.NestedJarHandler;
import java.util.concurrent.atomic.AtomicBoolean;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.io.Closeable;

public class PathSlice extends Slice implements Closeable
{
    public final Path path;
    private final long fileLength;
    private FileChannel fileChannel;
    private final boolean isTopLevelFileSlice;
    private final AtomicBoolean isClosed;
    
    private PathSlice(final PathSlice parentSlice, final long offset, final long length, final boolean isDeflatedZipEntry, final long inflatedLengthHint, final NestedJarHandler nestedJarHandler) {
        super(parentSlice, offset, length, isDeflatedZipEntry, inflatedLengthHint, nestedJarHandler);
        this.isClosed = new AtomicBoolean();
        this.path = parentSlice.path;
        this.fileChannel = parentSlice.fileChannel;
        this.fileLength = parentSlice.fileLength;
        this.isTopLevelFileSlice = false;
    }
    
    public PathSlice(final Path path, final boolean isDeflatedZipEntry, final long inflatedLengthHint, final NestedJarHandler nestedJarHandler) throws IOException {
        super(0L, isDeflatedZipEntry, inflatedLengthHint, nestedJarHandler);
        this.isClosed = new AtomicBoolean();
        FileUtils.checkCanReadAndIsFile(path);
        this.path = path;
        this.fileChannel = FileChannel.open(path, StandardOpenOption.READ);
        this.fileLength = this.fileChannel.size();
        this.isTopLevelFileSlice = true;
        this.sliceLength = this.fileLength;
        nestedJarHandler.markSliceAsOpen(this);
    }
    
    public PathSlice(final Path path, final NestedJarHandler nestedJarHandler) throws IOException {
        this(path, false, 0L, nestedJarHandler);
    }
    
    @Override
    public Slice slice(final long offset, final long length, final boolean isDeflatedZipEntry, final long inflatedLengthHint) {
        if (this.isDeflatedZipEntry) {
            throw new IllegalArgumentException("Cannot slice a deflated zip entry");
        }
        return new PathSlice(this, offset, length, isDeflatedZipEntry, inflatedLengthHint, this.nestedJarHandler);
    }
    
    @Override
    public RandomAccessReader randomAccessReader() {
        return new RandomAccessFileChannelReader(this.fileChannel, this.sliceStartPos, this.sliceLength);
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
            if (this.isTopLevelFileSlice && this.fileChannel != null) {
                try {
                    this.fileChannel.close();
                }
                catch (IOException ex) {}
                this.fileChannel = null;
            }
            this.fileChannel = null;
            this.nestedJarHandler.markSliceAsClosed(this);
        }
    }
}
