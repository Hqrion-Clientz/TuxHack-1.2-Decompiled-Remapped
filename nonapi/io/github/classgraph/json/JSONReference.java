// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.json;

class JSONReference
{
    Object idObject;
    
    public JSONReference(final Object idObject) {
        if (idObject == null) {
            throw new IllegalArgumentException("idObject cannot be null");
        }
        this.idObject = idObject;
    }
}
