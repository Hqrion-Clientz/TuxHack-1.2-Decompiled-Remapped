// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.scanspec;

import nonapi.io.github.classgraph.utils.LogNode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.lang.reflect.Field;
import io.github.classgraph.ClassGraphException;
import io.github.classgraph.ModulePathInfo;
import io.github.classgraph.ClassGraph;
import java.util.List;
import java.util.Set;

public class ScanSpec
{
    public AcceptReject.AcceptRejectWholeString packageAcceptReject;
    public AcceptReject.AcceptRejectPrefix packagePrefixAcceptReject;
    public AcceptReject.AcceptRejectWholeString pathAcceptReject;
    public AcceptReject.AcceptRejectPrefix pathPrefixAcceptReject;
    public AcceptReject.AcceptRejectWholeString classAcceptReject;
    public AcceptReject.AcceptRejectWholeString classfilePathAcceptReject;
    public AcceptReject.AcceptRejectWholeString classPackageAcceptReject;
    public AcceptReject.AcceptRejectWholeString classPackagePathAcceptReject;
    public AcceptReject.AcceptRejectWholeString moduleAcceptReject;
    public AcceptReject.AcceptRejectLeafname jarAcceptReject;
    public AcceptReject.AcceptRejectWholeString classpathElementResourcePathAcceptReject;
    public AcceptReject.AcceptRejectLeafname libOrExtJarAcceptReject;
    public boolean scanJars;
    public boolean scanNestedJars;
    public boolean scanDirs;
    public boolean scanModules;
    public boolean enableClassInfo;
    public boolean enableFieldInfo;
    public boolean enableMethodInfo;
    public boolean enableAnnotationInfo;
    public boolean enableStaticFinalFieldConstantInitializerValues;
    public boolean enableInterClassDependencies;
    public boolean enableExternalClasses;
    public boolean enableSystemJarsAndModules;
    public boolean ignoreClassVisibility;
    public boolean ignoreFieldVisibility;
    public boolean ignoreMethodVisibility;
    public boolean disableRuntimeInvisibleAnnotations;
    public boolean extendScanningUpwardsToExternalClasses;
    public Set<String> allowedURLSchemes;
    public transient List<ClassLoader> addedClassLoaders;
    public transient List<ClassLoader> overrideClassLoaders;
    public transient List<Object> addedModuleLayers;
    public transient List<Object> overrideModuleLayers;
    public List<Object> overrideClasspath;
    public transient List<ClassGraph.ClasspathElementFilter> classpathElementFilters;
    public boolean initializeLoadedClasses;
    public boolean removeTemporaryFilesAfterScan;
    public boolean ignoreParentClassLoaders;
    public boolean ignoreParentModuleLayers;
    public ModulePathInfo modulePathInfo;
    public int maxBufferedJarRAMSize;
    public boolean enableMemoryMapping;
    
    public ScanSpec() {
        this.packageAcceptReject = new AcceptReject.AcceptRejectWholeString('.');
        this.packagePrefixAcceptReject = new AcceptReject.AcceptRejectPrefix('.');
        this.pathAcceptReject = new AcceptReject.AcceptRejectWholeString('/');
        this.pathPrefixAcceptReject = new AcceptReject.AcceptRejectPrefix('/');
        this.classAcceptReject = new AcceptReject.AcceptRejectWholeString('.');
        this.classfilePathAcceptReject = new AcceptReject.AcceptRejectWholeString('/');
        this.classPackageAcceptReject = new AcceptReject.AcceptRejectWholeString('.');
        this.classPackagePathAcceptReject = new AcceptReject.AcceptRejectWholeString('/');
        this.moduleAcceptReject = new AcceptReject.AcceptRejectWholeString('.');
        this.jarAcceptReject = new AcceptReject.AcceptRejectLeafname('/');
        this.classpathElementResourcePathAcceptReject = new AcceptReject.AcceptRejectWholeString('/');
        this.libOrExtJarAcceptReject = new AcceptReject.AcceptRejectLeafname('/');
        this.scanJars = true;
        this.scanNestedJars = true;
        this.scanDirs = true;
        this.scanModules = true;
        this.extendScanningUpwardsToExternalClasses = true;
        this.modulePathInfo = new ModulePathInfo();
        this.maxBufferedJarRAMSize = 67108864;
    }
    
    public void sortPrefixes() {
        for (final Field field : ScanSpec.class.getDeclaredFields()) {
            if (AcceptReject.class.isAssignableFrom(field.getType())) {
                try {
                    ((AcceptReject)field.get(this)).sortPrefixes();
                }
                catch (ReflectiveOperationException e) {
                    throw ClassGraphException.newClassGraphException("Field is not accessible: " + field, e);
                }
            }
        }
    }
    
    public void addClasspathOverride(final Object overrideClasspathElement) {
        if (this.overrideClasspath == null) {
            this.overrideClasspath = new ArrayList<Object>();
        }
        this.overrideClasspath.add((overrideClasspathElement instanceof String || overrideClasspathElement instanceof URL || overrideClasspathElement instanceof URI) ? overrideClasspathElement : overrideClasspathElement.toString());
    }
    
    public void filterClasspathElements(final ClassGraph.ClasspathElementFilter classpathElementFilter) {
        if (this.classpathElementFilters == null) {
            this.classpathElementFilters = new ArrayList<ClassGraph.ClasspathElementFilter>(2);
        }
        this.classpathElementFilters.add(classpathElementFilter);
    }
    
    public void addClassLoader(final ClassLoader classLoader) {
        if (this.addedClassLoaders == null) {
            this.addedClassLoaders = new ArrayList<ClassLoader>();
        }
        if (classLoader != null) {
            this.addedClassLoaders.add(classLoader);
        }
    }
    
