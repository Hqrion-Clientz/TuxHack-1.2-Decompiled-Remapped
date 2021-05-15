// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.concurrency;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;

public class SimpleThreadFactory implements ThreadFactory
{
    private final String threadNamePrefix;
    private static final AtomicInteger threadIdx;
    private final boolean daemon;
    
    SimpleThreadFactory(final String threadNamePrefix, final boolean daemon) {
        this.threadNamePrefix = threadNamePrefix;
        this.daemon = daemon;
    }
    
    @Override
    public Thread newThread(final Runnable runnable) {
        final SecurityManager s = System.getSecurityManager();
        final Thread thread = new Thread((s != null) ? s.getThreadGroup() : new ThreadGroup("ClassGraph-thread-group"), runnable, this.threadNamePrefix + SimpleThreadFactory.threadIdx.getAndIncrement());
        thread.setDaemon(this.daemon);
        return thread;
    }
    
    static {
        threadIdx = new AtomicInteger();
    }
}
