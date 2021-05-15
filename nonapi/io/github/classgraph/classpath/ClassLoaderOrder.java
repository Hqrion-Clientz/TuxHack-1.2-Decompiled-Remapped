// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classpath;

import java.util.AbstractMap;
import java.util.Iterator;
import nonapi.io.github.classgraph.utils.LogNode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import nonapi.io.github.classgraph.classloaderhandler.ClassLoaderHandlerRegistry;
import java.util.Map;
import java.util.List;

public class ClassLoaderOrder
{
    private final List<Map.Entry<ClassLoader, ClassLoaderHandlerRegistry.ClassLoaderHandlerRegistryEntry>> classLoaderOrder;
    private final Set<ClassLoader> added;
    private final Set<ClassLoader> delegatedTo;
    private final Set<ClassLoader> allParentClassLoaders;
    private final Map<ClassLoader, ClassLoaderHandlerRegistry.ClassLoaderHandlerRegistryEntry> classLoaderToClassLoaderHandlerRegistryEntry;
    
    public ClassLoaderOrder() {
        this.classLoaderOrder = new ArrayList<Map.Entry<ClassLoader, ClassLoaderHandlerRegistry.ClassLoaderHandlerRegistryEntry>>();
        this.added = new HashSet<ClassLoader>();
        this.delegatedTo = new HashSet<ClassLoader>();
        this.allParentClassLoaders = new HashSet<ClassLoader>();
        this.classLoaderToClassLoaderHandlerRegistryEntry = new HashMap<ClassLoader, ClassLoaderHandlerRegistry.ClassLoaderHandlerRegistryEntry>();
    }
    
    public List<Map.Entry<ClassLoader, ClassLoaderHandlerRegistry.ClassLoaderHandlerRegistryEntry>> getClassLoaderOrder() {
        return this.classLoaderOrder;
    }
    
    public Set<ClassLoader> getAllParentClassLoaders() {
        return this.allParentClassLoaders;
    }
    
    private ClassLoaderHandlerRegistry.ClassLoaderHandlerRegistryEntry getRegistryEntry(final ClassLoader classLoader, final LogNode log) {
        ClassLoaderHandlerRegistry.ClassLoaderHandlerRegistryEntry entry = this.classLoaderToClassLoaderHandlerRegistryEntry.get(classLoader);
        if (entry == null) {
            for (Class<?> currClassLoaderClass = classLoader.getClass(); currClassLoaderClass != Object.class && currClassLoaderClass != null; currClassLoaderClass = currClassLoaderClass.getSuperclass()) {
                for (final ClassLoaderHandlerRegistry.ClassLoaderHandlerRegistryEntry ent : ClassLoaderHandlerRegistry.CLASS_LOADER_HANDLERS) {
                    if (ent.canHandle(currClassLoaderClass, log)) {
                        entry = ent;
                        break;
                    }
                }
                if (entry != null) {
                    break;
                }
            }
            if (entry == null) {
                entry = ClassLoaderHandlerRegistry.FALLBACK_HANDLER;
            }
            this.classLoaderToClassLoaderHandlerRegistryEntry.put(classLoader, entry);
        }
        return entry;
    }
    
    public void add(final ClassLoader classLoader, final LogNode log) {
        if (classLoader == null) {
            return;
        }
        if (this.added.add(classLoader)) {
            final ClassLoaderHandlerRegistry.ClassLoaderHandlerRegistryEntry entry = this.getRegistryEntry(classLoader, log);
            if (entry != null) {
                this.classLoaderOrder.add(new AbstractMap.SimpleEntry<ClassLoader, ClassLoaderHandlerRegistry.ClassLoaderHandlerRegistryEntry>(classLoader, entry));
            }
        }
    }
    
    public void delegateTo(final ClassLoader classLoader, final boolean isParent, final LogNode log) {
        if (classLoader == null) {
            return;
        }
        if (isParent) {
            this.allParentClassLoaders.add(classLoader);
        }
        if (this.delegatedTo.add(classLoader)) {
            final ClassLoaderHandlerRegistry.ClassLoaderHandlerRegistryEntry entry = this.getRegistryEntry(classLoader, log);
            entry.findClassLoaderOrder(classLoader, this, log);
        }
    }
}
