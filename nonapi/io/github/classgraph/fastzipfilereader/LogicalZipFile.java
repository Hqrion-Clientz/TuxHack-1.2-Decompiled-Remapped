// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.fastzipfilereader;

import java.util.Iterator;
import nonapi.io.github.classgraph.fileslice.reader.RandomAccessReader;
import java.util.HashMap;
import nonapi.io.github.classgraph.utils.Join;
import nonapi.io.github.classgraph.utils.CollectionUtils;
import java.util.Collection;
import java.util.HashSet;
import nonapi.io.github.classgraph.utils.VersionFinder;
import java.io.EOFException;
import nonapi.io.github.classgraph.utils.FileUtils;
import java.util.ArrayList;
import nonapi.io.github.classgraph.fileslice.ArraySlice;
import java.util.AbstractMap;
import java.io.UnsupportedEncodingException;
import io.github.classgraph.ClassGraphException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import nonapi.io.github.classgraph.utils.LogNode;
import java.util.Set;
import java.util.List;

public class LogicalZipFile extends ZipFileSlice
{
    public List<FastZipEntry> entries;
    private boolean isMultiReleaseJar;
    Set<String> classpathRoots;
    public String classPathManifestEntryValue;
    public String bundleClassPathManifestEntryValue;
    public String addExportsManifestEntryValue;
    public String addOpensManifestEntryValue;
    public String automaticModuleNameManifestEntryValue;
    public boolean isJREJar;
    static final String META_INF_PATH_PREFIX = "META-INF/";
    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
    public static final String MULTI_RELEASE_PATH_PREFIX = "META-INF/versions/";
    private static final byte[] IMPLEMENTATION_TITLE_KEY;
    private static final byte[] SPECIFICATION_TITLE_KEY;
    private static final byte[] CLASS_PATH_KEY;
    private static final byte[] BUNDLE_CLASSPATH_KEY;
    private static final byte[] SPRING_BOOT_CLASSES_KEY;
    private static final byte[] SPRING_BOOT_LIB_KEY;
    private static final byte[] MULTI_RELEASE_KEY;
    private static final byte[] ADD_EXPORTS_KEY;
    private static final byte[] ADD_OPENS_KEY;
    private static final byte[] AUTOMATIC_MODULE_NAME_KEY;
    private static byte[] toLowerCase;
    
    LogicalZipFile(final ZipFileSlice zipFileSlice, final NestedJarHandler nestedJarHandler, final LogNode log) throws IOException, InterruptedException {
        super(zipFileSlice);
        this.classpathRoots = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        this.readCentralDirectory(nestedJarHandler, log);
    }
    
