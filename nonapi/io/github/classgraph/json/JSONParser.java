// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.json;

import java.util.AbstractMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import nonapi.io.github.classgraph.types.ParseException;
import nonapi.io.github.classgraph.types.Parser;

final class JSONParser extends Parser
{
    private JSONParser(final String string) throws ParseException {
        super(string);
    }
    
    private int getAndParseHexChar() throws ParseException {
        final char hexChar = this.getc();
        if (hexChar >= '0' && hexChar <= '9') {
            return hexChar - '0';
        }
        if (hexChar >= 'a' && hexChar <= 'f') {
            return hexChar - 'a' + 10;
        }
        if (hexChar >= 'A' && hexChar <= 'F') {
            return hexChar - 'A' + 10;
        }
        throw new ParseException(this, "Invalid character in Unicode escape sequence: " + hexChar);
    }
    
    private CharSequence parseString() throws ParseException {
        this.skipWhitespace();
        if (this.peek() != '\"') {
            return null;
        }
        this.next();
        final int startIdx = this.getPosition();
        boolean hasEscape = false;
        while (this.hasMore()) {
            final char c = this.getc();
            if (c == '\\') {
                switch (this.getc()) {
                    case '\"':
                    case '\'':
                    case '/':
                    case '\\':
                    case 'b':
                    case 'f':
                    case 'n':
                    case 'r':
                    case 't': {
                        hasEscape = true;
                        continue;
                    }
                    case 'u': {
                        hasEscape = true;
                        this.advance(4);
                        continue;
                    }
                    default: {
                        throw new ParseException(this, "Invalid escape sequence: \\" + c);
                    }
                }
            }
            else {
                if (c == '\"') {
                    break;
                }
                continue;
            }
        }
        final int endIdx = this.getPosition() - 1;
        if (!hasEscape) {
            return this.getSubsequence(startIdx, endIdx);
        }
        this.setPosition(startIdx);
        final StringBuilder buf = new StringBuilder();
        while (this.hasMore()) {
            final char c2 = this.getc();
            if (c2 == '\\') {
                final char c3 = this.getc();
                switch (c3) {
                    case 'b': {
                        buf.append('\b');
                        continue;
                    }
                    case 'f': {
                        buf.append('\f');
                        continue;
                    }
                    case 'n': {
                        buf.append('\n');
                        continue;
                    }
                    case 'r': {
                        buf.append('\r');
                        continue;
                    }
                    case 't': {
                        buf.append('\t');
                        continue;
                    }
                    case '\"':
                    case '\'':
                    case '/':
                    case '\\': {
                        buf.append(c3);
                        continue;
                    }
                    case 'u': {
                        int charVal = 0;
                        charVal = this.getAndParseHexChar() << 12;
                        charVal |= this.getAndParseHexChar() << 8;
                        charVal |= this.getAndParseHexChar() << 4;
                        charVal |= this.getAndParseHexChar();
                        buf.append((char)charVal);
                        continue;
                    }
                    default: {
                        throw new ParseException(this, "Invalid escape sequence: \\" + c2);
                    }
                }
            }
            else {
                if (c2 == '\"') {
                    break;
                }
                buf.append(c2);
            }
        }
        this.skipWhitespace();
        return buf.toString();
    }
    
