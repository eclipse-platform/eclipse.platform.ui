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
package org.eclipse.debug.internal.ui.model.elements;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;

/**
 * Breakpoint label provider.
 * 
 * @since 3.6
 */
public class BreakpointLabelProvider extends DebugElementLabelProvider {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementLabelProvider#getChecked(org.eclipse.jface.viewers.TreePath, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	public boolean getChecked(TreePath path, IPresentationContext presentationContext) throws CoreException {
		Object lastSegment = path.getLastSegment();
		if (lastSegment instanceof IBreakpoint) {
			return ((IBreakpoint) lastSegment).isEnabled();
		}
		
		return false;
	}
}
