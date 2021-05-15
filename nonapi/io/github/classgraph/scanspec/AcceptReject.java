// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.scanspec;

import nonapi.io.github.classgraph.utils.FileUtils;
import java.util.HashSet;
import nonapi.io.github.classgraph.utils.CollectionUtils;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import nonapi.io.github.classgraph.utils.JarUtils;
import nonapi.io.github.classgraph.utils.FastPathResolver;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Set;

public abstract class AcceptReject
{
    protected Set<String> accept;
    protected Set<String> reject;
    protected Set<String> acceptPrefixesSet;
    protected List<String> acceptPrefixes;
    protected List<String> rejectPrefixes;
    protected Set<String> acceptGlobs;
    protected Set<String> rejectGlobs;
    protected transient List<Pattern> acceptPatterns;
    protected transient List<Pattern> rejectPatterns;
    protected char separatorChar;
    
    public AcceptReject() {
    }
    
    public AcceptReject(final char separatorChar) {
        this.separatorChar = separatorChar;
    }
    
    public abstract void addToAccept(final String p0);
    
    public abstract void addToReject(final String p0);
    
    public abstract boolean isAcceptedAndNotRejected(final String p0);
    
    public abstract boolean isAccepted(final String p0);
    
    public abstract boolean acceptHasPrefix(final String p0);
    
    public abstract boolean isRejected(final String p0);
    
    public static String normalizePath(final String path) {
        String pathResolved;
        for (pathResolved = FastPathResolver.resolve(path); pathResolved.startsWith("/"); pathResolved = pathResolved.substring(1)) {}
        return pathResolved;
    }
    
    public static String normalizePackageOrClassName(final String packageOrClassName) {
        return normalizePath(packageOrClassName.replace('.', '/')).replace('/', '.');
    }
    
    public static String pathToPackageName(final String path) {
        return path.replace('/', '.');
    }
    
    public static String packageNameToPath(final String packageName) {
        return packageName.replace('.', '/');
    }
    
    public static String classNameToClassfilePath(final String className) {
        return JarUtils.classNameToClassfilePath(className);
    }
    
    public static Pattern globToPattern(final String glob) {
        return Pattern.compile("^" + glob.replace(".", "\\.").replace("*", ".*") + "$");
    }
    
