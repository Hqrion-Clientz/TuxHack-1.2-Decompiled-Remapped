// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.io.InputStream;
import java.util.ListIterator;
import java.nio.ByteBuffer;
import java.io.IOException;
import nonapi.io.github.classgraph.utils.CollectionUtils;
import java.util.Comparator;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;

public class ResourceList extends PotentiallyUnmodifiableList<Resource> implements AutoCloseable
{
    static final long serialVersionUID = 1L;
    static final ResourceList EMPTY_LIST;
    private static final ResourceFilter CLASSFILE_FILTER;
    
    public static ResourceList emptyList() {
        return ResourceList.EMPTY_LIST;
    }
    
    public ResourceList() {
    }
    
    public ResourceList(final int sizeHint) {
        super(sizeHint);
    }
    
    public ResourceList(final Collection<Resource> resourceCollection) {
        super(resourceCollection);
    }
    
    public ResourceList get(final String resourcePath) {
        boolean hasResourceWithPath = false;
        for (final Resource res : this) {
            if (res.getPath().equals(resourcePath)) {
                hasResourceWithPath = true;
                break;
            }
        }
        if (!hasResourceWithPath) {
            return ResourceList.EMPTY_LIST;
        }
        final ResourceList matchingResources = new ResourceList(2);
        for (final Resource res2 : this) {
            if (res2.getPath().equals(resourcePath)) {
                matchingResources.add(res2);
            }
        }
        return matchingResources;
    }
    
    public List<String> getPaths() {
        final List<String> resourcePaths = new ArrayList<String>(this.size());
        for (final Resource resource : this) {
            resourcePaths.add(resource.getPath());
        }
        return resourcePaths;
    }
    
    public List<String> getPathsRelativeToClasspathElement() {
        final List<String> resourcePaths = new ArrayList<String>(this.size());
        for (final Resource resource : this) {
            resourcePaths.add(resource.getPath());
        }
        return resourcePaths;
    }
    
    public List<URL> getURLs() {
        final List<URL> resourceURLs = new ArrayList<URL>(this.size());
        for (final Resource resource : this) {
            resourceURLs.add(resource.getURL());
        }
        return resourceURLs;
    }
    
    public List<URI> getURIs() {
        final List<URI> resourceURLs = new ArrayList<URI>(this.size());
        for (final Resource resource : this) {
            resourceURLs.add(resource.getURI());
        }
        return resourceURLs;
    }
    
    public ResourceList classFilesOnly() {
        return this.filter(ResourceList.CLASSFILE_FILTER);
    }
    
    public ResourceList nonClassFilesOnly() {
        return this.filter(new ResourceFilter() {
            @Override
            public boolean accept(final Resource resource) {
                return !ResourceList.CLASSFILE_FILTER.accept(resource);
            }
        });
    }
    
    public Map<String, ResourceList> asMap() {
        final Map<String, ResourceList> pathToResourceList = new HashMap<String, ResourceList>();
        for (final Resource resource : this) {
            final String path = resource.getPath();
            ResourceList resourceList = pathToResourceList.get(path);
            if (resourceList == null) {
                resourceList = new ResourceList(1);
                pathToResourceList.put(path, resourceList);
            }
            resourceList.add(resource);
        }
        return pathToResourceList;
    }
    
    public List<Map.Entry<String, ResourceList>> findDuplicatePaths() {
        final List<Map.Entry<String, ResourceList>> duplicatePaths = new ArrayList<Map.Entry<String, ResourceList>>();
        for (final Map.Entry<String, ResourceList> pathAndResourceList : this.asMap().entrySet()) {
            if (pathAndResourceList.getValue().size() > 1) {
                duplicatePaths.add(new AbstractMap.SimpleEntry<String, ResourceList>(pathAndResourceList.getKey(), pathAndResourceList.getValue()));
            }
        }
        CollectionUtils.sortIfNotEmpty(duplicatePaths, new Comparator<Map.Entry<String, ResourceList>>() {
            @Override
            public int compare(final Map.Entry<String, ResourceList> o1, final Map.Entry<String, ResourceList> o2) {
                return o1.getKey().compareTo((String)o2.getKey());
            }
        });
        return duplicatePaths;
    }
    
    public ResourceList filter(final ResourceFilter filter) {
        final ResourceList resourcesFiltered = new ResourceList();
        for (final Resource resource : this) {
            if (filter.accept(resource)) {
                resourcesFiltered.add(resource);
            }
        }
        return resourcesFiltered;
    }
    
    @Deprecated
    public void forEachByteArray(final ByteArrayConsumer byteArrayConsumer, final boolean ignoreIOExceptions) {
        for (final Resource resource : this) {
            try {
                final byte[] resourceContent = resource.load();
                byteArrayConsumer.accept(resource, resourceContent);
            }
            catch (IOException e) {
                if (!ignoreIOExceptions) {
                    throw new IllegalArgumentException("Could not load resource " + resource, e);
                }
                continue;
            }
            finally {
                resource.close();
            }
        }
    }
    
    @Deprecated
    public void forEachByteArray(final ByteArrayConsumer byteArrayConsumer) {
        this.forEachByteArray(byteArrayConsumer, false);
    }
    
