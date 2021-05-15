// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.concurrency;

import io.github.classgraph.ClassGraphException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;

public class AutoCloseableExecutorService extends ThreadPoolExecutor implements AutoCloseable
{
    public final InterruptionChecker interruptionChecker;
    
    public AutoCloseableExecutorService(final int numThreads) {
        super(numThreads, numThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new SimpleThreadFactory("ClassGraph-worker-", true));
        this.interruptionChecker = new InterruptionChecker();
    }
    
    public void afterExecute(final Runnable runnable, final Throwable throwable) {
        super.afterExecute(runnable, throwable);
        if (throwable != null) {
            this.interruptionChecker.setExecutionException(new ExecutionException("Uncaught exception", throwable));
            this.interruptionChecker.interrupt();
        }
        else if (runnable instanceof Future) {
            try {
                ((Future)runnable).get();
            }
            catch (CancellationException | InterruptedException ex2) {
                final Exception ex;
                final Exception e = ex;
                this.interruptionChecker.interrupt();
            }
            catch (ExecutionException e2) {
                this.interruptionChecker.setExecutionException(e2);
                this.interruptionChecker.interrupt();
            }
        }
    }
    
    @Override
    public void close() {
        try {
            this.shutdown();
        }
        catch (SecurityException ex) {}
        boolean terminated = false;
        try {
            terminated = this.awaitTermination(2500L, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e2) {
            this.interruptionChecker.interrupt();
        }
        if (!terminated) {
            try {
                this.shutdownNow();
            }
            catch (SecurityException e) {
                throw ClassGraphException.newClassGraphException("Could not shut down ExecutorService -- need java.lang.RuntimePermission(\"modifyThread\"), or the security manager's checkAccess method denies access", e);
            }
        }
    }
}
