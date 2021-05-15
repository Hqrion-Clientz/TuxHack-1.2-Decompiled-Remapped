// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.json;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

class JSONObject
{
    List<Map.Entry<String, Object>> items;
    CharSequence objectId;
    
    public JSONObject(final int sizeHint) {
        this.items = new ArrayList<Map.Entry<String, Object>>(sizeHint);
    }
    
    public JSONObject(final List<Map.Entry<String, Object>> items) {
        this.items = items;
    }
    
    void toJSONString(final Map<ReferenceEqualityKey<JSONReference>, CharSequence> jsonReferenceToId, final boolean includeNullValuedFields, final int depth, final int indentWidth, final StringBuilder buf) {
        final boolean prettyPrint = indentWidth > 0;
        final int n = this.items.size();
        int numDisplayedFields;
        if (includeNullValuedFields) {
            numDisplayedFields = n;
        }
        else {
            numDisplayedFields = 0;
            for (final Map.Entry<String, Object> item : this.items) {
                if (item.getValue() != null) {
                    ++numDisplayedFields;
                }
            }
        }
        if (this.objectId == null && numDisplayedFields == 0) {
            buf.append("{}");
        }
        else {
            buf.append(prettyPrint ? "{\n" : "{");
            if (this.objectId != null) {
                if (prettyPrint) {
                    JSONUtils.indent(depth + 1, indentWidth, buf);
                }
                buf.append('\"');
                buf.append("__ID");
                buf.append(prettyPrint ? "\": " : "\":");
                JSONSerializer.jsonValToJSONString(this.objectId, jsonReferenceToId, includeNullValuedFields, depth + 1, indentWidth, buf);
                if (numDisplayedFields > 0) {
                    buf.append(',');
                }
                if (prettyPrint) {
                    buf.append('\n');
                }
            }
            int i = 0;
            int j = 0;
            while (i < n) {
                final Map.Entry<String, Object> item2 = this.items.get(i);
                final Object val = item2.getValue();
                if (val != null || includeNullValuedFields) {
                    final String key = item2.getKey();
                    if (key == null) {
                        throw new IllegalArgumentException("Cannot serialize JSON object with null key");
                    }
                    if (prettyPrint) {
                        JSONUtils.indent(depth + 1, indentWidth, buf);
                    }
                    buf.append('\"');
                    JSONUtils.escapeJSONString(key, buf);
                    buf.append(prettyPrint ? "\": " : "\":");
                    JSONSerializer.jsonValToJSONString(val, jsonReferenceToId, includeNullValuedFields, depth + 1, indentWidth, buf);
                    if (++j < numDisplayedFields) {
                        buf.append(',');
                    }
                    if (prettyPrint) {
                        buf.append('\n');
                    }
                }
                ++i;
            }
            if (prettyPrint) {
                JSONUtils.indent(depth, indentWidth, buf);
            }
            buf.append('}');
        }
    }
}
