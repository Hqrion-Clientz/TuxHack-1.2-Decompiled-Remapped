// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.fileslice.reader;

import nonapi.io.github.classgraph.utils.StringUtils;
import java.nio.ReadOnlyBufferException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.io.IOException;

public class RandomAccessArrayReader implements RandomAccessReader
{
    private final byte[] arr;
    private final int sliceStartPos;
    private final int sliceLength;
    
    public RandomAccessArrayReader(final byte[] arr, final int sliceStartPos, final int sliceLength) {
        this.arr = arr;
        this.sliceStartPos = sliceStartPos;
        this.sliceLength = sliceLength;
    }
    
    @Override
    public int read(final long srcOffset, final byte[] dstArr, final int dstArrStart, final int numBytes) throws IOException {
        if (numBytes == 0) {
            return 0;
        }
        if (srcOffset < 0L || numBytes < 0 || numBytes > this.sliceLength - srcOffset) {
            throw new IOException("Read index out of bounds");
        }
        try {
            final int numBytesToRead = Math.max(Math.min(numBytes, dstArr.length - dstArrStart), 0);
            if (numBytesToRead == 0) {
                return -1;
            }
            final int srcStart = (int)(this.sliceStartPos + srcOffset);
            System.arraycopy(this.arr, srcStart, dstArr, dstArrStart, numBytesToRead);
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
        if (srcOffset < 0L || numBytes < 0 || numBytes > this.sliceLength - srcOffset) {
            throw new IOException("Read index out of bounds");
        }
        try {
            final int numBytesToRead = Math.max(Math.min(numBytes, dstBuf.capacity() - dstBufStart), 0);
            if (numBytesToRead == 0) {
                return -1;
            }
            try {
                final int srcStart = (int)(this.sliceStartPos + srcOffset);
                dstBuf.position(dstBufStart);
                dstBuf.limit(dstBufStart + numBytesToRead);
                dstBuf.put(this.arr, srcStart, numBytesToRead);
                return numBytesToRead;
            }
            catch (IndexOutOfBoundsException e) {
                throw new IOException("Read index out of bounds");
            }
        }
        catch (BufferUnderflowException ex) {}
        catch (IndexOutOfBoundsException ex2) {}
        catch (ReadOnlyBufferException ex3) {}
    }
    
    @Override
    public byte readByte(final long offset) throws IOException {
        final int idx = this.sliceStartPos + (int)offset;
        return this.arr[idx];
    }
    
    @Override
    public int readUnsignedByte(final long offset) throws IOException {
        final int idx = this.sliceStartPos + (int)offset;
        return this.arr[idx] & 0xFF;
    }
    
    @Override
    public short readShort(final long offset) throws IOException {
        return (short)this.readUnsignedShort(offset);
    }
    
    @Override
    public int readUnsignedShort(final long offset) throws IOException {
        final int idx = this.sliceStartPos + (int)offset;
        return (this.arr[idx + 1] & 0xFF) << 8 | (this.arr[idx] & 0xFF);
    }
    
    @Override
    public int readInt(final long offset) throws IOException {
        final int idx = this.sliceStartPos + (int)offset;
        return (this.arr[idx + 3] & 0xFF) << 24 | (this.arr[idx + 2] & 0xFF) << 16 | (this.arr[idx + 1] & 0xFF) << 8 | (this.arr[idx] & 0xFF);
    }
    
    @Override
    public long readUnsignedInt(final long offset) throws IOException {
        return (long)this.readInt(offset) & 0xFFFFFFFFL;
    }
    
    @Override
    public long readLong(final long offset) throws IOException {
        final int idx = this.sliceStartPos + (int)offset;
        return ((long)this.arr[idx + 7] & 0xFFL) << 56 | ((long)this.arr[idx + 6] & 0xFFL) << 48 | ((long)this.arr[idx + 5] & 0xFFL) << 40 | ((long)this.arr[idx + 4] & 0xFFL) << 32 | ((long)this.arr[idx + 3] & 0xFFL) << 24 | ((long)this.arr[idx + 2] & 0xFFL) << 16 | ((long)this.arr[idx + 1] & 0xFFL) << 8 | ((long)this.arr[idx] & 0xFFL);
    }
    
    @Override
    public String readString(final long offset, final int numBytes, final boolean replaceSlashWithDot, final boolean stripLSemicolon) throws IOException {
        final int idx = this.sliceStartPos + (int)offset;
        return StringUtils.readString(this.arr, idx, numBytes, replaceSlashWithDot, stripLSemicolon);
    }
    
    @Override
    public String readString(final long offset, final int numBytes) throws IOException {
        return this.readString(offset, numBytes, false, false);
    }
}
