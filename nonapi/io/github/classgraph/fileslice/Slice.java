// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.fileslice;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import nonapi.io.github.classgraph.fileslice.reader.ClassfileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import nonapi.io.github.classgraph.fileslice.reader.RandomAccessReader;
import java.io.InputStream;
import nonapi.io.github.classgraph.fastzipfilereader.NestedJarHandler;
import java.io.Closeable;

public abstract class Slice implements Closeable
{
    protected final NestedJarHandler nestedJarHandler;
    protected final Slice parentSlice;
    public final long sliceStartPos;
    public long sliceLength;
    public final boolean isDeflatedZipEntry;
    public final long inflatedLengthHint;
    private int hashCode;
    
    protected Slice(final Slice parentSlice, final long offset, final long length, final boolean isDeflatedZipEntry, final long inflatedLengthHint, final NestedJarHandler nestedJarHandler) {
        this.parentSlice = parentSlice;
        final long parentSliceStartPos = (parentSlice == null) ? 0L : parentSlice.sliceStartPos;
        this.sliceStartPos = parentSliceStartPos + offset;
        this.sliceLength = length;
        this.isDeflatedZipEntry = isDeflatedZipEntry;
        this.inflatedLengthHint = inflatedLengthHint;
        this.nestedJarHandler = nestedJarHandler;
        if (this.sliceStartPos < 0L) {
            throw new IllegalArgumentException("Invalid startPos");
        }
        if (length < 0L) {
            throw new IllegalArgumentException("Invalid length");
        }
        if (parentSlice != null && (this.sliceStartPos < parentSliceStartPos || this.sliceStartPos + length > parentSliceStartPos + parentSlice.sliceLength)) {
            throw new IllegalArgumentException("Child slice is not completely contained within parent slice");
        }
    }
    
    protected Slice(final long length, final boolean isDeflatedZipEntry, final long inflatedLengthHint, final NestedJarHandler nestedJarHandler) {
        this(null, 0L, length, isDeflatedZipEntry, inflatedLengthHint, nestedJarHandler);
    }
    
    public abstract Slice slice(final long p0, final long p1, final boolean p2, final long p3);
    
    public InputStream open() throws IOException {
        final InputStream rawInputStream = new InputStream() {
            RandomAccessReader randomAccessReader = Slice.this.randomAccessReader();
            private long currOff;
            private long markOff;
            private final byte[] byteBuf = new byte[1];
            private final AtomicBoolean closed = new AtomicBoolean();
            
            @Override
            public int read() throws IOException {
                if (this.closed.get()) {
                    throw new IOException("Already closed");
                }
                return this.read(this.byteBuf, 0, 1);
            }
            
            @Override
            public int read(final byte[] buf, final int off, final int len) throws IOException {
                if (this.closed.get()) {
                    throw new IOException("Already closed");
                }
                if (len == 0) {
                    return 0;
                }
                final int numBytesToRead = Math.min(len, this.available());
                if (numBytesToRead < 1) {
                    return -1;
                }
                final int numBytesRead = this.randomAccessReader.read(this.currOff, buf, off, numBytesToRead);
                if (numBytesRead > 0) {
                    this.currOff += numBytesRead;
                }
                return numBytesRead;
            }
            
            @Override
            public long skip(final long n) throws IOException {
                if (this.closed.get()) {
                    throw new IOException("Already closed");
                }
                final long newOff = Math.min(this.currOff + n, Slice.this.sliceLength);
                final long skipped = newOff - this.currOff;
                this.currOff = newOff;
                return skipped;
            }
            
            @Override
            public int available() {
                return (int)Math.min(Math.max(Slice.this.sliceLength - this.currOff, 0L), 2147483639L);
            }
            
            @Override
            public synchronized void mark(final int readlimit) {
                this.markOff = this.currOff;
            }
            
            @Override
            public synchronized void reset() {
                this.currOff = this.markOff;
            }
            
            @Override
            public boolean markSupported() {
                return true;
            }
            
            @Override
            public void close() {
                this.closed.getAndSet(true);
            }
        };
        return this.isDeflatedZipEntry ? this.nestedJarHandler.openInflaterInputStream(rawInputStream) : rawInputStream;
    }
    
    public abstract RandomAccessReader randomAccessReader();
    
    public ClassfileReader openClassfileReader() throws IOException {
        if (this.sliceLength > 2147483639L) {
            throw new IllegalArgumentException("Cannot open slices larger than 2GB for sequential buffered reading");
        }
        return new ClassfileReader(this);
    }
    
    public abstract byte[] load() throws IOException;
    
    public String loadAsString() throws IOException {
        return new String(this.load(), StandardCharsets.UTF_8);
    }
    
    public ByteBuffer read() throws IOException {
        return ByteBuffer.wrap(this.load());
    }
    
    @Override
    public void close() throws IOException {
    }
    
    @Override
    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode = (((this.parentSlice == null) ? 1 : this.parentSlice.hashCode()) ^ (int)this.sliceStartPos * 7 ^ (int)this.sliceLength * 15);
            if (this.hashCode == 0) {
                this.hashCode = 1;
            }
        }
        return this.hashCode;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Slice)) {
            return false;
        }
        final Slice other = (Slice)o;
        return this.parentSlice == other.parentSlice && this.sliceStartPos == other.sliceStartPos && this.sliceLength == other.sliceLength;
    }
}
