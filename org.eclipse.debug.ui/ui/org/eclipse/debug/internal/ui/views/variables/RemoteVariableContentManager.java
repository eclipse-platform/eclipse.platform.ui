/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredVariableLogicalStructure;
import org.eclipse.debug.internal.ui.views.RemoteTreeContentManager;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.progress.PendingUpdateAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

/**
 * Remote content manager for variables. Creates an appropriate adapter for
 * logical structures.
 */
public class RemoteVariableContentManager extends RemoteTreeContentManager {

    protected VariablesView fView;
    private IDeferredWorkbenchAdapter fVariableLogicalStructureAdapter = new DeferredVariableLogicalStructure();
    
    private Set fHasChildren = new HashSet();
    private Set fNoChildren = new HashSet();
    
    /**
     * Special collector to also collect accurate "has children" information. 
     */
    public class VariableCollector extends  RemoteTreeContentManager.Collector {            

    	public VariableCollector(Object parent) {
        	super(parent);
        }
        
        /**
         * Notification the given element has children
         * 
         * @param element
         */
        public void setHasChildren(Object element, boolean children) {
        	synchronized (fHasChildren) {
            	if (children) {
            		fHasChildren.add(element);
            		fNoChildren.remove(element);
            	} else {
            		fNoChildren.add(element);
            		fHasChildren.remove(element);
            	}
			}
        }

    }
    
    /**
     * Constructs a remote content manager for a variables view.
     */
    public RemoteVariableContentManager(ITreeContentProvider provider, RemoteTreeViewer viewer, IWorkbenchPartSite site, VariablesView view) {
        super(provider, viewer, site);
        fView = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.progress.DeferredTreeContentManager#getAdapter(java.lang.Object)
     */
    protected IDeferredWorkbenchAdapter getAdapter(Object element) {
        if (element instanceof IVariable && fView !=null && fView.isShowLogicalStructure()) {
            return fVariableLogicalStructureAdapter;
        }
        return super.getAdapter(element);
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.progress.DeferredTreeContentManager#createElementCollector(java.lang.Object, org.eclipse.ui.internal.progress.PendingUpdateAdapter)
	 */
	protected IElementCollector createElementCollector(Object parent, PendingUpdateAdapter placeholder) {
		return new VariableCollector(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.progress.DeferredTreeContentManager#mayHaveChildren(java.lang.Object)
	 */
	public boolean mayHaveChildren(Object element) {
		synchronized (fHasChildren) {
			if (fHasChildren.contains(element)) {
				return true;
			} else if (fNoChildren.contains(element)) {
				return false;
			}
		}
		return super.mayHaveChildren(element);
	}
    
	/**
	 * Called to clear the "has children" state cache when the view input changes.
	 */
    public void clearHasChildrenCache() {
        synchronized (fHasChildren) {
            fHasChildren.clear();
            fNoChildren.clear();
        }
    }    
}
