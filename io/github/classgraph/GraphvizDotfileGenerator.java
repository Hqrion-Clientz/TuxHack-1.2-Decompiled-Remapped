// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import nonapi.io.github.classgraph.utils.CollectionUtils;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import java.util.BitSet;

final class GraphvizDotfileGenerator
{
    private static final String STANDARD_CLASS_COLOR = "fff2b6";
    private static final String INTERFACE_COLOR = "b6e7ff";
    private static final String ANNOTATION_COLOR = "f3c9ff";
    private static final int PARAM_WRAP_WIDTH = 40;
    private static final BitSet IS_UNICODE_WHITESPACE;
    
    private GraphvizDotfileGenerator() {
    }
    
    private static boolean isUnicodeWhitespace(final char c) {
        return GraphvizDotfileGenerator.IS_UNICODE_WHITESPACE.get(c);
    }
    
    private static void htmlEncode(final CharSequence unsafeStr, final boolean turnNewlineIntoBreak, final StringBuilder buf) {
        for (int i = 0, n = unsafeStr.length(); i < n; ++i) {
            final char c = unsafeStr.charAt(i);
            switch (c) {
                case '&': {
                    buf.append("&amp;");
                    break;
                }
                case '<': {
                    buf.append("&lt;");
                    break;
                }
                case '>': {
                    buf.append("&gt;");
                    break;
                }
                case '\"': {
                    buf.append("&quot;");
                    break;
                }
                case '\'': {
                    buf.append("&#x27;");
                    break;
                }
                case '\\': {
                    buf.append("&lsol;");
                    break;
                }
                case '/': {
                    buf.append("&#x2F;");
                    break;
                }
                case '\u2014': {
                    buf.append("&mdash;");
                    break;
                }
                case '\u2013': {
                    buf.append("&ndash;");
                    break;
                }
                case '\u201c': {
                    buf.append("&ldquo;");
                    break;
                }
                case '\u201d': {
                    buf.append("&rdquo;");
                    break;
                }
                case '\u2018': {
                    buf.append("&lsquo;");
                    break;
                }
                case '\u2019': {
                    buf.append("&rsquo;");
                    break;
                }
                case '«': {
                    buf.append("&laquo;");
                    break;
                }
                case '»': {
                    buf.append("&raquo;");
                    break;
                }
                case '£': {
                    buf.append("&pound;");
                    break;
                }
                case '©': {
                    buf.append("&copy;");
                    break;
                }
                case '®': {
                    buf.append("&reg;");
                    break;
                }
                case ' ': {
                    buf.append("&nbsp;");
                    break;
                }
                case '\n': {
                    if (turnNewlineIntoBreak) {
                        buf.append("<br>");
                        break;
                    }
                    buf.append(' ');
                    break;
                }
                default: {
                    if (c <= ' ' || isUnicodeWhitespace(c)) {
                        buf.append(' ');
                        break;
                    }
                    buf.append(c);
                    break;
                }
            }
        }
    }
    
    private static void htmlEncode(final CharSequence unsafeStr, final StringBuilder buf) {
        htmlEncode(unsafeStr, false, buf);
    }
    
