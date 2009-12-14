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
import org.eclipse.debug.internal.ui.viewers.model.provisional.ICheckboxModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.jface.viewers.TreePath;

/**
 * Breakpoint model proxy.
 * 
 * @since 3.6
 */
public class BreakpointProxy extends AbstractModelProxy implements ICheckboxModelProxy {

	/**
	 * Breakpoint object
	 */
	protected IBreakpoint fBreakpoint;
	
	/**
	 * Constructor.
	 * 
	 * @param breakpoint the breakpoint for this model proxy
	 */
	public BreakpointProxy(IBreakpoint breakpoint) {
		fBreakpoint = breakpoint;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ICheckboxModelProxy#setChecked(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object, org.eclipse.jface.viewers.TreePath, boolean)
	 */
	public boolean setChecked(IPresentationContext context, Object viewerInput, TreePath path, boolean checked) {
		try {
			fBreakpoint.setEnabled(checked);
			return fBreakpoint.isEnabled() == checked;
		} catch (CoreException e) {
			return false;
		}
	}

}
