// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.json;

public class ReferenceEqualityKey<K>
{
    private final K wrappedKey;
    
    public ReferenceEqualityKey(final K wrappedKey) {
        this.wrappedKey = wrappedKey;
    }
    
    public K get() {
        return this.wrappedKey;
    }
    
    @Override
    public int hashCode() {
        final K key = this.wrappedKey;
        return (key == null) ? 0 : System.identityHashCode(key);
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj == this || (obj instanceof ReferenceEqualityKey && this.wrappedKey == ((ReferenceEqualityKey)obj).wrappedKey);
    }
    
    @Override
    public String toString() {
        final K key = this.wrappedKey;
        return (key == null) ? "null" : key.toString();
    }
}
