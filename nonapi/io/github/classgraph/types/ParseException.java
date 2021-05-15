// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.types;

public class ParseException extends Exception
{
    static final long serialVersionUID = 1L;
    
    public ParseException(final Parser parser, final String msg) {
        super((parser == null) ? msg : (msg + " (" + parser.getPositionInfo() + ")"));
    }
}
