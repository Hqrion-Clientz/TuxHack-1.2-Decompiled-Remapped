// 
// Decompiled by Procyon v0.5.36
// 

package io.github.classgraph;

import nonapi.io.github.classgraph.utils.Join;
import java.io.File;
import java.util.Iterator;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import nonapi.io.github.classgraph.utils.JarUtils;
import nonapi.io.github.classgraph.utils.ReflectionUtils;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ModulePathInfo
{
    public final Set<String> modulePath;
    public final Set<String> addModules;
    public final Set<String> patchModules;
    public final Set<String> addExports;
    public final Set<String> addOpens;
    public final Set<String> addReads;
    private final List<Set<String>> fields;
    private static final List<String> argSwitches;
    private static final List<Character> argPartSeparatorChars;
    
    public ModulePathInfo() {
        this.modulePath = new LinkedHashSet<String>();
        this.addModules = new LinkedHashSet<String>();
        this.patchModules = new LinkedHashSet<String>();
        this.addExports = new LinkedHashSet<String>();
        this.addOpens = new LinkedHashSet<String>();
        this.addReads = new LinkedHashSet<String>();
        this.fields = Arrays.asList(this.modulePath, this.addModules, this.patchModules, this.addExports, this.addOpens, this.addReads);
        final Class<?> managementFactory = ReflectionUtils.classForNameOrNull("java.lang.management.ManagementFactory");
        final Object runtimeMXBean = (managementFactory == null) ? null : ReflectionUtils.invokeStaticMethod(managementFactory, "getRuntimeMXBean", false);
        final List<String> commandlineArguments = (List<String>)((runtimeMXBean == null) ? null : ((List)ReflectionUtils.invokeMethod(runtimeMXBean, "getInputArguments", false)));
        if (commandlineArguments != null) {
            for (final String arg : commandlineArguments) {
                for (int i = 0; i < this.fields.size(); ++i) {
                    final String argSwitch = ModulePathInfo.argSwitches.get(i);
                    if (arg.startsWith(argSwitch)) {
                        final String argParam = arg.substring(argSwitch.length());
                        final Set<String> argField = this.fields.get(i);
                        final char sepChar = ModulePathInfo.argPartSeparatorChars.get(i);
                        if (sepChar == '\0') {
                            argField.add(argParam);
                        }
                        else {
                            for (final String argPart : JarUtils.smartPathSplit(argParam, sepChar, null)) {
                                argField.add(argPart);
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(1024);
        if (!this.modulePath.isEmpty()) {
            buf.append("--module-path=");
            buf.append(Join.join(File.pathSeparator, this.modulePath));
        }
        if (!this.addModules.isEmpty()) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append("--add-modules=");
            buf.append(Join.join(",", this.addModules));
        }
        for (final String patchModulesEntry : this.patchModules) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append("--patch-module=");
            buf.append(patchModulesEntry);
        }
        for (final String addExportsEntry : this.addExports) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append("--add-exports=");
            buf.append(addExportsEntry);
        }
        for (final String addOpensEntry : this.addOpens) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append("--add-opens=");
            buf.append(addOpensEntry);
        }
        for (final String addReadsEntry : this.addReads) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append("--add-reads=");
            buf.append(addReadsEntry);
        }
        return buf.toString();
    }
    
    static {
        argSwitches = Arrays.asList("--module-path=", "--add-modules=", "--patch-module=", "--add-exports=", "--add-opens=", "--add-reads=");
        argPartSeparatorChars = Arrays.asList(File.pathSeparatorChar, ',', '\0', '\0', '\0', '\0');
    }
}
