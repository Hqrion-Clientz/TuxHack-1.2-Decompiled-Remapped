// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.json;

import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.reflect.AccessibleObject;
import java.util.Collection;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.invoke.MethodHandle;

public final class JSONUtils
{
    static final String ID_KEY = "__ID";
    static final String ID_PREFIX = "[#";
    static final String ID_SUFFIX = "]";
    private static final String[] JSON_CHAR_REPLACEMENTS;
    private static MethodHandle isAccessibleMethodHandle;
    private static Method isAccessibleMethod;
    private static MethodHandle trySetAccessibleMethodHandle;
    private static Method trySetAccessibleMethod;
    private static final String[] INDENT_LEVELS;
    
    private JSONUtils() {
    }
    
    static void escapeJSONString(final String unsafeStr, final StringBuilder buf) {
        if (unsafeStr == null) {
            return;
        }
        boolean needsEscaping = false;
        for (int i = 0, n = unsafeStr.length(); i < n; ++i) {
            final char c = unsafeStr.charAt(i);
            if (c > '\u00ff' || JSONUtils.JSON_CHAR_REPLACEMENTS[c] != null) {
                needsEscaping = true;
                break;
            }
        }
        if (!needsEscaping) {
            buf.append(unsafeStr);
            return;
        }
        for (int i = 0, n = unsafeStr.length(); i < n; ++i) {
            final char c = unsafeStr.charAt(i);
            if (c > '\u00ff') {
                buf.append("\\u");
                final int nibble3 = (c & '\uf000') >> 12;
                buf.append((nibble3 <= 9) ? ((char)(48 + nibble3)) : ((char)(65 + nibble3 - 10)));
                final int nibble4 = (c & '\u0f00') >> 8;
                buf.append((nibble4 <= 9) ? ((char)(48 + nibble4)) : ((char)(65 + nibble4 - 10)));
                final int nibble5 = (c & '\u00f0') >> 4;
                buf.append((nibble5 <= 9) ? ((char)(48 + nibble5)) : ((char)(65 + nibble5 - 10)));
                final int nibble6 = c & '\u000f';
                buf.append((nibble6 <= 9) ? ((char)(48 + nibble6)) : ((char)(65 + nibble6 - 10)));
            }
            else {
                final String replacement = JSONUtils.JSON_CHAR_REPLACEMENTS[c];
                if (replacement == null) {
                    buf.append(c);
                }
                else {
                    buf.append(replacement);
                }
            }
        }
    }
    
    public static String escapeJSONString(final String unsafeStr) {
        final StringBuilder buf = new StringBuilder(unsafeStr.length() * 2);
        escapeJSONString(unsafeStr, buf);
        return buf.toString();
    }
    
    static void indent(final int depth, final int indentWidth, final StringBuilder buf) {
        final int maxIndent = JSONUtils.INDENT_LEVELS.length - 1;
        int n;
        for (int d = depth * indentWidth; d > 0; d -= n) {
            n = Math.min(d, maxIndent);
            buf.append(JSONUtils.INDENT_LEVELS[n]);
        }
    }
    
    static Object getFieldValue(final Object containingObj, final Field field) throws IllegalArgumentException, IllegalAccessException {
        final Class<?> fieldType = field.getType();
        if (fieldType == Integer.TYPE) {
            return field.getInt(containingObj);
        }
        if (fieldType == Long.TYPE) {
            return field.getLong(containingObj);
        }
        if (fieldType == Short.TYPE) {
            return field.getShort(containingObj);
        }
        if (fieldType == Double.TYPE) {
            return field.getDouble(containingObj);
        }
        if (fieldType == Float.TYPE) {
            return field.getFloat(containingObj);
        }
        if (fieldType == Boolean.TYPE) {
            return field.getBoolean(containingObj);
        }
        if (fieldType == Byte.TYPE) {
            return field.getByte(containingObj);
        }
        if (fieldType == Character.TYPE) {
            return field.getChar(containingObj);
        }
        if (fieldType == Class.class) {
            return field.get(containingObj);
        }
        return field.get(containingObj);
    }
    
    static boolean isBasicValueType(final Class<?> cls) {
        return cls == String.class || cls == Integer.class || cls == Integer.TYPE || cls == Long.class || cls == Long.TYPE || cls == Short.class || cls == Short.TYPE || cls == Float.class || cls == Float.TYPE || cls == Double.class || cls == Double.TYPE || cls == Byte.class || cls == Byte.TYPE || cls == Character.class || cls == Character.TYPE || cls == Boolean.class || cls == Boolean.TYPE || cls.isEnum() || cls == Class.class;
    }
    
    static boolean isBasicValueType(final Type type) {
        if (type instanceof Class) {
            return isBasicValueType((Class<?>)type);
        }
        return type instanceof ParameterizedType && isBasicValueType(((ParameterizedType)type).getRawType());
    }
    
    static boolean isBasicValueType(final Object obj) {
        return obj == null || obj instanceof String || obj instanceof Integer || obj instanceof Boolean || obj instanceof Long || obj instanceof Float || obj instanceof Double || obj instanceof Short || obj instanceof Byte || obj instanceof Character || obj.getClass().isEnum() || obj instanceof Class;
    }
    
    static boolean isCollectionOrArray(final Object obj) {
        final Class<?> cls = obj.getClass();
        return Collection.class.isAssignableFrom(cls) || cls.isArray();
    }
    
