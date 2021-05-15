// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import nonapi.io.github.classgraph.types.ParseException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import nonapi.io.github.classgraph.types.TypeUtils;
import java.util.LinkedList;
import java.util.Collections;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import java.util.Collection;
import java.lang.reflect.Modifier;
import java.lang.annotation.Inherited;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nonapi.io.github.classgraph.json.Id;

public class ClassInfo extends ScanResultObject implements Comparable<ClassInfo>, HasName
{
    @Id
    protected String name;
    private int modifiers;
    private boolean isRecord;
    boolean isInherited;
    private int classfileMinorVersion;
    private int classfileMajorVersion;
    protected String typeSignatureStr;
    private transient ClassTypeSignature typeSignature;
    private String fullyQualifiedDefiningMethodName;
    protected boolean isExternalClass;
    protected boolean isScannedClass;
    transient ClasspathElement classpathElement;
    protected transient Resource classfileResource;
    transient ClassLoader classLoader;
    ModuleInfo moduleInfo;
    PackageInfo packageInfo;
    AnnotationInfoList annotationInfo;
    FieldInfoList fieldInfo;
    MethodInfoList methodInfo;
    AnnotationParameterValueList annotationDefaultParamValues;
    private Set<String> referencedClassNames;
    private ClassInfoList referencedClasses;
    transient boolean annotationDefaultParamValuesHasBeenConvertedToPrimitive;
    private Map<RelType, Set<ClassInfo>> relatedClasses;
    private transient List<ClassInfo> overrideOrder;
    private static final int ANNOTATION_CLASS_MODIFIER = 8192;
    private static final ReachableAndDirectlyRelatedClasses NO_REACHABLE_CLASSES;
    
    ClassInfo() {
        this.isExternalClass = true;
    }
    
    protected ClassInfo(final String name, final int classModifiers, final Resource classfileResource) {
        this.isExternalClass = true;
        this.name = name;
        if (name.endsWith(";")) {
            throw new IllegalArgumentException("Bad class name");
        }
        this.setModifiers(classModifiers);
        this.classfileResource = classfileResource;
        this.relatedClasses = new EnumMap<RelType, Set<ClassInfo>>(RelType.class);
    }
    
    boolean addRelatedClass(final RelType relType, final ClassInfo classInfo) {
        Set<ClassInfo> classInfoSet = this.relatedClasses.get(relType);
        if (classInfoSet == null) {
            this.relatedClasses.put(relType, classInfoSet = new LinkedHashSet<ClassInfo>(4));
        }
        return classInfoSet.add(classInfo);
    }
    
    static ClassInfo getOrCreateClassInfo(final String className, final Map<String, ClassInfo> classNameToClassInfo) {
        int numArrayDims = 0;
        String baseClassName;
        for (baseClassName = className; baseClassName.endsWith("[]"); baseClassName = baseClassName.substring(0, baseClassName.length() - 2)) {
            ++numArrayDims;
        }
        while (baseClassName.startsWith("[")) {
            ++numArrayDims;
            baseClassName = baseClassName.substring(1);
        }
        if (baseClassName.endsWith(";")) {
            baseClassName = baseClassName.substring(baseClassName.length() - 1);
        }
        baseClassName = baseClassName.replace('/', '.');
        ClassInfo classInfo = classNameToClassInfo.get(className);
        if (classInfo == null) {
            classNameToClassInfo.put(className, classInfo = ((numArrayDims == 0) ? new ClassInfo(baseClassName, 0, null) : new ArrayClassInfo(new ArrayTypeSignature(baseClassName, numArrayDims))));
        }
        return classInfo;
    }
    
    void setClassfileVersion(final int minorVersion, final int majorVersion) {
        this.classfileMinorVersion = minorVersion;
        this.classfileMajorVersion = majorVersion;
    }
    
    void setModifiers(final int modifiers) {
        this.modifiers |= modifiers;
    }
    
    void setIsInterface(final boolean isInterface) {
        if (isInterface) {
            this.modifiers |= 0x200;
        }
    }
    
    void setIsAnnotation(final boolean isAnnotation) {
        if (isAnnotation) {
            this.modifiers |= 0x2000;
        }
    }
    
    void setIsRecord(final boolean isRecord) {
        if (isRecord) {
            this.isRecord = isRecord;
        }
    }
    
    void addSuperclass(final String superclassName, final Map<String, ClassInfo> classNameToClassInfo) {
        if (superclassName != null && !superclassName.equals("java.lang.Object")) {
            final ClassInfo superclassClassInfo = getOrCreateClassInfo(superclassName, classNameToClassInfo);
            this.addRelatedClass(RelType.SUPERCLASSES, superclassClassInfo);
            superclassClassInfo.addRelatedClass(RelType.SUBCLASSES, this);
        }
    }
    
    void addImplementedInterface(final String interfaceName, final Map<String, ClassInfo> classNameToClassInfo) {
        final ClassInfo interfaceClassInfo = getOrCreateClassInfo(interfaceName, classNameToClassInfo);
        interfaceClassInfo.setIsInterface(true);
        this.addRelatedClass(RelType.IMPLEMENTED_INTERFACES, interfaceClassInfo);
        interfaceClassInfo.addRelatedClass(RelType.CLASSES_IMPLEMENTING, this);
    }
    
    static void addClassContainment(final List<Classfile.ClassContainment> classContainmentEntries, final Map<String, ClassInfo> classNameToClassInfo) {
        for (final Classfile.ClassContainment classContainment : classContainmentEntries) {
            final ClassInfo innerClassInfo = getOrCreateClassInfo(classContainment.innerClassName, classNameToClassInfo);
            innerClassInfo.setModifiers(classContainment.innerClassModifierBits);
            final ClassInfo outerClassInfo = getOrCreateClassInfo(classContainment.outerClassName, classNameToClassInfo);
            innerClassInfo.addRelatedClass(RelType.CONTAINED_WITHIN_OUTER_CLASS, outerClassInfo);
            outerClassInfo.addRelatedClass(RelType.CONTAINS_INNER_CLASS, innerClassInfo);
        }
    }
    
    void addFullyQualifiedDefiningMethodName(final String fullyQualifiedDefiningMethodName) {
        this.fullyQualifiedDefiningMethodName = fullyQualifiedDefiningMethodName;
    }
    
    void addClassAnnotation(final AnnotationInfo classAnnotationInfo, final Map<String, ClassInfo> classNameToClassInfo) {
        final ClassInfo annotationClassInfo = getOrCreateClassInfo(classAnnotationInfo.getName(), classNameToClassInfo);
        annotationClassInfo.setModifiers(8192);
        if (this.annotationInfo == null) {
            this.annotationInfo = new AnnotationInfoList(2);
        }
        this.annotationInfo.add(classAnnotationInfo);
        this.addRelatedClass(RelType.CLASS_ANNOTATIONS, annotationClassInfo);
        annotationClassInfo.addRelatedClass(RelType.CLASSES_WITH_ANNOTATION, this);
        if (classAnnotationInfo.getName().equals(Inherited.class.getName())) {
            this.isInherited = true;
        }
    }
    
