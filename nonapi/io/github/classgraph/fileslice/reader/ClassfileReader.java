// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.fileslice.reader;

import nonapi.io.github.classgraph.utils.StringUtils;
import java.nio.ReadOnlyBufferException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.Arrays;
import nonapi.io.github.classgraph.fileslice.ArraySlice;
import nonapi.io.github.classgraph.fileslice.Slice;
import java.io.InputStream;
import java.io.Closeable;

public class ClassfileReader implements RandomAccessReader, SequentialReader, Closeable
{
    private InputStream inflaterInputStream;
    private RandomAccessReader randomAccessReader;
    private byte[] arr;
    private int arrUsed;
    private int currIdx;
    private int classfileLengthHint;
    private static final int INITIAL_BUF_SIZE = 16384;
    private static final int BUF_CHUNK_SIZE = 8184;
    
    public ClassfileReader(final Slice slice) throws IOException {
        this.classfileLengthHint = -1;
        this.classfileLengthHint = (int)slice.sliceLength;
        if (slice.isDeflatedZipEntry) {
            this.inflaterInputStream = slice.open();
            this.arr = new byte[16384];
            this.classfileLengthHint = (int)Math.min(slice.inflatedLengthHint, 2147483639L);
        }
        else if (slice instanceof ArraySlice) {
            final ArraySlice arraySlice = (ArraySlice)slice;
            if (arraySlice.sliceStartPos == 0L && arraySlice.sliceLength == arraySlice.arr.length) {
                this.arr = arraySlice.arr;
            }
            else {
                this.arr = Arrays.copyOfRange(arraySlice.arr, (int)arraySlice.sliceStartPos, (int)(arraySlice.sliceStartPos + arraySlice.sliceLength));
            }
            this.arrUsed = this.arr.length;
            this.classfileLengthHint = this.arr.length;
        }
        else {
            this.randomAccessReader = slice.randomAccessReader();
            this.arr = new byte[16384];
            this.classfileLengthHint = (int)Math.min(slice.sliceLength, 2147483639L);
        }
    }
    
    public ClassfileReader(final InputStream inputStream) throws IOException {
        this.classfileLengthHint = -1;
        this.inflaterInputStream = inputStream;
        this.arr = new byte[16384];
    }
    
    public int currPos() {
        return this.currIdx;
    }
    
    public byte[] buf() {
        return this.arr;
    }
    
    private void readTo(final int targetArrUsed) throws IOException {
        final int maxArrLen = (this.classfileLengthHint == -1) ? 2147483639 : this.classfileLengthHint;
        if (this.inflaterInputStream == null && this.randomAccessReader == null) {
            throw new IOException("Tried to read past end of fixed array buffer");
        }
        if (targetArrUsed > 2147483639 || targetArrUsed < 0 || this.arrUsed == maxArrLen) {
            throw new IOException("Hit 2GB limit while trying to grow buffer array");
        }
        int maxNewArrUsed;
        long newArrLength;
        for (maxNewArrUsed = (int)Math.min(Math.max(targetArrUsed, (long)(this.arrUsed + 8184)), maxArrLen), newArrLength = this.arr.length; newArrLength < maxNewArrUsed; newArrLength = Math.min(maxNewArrUsed, newArrLength * 2L)) {}
        if (newArrLength > 2147483639L) {
            throw new IOException("Hit 2GB limit while trying to grow buffer array");
        }
        this.arr = Arrays.copyOf(this.arr, (int)Math.min(newArrLength, maxArrLen));
        final int maxBytesToRead = this.arr.length - this.arrUsed;
        if (this.inflaterInputStream != null) {
            final int numRead = this.inflaterInputStream.read(this.arr, this.arrUsed, maxBytesToRead);
            if (numRead > 0) {
                this.arrUsed += numRead;
            }
        }
        else {
            final int bytesToRead = Math.min(maxBytesToRead, maxArrLen - this.arrUsed);
            final int numBytesRead = this.randomAccessReader.read(this.arrUsed, this.arr, this.arrUsed, bytesToRead);
            if (numBytesRead > 0) {
                this.arrUsed += numBytesRead;
            }
        }
        if (this.arrUsed < targetArrUsed) {
            throw new IOException("Buffer underflow");
        }
    }
    
    public void bufferTo(final int numBytes) throws IOException {
        if (numBytes > this.arrUsed) {
            this.readTo(numBytes);
        }
    }
    
    @Override
    public int read(final long srcOffset, final byte[] dstArr, final int dstArrStart, final int numBytes) throws IOException {
        if (numBytes == 0) {
            return 0;
        }
        final int idx = (int)srcOffset;
        if (idx + numBytes > this.arrUsed) {
            this.readTo(idx + numBytes);
        }
        final int numBytesToRead = Math.max(Math.min(numBytes, dstArr.length - dstArrStart), 0);
        if (numBytesToRead == 0) {
            return -1;
        }
        try {
            System.arraycopy(this.arr, idx, dstArr, dstArrStart, numBytesToRead);
            return numBytesToRead;
        }
        catch (IndexOutOfBoundsException e) {
            throw new IOException("Read index out of bounds");
        }
    }
    
