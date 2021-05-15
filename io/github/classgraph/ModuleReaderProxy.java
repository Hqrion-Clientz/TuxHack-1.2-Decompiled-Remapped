// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.nio.ByteBuffer;
import java.io.InputStream;
import java.util.List;
import java.io.IOException;
import nonapi.io.github.classgraph.utils.ReflectionUtils;
import java.io.Closeable;

public class ModuleReaderProxy implements Closeable
{
    private final AutoCloseable moduleReader;
    private static Class<?> collectorClass;
    private static Object collectorsToList;
    
    ModuleReaderProxy(final ModuleRef moduleRef) throws IOException {
        try {
            this.moduleReader = (AutoCloseable)ReflectionUtils.invokeMethod(moduleRef.getReference(), "open", true);
            if (this.moduleReader == null) {
                throw new IllegalArgumentException("moduleReference.open() should not return null");
            }
        }
        catch (SecurityException e) {
            throw new IOException("Could not open module " + moduleRef.getName(), e);
        }
    }
    
    @Override
    public void close() {
        try {
            this.moduleReader.close();
        }
        catch (Exception ex) {}
    }
    
    public List<String> list() throws SecurityException {
        if (ModuleReaderProxy.collectorsToList == null) {
            throw new IllegalArgumentException("Could not call Collectors.toList()");
        }
        final Object resourcesStream = ReflectionUtils.invokeMethod(this.moduleReader, "list", true);
        if (resourcesStream == null) {
            throw new IllegalArgumentException("Could not call moduleReader.list()");
        }
        final Object resourcesList = ReflectionUtils.invokeMethod(resourcesStream, "collect", ModuleReaderProxy.collectorClass, ModuleReaderProxy.collectorsToList, true);
        if (resourcesList == null) {
            throw new IllegalArgumentException("Could not call moduleReader.list().collect(Collectors.toList())");
        }
        final List<String> resourcesListTyped = (List<String>)resourcesList;
        return resourcesListTyped;
    }
    
    private Object openOrRead(final String path, final boolean open) throws SecurityException {
        final String methodName = open ? "open" : "read";
        final Object optionalInputStream = ReflectionUtils.invokeMethod(this.moduleReader, methodName, String.class, path, true);
        if (optionalInputStream == null) {
            throw new IllegalArgumentException("Got null result from moduleReader." + methodName + "(name)");
        }
        final Object inputStream = ReflectionUtils.invokeMethod(optionalInputStream, "get", true);
        if (inputStream == null) {
            throw new IllegalArgumentException("Got null result from moduleReader." + methodName + "(name).get()");
        }
        return inputStream;
    }
    
    public InputStream open(final String path) throws SecurityException {
        return (InputStream)this.openOrRead(path, true);
    }
    
    public ByteBuffer read(final String path) throws SecurityException, OutOfMemoryError {
        return (ByteBuffer)this.openOrRead(path, false);
    }
    
    public void release(final ByteBuffer byteBuffer) {
        ReflectionUtils.invokeMethod(this.moduleReader, "release", ByteBuffer.class, byteBuffer, true);
    }
    
    static {
        ModuleReaderProxy.collectorClass = ReflectionUtils.classForNameOrNull("java.util.stream.Collector");
        final Class<?> collectorsClass = ReflectionUtils.classForNameOrNull("java.util.stream.Collectors");
        if (collectorsClass != null) {
            ModuleReaderProxy.collectorsToList = ReflectionUtils.invokeStaticMethod(collectorsClass, "toList", true);
        }
    }
}