    private static boolean matchesPatternList(final String str, final List<Pattern> patterns) {
        if (patterns != null) {
            for (final Pattern pattern : patterns) {
                if (pattern.matcher(str).matches()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean acceptIsEmpty() {
        return this.accept == null && this.acceptPrefixes == null && this.acceptGlobs == null;
    }
    
    public boolean rejectIsEmpty() {
        return this.reject == null && this.rejectPrefixes == null && this.rejectGlobs == null;
    }
    
    public boolean acceptAndRejectAreEmpty() {
        return this.acceptIsEmpty() && this.rejectIsEmpty();
    }
    
    public boolean isSpecificallyAcceptedAndNotRejected(final String str) {
        return !this.acceptIsEmpty() && this.isAcceptedAndNotRejected(str);
    }
    
    public boolean isSpecificallyAccepted(final String str) {
        return !this.acceptIsEmpty() && this.isAccepted(str);
    }
    
    void sortPrefixes() {
        if (this.acceptPrefixesSet != null) {
            this.acceptPrefixes = new ArrayList<String>(this.acceptPrefixesSet);
        }
        if (this.acceptPrefixes != null) {
            CollectionUtils.sortIfNotEmpty(this.acceptPrefixes);
        }
        if (this.rejectPrefixes != null) {
            CollectionUtils.sortIfNotEmpty(this.rejectPrefixes);
        }
    }
    
    private static void quoteList(final Collection<String> coll, final StringBuilder buf) {
        buf.append('[');
        boolean first = true;
        for (final String item : coll) {
            if (first) {
                first = false;
            }
            else {
                buf.append(", ");
            }
            buf.append('\"');
            for (int i = 0; i < item.length(); ++i) {
                final char c = item.charAt(i);
                if (c == '\"') {
                    buf.append("\\\"");
                }
                else {
                    buf.append(c);
                }
            }
            buf.append('\"');
        }
        buf.append(']');
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        if (this.accept != null) {
            buf.append("accept: ");
            quoteList(this.accept, buf);
        }
        if (this.acceptPrefixes != null) {
            if (buf.length() > 0) {
                buf.append("; ");
            }
            buf.append("acceptPrefixes: ");
            quoteList(this.acceptPrefixes, buf);
        }
        if (this.acceptGlobs != null) {
            if (buf.length() > 0) {
                buf.append("; ");
            }
            buf.append("acceptGlobs: ");
            quoteList(this.acceptGlobs, buf);
        }
        if (this.reject != null) {
            if (buf.length() > 0) {
                buf.append("; ");
            }
            buf.append("reject: ");
            quoteList(this.reject, buf);
        }
        if (this.rejectPrefixes != null) {
            if (buf.length() > 0) {
                buf.append("; ");
            }
            buf.append("rejectPrefixes: ");
            quoteList(this.rejectPrefixes, buf);
        }
        if (this.rejectGlobs != null) {
            if (buf.length() > 0) {
                buf.append("; ");
            }
            buf.append("rejectGlobs: ");
            quoteList(this.rejectGlobs, buf);
        }
        return buf.toString();
    }
    
    public static class AcceptRejectPrefix extends AcceptReject
    {
        public AcceptRejectPrefix() {
        }
        
        public AcceptRejectPrefix(final char separatorChar) {
            super(separatorChar);
        }
        
        @Override
        public void addToAccept(final String str) {
            if (str.contains("*")) {
                throw new IllegalArgumentException("Cannot use a glob wildcard here: " + str);
            }
            if (this.acceptPrefixesSet == null) {
                this.acceptPrefixesSet = new HashSet<String>();
            }
            this.acceptPrefixesSet.add(str);
        }
        
        @Override
        public void addToReject(final String str) {
            if (str.contains("*")) {
                throw new IllegalArgumentException("Cannot use a glob wildcard here: " + str);
            }
            if (this.rejectPrefixes == null) {
                this.rejectPrefixes = new ArrayList<String>();
            }
            this.rejectPrefixes.add(str);
        }
        
        @Override
        public boolean isAcceptedAndNotRejected(final String str) {
            boolean isAccepted = this.acceptPrefixes == null;
            if (!isAccepted) {
                for (final String prefix : this.acceptPrefixes) {
                    if (str.startsWith(prefix)) {
                        isAccepted = true;
                        break;
                    }
                }
            }
            if (!isAccepted) {
                return false;
            }
            if (this.rejectPrefixes != null) {
                for (final String prefix : this.rejectPrefixes) {
                    if (str.startsWith(prefix)) {
                        return false;
                    }
                }
            }
            return true;
        }
        
        @Override
        public boolean isAccepted(final String str) {
            boolean isAccepted = this.acceptPrefixes == null;
            if (!isAccepted) {
                for (final String prefix : this.acceptPrefixes) {
                    if (str.startsWith(prefix)) {
                        isAccepted = true;
                        break;
                    }
                }
            }
            return isAccepted;
        }
        
        @Override
        public boolean acceptHasPrefix(final String str) {
            throw new IllegalArgumentException("Can only find prefixes of whole strings");
        }
        
        @Override
        public boolean isRejected(final String str) {
            if (this.rejectPrefixes != null) {
                for (final String prefix : this.rejectPrefixes) {
                    if (str.startsWith(prefix)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    public static class AcceptRejectWholeString extends AcceptReject
    {
        public AcceptRejectWholeString() {
        }
        
        public AcceptRejectWholeString(final char separatorChar) {
            super(separatorChar);
        }
        
        @Override
        public void addToAccept(final String str) {
            if (str.contains("*")) {
                if (this.acceptGlobs == null) {
                    this.acceptGlobs = new HashSet<String>();
                    this.acceptPatterns = new ArrayList<Pattern>();
                }
                this.acceptGlobs.add(str);
                this.acceptPatterns.add(AcceptReject.globToPattern(str));
            }
            else {
                if (this.accept == null) {
                    this.accept = new HashSet<String>();
                }
                this.accept.add(str);
            }
            if (this.acceptPrefixesSet == null) {
                (this.acceptPrefixesSet = new HashSet<String>()).add("");
                this.acceptPrefixesSet.add("/");
            }
            final String separator = Character.toString(this.separatorChar);
            String prefix = str;
            if (prefix.contains("*")) {
                prefix = prefix.substring(0, prefix.indexOf(42));
                final int sepIdx = prefix.lastIndexOf(this.separatorChar);
                if (sepIdx < 0) {
                    prefix = "";
                }
                else {
                    prefix = prefix.substring(0, prefix.lastIndexOf(this.separatorChar));
                }
            }
            while (prefix.endsWith(separator)) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
            while (!prefix.isEmpty()) {
                this.acceptPrefixesSet.add(prefix + this.separatorChar);
                prefix = FileUtils.getParentDirPath(prefix, this.separatorChar);
            }
        }
        
        @Override
        public void addToReject(final String str) {
            if (str.contains("*")) {
                if (this.rejectGlobs == null) {
                    this.rejectGlobs = new HashSet<String>();
                    this.rejectPatterns = new ArrayList<Pattern>();
                }
                this.rejectGlobs.add(str);
                this.rejectPatterns.add(AcceptReject.globToPattern(str));
            }
            else {
                if (this.reject == null) {
                    this.reject = new HashSet<String>();
                }
                this.reject.add(str);
            }
        }
        
        @Override
        public boolean isAcceptedAndNotRejected(final String str) {
            return this.isAccepted(str) && !this.isRejected(str);
        }
        
        @Override
        public boolean isAccepted(final String str) {
            return (this.accept == null && this.acceptPatterns == null) || (this.accept != null && this.accept.contains(str)) || matchesPatternList(str, this.acceptPatterns);
        }
        
        @Override
        public boolean acceptHasPrefix(final String str) {
            return this.acceptPrefixesSet != null && this.acceptPrefixesSet.contains(str);
        }
        
        @Override
        public boolean isRejected(final String str) {
            return (this.reject != null && this.reject.contains(str)) || matchesPatternList(str, this.rejectPatterns);
        }
    }
    
    public static class AcceptRejectLeafname extends AcceptRejectWholeString
    {
        public AcceptRejectLeafname() {
        }
        
        public AcceptRejectLeafname(final char separatorChar) {
            super(separatorChar);
        }
        
        @Override
        public void addToAccept(final String str) {
            super.addToAccept(JarUtils.leafName(str));
        }
        
        @Override
        public void addToReject(final String str) {
            super.addToReject(JarUtils.leafName(str));
        }
        
        @Override
        public boolean isAcceptedAndNotRejected(final String str) {
            return super.isAcceptedAndNotRejected(JarUtils.leafName(str));
        }
        
        @Override
        public boolean isAccepted(final String str) {
            return super.isAccepted(JarUtils.leafName(str));
        }
        
        @Override
        public boolean acceptHasPrefix(final String str) {
            throw new IllegalArgumentException("Can only find prefixes of whole strings");
        }
        
        @Override
        public boolean isRejected(final String str) {
            return super.isRejected(JarUtils.leafName(str));
        }
    }
}
