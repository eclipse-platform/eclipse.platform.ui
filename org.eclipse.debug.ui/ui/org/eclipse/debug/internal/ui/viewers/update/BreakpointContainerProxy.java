/*****************************************************************
 * Copyright (c) 2009 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial API and implementation (Bug 238956)
 *****************************************************************/
package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointContainer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ICheckboxModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.jface.viewers.TreePath;

/**
 * Breakpoint container model proxy.
 * 
 * @since 3.6
 */
public class BreakpointContainerProxy extends AbstractModelProxy implements	ICheckboxModelProxy {
	
	/**
	 * The breakpoint container
	 */
	private IBreakpointContainer fContainer;
	
	/**
	 * Constructor.
	 * 
	 * @param container the breakpoint container.
	 */
	public BreakpointContainerProxy(IBreakpointContainer container) {
		fContainer = container;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ICheckboxModelProxy#setChecked(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object, org.eclipse.jface.viewers.TreePath, boolean)
	 */
	public boolean setChecked(IPresentationContext context, Object viewerInput, TreePath path, boolean checked) {
		boolean atLeastOne = false;
		IBreakpoint[] breakpoints = fContainer.getBreakpoints();
		for (int i = 0; i < breakpoints.length; ++i) {
			try {
				breakpoints[i].setEnabled(checked);
				atLeastOne = true;
			} catch (CoreException e) {}
		}
		return atLeastOne;
	}

}