    private void addFieldOrMethodAnnotationInfo(final AnnotationInfoList annotationInfoList, final boolean isField, final int modifiers, final Map<String, ClassInfo> classNameToClassInfo) {
        if (annotationInfoList != null) {
            for (final AnnotationInfo fieldAnnotationInfo : annotationInfoList) {
                final ClassInfo annotationClassInfo = getOrCreateClassInfo(fieldAnnotationInfo.getName(), classNameToClassInfo);
                annotationClassInfo.setModifiers(8192);
                this.addRelatedClass(isField ? RelType.FIELD_ANNOTATIONS : RelType.METHOD_ANNOTATIONS, annotationClassInfo);
                annotationClassInfo.addRelatedClass(isField ? RelType.CLASSES_WITH_FIELD_ANNOTATION : RelType.CLASSES_WITH_METHOD_ANNOTATION, this);
                if (!Modifier.isPrivate(modifiers)) {
                    annotationClassInfo.addRelatedClass(isField ? RelType.CLASSES_WITH_NONPRIVATE_FIELD_ANNOTATION : RelType.CLASSES_WITH_NONPRIVATE_METHOD_ANNOTATION, this);
                }
            }
        }
    }
    
    void addFieldInfo(final FieldInfoList fieldInfoList, final Map<String, ClassInfo> classNameToClassInfo) {
        for (final FieldInfo fi : fieldInfoList) {
            this.addFieldOrMethodAnnotationInfo(fi.annotationInfo, true, fi.getModifiers(), classNameToClassInfo);
        }
        if (this.fieldInfo == null) {
            this.fieldInfo = fieldInfoList;
        }
        else {
            this.fieldInfo.addAll(fieldInfoList);
        }
    }
    
    void addMethodInfo(final MethodInfoList methodInfoList, final Map<String, ClassInfo> classNameToClassInfo) {
        for (final MethodInfo mi : methodInfoList) {
            this.addFieldOrMethodAnnotationInfo(mi.annotationInfo, false, mi.getModifiers(), classNameToClassInfo);
            if (mi.parameterAnnotationInfo != null) {
                for (int i = 0; i < mi.parameterAnnotationInfo.length; ++i) {
                    final AnnotationInfo[] paramAnnotationInfoArr = mi.parameterAnnotationInfo[i];
                    if (paramAnnotationInfoArr != null) {
                        for (int j = 0; j < paramAnnotationInfoArr.length; ++j) {
                            final AnnotationInfo methodParamAnnotationInfo = paramAnnotationInfoArr[j];
                            final ClassInfo annotationClassInfo = getOrCreateClassInfo(methodParamAnnotationInfo.getName(), classNameToClassInfo);
                            annotationClassInfo.setModifiers(8192);
                            this.addRelatedClass(RelType.METHOD_PARAMETER_ANNOTATIONS, annotationClassInfo);
                            annotationClassInfo.addRelatedClass(RelType.CLASSES_WITH_METHOD_PARAMETER_ANNOTATION, this);
                            if (!Modifier.isPrivate(mi.getModifiers())) {
                                annotationClassInfo.addRelatedClass(RelType.CLASSES_WITH_NONPRIVATE_METHOD_PARAMETER_ANNOTATION, this);
                            }
                        }
                    }
                }
            }
        }
        if (this.methodInfo == null) {
            this.methodInfo = methodInfoList;
        }
        else {
            this.methodInfo.addAll(methodInfoList);
        }
    }
    
    void setTypeSignature(final String typeSignatureStr) {
        this.typeSignatureStr = typeSignatureStr;
    }
    
    void addAnnotationParamDefaultValues(final AnnotationParameterValueList paramNamesAndValues) {
        this.setIsAnnotation(true);
        if (this.annotationDefaultParamValues == null) {
            this.annotationDefaultParamValues = paramNamesAndValues;
        }
        else {
            this.annotationDefaultParamValues.addAll(paramNamesAndValues);
        }
    }
    
    static ClassInfo addScannedClass(final String className, final int classModifiers, final boolean isExternalClass, final Map<String, ClassInfo> classNameToClassInfo, final ClasspathElement classpathElement, final Resource classfileResource) {
        ClassInfo classInfo = classNameToClassInfo.get(className);
        if (classInfo == null) {
            classNameToClassInfo.put(className, classInfo = new ClassInfo(className, classModifiers, classfileResource));
        }
        else {
            if (classInfo.isScannedClass) {
                throw new IllegalArgumentException("Class " + className + " should not have been encountered more than once due to classpath masking -- please report this bug at: https://github.com/classgraph/classgraph/issues");
            }
            classInfo.classfileResource = classfileResource;
            final ClassInfo classInfo2 = classInfo;
            classInfo2.modifiers |= classModifiers;
        }
        classInfo.isScannedClass = true;
        classInfo.isExternalClass = isExternalClass;
        classInfo.classpathElement = classpathElement;
        classInfo.classLoader = classpathElement.getClassLoader();
        return classInfo;
    }
    
    private static Set<ClassInfo> filterClassInfo(final Collection<ClassInfo> classes, final ScanSpec scanSpec, final boolean strictAccept, final ClassType... classTypes) {
        if (classes == null) {
            return Collections.emptySet();
        }
        boolean includeAllTypes = classTypes.length == 0;
        boolean includeStandardClasses = false;
        boolean includeImplementedInterfaces = false;
        boolean includeAnnotations = false;
        boolean includeEnums = false;
        boolean includeRecords = false;
        for (final ClassType classType : classTypes) {
            switch (classType) {
                case ALL: {
                    includeAllTypes = true;
                    break;
                }
                case STANDARD_CLASS: {
                    includeStandardClasses = true;
                    break;
                }
                case IMPLEMENTED_INTERFACE: {
                    includeImplementedInterfaces = true;
                    break;
                }
                case ANNOTATION: {
                    includeAnnotations = true;
                    break;
                }
                case INTERFACE_OR_ANNOTATION: {
                    includeAnnotations = (includeImplementedInterfaces = true);
                    break;
                }
                case ENUM: {
                    includeEnums = true;
                    break;
                }
                case RECORD: {
                    includeRecords = true;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unknown ClassType: " + classType);
                }
            }
        }
        if (includeStandardClasses && includeImplementedInterfaces && includeAnnotations) {
            includeAllTypes = true;
        }
        final Set<ClassInfo> classInfoSetFiltered = new LinkedHashSet<ClassInfo>(classes.size());
        for (final ClassInfo classInfo : classes) {
            if ((includeAllTypes || (includeStandardClasses && classInfo.isStandardClass()) || (includeImplementedInterfaces && classInfo.isImplementedInterface()) || (includeAnnotations && classInfo.isAnnotation()) || (includeEnums && classInfo.isEnum()) || (includeRecords && classInfo.isRecord())) && !scanSpec.classOrPackageIsRejected(classInfo.name) && (!classInfo.isExternalClass || scanSpec.enableExternalClasses || !strictAccept)) {
                classInfoSetFiltered.add(classInfo);
            }
        }
        return classInfoSetFiltered;
    }
    
