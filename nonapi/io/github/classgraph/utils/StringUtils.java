// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.utils;

public final class StringUtils
{
    private StringUtils() {
    }
    
    public static String readString(final byte[] arr, final int startOffset, final int numBytes, final boolean replaceSlashWithDot, final boolean stripLSemicolon) throws IllegalArgumentException {
        if (startOffset < 0L || numBytes < 0 || startOffset + numBytes > arr.length) {
            throw new IllegalArgumentException("offset or numBytes out of range");
        }
        final char[] chars = new char[numBytes];
        int byteIdx = 0;
        int charIdx = 0;
        while (byteIdx < numBytes) {
            final int c = arr[startOffset + byteIdx] & 0xFF;
            if (c > 127) {
                break;
            }
            chars[charIdx++] = (char)((replaceSlashWithDot && c == 47) ? 46 : c);
            ++byteIdx;
        }
        while (byteIdx < numBytes) {
            final int c = arr[startOffset + byteIdx] & 0xFF;
            switch (c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7: {
                    ++byteIdx;
                    chars[charIdx++] = (char)((replaceSlashWithDot && c == 47) ? 46 : c);
                    continue;
                }
                case 12:
                case 13: {
                    byteIdx += 2;
                    if (byteIdx > numBytes) {
                        throw new IllegalArgumentException("Bad modified UTF8");
                    }
                    final int c2 = arr[startOffset + byteIdx - 1];
                    if ((c2 & 0xC0) != 0x80) {
                        throw new IllegalArgumentException("Bad modified UTF8");
                    }
                    final int c3 = (c & 0x1F) << 6 | (c2 & 0x3F);
                    chars[charIdx++] = (char)((replaceSlashWithDot && c3 == 47) ? 46 : c3);
                    continue;
                }
                case 14: {
                    byteIdx += 3;
                    if (byteIdx > numBytes) {
                        throw new IllegalArgumentException("Bad modified UTF8");
                    }
                    final int c2 = arr[startOffset + byteIdx - 2];
                    final int c3 = arr[startOffset + byteIdx - 1];
                    if ((c2 & 0xC0) != 0x80 || (c3 & 0xC0) != 0x80) {
                        throw new IllegalArgumentException("Bad modified UTF8");
                    }
                    final int c4 = (c & 0xF) << 12 | (c2 & 0x3F) << 6 | (c3 & 0x3F);
                    chars[charIdx++] = (char)((replaceSlashWithDot && c4 == 47) ? 46 : c4);
                    continue;
                }
                default: {
                    throw new IllegalArgumentException("Bad modified UTF8");
                }
            }
        }
        if (charIdx == numBytes && !stripLSemicolon) {
            return new String(chars);
        }
        if (!stripLSemicolon) {
            return new String(chars, 0, charIdx);
        }
        if (charIdx < 2 || chars[0] != 'L' || chars[charIdx - 1] != ';') {
            throw new IllegalArgumentException("Expected string to start with 'L' and end with ';', got \"" + new String(chars) + "\"");
        }
        return new String(chars, 1, charIdx - 2);
    }
}
