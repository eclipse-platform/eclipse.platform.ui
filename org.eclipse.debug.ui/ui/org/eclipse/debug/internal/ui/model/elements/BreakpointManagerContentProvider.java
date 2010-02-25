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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.elements.adapters.AbstractBreakpointManagerInput;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Breakpoint manager content provider.
 * 
 * @since 3.6
 */
public class BreakpointManagerContentProvider extends AbstractBreakpointManagerContentProvider {
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.AbstractBreakpointManagerContentProvider#supportsBreakpoint(org.eclipse.jface.viewers.IStructuredSelection, org.eclipse.debug.core.model.IBreakpoint)
	 */
	protected boolean supportsBreakpoint(IStructuredSelection ss, IBreakpoint breakpoint) {
		return supportsBreakpoint(getDebugTargets(ss), breakpoint);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.AbstractBreakpointManagerContentProvider#filterBreakpointsByInput(org.eclipse.debug.internal.ui.elements.adapters.AbstractBreakpointManagerInput, org.eclipse.debug.core.model.IBreakpoint[])
	 */
	protected IBreakpoint[] filterBreakpoints(AbstractBreakpointManagerInput input, IBreakpoint[] breakpoints) {		
		IStructuredSelection selectionFilter = getSelectionFilter(input);
		if (selectionFilter != null && !selectionFilter.isEmpty()) {
			List targets = getDebugTargets(selectionFilter);
			ArrayList retVal = new ArrayList();
			if (targets != null) {
				for (int i = 0; i < breakpoints.length; ++i) {
					if (supportsBreakpoint(targets, breakpoints[i]))
						retVal.add(breakpoints[i]);
				}
			}
			return (IBreakpoint[]) retVal.toArray(new IBreakpoint[retVal.size()]);
		} else {
			return breakpoints;
		}
	}
	
	/**
	 * Returns true if the breakpoint contains in one of the targets.
	 * 
	 * @param targets a list of <code>IDebugTarget</code> objects.
	 * @param breakpoint the breakpoint.
	 * @return true if breakpoint contains in the list of targets.
	 */
	protected boolean supportsBreakpoint(List targets, IBreakpoint breakpoint) {
		boolean exist = targets.size() == 0 ? true : false;
		for (int i = 0; !exist && i < targets.size(); ++i) {
			IDebugTarget target = (IDebugTarget) targets.get(i);
			exist |= target.supportsBreakpoint(breakpoint);
		}
		return exist;
	}
	
	/**
	 * Returns the list of IDebugTarget for the selection.
	 * 
	 * @param ss the selection.
	 * @return list of IDebugTarget object.
	 */
	protected List getDebugTargets(IStructuredSelection ss) {
		List debugTargets = new ArrayList(2);
		if (ss != null) {
			Iterator i = ss.iterator();
			while (i.hasNext()) {
				Object next = i.next();
				if (next instanceof IDebugElement) {
					debugTargets.add(((IDebugElement)next).getDebugTarget());
				} else if (next instanceof ILaunch) {
					IDebugTarget[] targets = ((ILaunch)next).getDebugTargets();
					for (int j = 0; j < targets.length; j++) {
						debugTargets.add(targets[j]);
					}
				} else if (next instanceof IProcess) {
					IDebugTarget target = (IDebugTarget)((IProcess)next).getAdapter(IDebugTarget.class);
					if (target != null) {
						debugTargets.add(target);
					}
				}	
			}
		}
		return debugTargets;
	}


}
