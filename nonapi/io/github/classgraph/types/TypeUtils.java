// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.types;

public final class TypeUtils
{
    private TypeUtils() {
    }
    
    public static boolean getIdentifierToken(final Parser parser, final char separator, final char separatorReplace) {
        boolean consumedChar = false;
        while (parser.hasMore()) {
            final char c = parser.peek();
            if (c == separator) {
                parser.appendToToken(separatorReplace);
                parser.next();
                consumedChar = true;
            }
            else {
                if (c == ';' || c == '[' || c == '<' || c == '>' || c == ':' || c == '/' || c == '.') {
                    break;
                }
                parser.appendToToken(c);
                parser.next();
                consumedChar = true;
            }
        }
        return consumedChar;
    }
    
    public static boolean getIdentifierToken(final Parser parser) throws ParseException {
        return getIdentifierToken(parser, '\0', '\0');
    }
    
    private static void appendModifierKeyword(final StringBuilder buf, final String modifierKeyword) {
        if (buf.length() > 0 && buf.charAt(buf.length() - 1) != ' ') {
            buf.append(' ');
        }
        buf.append(modifierKeyword);
    }
    
    public static void modifiersToString(final int modifiers, final ModifierType modifierType, final boolean isDefault, final StringBuilder buf) {
        if ((modifiers & 0x1) != 0x0) {
            appendModifierKeyword(buf, "public");
        }
        else if ((modifiers & 0x2) != 0x0) {
            appendModifierKeyword(buf, "private");
        }
        else if ((modifiers & 0x4) != 0x0) {
            appendModifierKeyword(buf, "protected");
        }
        if (modifierType != ModifierType.FIELD && (modifiers & 0x400) != 0x0) {
            appendModifierKeyword(buf, "abstract");
        }
        if ((modifiers & 0x8) != 0x0) {
            appendModifierKeyword(buf, "static");
        }
        if (modifierType == ModifierType.FIELD) {
            if ((modifiers & 0x40) != 0x0) {
                appendModifierKeyword(buf, "volatile");
            }
            if ((modifiers & 0x80) != 0x0) {
                appendModifierKeyword(buf, "transient");
            }
        }
        if ((modifiers & 0x10) != 0x0) {
            appendModifierKeyword(buf, "final");
        }
        if (modifierType == ModifierType.METHOD) {
            if ((modifiers & 0x20) != 0x0) {
                appendModifierKeyword(buf, "synchronized");
            }
            if (isDefault) {
                appendModifierKeyword(buf, "default");
            }
        }
        if ((modifiers & 0x1000) != 0x0) {
            appendModifierKeyword(buf, "synthetic");
        }
        if (modifierType != ModifierType.FIELD && (modifiers & 0x40) != 0x0) {
            appendModifierKeyword(buf, "bridge");
        }
        if (modifierType == ModifierType.METHOD && (modifiers & 0x100) != 0x0) {
            appendModifierKeyword(buf, "native");
        }
        if (modifierType != ModifierType.FIELD && (modifiers & 0x800) != 0x0) {
            appendModifierKeyword(buf, "strictfp");
        }
    }
    
    public enum ModifierType
    {
        CLASS, 
        METHOD, 
        FIELD;
    }
}