    static Class<?> getRawType(final Type type) {
        if (type instanceof Class) {
            return (Class<?>)type;
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>)((ParameterizedType)type).getRawType();
        }
        throw new IllegalArgumentException("Illegal type: " + type);
    }
    
    static boolean isAccessibleOrMakeAccessible(final AccessibleObject fieldOrConstructor) {
        final AtomicBoolean accessible = new AtomicBoolean(false);
        if (!accessible.get()) {
            if (JSONUtils.isAccessibleMethodHandle != null) {
                try {
                    final Object invokeResult = JSONUtils.isAccessibleMethodHandle.invoke(fieldOrConstructor);
                    accessible.set((boolean)invokeResult);
                }
                catch (Throwable t) {}
            }
            else if (JSONUtils.isAccessibleMethod != null) {
                try {
                    accessible.set((boolean)JSONUtils.isAccessibleMethod.invoke(fieldOrConstructor, new Object[0]));
                }
                catch (Throwable t2) {}
            }
        }
        if (!accessible.get()) {
            if (JSONUtils.trySetAccessibleMethodHandle != null) {
                try {
                    final Object invokeResult = JSONUtils.trySetAccessibleMethodHandle.invoke(fieldOrConstructor);
                    accessible.set((boolean)invokeResult);
                }
                catch (Throwable t3) {}
            }
            else if (JSONUtils.trySetAccessibleMethod != null) {
                try {
                    accessible.set((boolean)JSONUtils.trySetAccessibleMethod.invoke(fieldOrConstructor, new Object[0]));
                }
                catch (Throwable t4) {}
            }
            if (!accessible.get()) {
                try {
                    fieldOrConstructor.setAccessible(true);
                    accessible.set(true);
                }
                catch (Throwable t5) {}
            }
            if (!accessible.get()) {
                AccessController.doPrivileged((PrivilegedAction<Object>)new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        if (JSONUtils.trySetAccessibleMethodHandle != null) {
                            try {
                                final Object invokeResult = JSONUtils.trySetAccessibleMethodHandle.invoke(fieldOrConstructor);
                                accessible.set((boolean)invokeResult);
                            }
                            catch (Throwable t) {}
                        }
                        else if (JSONUtils.trySetAccessibleMethod != null) {
                            try {
                                accessible.set((boolean)JSONUtils.trySetAccessibleMethod.invoke(fieldOrConstructor, new Object[0]));
                            }
                            catch (Throwable t2) {}
                        }
                        if (!accessible.get()) {
                            try {
                                fieldOrConstructor.setAccessible(true);
                                accessible.set(true);
                            }
                            catch (Throwable t3) {}
                        }
                        return null;
                    }
                });
            }
        }
        return accessible.get();
    }
    
    static boolean fieldIsSerializable(final Field field, final boolean onlySerializePublicFields) {
        final int modifiers = field.getModifiers();
        return (!onlySerializePublicFields || Modifier.isPublic(modifiers)) && !Modifier.isTransient(modifiers) && !Modifier.isFinal(modifiers) && (modifiers & 0x1000) == 0x0 && isAccessibleOrMakeAccessible(field);
    }
    
    static {
        JSON_CHAR_REPLACEMENTS = new String[256];
        JSONUtils.isAccessibleMethodHandle = null;
        JSONUtils.isAccessibleMethod = null;
        JSONUtils.trySetAccessibleMethodHandle = null;
        JSONUtils.trySetAccessibleMethod = null;
        for (int c = 0; c < 256; ++c) {
            if (c == 32) {
                c = 127;
            }
            final int nibble1 = c >> 4;
            final char hexDigit1 = (nibble1 <= 9) ? ((char)(48 + nibble1)) : ((char)(65 + nibble1 - 10));
            final int nibble2 = c & 0xF;
            final char hexDigit2 = (nibble2 <= 9) ? ((char)(48 + nibble2)) : ((char)(65 + nibble2 - 10));
            JSONUtils.JSON_CHAR_REPLACEMENTS[c] = "\\u00" + Character.toString(hexDigit1) + Character.toString(hexDigit2);
        }
        JSONUtils.JSON_CHAR_REPLACEMENTS[34] = "\\\"";
        JSONUtils.JSON_CHAR_REPLACEMENTS[92] = "\\\\";
        JSONUtils.JSON_CHAR_REPLACEMENTS[10] = "\\n";
        JSONUtils.JSON_CHAR_REPLACEMENTS[13] = "\\r";
        JSONUtils.JSON_CHAR_REPLACEMENTS[9] = "\\t";
        JSONUtils.JSON_CHAR_REPLACEMENTS[8] = "\\b";
        JSONUtils.JSON_CHAR_REPLACEMENTS[12] = "\\f";
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            JSONUtils.isAccessibleMethodHandle = lookup.findVirtual(AccessibleObject.class, "isAccessible", MethodType.methodType(Boolean.TYPE));
        }
        catch (NoSuchMethodException ex) {}
        catch (IllegalAccessException ex2) {}
        try {
            JSONUtils.isAccessibleMethod = AccessibleObject.class.getDeclaredMethod("isAccessible", Object.class);
        }
        catch (NoSuchMethodException ex3) {}
        catch (SecurityException ex4) {}
        try {
            JSONUtils.trySetAccessibleMethodHandle = lookup.findVirtual(AccessibleObject.class, "trySetAccessible", MethodType.methodType(Boolean.TYPE));
        }
        catch (NoSuchMethodException ex5) {}
        catch (IllegalAccessException ex6) {}
        try {
            JSONUtils.trySetAccessibleMethod = AccessibleObject.class.getDeclaredMethod("trySetAccessible", (Class<?>[])new Class[0]);
        }
        catch (NoSuchMethodException ex7) {}
        catch (SecurityException ex8) {}
        INDENT_LEVELS = new String[17];
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < JSONUtils.INDENT_LEVELS.length; ++i) {
            JSONUtils.INDENT_LEVELS[i] = buf.toString();
            buf.append(' ');
        }
    }
}