    private ReachableAndDirectlyRelatedClasses filterClassInfo(final RelType relType, final boolean strictAccept, final ClassType... classTypes) {
        Set<ClassInfo> directlyRelatedClasses = this.relatedClasses.get(relType);
        if (directlyRelatedClasses == null) {
            return ClassInfo.NO_REACHABLE_CLASSES;
        }
        directlyRelatedClasses = new LinkedHashSet<ClassInfo>(directlyRelatedClasses);
        final Set<ClassInfo> reachableClasses = new LinkedHashSet<ClassInfo>(directlyRelatedClasses);
        if (relType == RelType.METHOD_ANNOTATIONS || relType == RelType.METHOD_PARAMETER_ANNOTATIONS || relType == RelType.FIELD_ANNOTATIONS) {
            for (final ClassInfo annotation : directlyRelatedClasses) {
                reachableClasses.addAll(annotation.filterClassInfo(RelType.CLASS_ANNOTATIONS, strictAccept, new ClassType[0]).reachableClasses);
            }
        }
        else if (relType == RelType.CLASSES_WITH_METHOD_ANNOTATION || relType == RelType.CLASSES_WITH_NONPRIVATE_METHOD_ANNOTATION || relType == RelType.CLASSES_WITH_METHOD_PARAMETER_ANNOTATION || relType == RelType.CLASSES_WITH_NONPRIVATE_METHOD_PARAMETER_ANNOTATION || relType == RelType.CLASSES_WITH_FIELD_ANNOTATION || relType == RelType.CLASSES_WITH_NONPRIVATE_FIELD_ANNOTATION) {
            for (final ClassInfo subAnnotation : this.filterClassInfo(RelType.CLASSES_WITH_ANNOTATION, strictAccept, ClassType.ANNOTATION).reachableClasses) {
                final Set<ClassInfo> annotatedClasses = subAnnotation.relatedClasses.get(relType);
                if (annotatedClasses != null) {
                    reachableClasses.addAll(annotatedClasses);
                }
            }
        }
        else {
            final LinkedList<ClassInfo> queue = new LinkedList<ClassInfo>(directlyRelatedClasses);
            while (!queue.isEmpty()) {
                final ClassInfo head = queue.removeFirst();
                final Set<ClassInfo> headRelatedClasses = head.relatedClasses.get(relType);
                if (headRelatedClasses != null) {
                    for (final ClassInfo directlyReachableFromHead : headRelatedClasses) {
                        if (reachableClasses.add(directlyReachableFromHead)) {
                            queue.add(directlyReachableFromHead);
                        }
                    }
                }
            }
        }
        if (reachableClasses.isEmpty()) {
            return ClassInfo.NO_REACHABLE_CLASSES;
        }
        if (relType == RelType.CLASS_ANNOTATIONS || relType == RelType.METHOD_ANNOTATIONS || relType == RelType.METHOD_PARAMETER_ANNOTATIONS || relType == RelType.FIELD_ANNOTATIONS) {
            Set<ClassInfo> reachableClassesToRemove = null;
            for (final ClassInfo reachableClassInfo : reachableClasses) {
                if (reachableClassInfo.getName().startsWith("java.lang.annotation.") && !directlyRelatedClasses.contains(reachableClassInfo)) {
                    if (reachableClassesToRemove == null) {
                        reachableClassesToRemove = new LinkedHashSet<ClassInfo>();
                    }
                    reachableClassesToRemove.add(reachableClassInfo);
                }
            }
            if (reachableClassesToRemove != null) {
                reachableClasses.removeAll(reachableClassesToRemove);
            }
        }
        return new ReachableAndDirectlyRelatedClasses((Set)filterClassInfo(reachableClasses, this.scanResult.scanSpec, strictAccept, classTypes), (Set)filterClassInfo(directlyRelatedClasses, this.scanResult.scanSpec, strictAccept, classTypes));
    }
    
    static ClassInfoList getAllClasses(final Collection<ClassInfo> classes, final ScanSpec scanSpec) {
        return new ClassInfoList(filterClassInfo(classes, scanSpec, true, ClassType.ALL), true);
    }
    
    static ClassInfoList getAllEnums(final Collection<ClassInfo> classes, final ScanSpec scanSpec) {
        return new ClassInfoList(filterClassInfo(classes, scanSpec, true, ClassType.ENUM), true);
    }
    
    static ClassInfoList getAllRecords(final Collection<ClassInfo> classes, final ScanSpec scanSpec) {
        return new ClassInfoList(filterClassInfo(classes, scanSpec, true, ClassType.RECORD), true);
    }
    
    static ClassInfoList getAllStandardClasses(final Collection<ClassInfo> classes, final ScanSpec scanSpec) {
        return new ClassInfoList(filterClassInfo(classes, scanSpec, true, ClassType.STANDARD_CLASS), true);
    }
    
    static ClassInfoList getAllImplementedInterfaceClasses(final Collection<ClassInfo> classes, final ScanSpec scanSpec) {
        return new ClassInfoList(filterClassInfo(classes, scanSpec, true, ClassType.IMPLEMENTED_INTERFACE), true);
    }
    
    static ClassInfoList getAllAnnotationClasses(final Collection<ClassInfo> classes, final ScanSpec scanSpec) {
        return new ClassInfoList(filterClassInfo(classes, scanSpec, true, ClassType.ANNOTATION), true);
    }
    
