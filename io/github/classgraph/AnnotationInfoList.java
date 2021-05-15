// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import nonapi.io.github.classgraph.utils.CollectionUtils;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;

public class AnnotationInfoList extends MappableInfoList<AnnotationInfo>
{
    private AnnotationInfoList directlyRelatedAnnotations;
    private static final long serialVersionUID = 1L;
    static final AnnotationInfoList EMPTY_LIST;
    
    public static AnnotationInfoList emptyList() {
        return AnnotationInfoList.EMPTY_LIST;
    }
    
    public AnnotationInfoList() {
    }
    
    public AnnotationInfoList(final int sizeHint) {
        super(sizeHint);
    }
    
    public AnnotationInfoList(final AnnotationInfoList reachableAnnotations) {
        this(reachableAnnotations, reachableAnnotations);
    }
    
    AnnotationInfoList(final AnnotationInfoList reachableAnnotations, final AnnotationInfoList directlyRelatedAnnotations) {
        super(reachableAnnotations);
        this.directlyRelatedAnnotations = directlyRelatedAnnotations;
    }
    
    public AnnotationInfoList filter(final AnnotationInfoFilter filter) {
        final AnnotationInfoList annotationInfoFiltered = new AnnotationInfoList();
        for (final AnnotationInfo resource : this) {
            if (filter.accept(resource)) {
                annotationInfoFiltered.add(resource);
            }
        }
        return annotationInfoFiltered;
    }
    
    protected void findReferencedClassInfo(final Map<String, ClassInfo> classNameToClassInfo, final Set<ClassInfo> refdClassInfo) {
        for (final AnnotationInfo ai : this) {
            ai.findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
        }
    }
    
