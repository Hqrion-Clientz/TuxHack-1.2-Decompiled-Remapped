// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.utils;

import io.github.classgraph.ClassGraphException;
import java.util.regex.Matcher;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.Collection;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.File;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import java.util.regex.Pattern;

public final class JarUtils
{
    public static final Pattern URL_SCHEME_PATTERN;
    private static final Pattern DASH_VERSION;
    private static final Pattern NON_ALPHANUM;
    private static final Pattern REPEATING_DOTS;
    private static final Pattern LEADING_DOTS;
    private static final Pattern TRAILING_DOTS;
    private static final String[] UNIX_NON_PATH_SEPARATORS;
    private static final int[] UNIX_NON_PATH_SEPARATOR_COLON_POSITIONS;
    
    private JarUtils() {
    }
    
    public static String[] smartPathSplit(final String pathStr, final ScanSpec scanSpec) {
        return smartPathSplit(pathStr, File.pathSeparatorChar, scanSpec);
    }
    
    public static String[] smartPathSplit(final String pathStr, final char separatorChar, final ScanSpec scanSpec) {
        if (pathStr == null || pathStr.isEmpty()) {
            return new String[0];
        }
        if (separatorChar != ':') {
            final List<String> partsFiltered = new ArrayList<String>();
            for (final String part : pathStr.split(String.valueOf(separatorChar))) {
                final String partFiltered = part.trim();
                if (!partFiltered.isEmpty()) {
                    partsFiltered.add(partFiltered);
                }
            }
            return partsFiltered.toArray(new String[0]);
        }
        final Set<Integer> splitPoints = new HashSet<Integer>();
        int i = -1;
        while (true) {
            boolean foundNonPathSeparator = false;
            for (int j = 0; j < JarUtils.UNIX_NON_PATH_SEPARATORS.length; ++j) {
                final int startIdx = i - JarUtils.UNIX_NON_PATH_SEPARATOR_COLON_POSITIONS[j];
                if (pathStr.regionMatches(true, startIdx, JarUtils.UNIX_NON_PATH_SEPARATORS[j], 0, JarUtils.UNIX_NON_PATH_SEPARATORS[j].length()) && (startIdx == 0 || pathStr.charAt(startIdx - 1) == ':')) {
                    foundNonPathSeparator = true;
                    break;
                }
            }
            if (!foundNonPathSeparator && scanSpec != null && scanSpec.allowedURLSchemes != null && !scanSpec.allowedURLSchemes.isEmpty()) {
                for (final String scheme : scanSpec.allowedURLSchemes) {
                    if (!scheme.equals("http") && !scheme.equals("https") && !scheme.equals("jar") && !scheme.equals("file")) {
                        final int schemeLen = scheme.length();
                        final int startIdx2 = i - schemeLen;
                        if (pathStr.regionMatches(true, startIdx2, scheme, 0, schemeLen) && (startIdx2 == 0 || pathStr.charAt(startIdx2 - 1) == ':')) {
                            foundNonPathSeparator = true;
                            break;
                        }
                        continue;
                    }
                }
            }
            if (!foundNonPathSeparator) {
                splitPoints.add(i);
            }
            i = pathStr.indexOf(58, i + 1);
            if (i < 0) {
                break;
            }
        }
        splitPoints.add(pathStr.length());
        final List<Integer> splitPointsSorted = new ArrayList<Integer>(splitPoints);
        CollectionUtils.sortIfNotEmpty(splitPointsSorted);
        final List<String> parts = new ArrayList<String>();
        for (int k = 1; k < splitPointsSorted.size(); ++k) {
            final int idx0 = splitPointsSorted.get(k - 1);
            final int idx2 = splitPointsSorted.get(k);
            String part2 = pathStr.substring(idx0 + 1, idx2).trim();
            part2 = part2.replaceAll("\\\\:", ":");
            if (!part2.isEmpty()) {
                parts.add(part2);
            }
        }
        return parts.toArray(new String[0]);
    }
    
    private static void appendPathElt(final Object pathElt, final StringBuilder buf) {
        if (buf.length() > 0) {
            buf.append(File.pathSeparatorChar);
        }
        final String path = (File.separatorChar == '\\') ? pathElt.toString() : pathElt.toString().replaceAll(File.pathSeparator, "\\" + File.pathSeparator);
        buf.append(path);
    }
    
    public static String pathElementsToPathStr(final Object... pathElts) {
        final StringBuilder buf = new StringBuilder();
        for (final Object pathElt : pathElts) {
            appendPathElt(pathElt, buf);
        }
        return buf.toString();
    }
    
