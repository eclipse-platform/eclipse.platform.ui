/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingManager;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;

/**
 * The memory rendering manager.
 * 
 * @see org.eclipse.debug.ui.memory.IMemoryRenderingManager
 * @since 3.1
 */
public class MemoryRenderingManager implements IMemoryRenderingManager {
    
    // map of rendering type ids to valid rendering types
    private Map fRenderingTypes = new HashMap();
    
    // list of renderingBindings
    private List fBindings = new ArrayList();
    
    // list of default bindings
    private List fDefaultBindings = new ArrayList();
    
    // singleton manager
    private static MemoryRenderingManager fgDefault;
    
    // elements in the memory renderings extension point
    public static final String ELEMENT_MEMORY_RENDERING_TYPE = "memoryRenderingType"; //$NON-NLS-1$
    public static final String ELEMENT_RENDERING_BINDINGS = "renderingBindings"; //$NON-NLS-1$
    public static final String ELEMENT_DEFAULT_RENDERINGS = "defaultRenderings"; //$NON-NLS-1$
    
    /**
     * Returns the memory rendering manager.
     * 
     * @return the memory rendering manager
     */
    public static IMemoryRenderingManager getDefault() {
        if (fgDefault == null) {
            fgDefault = new MemoryRenderingManager();
        }
        return fgDefault;
    }
    /**
     * Construts a new memory rendering manager. Only to be called by DebugUIPlugin.
     */
    private MemoryRenderingManager() {
        initializeRenderings();
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.memory.IMemoryRenderingManager#createRendering(java.lang.String)
     */
    public IMemoryRendering createRendering(String id) throws CoreException {
        IMemoryRenderingType type = getRenderingType(id);
        if (type != null) {
            return type.createRendering();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.memory.IMemoryRenderingManager#getRenderingTypes()
     */
    public IMemoryRenderingType[] getRenderingTypes() {
        Collection types = fRenderingTypes.values();
        return (IMemoryRenderingType[]) types.toArray(new IMemoryRenderingType[types.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.memory.IMemoryRenderingManager#getRenderingType(java.lang.String)
     */
    public IMemoryRenderingType getRenderingType(String id) {
        return (IMemoryRenderingType) fRenderingTypes.get(id);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.memory.IMemoryRenderingManager#getDefaultRenderingTypes(org.eclipse.debug.core.model.IMemoryBlock)
     */
    public IMemoryRenderingType[] getDefaultRenderingTypes(IMemoryBlock block) {
        List list = gatherEnabledRenderingTypes(fDefaultBindings, block);
        return (IMemoryRenderingType[]) list.toArray(new IMemoryRenderingType[list.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.memory.IMemoryRenderingManager#getPrimaryRenderingType(org.eclipse.debug.core.model.IMemoryBlock)
     */
    public IMemoryRenderingType getPrimaryRenderingType(IMemoryBlock block) {
        Iterator iterator = fDefaultBindings.iterator();
        while (iterator.hasNext()) {
            DefaultBindings binding = (DefaultBindings)iterator.next();
            if (binding.hasPrimary() && binding.isEnabled(block)) {
                return getRenderingType(binding.getPrimaryId());
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.memory.IMemoryRenderingManager#getRenderingTypes(org.eclipse.debug.core.model.IMemoryBlock)
     */
    public IMemoryRenderingType[] getRenderingTypes(IMemoryBlock block) {
        List def = gatherEnabledRenderingTypes(fDefaultBindings, block);
        List others = gatherEnabledRenderingTypes(fBindings, block);
        def.addAll(others);
        return (IMemoryRenderingType[]) def.toArray(new IMemoryRenderingType[def.size()]);
    }
    
    /**
     * Collects and returns a list of enabled rendering types based on the given bindings,
     * for the specified memory block.
     * 
     * @param bindings bindings to consider
     * @param block the memory block to consider
     * @return a list of enabled rendering types based on the given bindings,
     * for the specified memory block
     */
    private List gatherEnabledRenderingTypes(List bindings, IMemoryBlock block) {
        List types = new ArrayList();
        Iterator iterator = bindings.iterator();
        while (iterator.hasNext()) {
            RenderingBindings binding = (RenderingBindings)iterator.next();
            if (binding.isEnabled(block)) {
                String[] enabledRenderingIds = binding.getRenderingIds(block);
                for (int i = 0; i < enabledRenderingIds.length; i++) {
                    String id = enabledRenderingIds[i];
                    IMemoryRenderingType type = getRenderingType(id);
                    if (type != null && !types.contains(type)) {
                        types.add(type);
                    }
                }
            }
        }
        return types;
    }
    
    /**
     * Processes memory rendering contributions.
     */
    private void initializeRenderings() {
        IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_MEMORY_RENDERIGNS);
        IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
        for (int i = 0; i < configurationElements.length; i++) {
            IConfigurationElement element= configurationElements[i];
            String name = element.getName();
            if (name.equals(ELEMENT_MEMORY_RENDERING_TYPE)) {
                MemoryRenderingType type = new MemoryRenderingType(element);
                try {
                    type.validate();
                    fRenderingTypes.put(type.getId(), type);
                } catch (CoreException e) {
                    DebugUIPlugin.log(e);
                }
            } else if (name.equals(ELEMENT_RENDERING_BINDINGS)) {
                RenderingBindings bindings = new RenderingBindings(element);
                try {
                    bindings.validate();
                    fBindings.add(bindings);
                } catch (CoreException e) {
                    DebugUIPlugin.log(e);
                }
            } else if (name.equals(ELEMENT_DEFAULT_RENDERINGS)) {
                DefaultBindings bindings = new DefaultBindings(element);
                try {
                    bindings.validate();
                    fDefaultBindings.add(bindings);
                } catch (CoreException e) {
                    DebugUIPlugin.log(e);
                }
            }
        }        
    }

}
