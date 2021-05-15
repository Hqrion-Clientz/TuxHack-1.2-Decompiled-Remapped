// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.utils;

import java.util.Iterator;

public final class Join
{
    private Join() {
    }
    
    public static void join(final StringBuilder buf, final String addAtBeginning, final String sep, final String addAtEnd, final Iterable<?> iterable) {
        if (!addAtBeginning.isEmpty()) {
            buf.append(addAtBeginning);
        }
        boolean first = true;
        for (final Object item : iterable) {
            if (first) {
                first = false;
            }
            else {
                buf.append(sep);
            }
            buf.append((item == null) ? "null" : item.toString());
        }
        if (!addAtEnd.isEmpty()) {
            buf.append(addAtEnd);
        }
    }
    
    public static String join(final String sep, final Iterable<?> iterable) {
        final StringBuilder buf = new StringBuilder();
        join(buf, "", sep, "", iterable);
        return buf.toString();
    }
    
    public static String join(final String sep, final Object... items) {
        final StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (final Object item : items) {
            if (first) {
                first = false;
            }
            else {
                buf.append(sep);
            }
            buf.append(item.toString());
        }
        return buf.toString();
    }
}
