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
import java.util.Map;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class BreakpointsViewContentProvider implements ITreeContentProvider {
	
	private List fBreakpointContainerFactories= new ArrayList();
	
	private Map fParentMap= new HashMap();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		if (parent instanceof IBreakpoint) {
			return new Object[0];
		}
		Object children[];
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
		if (fBreakpointContainerFactories.isEmpty()) {
			children= breakpoints;
		} else if (parent instanceof IBreakpointManager) {
			IBreakpointContainerFactory factory = (IBreakpointContainerFactory) fBreakpointContainerFactories.get(0);
			children= getFactoryChildren(factory, "", breakpoints); //$NON-NLS-1$
		} else if (parent instanceof IBreakpointContainer) {
			IBreakpointContainer container = ((IBreakpointContainer) parent);
			IBreakpointContainerFactory parentFactory = container.getParentFactory();
			int index = fBreakpointContainerFactories.indexOf(parentFactory);
			if (index == -1) {
				children= new Object[0];
			} else if (index == fBreakpointContainerFactories.size() - 1) {
				// last container level
				children= container.getBreakpoints();
			} else {
				IBreakpointContainerFactory nextFactory = (IBreakpointContainerFactory) fBreakpointContainerFactories.get(index + 1);
				children= getFactoryChildren(nextFactory, getParentId(container), container.getBreakpoints());
			}
		} else {
			children= new Object[0];
		}
		for (int i = 0; i < children.length; i++) {
			fParentMap.put(children[i], parent);
		}
		return children;
	}
	
	public Object[] getFactoryChildren(IBreakpointContainerFactory factory, String parentId, IBreakpoint[] breakpoints) {
	    Object[] children= factory.getContainers(breakpoints, parentId);
		if (children.length == 1) {
			children= getElements(children[0]);
		}
		return children;
	}
	
	public String getParentId(IBreakpointContainer container) {
		Object parent= getParent(container);
		if (parent instanceof IBreakpointContainer) {
			return getParentId((IBreakpointContainer) parent) + '.' + container.getName();
		} else if (parent instanceof IBreakpointManager) {
			return container.getName();
		}
		return ""; //$NON-NLS-1$
	}
	
	public void setBreakpointContainerFactories(List factories) {
		Iterator iter = fBreakpointContainerFactories.iterator();
		while (iter.hasNext()) {
			((IBreakpointContainerFactory) iter.next()).dispose();
		}
		fBreakpointContainerFactories= factories;
	}
	
	public List getBreakpointContainerFactories() {
		return fBreakpointContainerFactories;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fParentMap.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		return fParentMap.get(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return element instanceof IBreakpointContainer;
	}
}
