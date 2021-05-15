// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.ListIterator;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

class PotentiallyUnmodifiableList<T> extends ArrayList<T>
{
    static final long serialVersionUID = 1L;
    boolean modifiable;
    
    PotentiallyUnmodifiableList() {
        this.modifiable = true;
    }
    
    PotentiallyUnmodifiableList(final int sizeHint) {
        super(sizeHint);
        this.modifiable = true;
    }
    
    PotentiallyUnmodifiableList(final Collection<T> collection) {
        super(collection);
        this.modifiable = true;
    }
    
    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    void makeUnmodifiable() {
        this.modifiable = false;
    }
    
    @Override
    public boolean add(final T element) {
        if (!this.modifiable) {
            throw new IllegalArgumentException("List is immutable");
        }
        return super.add(element);
    }
    
    @Override
    public void add(final int index, final T element) {
        if (!this.modifiable) {
            throw new IllegalArgumentException("List is immutable");
        }
        super.add(index, element);
    }
    
    @Override
    public boolean remove(final Object o) {
        if (!this.modifiable) {
            throw new IllegalArgumentException("List is immutable");
        }
        return super.remove(o);
    }
    
    @Override
    public T remove(final int index) {
        if (!this.modifiable) {
            throw new IllegalArgumentException("List is immutable");
        }
        return super.remove(index);
    }
    
    @Override
    public boolean addAll(final Collection<? extends T> c) {
        if (!this.modifiable && !c.isEmpty()) {
            throw new IllegalArgumentException("List is immutable");
        }
        return super.addAll(c);
    }
    
    @Override
    public boolean addAll(final int index, final Collection<? extends T> c) {
        if (!this.modifiable && !c.isEmpty()) {
            throw new IllegalArgumentException("List is immutable");
        }
        return super.addAll(index, c);
    }
    
    @Override
    public boolean removeAll(final Collection<?> c) {
        if (!this.modifiable && !c.isEmpty()) {
            throw new IllegalArgumentException("List is immutable");
        }
        return super.removeAll(c);
    }
    
    @Override
    public boolean retainAll(final Collection<?> c) {
        if (!this.modifiable && !this.isEmpty()) {
            throw new IllegalArgumentException("List is immutable");
        }
        return super.retainAll(c);
    }
    
    @Override
    public void clear() {
        if (!this.modifiable && !this.isEmpty()) {
            throw new IllegalArgumentException("List is immutable");
        }
        super.clear();
    }
    
    @Override
    public T set(final int index, final T element) {
        if (!this.modifiable) {
            throw new IllegalArgumentException("List is immutable");
        }
        return super.set(index, element);
    }
    
    @Override
    public Iterator<T> iterator() {
        final Iterator<T> iterator = super.iterator();
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return !PotentiallyUnmodifiableList.this.isEmpty() && iterator.hasNext();
            }
            
            @Override
            public T next() {
                return iterator.next();
            }
            
            @Override
            public void remove() {
                if (!PotentiallyUnmodifiableList.this.modifiable) {
                    throw new IllegalArgumentException("List is immutable");
                }
                iterator.remove();
            }
        };
    }
    
    @Override
    public ListIterator<T> listIterator() {
        final ListIterator<T> iterator = super.listIterator();
        return new ListIterator<T>() {
            @Override
            public boolean hasNext() {
                return !PotentiallyUnmodifiableList.this.isEmpty() && iterator.hasNext();
            }
            
            @Override
            public T next() {
                return iterator.next();
            }
            
            @Override
            public boolean hasPrevious() {
                return !PotentiallyUnmodifiableList.this.isEmpty() && iterator.hasPrevious();
            }
            
            @Override
            public T previous() {
                return iterator.previous();
            }
            
            @Override
            public int nextIndex() {
                if (PotentiallyUnmodifiableList.this.isEmpty()) {
                    return 0;
                }
                return iterator.nextIndex();
            }
            
            @Override
            public int previousIndex() {
                if (PotentiallyUnmodifiableList.this.isEmpty()) {
                    return -1;
                }
                return iterator.previousIndex();
            }
            
            @Override
            public void remove() {
                if (!PotentiallyUnmodifiableList.this.modifiable) {
                    throw new IllegalArgumentException("List is immutable");
                }
                iterator.remove();
            }
            
            @Override
            public void set(final T e) {
                if (!PotentiallyUnmodifiableList.this.modifiable) {
                    throw new IllegalArgumentException("List is immutable");
                }
                iterator.set(e);
            }
            
            @Override
            public void add(final T e) {
                if (!PotentiallyUnmodifiableList.this.modifiable) {
                    throw new IllegalArgumentException("List is immutable");
                }
                iterator.add(e);
            }
        };
    }
}
