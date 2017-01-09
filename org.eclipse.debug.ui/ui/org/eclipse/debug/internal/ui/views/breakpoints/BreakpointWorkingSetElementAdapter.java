/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetElementAdapter;

/**
 * Consulted by workbench pull down actions that add/remove selected elements to/from
 * working sets. Allows breakpoint working sets to select which elements are applicable
 * for adding/removing.
 *
 * @since 3.3
 */
public class BreakpointWorkingSetElementAdapter implements IWorkingSetElementAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkingSetElementAdapter#adaptElements(org.eclipse.ui.IWorkingSet, org.eclipse.core.runtime.IAdaptable[])
	 */
	@Override
	public IAdaptable[] adaptElements(IWorkingSet ws, IAdaptable[] elements) {
		for (int i = 0; i < elements.length; i++) {
	        IBreakpoint breakpoint = (IBreakpoint)DebugPlugin.getAdapter(elements[i], IBreakpoint.class);
			if (breakpoint != null) {
				return selectBreakpoints(elements);
			}
		}
		return elements;
	}

	private IAdaptable[] selectBreakpoints(IAdaptable[] elements) {
		List<IBreakpoint> breakpoints = new ArrayList<IBreakpoint>(elements.length);
		for (int i = 0; i < elements.length; i++) {
            IBreakpoint breakpoint = (IBreakpoint)DebugPlugin.getAdapter(elements[i], IBreakpoint.class);
			if (breakpoint != null) {
				breakpoints.add(breakpoint);
			}
		}
		return breakpoints.toArray(new IAdaptable[breakpoints.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkingSetElementAdapter#dispose()
	 */
	@Override
	public void dispose() {
	}

}
