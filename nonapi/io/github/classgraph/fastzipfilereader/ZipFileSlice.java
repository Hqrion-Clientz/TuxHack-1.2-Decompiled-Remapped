// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.fastzipfilereader;

import java.util.Objects;
import java.nio.file.Path;
import java.io.File;
import nonapi.io.github.classgraph.scanspec.AcceptReject;
import java.io.IOException;
import nonapi.io.github.classgraph.fileslice.Slice;

public class ZipFileSlice
{
    private final ZipFileSlice parentZipFileSlice;
    protected final PhysicalZipFile physicalZipFile;
    private final String pathWithinParentZipFileSlice;
    public Slice slice;
    
    ZipFileSlice(final PhysicalZipFile physicalZipFile) {
        this.parentZipFileSlice = null;
        this.physicalZipFile = physicalZipFile;
        this.slice = physicalZipFile.slice;
        this.pathWithinParentZipFileSlice = physicalZipFile.getPathStr();
    }
    
    ZipFileSlice(final PhysicalZipFile physicalZipFile, final FastZipEntry zipEntry) {
        this.parentZipFileSlice = zipEntry.parentLogicalZipFile;
        this.physicalZipFile = physicalZipFile;
        this.slice = physicalZipFile.slice;
        this.pathWithinParentZipFileSlice = zipEntry.entryName;
    }
    
    ZipFileSlice(final FastZipEntry zipEntry) throws IOException, InterruptedException {
        this.parentZipFileSlice = zipEntry.parentLogicalZipFile;
        this.physicalZipFile = zipEntry.parentLogicalZipFile.physicalZipFile;
        this.slice = zipEntry.getSlice();
        this.pathWithinParentZipFileSlice = zipEntry.entryName;
    }
    
    ZipFileSlice(final ZipFileSlice other) {
        this.parentZipFileSlice = other.parentZipFileSlice;
        this.physicalZipFile = other.physicalZipFile;
        this.slice = other.slice;
        this.pathWithinParentZipFileSlice = other.pathWithinParentZipFileSlice;
    }
    
    public boolean isAcceptedAndNotRejected(final AcceptReject.AcceptRejectLeafname jarAcceptReject) {
        return jarAcceptReject.isAcceptedAndNotRejected(this.pathWithinParentZipFileSlice) && (this.parentZipFileSlice == null || this.parentZipFileSlice.isAcceptedAndNotRejected(jarAcceptReject));
    }
    
    public ZipFileSlice getParentZipFileSlice() {
        return this.parentZipFileSlice;
    }
    
    public String getPathWithinParentZipFileSlice() {
        return this.pathWithinParentZipFileSlice;
    }
    
    private void appendPath(final StringBuilder buf) {
        if (this.parentZipFileSlice != null) {
            this.parentZipFileSlice.appendPath(buf);
            if (buf.length() > 0) {
                buf.append("!/");
            }
        }
        buf.append(this.pathWithinParentZipFileSlice);
    }
    
    public String getPath() {
        final StringBuilder buf = new StringBuilder();
        this.appendPath(buf);
        return buf.toString();
    }
    
    public File getPhysicalFile() {
        final Path path = this.physicalZipFile.getPath();
        if (path != null) {
            try {
                return path.toFile();
            }
            catch (UnsupportedOperationException e) {
                return null;
            }
        }
        return this.physicalZipFile.getFile();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ZipFileSlice)) {
            return false;
        }
        final ZipFileSlice other = (ZipFileSlice)o;
        return Objects.equals(this.physicalZipFile, other.physicalZipFile) && Objects.equals(this.slice, other.slice) && Objects.equals(this.pathWithinParentZipFileSlice, other.pathWithinParentZipFileSlice);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.physicalZipFile, this.slice, this.pathWithinParentZipFileSlice);
    }
    
    @Override
    public String toString() {
        final String path = this.getPath();
        String fileStr = (this.physicalZipFile.getPath() == null) ? null : this.physicalZipFile.getPath().toString();
        if (fileStr == null) {
            fileStr = ((this.physicalZipFile.getFile() == null) ? null : this.physicalZipFile.getFile().toString());
        }
        return "[" + ((fileStr != null && !fileStr.equals(path)) ? (path + " -> " + fileStr) : path) + " ; byte range: " + this.slice.sliceStartPos + ".." + (this.slice.sliceStartPos + this.slice.sliceLength) + " / " + this.physicalZipFile.length() + "]";
    }
}
