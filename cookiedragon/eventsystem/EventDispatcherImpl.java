// 
// Decompiled by Procyon v0.5.36
// 

package cookiedragon.eventsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import kotlin.jvm.internal.DefaultConstructorMarker;
import java.util.HashSet;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import java.util.Set;
import java.util.Map;
import java.lang.invoke.MethodHandles;
import kotlin.Metadata;

@Metadata(mv = { 1, 1, 16 }, bv = { 1, 0, 3 }, k = 1, d1 = { "\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010%\n\u0002\u0018\u0002\n\u0002\u0010#\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u00c0\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u001f\u0010\u000b\u001a\u0002H\f\"\b\b\u0000\u0010\f*\u00020\r2\u0006\u0010\u000e\u001a\u0002H\fH\u0016¢\u0006\u0002\u0010\u000fJ\u0014\u0010\u0010\u001a\u00020\u00112\n\u0010\u0012\u001a\u0006\u0012\u0002\b\u00030\bH\u0016J\u001e\u0010\u0010\u001a\u00020\u00112\n\u0010\u0013\u001a\u0006\u0012\u0002\b\u00030\b2\b\u0010\u0014\u001a\u0004\u0018\u00010\rH\u0002J\u0010\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\rH\u0016J\u001c\u0010\u0015\u001a\u00020\u00112\n\u0010\u0012\u001a\u0006\u0012\u0002\b\u00030\b2\u0006\u0010\u0016\u001a\u00020\u0017H\u0002J\u001a\u0010\u0015\u001a\u00020\u00112\b\u0010\u0014\u001a\u0004\u0018\u00010\r2\u0006\u0010\u0016\u001a\u00020\u0017H\u0002J\u0014\u0010\u0018\u001a\u00020\u00112\n\u0010\u0012\u001a\u0006\u0012\u0002\b\u00030\bH\u0016J\u0010\u0010\u0018\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\rH\u0016J\u0014\u0010\u0019\u001a\u00020\u00112\n\u0010\u0012\u001a\u0006\u0012\u0002\b\u00030\bH\u0016J\u0010\u0010\u0019\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\rH\u0016R\u0016\u0010\u0003\u001a\n \u0005*\u0004\u0018\u00010\u00040\u0004X\u0082\u0004¢\u0006\u0002\n\u0000R(\u0010\u0006\u001a\u001c\u0012\b\u0012\u0006\u0012\u0002\b\u00030\b\u0012\u000e\u0012\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\n0\t0\u0007X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u001a" }, d2 = { "Lcookiedragon/eventsystem/EventDispatcherImpl;", "Lcookiedragon/eventsystem/EventDispatcher;", "()V", "lookup", "Ljava/lang/invoke/MethodHandles$Lookup;", "kotlin.jvm.PlatformType", "subscriptions", "", "Ljava/lang/Class;", "", "Lcookiedragon/eventsystem/SubscribingMethod;", "dispatch", "T", "", "event", "(Ljava/lang/Object;)Ljava/lang/Object;", "register", "", "subscriber", "clazz", "instance", "setActive", "active", "", "subscribe", "unsubscribe", "EventSystem" })
public final class EventDispatcherImpl implements EventDispatcher
{
    private static final MethodHandles.Lookup lookup;
    private static final Map<Class<?>, Set<SubscribingMethod<?>>> subscriptions;
    public static final EventDispatcherImpl INSTANCE;
    
    @NotNull
    @Override
    public <T> T dispatch(@NotNull final T event) {
        Intrinsics.checkParameterIsNotNull((Object)event, "event");
        Class clazz = event.getClass();
        while (true) {
            final Set<SubscribingMethod<?>> set = EventDispatcherImpl.subscriptions.get(clazz);
            if (set != null) {
                final Set methods = set;
                final int n = 0;
                for (final SubscribingMethod method : methods) {
                    if (method.getActive()) {
                        method.invoke(event);
                    }
                }
            }
            if (Intrinsics.areEqual((Object)clazz, (Object)Object.class)) {
                break;
            }
            final Class superclass = clazz.getSuperclass();
            Intrinsics.checkExpressionValueIsNotNull((Object)superclass, "clazz.superclass");
            clazz = superclass;
        }
        return event;
    }
    
    @Override
    public void register(@NotNull final Class<?> subscriber) {
        Intrinsics.checkParameterIsNotNull((Object)subscriber, "subscriber");
        this.register(subscriber, null);
    }
    
    @Override
    public void register(@NotNull final Object subscriber) {
        Intrinsics.checkParameterIsNotNull(subscriber, "subscriber");
        this.register(subscriber.getClass(), subscriber);
    }
    
