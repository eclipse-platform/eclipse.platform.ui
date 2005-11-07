/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Mikhail Khodjaiants - Registers View (Bug 53640)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.AbstractViewerState;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Memento of the expanded and selected items in a variables viewer.
 * 
 * @since 2.1
 */
public class ViewerState extends AbstractViewerState {

	/**
	 * Constructs a memento for the given viewer.
	 */
	public ViewerState(TreeViewer viewer) {
		super(viewer);
	}
    
    public ViewerState() {
        super();
    }

	/**
	 * @see org.eclipse.debug.internal.ui.views.AbstractViewerState#encodeElement(org.eclipse.swt.widgets.TreeItem)
	 */
	protected IPath encodeElement(TreeItem item) throws DebugException {
	    Object data = item.getData();
	    if (data instanceof IVariable) {
	        IVariable variable = (IVariable)data;
	        IPath path = new Path(variable.getName());
	        TreeItem parent = item.getParentItem();
	        while (parent != null) {
	            variable = (IVariable)parent.getData();
	            path = new Path(variable.getName()).append(path);
	            parent = parent.getParentItem();
	        }
	        return path;
	    }
	    
        return null;
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.AbstractViewerState#decodePath(org.eclipse.core.runtime.IPath, org.eclipse.jface.viewers.TreeViewer)
	 */
	protected Object decodePath(IPath path, TreeViewer viewer) throws DebugException {
		ITreeContentProvider contentProvider = (ITreeContentProvider)viewer.getContentProvider();
		String[] names = path.segments();
		Object parent = viewer.getInput();
		IVariable variable = null;
		for (int i = 0; i < names.length; i++) {
			variable = null;
			Object[] children = null;
			if (viewer instanceof RemoteTreeViewer) {
				children = ((RemoteTreeViewer)viewer).getCurrentChildren(parent);
			} else {
				children = contentProvider.getChildren(parent);
			}
			if (children == null) {
				return null;
			}
			String name = names[i];
			for (int j = 0; j < children.length; j++) {
                if (!(children[j] instanceof IVariable)) {
                    continue;
                }
				IVariable var = (IVariable)children[j];
				if (var.getName().equals(name)) {
					variable = var;
					break;
				}
			}
			if (variable == null) {
				return null;
			} 
			parent = variable;
		}
		return variable;
	}
    
    public AbstractViewerState copy() {
        ViewerState copy = new ViewerState();
        if (fSavedExpansion != null) {
            copy.fSavedExpansion = new ArrayList();
            for (Iterator iter = fSavedExpansion.iterator(); iter.hasNext();) {
                copy.fSavedExpansion.add(iter.next());
            }
        }
        
        if (fSelection != null) {
            copy.fSelection = new IPath[fSelection.length];
            for (int i = 0; i < fSelection.length; i++) {
                IPath sel = fSelection[i];
                copy.fSelection[i] = sel;
            }
        }
        return copy;
    }
}