    public static String pathElementsToPathStr(final Iterable<?> pathElts) {
        final StringBuilder buf = new StringBuilder();
        for (final Object pathElt : pathElts) {
            appendPathElt(pathElt, buf);
        }
        return buf.toString();
    }
    
    public static String leafName(final String path) {
        final int bangIdx = path.indexOf(33);
        final int endIdx = (bangIdx >= 0) ? bangIdx : path.length();
        int leafStartIdx = 1 + ((File.separatorChar == '/') ? path.lastIndexOf(47, endIdx) : Math.max(path.lastIndexOf(47, endIdx), path.lastIndexOf(File.separatorChar, endIdx)));
        int sepIdx = path.indexOf("---");
        if (sepIdx >= 0) {
            sepIdx += "---".length();
        }
        leafStartIdx = Math.max(leafStartIdx, sepIdx);
        leafStartIdx = Math.min(leafStartIdx, endIdx);
        return path.substring(leafStartIdx, endIdx);
    }
    
    public static String classfilePathToClassName(final String classfilePath) {
        if (!classfilePath.endsWith(".class")) {
            throw new IllegalArgumentException("Classfile path does not end with \".class\": " + classfilePath);
        }
        return classfilePath.substring(0, classfilePath.length() - 6).replace('/', '.');
    }
    
    public static String classNameToClassfilePath(final String className) {
        return className.replace('.', '/') + ".class";
    }
    
    public static String derivedAutomaticModuleName(final String jarPath) {
        int endIdx = jarPath.length();
        final int lastPlingIdx = jarPath.lastIndexOf(33);
        if (lastPlingIdx > 0 && jarPath.lastIndexOf(46) <= Math.max(lastPlingIdx, jarPath.lastIndexOf(47))) {
            endIdx = lastPlingIdx;
        }
        final int secondToLastPlingIdx = (endIdx == 0) ? -1 : jarPath.lastIndexOf("!", endIdx - 1);
        final int startIdx = Math.max(secondToLastPlingIdx, jarPath.lastIndexOf(47, endIdx - 1)) + 1;
        final int lastDotBeforeLastPlingIdx = jarPath.lastIndexOf(46, endIdx - 1);
        if (lastDotBeforeLastPlingIdx > startIdx) {
            endIdx = lastDotBeforeLastPlingIdx;
        }
        String moduleName = jarPath.substring(startIdx, endIdx);
        final Matcher matcher = JarUtils.DASH_VERSION.matcher(moduleName);
        if (matcher.find()) {
            moduleName = moduleName.substring(0, matcher.start());
        }
        moduleName = JarUtils.NON_ALPHANUM.matcher(moduleName).replaceAll(".");
        moduleName = JarUtils.REPEATING_DOTS.matcher(moduleName).replaceAll(".");
        if (moduleName.length() > 0 && moduleName.charAt(0) == '.') {
            moduleName = JarUtils.LEADING_DOTS.matcher(moduleName).replaceAll("");
        }
        final int len = moduleName.length();
        if (len > 0 && moduleName.charAt(len - 1) == '.') {
            moduleName = JarUtils.TRAILING_DOTS.matcher(moduleName).replaceAll("");
        }
        return moduleName;
    }
    
    static {
        URL_SCHEME_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9+-.]+[:].*");
        DASH_VERSION = Pattern.compile("-(\\d+(\\.|$))");
        NON_ALPHANUM = Pattern.compile("[^A-Za-z0-9]");
        REPEATING_DOTS = Pattern.compile("(\\.)(\\1)+");
        LEADING_DOTS = Pattern.compile("^\\.");
        TRAILING_DOTS = Pattern.compile("\\.$");
        UNIX_NON_PATH_SEPARATORS = new String[] { "jar:", "file:", "http://", "https://", "\\:" };
        UNIX_NON_PATH_SEPARATOR_COLON_POSITIONS = new int[JarUtils.UNIX_NON_PATH_SEPARATORS.length];
        for (int i = 0; i < JarUtils.UNIX_NON_PATH_SEPARATORS.length; ++i) {
            JarUtils.UNIX_NON_PATH_SEPARATOR_COLON_POSITIONS[i] = JarUtils.UNIX_NON_PATH_SEPARATORS[i].indexOf(58);
            if (JarUtils.UNIX_NON_PATH_SEPARATOR_COLON_POSITIONS[i] < 0) {
                throw ClassGraphException.newClassGraphException("Could not find ':' in \"" + JarUtils.UNIX_NON_PATH_SEPARATORS[i] + "\"");
            }
        }
    }
}
