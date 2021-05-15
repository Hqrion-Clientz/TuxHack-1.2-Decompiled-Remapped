// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import nonapi.io.github.classgraph.utils.CollectionUtils;
import java.util.Collection;
import nonapi.io.github.classgraph.utils.Join;
import nonapi.io.github.classgraph.concurrency.WorkQueue;
import java.lang.reflect.Modifier;
import nonapi.io.github.classgraph.types.ParseException;
import java.util.HashSet;
import java.util.Arrays;
import java.io.IOException;
import java.util.Map;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.ArrayList;
import nonapi.io.github.classgraph.utils.JarUtils;
import nonapi.io.github.classgraph.utils.LogNode;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import nonapi.io.github.classgraph.fileslice.reader.ClassfileReader;

class Classfile
{
    private ClassfileReader reader;
    private final ClasspathElement classpathElement;
    private final List<ClasspathElement> classpathOrder;
    private final String relativePath;
    private final Resource classfileResource;
    private final ConcurrentHashMap<String, String> stringInternMap;
    private String className;
    private int minorVersion;
    private int majorVersion;
    private final boolean isExternalClass;
    private int classModifiers;
    private boolean isInterface;
    private boolean isRecord;
    private boolean isAnnotation;
    private String superclassName;
    private List<String> implementedInterfaces;
    private AnnotationInfoList classAnnotations;
    private String fullyQualifiedDefiningMethodName;
    private List<ClassContainment> classContainmentEntries;
    private AnnotationParameterValueList annotationParamDefaultValues;
    private Set<String> refdClassNames;
    private FieldInfoList fieldInfoList;
    private MethodInfoList methodInfoList;
    private String typeSignature;
    private final Set<String> acceptedClassNamesFound;
    private final Set<String> classNamesScheduledForExtendedScanning;
    private List<Scanner.ClassfileScanWorkUnit> additionalWorkUnits;
    private final ScanSpec scanSpec;
    private int cpCount;
    private int[] entryOffset;
    private int[] entryTag;
    private int[] indirectStringRefs;
    private static final AnnotationInfo[] NO_ANNOTATIONS;
    
    private void scheduleScanningIfExternalClass(final String className, final String relationship, final LogNode log) {
        if (className != null && !className.equals("java.lang.Object") && !this.acceptedClassNamesFound.contains(className) && this.classNamesScheduledForExtendedScanning.add(className)) {
            if (this.scanSpec.classAcceptReject.isRejected(className)) {
                if (log != null) {
                    log.log("Cannot extend scanning upwards to external " + relationship + " " + className + ", since it is rejected");
                }
            }
            else {
                final String classfilePath = JarUtils.classNameToClassfilePath(className);
                Resource classResource = this.classpathElement.getResource(classfilePath);
                ClasspathElement foundInClasspathElt = null;
                if (classResource != null) {
                    foundInClasspathElt = this.classpathElement;
                }
                else {
                    for (final ClasspathElement classpathOrderElt : this.classpathOrder) {
                        if (classpathOrderElt != this.classpathElement) {
                            classResource = classpathOrderElt.getResource(classfilePath);
                            if (classResource != null) {
                                foundInClasspathElt = classpathOrderElt;
                                break;
                            }
                            continue;
                        }
                    }
                }
                if (classResource != null) {
                    if (log != null) {
                        classResource.scanLog = log.log("Extending scanning to external " + relationship + ((foundInClasspathElt == this.classpathElement) ? " in same classpath element" : (" in classpath element " + foundInClasspathElt)) + ": " + className);
                    }
                    if (this.additionalWorkUnits == null) {
                        this.additionalWorkUnits = new ArrayList<Scanner.ClassfileScanWorkUnit>();
                    }
                    this.additionalWorkUnits.add(new Scanner.ClassfileScanWorkUnit(foundInClasspathElt, classResource, true));
                }
                else if (log != null) {
                    log.log("External " + relationship + " " + className + " was not found in non-rejected packages -- cannot extend scanning to this class");
                }
            }
        }
    }
    
    private void extendScanningUpwardsFromAnnotationParameterValues(final Object annotationParamVal, final LogNode log) {
        if (annotationParamVal != null) {
            if (annotationParamVal instanceof AnnotationInfo) {
                final AnnotationInfo annotationInfo = (AnnotationInfo)annotationParamVal;
                this.scheduleScanningIfExternalClass(annotationInfo.getClassName(), "annotation class", log);
                for (final AnnotationParameterValue apv : annotationInfo.getParameterValues()) {
                    this.extendScanningUpwardsFromAnnotationParameterValues(apv.getValue(), log);
                }
            }
            else if (annotationParamVal instanceof AnnotationEnumValue) {
                this.scheduleScanningIfExternalClass(((AnnotationEnumValue)annotationParamVal).getClassName(), "enum class", log);
            }
            else if (annotationParamVal instanceof AnnotationClassRef) {
                this.scheduleScanningIfExternalClass(((AnnotationClassRef)annotationParamVal).getClassName(), "class ref", log);
            }
            else if (annotationParamVal.getClass().isArray()) {
                for (int i = 0, n = Array.getLength(annotationParamVal); i < n; ++i) {
                    this.extendScanningUpwardsFromAnnotationParameterValues(Array.get(annotationParamVal, i), log);
                }
            }
        }
    }
    
