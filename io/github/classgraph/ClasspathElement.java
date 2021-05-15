// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.net.URI;
import nonapi.io.github.classgraph.concurrency.WorkQueue;
import nonapi.io.github.classgraph.utils.FileUtils;
import java.util.Iterator;
import nonapi.io.github.classgraph.utils.JarUtils;
import java.util.Set;
import nonapi.io.github.classgraph.utils.LogNode;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.File;
import java.util.Map;
import java.util.Queue;
import java.util.List;

abstract class ClasspathElement
{
    int classpathElementIdx;
    List<String> nestedClasspathRootPrefixes;
    boolean skipClasspathElement;
    boolean containsSpecificallyAcceptedClasspathElementResourcePath;
    final Queue<Map.Entry<Integer, ClasspathElement>> childClasspathElementsIndexed;
    List<ClasspathElement> childClasspathElementsOrdered;
    protected final List<Resource> acceptedResources;
    protected List<Resource> acceptedClassfileResources;
    protected final Map<File, Long> fileToLastModified;
    protected final AtomicBoolean scanned;
    protected ClassLoader classLoader;
    String moduleNameFromModuleDescriptor;
    final ScanSpec scanSpec;
    
    ClasspathElement(final ClassLoader classLoader, final ScanSpec scanSpec) {
        this.childClasspathElementsIndexed = new ConcurrentLinkedQueue<Map.Entry<Integer, ClasspathElement>>();
        this.acceptedResources = new ArrayList<Resource>();
        this.acceptedClassfileResources = new ArrayList<Resource>();
        this.fileToLastModified = new ConcurrentHashMap<File, Long>();
        this.scanned = new AtomicBoolean(false);
        this.classLoader = classLoader;
        this.scanSpec = scanSpec;
    }
    
    ClassLoader getClassLoader() {
        return this.classLoader;
    }
    
    int getNumClassfileMatches() {
        return (this.acceptedClassfileResources == null) ? 0 : this.acceptedClassfileResources.size();
    }
    
    protected void checkResourcePathAcceptReject(final String relativePath, final LogNode log) {
        if (!this.scanSpec.classpathElementResourcePathAcceptReject.acceptAndRejectAreEmpty()) {
            if (this.scanSpec.classpathElementResourcePathAcceptReject.isRejected(relativePath)) {
                if (log != null) {
                    log.log("Reached rejected classpath element resource path, stopping scanning: " + relativePath);
                }
                this.skipClasspathElement = true;
                return;
            }
            if (this.scanSpec.classpathElementResourcePathAcceptReject.isSpecificallyAccepted(relativePath)) {
                if (log != null) {
                    log.log("Reached specifically accepted classpath element resource path: " + relativePath);
                }
                this.containsSpecificallyAcceptedClasspathElementResourcePath = true;
            }
        }
    }
    
    void maskClassfiles(final int classpathIdx, final Set<String> classpathRelativePathsFound, final LogNode log) {
        final List<Resource> acceptedClassfileResourcesFiltered = new ArrayList<Resource>(this.acceptedClassfileResources.size());
        boolean foundMasked = false;
        for (final Resource res : this.acceptedClassfileResources) {
            final String pathRelativeToPackageRoot = res.getPath();
            if (!pathRelativeToPackageRoot.equals("module-info.class") && !pathRelativeToPackageRoot.equals("package-info.class") && !pathRelativeToPackageRoot.endsWith("/package-info.class") && !classpathRelativePathsFound.add(pathRelativeToPackageRoot)) {
                foundMasked = true;
                if (log == null) {
                    continue;
                }
                log.log(String.format("%06d-1", classpathIdx), "Ignoring duplicate (masked) class " + JarUtils.classfilePathToClassName(pathRelativeToPackageRoot) + " found at " + res);
            }
            else {
                acceptedClassfileResourcesFiltered.add(res);
            }
        }
        if (foundMasked) {
            this.acceptedClassfileResources = acceptedClassfileResourcesFiltered;
        }
    }
    
    protected void addAcceptedResource(final Resource resource, final ScanSpec.ScanSpecPathMatch parentMatchStatus, final boolean isClassfileOnly, final LogNode log) {
        final String path = resource.getPath();
        final boolean isClassFile = FileUtils.isClassfile(path);
        boolean isAccepted = false;
        if (isClassFile) {
            if (this.scanSpec.enableClassInfo && !this.scanSpec.classfilePathAcceptReject.isRejected(path)) {
                this.acceptedClassfileResources.add(resource);
                isAccepted = true;
            }
        }
        else {
            isAccepted = true;
        }
        if (!isClassfileOnly) {
            this.acceptedResources.add(resource);
        }
        if (log != null && isAccepted) {
            final String type = isClassFile ? "classfile" : "resource";
            String logStr = null;
            switch (parentMatchStatus) {
                case HAS_ACCEPTED_PATH_PREFIX: {
                    logStr = "Found " + type + " within subpackage of accepted package: ";
                    break;
                }
                case AT_ACCEPTED_PATH: {
                    logStr = "Found " + type + " within accepted package: ";
                    break;
                }
                case AT_ACCEPTED_CLASS_PACKAGE: {
                    logStr = "Found specifically-accepted " + type + ": ";
                    break;
                }
                default: {
                    logStr = "Found accepted " + type + ": ";
                    break;
                }
            }
            resource.scanLog = log.log("0:" + path, logStr + path + (path.equals(resource.getPathRelativeToClasspathElement()) ? "" : (" ; full path: " + resource.getPathRelativeToClasspathElement())));
        }
    }
    
    protected void finishScanPaths(final LogNode log) {
        if (log != null) {
            if (this.acceptedResources.isEmpty() && this.acceptedClassfileResources.isEmpty()) {
                log.log(this.scanSpec.enableClassInfo ? "No accepted classfiles or resources found" : "Classfile scanning is disabled, and no accepted resources found");
            }
            else if (this.acceptedResources.isEmpty()) {
                log.log("No accepted resources found");
            }
            else if (this.acceptedClassfileResources.isEmpty()) {
                log.log(this.scanSpec.enableClassInfo ? "No accepted classfiles found" : "Classfile scanning is disabled");
            }
        }
        if (log != null) {
            log.addElapsedTime();
        }
    }
    
    protected LogNode log(final int classpathElementIdx, final String msg, final LogNode log) {
        return log.log(String.format("%07d", classpathElementIdx), msg);
    }
    
    abstract void open(final WorkQueue<Scanner.ClasspathEntryWorkUnit> p0, final LogNode p1) throws InterruptedException;
    
    abstract void scanPaths(final LogNode p0);
    
    abstract Resource getResource(final String p0);
    
    abstract URI getURI();
    
    abstract File getFile();
    
    abstract String getModuleName();
}