    private final void register(final Class<?> clazz, final Object instance) {
        for (final Method method : clazz.getDeclaredMethods()) {
            Label_0322: {
                if (instance == null) {
                    final Method $this$access_u24isStatic = method;
                    Intrinsics.checkExpressionValueIsNotNull((Object)$this$access_u24isStatic, "method");
                    if (!EventDispatcherImplKt.access$isStatic($this$access_u24isStatic)) {
                        break Label_0322;
                    }
                }
                if (instance != null) {
                    final Method $this$access_u24isStatic2 = method;
                    Intrinsics.checkExpressionValueIsNotNull((Object)$this$access_u24isStatic2, "method");
                    if (EventDispatcherImplKt.access$isStatic($this$access_u24isStatic2)) {
                        break Label_0322;
                    }
                }
                if (method.isAnnotationPresent(Subscriber.class)) {
                    final Method method2 = method;
                    Intrinsics.checkExpressionValueIsNotNull((Object)method2, "method");
                    if (Intrinsics.areEqual((Object)method2.getReturnType(), (Object)Void.TYPE) ^ true) {
                        new IllegalArgumentException("Subscriber " + clazz + '.' + method.getName() + " cannot return type").printStackTrace();
                    }
                    else if (method.getParameterCount() != 1) {
                        new IllegalArgumentException("Expected only 1 parameter for " + clazz + '.' + method.getName()).printStackTrace();
                    }
                    else {
                        method.setAccessible(true);
                        final Class<?> clazz2 = method.getParameterTypes()[0];
                        if (clazz2 == null) {
                            Intrinsics.throwNpe();
                        }
                        final Class eventType = clazz2;
                        final MethodHandle methodHandle = EventDispatcherImpl.lookup.unreflect(method);
                        final Map $this$getOrPut$iv = EventDispatcherImpl.subscriptions;
                        final int $i$f$getOrPut = 0;
                        final Object value$iv = $this$getOrPut$iv.get(eventType);
                        Object o;
                        if (value$iv == null) {
                            final int n = 0;
                            final Object answer$iv = new HashSet();
                            $this$getOrPut$iv.put(eventType, answer$iv);
                            o = answer$iv;
                        }
                        else {
                            o = value$iv;
                        }
                        final Set<Object> set = (Set<Object>)o;
                        final boolean access$isStatic = EventDispatcherImplKt.access$isStatic(method);
                        final MethodHandle methodHandle2 = methodHandle;
                        Intrinsics.checkExpressionValueIsNotNull((Object)methodHandle2, "methodHandle");
                        set.add(new SubscribingMethod(clazz, instance, access$isStatic, methodHandle2, false, 16, null));
                    }
                }
            }
        }
    }
    
    @Override
    public void subscribe(@NotNull final Class<?> subscriber) {
        Intrinsics.checkParameterIsNotNull((Object)subscriber, "subscriber");
        this.setActive(subscriber, true);
    }
    
    @Override
    public void subscribe(@NotNull final Object subscriber) {
        Intrinsics.checkParameterIsNotNull(subscriber, "subscriber");
        this.setActive(subscriber, true);
    }
    
    @Override
    public void unsubscribe(@NotNull final Class<?> subscriber) {
        Intrinsics.checkParameterIsNotNull((Object)subscriber, "subscriber");
        this.setActive(subscriber, false);
    }
    
    @Override
    public void unsubscribe(@NotNull final Object subscriber) {
        Intrinsics.checkParameterIsNotNull(subscriber, "subscriber");
        this.setActive(subscriber, false);
    }
    
    private final void setActive(final Object instance, final boolean active) {
        for (final Set methods : EventDispatcherImpl.subscriptions.values()) {
            for (final SubscribingMethod method : methods) {
                if (Intrinsics.areEqual(method.getInstance(), instance)) {
                    method.setActive(active);
                }
            }
        }
    }
    
    private final void setActive(final Class<?> subscriber, final boolean active) {
        for (final Set methods : EventDispatcherImpl.subscriptions.values()) {
            for (final SubscribingMethod method : methods) {
                if (Intrinsics.areEqual((Object)method.getClazz(), (Object)subscriber)) {
                    method.setActive(active);
                }
            }
        }
    }
    
    private EventDispatcherImpl() {
    }
    
    static {
        INSTANCE = new EventDispatcherImpl();
        lookup = MethodHandles.lookup();
        subscriptions = new ConcurrentHashMap<Class<?>, Set<SubscribingMethod<?>>>();
    }
}
