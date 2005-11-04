/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.EditorActionBars;
import org.eclipse.ui.internal.EditorActionBuilder;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.components.framework.Components;
import org.eclipse.ui.internal.components.framework.IDisposable;
import org.eclipse.ui.internal.part.Part;
import org.eclipse.ui.internal.part.components.services.IActionBarContributor;
import org.eclipse.ui.internal.part.components.services.IActionBarContributorFactory;
import org.eclipse.ui.internal.part.components.services.IPartActionBars;
import org.eclipse.ui.internal.part.components.services.IPartDescriptor;
import org.eclipse.ui.internal.part.components.services.IStatusFactory;
import org.eclipse.ui.internal.part.components.services.IStatusHandler;
import org.eclipse.ui.internal.registry.EditorDescriptor;

public class ActionBarContributorFactory implements IActionBarContributorFactory, IDisposable {

//    private IPartActionBars partActionBars;
    private IActionBars2 partToEditorActionBars;
    private IWorkbenchPage page;
    
    private Map actionCache = new HashMap();
    private class NullActivator implements IActionBarContributor {
        IPartDescriptor descriptor = new NullPartDescriptor();
        
        public void dispose() {
            
        }
        public Object getAdapter(Class adapter) {
            return null;
        }
        
        public IPartDescriptor getDescriptor() {
            return descriptor;
        }
    }
    
    private NullActivator nullActivator = new NullActivator();
    
    private class EditorActionBarActivator implements IActionBarContributor {
        EditorActionBars actionBars;
        IPartDescriptor descriptor;
        IEditorPart activePart = null;
        boolean isActive = false;
        
        EditorActionBarActivator(EditorActionBars actionBars, IPartDescriptor descriptor) {
            this.actionBars = actionBars;
            this.descriptor = descriptor;
        }
        
        public void dispose() {
            if (actionBars == null) {
                return;
            }
            
            actionBars.removeRef();
            if (actionBars.getRef() <= 0) {
                String type = actionBars.getEditorType();
                actionCache.remove(type);
                actionBars.dispose();
                actionBars = null;
            }
        }
        
        public Object getAdapter(Class adapter) {
            if (actionBars == null) {
                return null;
            }
            if (adapter == IEditorActionBarContributor.class) {
                return actionBars.getEditorContributor();
            }
            if (adapter == IActionBars.class) {
                return actionBars;
            }
            return null;
        }

        public IPartDescriptor getDescriptor() {
            return descriptor;
        }
        
        public void activate(Part activePart) {
            if (actionBars == null) {
                return;
            }
            
            IEditorPart editor = (IEditorPart)Components.getAdapter(activePart, IEditorPart.class);
            
            if (!isActive) {
                isActive = true;
                actionBars.activate(true);
            }
            
            if (editor != activePart) {
                actionBars.partChanged(editor);
                this.activePart = editor;
            }
            
            actionBars.updateActionBars();
        }
        
        public void deactivate() {
            if (actionBars == null) {
                return;
            }
            if (!isActive) {
                return;
            }
            
            actionBars.deactivate(true);
            isActive = false;
        }
    }
    
    public ActionBarContributorFactory(IPartActionBars parent, IWorkbenchPage page, IStatusHandler handler, IStatusFactory factory) {
        this.partToEditorActionBars = new PartToEditorActionBarsAdapter(parent, 
                handler, factory);
        this.page = page;
    }
    
    public void activateBars(IActionBarContributor toActivate, Part actualPart) {
        if (toActivate instanceof EditorActionBarActivator) {
            EditorActionBarActivator eaba = (EditorActionBarActivator) toActivate;
            
            eaba.activate(actualPart);
        }
    }
    
    public void deactivateBars(IActionBarContributor toDeactivate) {
        if (toDeactivate instanceof EditorActionBarActivator) {
            EditorActionBarActivator eaba = (EditorActionBarActivator) toDeactivate;
            
            eaba.deactivate();
        }        
    }
    
    public IActionBarContributor getContributor(IPartDescriptor descriptor) {
        String id = descriptor.getId();
        
        IEditorRegistry reg = WorkbenchPlugin.getDefault().getEditorRegistry();
        EditorDescriptor desc = (EditorDescriptor) reg.findEditor(id);
        if (desc == null) {
            return nullActivator;
        }
        
        EditorActionBarActivator activator = (EditorActionBarActivator) actionCache.get(id);
        if (activator == null) {   
            // Create a new action bar set.
            EditorActionBars actionBars = new EditorActionBars(
                    partToEditorActionBars, id);

            // Read base contributor.
            IEditorActionBarContributor contr = desc.createActionBarContributor();
            if (contr != null) {
                actionBars.setEditorContributor(contr);
                contr.init(actionBars, page);
            }

            // Read action extensions.
            EditorActionBuilder builder = new EditorActionBuilder();
            contr = builder.readActionExtensions(desc);
            if (contr != null) {
                actionBars.setExtensionContributor(contr);
                contr.init(actionBars, page);
            }
            
            activator = new EditorActionBarActivator(actionBars, descriptor);
            
            actionCache.put(id, activator);
        }
        
        activator.actionBars.addRef();
        return activator;
    }

    public void dispose() {
        Collection activators = actionCache.values();
        
        for (Iterator iter = activators.iterator(); iter.hasNext();) {
            EditorActionBarActivator next = (EditorActionBarActivator) iter.next();
            
            next.actionBars.dispose();
        }
        
        actionCache.clear();
    }
}