    private Number parseNumber() throws ParseException {
        final int startIdx = this.getPosition();
        if (this.peekMatches("Infinity")) {
            this.advance(8);
            return Double.POSITIVE_INFINITY;
        }
        if (this.peekMatches("-Infinity")) {
            this.advance(9);
            return Double.NEGATIVE_INFINITY;
        }
        if (this.peekMatches("NaN")) {
            this.advance(3);
            return Double.NaN;
        }
        if (this.peek() == '-') {
            this.next();
        }
        final int integralStartIdx = this.getPosition();
        while (this.hasMore()) {
            final char c = this.peek();
            if (c < '0') {
                break;
            }
            if (c > '9') {
                break;
            }
            this.next();
        }
        final int integralEndIdx = this.getPosition();
        final int numIntegralDigits = integralEndIdx - integralStartIdx;
        if (numIntegralDigits == 0) {
            throw new ParseException(this, "Expected a number");
        }
        final boolean hasFractionalPart = this.peek() == '.';
        if (hasFractionalPart) {
            this.next();
            while (this.hasMore()) {
                final char c2 = this.peek();
                if (c2 < '0') {
                    break;
                }
                if (c2 > '9') {
                    break;
                }
                this.next();
            }
            if (this.getPosition() - (integralEndIdx + 1) == 0) {
                throw new ParseException(this, "Expected digits after decimal point");
            }
        }
        final boolean hasExponentPart = this.peek() == 'e' || this.peek() == 'E';
        if (hasExponentPart) {
            this.next();
            final char sign = this.peek();
            if (sign == '-' || sign == '+') {
                this.next();
            }
            final int exponentStart = this.getPosition();
            while (this.hasMore()) {
                final char c3 = this.peek();
                if (c3 < '0') {
                    break;
                }
                if (c3 > '9') {
                    break;
                }
                this.next();
            }
            if (this.getPosition() - exponentStart == 0) {
                throw new ParseException(this, "Expected an exponent");
            }
        }
        final int endIdx = this.getPosition();
        final String numberStr = this.getSubstring(startIdx, endIdx);
        if (hasFractionalPart || hasExponentPart) {
            return Double.valueOf(numberStr);
        }
        if (numIntegralDigits < 10) {
            return Integer.valueOf(numberStr);
        }
        if (numIntegralDigits != 10) {
            return Long.valueOf(numberStr);
        }
        final long longVal = Long.parseLong(numberStr);
        if (longVal >= -2147483648L && longVal <= 2147483647L) {
            return (int)longVal;
        }
        return longVal;
    }
    
    private JSONArray parseJSONArray() throws ParseException {
        this.expect('[');
        this.skipWhitespace();
        if (this.peek() == ']') {
            this.next();
            return new JSONArray(Collections.emptyList());
        }
        final List<Object> elements = new ArrayList<Object>();
        boolean first = true;
        while (this.peek() != ']') {
            if (first) {
                first = false;
            }
            else {
                this.expect(',');
            }
            elements.add(this.parseJSON());
        }
        this.expect(']');
        return new JSONArray(elements);
    }
    
    private JSONObject parseJSONObject() throws ParseException {
        this.expect('{');
        this.skipWhitespace();
        if (this.peek() == '}') {
            this.next();
            return new JSONObject(Collections.emptyList());
        }
        final List<Map.Entry<String, Object>> kvPairs = new ArrayList<Map.Entry<String, Object>>();
        final JSONObject jsonObject = new JSONObject(kvPairs);
        boolean first = true;
        while (this.peek() != '}') {
            if (first) {
                first = false;
            }
            else {
                this.expect(',');
            }
            final CharSequence key = this.parseString();
            if (key == null) {
                throw new ParseException(this, "Object keys must be strings");
            }
            if (this.peek() != ':') {
                return null;
            }
            this.expect(':');
            final Object value = this.parseJSON();
            if (key.equals("__ID")) {
                if (value == null) {
                    throw new ParseException(this, "Got null value for \"__ID\" key");
                }
                jsonObject.objectId = (CharSequence)value;
            }
            else {
                kvPairs.add(new AbstractMap.SimpleEntry<String, Object>(key.toString(), value));
            }
        }
        this.expect('}');
        return jsonObject;
    }
    
    private Object parseJSON() throws ParseException {
        this.skipWhitespace();
        try {
            final char c = this.peek();
            if (c == '{') {
                return this.parseJSONObject();
            }
            if (c == '[') {
                return this.parseJSONArray();
            }
            if (c == '\"') {
                final CharSequence charSequence = this.parseString();
                if (charSequence == null) {
                    throw new ParseException(this, "Invalid string");
                }
                return charSequence;
            }
            else {
                if (this.peekMatches("true")) {
                    this.advance(4);
                    return Boolean.TRUE;
                }
                if (this.peekMatches("false")) {
                    this.advance(5);
                    return Boolean.FALSE;
                }
                if (this.peekMatches("null")) {
                    this.advance(4);
                    return null;
                }
                return this.parseNumber();
            }
        }
        finally {
            this.skipWhitespace();
        }
    }
    
    static Object parseJSON(final String str) throws ParseException {
        return new JSONParser(str).parseJSON();
    }
}
