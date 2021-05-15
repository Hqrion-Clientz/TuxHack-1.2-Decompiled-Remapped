// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classpath;

import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.utils.JarUtils;
import nonapi.io.github.classgraph.utils.VersionFinder;
import java.util.LinkedHashSet;
import java.io.IOException;
import nonapi.io.github.classgraph.utils.FastPathResolver;
import nonapi.io.github.classgraph.utils.FileUtils;
import java.io.File;
import java.util.Set;

public final class SystemJarFinder
{
    private static final Set<String> RT_JARS;
    private static final String RT_JAR;
    private static final Set<String> JRE_LIB_OR_EXT_JARS;
    
    private SystemJarFinder() {
    }
    
    private static boolean addJREPath(final File dir) {
        if (dir != null && !dir.getPath().isEmpty() && FileUtils.canReadAndIsDir(dir)) {
            final File[] dirFiles = dir.listFiles();
            if (dirFiles != null) {
                for (final File file : dirFiles) {
                    final String filePath = file.getPath();
                    if (filePath.endsWith(".jar")) {
                        final String jarPathResolved = FastPathResolver.resolve(FileUtils.CURR_DIR_PATH, filePath);
                        if (jarPathResolved.endsWith("/rt.jar")) {
                            SystemJarFinder.RT_JARS.add(jarPathResolved);
                        }
                        else {
                            SystemJarFinder.JRE_LIB_OR_EXT_JARS.add(jarPathResolved);
                        }
                        try {
                            final File canonicalFile = file.getCanonicalFile();
                            final String canonicalFilePath = canonicalFile.getPath();
                            if (!canonicalFilePath.equals(filePath)) {
                                final String canonicalJarPathResolved = FastPathResolver.resolve(FileUtils.CURR_DIR_PATH, filePath);
                                SystemJarFinder.JRE_LIB_OR_EXT_JARS.add(canonicalJarPathResolved);
                            }
                        }
                        catch (IOException ex) {}
                        catch (SecurityException ex2) {}
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    public static String getJreRtJarPath() {
        return SystemJarFinder.RT_JAR;
    }
    
    public static Set<String> getJreLibOrExtJars() {
        return SystemJarFinder.JRE_LIB_OR_EXT_JARS;
    }
    
    static {
        RT_JARS = new LinkedHashSet<String>();
        JRE_LIB_OR_EXT_JARS = new LinkedHashSet<String>();
        String javaHome = VersionFinder.getProperty("java.home");
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getenv("JAVA_HOME");
        }
        if (javaHome != null && !javaHome.isEmpty()) {
            final File javaHomeFile = new File(javaHome);
            addJREPath(javaHomeFile);
            if (javaHomeFile.getName().equals("jre")) {
                final File jreParent = javaHomeFile.getParentFile();
                addJREPath(jreParent);
                addJREPath(new File(jreParent, "lib"));
                addJREPath(new File(jreParent, "lib/ext"));
            }
            else {
                addJREPath(new File(javaHomeFile, "jre"));
            }
            addJREPath(new File(javaHomeFile, "lib"));
            addJREPath(new File(javaHomeFile, "lib/ext"));
            addJREPath(new File(javaHomeFile, "jre/lib"));
            addJREPath(new File(javaHomeFile, "jre/lib/ext"));
            addJREPath(new File(javaHomeFile, "packages"));
            addJREPath(new File(javaHomeFile, "packages/lib"));
            addJREPath(new File(javaHomeFile, "packages/lib/ext"));
        }
        final String javaExtDirs = VersionFinder.getProperty("java.ext.dirs");
        if (javaExtDirs != null && !javaExtDirs.isEmpty()) {
            for (final String javaExtDir : JarUtils.smartPathSplit(javaExtDirs, null)) {
                if (!javaExtDir.isEmpty()) {
                    addJREPath(new File(javaExtDir));
                }
            }
        }
        switch (VersionFinder.OS) {
            case Linux:
            case Unix:
            case BSD:
            case Unknown: {
                addJREPath(new File("/usr/java/packages"));
                addJREPath(new File("/usr/java/packages/lib"));
                addJREPath(new File("/usr/java/packages/lib/ext"));
                break;
            }
            case MacOSX: {
                addJREPath(new File("/System/Library/Java"));
                addJREPath(new File("/System/Library/Java/Libraries"));
                addJREPath(new File("/System/Library/Java/Extensions"));
                break;
            }
            case Windows: {
                final String systemRoot = (File.separatorChar == '\\') ? System.getenv("SystemRoot") : null;
                if (systemRoot != null) {
                    addJREPath(new File(systemRoot, "Sun\\Java"));
                    addJREPath(new File(systemRoot, "Sun\\Java\\lib"));
                    addJREPath(new File(systemRoot, "Sun\\Java\\lib\\ext"));
                    addJREPath(new File(systemRoot, "Oracle\\Java"));
                    addJREPath(new File(systemRoot, "Oracle\\Java\\lib"));
                    addJREPath(new File(systemRoot, "Oracle\\Java\\lib\\ext"));
                    break;
                }
                break;
            }
            case Solaris: {
                addJREPath(new File("/usr/jdk/packages"));
                addJREPath(new File("/usr/jdk/packages/lib"));
                addJREPath(new File("/usr/jdk/packages/lib/ext"));
                break;
            }
        }
        RT_JAR = (SystemJarFinder.RT_JARS.isEmpty() ? null : FastPathResolver.resolve(SystemJarFinder.RT_JARS.iterator().next()));
    }
}
