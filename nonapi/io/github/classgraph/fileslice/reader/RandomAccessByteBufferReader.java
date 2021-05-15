// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.fileslice.reader;

import nonapi.io.github.classgraph.utils.StringUtils;
import java.nio.ReadOnlyBufferException;
import java.nio.BufferUnderflowException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;

public class RandomAccessByteBufferReader implements RandomAccessReader
{
    private final ByteBuffer byteBuffer;
    private final int sliceStartPos;
    private final int sliceLength;
    
    public RandomAccessByteBufferReader(final ByteBuffer byteBuffer, final long sliceStartPos, final long sliceLength) {
        (this.byteBuffer = byteBuffer.duplicate()).order(ByteOrder.LITTLE_ENDIAN);
        this.sliceStartPos = (int)sliceStartPos;
        this.sliceLength = (int)sliceLength;
        this.byteBuffer.position(this.sliceStartPos);
        this.byteBuffer.limit(this.sliceStartPos + this.sliceLength);
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
            final int srcStart = (int)srcOffset;
            this.byteBuffer.position(this.sliceStartPos + srcStart);
            this.byteBuffer.get(dstArr, dstArrStart, numBytesToRead);
            this.byteBuffer.position(this.sliceStartPos);
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
                this.byteBuffer.position(srcStart);
                dstBuf.position(dstBufStart);
                dstBuf.limit(dstBufStart + numBytesToRead);
                dstBuf.put(this.byteBuffer);
                this.byteBuffer.limit(this.sliceStartPos + this.sliceLength);
                this.byteBuffer.position(this.sliceStartPos);
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
        final int idx = (int)(this.sliceStartPos + offset);
        return this.byteBuffer.get(idx);
    }
    
    @Override
    public int readUnsignedByte(final long offset) throws IOException {
        final int idx = (int)(this.sliceStartPos + offset);
        return this.byteBuffer.get(idx) & 0xFF;
    }
    
    @Override
    public int readUnsignedShort(final long offset) throws IOException {
        final int idx = (int)(this.sliceStartPos + offset);
        return this.byteBuffer.getShort(idx) & 0xFF;
    }
    
    @Override
    public short readShort(final long offset) throws IOException {
        return (short)this.readUnsignedShort(offset);
    }
    
    @Override
    public int readInt(final long offset) throws IOException {
        final int idx = (int)(this.sliceStartPos + offset);
        return this.byteBuffer.getInt(idx);
    }
    
    @Override
    public long readUnsignedInt(final long offset) throws IOException {
        return (long)this.readInt(offset) & 0xFFFFFFFFL;
    }
    
    @Override
    public long readLong(final long offset) throws IOException {
        final int idx = (int)(this.sliceStartPos + offset);
        return this.byteBuffer.getLong(idx);
    }
    
    @Override
    public String readString(final long offset, final int numBytes, final boolean replaceSlashWithDot, final boolean stripLSemicolon) throws IOException {
        final int idx = (int)(this.sliceStartPos + offset);
        final byte[] arr = new byte[numBytes];
        if (this.read(offset, arr, 0, numBytes) < numBytes) {
            throw new IOException("Premature EOF while reading string");
        }
        return StringUtils.readString(arr, idx, numBytes, replaceSlashWithDot, stripLSemicolon);
    }
    
    @Override
    public String readString(final long offset, final int numBytes) throws IOException {
        return this.readString(offset, numBytes, false, false);
    }
}
