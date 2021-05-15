// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.concurrency;

import java.util.concurrent.CancellationException;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.Collection;
import nonapi.io.github.classgraph.utils.LogNode;
import java.util.concurrent.Future;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.BlockingQueue;

public class WorkQueue<T> implements AutoCloseable
{
    private final WorkUnitProcessor<T> workUnitProcessor;
    private final BlockingQueue<WorkUnitWrapper<T>> workUnits;
    private final int numWorkers;
    private final AtomicInteger numIncompleteWorkUnits;
    private final ConcurrentLinkedQueue<Future<?>> workerFutures;
    private final InterruptionChecker interruptionChecker;
    private final LogNode log;
    
    public static <U> void runWorkQueue(final Collection<U> elements, final ExecutorService executorService, final InterruptionChecker interruptionChecker, final int numParallelTasks, final LogNode log, final WorkUnitProcessor<U> workUnitProcessor) throws InterruptedException, ExecutionException {
        if (elements.isEmpty()) {
            return;
        }
        try (final WorkQueue<U> workQueue = new WorkQueue<U>(elements, workUnitProcessor, numParallelTasks, interruptionChecker, log)) {
            workQueue.startWorkers(executorService, numParallelTasks - 1);
            workQueue.runWorkLoop();
        }
    }
    
    private WorkQueue(final Collection<T> initialWorkUnits, final WorkUnitProcessor<T> workUnitProcessor, final int numWorkers, final InterruptionChecker interruptionChecker, final LogNode log) {
        this.workUnits = new LinkedBlockingQueue<WorkUnitWrapper<T>>();
        this.numIncompleteWorkUnits = new AtomicInteger();
        this.workerFutures = new ConcurrentLinkedQueue<Future<?>>();
        this.workUnitProcessor = workUnitProcessor;
        this.numWorkers = numWorkers;
        this.interruptionChecker = interruptionChecker;
        this.log = log;
        this.addWorkUnits(initialWorkUnits);
    }
    
    private void startWorkers(final ExecutorService executorService, final int numTasks) {
        for (int i = 0; i < numTasks; ++i) {
            this.workerFutures.add(executorService.submit((Callable<Object>)new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    WorkQueue.this.runWorkLoop();
                    return null;
                }
            }));
        }
    }
    
    private void sendPoisonPills() {
        for (int i = 0; i < this.numWorkers; ++i) {
            this.workUnits.add(new WorkUnitWrapper<T>(null));
        }
    }
    
    private void runWorkLoop() throws InterruptedException, ExecutionException {
        while (true) {
            this.interruptionChecker.check();
            final WorkUnitWrapper<T> workUnitWrapper = this.workUnits.take();
            if (workUnitWrapper.workUnit == null) {
                break;
            }
            try {
                this.workUnitProcessor.processWorkUnit(workUnitWrapper.workUnit, this, this.log);
            }
            catch (InterruptedException | OutOfMemoryError ex) {
                final Throwable t;
                final Throwable e = t;
                this.workUnits.clear();
                this.sendPoisonPills();
                throw e;
            }
            catch (RuntimeException e2) {
                this.workUnits.clear();
                this.sendPoisonPills();
                throw new ExecutionException("Worker thread threw unchecked exception", e2);
            }
            finally {
                if (this.numIncompleteWorkUnits.decrementAndGet() == 0) {
                    this.sendPoisonPills();
                }
            }
        }
    }
    
    public void addWorkUnit(final T workUnit) {
        if (workUnit == null) {
            throw new NullPointerException("workUnit cannot be null");
        }
        this.numIncompleteWorkUnits.incrementAndGet();
        this.workUnits.add(new WorkUnitWrapper<T>(workUnit));
    }
    
    public void addWorkUnits(final Collection<T> workUnits) {
        for (final T workUnit : workUnits) {
            this.addWorkUnit(workUnit);
        }
    }
    
    @Override
    public void close() throws ExecutionException {
        Future<?> future;
        while ((future = this.workerFutures.poll()) != null) {
            try {
                future.get();
            }
            catch (CancellationException e2) {
                if (this.log == null) {
                    continue;
                }
                this.log.log("~", "Worker thread was cancelled");
            }
            catch (InterruptedException e3) {
                if (this.log != null) {
                    this.log.log("~", "Worker thread was interrupted");
                }
                this.interruptionChecker.interrupt();
            }
            catch (ExecutionException e) {
                this.interruptionChecker.setExecutionException(e);
                this.interruptionChecker.interrupt();
            }
        }
    }
    
    private static class WorkUnitWrapper<T>
    {
        final T workUnit;
        
        public WorkUnitWrapper(final T workUnit) {
            this.workUnit = workUnit;
        }
    }
    
    public interface WorkUnitProcessor<T>
    {
        void processWorkUnit(final T p0, final WorkQueue<T> p1, final LogNode p2) throws InterruptedException;
    }
}
