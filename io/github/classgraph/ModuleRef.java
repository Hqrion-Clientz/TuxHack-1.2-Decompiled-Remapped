// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.io.IOException;
import nonapi.io.github.classgraph.utils.CollectionUtils;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Set;
import nonapi.io.github.classgraph.utils.ReflectionUtils;
import java.io.File;
import java.net.URI;
import java.util.List;

public class ModuleRef implements Comparable<ModuleRef>
{
    private final String name;
    private final Object reference;
    private final Object layer;
    private final Object descriptor;
    private final List<String> packages;
    private final URI location;
    private String locationStr;
    private File locationFile;
    private String rawVersion;
    private final ClassLoader classLoader;
    
    public ModuleRef(final Object moduleReference, final Object moduleLayer) {
        if (moduleReference == null) {
            throw new IllegalArgumentException("moduleReference cannot be null");
        }
        if (moduleLayer == null) {
            throw new IllegalArgumentException("moduleLayer cannot be null");
        }
        this.reference = moduleReference;
        this.layer = moduleLayer;
        this.descriptor = ReflectionUtils.invokeMethod(moduleReference, "descriptor", true);
        if (this.descriptor == null) {
            throw new IllegalArgumentException("moduleReference.descriptor() should not return null");
        }
        final String moduleName = (String)ReflectionUtils.invokeMethod(this.descriptor, "name", true);
        this.name = moduleName;
        final Set<String> modulePackages = (Set<String>)ReflectionUtils.invokeMethod(this.descriptor, "packages", true);
        if (modulePackages == null) {
            throw new IllegalArgumentException("moduleReference.descriptor().packages() should not return null");
        }
        CollectionUtils.sortIfNotEmpty(this.packages = new ArrayList<String>(modulePackages));
        final Object optionalRawVersion = ReflectionUtils.invokeMethod(this.descriptor, "rawVersion", true);
        if (optionalRawVersion != null) {
            final Boolean isPresent = (Boolean)ReflectionUtils.invokeMethod(optionalRawVersion, "isPresent", true);
            if (isPresent != null && isPresent) {
                this.rawVersion = (String)ReflectionUtils.invokeMethod(optionalRawVersion, "get", true);
            }
        }
        final Object moduleLocationOptional = ReflectionUtils.invokeMethod(moduleReference, "location", true);
        if (moduleLocationOptional == null) {
            throw new IllegalArgumentException("moduleReference.location() should not return null");
        }
        final Object moduleLocationIsPresent = ReflectionUtils.invokeMethod(moduleLocationOptional, "isPresent", true);
        if (moduleLocationIsPresent == null) {
            throw new IllegalArgumentException("moduleReference.location().isPresent() should not return null");
        }
        if (moduleLocationIsPresent) {
            this.location = (URI)ReflectionUtils.invokeMethod(moduleLocationOptional, "get", true);
            if (this.location == null) {
                throw new IllegalArgumentException("moduleReference.location().get() should not return null");
            }
        }
        else {
            this.location = null;
        }
        this.classLoader = (ClassLoader)ReflectionUtils.invokeMethod(moduleLayer, "findLoader", String.class, this.name, true);
    }
    
    public String getName() {
        return this.name;
    }
    
    public Object getReference() {
        return this.reference;
    }
    
    public Object getLayer() {
        return this.layer;
    }
    
    public Object getDescriptor() {
        return this.descriptor;
    }
    
    public List<String> getPackages() {
        return this.packages;
    }
    
    public URI getLocation() {
        return this.location;
    }
    
    public String getLocationStr() {
        if (this.locationStr == null && this.location != null) {
            this.locationStr = this.location.toString();
        }
        return this.locationStr;
    }
    
    public File getLocationFile() {
        if (this.locationFile == null && this.location != null && "file".equals(this.location.getScheme())) {
            this.locationFile = new File(this.location);
        }
        return this.locationFile;
    }
    
    public String getRawVersion() {
        return this.rawVersion;
    }
    
    public boolean isSystemModule() {
        return this.name != null && !this.name.isEmpty() && (this.name.startsWith("java.") || this.name.startsWith("jdk.") || this.name.startsWith("javafx.") || this.name.startsWith("oracle."));
    }
    
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ModuleRef)) {
            return false;
        }
        final ModuleRef modRef = (ModuleRef)obj;
        return modRef.reference.equals(this.reference) && modRef.layer.equals(this.layer);
    }
    
    @Override
    public int hashCode() {
        return this.reference.hashCode() * this.layer.hashCode();
    }
    
    @Override
    public String toString() {
        return this.reference.toString();
    }
    
    @Override
    public int compareTo(final ModuleRef o) {
        final int diff = this.name.compareTo(o.name);
        return (diff != 0) ? diff : (this.hashCode() - o.hashCode());
    }
    
    public ModuleReaderProxy open() throws IOException {
        return new ModuleReaderProxy(this);
    }
}
