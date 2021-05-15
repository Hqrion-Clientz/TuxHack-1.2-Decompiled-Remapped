// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.utils;

import java.util.Comparator;
import java.util.Collections;
import java.util.List;

public final class CollectionUtils
{
    private CollectionUtils() {
    }
    
    public static <T extends Comparable<? super T>> void sortIfNotEmpty(final List<T> list) {
        if (!list.isEmpty()) {
            Collections.sort(list);
        }
    }
    
    public static <T> void sortIfNotEmpty(final List<T> list, final Comparator<? super T> comparator) {
        if (!list.isEmpty()) {
            Collections.sort(list, comparator);
        }
    }
}
