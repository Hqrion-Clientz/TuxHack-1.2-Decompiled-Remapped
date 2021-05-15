// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classpath;

import java.util.Objects;
import nonapi.io.github.classgraph.classloaderhandler.ClassLoaderHandlerRegistry;
import java.lang.reflect.Array;
import nonapi.io.github.classgraph.utils.JarUtils;
import nonapi.io.github.classgraph.utils.LogNode;
import nonapi.io.github.classgraph.utils.FastPathResolver;
import nonapi.io.github.classgraph.utils.FileUtils;
import java.nio.file.InvalidPathException;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.io.File;
import java.nio.file.Path;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import io.github.classgraph.ClassGraph;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nonapi.io.github.classgraph.scanspec.ScanSpec;

public class ClasspathOrder
{
    private final ScanSpec scanSpec;
    private final Set<String> classpathEntryUniqueResolvedPaths;
    private final List<ClasspathElementAndClassLoader> order;
    private static final List<String> AUTOMATIC_PACKAGE_ROOT_SUFFIXES;
    
    ClasspathOrder(final ScanSpec scanSpec) {
        this.classpathEntryUniqueResolvedPaths = new HashSet<String>();
        this.order = new ArrayList<ClasspathElementAndClassLoader>();
        this.scanSpec = scanSpec;
    }
    
    public List<ClasspathElementAndClassLoader> getOrder() {
        return this.order;
    }
    
    public Set<String> getClasspathEntryUniqueResolvedPaths() {
        return this.classpathEntryUniqueResolvedPaths;
    }
    
