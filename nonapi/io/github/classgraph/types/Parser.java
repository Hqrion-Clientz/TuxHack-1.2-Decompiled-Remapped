// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.types;

import nonapi.io.github.classgraph.json.JSONUtils;

public class Parser
{
    private final String string;
    private int position;
    private final StringBuilder token;
    private Object state;
    private static final int SHOW_BEFORE = 80;
    private static final int SHOW_AFTER = 80;
    
    public Parser(final String string) throws ParseException {
        this.token = new StringBuilder();
        if (string == null) {
            throw new ParseException(null, "Cannot parse null string");
        }
        this.string = string;
    }
    
    public String getPositionInfo() {
        final int showStart = Math.max(0, this.position - 80);
        final int showEnd = Math.min(this.string.length(), this.position + 80);
        return "before: \"" + JSONUtils.escapeJSONString(this.string.substring(showStart, this.position)) + "\"; after: \"" + JSONUtils.escapeJSONString(this.string.substring(this.position, showEnd)) + "\"; position: " + this.position + "; token: \"" + (Object)this.token + "\"";
    }
    
    public Object setState(final Object state) {
        final Object oldState = this.state;
        this.state = state;
        return oldState;
    }
    
    public Object getState() {
        return this.state;
    }
    
    public char getc() throws ParseException {
        if (this.position >= this.string.length()) {
            throw new ParseException(this, "Ran out of input while parsing");
        }
        return this.string.charAt(this.position++);
    }
    
    public void expect(final char expectedChar) throws ParseException {
        final int next = this.getc();
        if (next != expectedChar) {
            throw new ParseException(this, "Expected '" + expectedChar + "'; got '" + (char)next + "'");
        }
    }
    
    public char peek() {
        return (this.position == this.string.length()) ? '\0' : this.string.charAt(this.position);
    }
    
    public void peekExpect(final char expectedChar) throws ParseException {
        if (this.position == this.string.length()) {
            throw new ParseException(this, "Expected '" + expectedChar + "'; reached end of string");
        }
        final char next = this.string.charAt(this.position);
        if (next != expectedChar) {
            throw new ParseException(this, "Expected '" + expectedChar + "'; got '" + next + "'");
        }
    }
    
    public boolean peekMatches(final String strMatch) {
        return this.string.regionMatches(this.position, strMatch, 0, strMatch.length());
    }
    
    public void next() {
        ++this.position;
    }
    
    public void advance(final int numChars) {
        if (this.position + numChars >= this.string.length()) {
            throw new IllegalArgumentException("Invalid skip distance");
        }
        this.position += numChars;
    }
    
    public boolean hasMore() {
        return this.position < this.string.length();
    }
    
    public int getPosition() {
        return this.position;
    }
    
    public void setPosition(final int position) {
        if (position < 0 || position >= this.string.length()) {
            throw new IllegalArgumentException("Invalid position");
        }
        this.position = position;
    }
    
    public CharSequence getSubsequence(final int startPosition, final int endPosition) {
        return this.string.subSequence(startPosition, endPosition);
    }
    
    public String getSubstring(final int startPosition, final int endPosition) {
        return this.string.substring(startPosition, endPosition);
    }
    
    public void appendToToken(final String str) {
        this.token.append(str);
    }
    
    public void appendToToken(final char c) {
        this.token.append(c);
    }
    
    public void skipWhitespace() {
        while (this.position < this.string.length()) {
            final char c = this.string.charAt(this.position);
            if (c != ' ' && c != '\n' && c != '\r' && c != '\t') {
                break;
            }
            ++this.position;
        }
    }
    
    public String currToken() {
        final String tok = this.token.toString();
        this.token.setLength(0);
        return tok;
    }
    
    @Override
    public String toString() {
        return this.getPositionInfo();
    }
}
