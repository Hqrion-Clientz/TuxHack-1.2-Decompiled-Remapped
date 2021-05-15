// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

public class ClassGraphException extends IllegalArgumentException
{
    static final long serialVersionUID = 1L;
    
    private ClassGraphException(final String message) {
        super(message);
    }
    
    private ClassGraphException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public static ClassGraphException newClassGraphException(final String message) {
        return new ClassGraphException(message);
    }
    
    public static ClassGraphException newClassGraphException(final String message, final Throwable cause) throws ClassGraphException {
        return new ClassGraphException(message, cause);
    }
}