    void handleRepeatableAnnotations(final Set<String> allRepeatableAnnotationNames, final ClassInfo containingClassInfo, final ClassInfo.RelType forwardRelType, final ClassInfo.RelType reverseRelType0, final ClassInfo.RelType reverseRelType1) {
        List<AnnotationInfo> repeatableAnnotations = null;
        for (int i = this.size() - 1; i >= 0; --i) {
            final AnnotationInfo ai = this.get(i);
            if (allRepeatableAnnotationNames.contains(ai.getName())) {
                if (repeatableAnnotations == null) {
                    repeatableAnnotations = new ArrayList<AnnotationInfo>();
                }
                repeatableAnnotations.add(ai);
                this.remove(i);
            }
        }
        if (repeatableAnnotations != null) {
            for (final AnnotationInfo repeatableAnnotation : repeatableAnnotations) {
                final AnnotationParameterValueList values = repeatableAnnotation.getParameterValues();
                if (!values.isEmpty()) {
                    final AnnotationParameterValue apv = values.get("value");
                    if (apv == null) {
                        continue;
                    }
                    final Object arr = apv.getValue();
                    if (!(arr instanceof Object[])) {
                        continue;
                    }
                    for (final Object value : (Object[])arr) {
                        if (value instanceof AnnotationInfo) {
                            final AnnotationInfo ai2 = (AnnotationInfo)value;
                            this.add(ai2);
                            if (forwardRelType != null && (reverseRelType0 != null || reverseRelType1 != null)) {
                                final ClassInfo annotationClass = ai2.getClassInfo();
                                if (annotationClass != null) {
                                    containingClassInfo.addRelatedClass(forwardRelType, annotationClass);
                                    if (reverseRelType0 != null) {
                                        annotationClass.addRelatedClass(reverseRelType0, containingClassInfo);
                                    }
                                    if (reverseRelType1 != null) {
                                        annotationClass.addRelatedClass(reverseRelType1, containingClassInfo);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static void findMetaAnnotations(final AnnotationInfo ai, final AnnotationInfoList allAnnotationsOut, final Set<ClassInfo> visited) {
        final ClassInfo annotationClassInfo = ai.getClassInfo();
        if (annotationClassInfo != null && annotationClassInfo.annotationInfo != null && visited.add(annotationClassInfo)) {
            for (final AnnotationInfo metaAnnotationInfo : annotationClassInfo.annotationInfo) {
                final ClassInfo metaAnnotationClassInfo = metaAnnotationInfo.getClassInfo();
                final String metaAnnotationClassName = metaAnnotationClassInfo.getName();
                if (!metaAnnotationClassName.startsWith("java.lang.annotation.")) {
                    allAnnotationsOut.add(metaAnnotationInfo);
                    findMetaAnnotations(metaAnnotationInfo, allAnnotationsOut, visited);
                }
            }
        }
    }
    
    static AnnotationInfoList getIndirectAnnotations(final AnnotationInfoList directAnnotationInfo, final ClassInfo annotatedClass) {
        final Set<ClassInfo> directOrInheritedAnnotationClasses = new HashSet<ClassInfo>();
        final Set<ClassInfo> reachedAnnotationClasses = new HashSet<ClassInfo>();
        final AnnotationInfoList reachableAnnotationInfo = new AnnotationInfoList((directAnnotationInfo == null) ? 2 : directAnnotationInfo.size());
        if (directAnnotationInfo != null) {
            for (final AnnotationInfo dai : directAnnotationInfo) {
                directOrInheritedAnnotationClasses.add(dai.getClassInfo());
                reachableAnnotationInfo.add(dai);
                findMetaAnnotations(dai, reachableAnnotationInfo, reachedAnnotationClasses);
            }
        }
        if (annotatedClass != null) {
            for (final ClassInfo superclass : annotatedClass.getSuperclasses()) {
                if (superclass.annotationInfo != null) {
                    for (final AnnotationInfo sai : superclass.annotationInfo) {
                        if (sai.isInherited() && directOrInheritedAnnotationClasses.add(sai.getClassInfo())) {
                            reachableAnnotationInfo.add(sai);
                            final AnnotationInfoList reachableMetaAnnotationInfo = new AnnotationInfoList(2);
                            findMetaAnnotations(sai, reachableMetaAnnotationInfo, reachedAnnotationClasses);
                            for (final AnnotationInfo rmai : reachableMetaAnnotationInfo) {
                                if (rmai.isInherited()) {
                                    reachableAnnotationInfo.add(rmai);
                                }
                            }
                        }
                    }
                }
            }
        }
        final AnnotationInfoList directAnnotationInfoSorted = (directAnnotationInfo == null) ? AnnotationInfoList.EMPTY_LIST : new AnnotationInfoList(directAnnotationInfo);
        CollectionUtils.sortIfNotEmpty((List<Comparable>)directAnnotationInfoSorted);
        final AnnotationInfoList annotationInfoList = new AnnotationInfoList(reachableAnnotationInfo, directAnnotationInfoSorted);
        CollectionUtils.sortIfNotEmpty((List<Comparable>)annotationInfoList);
        return annotationInfoList;
    }
    
    public AnnotationInfoList directOnly() {
        return (this.directlyRelatedAnnotations == null) ? this : new AnnotationInfoList(this.directlyRelatedAnnotations, null);
    }
    
    public AnnotationInfoList getRepeatable(final String name) {
        boolean hasNamedAnnotation = false;
        for (final AnnotationInfo ai : this) {
            if (ai.getName().equals(name)) {
                hasNamedAnnotation = true;
                break;
            }
        }
        if (!hasNamedAnnotation) {
            return AnnotationInfoList.EMPTY_LIST;
        }
        final AnnotationInfoList matchingAnnotations = new AnnotationInfoList(this.size());
        for (final AnnotationInfo ai2 : this) {
            if (ai2.getName().equals(name)) {
                matchingAnnotations.add(ai2);
            }
        }
        return matchingAnnotations;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AnnotationInfoList)) {
            return false;
        }
        final AnnotationInfoList other = (AnnotationInfoList)obj;
        if (this.directlyRelatedAnnotations == null != (other.directlyRelatedAnnotations == null)) {
            return false;
        }
        if (this.directlyRelatedAnnotations == null) {
            return super.equals(other);
        }
        return super.equals(other) && this.directlyRelatedAnnotations.equals(other.directlyRelatedAnnotations);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() ^ ((this.directlyRelatedAnnotations == null) ? 0 : this.directlyRelatedAnnotations.hashCode());
    }
    
    static {
        (EMPTY_LIST = new AnnotationInfoList()).makeUnmodifiable();
    }
    
    @FunctionalInterface
    public interface AnnotationInfoFilter
    {
        boolean accept(final AnnotationInfo p0);
    }
}