    private static void labelClassNodeHTML(final ClassInfo ci, final String shape, final String boxBgColor, final boolean showFields, final boolean showMethods, final boolean useSimpleNames, final ScanSpec scanSpec, final StringBuilder buf) {
        buf.append("[shape=").append(shape).append(",style=filled,fillcolor=\"#").append(boxBgColor).append("\",label=");
        buf.append('<');
        buf.append("<table border='0' cellborder='0' cellspacing='1'>");
        buf.append("<tr><td><font point-size='12'>").append(ci.getModifiersStr()).append(' ').append(ci.isEnum() ? "enum" : (ci.isAnnotation() ? "@interface" : (ci.isInterface() ? "interface" : "class"))).append("</font></td></tr>");
        if (ci.getName().contains(".")) {
            buf.append("<tr><td><font point-size='14'><b>");
            htmlEncode(ci.getPackageName() + ".", buf);
            buf.append("</b></font></td></tr>");
        }
        buf.append("<tr><td><font point-size='20'><b>");
        htmlEncode(ci.getSimpleName(), buf);
        buf.append("</b></font></td></tr>");
        final float darkness = 0.8f;
        final int r = (int)(Integer.parseInt(boxBgColor.substring(0, 2), 16) * 0.8f);
        final int g = (int)(Integer.parseInt(boxBgColor.substring(2, 4), 16) * 0.8f);
        final int b = (int)(Integer.parseInt(boxBgColor.substring(4, 6), 16) * 0.8f);
        final String darkerColor = String.format("#%s%s%s%s%s%s", Integer.toString(r >> 4, 16), Integer.toString(r & 0xF, 16), Integer.toString(g >> 4, 16), Integer.toString(g & 0xF, 16), Integer.toString(b >> 4, 16), Integer.toString(b & 0xF, 16));
        final AnnotationInfoList annotationInfo = ci.annotationInfo;
        if (annotationInfo != null && !annotationInfo.isEmpty()) {
            buf.append("<tr><td colspan='3' bgcolor='").append(darkerColor).append("'><font point-size='12'><b>ANNOTATIONS</b></font></td></tr>");
            final AnnotationInfoList annotationInfoSorted = new AnnotationInfoList(annotationInfo);
            CollectionUtils.sortIfNotEmpty((List<Comparable>)annotationInfoSorted);
            for (final AnnotationInfo ai : annotationInfoSorted) {
                final String annotationName = ai.getName();
                if (!annotationName.startsWith("java.lang.annotation.")) {
                    buf.append("<tr>");
                    buf.append("<td align='center' valign='top'>");
                    htmlEncode(ai.toString(), buf);
                    buf.append("</td></tr>");
                }
            }
        }
        final FieldInfoList fieldInfo = ci.fieldInfo;
        if (showFields && fieldInfo != null && !fieldInfo.isEmpty()) {
            final FieldInfoList fieldInfoSorted = new FieldInfoList(fieldInfo);
            CollectionUtils.sortIfNotEmpty((List<Comparable>)fieldInfoSorted);
            for (int i = fieldInfoSorted.size() - 1; i >= 0; --i) {
                if (fieldInfoSorted.get(i).getName().equals("serialVersionUID")) {
                    fieldInfoSorted.remove(i);
                }
            }
            if (!fieldInfoSorted.isEmpty()) {
                buf.append("<tr><td colspan='3' bgcolor='").append(darkerColor).append("'><font point-size='12'><b>").append(scanSpec.ignoreFieldVisibility ? "" : "PUBLIC ").append("FIELDS</b></font></td></tr>");
                buf.append("<tr><td cellpadding='0'>");
                buf.append("<table border='0' cellborder='0'>");
                for (final FieldInfo fi : fieldInfoSorted) {
                    buf.append("<tr>");
                    buf.append("<td align='right' valign='top'>");
                    final AnnotationInfoList fieldAnnotationInfo = fi.annotationInfo;
                    if (fieldAnnotationInfo != null) {
                        for (final AnnotationInfo ai2 : fieldAnnotationInfo) {
                            if (buf.charAt(buf.length() - 1) != ' ') {
                                buf.append(' ');
                            }
                            htmlEncode(ai2.toString(), buf);
                        }
                    }
                    if (scanSpec.ignoreFieldVisibility) {
                        if (buf.charAt(buf.length() - 1) != ' ') {
                            buf.append(' ');
                        }
                        buf.append(fi.getModifierStr());
                    }
                    if (buf.charAt(buf.length() - 1) != ' ') {
                        buf.append(' ');
                    }
                    final TypeSignature typeSig = fi.getTypeSignatureOrTypeDescriptor();
                    htmlEncode(useSimpleNames ? typeSig.toStringWithSimpleNames() : typeSig.toString(), buf);
                    buf.append("</td>");
                    buf.append("<td align='left' valign='top'><b>");
                    final String fieldName = fi.getName();
                    htmlEncode(fieldName, buf);
                    buf.append("</b></td></tr>");
                }
                buf.append("</table>");
                buf.append("</td></tr>");
            }
        }
        final MethodInfoList methodInfo = ci.methodInfo;
        if (showMethods && methodInfo != null) {
            final MethodInfoList methodInfoSorted = new MethodInfoList(methodInfo);
            CollectionUtils.sortIfNotEmpty((List<Comparable>)methodInfoSorted);
            for (int j = methodInfoSorted.size() - 1; j >= 0; --j) {
                final MethodInfo mi = methodInfoSorted.get(j);
                final String name = mi.getName();
                final int numParam = mi.getParameterInfo().length;
                if (name.equals("<clinit>") || (name.equals("hashCode") && numParam == 0) || (name.equals("toString") && numParam == 0) || (name.equals("equals") && numParam == 1 && mi.getTypeDescriptor().toString().equals("boolean (java.lang.Object)"))) {
                    methodInfoSorted.remove(j);
                }
            }
            if (!methodInfoSorted.isEmpty()) {
                buf.append("<tr><td cellpadding='0'>");
                buf.append("<table border='0' cellborder='0'>");
                buf.append("<tr><td colspan='3' bgcolor='").append(darkerColor).append("'><font point-size='12'><b>").append(scanSpec.ignoreMethodVisibility ? "" : "PUBLIC ").append("METHODS</b></font></td></tr>");
                for (final MethodInfo mi : methodInfoSorted) {
                    buf.append("<tr>");
                    buf.append("<td align='right' valign='top'>");
                    final AnnotationInfoList methodAnnotationInfo = mi.annotationInfo;
                    if (methodAnnotationInfo != null) {
                        for (final AnnotationInfo ai3 : methodAnnotationInfo) {
                            if (buf.charAt(buf.length() - 1) != ' ') {
                                buf.append(' ');
                            }
                            htmlEncode(ai3.toString(), buf);
                        }
                    }
                    if (scanSpec.ignoreMethodVisibility) {
                        if (buf.charAt(buf.length() - 1) != ' ') {
                            buf.append(' ');
                        }
                        buf.append(mi.getModifiersStr());
                    }
                    if (buf.charAt(buf.length() - 1) != ' ') {
                        buf.append(' ');
                    }
                    if (!mi.getName().equals("<init>")) {
                        final TypeSignature resultTypeSig = mi.getTypeSignatureOrTypeDescriptor().getResultType();
                        htmlEncode(useSimpleNames ? resultTypeSig.toStringWithSimpleNames() : resultTypeSig.toString(), buf);
                    }
                    else {
                        buf.append("<b>&lt;constructor&gt;</b>");
                    }
                    buf.append("</td>");
                    buf.append("<td align='left' valign='top'>");
                    buf.append("<b>");
                    if (mi.getName().equals("<init>")) {
                        htmlEncode(ci.getSimpleName(), buf);
                    }
                    else {
                        htmlEncode(mi.getName(), buf);
                    }
                    buf.append("</b>&nbsp;");
                    buf.append("</td>");
                    buf.append("<td align='left' valign='top'>");
                    buf.append('(');
                    final MethodParameterInfo[] paramInfo = mi.getParameterInfo();
                    if (paramInfo.length != 0) {
                        int k = 0;
                        int wrapPos = 0;
                        while (k < paramInfo.length) {
                            if (k > 0) {
                                buf.append(", ");
                                wrapPos += 2;
                            }
                            if (wrapPos > 40) {
                                buf.append("</td></tr><tr><td></td><td></td><td align='left' valign='top'>");
                                wrapPos = 0;
                            }
                            final AnnotationInfo[] paramAnnotationInfo = paramInfo[k].annotationInfo;
                            if (paramAnnotationInfo != null) {
                                for (final AnnotationInfo ai4 : paramAnnotationInfo) {
                                    final String ais = ai4.toString();
                                    if (!ais.isEmpty()) {
                                        if (buf.charAt(buf.length() - 1) != ' ') {
                                            buf.append(' ');
                                        }
                                        htmlEncode(ais, buf);
                                        wrapPos += 1 + ais.length();
                                        if (wrapPos > 40) {
                                            buf.append("</td></tr><tr><td></td><td></td><td align='left' valign='top'>");
                                            wrapPos = 0;
                                        }
                                    }
                                }
                            }
                            final TypeSignature paramTypeSig = paramInfo[k].getTypeSignatureOrTypeDescriptor();
                            final String paramTypeStr = useSimpleNames ? paramTypeSig.toStringWithSimpleNames() : paramTypeSig.toString();
                            htmlEncode(paramTypeStr, buf);
                            wrapPos += paramTypeStr.length();
                            final String paramName = paramInfo[k].getName();
                            if (paramName != null) {
                                buf.append(" <B>");
                                htmlEncode(paramName, buf);
                                wrapPos += 1 + paramName.length();
                                buf.append("</B>");
                            }
                            ++k;
                        }
                    }
                    buf.append(')');
                    buf.append("</td></tr>");
                }
                buf.append("</table>");
                buf.append("</td></tr>");
            }
        }
        buf.append("</table>");
        buf.append(">]");
    }
    
