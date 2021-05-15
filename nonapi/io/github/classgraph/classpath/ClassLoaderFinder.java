// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classpath;

import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedHashSet;
import nonapi.io.github.classgraph.utils.LogNode;
import nonapi.io.github.classgraph.scanspec.ScanSpec;

public class ClassLoaderFinder
{
    private final ClassLoader[] contextClassLoaders;
    
    public ClassLoader[] getContextClassLoaders() {
        return this.contextClassLoaders;
    }
    
    ClassLoaderFinder(final ScanSpec scanSpec, final LogNode log) {
        LinkedHashSet<ClassLoader> classLoadersUnique;
        LogNode classLoadersFoundLog;
        if (scanSpec.overrideClassLoaders == null) {
            classLoadersUnique = new LinkedHashSet<ClassLoader>();
            final ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
            if (threadClassLoader != null) {
                classLoadersUnique.add(threadClassLoader);
            }
            final ClassLoader currClassClassLoader = this.getClass().getClassLoader();
            if (currClassClassLoader != null) {
                classLoadersUnique.add(currClassClassLoader);
            }
            final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            if (systemClassLoader != null) {
                classLoadersUnique.add(systemClassLoader);
            }
            try {
                final Class<?>[] callStack = CallStackReader.getClassContext(log);
                for (int i = callStack.length - 1; i >= 0; --i) {
                    final ClassLoader callerClassLoader = callStack[i].getClassLoader();
                    if (callerClassLoader != null) {
                        classLoadersUnique.add(callerClassLoader);
                    }
                }
            }
            catch (IllegalArgumentException e) {
                if (log != null) {
                    log.log("Could not get call stack", e);
                }
            }
            if (scanSpec.addedClassLoaders != null) {
                classLoadersUnique.addAll((Collection<?>)scanSpec.addedClassLoaders);
            }
            classLoadersFoundLog = ((log == null) ? null : log.log("Found ClassLoaders:"));
        }
        else {
            classLoadersUnique = new LinkedHashSet<ClassLoader>(scanSpec.overrideClassLoaders);
            classLoadersFoundLog = ((log == null) ? null : log.log("Override ClassLoaders:"));
        }
        if (classLoadersFoundLog != null) {
            for (final ClassLoader classLoader : classLoadersUnique) {
                classLoadersFoundLog.log(classLoader.getClass().getName());
            }
        }
        this.contextClassLoaders = classLoadersUnique.toArray(new ClassLoader[0]);
    }
}
