/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
import java.util.Set;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;

/**
 * 
 */
public class BreakpointTypeContainerFactory extends AbstractBreakpointContainerFactory {
	
	public BreakpointTypeContainerFactory() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#getContainers(org.eclipse.debug.core.model.IBreakpoint[])
	 */
	public IBreakpointContainer[] getContainers(IBreakpoint[] breakpoints, String parentId) {
		HashMap map= new HashMap();
		List other= new ArrayList();
		for (int i = 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint = breakpoints[i];
			String typeName= DebugPlugin.getDefault().getBreakpointManager().getTypeName(breakpoint);
			if (typeName == null) {
				typeName= DebugUIViewsMessages.getString("BreakpointTypeContainerFactory.0"); //$NON-NLS-1$
			}
			List list = (List) map.get(typeName);
			if (list == null) {
				list= new ArrayList();
				map.put(typeName, list);
			}
			list.add(breakpoint);
			continue;
		}
		List containers= new ArrayList(map.size());
		Set typeNames = map.keySet();
		Iterator breakpointIter= typeNames.iterator();
		while (breakpointIter.hasNext()) {
			String typeName= (String) breakpointIter.next();
			List list= (List) map.get(typeName);
			BreakpointContainer container= new BreakpointContainer(
					(IBreakpoint[]) list.toArray(new IBreakpoint[0]),
					this,
					typeName,
					parentId);
			containers.add(container);
		}
		return (IBreakpointContainer[]) containers.toArray(new IBreakpointContainer[containers.size()]);
	}
}