    static String generateGraphVizDotFile(final ClassInfoList classInfoList, final float sizeX, final float sizeY, final boolean showFields, final boolean showFieldTypeDependencyEdges, final boolean showMethods, final boolean showMethodTypeDependencyEdges, final boolean showAnnotations, final boolean useSimpleNames, final ScanSpec scanSpec) {
        final StringBuilder buf = new StringBuilder(1048576);
        buf.append("digraph {\n");
        buf.append("size=\"").append(sizeX).append(',').append(sizeY).append("\";\n");
        buf.append("layout=dot;\n");
        buf.append("rankdir=\"BT\";\n");
        buf.append("overlap=false;\n");
        buf.append("splines=true;\n");
        buf.append("pack=true;\n");
        buf.append("graph [fontname = \"Courier, Regular\"]\n");
        buf.append("node [fontname = \"Courier, Regular\"]\n");
        buf.append("edge [fontname = \"Courier, Regular\"]\n");
        final ClassInfoList standardClassNodes = classInfoList.getStandardClasses();
        final ClassInfoList interfaceNodes = classInfoList.getInterfaces();
        final ClassInfoList annotationNodes = classInfoList.getAnnotations();
        for (final ClassInfo node : standardClassNodes) {
            buf.append('\"').append(node.getName()).append('\"');
            labelClassNodeHTML(node, "box", "fff2b6", showFields, showMethods, useSimpleNames, scanSpec, buf);
            buf.append(";\n");
        }
        for (final ClassInfo node : interfaceNodes) {
            buf.append('\"').append(node.getName()).append('\"');
            labelClassNodeHTML(node, "diamond", "b6e7ff", showFields, showMethods, useSimpleNames, scanSpec, buf);
            buf.append(";\n");
        }
        for (final ClassInfo node : annotationNodes) {
            buf.append('\"').append(node.getName()).append('\"');
            labelClassNodeHTML(node, "oval", "f3c9ff", showFields, showMethods, useSimpleNames, scanSpec, buf);
            buf.append(";\n");
        }
        final Set<String> allVisibleNodes = new HashSet<String>();
        allVisibleNodes.addAll(standardClassNodes.getNames());
        allVisibleNodes.addAll(interfaceNodes.getNames());
        allVisibleNodes.addAll(annotationNodes.getNames());
        buf.append('\n');
        for (final ClassInfo classNode : standardClassNodes) {
            for (final ClassInfo directSuperclassNode : classNode.getSuperclasses().directOnly()) {
                if (directSuperclassNode != null && allVisibleNodes.contains(directSuperclassNode.getName()) && !directSuperclassNode.getName().equals("java.lang.Object")) {
                    buf.append("  \"").append(classNode.getName()).append("\" -> \"").append(directSuperclassNode.getName()).append("\" [arrowsize=2.5]\n");
                }
            }
            for (final ClassInfo implementedInterfaceNode : classNode.getInterfaces().directOnly()) {
                if (allVisibleNodes.contains(implementedInterfaceNode.getName())) {
                    buf.append("  \"").append(classNode.getName()).append("\" -> \"").append(implementedInterfaceNode.getName()).append("\" [arrowhead=diamond, arrowsize=2.5]\n");
                }
            }
            if (showFieldTypeDependencyEdges && classNode.fieldInfo != null) {
                for (final FieldInfo fi : classNode.fieldInfo) {
                    for (final ClassInfo referencedFieldType : fi.findReferencedClassInfo()) {
                        if (allVisibleNodes.contains(referencedFieldType.getName())) {
                            buf.append("  \"").append(referencedFieldType.getName()).append("\" -> \"").append(classNode.getName()).append("\" [arrowtail=obox, arrowsize=2.5, dir=back]\n");
                        }
                    }
                }
            }
            if (showMethodTypeDependencyEdges && classNode.methodInfo != null) {
                for (final MethodInfo mi : classNode.methodInfo) {
                    for (final ClassInfo referencedMethodType : mi.findReferencedClassInfo()) {
                        if (allVisibleNodes.contains(referencedMethodType.getName())) {
                            buf.append("  \"").append(referencedMethodType.getName()).append("\" -> \"").append(classNode.getName()).append("\" [arrowtail=box, arrowsize=2.5, dir=back]\n");
                        }
                    }
                }
            }
        }
        for (final ClassInfo interfaceNode : interfaceNodes) {
            for (final ClassInfo superinterfaceNode : interfaceNode.getInterfaces().directOnly()) {
                if (allVisibleNodes.contains(superinterfaceNode.getName())) {
                    buf.append("  \"").append(interfaceNode.getName()).append("\" -> \"").append(superinterfaceNode.getName()).append("\" [arrowhead=diamond, arrowsize=2.5]\n");
                }
            }
        }
        if (showAnnotations) {
            for (final ClassInfo annotationNode : annotationNodes) {
                for (final ClassInfo annotatedClassNode : annotationNode.getClassesWithAnnotationDirectOnly()) {
                    if (allVisibleNodes.contains(annotatedClassNode.getName())) {
                        buf.append("  \"").append(annotatedClassNode.getName()).append("\" -> \"").append(annotationNode.getName()).append("\" [arrowhead=dot, arrowsize=2.5]\n");
                    }
                }
                for (final ClassInfo classWithMethodAnnotationNode : annotationNode.getClassesWithMethodAnnotationDirectOnly()) {
                    if (allVisibleNodes.contains(classWithMethodAnnotationNode.getName())) {
                        buf.append("  \"").append(classWithMethodAnnotationNode.getName()).append("\" -> \"").append(annotationNode.getName()).append("\" [arrowhead=odot, arrowsize=2.5]\n");
                    }
                }
                for (final ClassInfo classWithMethodAnnotationNode : annotationNode.getClassesWithFieldAnnotationDirectOnly()) {
                    if (allVisibleNodes.contains(classWithMethodAnnotationNode.getName())) {
                        buf.append("  \"").append(classWithMethodAnnotationNode.getName()).append("\" -> \"").append(annotationNode.getName()).append("\" [arrowhead=odot, arrowsize=2.5]\n");
                    }
                }
            }
        }
        buf.append('}');
        return buf.toString();
    }
    
