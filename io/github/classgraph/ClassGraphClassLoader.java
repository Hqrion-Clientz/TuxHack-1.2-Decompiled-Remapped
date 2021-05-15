// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.io.IOException;
import java.security.ProtectionDomain;
import nonapi.io.github.classgraph.utils.JarUtils;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import java.util.Collection;
import java.net.URLClassLoader;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

class ClassGraphClassLoader extends ClassLoader
{
    private final ScanResult scanResult;
    private final boolean initializeLoadedClasses;
    private final Set<ClassLoader> environmentClassLoaderDelegationOrder;
    private final Set<ClassLoader> overriddenOrAddedClassLoaderDelegationOrder;
    
    ClassGraphClassLoader(final ScanResult scanResult) {
        super(null);
        registerAsParallelCapable();
        this.scanResult = scanResult;
        final ScanSpec scanSpec = scanResult.scanSpec;
        this.initializeLoadedClasses = scanSpec.initializeLoadedClasses;
        final boolean classpathOverridden = scanSpec.overrideClasspath != null && !scanSpec.overrideClasspath.isEmpty();
        final boolean classloadersOverridden = scanSpec.overrideClassLoaders != null && !scanSpec.overrideClassLoaders.isEmpty();
        final boolean clasloadersAdded = scanSpec.addedClassLoaders != null && !scanSpec.addedClassLoaders.isEmpty();
        this.environmentClassLoaderDelegationOrder = new LinkedHashSet<ClassLoader>();
        if (!classpathOverridden && !classloadersOverridden) {
            this.environmentClassLoaderDelegationOrder.add(null);
            final ClassLoader[] envClassLoaderOrder = scanResult.getClassLoaderOrderRespectingParentDelegation();
            if (envClassLoaderOrder != null) {
                for (final ClassLoader envClassLoader : envClassLoaderOrder) {
                    this.environmentClassLoaderDelegationOrder.add(envClassLoader);
                }
            }
        }
        final URLClassLoader classpathClassLoader = new URLClassLoader(scanResult.getClasspathURLs().toArray(new URL[0]));
        if (classpathOverridden) {
            this.environmentClassLoaderDelegationOrder.add(classpathClassLoader);
        }
        this.overriddenOrAddedClassLoaderDelegationOrder = new LinkedHashSet<ClassLoader>();
        if (classloadersOverridden) {
            this.overriddenOrAddedClassLoaderDelegationOrder.addAll(scanSpec.overrideClassLoaders);
        }
        if (clasloadersAdded) {
            this.overriddenOrAddedClassLoaderDelegationOrder.addAll(scanSpec.addedClassLoaders);
        }
        if (!classpathOverridden) {
            this.overriddenOrAddedClassLoaderDelegationOrder.add(classpathClassLoader);
        }
        this.overriddenOrAddedClassLoaderDelegationOrder.removeAll(this.environmentClassLoaderDelegationOrder);
    }
    
