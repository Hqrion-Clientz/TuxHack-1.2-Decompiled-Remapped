// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classpath;

import java.security.AccessController;
import java.security.PrivilegedAction;
import nonapi.io.github.classgraph.utils.VersionFinder;
import nonapi.io.github.classgraph.utils.LogNode;
import java.lang.reflect.Proxy;
import java.util.List;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;

class CallStackReader
{
    private static Class<?>[] callStack;
    
    private CallStackReader() {
    }
    
    private static Class<?>[] getCallStackViaStackWalker() {
        try {
            final Class<?> consumerClass = Class.forName("java.util.function.Consumer");
            final List<Class<?>> stackFrameClasses = new ArrayList<Class<?>>();
            final Class<?> stackWalkerOptionClass = Class.forName("java.lang.StackWalker$Option");
            final Object retainClassReference = Class.forName("java.lang.Enum").getMethod("valueOf", Class.class, String.class).invoke(null, stackWalkerOptionClass, "RETAIN_CLASS_REFERENCE");
            final Class<?> stackWalkerClass = Class.forName("java.lang.StackWalker");
            final Object stackWalkerInstance = stackWalkerClass.getMethod("getInstance", stackWalkerOptionClass).invoke(null, retainClassReference);
            final Method stackFrameGetDeclaringClassMethod = Class.forName("java.lang.StackWalker$StackFrame").getMethod("getDeclaringClass", (Class<?>[])new Class[0]);
            stackWalkerClass.getMethod("forEach", consumerClass).invoke(stackWalkerInstance, Proxy.newProxyInstance(consumerClass.getClassLoader(), new Class[] { consumerClass }, new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    final Class<?> declaringClass = (Class<?>)stackFrameGetDeclaringClassMethod.invoke(args[0], new Object[0]);
                    stackFrameClasses.add(declaringClass);
                    return null;
                }
            }));
            return stackFrameClasses.toArray(new Class[0]);
        }
        catch (Exception | LinkageError ex) {
            final Throwable t;
            final Throwable e = t;
            return null;
        }
    }
    
    private static Class<?>[] getCallStackViaSecurityManager(final LogNode log) {
        try {
            return new CallerResolver().getClassContext();
        }
        catch (SecurityException e) {
            if (log != null) {
                log.log("Exception while trying to obtain call stack via SecurityManager", e);
            }
            return null;
        }
    }
    
    static Class<?>[] getClassContext(final LogNode log) {
        if (CallStackReader.callStack == null) {
            if ((VersionFinder.JAVA_MAJOR_VERSION == 11 && (VersionFinder.JAVA_MINOR_VERSION >= 1 || VersionFinder.JAVA_SUB_VERSION >= 4) && !VersionFinder.JAVA_IS_EA_VERSION) || (VersionFinder.JAVA_MAJOR_VERSION == 12 && (VersionFinder.JAVA_MINOR_VERSION >= 1 || VersionFinder.JAVA_SUB_VERSION >= 2) && !VersionFinder.JAVA_IS_EA_VERSION) || (VersionFinder.JAVA_MAJOR_VERSION == 13 && !VersionFinder.JAVA_IS_EA_VERSION) || VersionFinder.JAVA_MAJOR_VERSION > 13) {
                CallStackReader.callStack = (Class<?>[])AccessController.doPrivileged((PrivilegedAction<Class[]>)new PrivilegedAction<Class<?>[]>() {
                    @Override
                    public Class<?>[] run() {
                        return getCallStackViaStackWalker();
                    }
                });
            }
            if (CallStackReader.callStack == null || CallStackReader.callStack.length == 0) {
                CallStackReader.callStack = (Class<?>[])AccessController.doPrivileged((PrivilegedAction<Class[]>)new PrivilegedAction<Class<?>[]>() {
                    @Override
                    public Class<?>[] run() {
                        return getCallStackViaSecurityManager(log);
                    }
                });
            }
            if (CallStackReader.callStack == null || CallStackReader.callStack.length == 0) {
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                Label_0173: {
                    if (stackTrace != null) {
                        if (stackTrace.length != 0) {
                            break Label_0173;
                        }
                    }
                    try {
                        throw new Exception();
                    }
                    catch (Exception e) {
                        stackTrace = e.getStackTrace();
                    }
                }
                final List<Class<?>> stackClassesList = new ArrayList<Class<?>>();
                for (final StackTraceElement elt : stackTrace) {
                    try {
                        stackClassesList.add(Class.forName(elt.getClassName()));
                    }
                    catch (ClassNotFoundException ex) {}
                    catch (LinkageError linkageError) {}
                }
                if (!stackClassesList.isEmpty()) {
                    CallStackReader.callStack = stackClassesList.toArray(new Class[0]);
                }
                else {
                    CallStackReader.callStack = (Class<?>[])new Class[] { CallStackReader.class };
                }
            }
        }
        return CallStackReader.callStack;
    }
    
    private static final class CallerResolver extends SecurityManager
    {
        @Override
        protected Class<?>[] getClassContext() {
            return super.getClassContext();
        }
    }
}
