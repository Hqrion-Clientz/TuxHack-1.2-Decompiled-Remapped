// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.fileslice.reader;

import nonapi.io.github.classgraph.utils.StringUtils;
import java.nio.BufferUnderflowException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class RandomAccessFileChannelReader implements RandomAccessReader
{
    private final FileChannel fileChannel;
    private final long sliceStartPos;
    private final long sliceLength;
    private ByteBuffer reusableByteBuffer;
    private final byte[] scratchArr;
    private final ByteBuffer scratchByteBuf;
    private byte[] utf8Bytes;
    
    public RandomAccessFileChannelReader(final FileChannel fileChannel, final long sliceStartPos, final long sliceLength) {
        this.scratchArr = new byte[8];
        this.scratchByteBuf = ByteBuffer.wrap(this.scratchArr);
        this.fileChannel = fileChannel;
        this.sliceStartPos = sliceStartPos;
        this.sliceLength = sliceLength;
    }
    
    @Override
    public int read(final long srcOffset, final ByteBuffer dstBuf, final int dstBufStart, final int numBytes) throws IOException {
        if (numBytes == 0) {
            return 0;
        }
        try {
            if (srcOffset < 0L || numBytes < 0 || numBytes > this.sliceLength - srcOffset) {
                throw new IOException("Read index out of bounds");
            }
            final long srcStart = this.sliceStartPos + srcOffset;
            dstBuf.position(dstBufStart);
            dstBuf.limit(dstBufStart + numBytes);
            final int numBytesRead = this.fileChannel.read(dstBuf, srcStart);
            return (numBytesRead == 0) ? -1 : numBytesRead;
        }
        catch (BufferUnderflowException | IndexOutOfBoundsException ex2) {
            final RuntimeException ex;
            final RuntimeException e = ex;
            throw new IOException("Read index out of bounds");
        }
    }
    
    @Override
    public int read(final long srcOffset, final byte[] dstArr, final int dstArrStart, final int numBytes) throws IOException {
        if (numBytes == 0) {
            return 0;
        }
        try {
            if (srcOffset < 0L || numBytes < 0 || numBytes > this.sliceLength - srcOffset) {
                throw new IOException("Read index out of bounds");
            }
            if (this.reusableByteBuffer == null || this.reusableByteBuffer.array() != dstArr) {
                this.reusableByteBuffer = ByteBuffer.wrap(dstArr);
            }
            return this.read(srcOffset, this.reusableByteBuffer, dstArrStart, numBytes);
        }
        catch (BufferUnderflowException | IndexOutOfBoundsException ex2) {
            final RuntimeException ex;
            final RuntimeException e = ex;
            throw new IOException("Read index out of bounds");
        }
    }
    
    @Override
    public byte readByte(final long offset) throws IOException {
        if (this.read(offset, this.scratchByteBuf, 0, 1) < 1) {
            throw new IOException("Premature EOF");
        }
        return this.scratchArr[0];
    }
    
    @Override
    public int readUnsignedByte(final long offset) throws IOException {
        if (this.read(offset, this.scratchByteBuf, 0, 1) < 1) {
            throw new IOException("Premature EOF");
        }
        return this.scratchArr[0] & 0xFF;
    }
    
    @Override
    public short readShort(final long offset) throws IOException {
        return (short)this.readUnsignedShort(offset);
    }
    
    @Override
    public int readUnsignedShort(final long offset) throws IOException {
        if (this.read(offset, this.scratchByteBuf, 0, 2) < 2) {
            throw new IOException("Premature EOF");
        }
        return (this.scratchArr[1] & 0xFF) << 8 | (this.scratchArr[0] & 0xFF);
    }
    
    @Override
    public int readInt(final long offset) throws IOException {
        if (this.read(offset, this.scratchByteBuf, 0, 4) < 4) {
            throw new IOException("Premature EOF");
        }
        return (this.scratchArr[3] & 0xFF) << 24 | (this.scratchArr[2] & 0xFF) << 16 | (this.scratchArr[1] & 0xFF) << 8 | (this.scratchArr[0] & 0xFF);
    }
    
    @Override
    public long readUnsignedInt(final long offset) throws IOException {
        return (long)this.readInt(offset) & 0xFFFFFFFFL;
    }
    
    @Override
    public long readLong(final long offset) throws IOException {
        if (this.read(offset, this.scratchByteBuf, 0, 8) < 8) {
            throw new IOException("Premature EOF");
        }
        return ((long)this.scratchArr[7] & 0xFFL) << 56 | ((long)this.scratchArr[6] & 0xFFL) << 48 | ((long)this.scratchArr[5] & 0xFFL) << 40 | ((long)this.scratchArr[4] & 0xFFL) << 32 | ((long)this.scratchArr[3] & 0xFFL) << 24 | ((long)this.scratchArr[2] & 0xFFL) << 16 | ((long)this.scratchArr[1] & 0xFFL) << 8 | ((long)this.scratchArr[0] & 0xFFL);
    }
    
    @Override
    public String readString(final long offset, final int numBytes, final boolean replaceSlashWithDot, final boolean stripLSemicolon) throws IOException {
        if (this.utf8Bytes == null || this.utf8Bytes.length < numBytes) {
            this.utf8Bytes = new byte[numBytes];
        }
        if (this.read(offset, this.utf8Bytes, 0, numBytes) < numBytes) {
            throw new IOException("Premature EOF");
        }
        return StringUtils.readString(this.utf8Bytes, 0, numBytes, replaceSlashWithDot, stripLSemicolon);
    }
    
    @Override
    public String readString(final long offset, final int numBytes) throws IOException {
        return this.readString(offset, numBytes, false, false);
    }
}
