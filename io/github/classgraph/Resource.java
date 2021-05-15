// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import nonapi.io.github.classgraph.fileslice.reader.ClassfileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.net.URISyntaxException;
import nonapi.io.github.classgraph.utils.URLPathEncoder;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import nonapi.io.github.classgraph.utils.LogNode;
import java.nio.ByteBuffer;
import java.io.InputStream;
import java.io.Closeable;

public abstract class Resource implements Closeable, Comparable<Resource>
{
    private final ClasspathElement classpathElement;
    protected InputStream inputStream;
    protected ByteBuffer byteBuffer;
    protected long length;
    private String toString;
    LogNode scanLog;
    
    public Resource(final ClasspathElement classpathElement, final long length) {
        this.classpathElement = classpathElement;
        this.length = length;
    }
    
    private static URL uriToURL(final URI uri) {
        if (uri.getScheme().equals("jrt")) {
            throw new IllegalArgumentException("Could not create URL from URI with \"jrt:\" scheme: " + uri);
        }
        try {
            return uri.toURL();
        }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not create URL from URI: " + uri + " -- " + e);
        }
    }
    
    public URI getURI() {
        final URI locationURI = this.getClasspathElementURI();
        final String locationURIStr = locationURI.toString();
        final String resourcePath = this.getPathRelativeToClasspathElement();
        final boolean isDir = locationURIStr.endsWith("/");
        try {
            return new URI(((isDir || locationURIStr.startsWith("jar:") || locationURIStr.startsWith("jrt:")) ? "" : "jar:") + locationURIStr + (isDir ? "" : (locationURIStr.startsWith("jrt:") ? "/" : "!/")) + URLPathEncoder.encodePath(resourcePath));
        }
        catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not form URL for classpath element: " + locationURIStr + " ; path: " + resourcePath + " : " + e);
        }
    }
    
    public URL getURL() {
        return uriToURL(this.getURI());
    }
    
    public URI getClasspathElementURI() {
        return this.classpathElement.getURI();
    }
    
    public URL getClasspathElementURL() {
        return uriToURL(this.getClasspathElementURI());
    }
    
    public File getClasspathElementFile() {
        return this.classpathElement.getFile();
    }
    
    public ModuleRef getModuleRef() {
        return (this.classpathElement instanceof ClasspathElementModule) ? ((ClasspathElementModule)this.classpathElement).moduleRef : null;
    }
    
    public String getContentAsString() throws IOException {
        try {
            return new String(this.load(), StandardCharsets.UTF_8);
        }
        finally {
            this.close();
        }
    }
    
    public abstract String getPath();
    
    public abstract String getPathRelativeToClasspathElement();
    
    public abstract InputStream open() throws IOException;
    
    public abstract ByteBuffer read() throws IOException;
    
    public abstract byte[] load() throws IOException;
    
    abstract ClassfileReader openClassfile() throws IOException;
    
    public long getLength() {
        return this.length;
    }
    
    public abstract long getLastModified();
    
    public abstract Set<PosixFilePermission> getPosixFilePermissions();
    
    @Override
    public String toString() {
        if (this.toString != null) {
            return this.toString;
        }
        return this.toString = this.getURI().toString();
    }
    
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj == this || (obj instanceof Resource && this.toString().equals(obj.toString()));
    }
    
    @Override
    public int compareTo(final Resource o) {
        return this.toString().compareTo(o.toString());
    }
    
    @Override
    public void close() {
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            }
            catch (IOException ex) {}
            this.inputStream = null;
        }
    }
}
