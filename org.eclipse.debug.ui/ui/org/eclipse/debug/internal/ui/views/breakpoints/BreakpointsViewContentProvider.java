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
package org.eclipse.debug.internal.ui.views.breakpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider that provides a tree of breakpoint containers for display
 * in the breakpoints view.
 */
public class BreakpointsViewContentProvider implements ITreeContentProvider {
	
    /**
     * The collection of breakpoint container factories that will be used to
     * generate content. List elements are applied to their corresponding
     * level in the tree. The 0th element generates the 0th level of containers,
     * the 1st element generates the 1st level of containers, and so on. 
     */
	private List fBreakpointContainerFactories= new ArrayList();
	
	/**
	 * The top level content elements. If the top level elements are
	 * breakpoint containers, these containers can be queried to
	 * determine the subsequent levels of content.
	 */
	private Object[] topLevelElements= new Object[0];
	
	/**
	 * The map which stores the parent elements (IBreakpiontContainer)
	 * for breakpoints. IBreakpointContainers know their own parents.
	 */
	private HashMap fBreakpointParents= new HashMap();
	
	/**
	 * Completely recomputes the content. All old state is discarded and a new tree
	 * of data is created.
	 */
	public void recomputeContent() {
		fBreakpointParents.clear();
	    IBreakpoint[] breakpoints= DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
		if (!fBreakpointContainerFactories.isEmpty()) {
			IBreakpointContainerFactory factory= (IBreakpointContainerFactory) fBreakpointContainerFactories.get(0);
			IBreakpointContainer[] containers = factory.getContainers(null);
			computeChildContent(containers);
			topLevelElements= containers;
		} else {
		    topLevelElements= breakpoints;
		}
	}
	
	/**
	 * Recursively creates the children of the given container. This method
	 * will create all of the appropriate breakpoint containers from the 
	 * breakpoint container factories.
	 * @param containers the containers whose children should be created
	 */
	private void computeChildContent(IBreakpointContainer[] containers) {
	    for (int i = 0; i < containers.length; i++) {
            IBreakpointContainer container = containers[i];
			IBreakpoint[] breakpoints = container.getBreakpoints();
            IBreakpointContainerFactory parentFactory = container.getCreatingFactory();
    		int index = fBreakpointContainerFactories.indexOf(parentFactory);
    		if (index == fBreakpointContainerFactories.size() - 1) {
    			// last container level. cache parent info for breakpoints
    			for (int j = 0; j < breakpoints.length; j++) {
                    fBreakpointParents.put(breakpoints[j], container);
                }
    		} else if (index >= 0) {
    			IBreakpointContainerFactory nextFactory = (IBreakpointContainerFactory) fBreakpointContainerFactories.get(index + 1);
    			computeChildContent(nextFactory.getContainers(container));
    		}   
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
	    return topLevelElements;
	}
	
	/**
	 * Sets the collection of breakpoint container factories to the
	 * given collection. These factories will be used to generate the
	 * content tree the next time recomputeContent() is called.
	 * @param factories the factories to set
	 */
	public void setBreakpointContainerFactories(List factories) {
		Iterator iter = fBreakpointContainerFactories.iterator();
		while (iter.hasNext()) {
			((IBreakpointContainerFactory) iter.next()).dispose();
		}
		fBreakpointContainerFactories= factories;
	}
	
	/**
	 * Returns the collection of breakpoint container factories. These factories
	 * are used to generate the content tree.
	 * @return the collection of breakpoint container factories
	 */
	public List getBreakpointContainerFactories() {
		return fBreakpointContainerFactories;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		fBreakpointParents.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
	    if (parentElement instanceof IBreakpointManager) {
	        return topLevelElements;
	    } else if (parentElement instanceof IBreakpointContainer) {
	        IBreakpointContainer container= (IBreakpointContainer) parentElement;
	        IBreakpointContainer[] containers = container.getContainers();
	        if (containers.length > 0) {
	            return containers;
	        } else {
	            return container.getBreakpoints();
	        }
	    }
	    return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
	    if (element instanceof IBreakpointContainer) {
	        IBreakpointContainer parent = ((IBreakpointContainer) element).getParentContainer();
	        if (parent != null) {
	            return parent;
	        }
	        // Containers with no parent container are at the root.
	        return DebugPlugin.getDefault().getBreakpointManager();
	    }
		return fBreakpointParents.get(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return element instanceof IBreakpointContainer;
	}
}