    static ClassInfoList getAllInterfacesOrAnnotationClasses(final Collection<ClassInfo> classes, final ScanSpec scanSpec) {
        return new ClassInfoList(filterClassInfo(classes, scanSpec, true, ClassType.INTERFACE_OR_ANNOTATION), true);
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    static String getSimpleName(final String className) {
        return className.substring(className.lastIndexOf(46) + 1);
    }
    
    public String getSimpleName() {
        return getSimpleName(this.name);
    }
    
    public ModuleInfo getModuleInfo() {
        return this.moduleInfo;
    }
    
    public PackageInfo getPackageInfo() {
        return this.packageInfo;
    }
    
    public String getPackageName() {
        return PackageInfo.getParentPackageName(this.name);
    }
    
    public boolean isExternalClass() {
        return this.isExternalClass;
    }
    
    public int getClassfileMinorVersion() {
        return this.classfileMinorVersion;
    }
    
    public int getClassfileMajorVersion() {
        return this.classfileMajorVersion;
    }
    
    public int getModifiers() {
        return this.modifiers;
    }
    
    public String getModifiersStr() {
        final StringBuilder buf = new StringBuilder();
        TypeUtils.modifiersToString(this.modifiers, TypeUtils.ModifierType.CLASS, false, buf);
        return buf.toString();
    }
    
    public boolean isPublic() {
        return (this.modifiers & 0x1) != 0x0;
    }
    
    public boolean isAbstract() {
        return (this.modifiers & 0x400) != 0x0;
    }
    
    public boolean isSynthetic() {
        return (this.modifiers & 0x1000) != 0x0;
    }
    
    public boolean isFinal() {
        return (this.modifiers & 0x10) != 0x0;
    }
    
    public boolean isStatic() {
        return Modifier.isStatic(this.modifiers);
    }
    
    public boolean isAnnotation() {
        return (this.modifiers & 0x2000) != 0x0;
    }
    
    public boolean isInterface() {
        return this.isInterfaceOrAnnotation() && !this.isAnnotation();
    }
    
    public boolean isInterfaceOrAnnotation() {
        return (this.modifiers & 0x200) != 0x0;
    }
    
    public boolean isEnum() {
        return (this.modifiers & 0x4000) != 0x0;
    }
    
    public boolean isRecord() {
        return this.isRecord;
    }
    
    public boolean isStandardClass() {
        return !this.isAnnotation() && !this.isInterface();
    }
    
    public boolean isArrayClass() {
        return this instanceof ArrayClassInfo;
    }
    
    public boolean extendsSuperclass(final String superclassName) {
        return this.getSuperclasses().containsName(superclassName);
    }
    
    public boolean isInnerClass() {
        return !this.getOuterClasses().isEmpty();
    }
    
    public boolean isOuterClass() {
        return !this.getInnerClasses().isEmpty();
    }
    
    public boolean isAnonymousInnerClass() {
        return this.fullyQualifiedDefiningMethodName != null;
    }
    
    public boolean isImplementedInterface() {
        return this.relatedClasses.get(RelType.CLASSES_IMPLEMENTING) != null || this.isInterface();
    }
    
    public boolean implementsInterface(final String interfaceName) {
        return this.getInterfaces().containsName(interfaceName);
    }
    
    public boolean hasAnnotation(final String annotationName) {
        return this.getAnnotations().containsName(annotationName);
    }
    
    public boolean hasDeclaredField(final String fieldName) {
        return this.getDeclaredFieldInfo().containsName(fieldName);
    }
    
    public boolean hasField(final String fieldName) {
        for (final ClassInfo ci : this.getOverrideOrder()) {
            if (ci.hasDeclaredField(fieldName)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasDeclaredFieldAnnotation(final String fieldAnnotationName) {
        for (final FieldInfo fi : this.getDeclaredFieldInfo()) {
            if (fi.hasAnnotation(fieldAnnotationName)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasFieldAnnotation(final String fieldAnnotationName) {
        for (final ClassInfo ci : this.getOverrideOrder()) {
            if (ci.hasDeclaredFieldAnnotation(fieldAnnotationName)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasDeclaredMethod(final String methodName) {
        return this.getDeclaredMethodInfo().containsName(methodName);
    }
    
    public boolean hasMethod(final String methodName) {
        for (final ClassInfo ci : this.getOverrideOrder()) {
            if (ci.hasDeclaredMethod(methodName)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasDeclaredMethodAnnotation(final String methodAnnotationName) {
        for (final MethodInfo mi : this.getDeclaredMethodInfo()) {
            if (mi.hasAnnotation(methodAnnotationName)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasMethodAnnotation(final String methodAnnotationName) {
        for (final ClassInfo ci : this.getOverrideOrder()) {
            if (ci.hasDeclaredMethodAnnotation(methodAnnotationName)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasDeclaredMethodParameterAnnotation(final String methodParameterAnnotationName) {
        for (final MethodInfo mi : this.getDeclaredMethodInfo()) {
            if (mi.hasParameterAnnotation(methodParameterAnnotationName)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasMethodParameterAnnotation(final String methodParameterAnnotationName) {
        for (final ClassInfo ci : this.getOverrideOrder()) {
            if (ci.hasDeclaredMethodParameterAnnotation(methodParameterAnnotationName)) {
                return true;
            }
        }
        return false;
    }
    
    private List<ClassInfo> getOverrideOrder(final Set<ClassInfo> visited, final List<ClassInfo> overrideOrderOut) {
        if (visited.add(this)) {
            overrideOrderOut.add(this);
            for (final ClassInfo iface : this.getInterfaces()) {
                iface.getOverrideOrder(visited, overrideOrderOut);
            }
            final ClassInfo superclass = this.getSuperclass();
            if (superclass != null) {
                superclass.getOverrideOrder(visited, overrideOrderOut);
            }
        }
        return overrideOrderOut;
    }
    
    private List<ClassInfo> getOverrideOrder() {
        if (this.overrideOrder == null) {
            this.overrideOrder = this.getOverrideOrder(new HashSet<ClassInfo>(), new ArrayList<ClassInfo>());
        }
        return this.overrideOrder;
    }
    
    public ClassInfoList getSubclasses() {
        if (this.getName().equals("java.lang.Object")) {
            return this.scanResult.getAllClasses();
        }
        return new ClassInfoList(this.filterClassInfo(RelType.SUBCLASSES, !this.isExternalClass, new ClassType[0]), true);
    }
    
    public ClassInfoList getSuperclasses() {
        return new ClassInfoList(this.filterClassInfo(RelType.SUPERCLASSES, false, new ClassType[0]), false);
    }
    
    public ClassInfo getSuperclass() {
        final Set<ClassInfo> superClasses = this.relatedClasses.get(RelType.SUPERCLASSES);
        if (superClasses == null || superClasses.isEmpty()) {
            return null;
        }
        if (superClasses.size() > 2) {
            throw new IllegalArgumentException("More than one superclass: " + superClasses);
        }
        final ClassInfo superclass = superClasses.iterator().next();
        if (superclass.getName().equals("java.lang.Object")) {
            return null;
        }
        return superclass;
    }
    
    public ClassInfoList getOuterClasses() {
        return new ClassInfoList(this.filterClassInfo(RelType.CONTAINED_WITHIN_OUTER_CLASS, false, new ClassType[0]), false);
    }
    
    public ClassInfoList getInnerClasses() {
        return new ClassInfoList(this.filterClassInfo(RelType.CONTAINS_INNER_CLASS, false, new ClassType[0]), true);
    }
    
    public String getFullyQualifiedDefiningMethodName() {
        return this.fullyQualifiedDefiningMethodName;
    }
    
    public ClassInfoList getInterfaces() {
        final ReachableAndDirectlyRelatedClasses implementedInterfaces = this.filterClassInfo(RelType.IMPLEMENTED_INTERFACES, false, new ClassType[0]);
        final Set<ClassInfo> allInterfaces = new LinkedHashSet<ClassInfo>(implementedInterfaces.reachableClasses);
        for (final ClassInfo superclass : this.filterClassInfo(RelType.SUPERCLASSES, false, new ClassType[0]).reachableClasses) {
            final Set<ClassInfo> superclassImplementedInterfaces = superclass.filterClassInfo(RelType.IMPLEMENTED_INTERFACES, false, new ClassType[0]).reachableClasses;
            allInterfaces.addAll(superclassImplementedInterfaces);
        }
        return new ClassInfoList(allInterfaces, implementedInterfaces.directlyRelatedClasses, true);
    }
    
    public ClassInfoList getClassesImplementing() {
        if (!this.isInterface()) {
            throw new IllegalArgumentException("Class is not an interface: " + this.getName());
        }
        final ReachableAndDirectlyRelatedClasses implementingClasses = this.filterClassInfo(RelType.CLASSES_IMPLEMENTING, !this.isExternalClass, new ClassType[0]);
        final Set<ClassInfo> allImplementingClasses = new LinkedHashSet<ClassInfo>(implementingClasses.reachableClasses);
        for (final ClassInfo implementingClass : implementingClasses.reachableClasses) {
            final Set<ClassInfo> implementingSubclasses = implementingClass.filterClassInfo(RelType.SUBCLASSES, !implementingClass.isExternalClass, new ClassType[0]).reachableClasses;
            allImplementingClasses.addAll(implementingSubclasses);
        }
        return new ClassInfoList(allImplementingClasses, implementingClasses.directlyRelatedClasses, true);
    }
    
    public ClassInfoList getAnnotations() {
        if (!this.scanResult.scanSpec.enableAnnotationInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableAnnotationInfo() before #scan()");
        }
        final ReachableAndDirectlyRelatedClasses annotationClasses = this.filterClassInfo(RelType.CLASS_ANNOTATIONS, false, new ClassType[0]);
        Set<ClassInfo> inheritedSuperclassAnnotations = null;
        for (final ClassInfo superclass : this.getSuperclasses()) {
            for (final ClassInfo superclassAnnotation : superclass.filterClassInfo(RelType.CLASS_ANNOTATIONS, false, new ClassType[0]).reachableClasses) {
                if (superclassAnnotation != null && superclassAnnotation.isInherited) {
                    if (inheritedSuperclassAnnotations == null) {
                        inheritedSuperclassAnnotations = new LinkedHashSet<ClassInfo>();
                    }
                    inheritedSuperclassAnnotations.add(superclassAnnotation);
                }
            }
        }
        if (inheritedSuperclassAnnotations == null) {
            return new ClassInfoList(annotationClasses, true);
        }
        inheritedSuperclassAnnotations.addAll(annotationClasses.reachableClasses);
        return new ClassInfoList(inheritedSuperclassAnnotations, annotationClasses.directlyRelatedClasses, true);
    }
    
    private ClassInfoList getFieldOrMethodAnnotations(final RelType relType) {
        final boolean isField = relType == RelType.FIELD_ANNOTATIONS;
        if (isField) {
            if (!this.scanResult.scanSpec.enableFieldInfo) {
                throw new IllegalArgumentException("Please call ClassGraph#enable" + (isField ? "Field" : "Method") + "Info() and #enableAnnotationInfo() before #scan()");
            }
        }
        else if (!this.scanResult.scanSpec.enableMethodInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enable" + (isField ? "Field" : "Method") + "Info() and #enableAnnotationInfo() before #scan()");
        }
        if (this.scanResult.scanSpec.enableAnnotationInfo) {
            final ReachableAndDirectlyRelatedClasses fieldOrMethodAnnotations = this.filterClassInfo(relType, false, ClassType.ANNOTATION);
            final Set<ClassInfo> fieldOrMethodAnnotationsAndMetaAnnotations = new LinkedHashSet<ClassInfo>(fieldOrMethodAnnotations.reachableClasses);
            return new ClassInfoList(fieldOrMethodAnnotationsAndMetaAnnotations, fieldOrMethodAnnotations.directlyRelatedClasses, true);
        }
        throw new IllegalArgumentException("Please call ClassGraph#enable" + (isField ? "Field" : "Method") + "Info() and #enableAnnotationInfo() before #scan()");
    }
    
    private ClassInfoList getClassesWithFieldOrMethodAnnotation(final RelType relType) {
        final boolean isField = relType == RelType.CLASSES_WITH_FIELD_ANNOTATION || relType == RelType.CLASSES_WITH_NONPRIVATE_FIELD_ANNOTATION;
        if (isField) {
            if (!this.scanResult.scanSpec.enableFieldInfo) {
                throw new IllegalArgumentException("Please call ClassGraph#enable" + (isField ? "Field" : "Method") + "Info() and #enableAnnotationInfo() before #scan()");
            }
        }
        else if (!this.scanResult.scanSpec.enableMethodInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enable" + (isField ? "Field" : "Method") + "Info() and #enableAnnotationInfo() before #scan()");
        }
        if (this.scanResult.scanSpec.enableAnnotationInfo) {
            final ReachableAndDirectlyRelatedClasses classesWithDirectlyAnnotatedFieldsOrMethods = this.filterClassInfo(relType, !this.isExternalClass, new ClassType[0]);
            final ReachableAndDirectlyRelatedClasses annotationsWithThisMetaAnnotation = this.filterClassInfo(RelType.CLASSES_WITH_ANNOTATION, !this.isExternalClass, ClassType.ANNOTATION);
            if (annotationsWithThisMetaAnnotation.reachableClasses.isEmpty()) {
                return new ClassInfoList(classesWithDirectlyAnnotatedFieldsOrMethods, true);
            }
            final Set<ClassInfo> allClassesWithAnnotatedOrMetaAnnotatedFieldsOrMethods = new LinkedHashSet<ClassInfo>(classesWithDirectlyAnnotatedFieldsOrMethods.reachableClasses);
            for (final ClassInfo metaAnnotatedAnnotation : annotationsWithThisMetaAnnotation.reachableClasses) {
                allClassesWithAnnotatedOrMetaAnnotatedFieldsOrMethods.addAll(metaAnnotatedAnnotation.filterClassInfo(relType, !metaAnnotatedAnnotation.isExternalClass, new ClassType[0]).reachableClasses);
            }
            return new ClassInfoList(allClassesWithAnnotatedOrMetaAnnotatedFieldsOrMethods, classesWithDirectlyAnnotatedFieldsOrMethods.directlyRelatedClasses, true);
        }
        throw new IllegalArgumentException("Please call ClassGraph#enable" + (isField ? "Field" : "Method") + "Info() and #enableAnnotationInfo() before #scan()");
    }
    
    public AnnotationInfoList getAnnotationInfo() {
        if (!this.scanResult.scanSpec.enableAnnotationInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableAnnotationInfo() before #scan()");
        }
        return AnnotationInfoList.getIndirectAnnotations(this.annotationInfo, this);
    }
    
    public AnnotationInfo getAnnotationInfo(final String annotationName) {
        return this.getAnnotationInfo().get(annotationName);
    }
    
    public AnnotationInfoList getAnnotationInfoRepeatable(final String annotationName) {
        return this.getAnnotationInfo().getRepeatable(annotationName);
    }
    
    public AnnotationParameterValueList getAnnotationDefaultParameterValues() {
        if (!this.scanResult.scanSpec.enableAnnotationInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableAnnotationInfo() before #scan()");
        }
        if (!this.isAnnotation()) {
            throw new IllegalArgumentException("Class is not an annotation: " + this.getName());
        }
        if (this.annotationDefaultParamValues == null) {
            return AnnotationParameterValueList.EMPTY_LIST;
        }
        if (!this.annotationDefaultParamValuesHasBeenConvertedToPrimitive) {
            this.annotationDefaultParamValues.convertWrapperArraysToPrimitiveArrays(this);
            this.annotationDefaultParamValuesHasBeenConvertedToPrimitive = true;
        }
        return this.annotationDefaultParamValues;
    }
    
    public ClassInfoList getClassesWithAnnotation() {
        if (!this.scanResult.scanSpec.enableAnnotationInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableAnnotationInfo() before #scan()");
        }
        if (!this.isAnnotation()) {
            throw new IllegalArgumentException("Class is not an annotation: " + this.getName());
        }
        final ReachableAndDirectlyRelatedClasses classesWithAnnotation = this.filterClassInfo(RelType.CLASSES_WITH_ANNOTATION, !this.isExternalClass, new ClassType[0]);
        if (this.isInherited) {
            final Set<ClassInfo> classesWithAnnotationAndTheirSubclasses = new LinkedHashSet<ClassInfo>(classesWithAnnotation.reachableClasses);
            for (final ClassInfo classWithAnnotation : classesWithAnnotation.reachableClasses) {
                classesWithAnnotationAndTheirSubclasses.addAll(classWithAnnotation.getSubclasses());
            }
            return new ClassInfoList(classesWithAnnotationAndTheirSubclasses, classesWithAnnotation.directlyRelatedClasses, true);
        }
        return new ClassInfoList(classesWithAnnotation, true);
    }
    
    ClassInfoList getClassesWithAnnotationDirectOnly() {
        return new ClassInfoList(this.filterClassInfo(RelType.CLASSES_WITH_ANNOTATION, !this.isExternalClass, new ClassType[0]), true);
    }
    
    private MethodInfoList getDeclaredMethodInfo(final String methodName, final boolean getNormalMethods, final boolean getConstructorMethods, final boolean getStaticInitializerMethods) {
        if (!this.scanResult.scanSpec.enableMethodInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableMethodInfo() before #scan()");
        }
        if (this.methodInfo == null) {
            return MethodInfoList.EMPTY_LIST;
        }
        if (methodName == null) {
            final MethodInfoList methodInfoList = new MethodInfoList();
            for (final MethodInfo mi : this.methodInfo) {
                final String miName = mi.getName();
                final boolean isConstructor = "<init>".equals(miName);
                final boolean isStaticInitializer = "<clinit>".equals(miName);
                if ((isConstructor && getConstructorMethods) || (isStaticInitializer && getStaticInitializerMethods) || (!isConstructor && !isStaticInitializer && getNormalMethods)) {
                    methodInfoList.add(mi);
                }
            }
            return methodInfoList;
        }
        boolean hasMethodWithName = false;
        for (final MethodInfo f : this.methodInfo) {
            if (f.getName().equals(methodName)) {
                hasMethodWithName = true;
                break;
            }
        }
        if (!hasMethodWithName) {
            return MethodInfoList.EMPTY_LIST;
        }
        final MethodInfoList methodInfoList2 = new MethodInfoList();
        for (final MethodInfo mi2 : this.methodInfo) {
            if (mi2.getName().equals(methodName)) {
                methodInfoList2.add(mi2);
            }
        }
        return methodInfoList2;
    }
    
    private MethodInfoList getMethodInfo(final String methodName, final boolean getNormalMethods, final boolean getConstructorMethods, final boolean getStaticInitializerMethods) {
        if (!this.scanResult.scanSpec.enableMethodInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableMethodInfo() before #scan()");
        }
        final MethodInfoList methodInfoList = new MethodInfoList();
        final Set<Map.Entry<String, String>> nameAndTypeDescriptorSet = new HashSet<Map.Entry<String, String>>();
        for (final ClassInfo ci : this.getOverrideOrder()) {
            for (final MethodInfo mi : ci.getDeclaredMethodInfo(methodName, getNormalMethods, getConstructorMethods, getStaticInitializerMethods)) {
                if (nameAndTypeDescriptorSet.add(new AbstractMap.SimpleEntry<String, String>(mi.getName(), mi.getTypeDescriptor().toString()))) {
                    methodInfoList.add(mi);
                }
            }
        }
        return methodInfoList;
    }
    
    public MethodInfoList getDeclaredMethodInfo() {
        return this.getDeclaredMethodInfo(null, true, false, false);
    }
    
    public MethodInfoList getMethodInfo() {
        return this.getMethodInfo(null, true, false, false);
    }
    
    public MethodInfoList getDeclaredConstructorInfo() {
        return this.getDeclaredMethodInfo(null, false, true, false);
    }
    
    public MethodInfoList getConstructorInfo() {
        return this.getMethodInfo(null, false, true, false);
    }
    
    public MethodInfoList getDeclaredMethodAndConstructorInfo() {
        return this.getDeclaredMethodInfo(null, true, true, false);
    }
    
    public MethodInfoList getMethodAndConstructorInfo() {
        return this.getMethodInfo(null, true, true, false);
    }
    
    public MethodInfoList getDeclaredMethodInfo(final String methodName) {
        return this.getDeclaredMethodInfo(methodName, false, false, false);
    }
    
    public MethodInfoList getMethodInfo(final String methodName) {
        return this.getMethodInfo(methodName, false, false, false);
    }
    
    public ClassInfoList getMethodAnnotations() {
        return this.getFieldOrMethodAnnotations(RelType.METHOD_ANNOTATIONS);
    }
    
    public ClassInfoList getMethodParameterAnnotations() {
        return this.getFieldOrMethodAnnotations(RelType.METHOD_PARAMETER_ANNOTATIONS);
    }
    
    public ClassInfoList getClassesWithMethodAnnotation() {
        final Set<ClassInfo> classesWithMethodAnnotation = new HashSet<ClassInfo>(this.getClassesWithFieldOrMethodAnnotation(RelType.CLASSES_WITH_METHOD_ANNOTATION));
        for (final ClassInfo classWithNonprivateMethodAnnotationOrMetaAnnotation : this.getClassesWithFieldOrMethodAnnotation(RelType.CLASSES_WITH_NONPRIVATE_METHOD_ANNOTATION)) {
            classesWithMethodAnnotation.addAll(classWithNonprivateMethodAnnotationOrMetaAnnotation.getSubclasses());
        }
        return new ClassInfoList(classesWithMethodAnnotation, new HashSet<ClassInfo>(this.getClassesWithMethodAnnotationDirectOnly()), true);
    }
    
    public ClassInfoList getClassesWithMethodParameterAnnotation() {
        final Set<ClassInfo> classesWithMethodParameterAnnotation = new HashSet<ClassInfo>(this.getClassesWithFieldOrMethodAnnotation(RelType.CLASSES_WITH_METHOD_PARAMETER_ANNOTATION));
        for (final ClassInfo classWithNonprivateMethodParameterAnnotationOrMetaAnnotation : this.getClassesWithFieldOrMethodAnnotation(RelType.CLASSES_WITH_NONPRIVATE_METHOD_PARAMETER_ANNOTATION)) {
            classesWithMethodParameterAnnotation.addAll(classWithNonprivateMethodParameterAnnotationOrMetaAnnotation.getSubclasses());
        }
        return new ClassInfoList(classesWithMethodParameterAnnotation, new HashSet<ClassInfo>(this.getClassesWithMethodParameterAnnotationDirectOnly()), true);
    }
    
    ClassInfoList getClassesWithMethodAnnotationDirectOnly() {
        return new ClassInfoList(this.filterClassInfo(RelType.CLASSES_WITH_METHOD_ANNOTATION, !this.isExternalClass, new ClassType[0]), true);
    }
    
    ClassInfoList getClassesWithMethodParameterAnnotationDirectOnly() {
        return new ClassInfoList(this.filterClassInfo(RelType.CLASSES_WITH_METHOD_PARAMETER_ANNOTATION, !this.isExternalClass, new ClassType[0]), true);
    }
    
    public FieldInfoList getDeclaredFieldInfo() {
        if (!this.scanResult.scanSpec.enableFieldInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableFieldInfo() before #scan()");
        }
        return (this.fieldInfo == null) ? FieldInfoList.EMPTY_LIST : this.fieldInfo;
    }
    
    public FieldInfoList getFieldInfo() {
        if (!this.scanResult.scanSpec.enableFieldInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableFieldInfo() before #scan()");
        }
        final FieldInfoList fieldInfoList = new FieldInfoList();
        final Set<String> fieldNameSet = new HashSet<String>();
        for (final ClassInfo ci : this.getOverrideOrder()) {
            for (final FieldInfo fi : ci.getDeclaredFieldInfo()) {
                if (fieldNameSet.add(fi.getName())) {
                    fieldInfoList.add(fi);
                }
            }
        }
        return fieldInfoList;
    }
    
    public FieldInfo getDeclaredFieldInfo(final String fieldName) {
        if (!this.scanResult.scanSpec.enableFieldInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableFieldInfo() before #scan()");
        }
        if (this.fieldInfo == null) {
            return null;
        }
        for (final FieldInfo fi : this.fieldInfo) {
            if (fi.getName().equals(fieldName)) {
                return fi;
            }
        }
        return null;
    }
    
    public FieldInfo getFieldInfo(final String fieldName) {
        if (!this.scanResult.scanSpec.enableFieldInfo) {
            throw new IllegalArgumentException("Please call ClassGraph#enableFieldInfo() before #scan()");
        }
        for (final ClassInfo ci : this.getOverrideOrder()) {
            final FieldInfo fi = ci.getDeclaredFieldInfo(fieldName);
            if (fi != null) {
                return fi;
            }
        }
        return null;
    }
    
    public ClassInfoList getFieldAnnotations() {
        return this.getFieldOrMethodAnnotations(RelType.FIELD_ANNOTATIONS);
    }
    
    public ClassInfoList getClassesWithFieldAnnotation() {
        final Set<ClassInfo> classesWithMethodAnnotation = new HashSet<ClassInfo>(this.getClassesWithFieldOrMethodAnnotation(RelType.CLASSES_WITH_FIELD_ANNOTATION));
        for (final ClassInfo classWithNonprivateMethodAnnotationOrMetaAnnotation : this.getClassesWithFieldOrMethodAnnotation(RelType.CLASSES_WITH_NONPRIVATE_FIELD_ANNOTATION)) {
            classesWithMethodAnnotation.addAll(classWithNonprivateMethodAnnotationOrMetaAnnotation.getSubclasses());
        }
        return new ClassInfoList(classesWithMethodAnnotation, new HashSet<ClassInfo>(this.getClassesWithMethodAnnotationDirectOnly()), true);
    }
    
    ClassInfoList getClassesWithFieldAnnotationDirectOnly() {
        return new ClassInfoList(this.filterClassInfo(RelType.CLASSES_WITH_FIELD_ANNOTATION, !this.isExternalClass, new ClassType[0]), true);
    }
    
    public ClassTypeSignature getTypeSignature() {
        if (this.typeSignatureStr == null) {
            return null;
        }
        if (this.typeSignature == null) {
            try {
                (this.typeSignature = ClassTypeSignature.parse(this.typeSignatureStr, this)).setScanResult(this.scanResult);
            }
            catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return this.typeSignature;
    }
    
    public String getTypeSignatureStr() {
        return this.typeSignatureStr;
    }
    
    public URI getClasspathElementURI() {
        if (this.classpathElement == null) {
            throw new IllegalArgumentException("Classpath element is not known for this classpath element");
        }
        return this.classpathElement.getURI();
    }
    
    public URL getClasspathElementURL() {
        if (this.classpathElement == null) {
            throw new IllegalArgumentException("Classpath element is not known for this classpath element");
        }
        try {
            return this.classpathElement.getURI().toURL();
        }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not get classpath element URL", e);
        }
    }
    
    public File getClasspathElementFile() {
        if (this.classpathElement == null) {
            throw new IllegalArgumentException("Classpath element is not known for this classpath element");
        }
        return this.classpathElement.getFile();
    }
    
    public ModuleRef getModuleRef() {
        if (this.classpathElement == null) {
            throw new IllegalArgumentException("Classpath element is not known for this classpath element");
        }
        return (this.classpathElement instanceof ClasspathElementModule) ? ((ClasspathElementModule)this.classpathElement).getModuleRef() : null;
    }
    
    public Resource getResource() {
        return this.classfileResource;
    }
    
    public <T> Class<T> loadClass(final Class<T> superclassOrInterfaceType, final boolean ignoreExceptions) {
        return super.loadClass(superclassOrInterfaceType, ignoreExceptions);
    }
    
    public <T> Class<T> loadClass(final Class<T> superclassOrInterfaceType) {
        return super.loadClass(superclassOrInterfaceType, false);
    }
    
    public Class<?> loadClass(final boolean ignoreExceptions) {
        return super.loadClass(ignoreExceptions);
    }
    
    public Class<?> loadClass() {
        return super.loadClass(false);
    }
    
    @Override
    protected String getClassName() {
        return this.name;
    }
    
    protected ClassInfo getClassInfo() {
        return this;
    }
    
    @Override
    void setScanResult(final ScanResult scanResult) {
        super.setScanResult(scanResult);
        if (this.typeSignature != null) {
            this.typeSignature.setScanResult(scanResult);
        }
        if (this.annotationInfo != null) {
            for (final AnnotationInfo ai : this.annotationInfo) {
                ai.setScanResult(scanResult);
            }
        }
        if (this.fieldInfo != null) {
            for (final FieldInfo fi : this.fieldInfo) {
                fi.setScanResult(scanResult);
            }
        }
        if (this.methodInfo != null) {
            for (final MethodInfo mi : this.methodInfo) {
                mi.setScanResult(scanResult);
            }
        }
        if (this.annotationDefaultParamValues != null) {
            for (final AnnotationParameterValue apv : this.annotationDefaultParamValues) {
                apv.setScanResult(scanResult);
            }
        }
    }
    
    void handleRepeatableAnnotations(final Set<String> allRepeatableAnnotationNames) {
        if (this.annotationInfo != null) {
            this.annotationInfo.handleRepeatableAnnotations(allRepeatableAnnotationNames, this, RelType.CLASS_ANNOTATIONS, RelType.CLASSES_WITH_ANNOTATION, null);
        }
        if (this.fieldInfo != null) {
            for (final FieldInfo fi : this.fieldInfo) {
                fi.handleRepeatableAnnotations(allRepeatableAnnotationNames);
            }
        }
        if (this.methodInfo != null) {
            for (final MethodInfo mi : this.methodInfo) {
                mi.handleRepeatableAnnotations(allRepeatableAnnotationNames);
            }
        }
    }
    
    void addReferencedClassNames(final Set<String> refdClassNames) {
        if (this.referencedClassNames == null) {
            this.referencedClassNames = refdClassNames;
        }
        else {
            this.referencedClassNames.addAll(refdClassNames);
        }
    }
    
    @Override
    protected void findReferencedClassInfo(final Map<String, ClassInfo> classNameToClassInfo, final Set<ClassInfo> refdClassInfo) {
        super.findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
        if (this.referencedClassNames != null) {
            for (final String refdClassName : this.referencedClassNames) {
                final ClassInfo classInfo = getOrCreateClassInfo(refdClassName, classNameToClassInfo);
                classInfo.setScanResult(this.scanResult);
                refdClassInfo.add(classInfo);
            }
        }
        this.getMethodInfo().findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
        this.getFieldInfo().findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
        this.getAnnotationInfo().findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
        if (this.annotationDefaultParamValues != null) {
            this.annotationDefaultParamValues.findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
        }
        final ClassTypeSignature classSig = this.getTypeSignature();
        if (classSig != null) {
            classSig.findReferencedClassInfo(classNameToClassInfo, refdClassInfo);
        }
    }
    
    void setReferencedClasses(final ClassInfoList refdClasses) {
        this.referencedClasses = refdClasses;
    }
    
    public ClassInfoList getClassDependencies() {
        if (!this.scanResult.scanSpec.enableInterClassDependencies) {
            throw new IllegalArgumentException("Please call ClassGraph#enableInterClassDependencies() before #scan()");
        }
        return (this.referencedClasses == null) ? ClassInfoList.EMPTY_LIST : this.referencedClasses;
    }
    
    @Override
    public int compareTo(final ClassInfo o) {
        return this.name.compareTo(o.name);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ClassInfo)) {
            return false;
        }
        final ClassInfo other = (ClassInfo)obj;
        return this.name.equals(other.name);
    }
    
    @Override
    public int hashCode() {
        return (this.name == null) ? 0 : this.name.hashCode();
    }
    
    protected String toString(final boolean typeNameOnly) {
        final ClassTypeSignature typeSig = this.getTypeSignature();
        if (typeSig != null) {
            return typeSig.toString(this.name, typeNameOnly, this.modifiers, this.isAnnotation(), this.isInterface());
        }
        final StringBuilder buf = new StringBuilder();
        if (typeNameOnly) {
            buf.append(this.name);
        }
        else {
            TypeUtils.modifiersToString(this.modifiers, TypeUtils.ModifierType.CLASS, false, buf);
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append(this.isRecord() ? "record " : (this.isEnum() ? "enum " : (this.isAnnotation() ? "@interface " : (this.isInterface() ? "interface " : "class "))));
            buf.append(this.name);
            final ClassInfo superclass = this.getSuperclass();
            if (superclass != null && !superclass.getName().equals("java.lang.Object")) {
                buf.append(" extends ").append(superclass.toString(true));
            }
            final Set<ClassInfo> interfaces = this.filterClassInfo(RelType.IMPLEMENTED_INTERFACES, false, new ClassType[0]).directlyRelatedClasses;
            if (!interfaces.isEmpty()) {
                buf.append(this.isInterface() ? " extends " : " implements ");
                boolean first = true;
                for (final ClassInfo iface : interfaces) {
                    if (first) {
                        first = false;
                    }
                    else {
                        buf.append(", ");
                    }
                    buf.append(iface.toString(true));
                }
            }
        }
        return buf.toString();
    }
    
    @Override
    public String toString() {
        return this.toString(false);
    }
    
    static {
        NO_REACHABLE_CLASSES = new ReachableAndDirectlyRelatedClasses((Set)Collections.emptySet(), (Set)Collections.emptySet());
    }
    
    enum RelType
    {
        SUPERCLASSES, 
        SUBCLASSES, 
        CONTAINS_INNER_CLASS, 
        CONTAINED_WITHIN_OUTER_CLASS, 
        IMPLEMENTED_INTERFACES, 
        CLASSES_IMPLEMENTING, 
        CLASS_ANNOTATIONS, 
        CLASSES_WITH_ANNOTATION, 
        METHOD_ANNOTATIONS, 
        CLASSES_WITH_METHOD_ANNOTATION, 
        CLASSES_WITH_NONPRIVATE_METHOD_ANNOTATION, 
        METHOD_PARAMETER_ANNOTATIONS, 
        CLASSES_WITH_METHOD_PARAMETER_ANNOTATION, 
        CLASSES_WITH_NONPRIVATE_METHOD_PARAMETER_ANNOTATION, 
        FIELD_ANNOTATIONS, 
        CLASSES_WITH_FIELD_ANNOTATION, 
        CLASSES_WITH_NONPRIVATE_FIELD_ANNOTATION;
    }
    
    private enum ClassType
    {
        ALL, 
        STANDARD_CLASS, 
        IMPLEMENTED_INTERFACE, 
        ANNOTATION, 
        INTERFACE_OR_ANNOTATION, 
        ENUM, 
        RECORD;
    }
    
    static class ReachableAndDirectlyRelatedClasses
    {
        final Set<ClassInfo> reachableClasses;
        final Set<ClassInfo> directlyRelatedClasses;
        
        private ReachableAndDirectlyRelatedClasses(final Set<ClassInfo> reachableClasses, final Set<ClassInfo> directlyRelatedClasses) {
            this.reachableClasses = reachableClasses;
            this.directlyRelatedClasses = directlyRelatedClasses;
        }
    }
}
