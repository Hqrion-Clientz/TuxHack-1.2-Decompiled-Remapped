// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.utils;

import java.util.Locale;
import java.util.Collection;
import java.util.Iterator;
import java.util.Calendar;
import io.github.classgraph.ClassGraph;
import nonapi.io.github.classgraph.classpath.SystemJarFinder;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentSkipListMap;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.logging.Logger;

public final class LogNode
{
    private static final Logger log;
    private final long timeStampNano;
    private final long timeStampMillis;
    private final String msg;
    private String stackTrace;
    private long elapsedTimeNanos;
    private LogNode parent;
    private final Map<String, LogNode> children;
    private final String sortKeyPrefix;
    private static AtomicInteger sortKeyUniqueSuffix;
    private static final SimpleDateFormat dateTimeFormatter;
    private static final DecimalFormat nanoFormatter;
    private static boolean logInRealtime;
    
    public static void logInRealtime(final boolean logInRealtime) {
        LogNode.logInRealtime = logInRealtime;
    }
    
    private LogNode(final String sortKey, final String msg, final long elapsedTimeNanos, final Throwable exception) {
        this.timeStampNano = System.nanoTime();
        this.timeStampMillis = System.currentTimeMillis();
        this.children = new ConcurrentSkipListMap<String, LogNode>();
        this.sortKeyPrefix = sortKey;
        this.msg = msg;
        this.elapsedTimeNanos = elapsedTimeNanos;
        if (exception != null) {
            final StringWriter writer = new StringWriter();
            exception.printStackTrace(new PrintWriter(writer));
            this.stackTrace = writer.toString();
        }
        else {
            this.stackTrace = null;
        }
        if (LogNode.logInRealtime) {
            LogNode.log.info(this.toString());
        }
    }
    
    public LogNode() {
        this("", "", -1L, null);
        this.log("ClassGraph version " + VersionFinder.getVersion());
        this.logJavaInfo();
    }
    
    private void logJavaInfo() {
        this.log("Operating system: " + VersionFinder.getProperty("os.name") + " " + VersionFinder.getProperty("os.version") + " " + VersionFinder.getProperty("os.arch"));
        this.log("Java version: " + VersionFinder.getProperty("java.version") + " / " + VersionFinder.getProperty("java.runtime.version") + " (" + VersionFinder.getProperty("java.vendor") + ")");
        this.log("Java home: " + VersionFinder.getProperty("java.home"));
        final String jreRtJarPath = SystemJarFinder.getJreRtJarPath();
        if (jreRtJarPath != null) {
            this.log("JRE rt.jar:").log(jreRtJarPath);
        }
    }
    
    private void appendLine(final String timeStampStr, final int indentLevel, final String line, final StringBuilder buf) {
        buf.append(timeStampStr);
        buf.append('\t');
        buf.append(ClassGraph.class.getSimpleName());
        buf.append('\t');
        final int numDashes = 2 * (indentLevel - 1);
        for (int i = 0; i < numDashes; ++i) {
            buf.append('-');
        }
        if (numDashes > 0) {
            buf.append(' ');
        }
        buf.append(line);
        buf.append('\n');
    }
    
    private void toString(final int indentLevel, final StringBuilder buf) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(this.timeStampMillis);
        final String timeStampStr;
        synchronized (LogNode.dateTimeFormatter) {
            timeStampStr = LogNode.dateTimeFormatter.format(cal.getTime());
        }
        if (this.msg != null && !this.msg.isEmpty()) {
            this.appendLine(timeStampStr, indentLevel, (this.elapsedTimeNanos > 0L) ? (this.msg + " (took " + LogNode.nanoFormatter.format(this.elapsedTimeNanos * 1.0E-9) + " sec)") : this.msg, buf);
        }
        if (this.stackTrace != null && !this.stackTrace.isEmpty()) {
            final String[] split;
            final String[] parts = split = this.stackTrace.split("\n");
            for (final String part : split) {
                this.appendLine(timeStampStr, indentLevel, part, buf);
            }
        }
        for (final Map.Entry<String, LogNode> ent : this.children.entrySet()) {
            final LogNode child = ent.getValue();
            child.toString(indentLevel + 1, buf);
        }
    }
    
    @Override
    public String toString() {
        synchronized (LogNode.dateTimeFormatter) {
            final StringBuilder buf = new StringBuilder();
            this.toString(0, buf);
            return buf.toString();
        }
    }
    
    public void addElapsedTime() {
        this.elapsedTimeNanos = System.nanoTime() - this.timeStampNano;
    }
    
    private LogNode addChild(final String sortKey, final String msg, final long elapsedTimeNanos, final Throwable exception) {
        final String newSortKey = this.sortKeyPrefix + "\t" + ((sortKey == null) ? "" : sortKey) + "\t" + String.format("%09d", LogNode.sortKeyUniqueSuffix.getAndIncrement());
        final LogNode newChild = new LogNode(newSortKey, msg, elapsedTimeNanos, exception);
        newChild.parent = this;
        this.children.put(newSortKey, newChild);
        return newChild;
    }
    
    private LogNode addChild(final String sortKey, final String msg, final long elapsedTimeNanos) {
        return this.addChild(sortKey, msg, elapsedTimeNanos, null);
    }
    
    private LogNode addChild(final Throwable exception) {
        return this.addChild("", "", -1L, exception);
    }
    
    public LogNode log(final String sortKey, final String msg, final long elapsedTimeNanos, final Throwable e) {
        return this.addChild(sortKey, msg, elapsedTimeNanos).addChild(e);
    }
    
    public LogNode log(final String sortKey, final String msg, final long elapsedTimeNanos) {
        return this.addChild(sortKey, msg, elapsedTimeNanos);
    }
    
    public LogNode log(final String sortKey, final String msg, final Throwable e) {
        return this.addChild(sortKey, msg, -1L).addChild(e);
    }
    
    public LogNode log(final String sortKey, final String msg) {
        return this.addChild(sortKey, msg, -1L);
    }
    
    public LogNode log(final String msg, final long elapsedTimeNanos, final Throwable e) {
        return this.addChild("", msg, elapsedTimeNanos).addChild(e);
    }
    
    public LogNode log(final String msg, final long elapsedTimeNanos) {
        return this.addChild("", msg, elapsedTimeNanos);
    }
    
    public LogNode log(final String msg, final Throwable e) {
        return this.addChild("", msg, -1L).addChild(e);
    }
    
    public LogNode log(final String msg) {
        return this.addChild("", msg, -1L);
    }
    
    public LogNode log(final Collection<String> msgs) {
        LogNode last = null;
        for (final String m : msgs) {
            last = this.log(m);
        }
        return last;
    }
    
    public LogNode log(final Throwable e) {
        return this.log("Exception thrown").addChild(e);
    }
    
    public void flush() {
        if (this.parent != null) {
            throw new IllegalArgumentException("Only flush the toplevel LogNode");
        }
        if (!this.children.isEmpty()) {
            final String logOutput = this.toString();
            this.children.clear();
            LogNode.log.info(logOutput);
        }
    }
    
    static {
        log = Logger.getLogger(ClassGraph.class.getName());
        LogNode.sortKeyUniqueSuffix = new AtomicInteger(0);
        dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ", Locale.US);
        nanoFormatter = new DecimalFormat("0.000000");
    }
}