    public void forEachByteArrayIgnoringIOException(final ByteArrayConsumer byteArrayConsumer) {
        for (final Resource resource : this) {
            try {
                final byte[] resourceContent = resource.load();
                byteArrayConsumer.accept(resource, resourceContent);
            }
            catch (IOException ex) {}
            finally {
                resource.close();
            }
        }
    }
    
    public void forEachByteArrayThrowingIOException(final ByteArrayConsumerThrowsIOException byteArrayConsumerThrowsIOException) throws IOException {
        for (final Resource resource : this) {
            try {
                final byte[] resourceContent = resource.load();
                byteArrayConsumerThrowsIOException.accept(resource, resourceContent);
            }
            finally {
                resource.close();
            }
        }
    }
    
    @Deprecated
    public void forEachInputStream(final InputStreamConsumer inputStreamConsumer, final boolean ignoreIOExceptions) {
        for (final Resource resource : this) {
            try {
                inputStreamConsumer.accept(resource, resource.open());
            }
            catch (IOException e) {
                if (!ignoreIOExceptions) {
                    throw new IllegalArgumentException("Could not load resource " + resource, e);
                }
                continue;
            }
            finally {
                resource.close();
            }
        }
    }
    
    @Deprecated
    public void forEachInputStream(final InputStreamConsumer inputStreamConsumer) {
        this.forEachInputStream(inputStreamConsumer, false);
    }
    
    public void forEachInputStreamIgnoringIOException(final InputStreamConsumer inputStreamConsumer) {
        for (final Resource resource : this) {
            try {
                inputStreamConsumer.accept(resource, resource.open());
            }
            catch (IOException ex) {}
            finally {
                resource.close();
            }
        }
    }
    
    public void forEachInputStreamThrowingIOException(final InputStreamConsumerThrowsIOException inputStreamConsumerThrowsIOException) throws IOException {
        for (final Resource resource : this) {
            try {
                inputStreamConsumerThrowsIOException.accept(resource, resource.open());
            }
            finally {
                resource.close();
            }
        }
    }
    
    @Deprecated
    public void forEachByteBuffer(final ByteBufferConsumer byteBufferConsumer, final boolean ignoreIOExceptions) {
        for (final Resource resource : this) {
            try {
                final ByteBuffer byteBuffer = resource.read();
                byteBufferConsumer.accept(resource, byteBuffer);
            }
            catch (IOException e) {
                if (!ignoreIOExceptions) {
                    throw new IllegalArgumentException("Could not load resource " + resource, e);
                }
                continue;
            }
            finally {
                resource.close();
            }
        }
    }
    
    @Deprecated
    public void forEachByteBuffer(final ByteBufferConsumer byteBufferConsumer) {
        this.forEachByteBuffer(byteBufferConsumer, false);
    }
    
    public void forEachByteBufferIgnoringIOException(final ByteBufferConsumer byteBufferConsumer) {
        for (final Resource resource : this) {
            try {
                final ByteBuffer byteBuffer = resource.read();
                byteBufferConsumer.accept(resource, byteBuffer);
            }
            catch (IOException ex) {}
            finally {
                resource.close();
            }
        }
    }
    
    public void forEachByteBufferThrowingIOException(final ByteBufferConsumerThrowsIOException byteBufferConsumerThrowsIOException) throws IOException {
        for (final Resource resource : this) {
            try {
                final ByteBuffer byteBuffer = resource.read();
                byteBufferConsumerThrowsIOException.accept(resource, byteBuffer);
            }
            finally {
                resource.close();
            }
        }
    }
    
    @Override
    public void close() {
        for (final Resource resource : this) {
            resource.close();
        }
    }
    
    static {
        (EMPTY_LIST = new ResourceList()).makeUnmodifiable();
        CLASSFILE_FILTER = new ResourceFilter() {
            @Override
            public boolean accept(final Resource resource) {
                final String path = resource.getPath();
                if (!path.endsWith(".class") || path.length() < 7) {
                    return false;
                }
                final char c = path.charAt(path.length() - 7);
                return c != '/' && c != '.';
            }
        };
    }
    
    @FunctionalInterface
    public interface ByteBufferConsumerThrowsIOException
    {
        void accept(final Resource p0, final ByteBuffer p1) throws IOException;
    }
    
    @FunctionalInterface
    public interface ByteBufferConsumer
    {
        void accept(final Resource p0, final ByteBuffer p1);
    }
    
    @FunctionalInterface
    public interface InputStreamConsumerThrowsIOException
    {
        void accept(final Resource p0, final InputStream p1) throws IOException;
    }
    
    @FunctionalInterface
    public interface InputStreamConsumer
    {
        void accept(final Resource p0, final InputStream p1);
    }
    
    @FunctionalInterface
    public interface ByteArrayConsumerThrowsIOException
    {
        void accept(final Resource p0, final byte[] p1) throws IOException;
    }
    
    @FunctionalInterface
    public interface ByteArrayConsumer
    {
        void accept(final Resource p0, final byte[] p1);
    }
    
    @FunctionalInterface
    public interface ResourceFilter
    {
        boolean accept(final Resource p0);
    }
}