    @Override
    protected Class<?> findClass(final String className) throws ClassNotFoundException, LinkageError, SecurityException {
        final Class<?> loadedClass = this.findLoadedClass(className);
        if (loadedClass != null) {
            return loadedClass;
        }
        if (!this.environmentClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader envClassLoader : this.environmentClassLoaderDelegationOrder) {
                try {
                    return Class.forName(className, this.initializeLoadedClasses, envClassLoader);
                }
                catch (ClassNotFoundException | LinkageError ex) {
                    continue;
                }
                break;
            }
        }
        ClassLoader classInfoClassLoader = null;
        final ClassInfo classInfo = (this.scanResult.classNameToClassInfo == null) ? null : this.scanResult.classNameToClassInfo.get(className);
        if (classInfo != null) {
            classInfoClassLoader = classInfo.classLoader;
            if (classInfoClassLoader != null && !this.environmentClassLoaderDelegationOrder.contains(classInfoClassLoader)) {
                try {
                    return Class.forName(className, this.initializeLoadedClasses, classInfoClassLoader);
                }
                catch (ClassNotFoundException ex2) {}
                catch (LinkageError linkageError) {}
            }
            if (classInfo.classpathElement instanceof ClasspathElementModule && !classInfo.isPublic()) {
                throw new ClassNotFoundException("Classfile for class " + className + " was found in a module, but the context and system classloaders could not load the class, probably because the class is not public.");
            }
        }
        if (!this.overriddenOrAddedClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader additionalClassLoader : this.overriddenOrAddedClassLoaderDelegationOrder) {
                if (additionalClassLoader != classInfoClassLoader) {
                    try {
                        return Class.forName(className, this.initializeLoadedClasses, additionalClassLoader);
                    }
                    catch (ClassNotFoundException ex3) {}
                    catch (LinkageError linkageError2) {}
                }
            }
        }
        final ResourceList classfileResources = this.scanResult.getResourcesWithPath(JarUtils.classNameToClassfilePath(className));
        if (classfileResources != null) {
            final Iterator iterator3 = classfileResources.iterator();
            if (iterator3.hasNext()) {
                final Resource resource = iterator3.next();
                try {
                    try {
                        final ByteBuffer resourceByteBuffer = resource.read();
                        return this.defineClass(className, resourceByteBuffer, null);
                    }
                    finally {
                        resource.close();
                    }
                }
                catch (IOException e) {
                    throw new ClassNotFoundException("Could not load classfile for class " + className + " : " + e);
                }
                finally {
                    resource.close();
                }
            }
        }
        throw new ClassNotFoundException("Could not load classfile for class " + className);
    }
    
    @Override
    public URL getResource(final String path) {
        if (!this.environmentClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader envClassLoader : this.environmentClassLoaderDelegationOrder) {
                final URL resource = envClassLoader.getResource(path);
                if (resource != null) {
                    return resource;
                }
            }
        }
        if (!this.overriddenOrAddedClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader additionalClassLoader : this.overriddenOrAddedClassLoaderDelegationOrder) {
                final URL resource = additionalClassLoader.getResource(path);
                if (resource != null) {
                    return resource;
                }
            }
        }
        final ResourceList resourceList = this.scanResult.getResourcesWithPath(path);
        if (resourceList == null || resourceList.isEmpty()) {
            return super.getResource(path);
        }
        return resourceList.get(0).getURL();
    }
    
    @Override
    public Enumeration<URL> getResources(final String path) throws IOException {
        if (!this.environmentClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader envClassLoader : this.environmentClassLoaderDelegationOrder) {
                final Enumeration<URL> resources = envClassLoader.getResources(path);
                if (resources != null && resources.hasMoreElements()) {
                    return resources;
                }
            }
        }
        if (!this.overriddenOrAddedClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader additionalClassLoader : this.overriddenOrAddedClassLoaderDelegationOrder) {
                final Enumeration<URL> resources = additionalClassLoader.getResources(path);
                if (resources != null && resources.hasMoreElements()) {
                    return resources;
                }
            }
        }
        final ResourceList resourceList = this.scanResult.getResourcesWithPath(path);
        if (resourceList == null || resourceList.isEmpty()) {
            return Collections.emptyEnumeration();
        }
        return new Enumeration<URL>() {
            int idx;
            
            @Override
            public boolean hasMoreElements() {
                return this.idx < resourceList.size();
            }
            
            @Override
            public URL nextElement() {
                return resourceList.get(this.idx++).getURL();
            }
        };
    }
    
    @Override
    public InputStream getResourceAsStream(final String path) {
        if (!this.environmentClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader envClassLoader : this.environmentClassLoaderDelegationOrder) {
                final InputStream inputStream = envClassLoader.getResourceAsStream(path);
                if (inputStream != null) {
                    return inputStream;
                }
            }
        }
        if (!this.overriddenOrAddedClassLoaderDelegationOrder.isEmpty()) {
            for (final ClassLoader additionalClassLoader : this.overriddenOrAddedClassLoaderDelegationOrder) {
                final InputStream inputStream = additionalClassLoader.getResourceAsStream(path);
                if (inputStream != null) {
                    return inputStream;
                }
            }
        }
        final ResourceList resourceList = this.scanResult.getResourcesWithPath(path);
        if (resourceList == null || resourceList.isEmpty()) {
            return super.getResourceAsStream(path);
        }
        try {
            return resourceList.get(0).open();
        }
        catch (IOException e) {
            return null;
        }
    }
}
