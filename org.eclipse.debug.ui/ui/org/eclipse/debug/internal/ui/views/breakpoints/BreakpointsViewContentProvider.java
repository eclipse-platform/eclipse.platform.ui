/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
import java.util.HashSet;
import java.util.List;

import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.internal.Workbench;

public class BreakpointsViewContentProvider
	implements IStructuredContentProvider {

	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object parent) {
		List filteredBreakpoints= new ArrayList();
		IActivityManager activityManager = ((Workbench) PlatformUI.getWorkbench()).getActivityManager();
		IBreakpoint[] breakpoints= ((IBreakpointManager) parent).getBreakpoints();
		for (int i = 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint= breakpoints[i];
			if (activityManager.getIdentifier(breakpoint.getModelIdentifier()).isEnabled()) {
				filteredBreakpoints.add(breakpoint);
			}
		}
		return filteredBreakpoints.toArray();
	}

	/**
	 * @see IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