    private void extendScanningUpwards(final LogNode log) {
        if (this.superclassName != null) {
            this.scheduleScanningIfExternalClass(this.superclassName, "superclass", log);
        }
        if (this.implementedInterfaces != null) {
            for (final String interfaceName : this.implementedInterfaces) {
                this.scheduleScanningIfExternalClass(interfaceName, "interface", log);
            }
        }
        if (this.classAnnotations != null) {
            for (final AnnotationInfo annotationInfo : this.classAnnotations) {
                this.scheduleScanningIfExternalClass(annotationInfo.getName(), "class annotation", log);
                this.extendScanningUpwardsFromAnnotationParameterValues(annotationInfo, log);
            }
        }
        if (this.annotationParamDefaultValues != null) {
            for (final AnnotationParameterValue apv : this.annotationParamDefaultValues) {
                this.extendScanningUpwardsFromAnnotationParameterValues(apv.getValue(), log);
            }
        }
        if (this.methodInfoList != null) {
            for (final MethodInfo methodInfo : this.methodInfoList) {
                if (methodInfo.annotationInfo != null) {
                    for (final AnnotationInfo methodAnnotationInfo : methodInfo.annotationInfo) {
                        this.scheduleScanningIfExternalClass(methodAnnotationInfo.getName(), "method annotation", log);
                        this.extendScanningUpwardsFromAnnotationParameterValues(methodAnnotationInfo, log);
                    }
                    if (methodInfo.parameterAnnotationInfo == null || methodInfo.parameterAnnotationInfo.length <= 0) {
                        continue;
                    }
                    for (final AnnotationInfo[] paramAnnInfoArr : methodInfo.parameterAnnotationInfo) {
                        if (paramAnnInfoArr != null && paramAnnInfoArr.length > 0) {
                            for (final AnnotationInfo paramAnnInfo : paramAnnInfoArr) {
                                this.scheduleScanningIfExternalClass(paramAnnInfo.getName(), "method parameter annotation", log);
                                this.extendScanningUpwardsFromAnnotationParameterValues(paramAnnInfo, log);
                            }
                        }
                    }
                }
            }
        }
        if (this.fieldInfoList != null) {
            for (final FieldInfo fieldInfo : this.fieldInfoList) {
                if (fieldInfo.annotationInfo != null) {
                    for (final AnnotationInfo fieldAnnotationInfo : fieldInfo.annotationInfo) {
                        this.scheduleScanningIfExternalClass(fieldAnnotationInfo.getName(), "field annotation", log);
                        this.extendScanningUpwardsFromAnnotationParameterValues(fieldAnnotationInfo, log);
                    }
                }
            }
        }
        if (this.classContainmentEntries != null) {
            for (final ClassContainment classContainmentEntry : this.classContainmentEntries) {
                if (classContainmentEntry.innerClassName.equals(this.className)) {
                    this.scheduleScanningIfExternalClass(classContainmentEntry.outerClassName, "outer class", log);
                }
            }
        }
    }
    
    void link(final Map<String, ClassInfo> classNameToClassInfo, final Map<String, PackageInfo> packageNameToPackageInfo, final Map<String, ModuleInfo> moduleNameToModuleInfo) {
        boolean isModuleDescriptor = false;
        boolean isPackageDescriptor = false;
        ClassInfo classInfo = null;
        if (this.className.equals("module-info")) {
            isModuleDescriptor = true;
        }
        else if (this.className.equals("package-info") || this.className.endsWith(".package-info")) {
            isPackageDescriptor = true;
        }
        else {
            classInfo = ClassInfo.addScannedClass(this.className, this.classModifiers, this.isExternalClass, classNameToClassInfo, this.classpathElement, this.classfileResource);
            classInfo.setClassfileVersion(this.minorVersion, this.majorVersion);
            classInfo.setModifiers(this.classModifiers);
            classInfo.setIsInterface(this.isInterface);
            classInfo.setIsAnnotation(this.isAnnotation);
            classInfo.setIsRecord(this.isRecord);
            if (this.superclassName != null) {
                classInfo.addSuperclass(this.superclassName, classNameToClassInfo);
            }
            if (this.implementedInterfaces != null) {
                for (final String interfaceName : this.implementedInterfaces) {
                    classInfo.addImplementedInterface(interfaceName, classNameToClassInfo);
                }
            }
            if (this.classAnnotations != null) {
                for (final AnnotationInfo classAnnotation : this.classAnnotations) {
                    classInfo.addClassAnnotation(classAnnotation, classNameToClassInfo);
                }
            }
            if (this.classContainmentEntries != null) {
                ClassInfo.addClassContainment(this.classContainmentEntries, classNameToClassInfo);
            }
            if (this.annotationParamDefaultValues != null) {
                classInfo.addAnnotationParamDefaultValues(this.annotationParamDefaultValues);
            }
            if (this.fullyQualifiedDefiningMethodName != null) {
                classInfo.addFullyQualifiedDefiningMethodName(this.fullyQualifiedDefiningMethodName);
            }
            if (this.fieldInfoList != null) {
                classInfo.addFieldInfo(this.fieldInfoList, classNameToClassInfo);
            }
            if (this.methodInfoList != null) {
                classInfo.addMethodInfo(this.methodInfoList, classNameToClassInfo);
            }
            if (this.typeSignature != null) {
                classInfo.setTypeSignature(this.typeSignature);
            }
            if (this.refdClassNames != null) {
                classInfo.addReferencedClassNames(this.refdClassNames);
            }
        }
        PackageInfo packageInfo = null;
        if (!isModuleDescriptor) {
            final String packageName = PackageInfo.getParentPackageName(this.className);
            packageInfo = PackageInfo.getOrCreatePackage(packageName, packageNameToPackageInfo);
            if (isPackageDescriptor) {
                packageInfo.addAnnotations(this.classAnnotations);
            }
            else if (classInfo != null) {
                packageInfo.addClassInfo(classInfo);
                classInfo.packageInfo = packageInfo;
            }
        }
        final String moduleName = this.classpathElement.getModuleName();
        if (moduleName != null) {
            ModuleInfo moduleInfo = moduleNameToModuleInfo.get(moduleName);
            if (moduleInfo == null) {
                moduleNameToModuleInfo.put(moduleName, moduleInfo = new ModuleInfo(this.classfileResource.getModuleRef(), this.classpathElement));
            }
            if (isModuleDescriptor) {
                moduleInfo.addAnnotations(this.classAnnotations);
            }
            if (classInfo != null) {
                moduleInfo.addClassInfo(classInfo);
                classInfo.moduleInfo = moduleInfo;
            }
            if (packageInfo != null) {
                moduleInfo.addPackageInfo(packageInfo);
            }
        }
    }
    
    private String intern(final String str) {
        if (str == null) {
            return null;
        }
        final String interned = this.stringInternMap.putIfAbsent(str, str);
        if (interned != null) {
            return interned;
        }
        return str;
    }
    
