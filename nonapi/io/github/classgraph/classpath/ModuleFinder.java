// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.classpath;

import nonapi.io.github.classgraph.utils.CollectionUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ArrayDeque;
import java.util.Collections;
import nonapi.io.github.classgraph.utils.LogNode;
import nonapi.io.github.classgraph.scanspec.ScanSpec;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Collection;
import nonapi.io.github.classgraph.utils.ReflectionUtils;
import java.util.Deque;
import java.util.Set;
import io.github.classgraph.ModuleRef;
import java.util.List;

public class ModuleFinder
{
    private List<ModuleRef> systemModuleRefs;
    private List<ModuleRef> nonSystemModuleRefs;
    private boolean forceScanJavaClassPath;
    
    public List<ModuleRef> getSystemModuleRefs() {
        return this.systemModuleRefs;
    }
    
    public List<ModuleRef> getNonSystemModuleRefs() {
        return this.nonSystemModuleRefs;
    }
    
    public boolean forceScanJavaClassPath() {
        return this.forceScanJavaClassPath;
    }
    
    private static void findLayerOrder(final Object layer, final Set<Object> layerVisited, final Set<Object> parentLayers, final Deque<Object> layerOrderOut) {
        if (layerVisited.add(layer)) {
            final List<Object> parents = (List<Object>)ReflectionUtils.invokeMethod(layer, "parents", true);
            if (parents != null) {
                parentLayers.addAll(parents);
                for (final Object parent : parents) {
                    findLayerOrder(parent, layerVisited, parentLayers, layerOrderOut);
                }
            }
            layerOrderOut.push(layer);
        }
    }
    
    private static List<ModuleRef> findModuleRefs(final LinkedHashSet<Object> layers, final ScanSpec scanSpec, final LogNode log) {
        if (layers.isEmpty()) {
            return Collections.emptyList();
        }
        final Deque<Object> layerOrder = new ArrayDeque<Object>();
        final Set<Object> parentLayers = new HashSet<Object>();
        for (final Object layer : layers) {
            if (layer != null) {
                findLayerOrder(layer, new HashSet<Object>(), parentLayers, layerOrder);
            }
        }
        if (scanSpec.addedModuleLayers != null) {
            for (final Object layer : scanSpec.addedModuleLayers) {
                if (layer != null) {
                    findLayerOrder(layer, new HashSet<Object>(), parentLayers, layerOrder);
                }
            }
        }
        List<Object> layerOrderFinal;
        if (scanSpec.ignoreParentModuleLayers) {
            layerOrderFinal = new ArrayList<Object>();
            for (final Object layer2 : layerOrder) {
                if (!parentLayers.contains(layer2)) {
                    layerOrderFinal.add(layer2);
                }
            }
        }
        else {
            layerOrderFinal = new ArrayList<Object>(layerOrder);
        }
        final Set<Object> addedModules = new HashSet<Object>();
        final LinkedHashSet<ModuleRef> moduleRefOrder = new LinkedHashSet<ModuleRef>();
        for (final Object layer3 : layerOrderFinal) {
            final Object configuration = ReflectionUtils.invokeMethod(layer3, "configuration", true);
            if (configuration != null) {
                final Set<Object> modules = (Set<Object>)ReflectionUtils.invokeMethod(configuration, "modules", true);
                if (modules == null) {
                    continue;
                }
                final List<ModuleRef> modulesInLayer = new ArrayList<ModuleRef>();
                for (final Object module : modules) {
                    final Object moduleReference = ReflectionUtils.invokeMethod(module, "reference", true);
                    if (moduleReference != null && addedModules.add(moduleReference)) {
                        try {
                            modulesInLayer.add(new ModuleRef(moduleReference, layer3));
                        }
                        catch (IllegalArgumentException e) {
                            if (log == null) {
                                continue;
                            }
                            log.log("Exception while creating ModuleRef for module " + moduleReference, e);
                        }
                    }
                }
                CollectionUtils.sortIfNotEmpty(modulesInLayer);
                moduleRefOrder.addAll((Collection<?>)modulesInLayer);
            }
        }
        return new ArrayList<ModuleRef>(moduleRefOrder);
    }
    
    private List<ModuleRef> findModuleRefsFromCallstack(final Class<?>[] callStack, final ScanSpec scanSpec, final LogNode log) {
        final LinkedHashSet<Object> layers = new LinkedHashSet<Object>();
        if (callStack != null) {
            for (final Class<?> stackFrameClass : callStack) {
                final Object module = ReflectionUtils.invokeMethod(stackFrameClass, "getModule", false);
                if (module != null) {
                    final Object layer = ReflectionUtils.invokeMethod(module, "getLayer", true);
                    if (layer != null) {
                        layers.add(layer);
                    }
                    else {
                        this.forceScanJavaClassPath = true;
                    }
                }
            }
        }
        Class<?> moduleLayerClass = null;
        try {
            moduleLayerClass = Class.forName("java.lang.ModuleLayer");
        }
        catch (ClassNotFoundException ex) {}
        catch (LinkageError linkageError) {}
        if (moduleLayerClass != null) {
            final Object bootLayer = ReflectionUtils.invokeStaticMethod(moduleLayerClass, "boot", false);
            if (bootLayer != null) {
                layers.add(bootLayer);
            }
            else {
                this.forceScanJavaClassPath = true;
            }
        }
        return findModuleRefs(layers, scanSpec, log);
    }
    
    public ModuleFinder(final Class<?>[] callStack, final ScanSpec scanSpec, final LogNode log) {
        if (scanSpec.scanModules) {
            List<ModuleRef> allModuleRefsList = null;
            if (scanSpec.overrideModuleLayers == null) {
                if (callStack != null && callStack.length > 0) {
                    allModuleRefsList = this.findModuleRefsFromCallstack(callStack, scanSpec, log);
                }
            }
            else {
                if (log != null) {
                    final LogNode subLog = log.log("Overriding module layers");
                    for (final Object moduleLayer : scanSpec.overrideModuleLayers) {
                        subLog.log(moduleLayer.toString());
                    }
                }
                allModuleRefsList = findModuleRefs(new LinkedHashSet<Object>(scanSpec.overrideModuleLayers), scanSpec, log);
            }
            if (allModuleRefsList != null) {
                this.systemModuleRefs = new ArrayList<ModuleRef>();
                this.nonSystemModuleRefs = new ArrayList<ModuleRef>();
                for (final ModuleRef moduleRef : allModuleRefsList) {
                    if (moduleRef != null) {
                        if (moduleRef.isSystemModule()) {
                            this.systemModuleRefs.add(moduleRef);
                        }
                        else {
                            this.nonSystemModuleRefs.add(moduleRef);
                        }
                    }
                }
            }
            if (log != null) {
                final LogNode sysSubLog = log.log("Found system modules:");
                if (this.systemModuleRefs != null && !this.systemModuleRefs.isEmpty()) {
                    for (final ModuleRef moduleRef2 : this.systemModuleRefs) {
                        sysSubLog.log(moduleRef2.toString());
                    }
                }
                else {
                    sysSubLog.log("[None]");
                }
                final LogNode nonSysSubLog = log.log("Found non-system modules:");
                if (this.nonSystemModuleRefs != null && !this.nonSystemModuleRefs.isEmpty()) {
                    for (final ModuleRef moduleRef3 : this.nonSystemModuleRefs) {
                        nonSysSubLog.log(moduleRef3.toString());
                    }
                }
                else {
                    nonSysSubLog.log("[None]");
                }
            }
        }
        else if (log != null) {
            log.log("Module scanning is disabled, because classloaders or classpath was overridden");
        }
    }
}
