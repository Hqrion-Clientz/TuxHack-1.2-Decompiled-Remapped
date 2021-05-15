// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.fileslice;

import nonapi.io.github.classgraph.fileslice.reader.RandomAccessArrayReader;
import nonapi.io.github.classgraph.fileslice.reader.RandomAccessReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import nonapi.io.github.classgraph.fastzipfilereader.NestedJarHandler;

public class ArraySlice extends Slice
{
    public byte[] arr;
    
    private ArraySlice(final ArraySlice parentSlice, final long offset, final long length, final boolean isDeflatedZipEntry, final long inflatedLengthHint, final NestedJarHandler nestedJarHandler) {
        super(parentSlice, offset, length, isDeflatedZipEntry, inflatedLengthHint, nestedJarHandler);
        this.arr = parentSlice.arr;
    }
    
    public ArraySlice(final byte[] arr, final boolean isDeflatedZipEntry, final long inflatedLengthHint, final NestedJarHandler nestedJarHandler) {
        super(arr.length, isDeflatedZipEntry, inflatedLengthHint, nestedJarHandler);
        this.arr = arr;
    }
    
    @Override
    public Slice slice(final long offset, final long length, final boolean isDeflatedZipEntry, final long inflatedLengthHint) {
        if (this.isDeflatedZipEntry) {
            throw new IllegalArgumentException("Cannot slice a deflated zip entry");
        }
        return new ArraySlice(this, offset, length, isDeflatedZipEntry, inflatedLengthHint, this.nestedJarHandler);
    }
    
    @Override
    public byte[] load() throws IOException {
        if (this.isDeflatedZipEntry) {
            try (final InputStream inputStream = this.open()) {
                return NestedJarHandler.readAllBytesAsArray(inputStream, this.inflatedLengthHint);
            }
        }
        if (this.sliceStartPos == 0L && this.sliceLength == this.arr.length) {
            return this.arr;
        }
        return Arrays.copyOfRange(this.arr, (int)this.sliceStartPos, (int)(this.sliceStartPos + this.sliceLength));
    }
    
    @Override
    public RandomAccessReader randomAccessReader() {
        return new RandomAccessArrayReader(this.arr, (int)this.sliceStartPos, (int)this.sliceLength);
    }
    
    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