    private boolean filter(final String classpathElementPath) {
        if (this.scanSpec.classpathElementFilters != null) {
            for (final ClassGraph.ClasspathElementFilter filter : this.scanSpec.classpathElementFilters) {
                if (!filter.includeClasspathElement(classpathElementPath)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    boolean addSystemClasspathEntry(final String pathEntry, final ClassLoader classLoader) {
        if (this.classpathEntryUniqueResolvedPaths.add(pathEntry)) {
            this.order.add(new ClasspathElementAndClassLoader(pathEntry, classLoader));
            return true;
        }
        return false;
    }
    
    private boolean addClasspathEntry(final Object pathElement, final String pathElementStr, final ClassLoader classLoader, final ScanSpec scanSpec) {
        String pathElementStrWithoutSuffix = pathElementStr;
        boolean hasSuffix = false;
        for (final String suffix : ClasspathOrder.AUTOMATIC_PACKAGE_ROOT_SUFFIXES) {
            if (pathElementStr.endsWith(suffix)) {
                pathElementStrWithoutSuffix = pathElementStr.substring(0, pathElementStr.length() - suffix.length());
                hasSuffix = true;
                break;
            }
        }
        if (pathElement instanceof URL || pathElement instanceof URI || pathElement instanceof Path || pathElement instanceof File) {
            Object pathElementWithoutSuffix = pathElement;
            if (hasSuffix) {
                try {
                    pathElementWithoutSuffix = ((pathElement instanceof URL) ? new URL(pathElementStrWithoutSuffix) : ((pathElement instanceof URI) ? new URI(pathElementStrWithoutSuffix) : ((pathElement instanceof Path) ? Paths.get(pathElementStrWithoutSuffix, new String[0]) : pathElementStrWithoutSuffix)));
                }
                catch (MalformedURLException | URISyntaxException | InvalidPathException ex2) {
                    final Exception ex;
                    final Exception e = ex;
                    return false;
                }
            }
            if (this.classpathEntryUniqueResolvedPaths.add(pathElementStrWithoutSuffix)) {
                this.order.add(new ClasspathElementAndClassLoader(pathElementWithoutSuffix, classLoader));
                return true;
            }
        }
        else {
            final String pathElementStrResolved = FastPathResolver.resolve(FileUtils.CURR_DIR_PATH, pathElementStrWithoutSuffix);
            if (scanSpec.overrideClasspath == null && (SystemJarFinder.getJreLibOrExtJars().contains(pathElementStrResolved) || pathElementStrResolved.equals(SystemJarFinder.getJreRtJarPath()))) {
                return false;
            }
            if (this.classpathEntryUniqueResolvedPaths.add(pathElementStrResolved)) {
                this.order.add(new ClasspathElementAndClassLoader(pathElementStrResolved, classLoader));
                return true;
            }
        }
        return false;
    }
    
    public boolean addClasspathEntry(final Object pathElement, final ClassLoader classLoader, final ScanSpec scanSpec, final LogNode log) {
        if (pathElement == null) {
            return false;
        }
        final String pathElementStr = FastPathResolver.resolve(FileUtils.CURR_DIR_PATH, pathElement.toString());
        if (pathElementStr.isEmpty()) {
            return false;
        }
        if (pathElement instanceof URL || pathElement instanceof URI || pathElement instanceof File) {
            if (!this.filter(pathElementStr)) {
                if (log != null) {
                    log.log("Classpath element did not match filter criterion, skipping: " + pathElementStr);
                }
                return false;
            }
            Object classpathElementObj;
            try {
                classpathElementObj = ((pathElement instanceof File) ? pathElementStr : ((pathElement instanceof URI) ? ((URI)pathElement).toURL() : pathElement));
            }
            catch (MalformedURLException e) {
                if (log != null) {
                    log.log("Cannot convert from URI to URL: " + pathElementStr);
                }
                return false;
            }
            if (this.addClasspathEntry(classpathElementObj, pathElementStr, classLoader, scanSpec)) {
                if (log != null) {
                    log.log("Found classpath element: " + pathElementStr);
                }
                return true;
            }
            if (log != null) {
                log.log("Ignoring duplicate classpath element: " + pathElementStr);
            }
            return false;
        }
        else if (pathElementStr.endsWith("*")) {
            if (pathElementStr.length() != 1 && (pathElementStr.length() <= 2 || pathElementStr.charAt(pathElementStr.length() - 1) != '*' || (pathElementStr.charAt(pathElementStr.length() - 2) != File.separatorChar && (File.separatorChar == '/' || pathElementStr.charAt(pathElementStr.length() - 2) != '/')))) {
                if (log != null) {
                    log.log("Wildcard classpath elements can only end with a leaf of \"*\", can't have a partial name and then a wildcard: " + pathElementStr);
                }
                return false;
            }
            final String baseDirPath = (pathElementStr.length() == 1) ? "" : pathElementStr.substring(0, pathElementStr.length() - 2);
            final String baseDirPathResolved = FastPathResolver.resolve(FileUtils.CURR_DIR_PATH, baseDirPath);
            if (!this.filter(baseDirPath) || (!baseDirPathResolved.equals(baseDirPath) && !this.filter(baseDirPathResolved))) {
                if (log != null) {
                    log.log("Classpath element did not match filter criterion, skipping: " + pathElementStr);
                }
                return false;
            }
            final File baseDir = new File(baseDirPathResolved);
            if (!baseDir.exists()) {
                if (log != null) {
                    log.log("Directory does not exist for wildcard classpath element: " + pathElementStr);
                }
                return false;
            }
            if (!FileUtils.canRead(baseDir)) {
                if (log != null) {
                    log.log("Cannot read directory for wildcard classpath element: " + pathElementStr);
                }
                return false;
            }
            if (!baseDir.isDirectory()) {
                if (log != null) {
                    log.log("Wildcard is appended to something other than a directory: " + pathElementStr);
                }
                return false;
            }
            final LogNode dirLog = (log == null) ? null : log.log("Adding classpath elements from wildcarded directory: " + pathElementStr);
            final File[] baseDirFiles = baseDir.listFiles();
            if (baseDirFiles != null) {
                for (final File fileInDir : baseDirFiles) {
                    final String name = fileInDir.getName();
                    if (!name.equals(".") && !name.equals("..")) {
                        final String fileInDirPath = fileInDir.getPath();
                        final String fileInDirPathResolved = FastPathResolver.resolve(FileUtils.CURR_DIR_PATH, fileInDirPath);
                        if (this.addClasspathEntry(fileInDirPathResolved, fileInDirPathResolved, classLoader, scanSpec)) {
                            if (dirLog != null) {
                                dirLog.log("Found classpath element: " + fileInDirPath + (fileInDirPath.equals(fileInDirPathResolved) ? "" : (" -> " + fileInDirPathResolved)));
                            }
                        }
                        else if (dirLog != null) {
                            dirLog.log("Ignoring duplicate classpath element: " + fileInDirPath + (fileInDirPath.equals(fileInDirPathResolved) ? "" : (" -> " + fileInDirPathResolved)));
                        }
                    }
                }
                return true;
            }
            return false;
        }
        else {
            final String pathElementResolved = FastPathResolver.resolve(FileUtils.CURR_DIR_PATH, pathElementStr);
            if (!this.filter(pathElementStr) || (!pathElementResolved.equals(pathElementStr) && !this.filter(pathElementResolved))) {
                if (log != null) {
                    log.log("Classpath element did not match filter criterion, skipping: " + pathElementStr + (pathElementStr.equals(pathElementResolved) ? "" : (" -> " + pathElementResolved)));
                }
                return false;
            }
            if (this.addClasspathEntry(pathElementResolved, pathElementResolved, classLoader, scanSpec)) {
                if (log != null) {
                    log.log("Found classpath element: " + pathElementStr + (pathElementStr.equals(pathElementResolved) ? "" : (" -> " + pathElementResolved)));
                }
                return true;
            }
            if (log != null) {
                log.log("Ignoring duplicate classpath element: " + pathElementStr + (pathElementStr.equals(pathElementResolved) ? "" : (" -> " + pathElementResolved)));
            }
            return false;
        }
    }
    
    public boolean addClasspathEntries(final List<Object> overrideClasspath, final ClassLoader classLoader, final ScanSpec scanSpec, final LogNode log) {
        if (overrideClasspath == null || overrideClasspath.isEmpty()) {
            return false;
        }
        for (final Object pathElement : overrideClasspath) {
            this.addClasspathEntry(pathElement, classLoader, scanSpec, log);
        }
        return true;
    }
    
    public boolean addClasspathPathStr(final String pathStr, final ClassLoader classLoader, final ScanSpec scanSpec, final LogNode log) {
        if (pathStr == null || pathStr.isEmpty()) {
            return false;
        }
        final String[] parts = JarUtils.smartPathSplit(pathStr, scanSpec);
        if (parts.length == 0) {
            return false;
        }
        for (final String pathElement : parts) {
            this.addClasspathEntry(pathElement, classLoader, scanSpec, log);
        }
        return true;
    }
    
    public boolean addClasspathEntryObject(final Object pathObject, final ClassLoader classLoader, final ScanSpec scanSpec, final LogNode log) {
        boolean valid = false;
        if (pathObject != null) {
            if (pathObject instanceof URL || pathObject instanceof URI || pathObject instanceof Path || pathObject instanceof File) {
                valid |= this.addClasspathEntry(pathObject, classLoader, scanSpec, log);
            }
            else if (pathObject instanceof Iterable) {
                for (final Object elt : (Iterable)pathObject) {
                    valid |= this.addClasspathEntryObject(elt, classLoader, scanSpec, log);
                }
            }
            else {
                final Class<?> valClass = pathObject.getClass();
                if (valClass.isArray()) {
                    for (int j = 0, n = Array.getLength(pathObject); j < n; ++j) {
                        final Object elt2 = Array.get(pathObject, j);
                        valid |= this.addClasspathEntryObject(elt2, classLoader, scanSpec, log);
                    }
                }
                else {
                    valid |= this.addClasspathPathStr(pathObject.toString(), classLoader, scanSpec, log);
                }
            }
        }
        return valid;
    }
    
    static {
        AUTOMATIC_PACKAGE_ROOT_SUFFIXES = new ArrayList<String>();
        for (final String prefix : ClassLoaderHandlerRegistry.AUTOMATIC_PACKAGE_ROOT_PREFIXES) {
            ClasspathOrder.AUTOMATIC_PACKAGE_ROOT_SUFFIXES.add("!/" + prefix.substring(0, prefix.length() - 1));
        }
    }
    
    public static class ClasspathElementAndClassLoader
    {
        public final Object classpathElementRoot;
        public final String dirOrPathPackageRoot;
        public final ClassLoader classLoader;
        
        public ClasspathElementAndClassLoader(final Object classpathElementRoot, final String dirOrPathPackageRoot, final ClassLoader classLoader) {
            this.classpathElementRoot = classpathElementRoot;
            this.dirOrPathPackageRoot = dirOrPathPackageRoot;
            this.classLoader = classLoader;
        }
        
        public ClasspathElementAndClassLoader(final Object classpathElementRoot, final ClassLoader classLoader) {
            this.classpathElementRoot = classpathElementRoot;
            this.dirOrPathPackageRoot = "";
            this.classLoader = classLoader;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(this.classpathElementRoot, this.dirOrPathPackageRoot, this.classLoader);
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ClasspathElementAndClassLoader)) {
                return false;
            }
            final ClasspathElementAndClassLoader other = (ClasspathElementAndClassLoader)obj;
            return Objects.equals(this.dirOrPathPackageRoot, other.dirOrPathPackageRoot) && Objects.equals(this.classpathElementRoot, other.classpathElementRoot) && Objects.equals(this.classLoader, other.classLoader);
        }
        
        @Override
        public String toString() {
            return this.classpathElementRoot + " [" + this.classLoader + "]";
        }
    }
}