    @Override
    public int read(final long srcOffset, final ByteBuffer dstBuf, final int dstBufStart, final int numBytes) throws IOException {
        if (numBytes == 0) {
            return 0;
        }
        final int idx = (int)srcOffset;
        if (idx + numBytes > this.arrUsed) {
            this.readTo(idx + numBytes);
        }
        final int numBytesToRead = Math.max(Math.min(numBytes, dstBuf.capacity() - dstBufStart), 0);
        if (numBytesToRead == 0) {
            return -1;
        }
        try {
            dstBuf.position(dstBufStart);
            dstBuf.limit(dstBufStart + numBytesToRead);
            dstBuf.put(this.arr, idx, numBytesToRead);
            return numBytesToRead;
        }
        catch (BufferUnderflowException | IndexOutOfBoundsException | ReadOnlyBufferException ex2) {
            final RuntimeException ex;
            final RuntimeException e = ex;
            throw new IOException("Read index out of bounds");
        }
    }
    
    @Override
    public byte readByte(final long offset) throws IOException {
        final int idx = (int)offset;
        if (idx + 1 > this.arrUsed) {
            this.readTo(idx + 1);
        }
        return this.arr[idx];
    }
    
    @Override
    public int readUnsignedByte(final long offset) throws IOException {
        final int idx = (int)offset;
        if (idx + 1 > this.arrUsed) {
            this.readTo(idx + 1);
        }
        return this.arr[idx] & 0xFF;
    }
    
    @Override
    public short readShort(final long offset) throws IOException {
        return (short)this.readUnsignedShort(offset);
    }
    
    @Override
    public int readUnsignedShort(final long offset) throws IOException {
        final int idx = (int)offset;
        if (idx + 2 > this.arrUsed) {
            this.readTo(idx + 2);
        }
        return (this.arr[idx] & 0xFF) << 8 | (this.arr[idx + 1] & 0xFF);
    }
    
    @Override
    public int readInt(final long offset) throws IOException {
        final int idx = (int)offset;
        if (idx + 4 > this.arrUsed) {
            this.readTo(idx + 4);
        }
        return (this.arr[idx] & 0xFF) << 24 | (this.arr[idx + 1] & 0xFF) << 16 | (this.arr[idx + 2] & 0xFF) << 8 | (this.arr[idx + 3] & 0xFF);
    }
    
    @Override
    public long readUnsignedInt(final long offset) throws IOException {
        return (long)this.readInt(offset) & 0xFFFFFFFFL;
    }
    
    @Override
    public long readLong(final long offset) throws IOException {
        final int idx = (int)offset;
        if (idx + 8 > this.arrUsed) {
            this.readTo(idx + 8);
        }
        return ((long)this.arr[idx] & 0xFFL) << 56 | ((long)this.arr[idx + 1] & 0xFFL) << 48 | ((long)this.arr[idx + 2] & 0xFFL) << 40 | ((long)this.arr[idx + 3] & 0xFFL) << 32 | ((long)this.arr[idx + 4] & 0xFFL) << 24 | ((long)this.arr[idx + 5] & 0xFFL) << 16 | ((long)this.arr[idx + 6] & 0xFFL) << 8 | ((long)this.arr[idx + 7] & 0xFFL);
    }
    
    @Override
    public byte readByte() throws IOException {
        final byte val = this.readByte(this.currIdx);
        ++this.currIdx;
        return val;
    }
    
    @Override
    public int readUnsignedByte() throws IOException {
        final int val = this.readUnsignedByte(this.currIdx);
        ++this.currIdx;
        return val;
    }
    
    @Override
    public short readShort() throws IOException {
        final short val = this.readShort(this.currIdx);
        this.currIdx += 2;
        return val;
    }
    
    @Override
    public int readUnsignedShort() throws IOException {
        final int val = this.readUnsignedShort(this.currIdx);
        this.currIdx += 2;
        return val;
    }
    
    @Override
    public int readInt() throws IOException {
        final int val = this.readInt(this.currIdx);
        this.currIdx += 4;
        return val;
    }
    
    @Override
    public long readUnsignedInt() throws IOException {
        final long val = this.readUnsignedInt(this.currIdx);
        this.currIdx += 4;
        return val;
    }
    
    @Override
    public long readLong() throws IOException {
        final long val = this.readLong(this.currIdx);
        this.currIdx += 8;
        return val;
    }
    
    @Override
    public void skip(final int bytesToSkip) throws IOException {
        if (bytesToSkip < 0) {
            throw new IllegalArgumentException("Tried to skip a negative number of bytes");
        }
        final int idx = this.currIdx;
        if (idx + bytesToSkip > this.arrUsed) {
            this.readTo(idx + bytesToSkip);
        }
        this.currIdx += bytesToSkip;
    }
    
    @Override
    public String readString(final long offset, final int numBytes, final boolean replaceSlashWithDot, final boolean stripLSemicolon) throws IOException {
        final int idx = (int)offset;
        if (idx + numBytes > this.arrUsed) {
            this.readTo(idx + numBytes);
        }
        return StringUtils.readString(this.arr, idx, numBytes, replaceSlashWithDot, stripLSemicolon);
    }
    
    @Override
    public String readString(final int numBytes, final boolean replaceSlashWithDot, final boolean stripLSemicolon) throws IOException {
        final String val = StringUtils.readString(this.arr, this.currIdx, numBytes, replaceSlashWithDot, stripLSemicolon);
        this.currIdx += numBytes;
        return val;
    }
    
    @Override
    public String readString(final long offset, final int numBytes) throws IOException {
        return this.readString(offset, numBytes, false, false);
    }
    
    @Override
    public String readString(final int numBytes) throws IOException {
        return this.readString(numBytes, false, false);
    }
    
    @Override
    public void close() {
        try {
            if (this.inflaterInputStream != null) {
                this.inflaterInputStream.close();
            }
        }
        catch (Exception ex) {}
    }
}
