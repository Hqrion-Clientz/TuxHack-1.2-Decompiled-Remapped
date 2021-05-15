// 
// Decompiled by Procyon v0.5.36
// 

package cookiedragon.eventsystem;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import kotlin.Metadata;

@Metadata(mv = { 1, 1, 16 }, bv = { 1, 0, 3 }, k = 1, d1 = { "\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\bf\u0018\u0000 \f2\u00020\u0001:\u0001\fJ\u001f\u0010\u0002\u001a\u0002H\u0003\"\b\b\u0000\u0010\u0003*\u00020\u00012\u0006\u0010\u0004\u001a\u0002H\u0003H&¢\u0006\u0002\u0010\u0005J\u0014\u0010\u0006\u001a\u00020\u00072\n\u0010\b\u001a\u0006\u0012\u0002\b\u00030\tH&J\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0001H&J\u0014\u0010\n\u001a\u00020\u00072\n\u0010\b\u001a\u0006\u0012\u0002\b\u00030\tH&J\u0010\u0010\n\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0001H&J\u0014\u0010\u000b\u001a\u00020\u00072\n\u0010\b\u001a\u0006\u0012\u0002\b\u00030\tH&J\u0010\u0010\u000b\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0001H&¨\u0006\r" }, d2 = { "Lcookiedragon/eventsystem/EventDispatcher;", "", "dispatch", "T", "event", "(Ljava/lang/Object;)Ljava/lang/Object;", "register", "", "subscriber", "Ljava/lang/Class;", "subscribe", "unsubscribe", "Companion", "EventSystem" })
public interface EventDispatcher
{
    public static final Companion Companion = EventDispatcher.Companion.$$INSTANCE;
    
    @NotNull
     <T> T dispatch(@NotNull final T p0);
    
    void register(@NotNull final Class<?> p0);
    
    void register(@NotNull final Object p0);
    
    void subscribe(@NotNull final Class<?> p0);
    
    void subscribe(@NotNull final Object p0);
    
    void unsubscribe(@NotNull final Class<?> p0);
    
    void unsubscribe(@NotNull final Object p0);
    
    @Metadata(mv = { 1, 1, 16 }, bv = { 1, 0, 3 }, k = 1, d1 = { "\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u001f\u0010\u0003\u001a\u0002H\u0004\"\b\b\u0000\u0010\u0004*\u00020\u00052\u0006\u0010\u0006\u001a\u0002H\u0004H\u0016¢\u0006\u0002\u0010\u0007J\u0014\u0010\b\u001a\u00020\t2\n\u0010\n\u001a\u0006\u0012\u0002\b\u00030\u000bH\u0016J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0005H\u0016J\u0014\u0010\f\u001a\u00020\t2\n\u0010\n\u001a\u0006\u0012\u0002\b\u00030\u000bH\u0016J\u0010\u0010\f\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0005H\u0016J\u0014\u0010\r\u001a\u00020\t2\n\u0010\n\u001a\u0006\u0012\u0002\b\u00030\u000bH\u0016J\u0010\u0010\r\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0005H\u0016¨\u0006\u000e" }, d2 = { "Lcookiedragon/eventsystem/EventDispatcher$Companion;", "Lcookiedragon/eventsystem/EventDispatcher;", "()V", "dispatch", "T", "", "event", "(Ljava/lang/Object;)Ljava/lang/Object;", "register", "", "subscriber", "Ljava/lang/Class;", "subscribe", "unsubscribe", "EventSystem" })
    public static final class Companion implements EventDispatcher
    {
        static final /* synthetic */ Companion $$INSTANCE;
        
        @NotNull
        @Override
        public <T> T dispatch(@NotNull final T event) {
            Intrinsics.checkParameterIsNotNull((Object)event, "event");
            return EventDispatcherImpl.INSTANCE.dispatch(event);
        }
        
        @Override
        public void register(@NotNull final Class<?> subscriber) {
            Intrinsics.checkParameterIsNotNull((Object)subscriber, "subscriber");
            EventDispatcherImpl.INSTANCE.register(subscriber);
        }
        
        @Override
        public void register(@NotNull final Object subscriber) {
            Intrinsics.checkParameterIsNotNull(subscriber, "subscriber");
            EventDispatcherImpl.INSTANCE.register(subscriber);
        }
        
        @Override
        public void subscribe(@NotNull final Class<?> subscriber) {
            Intrinsics.checkParameterIsNotNull((Object)subscriber, "subscriber");
            EventDispatcherImpl.INSTANCE.subscribe(subscriber);
        }
        
        @Override
        public void subscribe(@NotNull final Object subscriber) {
            Intrinsics.checkParameterIsNotNull(subscriber, "subscriber");
            EventDispatcherImpl.INSTANCE.subscribe(subscriber);
        }
        
        @Override
        public void unsubscribe(@NotNull final Class<?> subscriber) {
            Intrinsics.checkParameterIsNotNull((Object)subscriber, "subscriber");
            EventDispatcherImpl.INSTANCE.unsubscribe(subscriber);
        }
        
        @Override
        public void unsubscribe(@NotNull final Object subscriber) {
            Intrinsics.checkParameterIsNotNull(subscriber, "subscriber");
            EventDispatcherImpl.INSTANCE.unsubscribe(subscriber);
        }
        
        private Companion() {
        }
        
        static {
            $$INSTANCE = new Companion();
        }
    }
}
