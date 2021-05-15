// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.recycler;

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Queue;
import java.util.Set;

public abstract class Recycler<T, E extends Exception> implements AutoCloseable
{
    private final Set<T> usedInstances;
    private final Queue<T> unusedInstances;
    
    public Recycler() {
        this.usedInstances = Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>());
        this.unusedInstances = new ConcurrentLinkedQueue<T>();
    }
    
    public abstract T newInstance() throws E, Exception;
    
    public T acquire() throws E, Exception {
        final T recycledInstance = this.unusedInstances.poll();
        T instance;
        if (recycledInstance == null) {
            final T newInstance = this.newInstance();
            if (newInstance == null) {
                throw new NullPointerException("Failed to allocate a new recyclable instance");
            }
            instance = newInstance;
        }
        else {
            instance = recycledInstance;
        }
        this.usedInstances.add(instance);
        return instance;
    }
    
    public RecycleOnClose<T, E> acquireRecycleOnClose() throws E, Exception {
        return new RecycleOnClose<T, E>(this, this.acquire());
    }
    
    public final void recycle(final T instance) {
        if (instance != null) {
            if (!this.usedInstances.remove(instance)) {
                throw new IllegalArgumentException("Tried to recycle an instance that was not in use");
            }
            if (instance instanceof Resettable) {
                ((Resettable)instance).reset();
            }
            if (!this.unusedInstances.add(instance)) {
                throw new IllegalArgumentException("Tried to recycle an instance twice");
            }
        }
    }
    
    @Override
    public void close() {
        T unusedInstance;
        while ((unusedInstance = this.unusedInstances.poll()) != null) {
            if (unusedInstance instanceof AutoCloseable) {
                try {
                    ((AutoCloseable)unusedInstance).close();
                }
                catch (Exception ex) {}
            }
        }
    }
    
    public void forceClose() {
        for (final T usedInstance : new ArrayList<T>((Collection<? extends T>)this.usedInstances)) {
            if (this.usedInstances.remove(usedInstance)) {
                this.unusedInstances.add(usedInstance);
            }
        }
        this.close();
    }
}
