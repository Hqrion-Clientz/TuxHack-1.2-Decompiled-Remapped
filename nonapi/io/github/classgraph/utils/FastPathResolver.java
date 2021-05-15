// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.utils;

import java.io.File;
import java.util.regex.Matcher;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public final class FastPathResolver
{
    private static final Pattern percentMatcher;
    private static final Pattern schemeTwoSlashMatcher;
    private static final Pattern schemeOneSlashMatcher;
    private static final boolean WINDOWS;
    
    private FastPathResolver() {
    }
    
    private static void translateSeparator(final String path, final int startIdx, final int endIdx, final boolean stripFinalSeparator, final StringBuilder buf) {
        for (int i = startIdx; i < endIdx; ++i) {
            final char c = path.charAt(i);
            if (c == '\\' || c == '/') {
                if (i < endIdx - 1 || !stripFinalSeparator) {
                    final char prevChar = (buf.length() == 0) ? '\0' : buf.charAt(buf.length() - 1);
                    if (prevChar != '/') {
                        buf.append('/');
                    }
                }
            }
            else {
                buf.append(c);
            }
        }
    }
    
    private static int hexCharToInt(final char c) {
        return (c >= '0' && c <= '9') ? (c - '0') : ((c >= 'a' && c <= 'f') ? (c - 'a' + 10) : (c - 'A' + 10));
    }
    
    private static void unescapePercentEncoding(final String path, final int startIdx, final int endIdx, final StringBuilder buf) {
        if (endIdx - startIdx == 3 && path.charAt(startIdx + 1) == '2' && path.charAt(startIdx + 2) == '0') {
            buf.append(' ');
        }
        else {
            final byte[] bytes = new byte[(endIdx - startIdx) / 3];
            for (int i = startIdx, j = 0; i < endIdx; i += 3, ++j) {
                final char c1 = path.charAt(i + 1);
                final char c2 = path.charAt(i + 2);
                final int digit1 = hexCharToInt(c1);
                final int digit2 = hexCharToInt(c2);
                bytes[j] = (byte)(digit1 << 4 | digit2);
            }
            String str = new String(bytes, StandardCharsets.UTF_8);
            str = str.replace("/", "%2F").replace("\\", "%5C");
            buf.append(str);
        }
    }
    
    public static String normalizePath(final String path, final boolean isFileOrJarURL) {
        final boolean hasPercent = path.indexOf(37) >= 0;
        if (!hasPercent && path.indexOf(92) < 0 && !path.endsWith("/")) {
            return path;
        }
        final int len = path.length();
        final StringBuilder buf = new StringBuilder();
        if (hasPercent && isFileOrJarURL) {
            int prevEndMatchIdx = 0;
            final Matcher matcher = FastPathResolver.percentMatcher.matcher(path);
            while (matcher.find()) {
                final int startMatchIdx = matcher.start();
                final int endMatchIdx = matcher.end();
                translateSeparator(path, prevEndMatchIdx, startMatchIdx, false, buf);
                unescapePercentEncoding(path, startMatchIdx, endMatchIdx, buf);
                prevEndMatchIdx = endMatchIdx;
            }
            translateSeparator(path, prevEndMatchIdx, len, true, buf);
            return buf.toString();
        }
        translateSeparator(path, 0, len, true, buf);
        return buf.toString();
    }
    
    public static String resolve(final String resolveBasePath, final String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return (resolveBasePath == null) ? "" : resolveBasePath;
        }
        String prefix = "";
        boolean isAbsolutePath = false;
        boolean isFileOrJarURL = false;
        int startIdx = 0;
        if (relativePath.regionMatches(true, 0, "jar:", 0, 4)) {
            startIdx = 4;
            isFileOrJarURL = true;
        }
        if (relativePath.regionMatches(true, startIdx, "http://", 0, 7)) {
            startIdx += 7;
            prefix = "http://";
            isAbsolutePath = true;
        }
        else if (relativePath.regionMatches(true, startIdx, "https://", 0, 8)) {
            startIdx += 8;
            prefix = "https://";
            isAbsolutePath = true;
        }
        else if (relativePath.regionMatches(true, startIdx, "jrt:", 0, 5)) {
            startIdx += 4;
            prefix = "jrt:";
            isAbsolutePath = true;
        }
        else if (relativePath.regionMatches(true, startIdx, "file:", 0, 5)) {
            startIdx += 5;
            isFileOrJarURL = true;
        }
        else {
            final String relPath = (startIdx == 0) ? relativePath : relativePath.substring(startIdx);
            final Matcher m2 = FastPathResolver.schemeTwoSlashMatcher.matcher(relPath);
            if (m2.find()) {
                final String m2Match = m2.group();
                startIdx += m2Match.length();
                prefix = m2Match;
                isAbsolutePath = true;
            }
            else {
                final Matcher m3 = FastPathResolver.schemeOneSlashMatcher.matcher(relPath);
                if (m3.find()) {
                    final String m1Match = m3.group();
                    startIdx += m1Match.length();
                    prefix = m1Match;
                    isAbsolutePath = true;
                }
            }
        }
        if (isFileOrJarURL) {
            if (FastPathResolver.WINDOWS) {
                if (relativePath.startsWith("\\\\\\\\", startIdx) || relativePath.startsWith("////", startIdx)) {
                    startIdx += 4;
                    prefix += "//";
                    isAbsolutePath = true;
                }
                else if (relativePath.startsWith("\\\\", startIdx)) {
                    startIdx += 2;
                }
            }
            if (relativePath.startsWith("///", startIdx)) {
                startIdx += 2;
            }
        }
        if (FastPathResolver.WINDOWS) {
            if (relativePath.startsWith("//", startIdx) || relativePath.startsWith("\\\\", startIdx)) {
                startIdx += 2;
                prefix = "//";
                isAbsolutePath = true;
            }
            else if (relativePath.length() - startIdx > 2 && Character.isLetter(relativePath.charAt(startIdx)) && relativePath.charAt(startIdx + 1) == ':') {
                isAbsolutePath = true;
            }
            else if (relativePath.length() - startIdx > 3 && (relativePath.charAt(startIdx) == '/' || relativePath.charAt(startIdx) == '\\') && Character.isLetter(relativePath.charAt(startIdx + 1)) && relativePath.charAt(startIdx + 2) == ':') {
                isAbsolutePath = true;
                ++startIdx;
            }
        }
        if (relativePath.length() - startIdx > 1 && (relativePath.charAt(startIdx) == '/' || relativePath.charAt(startIdx) == '\\')) {
            isAbsolutePath = true;
        }
        String pathStr = normalizePath((startIdx == 0) ? relativePath : relativePath.substring(startIdx), isFileOrJarURL);
        if (!pathStr.equals("/")) {
            if (pathStr.endsWith("/")) {
                pathStr = pathStr.substring(0, pathStr.length() - 1);
            }
            if (pathStr.endsWith("!")) {
                pathStr = pathStr.substring(0, pathStr.length() - 1);
            }
            if (pathStr.endsWith("/")) {
                pathStr = pathStr.substring(0, pathStr.length() - 1);
            }
            if (pathStr.isEmpty()) {
                pathStr = "/";
            }
        }
        String pathResolved;
        if (isAbsolutePath || resolveBasePath == null || resolveBasePath.isEmpty()) {
            pathResolved = FileUtils.sanitizeEntryPath(pathStr, false, true);
        }
        else {
            pathResolved = FileUtils.sanitizeEntryPath(resolveBasePath + (resolveBasePath.endsWith("/") ? "" : "/") + pathStr, false, true);
        }
        return prefix.isEmpty() ? pathResolved : (prefix + pathResolved);
    }
    
    public static String resolve(final String pathStr) {
        return resolve(null, pathStr);
    }
    
    static {
        percentMatcher = Pattern.compile("([%][0-9a-fA-F][0-9a-fA-F])+");
        schemeTwoSlashMatcher = Pattern.compile("^[a-zA-Z+\\-.]+://");
        schemeOneSlashMatcher = Pattern.compile("^[a-zA-Z+\\-.]+:/");
        WINDOWS = (File.separatorChar == '\\');
    }
}