    private int getConstantPoolStringOffset(final int cpIdx, final int subFieldIdx) throws ClassfileFormatException {
        if (cpIdx < 1 || cpIdx >= this.cpCount) {
            throw new ClassfileFormatException("Constant pool index " + cpIdx + ", should be in range [1, " + (this.cpCount - 1) + "] -- cannot continue reading class. Please report this at https://github.com/classgraph/classgraph/issues");
        }
        final int t = this.entryTag[cpIdx];
        if ((t != 12 && subFieldIdx != 0) || (t == 12 && subFieldIdx != 0 && subFieldIdx != 1)) {
            throw new ClassfileFormatException("Bad subfield index " + subFieldIdx + " for tag " + t + ", cannot continue reading class. Please report this at https://github.com/classgraph/classgraph/issues");
        }
        if (t == 0) {
            return 0;
        }
        int cpIdxToUse;
        if (t == 1) {
            cpIdxToUse = cpIdx;
        }
        else if (t == 7 || t == 8 || t == 19) {
            final int indirIdx = this.indirectStringRefs[cpIdx];
            if (indirIdx == -1) {
                throw new ClassfileFormatException("Bad string indirection index, cannot continue reading class. Please report this at https://github.com/classgraph/classgraph/issues");
            }
            if (indirIdx == 0) {
                return 0;
            }
            cpIdxToUse = indirIdx;
        }
        else {
            if (t != 12) {
                throw new ClassfileFormatException("Wrong tag number " + t + " at constant pool index " + cpIdx + ", cannot continue reading class. Please report this at https://github.com/classgraph/classgraph/issues");
            }
            final int compoundIndirIdx = this.indirectStringRefs[cpIdx];
            if (compoundIndirIdx == -1) {
                throw new ClassfileFormatException("Bad string indirection index, cannot continue reading class. Please report this at https://github.com/classgraph/classgraph/issues");
            }
            final int indirIdx2 = ((subFieldIdx == 0) ? (compoundIndirIdx >> 16) : compoundIndirIdx) & 0xFFFF;
            if (indirIdx2 == 0) {
                throw new ClassfileFormatException("Bad string indirection index, cannot continue reading class. Please report this at https://github.com/classgraph/classgraph/issues");
            }
            cpIdxToUse = indirIdx2;
        }
        if (cpIdxToUse < 1 || cpIdxToUse >= this.cpCount) {
            throw new ClassfileFormatException("Constant pool index " + cpIdx + ", should be in range [1, " + (this.cpCount - 1) + "] -- cannot continue reading class. Please report this at https://github.com/classgraph/classgraph/issues");
        }
        return this.entryOffset[cpIdxToUse];
    }
    
    private String getConstantPoolString(final int cpIdx, final boolean replaceSlashWithDot, final boolean stripLSemicolon) throws ClassfileFormatException, IOException {
        final int constantPoolStringOffset = this.getConstantPoolStringOffset(cpIdx, 0);
        if (constantPoolStringOffset == 0) {
            return null;
        }
        final int utfLen = this.reader.readUnsignedShort(constantPoolStringOffset);
        if (utfLen == 0) {
            return "";
        }
        return this.intern(this.reader.readString(constantPoolStringOffset + 2L, utfLen, replaceSlashWithDot, stripLSemicolon));
    }
    
    private String getConstantPoolString(final int cpIdx, final int subFieldIdx) throws ClassfileFormatException, IOException {
        final int constantPoolStringOffset = this.getConstantPoolStringOffset(cpIdx, subFieldIdx);
        if (constantPoolStringOffset == 0) {
            return null;
        }
        final int utfLen = this.reader.readUnsignedShort(constantPoolStringOffset);
        if (utfLen == 0) {
            return "";
        }
        return this.intern(this.reader.readString(constantPoolStringOffset + 2L, utfLen, false, false));
    }
    
    private String getConstantPoolString(final int cpIdx) throws ClassfileFormatException, IOException {
        return this.getConstantPoolString(cpIdx, 0);
    }
    
    private byte getConstantPoolStringFirstByte(final int cpIdx) throws ClassfileFormatException, IOException {
        final int constantPoolStringOffset = this.getConstantPoolStringOffset(cpIdx, 0);
        if (constantPoolStringOffset == 0) {
            return 0;
        }
        final int utfLen = this.reader.readUnsignedShort(constantPoolStringOffset);
        if (utfLen == 0) {
            return 0;
        }
        return this.reader.readByte(constantPoolStringOffset + 2L);
    }
    
    private String getConstantPoolClassName(final int cpIdx) throws ClassfileFormatException, IOException {
        return this.getConstantPoolString(cpIdx, true, false);
    }
    
    private String getConstantPoolClassDescriptor(final int cpIdx) throws ClassfileFormatException, IOException {
        return this.getConstantPoolString(cpIdx, true, true);
    }
    
