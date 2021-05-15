// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import nonapi.io.github.classgraph.utils.LogNode;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class SingletonMap<K, V, E extends Exception>
{
    private final ConcurrentMap<K, SingletonHolder<V>> map;
    
    public SingletonMap() {
        this.map = new ConcurrentHashMap<K, SingletonHolder<V>>();
    }
    
    public abstract V newInstance(final K p0, final LogNode p1) throws E, InterruptedException, Exception;
    
    public V get(final K key, final LogNode log) throws E, InterruptedException, NullSingletonException, Exception {
        final SingletonHolder<V> singletonHolder = this.map.get(key);
        V instance = null;
        if (singletonHolder != null) {
            instance = singletonHolder.get();
        }
        else {
            final SingletonHolder<V> newSingletonHolder = new SingletonHolder<V>();
            final SingletonHolder<V> oldSingletonHolder = this.map.putIfAbsent(key, newSingletonHolder);
            if (oldSingletonHolder != null) {
                instance = oldSingletonHolder.get();
            }
            else {
                try {
                    instance = this.newInstance(key, log);
                }
                finally {
                    newSingletonHolder.set(instance);
                }
            }
        }
        if (instance == null) {
            throw new NullSingletonException((K)key);
        }
        return instance;
    }
    
    public List<V> values() throws InterruptedException {
        final List<V> entries = new ArrayList<V>(this.map.size());
        for (final Map.Entry<K, SingletonHolder<V>> ent : this.map.entrySet()) {
            final V entryValue = ent.getValue().get();
            if (entryValue != null) {
                entries.add(entryValue);
            }
        }
        return entries;
    }
    
    public boolean isEmpty() {
        return this.map.isEmpty();
    }
    
    public List<Map.Entry<K, V>> entries() throws InterruptedException {
        final List<Map.Entry<K, V>> entries = new ArrayList<Map.Entry<K, V>>(this.map.size());
        for (final Map.Entry<K, SingletonHolder<V>> ent : this.map.entrySet()) {
            entries.add(new AbstractMap.SimpleEntry<K, V>(ent.getKey(), ent.getValue().get()));
        }
        return entries;
    }
    
    public V remove(final K key) throws InterruptedException {
        final SingletonHolder<V> val = this.map.remove(key);
        return (val == null) ? null : val.get();
    }
    
    public void clear() {
        this.map.clear();
    }
    
    public static class NullSingletonException extends Exception
    {
        static final long serialVersionUID = 1L;
        
        public <K> NullSingletonException(final K key) {
            super("newInstance returned null for key " + key);
        }
    }
    
    private static class SingletonHolder<V>
    {
        private volatile V singleton;
        private final CountDownLatch initialized;
        
        private SingletonHolder() {
            this.initialized = new CountDownLatch(1);
        }
        
        void set(final V singleton) throws IllegalArgumentException {
            if (this.initialized.getCount() < 1L) {
                throw new IllegalArgumentException("Singleton already initialized");
            }
            this.singleton = singleton;
            this.initialized.countDown();
            if (this.initialized.getCount() != 0L) {
                throw new IllegalArgumentException("Singleton initialized more than once");
            }
        }
        
        V get() throws InterruptedException {
            this.initialized.await();
            return this.singleton;
        }
    }
}
