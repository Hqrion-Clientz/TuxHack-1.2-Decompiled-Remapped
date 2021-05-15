// 
// Decompiled by Procyon v0.5.36
// 

package cookiedragon.eventsystem;

import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import java.lang.invoke.MethodHandle;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import kotlin.Metadata;

@Metadata(mv = { 1, 1, 16 }, bv = { 1, 0, 3 }, k = 1, d1 = { "\u00006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0018\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u0000*\b\b\u0000\u0010\u0001*\u00020\u00022\u00020\u0002B5\u0012\n\u0010\u0003\u001a\u0006\u0012\u0002\b\u00030\u0004\u0012\b\u0010\u0005\u001a\u0004\u0018\u00018\u0000\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u0007¢\u0006\u0002\u0010\u000bJ\r\u0010\u0018\u001a\u0006\u0012\u0002\b\u00030\u0004H\u00c6\u0003J\u0010\u0010\u0019\u001a\u0004\u0018\u00018\u0000H\u00c6\u0003¢\u0006\u0002\u0010\u0013J\t\u0010\u001a\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\tH\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0007H\u00c6\u0003JL\u0010\u001d\u001a\b\u0012\u0004\u0012\u00028\u00000\u00002\f\b\u0002\u0010\u0003\u001a\u0006\u0012\u0002\b\u00030\u00042\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00018\u00002\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u0007H\u00c6\u0001¢\u0006\u0002\u0010\u001eJ\u0013\u0010\u001f\u001a\u00020\u00072\b\u0010 \u001a\u0004\u0018\u00010\u0002H\u00d6\u0003J\t\u0010!\u001a\u00020\"H\u00d6\u0001J\u001d\u0010#\u001a\u00020$\"\b\b\u0001\u0010%*\u00020\u00022\u0006\u0010&\u001a\u0002H%¢\u0006\u0002\u0010'J\t\u0010(\u001a\u00020)H\u00d6\u0001R\u001a\u0010\n\u001a\u00020\u0007X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fR\u0015\u0010\u0003\u001a\u0006\u0012\u0002\b\u00030\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0015\u0010\u0005\u001a\u0004\u0018\u00018\u0000¢\u0006\n\n\u0002\u0010\u0014\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\b\u001a\u00020\t¢\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\u0006\u001a\u00020\u0007¢\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\r¨\u0006*" }, d2 = { "Lcookiedragon/eventsystem/SubscribingMethod;", "T", "", "clazz", "Ljava/lang/Class;", "instance", "static", "", "method", "Ljava/lang/invoke/MethodHandle;", "active", "(Ljava/lang/Class;Ljava/lang/Object;ZLjava/lang/invoke/MethodHandle;Z)V", "getActive", "()Z", "setActive", "(Z)V", "getClazz", "()Ljava/lang/Class;", "getInstance", "()Ljava/lang/Object;", "Ljava/lang/Object;", "getMethod", "()Ljava/lang/invoke/MethodHandle;", "getStatic", "component1", "component2", "component3", "component4", "component5", "copy", "(Ljava/lang/Class;Ljava/lang/Object;ZLjava/lang/invoke/MethodHandle;Z)Lcookiedragon/eventsystem/SubscribingMethod;", "equals", "other", "hashCode", "", "invoke", "", "E", "event", "(Ljava/lang/Object;)V", "toString", "", "EventSystem" })
public final class SubscribingMethod<T>
{
    @NotNull
    private final Class<?> clazz;
    @Nullable
    private final T instance;
    private final boolean static;
    @NotNull
    private final MethodHandle method;
    private boolean active;
    
    public final <E> void invoke(@NotNull final E event) throws Throwable {
        Intrinsics.checkParameterIsNotNull((Object)event, "event");
        if (this.static) {
            this.method.invoke((Object)event);
        }
        else {
            this.method.invoke((Object)this.instance, (Object)event);
        }
    }
    
    @NotNull
    public final Class<?> getClazz() {
        return this.clazz;
    }
    
    @Nullable
    public final T getInstance() {
        return this.instance;
    }
    
    public final boolean getStatic() {
        return this.static;
    }
    
    @NotNull
    public final MethodHandle getMethod() {
        return this.method;
    }
    
    public final boolean getActive() {
        return this.active;
    }
    
    public final void setActive(final boolean <set-?>) {
        this.active = <set-?>;
    }
    
    public SubscribingMethod(@NotNull final Class<?> clazz, @Nullable final T instance, final boolean static, @NotNull final MethodHandle method, final boolean active) {
        Intrinsics.checkParameterIsNotNull((Object)clazz, "clazz");
        Intrinsics.checkParameterIsNotNull((Object)method, "method");
        this.clazz = clazz;
        this.instance = instance;
        this.static = static;
        this.method = method;
        this.active = active;
    }
    
    @NotNull
    public final Class<?> component1() {
        return this.clazz;
    }
    
    @Nullable
    public final T component2() {
        return this.instance;
    }
    
    public final boolean component3() {
        return this.static;
    }
    
    @NotNull
    public final MethodHandle component4() {
        return this.method;
    }
    
    public final boolean component5() {
        return this.active;
    }
    
    @NotNull
    public final SubscribingMethod<T> copy(@NotNull final Class<?> clazz, @Nullable final T instance, final boolean static, @NotNull final MethodHandle method, final boolean active) {
        Intrinsics.checkParameterIsNotNull((Object)clazz, "clazz");
        Intrinsics.checkParameterIsNotNull((Object)method, "method");
        return new SubscribingMethod<T>(clazz, instance, static, method, active);
    }
    
    @NotNull
    @Override
    public String toString() {
        return "SubscribingMethod(clazz=" + this.clazz + ", instance=" + this.instance + ", static=" + this.static + ", method=" + this.method + ", active=" + this.active + ")";
    }
    
    @Override
    public int hashCode() {
        final Class<?> clazz = this.clazz;
        final int n = ((clazz != null) ? clazz.hashCode() : 0) * 31;
        final T instance = this.instance;
        final int n2 = (n + ((instance != null) ? instance.hashCode() : 0)) * 31;
        int static1;
        if ((static1 = (this.static ? 1 : 0)) != 0) {
            static1 = 1;
        }
        final int n3 = (n2 + static1) * 31;
        final MethodHandle method = this.method;
        final int n4 = (n3 + ((method != null) ? method.hashCode() : 0)) * 31;
        int active;
        if ((active = (this.active ? 1 : 0)) != 0) {
            active = 1;
        }
        return n4 + active;
    }
    
    @Override
    public boolean equals(@Nullable final Object o) {
        if (this != o) {
            if (o instanceof SubscribingMethod) {
                final SubscribingMethod subscribingMethod = (SubscribingMethod)o;
                if (Intrinsics.areEqual((Object)this.clazz, (Object)subscribingMethod.clazz) && Intrinsics.areEqual((Object)this.instance, (Object)subscribingMethod.instance) && this.static == subscribingMethod.static && Intrinsics.areEqual((Object)this.method, (Object)subscribingMethod.method) && this.active == subscribingMethod.active) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