    private boolean constantPoolStringEquals(final int cpIdx, final String asciiStr) throws ClassfileFormatException, IOException {
        final int cpStrOffset = this.getConstantPoolStringOffset(cpIdx, 0);
        if (cpStrOffset == 0) {
            return asciiStr == null;
        }
        if (asciiStr == null) {
            return false;
        }
        final int cpStrLen = this.reader.readUnsignedShort(cpStrOffset);
        final int asciiStrLen = asciiStr.length();
        if (cpStrLen != asciiStrLen) {
            return false;
        }
        final int cpStrStart = cpStrOffset + 2;
        this.reader.bufferTo(cpStrStart + cpStrLen);
        final byte[] buf = this.reader.buf();
        for (int i = 0; i < cpStrLen; ++i) {
            if ((char)(buf[cpStrStart + i] & 0xFF) != asciiStr.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    
    private int cpReadUnsignedShort(final int cpIdx) throws IOException {
        if (cpIdx < 1 || cpIdx >= this.cpCount) {
            throw new ClassfileFormatException("Constant pool index " + cpIdx + ", should be in range [1, " + (this.cpCount - 1) + "] -- cannot continue reading class. Please report this at https://github.com/classgraph/classgraph/issues");
        }
        return this.reader.readUnsignedShort(this.entryOffset[cpIdx]);
    }
    
    private int cpReadInt(final int cpIdx) throws IOException {
        if (cpIdx < 1 || cpIdx >= this.cpCount) {
            throw new ClassfileFormatException("Constant pool index " + cpIdx + ", should be in range [1, " + (this.cpCount - 1) + "] -- cannot continue reading class. Please report this at https://github.com/classgraph/classgraph/issues");
        }
        return this.reader.readInt(this.entryOffset[cpIdx]);
    }
    
    private long cpReadLong(final int cpIdx) throws IOException {
        if (cpIdx < 1 || cpIdx >= this.cpCount) {
            throw new ClassfileFormatException("Constant pool index " + cpIdx + ", should be in range [1, " + (this.cpCount - 1) + "] -- cannot continue reading class. Please report this at https://github.com/classgraph/classgraph/issues");
        }
        return this.reader.readLong(this.entryOffset[cpIdx]);
    }
    
    private Object getFieldConstantPoolValue(final int tag, final char fieldTypeDescriptorFirstChar, final int cpIdx) throws ClassfileFormatException, IOException {
        switch (tag) {
            case 1:
            case 7:
            case 8: {
                return this.getConstantPoolString(cpIdx);
            }
            case 3: {
                final int intVal = this.cpReadInt(cpIdx);
                switch (fieldTypeDescriptorFirstChar) {
                    case 'I': {
                        return intVal;
                    }
                    case 'S': {
                        return (short)intVal;
                    }
                    case 'C': {
                        return (char)intVal;
                    }
                    case 'B': {
                        return (byte)intVal;
                    }
                    case 'Z': {
                        return intVal != 0;
                    }
                    default: {
                        throw new ClassfileFormatException("Unknown Constant_INTEGER type " + fieldTypeDescriptorFirstChar + ", cannot continue reading class. Please report this at https://github.com/classgraph/classgraph/issues");
                    }
                }
                break;
            }
            case 4: {
                return Float.intBitsToFloat(this.cpReadInt(cpIdx));
            }
            case 5: {
                return this.cpReadLong(cpIdx);
            }
            case 6: {
                return Double.longBitsToDouble(this.cpReadLong(cpIdx));
            }
            default: {
                throw new ClassfileFormatException("Unknown constant pool tag " + tag + ", cannot continue reading class. Please report this at https://github.com/classgraph/classgraph/issues");
            }
        }
    }
    
    private AnnotationInfo readAnnotation() throws IOException {
        final String annotationClassName = this.getConstantPoolClassDescriptor(this.reader.readUnsignedShort());
        final int numElementValuePairs = this.reader.readUnsignedShort();
        AnnotationParameterValueList paramVals = null;
        if (numElementValuePairs > 0) {
            paramVals = new AnnotationParameterValueList(numElementValuePairs);
            for (int i = 0; i < numElementValuePairs; ++i) {
                final String paramName = this.getConstantPoolString(this.reader.readUnsignedShort());
                final Object paramValue = this.readAnnotationElementValue();
                paramVals.add(new AnnotationParameterValue(paramName, paramValue));
            }
        }
        return new AnnotationInfo(annotationClassName, paramVals);
    }
    
    private Object readAnnotationElementValue() throws IOException {
        final int tag = (char)this.reader.readUnsignedByte();
        switch (tag) {
            case 66: {
                return (byte)this.cpReadInt(this.reader.readUnsignedShort());
            }
            case 67: {
                return (char)this.cpReadInt(this.reader.readUnsignedShort());
            }
            case 68: {
                return Double.longBitsToDouble(this.cpReadLong(this.reader.readUnsignedShort()));
            }
            case 70: {
                return Float.intBitsToFloat(this.cpReadInt(this.reader.readUnsignedShort()));
            }
            case 73: {
                return this.cpReadInt(this.reader.readUnsignedShort());
            }
            case 74: {
                return this.cpReadLong(this.reader.readUnsignedShort());
            }
            case 83: {
                return (short)this.cpReadUnsignedShort(this.reader.readUnsignedShort());
            }
            case 90: {
                return this.cpReadInt(this.reader.readUnsignedShort()) != 0;
            }
            case 115: {
                return this.getConstantPoolString(this.reader.readUnsignedShort());
            }
            case 101: {
                final String annotationClassName = this.getConstantPoolClassDescriptor(this.reader.readUnsignedShort());
                final String annotationConstName = this.getConstantPoolString(this.reader.readUnsignedShort());
                return new AnnotationEnumValue(annotationClassName, annotationConstName);
            }
            case 99: {
                final String classRefTypeDescriptor = this.getConstantPoolString(this.reader.readUnsignedShort());
                return new AnnotationClassRef(classRefTypeDescriptor);
            }
            case 64: {
                return this.readAnnotation();
            }
            case 91: {
                final int count = this.reader.readUnsignedShort();
                final Object[] arr = new Object[count];
                for (int i = 0; i < count; ++i) {
                    arr[i] = this.readAnnotationElementValue();
                }
                return arr;
            }
            default: {
                throw new ClassfileFormatException("Class " + this.className + " has unknown annotation element type tag '" + (char)tag + "': element size unknown, cannot continue reading class. Please report this at https://github.com/classgraph/classgraph/issues");
            }
        }
    }
    
    private void readConstantPoolEntries() throws IOException {
        List<Integer> classNameCpIdxs = null;
        List<Integer> typeSignatureIdxs = null;
        if (this.scanSpec.enableInterClassDependencies) {
            classNameCpIdxs = new ArrayList<Integer>();
            typeSignatureIdxs = new ArrayList<Integer>();
        }
        this.cpCount = this.reader.readUnsignedShort();
        this.entryOffset = new int[this.cpCount];
        this.entryTag = new int[this.cpCount];
        Arrays.fill(this.indirectStringRefs = new int[this.cpCount], 0, this.cpCount, -1);
        int i = 1;
        int skipSlot = 0;
        while (i < this.cpCount) {
            if (skipSlot == 1) {
                skipSlot = 0;
            }
            else {
                this.entryTag[i] = this.reader.readUnsignedByte();
                this.entryOffset[i] = this.reader.currPos();
                switch (this.entryTag[i]) {
                    case 0: {
                        throw new ClassfileFormatException("Unknown constant pool tag 0 in classfile " + this.relativePath + " (possible buffer underflow issue). Please report this at https://github.com/classgraph/classgraph/issues");
                    }
                    case 1: {
                        final int strLen = this.reader.readUnsignedShort();
                        this.reader.skip(strLen);
                        break;
                    }
                    case 3:
                    case 4: {
                        this.reader.skip(4);
                        break;
                    }
                    case 5:
                    case 6: {
                        this.reader.skip(8);
                        skipSlot = 1;
                        break;
                    }
                    case 7: {
                        this.indirectStringRefs[i] = this.reader.readUnsignedShort();
                        if (classNameCpIdxs != null) {
                            classNameCpIdxs.add(this.indirectStringRefs[i]);
                            break;
                        }
                        break;
                    }
                    case 8: {
                        this.indirectStringRefs[i] = this.reader.readUnsignedShort();
                        break;
                    }
                    case 9: {
                        this.reader.skip(4);
                        break;
                    }
                    case 10: {
                        this.reader.skip(4);
                        break;
                    }
                    case 11: {
                        this.reader.skip(4);
                        break;
                    }
                    case 12: {
                        final int nameRef = this.reader.readUnsignedShort();
                        final int typeRef = this.reader.readUnsignedShort();
                        if (typeSignatureIdxs != null) {
                            typeSignatureIdxs.add(typeRef);
                        }
                        this.indirectStringRefs[i] = (nameRef << 16 | typeRef);
                        break;
                    }
                    case 15: {
                        this.reader.skip(3);
                        break;
                    }
                    case 16: {
                        this.reader.skip(2);
                        break;
                    }
                    case 18: {
                        this.reader.skip(4);
                        break;
                    }
                    case 19: {
                        this.indirectStringRefs[i] = this.reader.readUnsignedShort();
                        break;
                    }
                    case 20: {
                        this.reader.skip(2);
                        break;
                    }
                    default: {
                        throw new ClassfileFormatException("Unknown constant pool tag " + this.entryTag[i] + " (element size unknown, cannot continue reading class). Please report this at https://github.com/classgraph/classgraph/issues");
                    }
                }
            }
            ++i;
        }
        if (classNameCpIdxs != null) {
            this.refdClassNames = new HashSet<String>();
            for (final int cpIdx : classNameCpIdxs) {
                final String refdClassName = this.getConstantPoolString(cpIdx, true, false);
                if (refdClassName != null) {
                    if (refdClassName.startsWith("[")) {
                        try {
                            final TypeSignature typeSig = TypeSignature.parse(refdClassName.replace('.', '/'), null);
                            typeSig.findReferencedClassNames(this.refdClassNames);
                            continue;
                        }
                        catch (ParseException e) {
                            throw new ClassfileFormatException("Could not parse class name: " + refdClassName, e);
                        }
                    }
                    this.refdClassNames.add(refdClassName);
                }
            }
        }
        if (typeSignatureIdxs != null) {
            for (final int cpIdx : typeSignatureIdxs) {
                final String typeSigStr = this.getConstantPoolString(cpIdx);
                if (typeSigStr != null) {
                    try {
                        if (typeSigStr.indexOf(40) >= 0 || "<init>".equals(typeSigStr)) {
                            final MethodTypeSignature typeSig2 = MethodTypeSignature.parse(typeSigStr, null);
                            typeSig2.findReferencedClassNames(this.refdClassNames);
                        }
                        else {
                            final TypeSignature typeSig = TypeSignature.parse(typeSigStr, null);
                            typeSig.findReferencedClassNames(this.refdClassNames);
                        }
                    }
                    catch (ParseException e) {
                        throw new ClassfileFormatException("Could not parse type signature: " + typeSigStr, e);
                    }
                }
            }
        }
    }
    
    private void readBasicClassInfo() throws IOException, ClassfileFormatException, SkipClassException {
        this.classModifiers = this.reader.readUnsignedShort();
        this.isInterface = ((this.classModifiers & 0x200) != 0x0);
        this.isAnnotation = ((this.classModifiers & 0x2000) != 0x0);
        final String classNamePath = this.getConstantPoolString(this.reader.readUnsignedShort());
        if (classNamePath == null) {
            throw new ClassfileFormatException("Class name is null");
        }
        this.className = classNamePath.replace('/', '.');
        if ("java.lang.Object".equals(this.className)) {
            throw new SkipClassException("No need to scan java.lang.Object");
        }
        final boolean isModule = (this.classModifiers & 0x8000) != 0x0;
        final boolean isPackage = this.relativePath.regionMatches(this.relativePath.lastIndexOf(47) + 1, "package-info.class", 0, 18);
        if (!this.scanSpec.ignoreClassVisibility && !Modifier.isPublic(this.classModifiers) && !isModule && !isPackage) {
            throw new SkipClassException("Class is not public, and ignoreClassVisibility() was not called");
        }
        if (!this.relativePath.endsWith(".class")) {
            throw new SkipClassException("Classfile filename " + this.relativePath + " does not end in \".class\"");
        }
        final int len = classNamePath.length();
        if (this.relativePath.length() != len + 6 || !classNamePath.regionMatches(0, this.relativePath, 0, len)) {
            throw new SkipClassException("Relative path " + this.relativePath + " does not match class name " + this.className);
        }
        final int superclassNameCpIdx = this.reader.readUnsignedShort();
        if (superclassNameCpIdx > 0) {
            this.superclassName = this.getConstantPoolClassName(superclassNameCpIdx);
        }
    }
    
    private void readInterfaces() throws IOException {
        for (int interfaceCount = this.reader.readUnsignedShort(), i = 0; i < interfaceCount; ++i) {
            final String interfaceName = this.getConstantPoolClassName(this.reader.readUnsignedShort());
            if (this.implementedInterfaces == null) {
                this.implementedInterfaces = new ArrayList<String>();
            }
            this.implementedInterfaces.add(interfaceName);
        }
    }
    
    private void readFields() throws IOException, ClassfileFormatException {
        for (int fieldCount = this.reader.readUnsignedShort(), i = 0; i < fieldCount; ++i) {
            final int fieldModifierFlags = this.reader.readUnsignedShort();
            final boolean isPublicField = (fieldModifierFlags & 0x1) == 0x1;
            final boolean fieldIsVisible = isPublicField || this.scanSpec.ignoreFieldVisibility;
            final boolean getStaticFinalFieldConstValue = this.scanSpec.enableStaticFinalFieldConstantInitializerValues && fieldIsVisible;
            if (!fieldIsVisible || (!this.scanSpec.enableFieldInfo && !getStaticFinalFieldConstValue)) {
                this.reader.readUnsignedShort();
                this.reader.readUnsignedShort();
                for (int attributesCount = this.reader.readUnsignedShort(), j = 0; j < attributesCount; ++j) {
                    this.reader.readUnsignedShort();
                    final int attributeLength = this.reader.readInt();
                    this.reader.skip(attributeLength);
                }
            }
            else {
                final int fieldNameCpIdx = this.reader.readUnsignedShort();
                final String fieldName = this.getConstantPoolString(fieldNameCpIdx);
                final int fieldTypeDescriptorCpIdx = this.reader.readUnsignedShort();
                final char fieldTypeDescriptorFirstChar = (char)this.getConstantPoolStringFirstByte(fieldTypeDescriptorCpIdx);
                String fieldTypeSignature = null;
                final String fieldTypeDescriptor = this.getConstantPoolString(fieldTypeDescriptorCpIdx);
                Object fieldConstValue = null;
                AnnotationInfoList fieldAnnotationInfo = null;
                for (int attributesCount2 = this.reader.readUnsignedShort(), k = 0; k < attributesCount2; ++k) {
                    final int attributeNameCpIdx = this.reader.readUnsignedShort();
                    final int attributeLength2 = this.reader.readInt();
                    if (getStaticFinalFieldConstValue && this.constantPoolStringEquals(attributeNameCpIdx, "ConstantValue")) {
                        final int cpIdx = this.reader.readUnsignedShort();
                        if (cpIdx < 1 || cpIdx >= this.cpCount) {
                            throw new ClassfileFormatException("Constant pool index " + cpIdx + ", should be in range [1, " + (this.cpCount - 1) + "] -- cannot continue reading class. Please report this at https://github.com/classgraph/classgraph/issues");
                        }
                        fieldConstValue = this.getFieldConstantPoolValue(this.entryTag[cpIdx], fieldTypeDescriptorFirstChar, cpIdx);
                    }
                    else if (fieldIsVisible && this.constantPoolStringEquals(attributeNameCpIdx, "Signature")) {
                        fieldTypeSignature = this.getConstantPoolString(this.reader.readUnsignedShort());
                    }
                    else if (this.scanSpec.enableAnnotationInfo && (this.constantPoolStringEquals(attributeNameCpIdx, "RuntimeVisibleAnnotations") || (!this.scanSpec.disableRuntimeInvisibleAnnotations && this.constantPoolStringEquals(attributeNameCpIdx, "RuntimeInvisibleAnnotations")))) {
                        final int fieldAnnotationCount = this.reader.readUnsignedShort();
                        if (fieldAnnotationCount > 0) {
                            if (fieldAnnotationInfo == null) {
                                fieldAnnotationInfo = new AnnotationInfoList(1);
                            }
                            for (int l = 0; l < fieldAnnotationCount; ++l) {
                                final AnnotationInfo fieldAnnotation = this.readAnnotation();
                                fieldAnnotationInfo.add(fieldAnnotation);
                            }
                        }
                    }
                    else {
                        this.reader.skip(attributeLength2);
                    }
                }
                if (this.scanSpec.enableFieldInfo && fieldIsVisible) {
                    if (this.fieldInfoList == null) {
                        this.fieldInfoList = new FieldInfoList();
                    }
                    this.fieldInfoList.add(new FieldInfo(this.className, fieldName, fieldModifierFlags, fieldTypeDescriptor, fieldTypeSignature, fieldConstValue, fieldAnnotationInfo));
                }
            }
        }
    }
    
    private void readMethods() throws IOException, ClassfileFormatException {
        for (int methodCount = this.reader.readUnsignedShort(), i = 0; i < methodCount; ++i) {
            final int methodModifierFlags = this.reader.readUnsignedShort();
            final boolean isPublicMethod = (methodModifierFlags & 0x1) == 0x1;
            final boolean methodIsVisible = isPublicMethod || this.scanSpec.ignoreMethodVisibility;
            String methodName = null;
            String methodTypeDescriptor = null;
            String methodTypeSignature = null;
            final boolean enableMethodInfo = this.scanSpec.enableMethodInfo || this.isAnnotation;
            if (enableMethodInfo || this.isAnnotation) {
                final int methodNameCpIdx = this.reader.readUnsignedShort();
                methodName = this.getConstantPoolString(methodNameCpIdx);
                final int methodTypeDescriptorCpIdx = this.reader.readUnsignedShort();
                methodTypeDescriptor = this.getConstantPoolString(methodTypeDescriptorCpIdx);
            }
            else {
                this.reader.skip(4);
            }
            final int attributesCount = this.reader.readUnsignedShort();
            String[] methodParameterNames = null;
            int[] methodParameterModifiers = null;
            AnnotationInfo[][] methodParameterAnnotations = null;
            AnnotationInfoList methodAnnotationInfo = null;
            boolean methodHasBody = false;
            if (!methodIsVisible || (!enableMethodInfo && !this.isAnnotation)) {
                for (int j = 0; j < attributesCount; ++j) {
                    this.reader.skip(2);
                    final int attributeLength = this.reader.readInt();
                    this.reader.skip(attributeLength);
                }
            }
            else {
                for (int j = 0; j < attributesCount; ++j) {
                    final int attributeNameCpIdx = this.reader.readUnsignedShort();
                    final int attributeLength2 = this.reader.readInt();
                    if (this.scanSpec.enableAnnotationInfo && (this.constantPoolStringEquals(attributeNameCpIdx, "RuntimeVisibleAnnotations") || (!this.scanSpec.disableRuntimeInvisibleAnnotations && this.constantPoolStringEquals(attributeNameCpIdx, "RuntimeInvisibleAnnotations")))) {
                        final int methodAnnotationCount = this.reader.readUnsignedShort();
                        if (methodAnnotationCount > 0) {
                            if (methodAnnotationInfo == null) {
                                methodAnnotationInfo = new AnnotationInfoList(1);
                            }
                            for (int k = 0; k < methodAnnotationCount; ++k) {
                                final AnnotationInfo annotationInfo = this.readAnnotation();
                                methodAnnotationInfo.add(annotationInfo);
                            }
                        }
                    }
                    else if (this.scanSpec.enableAnnotationInfo && (this.constantPoolStringEquals(attributeNameCpIdx, "RuntimeVisibleParameterAnnotations") || (!this.scanSpec.disableRuntimeInvisibleAnnotations && this.constantPoolStringEquals(attributeNameCpIdx, "RuntimeInvisibleParameterAnnotations")))) {
                        final int numParams = this.reader.readUnsignedByte();
                        if (methodParameterAnnotations == null) {
                            methodParameterAnnotations = new AnnotationInfo[numParams][];
                        }
                        else if (methodParameterAnnotations.length != numParams) {
                            throw new ClassfileFormatException("Mismatch in number of parameters between RuntimeVisibleParameterAnnotations and RuntimeInvisibleParameterAnnotations");
                        }
                        for (int paramIdx = 0; paramIdx < numParams; ++paramIdx) {
                            final int numAnnotations = this.reader.readUnsignedShort();
                            if (numAnnotations > 0) {
                                int annStartIdx = 0;
                                if (methodParameterAnnotations[paramIdx] != null) {
                                    annStartIdx = methodParameterAnnotations[paramIdx].length;
                                    methodParameterAnnotations[paramIdx] = Arrays.copyOf(methodParameterAnnotations[paramIdx], annStartIdx + numAnnotations);
                                }
                                else {
                                    methodParameterAnnotations[paramIdx] = new AnnotationInfo[numAnnotations];
                                }
                                for (int annIdx = 0; annIdx < numAnnotations; ++annIdx) {
                                    methodParameterAnnotations[paramIdx][annStartIdx + annIdx] = this.readAnnotation();
                                }
                            }
                            else if (methodParameterAnnotations[paramIdx] == null) {
                                methodParameterAnnotations[paramIdx] = Classfile.NO_ANNOTATIONS;
                            }
                        }
                    }
                    else if (this.constantPoolStringEquals(attributeNameCpIdx, "MethodParameters")) {
                        final int paramCount = this.reader.readUnsignedByte();
                        methodParameterNames = new String[paramCount];
                        methodParameterModifiers = new int[paramCount];
                        for (int k = 0; k < paramCount; ++k) {
                            final int cpIdx = this.reader.readUnsignedShort();
                            methodParameterNames[k] = ((cpIdx == 0) ? null : this.getConstantPoolString(cpIdx));
                            methodParameterModifiers[k] = this.reader.readUnsignedShort();
                        }
                    }
                    else if (this.constantPoolStringEquals(attributeNameCpIdx, "Signature")) {
                        methodTypeSignature = this.getConstantPoolString(this.reader.readUnsignedShort());
                    }
                    else if (this.constantPoolStringEquals(attributeNameCpIdx, "AnnotationDefault")) {
                        if (this.annotationParamDefaultValues == null) {
                            this.annotationParamDefaultValues = new AnnotationParameterValueList();
                        }
                        this.annotationParamDefaultValues.add(new AnnotationParameterValue(methodName, this.readAnnotationElementValue()));
                    }
                    else if (this.constantPoolStringEquals(attributeNameCpIdx, "Code")) {
                        methodHasBody = true;
                        this.reader.skip(attributeLength2);
                    }
                    else {
                        this.reader.skip(attributeLength2);
                    }
                }
                if (enableMethodInfo) {
                    if (this.methodInfoList == null) {
                        this.methodInfoList = new MethodInfoList();
                    }
                    this.methodInfoList.add(new MethodInfo(this.className, methodName, methodAnnotationInfo, methodModifierFlags, methodTypeDescriptor, methodTypeSignature, methodParameterNames, methodParameterModifiers, methodParameterAnnotations, methodHasBody));
                }
            }
        }
    }
    
    private void readClassAttributes() throws IOException, ClassfileFormatException {
        for (int attributesCount = this.reader.readUnsignedShort(), i = 0; i < attributesCount; ++i) {
            final int attributeNameCpIdx = this.reader.readUnsignedShort();
            final int attributeLength = this.reader.readInt();
            if (this.scanSpec.enableAnnotationInfo && (this.constantPoolStringEquals(attributeNameCpIdx, "RuntimeVisibleAnnotations") || (!this.scanSpec.disableRuntimeInvisibleAnnotations && this.constantPoolStringEquals(attributeNameCpIdx, "RuntimeInvisibleAnnotations")))) {
                final int annotationCount = this.reader.readUnsignedShort();
                if (annotationCount > 0) {
                    if (this.classAnnotations == null) {
                        this.classAnnotations = new AnnotationInfoList();
                    }
                    for (int m = 0; m < annotationCount; ++m) {
                        this.classAnnotations.add(this.readAnnotation());
                    }
                }
            }
            else if (this.constantPoolStringEquals(attributeNameCpIdx, "Record")) {
                this.isRecord = true;
                this.reader.skip(attributeLength);
            }
            else if (this.constantPoolStringEquals(attributeNameCpIdx, "InnerClasses")) {
                for (int numInnerClasses = this.reader.readUnsignedShort(), j = 0; j < numInnerClasses; ++j) {
                    final int innerClassInfoCpIdx = this.reader.readUnsignedShort();
                    final int outerClassInfoCpIdx = this.reader.readUnsignedShort();
                    this.reader.skip(2);
                    final int innerClassAccessFlags = this.reader.readUnsignedShort();
                    if (innerClassInfoCpIdx != 0 && outerClassInfoCpIdx != 0) {
                        final String innerClassName = this.getConstantPoolClassName(innerClassInfoCpIdx);
                        final String outerClassName = this.getConstantPoolClassName(outerClassInfoCpIdx);
                        if (innerClassName == null || outerClassName == null) {
                            throw new ClassfileFormatException("Inner and/or outer class name is null");
                        }
                        if (innerClassName.equals(outerClassName)) {
                            throw new ClassfileFormatException("Inner and outer class name cannot be the same");
                        }
                        if (!"java.lang.invoke.MethodHandles$Lookup".equals(innerClassName) || !"java.lang.invoke.MethodHandles".equals(outerClassName)) {
                            if (this.classContainmentEntries == null) {
                                this.classContainmentEntries = new ArrayList<ClassContainment>();
                            }
                            this.classContainmentEntries.add(new ClassContainment(innerClassName, innerClassAccessFlags, outerClassName));
                        }
                    }
                }
            }
            else if (this.constantPoolStringEquals(attributeNameCpIdx, "Signature")) {
                this.typeSignature = this.getConstantPoolString(this.reader.readUnsignedShort());
            }
            else if (this.constantPoolStringEquals(attributeNameCpIdx, "EnclosingMethod")) {
                final String innermostEnclosingClassName = this.getConstantPoolClassName(this.reader.readUnsignedShort());
                final int enclosingMethodCpIdx = this.reader.readUnsignedShort();
                String definingMethodName;
                if (enclosingMethodCpIdx == 0) {
                    definingMethodName = "<clinit>";
                }
                else {
                    definingMethodName = this.getConstantPoolString(enclosingMethodCpIdx, 0);
                }
                if (this.classContainmentEntries == null) {
                    this.classContainmentEntries = new ArrayList<ClassContainment>();
                }
                this.classContainmentEntries.add(new ClassContainment(this.className, this.classModifiers, innermostEnclosingClassName));
                this.fullyQualifiedDefiningMethodName = innermostEnclosingClassName + "." + definingMethodName;
            }
            else if (this.constantPoolStringEquals(attributeNameCpIdx, "Module")) {
                final int moduleNameCpIdx = this.reader.readUnsignedShort();
                this.classpathElement.moduleNameFromModuleDescriptor = this.getConstantPoolString(moduleNameCpIdx);
                this.reader.skip(attributeLength - 2);
            }
            else {
                this.reader.skip(attributeLength);
            }
        }
    }
    
    Classfile(final ClasspathElement classpathElement, final List<ClasspathElement> classpathOrder, final Set<String> acceptedClassNamesFound, final Set<String> classNamesScheduledForExtendedScanning, final String relativePath, final Resource classfileResource, final boolean isExternalClass, final ConcurrentHashMap<String, String> stringInternMap, final WorkQueue<Scanner.ClassfileScanWorkUnit> workQueue, final ScanSpec scanSpec, final LogNode log) throws IOException, ClassfileFormatException, SkipClassException {
        this.classpathElement = classpathElement;
        this.classpathOrder = classpathOrder;
        this.relativePath = relativePath;
        this.acceptedClassNamesFound = acceptedClassNamesFound;
        this.classNamesScheduledForExtendedScanning = classNamesScheduledForExtendedScanning;
        this.classfileResource = classfileResource;
        this.isExternalClass = isExternalClass;
        this.stringInternMap = stringInternMap;
        this.scanSpec = scanSpec;
        try {
            this.reader = classfileResource.openClassfile();
            if (this.reader.readInt() != -889275714) {
                throw new ClassfileFormatException("Classfile does not have correct magic number");
            }
            this.minorVersion = this.reader.readUnsignedShort();
            this.majorVersion = this.reader.readUnsignedShort();
            this.readConstantPoolEntries();
            this.readBasicClassInfo();
            this.readInterfaces();
            this.readFields();
            this.readMethods();
            this.readClassAttributes();
        }
        finally {
            classfileResource.close();
            this.reader = null;
        }
        final LogNode subLog = (log == null) ? null : log.log("Found " + (this.isAnnotation ? "annotation class" : (this.isInterface ? "interface class" : "class")) + " " + this.className);
        if (subLog != null) {
            if (this.superclassName != null) {
                subLog.log("Super" + ((this.isInterface && !this.isAnnotation) ? "interface" : "class") + ": " + this.superclassName);
            }
            if (this.implementedInterfaces != null) {
                subLog.log("Interfaces: " + Join.join(", ", this.implementedInterfaces));
            }
            if (this.classAnnotations != null) {
                subLog.log("Class annotations: " + Join.join(", ", this.classAnnotations));
            }
            if (this.annotationParamDefaultValues != null) {
                for (final AnnotationParameterValue apv : this.annotationParamDefaultValues) {
                    subLog.log("Annotation default param value: " + apv);
                }
            }
            if (this.fieldInfoList != null) {
                for (final FieldInfo fieldInfo : this.fieldInfoList) {
                    subLog.log("Field: " + fieldInfo);
                }
            }
            if (this.methodInfoList != null) {
                for (final MethodInfo methodInfo : this.methodInfoList) {
                    subLog.log("Method: " + methodInfo);
                }
            }
            if (this.typeSignature != null) {
                ClassTypeSignature typeSig = null;
                try {
                    typeSig = ClassTypeSignature.parse(this.typeSignature, null);
                    if (this.refdClassNames != null) {
                        typeSig.findReferencedClassNames(this.refdClassNames);
                    }
                }
                catch (ParseException ex) {}
                subLog.log("Class type signature: " + ((typeSig == null) ? this.typeSignature : typeSig.toString(this.className, false, this.classModifiers, this.isAnnotation, this.isInterface)));
            }
            if (this.refdClassNames != null) {
                final List<String> refdClassNamesSorted = new ArrayList<String>(this.refdClassNames);
                CollectionUtils.sortIfNotEmpty(refdClassNamesSorted);
                subLog.log("Referenced class names: " + Join.join(", ", refdClassNamesSorted));
            }
        }
        if (scanSpec.extendScanningUpwardsToExternalClasses) {
            this.extendScanningUpwards(subLog);
            if (this.additionalWorkUnits != null) {
                workQueue.addWorkUnits(this.additionalWorkUnits);
            }
        }
    }
    
    static {
        NO_ANNOTATIONS = new AnnotationInfo[0];
    }
    
    static class ClassContainment
    {
        public final String innerClassName;
        public final int innerClassModifierBits;
        public final String outerClassName;
        
        public ClassContainment(final String innerClassName, final int innerClassModifierBits, final String outerClassName) {
            this.innerClassName = innerClassName;
            this.innerClassModifierBits = innerClassModifierBits;
            this.outerClassName = outerClassName;
        }
    }
    
    static class ClassfileFormatException extends IOException
    {
        static final long serialVersionUID = 1L;
        
        public ClassfileFormatException(final String message) {
            super(message);
        }
        
        public ClassfileFormatException(final String message, final Throwable cause) {
            super(message, cause);
        }
        
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
    
    static class SkipClassException extends IOException
    {
        static final long serialVersionUID = 1L;
        
        public SkipClassException(final String message) {
            super(message);
        }
        
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