    public void enableURLScheme(final String scheme) {
        if (scheme == null || scheme.length() < 2) {
            throw new IllegalArgumentException("URL schemes must contain at least two characters");
        }
        if (this.allowedURLSchemes == null) {
            this.allowedURLSchemes = new HashSet<String>();
        }
        this.allowedURLSchemes.add(scheme.toLowerCase());
    }
    
    public void overrideClassLoaders(final ClassLoader... overrideClassLoaders) {
        if (overrideClassLoaders.length == 0) {
            throw new IllegalArgumentException("At least one override ClassLoader must be provided");
        }
        this.addedClassLoaders = null;
        this.overrideClassLoaders = new ArrayList<ClassLoader>();
        for (final ClassLoader classLoader : overrideClassLoaders) {
            if (classLoader != null) {
                this.overrideClassLoaders.add(classLoader);
            }
        }
    }
    
    private static boolean isModuleLayer(final Object moduleLayer) {
        if (moduleLayer == null) {
            throw new IllegalArgumentException("ModuleLayer references must not be null");
        }
        for (Class<?> currClass = moduleLayer.getClass(); currClass != null; currClass = currClass.getSuperclass()) {
            if (currClass.getName().equals("java.lang.ModuleLayer")) {
                return true;
            }
        }
        return false;
    }
    
    public void addModuleLayer(final Object moduleLayer) {
        if (!isModuleLayer(moduleLayer)) {
            throw new IllegalArgumentException("moduleLayer must be of type java.lang.ModuleLayer");
        }
        if (this.addedModuleLayers == null) {
            this.addedModuleLayers = new ArrayList<Object>();
        }
        this.addedModuleLayers.add(moduleLayer);
    }
    
    public void overrideModuleLayers(final Object... overrideModuleLayers) {
        if (overrideModuleLayers == null) {
            throw new IllegalArgumentException("overrideModuleLayers cannot be null");
        }
        if (overrideModuleLayers.length == 0) {
            throw new IllegalArgumentException("At least one override ModuleLayer must be provided");
        }
        for (final Object moduleLayer : overrideModuleLayers) {
            if (!isModuleLayer(moduleLayer)) {
                throw new IllegalArgumentException("moduleLayer must be of type java.lang.ModuleLayer");
            }
        }
        this.addedModuleLayers = null;
        Collections.addAll(this.overrideModuleLayers = new ArrayList<Object>(), overrideModuleLayers);
    }
    
    public ScanSpecPathMatch dirAcceptMatchStatus(final String relativePath) {
        if (this.pathAcceptReject.isRejected(relativePath)) {
            return ScanSpecPathMatch.HAS_REJECTED_PATH_PREFIX;
        }
        if (this.pathPrefixAcceptReject.isRejected(relativePath)) {
            return ScanSpecPathMatch.HAS_REJECTED_PATH_PREFIX;
        }
        if (this.pathAcceptReject.acceptIsEmpty() && this.classPackagePathAcceptReject.acceptIsEmpty()) {
            return (relativePath.isEmpty() || relativePath.equals("/")) ? ScanSpecPathMatch.AT_ACCEPTED_PATH : ScanSpecPathMatch.HAS_ACCEPTED_PATH_PREFIX;
        }
        if (this.pathAcceptReject.isSpecificallyAcceptedAndNotRejected(relativePath)) {
            return ScanSpecPathMatch.AT_ACCEPTED_PATH;
        }
        if (this.classPackagePathAcceptReject.isSpecificallyAcceptedAndNotRejected(relativePath)) {
            return ScanSpecPathMatch.AT_ACCEPTED_CLASS_PACKAGE;
        }
        if (this.pathPrefixAcceptReject.isSpecificallyAccepted(relativePath)) {
            return ScanSpecPathMatch.HAS_ACCEPTED_PATH_PREFIX;
        }
        if (relativePath.equals("/")) {
            return ScanSpecPathMatch.ANCESTOR_OF_ACCEPTED_PATH;
        }
        if (this.pathAcceptReject.acceptHasPrefix(relativePath)) {
            return ScanSpecPathMatch.ANCESTOR_OF_ACCEPTED_PATH;
        }
        if (this.classfilePathAcceptReject.acceptHasPrefix(relativePath)) {
            return ScanSpecPathMatch.ANCESTOR_OF_ACCEPTED_PATH;
        }
        return ScanSpecPathMatch.NOT_WITHIN_ACCEPTED_PATH;
    }
    
    public boolean classfileIsSpecificallyAccepted(final String relativePath) {
        return this.classfilePathAcceptReject.isSpecificallyAcceptedAndNotRejected(relativePath);
    }
    
    public boolean classOrPackageIsRejected(final String className) {
        return this.classAcceptReject.isRejected(className) || this.packagePrefixAcceptReject.isRejected(className);
    }
    
    public void log(final LogNode log) {
        if (log != null) {
            final LogNode scanSpecLog = log.log("ScanSpec:");
            for (final Field field : ScanSpec.class.getDeclaredFields()) {
                try {
                    scanSpecLog.log(field.getName() + ": " + field.get(this));
                }
                catch (ReflectiveOperationException ex) {}
            }
        }
    }
    
    public enum ScanSpecPathMatch
    {
        HAS_REJECTED_PATH_PREFIX, 
        HAS_ACCEPTED_PATH_PREFIX, 
        AT_ACCEPTED_PATH, 
        ANCESTOR_OF_ACCEPTED_PATH, 
        AT_ACCEPTED_CLASS_PACKAGE, 
        NOT_WITHIN_ACCEPTED_PATH;
    }
}