    private static Map.Entry<String, Integer> getManifestValue(final byte[] manifest, final int startIdx) {
        int curr;
        int len;
        for (curr = startIdx, len = manifest.length; curr < len && manifest[curr] == 32; ++curr) {}
        final int firstNonSpaceIdx = curr;
        boolean isMultiLine = false;
        while (curr < len && !isMultiLine) {
            final byte b = manifest[curr];
            if (b == 13 && curr < len - 1 && manifest[curr + 1] == 10) {
                if (curr < len - 2 && manifest[curr + 2] == 32) {
                    isMultiLine = true;
                    break;
                }
                break;
            }
            else if (b == 13 || b == 10) {
                if (curr < len - 1 && manifest[curr + 1] == 32) {
                    isMultiLine = true;
                    break;
                }
                break;
            }
            else {
                ++curr;
            }
        }
        String val;
        if (!isMultiLine) {
            val = new String(manifest, firstNonSpaceIdx, curr - firstNonSpaceIdx, StandardCharsets.UTF_8);
        }
        else {
            final ByteArrayOutputStream buf = new ByteArrayOutputStream();
            for (curr = firstNonSpaceIdx; curr < len; ++curr) {
                final byte b2 = manifest[curr];
                boolean isLineEnd;
                if (b2 == 13 && curr < len - 1 && manifest[curr + 1] == 10) {
                    curr += 2;
                    isLineEnd = true;
                }
                else if (b2 == 13 || b2 == 10) {
                    ++curr;
                    isLineEnd = true;
                }
                else {
                    buf.write(b2);
                    isLineEnd = false;
                }
                if (isLineEnd && curr < len && manifest[curr] != 32) {
                    break;
                }
            }
            try {
                val = buf.toString("UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                throw ClassGraphException.newClassGraphException("UTF-8 encoding unsupported", e);
            }
        }
        return new AbstractMap.SimpleEntry<String, Integer>(val.endsWith(" ") ? val.trim() : val, curr);
    }
    
    private static byte[] manifestKeyToBytes(final String key) {
        final byte[] bytes = new byte[key.length()];
        for (int i = 0; i < key.length(); ++i) {
            bytes[i] = (byte)Character.toLowerCase(key.charAt(i));
        }
        return bytes;
    }
    
    private static boolean keyMatchesAtPosition(final byte[] manifest, final byte[] key, final int pos) {
        if (pos + key.length + 1 > manifest.length || manifest[pos + key.length] != 58) {
            return false;
        }
        for (int i = 0; i < key.length; ++i) {
            if (LogicalZipFile.toLowerCase[manifest[i + pos]] != key[i]) {
                return false;
            }
        }
        return true;
    }
    
    private void parseManifest(final FastZipEntry manifestZipEntry, final LogNode log) throws IOException, InterruptedException {
        final byte[] manifest = manifestZipEntry.getSlice().load();
        int i = 0;
        while (i < manifest.length) {
            boolean skip = false;
            if (manifest[i] == 10 || manifest[i] == 13) {
                skip = true;
            }
            else if (keyMatchesAtPosition(manifest, LogicalZipFile.IMPLEMENTATION_TITLE_KEY, i)) {
                final Map.Entry<String, Integer> manifestValueAndEndIdx = getManifestValue(manifest, i + LogicalZipFile.IMPLEMENTATION_TITLE_KEY.length + 1);
                if (manifestValueAndEndIdx.getKey().equalsIgnoreCase("Java Runtime Environment")) {
                    this.isJREJar = true;
                }
                i = manifestValueAndEndIdx.getValue();
            }
            else if (keyMatchesAtPosition(manifest, LogicalZipFile.SPECIFICATION_TITLE_KEY, i)) {
                final Map.Entry<String, Integer> manifestValueAndEndIdx = getManifestValue(manifest, i + LogicalZipFile.SPECIFICATION_TITLE_KEY.length + 1);
                if (manifestValueAndEndIdx.getKey().equalsIgnoreCase("Java Platform API Specification")) {
                    this.isJREJar = true;
                }
                i = manifestValueAndEndIdx.getValue();
            }
            else if (keyMatchesAtPosition(manifest, LogicalZipFile.CLASS_PATH_KEY, i)) {
                final Map.Entry<String, Integer> manifestValueAndEndIdx = getManifestValue(manifest, i + LogicalZipFile.CLASS_PATH_KEY.length + 1);
                this.classPathManifestEntryValue = manifestValueAndEndIdx.getKey();
                if (log != null) {
                    log.log("Found Class-Path entry in manifest file: " + this.classPathManifestEntryValue);
                }
                i = manifestValueAndEndIdx.getValue();
            }
            else if (keyMatchesAtPosition(manifest, LogicalZipFile.BUNDLE_CLASSPATH_KEY, i)) {
                final Map.Entry<String, Integer> manifestValueAndEndIdx = getManifestValue(manifest, i + LogicalZipFile.BUNDLE_CLASSPATH_KEY.length + 1);
                this.bundleClassPathManifestEntryValue = manifestValueAndEndIdx.getKey();
                if (log != null) {
                    log.log("Found Bundle-ClassPath entry in manifest file: " + this.bundleClassPathManifestEntryValue);
                }
                i = manifestValueAndEndIdx.getValue();
            }
            else if (keyMatchesAtPosition(manifest, LogicalZipFile.SPRING_BOOT_CLASSES_KEY, i)) {
                final Map.Entry<String, Integer> manifestValueAndEndIdx = getManifestValue(manifest, i + LogicalZipFile.SPRING_BOOT_CLASSES_KEY.length + 1);
                final String springBootClassesFieldVal = manifestValueAndEndIdx.getKey();
                if (!springBootClassesFieldVal.equals("BOOT-INF/classes") && !springBootClassesFieldVal.equals("BOOT-INF/classes/") && !springBootClassesFieldVal.equals("WEB-INF/classes") && !springBootClassesFieldVal.equals("WEB-INF/classes/")) {
                    throw new IOException("Spring boot classes are at \"" + springBootClassesFieldVal + "\" rather than the standard location \"BOOT-INF/classes/\" or \"WEB-INF/classes/\" -- please report this at https://github.com/classgraph/classgraph/issues");
                }
                i = manifestValueAndEndIdx.getValue();
            }
            else if (keyMatchesAtPosition(manifest, LogicalZipFile.SPRING_BOOT_LIB_KEY, i)) {
                final Map.Entry<String, Integer> manifestValueAndEndIdx = getManifestValue(manifest, i + LogicalZipFile.SPRING_BOOT_LIB_KEY.length + 1);
                final String springBootLibFieldVal = manifestValueAndEndIdx.getKey();
                if (!springBootLibFieldVal.equals("BOOT-INF/lib") && !springBootLibFieldVal.equals("BOOT-INF/lib/") && !springBootLibFieldVal.equals("WEB-INF/lib") && !springBootLibFieldVal.equals("WEB-INF/lib/")) {
                    throw new IOException("Spring boot lib jars are at \"" + springBootLibFieldVal + "\" rather than the standard location \"BOOT-INF/lib/\" or \"WEB-INF/lib/\" -- please report this at https://github.com/classgraph/classgraph/issues");
                }
                i = manifestValueAndEndIdx.getValue();
            }
            else if (keyMatchesAtPosition(manifest, LogicalZipFile.MULTI_RELEASE_KEY, i)) {
                final Map.Entry<String, Integer> manifestValueAndEndIdx = getManifestValue(manifest, i + LogicalZipFile.MULTI_RELEASE_KEY.length + 1);
                if (manifestValueAndEndIdx.getKey().equalsIgnoreCase("true")) {
                    this.isMultiReleaseJar = true;
                }
                i = manifestValueAndEndIdx.getValue();
            }
            else if (keyMatchesAtPosition(manifest, LogicalZipFile.ADD_EXPORTS_KEY, i)) {
                final Map.Entry<String, Integer> manifestValueAndEndIdx = getManifestValue(manifest, i + LogicalZipFile.ADD_EXPORTS_KEY.length + 1);
                this.addExportsManifestEntryValue = manifestValueAndEndIdx.getKey();
                if (log != null) {
                    log.log("Found Add-Exports entry in manifest file: " + this.addExportsManifestEntryValue);
                }
                i = manifestValueAndEndIdx.getValue();
            }
            else if (keyMatchesAtPosition(manifest, LogicalZipFile.ADD_OPENS_KEY, i)) {
                final Map.Entry<String, Integer> manifestValueAndEndIdx = getManifestValue(manifest, i + LogicalZipFile.ADD_OPENS_KEY.length + 1);
                this.addExportsManifestEntryValue = manifestValueAndEndIdx.getKey();
                if (log != null) {
                    log.log("Found Add-Opens entry in manifest file: " + this.addOpensManifestEntryValue);
                }
                i = manifestValueAndEndIdx.getValue();
            }
            else if (keyMatchesAtPosition(manifest, LogicalZipFile.AUTOMATIC_MODULE_NAME_KEY, i)) {
                final Map.Entry<String, Integer> manifestValueAndEndIdx = getManifestValue(manifest, i + LogicalZipFile.AUTOMATIC_MODULE_NAME_KEY.length + 1);
                this.automaticModuleNameManifestEntryValue = manifestValueAndEndIdx.getKey();
                if (log != null) {
                    log.log("Found Automatic-Module-Name entry in manifest file: " + this.automaticModuleNameManifestEntryValue);
                }
                i = manifestValueAndEndIdx.getValue();
            }
            else {
                skip = true;
            }
            if (skip) {
                while (i < manifest.length - 2) {
                    if (manifest[i] == 13 && manifest[i + 1] == 10 && manifest[i + 2] != 32) {
                        i += 2;
                        break;
                    }
                    if ((manifest[i] == 13 || manifest[i] == 10) && manifest[i + 1] != 32) {
                        ++i;
                        break;
                    }
                    ++i;
                }
                if (i >= manifest.length - 2) {
                    break;
                }
                continue;
            }
        }
    }
    
    private void readCentralDirectory(final NestedJarHandler nestedJarHandler, final LogNode log) throws IOException, InterruptedException {
        if (this.slice.sliceLength < 22L) {
            throw new IOException("Zipfile too short to have a central directory");
        }
        final RandomAccessReader reader = this.slice.randomAccessReader();
        long eocdPos = -1L;
        for (long i = this.slice.sliceLength - 22L, iMin = this.slice.sliceLength - 22L - 32L; i >= iMin && i >= 0L; --i) {
            if (reader.readInt(i) == 101010256) {
                eocdPos = i;
                break;
            }
        }
        if (eocdPos < 0L && this.slice.sliceLength > 54L) {
            final int bytesToRead = (int)Math.min(this.slice.sliceLength, 65536L);
            final byte[] eocdBytes = new byte[bytesToRead];
            final long readStartOff = this.slice.sliceLength - bytesToRead;
            if (reader.read(readStartOff, eocdBytes, 0, bytesToRead) < bytesToRead) {
                throw new IOException("Zipfile is truncated");
            }
            try (final ArraySlice arraySlice = new ArraySlice(eocdBytes, false, 0L, nestedJarHandler)) {
                final RandomAccessReader eocdReader = arraySlice.randomAccessReader();
                for (long j = eocdBytes.length - 22L; j >= 0L; --j) {
                    if (eocdReader.readInt(j) == 101010256) {
                        eocdPos = j + readStartOff;
                        break;
                    }
                }
            }
        }
        if (eocdPos < 0L) {
            throw new IOException("Jarfile central directory signature not found: " + this.getPath());
        }
        long numEnt = reader.readUnsignedShort(eocdPos + 8L);
        if (reader.readUnsignedShort(eocdPos + 4L) > 0 || reader.readUnsignedShort(eocdPos + 6L) > 0 || numEnt != reader.readUnsignedShort(eocdPos + 10L)) {
            throw new IOException("Multi-disk jarfiles not supported: " + this.getPath());
        }
        long cenSize = reader.readUnsignedInt(eocdPos + 12L);
        if (cenSize > eocdPos) {
            throw new IOException("Central directory size out of range: " + cenSize + " vs. " + eocdPos + ": " + this.getPath());
        }
        long cenOff = reader.readUnsignedInt(eocdPos + 16L);
        long cenPos = eocdPos - cenSize;
        final long zip64cdLocIdx = eocdPos - 20L;
        if (zip64cdLocIdx >= 0L && reader.readInt(zip64cdLocIdx) == 117853008) {
            if (reader.readInt(zip64cdLocIdx + 4L) > 0 || reader.readInt(zip64cdLocIdx + 16L) > 1) {
                throw new IOException("Multi-disk jarfiles not supported: " + this.getPath());
            }
            final long eocdPos2 = reader.readLong(zip64cdLocIdx + 8L);
            if (reader.readInt(eocdPos2) != 101075792) {
                throw new IOException("Zip64 central directory at location " + eocdPos2 + " does not have Zip64 central directory header: " + this.getPath());
            }
            final long numEnt2 = reader.readLong(eocdPos2 + 24L);
            if (reader.readInt(eocdPos2 + 16L) > 0 || reader.readInt(eocdPos2 + 20L) > 0 || numEnt2 != reader.readLong(eocdPos2 + 32L)) {
                throw new IOException("Multi-disk jarfiles not supported: " + this.getPath());
            }
            if (numEnt == 65535L) {
                numEnt = numEnt2;
            }
            else if (numEnt != numEnt2) {
                numEnt = -1L;
            }
            final long cenSize2 = reader.readLong(eocdPos2 + 40L);
            if (cenSize == 4294967295L) {
                cenSize = cenSize2;
            }
            else if (cenSize != cenSize2) {
                throw new IOException("Mismatch in central directory size: " + cenSize + " vs. " + cenSize2 + ": " + this.getPath());
            }
            cenPos = eocdPos2 - cenSize;
            final long cenOff2 = reader.readLong(eocdPos2 + 48L);
            if (cenOff == 4294967295L) {
                cenOff = cenOff2;
            }
            else if (cenOff != cenOff2) {
                throw new IOException("Mismatch in central directory offset: " + cenOff + " vs. " + cenOff2 + ": " + this.getPath());
            }
        }
        final long locPos = cenPos - cenOff;
        if (locPos < 0L) {
            throw new IOException("Local file header offset out of range: " + locPos + ": " + this.getPath());
        }
        RandomAccessReader cenReader;
        if (cenSize > 2147483639L) {
            cenReader = this.slice.slice(cenPos, cenSize, false, 0L).randomAccessReader();
        }
        else {
            final byte[] entryBytes = new byte[(int)cenSize];
            if (reader.read(cenPos, entryBytes, 0, (int)cenSize) < cenSize) {
                throw new IOException("Zipfile is truncated");
            }
            cenReader = new ArraySlice(entryBytes, false, 0L, nestedJarHandler).randomAccessReader();
        }
        if (numEnt == -1L) {
            numEnt = 0L;
            int filenameLen;
            int extraFieldLen;
            int commentLen;
            for (long entOff = 0L; entOff + 46L <= cenSize; entOff += 46 + filenameLen + extraFieldLen + commentLen, ++numEnt) {
                final int sig = cenReader.readInt(entOff);
                if (sig != 33639248) {
                    throw new IOException("Invalid central directory signature: 0x" + Integer.toString(sig, 16) + ": " + this.getPath());
                }
                filenameLen = cenReader.readUnsignedShort(entOff + 28L);
                extraFieldLen = cenReader.readUnsignedShort(entOff + 30L);
                commentLen = cenReader.readUnsignedShort(entOff + 32L);
            }
        }
        if (numEnt > 2147483639L) {
            throw new IOException("Too many zipfile entries: " + numEnt);
        }
        if (numEnt > cenSize / 46L) {
            throw new IOException("Too many zipfile entries: " + numEnt + " (expected a max of " + cenSize / 46L + " based on central directory size)");
        }
        this.entries = new ArrayList<FastZipEntry>((int)numEnt);
        FastZipEntry manifestZipEntry = null;
        try {
            int entSize = 0;
            long entOff2 = 0L;
            while (entOff2 + 46L <= cenSize) {
                final int sig2 = cenReader.readInt(entOff2);
                if (sig2 != 33639248) {
                    throw new IOException("Invalid central directory signature: 0x" + Integer.toString(sig2, 16) + ": " + this.getPath());
                }
                final int filenameLen2 = cenReader.readUnsignedShort(entOff2 + 28L);
                final int extraFieldLen2 = cenReader.readUnsignedShort(entOff2 + 30L);
                final int commentLen2 = cenReader.readUnsignedShort(entOff2 + 32L);
                entSize = 46 + filenameLen2 + extraFieldLen2 + commentLen2;
                final long filenameStartOff = entOff2 + 46L;
                final long filenameEndOff = filenameStartOff + filenameLen2;
                if (filenameEndOff > cenSize) {
                    if (log != null) {
                        log.log("Filename extends past end of entry -- skipping entry at offset " + entOff2);
                        break;
                    }
                    break;
                }
                else {
                    final String entryName = cenReader.readString(filenameStartOff, filenameLen2);
                    String entryNameSanitized = FileUtils.sanitizeEntryPath(entryName, true, false);
                    if (!entryNameSanitized.isEmpty()) {
                        if (!entryName.endsWith("/")) {
                            final int flags = cenReader.readUnsignedShort(entOff2 + 8L);
                            if ((flags & 0x1) != 0x0) {
                                if (log != null) {
                                    log.log("Skipping encrypted zip entry: " + entryNameSanitized);
                                }
                            }
                            else {
                                final int compressionMethod = cenReader.readUnsignedShort(entOff2 + 10L);
                                if (compressionMethod != 0 && compressionMethod != 8) {
                                    if (log != null) {
                                        log.log("Skipping zip entry with invalid compression method " + compressionMethod + ": " + entryNameSanitized);
                                    }
                                }
                                else {
                                    final boolean isDeflated = compressionMethod == 8;
                                    long compressedSize = cenReader.readUnsignedInt(entOff2 + 20L);
                                    long uncompressedSize = cenReader.readUnsignedInt(entOff2 + 24L);
                                    final int fileAttributes = cenReader.readUnsignedShort(entOff2 + 40L);
                                    long pos = cenReader.readInt(entOff2 + 42L);
                                    long lastModifiedMillis = 0L;
                                    if (extraFieldLen2 > 0) {
                                        int extraFieldOff = 0;
                                        while (extraFieldOff + 4 < extraFieldLen2) {
                                            final long tagOff = filenameEndOff + extraFieldOff;
                                            final int tag = cenReader.readUnsignedShort(tagOff);
                                            final int size = cenReader.readUnsignedShort(tagOff + 2L);
                                            if (extraFieldOff + 4 + size > extraFieldLen2) {
                                                if (log != null) {
                                                    log.log("Skipping zip entry with invalid extra field size: " + entryNameSanitized);
                                                    break;
                                                }
                                                break;
                                            }
                                            else if (tag == 1 && size >= 20) {
                                                final long uncompressedSize2 = cenReader.readLong(tagOff + 4L + 0L);
                                                if (uncompressedSize == 4294967295L) {
                                                    uncompressedSize = uncompressedSize2;
                                                }
                                                else if (uncompressedSize != uncompressedSize2) {
                                                    throw new IOException("Mismatch in uncompressed size: " + uncompressedSize + " vs. " + uncompressedSize2 + ": " + entryNameSanitized);
                                                }
                                                final long compressedSize2 = cenReader.readLong(tagOff + 4L + 8L);
                                                if (compressedSize == 4294967295L) {
                                                    compressedSize = compressedSize2;
                                                }
                                                else if (compressedSize != compressedSize2) {
                                                    throw new IOException("Mismatch in compressed size: " + compressedSize + " vs. " + compressedSize2 + ": " + entryNameSanitized);
                                                }
                                                if (size >= 28) {
                                                    final long pos2 = cenReader.readLong(tagOff + 4L + 16L);
                                                    if (pos == 4294967295L) {
                                                        pos = pos2;
                                                    }
                                                    else if (pos != pos2) {
                                                        throw new IOException("Mismatch in entry pos: " + pos + " vs. " + pos2 + ": " + entryNameSanitized);
                                                    }
                                                    break;
                                                }
                                                break;
                                            }
                                            else {
                                                if (tag == 21589 && size >= 5) {
                                                    final byte bits = cenReader.readByte(tagOff + 4L + 0L);
                                                    if ((bits & 0x1) == 0x1 && size >= 13) {
                                                        lastModifiedMillis = cenReader.readLong(tagOff + 4L + 1L) * 1000L;
                                                    }
                                                }
                                                else if (tag == 22613 && size >= 20) {
                                                    lastModifiedMillis = cenReader.readLong(tagOff + 4L + 8L) * 1000L;
                                                }
                                                else if (tag != 30805) {
                                                    if (tag == 28789) {
                                                        final byte version = cenReader.readByte(tagOff + 4L + 0L);
                                                        if (version != 1) {
                                                            throw new IOException("Unknown Unicode entry name format " + version + " in extra field: " + entryNameSanitized);
                                                        }
                                                        if (size > 9) {
                                                            try {
                                                                entryNameSanitized = cenReader.readString(tagOff + 9L, size - 9);
                                                            }
                                                            catch (IllegalArgumentException e2) {
                                                                throw new IOException("Malformed extended Unicode entry name for entry: " + entryNameSanitized);
                                                            }
                                                        }
                                                    }
                                                }
                                                extraFieldOff += 4 + size;
                                            }
                                        }
                                    }
                                    int lastModifiedTimeMSDOS = 0;
                                    int lastModifiedDateMSDOS = 0;
                                    if (lastModifiedMillis == 0L) {
                                        lastModifiedTimeMSDOS = cenReader.readUnsignedShort(entOff2 + 12L);
                                        lastModifiedDateMSDOS = cenReader.readUnsignedShort(entOff2 + 14L);
                                    }
                                    if (compressedSize >= 0L) {
                                        if (pos >= 0L) {
                                            final long locHeaderPos = locPos + pos;
                                            if (locHeaderPos < 0L) {
                                                if (log != null) {
                                                    log.log("Skipping zip entry with invalid loc header position: " + entryNameSanitized);
                                                }
                                            }
                                            else if (locHeaderPos + 4L >= this.slice.sliceLength) {
                                                if (log != null) {
                                                    log.log("Unexpected EOF when trying to read LOC header: " + entryNameSanitized);
                                                }
                                            }
                                            else {
                                                final FastZipEntry entry = new FastZipEntry(this, locHeaderPos, entryNameSanitized, isDeflated, compressedSize, uncompressedSize, lastModifiedMillis, lastModifiedTimeMSDOS, lastModifiedDateMSDOS, fileAttributes);
                                                this.entries.add(entry);
                                                if (entry.entryName.equals("META-INF/MANIFEST.MF")) {
                                                    manifestZipEntry = entry;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    entOff2 += entSize;
                }
            }
        }
        catch (EOFException | IndexOutOfBoundsException ex2) {
            final Exception ex;
            final Exception e = ex;
            if (log != null) {
                log.log("Reached premature EOF" + (this.entries.isEmpty() ? "" : (" after reading zip entry " + this.entries.get(this.entries.size() - 1))));
            }
        }
        if (manifestZipEntry != null) {
            this.parseManifest(manifestZipEntry, log);
        }
        if (this.isMultiReleaseJar) {
            if (VersionFinder.JAVA_MAJOR_VERSION < 9) {
                if (log != null) {
                    log.log("This is a multi-release jar, but JRE version " + VersionFinder.JAVA_MAJOR_VERSION + " does not support multi-release jars");
                }
            }
            else {
                if (log != null) {
                    final Set<Integer> versionsFound = new HashSet<Integer>();
                    for (final FastZipEntry entry2 : this.entries) {
                        if (entry2.version > 8) {
                            versionsFound.add(entry2.version);
                        }
                    }
                    final List<Integer> versionsFoundSorted = new ArrayList<Integer>(versionsFound);
                    CollectionUtils.sortIfNotEmpty(versionsFoundSorted);
                    log.log("This is a multi-release jar, with versions: " + Join.join(", ", versionsFoundSorted));
                }
                CollectionUtils.sortIfNotEmpty(this.entries);
                final List<FastZipEntry> unversionedZipEntriesMasked = new ArrayList<FastZipEntry>(this.entries.size());
                final Map<String, String> unversionedPathToVersionedPath = new HashMap<String, String>();
                for (final FastZipEntry versionedZipEntry : this.entries) {
                    if (!unversionedPathToVersionedPath.containsKey(versionedZipEntry.entryNameUnversioned)) {
                        unversionedPathToVersionedPath.put(versionedZipEntry.entryNameUnversioned, versionedZipEntry.entryName);
                        unversionedZipEntriesMasked.add(versionedZipEntry);
                    }
                    else {
                        if (log == null) {
                            continue;
                        }
                        log.log(unversionedPathToVersionedPath.get(versionedZipEntry.entryNameUnversioned) + " masks " + versionedZipEntry.entryName);
                    }
                }
                this.entries = unversionedZipEntriesMasked;
            }
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
    public String toString() {
        return this.getPath();
    }
    
    static {
        IMPLEMENTATION_TITLE_KEY = manifestKeyToBytes("Implementation-Title");
        SPECIFICATION_TITLE_KEY = manifestKeyToBytes("Specification-Title");
        CLASS_PATH_KEY = manifestKeyToBytes("Class-Path");
        BUNDLE_CLASSPATH_KEY = manifestKeyToBytes("Bundle-ClassPath");
        SPRING_BOOT_CLASSES_KEY = manifestKeyToBytes("Spring-Boot-Classes");
        SPRING_BOOT_LIB_KEY = manifestKeyToBytes("Spring-Boot-Lib");
        MULTI_RELEASE_KEY = manifestKeyToBytes("Multi-Release");
        ADD_EXPORTS_KEY = manifestKeyToBytes("Add-Exports");
        ADD_OPENS_KEY = manifestKeyToBytes("Add-Opens");
        AUTOMATIC_MODULE_NAME_KEY = manifestKeyToBytes("Automatic-Module-Name");
        LogicalZipFile.toLowerCase = new byte[256];
        for (int i = 32; i < 127; ++i) {
            LogicalZipFile.toLowerCase[i] = (byte)Character.toLowerCase((char)i);
        }
    }
}
