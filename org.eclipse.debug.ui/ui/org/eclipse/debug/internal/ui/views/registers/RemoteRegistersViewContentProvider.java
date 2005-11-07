/**********************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.debug.internal.ui.views.registers;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.RemoteTreeContentManager;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.debug.internal.ui.views.variables.RemoteVariablesContentProvider;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Provides contents for the registers view
 */
public class RemoteRegistersViewContentProvider extends RemoteVariablesContentProvider {

	public RemoteRegistersViewContentProvider(RemoteTreeViewer viewer, IWorkbenchPartSite site, VariablesView view) {
		super(viewer, site, view);
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		try {
			if (element instanceof IStackFrame) {
				return ((IStackFrame)element).hasRegisterGroups();
			}
			if (element instanceof IRegisterGroup) {
				return ((IRegisterGroup)element).hasRegisters();
			}
		} catch (DebugException de) {
			DebugUIPlugin.log(de);
			return false;
		}
		return super.hasChildren(element);
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.variables.RemoteVariablesContentProvider#createContentManager(org.eclipse.debug.internal.ui.views.RemoteTreeViewer, org.eclipse.ui.IWorkbenchPartSite, org.eclipse.debug.internal.ui.views.variables.VariablesView)
     */
    protected RemoteTreeContentManager createContentManager(RemoteTreeViewer viewer, IWorkbenchPartSite site, VariablesView view) {
        return new RemoteRegisterContentManager(this, viewer, site, view);
    }
}