    static String generateGraphVizDotFileFromInterClassDependencies(final ClassInfoList classInfoList, final float sizeX, final float sizeY, final boolean includeExternalClasses) {
        final StringBuilder buf = new StringBuilder(1048576);
        buf.append("digraph {\n");
        buf.append("size=\"").append(sizeX).append(',').append(sizeY).append("\";\n");
        buf.append("layout=dot;\n");
        buf.append("rankdir=\"BT\";\n");
        buf.append("overlap=false;\n");
        buf.append("splines=true;\n");
        buf.append("pack=true;\n");
        buf.append("graph [fontname = \"Courier, Regular\"]\n");
        buf.append("node [fontname = \"Courier, Regular\"]\n");
        buf.append("edge [fontname = \"Courier, Regular\"]\n");
        final Set<ClassInfo> allVisibleNodes = new HashSet<ClassInfo>(classInfoList);
        if (includeExternalClasses) {
            for (final ClassInfo ci : classInfoList) {
                allVisibleNodes.addAll(ci.getClassDependencies());
            }
        }
        for (final ClassInfo ci : allVisibleNodes) {
            buf.append('\"').append(ci.getName()).append('\"');
            buf.append("[shape=").append(ci.isAnnotation() ? "oval" : (ci.isInterface() ? "diamond" : "box")).append(",style=filled,fillcolor=\"#").append(ci.isAnnotation() ? "f3c9ff" : (ci.isInterface() ? "b6e7ff" : "fff2b6")).append("\",label=");
            buf.append('<');
            buf.append("<table border='0' cellborder='0' cellspacing='1'>");
            buf.append("<tr><td><font point-size='12'>").append(ci.getModifiersStr()).append(' ').append(ci.isEnum() ? "enum" : (ci.isAnnotation() ? "@interface" : (ci.isInterface() ? "interface" : "class"))).append("</font></td></tr>");
            if (ci.getName().contains(".")) {
                buf.append("<tr><td><font point-size='14'><b>");
                htmlEncode(ci.getPackageName(), buf);
                buf.append("</b></font></td></tr>");
            }
            buf.append("<tr><td><font point-size='20'><b>");
            htmlEncode(ci.getSimpleName(), buf);
            buf.append("</b></font></td></tr>");
            buf.append("</table>");
            buf.append(">];\n");
        }
        buf.append('\n');
        for (final ClassInfo ci : classInfoList) {
            for (final ClassInfo dep : ci.getClassDependencies()) {
                if (includeExternalClasses || allVisibleNodes.contains(dep)) {
                    buf.append("  \"").append(ci.getName()).append("\" -> \"").append(dep.getName()).append("\" [arrowsize=2.5]\n");
                }
            }
        }
        buf.append('}');
        return buf.toString();
    }
    
    static {
        IS_UNICODE_WHITESPACE = new BitSet(65536);
        final String wsChars = " \t\n\u000b\f\r\u0085 \u1680\u180e\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u2028\u2029\u202f\u205f\u3000";
        for (int i = 0; i < " \t\n\u000b\f\r\u0085 \u1680\u180e\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u2028\u2029\u202f\u205f\u3000".length(); ++i) {
            GraphvizDotfileGenerator.IS_UNICODE_WHITESPACE.set(" \t\n\u000b\f\r\u0085 \u1680\u180e\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u2028\u2029\u202f\u205f\u3000".charAt(i));
        }
    }
}
