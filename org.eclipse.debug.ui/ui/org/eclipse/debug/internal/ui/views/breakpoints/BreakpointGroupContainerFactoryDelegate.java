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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.IBreakpointContainer;
import org.eclipse.debug.ui.IBreakpointContainerFactory;
import org.eclipse.debug.ui.IBreakpointContainerFactoryDelegate;

/**
 * A breakpoint container factory delegate that divides breakpoints based on their
 * "custom group".
 */
public class BreakpointGroupContainerFactoryDelegate implements IBreakpointContainerFactoryDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IBreakpointContainerFactoryDelegate#createContainers(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.debug.ui.IBreakpointContainerFactory)
	 */
	public IBreakpointContainer[] createContainers(IBreakpoint[] breakpoints, IBreakpointContainerFactory factory) {
		HashMap map= new HashMap();
		List other= new ArrayList();
		for (int i = 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint = breakpoints[i];
			String group= null;
			try {
				group = breakpoint.getGroup();
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
			if (group != null) {
				List list = (List) map.get(group);
				if (list == null) {
					list= new ArrayList();
					map.put(group, list);
				}
				list.add(breakpoint);
				continue;
			}
			// No group
			other.add(breakpoint);
		}
		List containers= new ArrayList(map.size());
		Set groups = map.keySet();
		Iterator iter= groups.iterator();
		while (iter.hasNext()) {
			String group= (String) iter.next();
			List list= (List) map.get(group);
			BreakpointGroupContainer container= new BreakpointGroupContainer(
					(IBreakpoint[]) list.toArray(new IBreakpoint[0]),
					factory,
					group);
			containers.add(container);
		}
		if (other.size() > 0) {
			BreakpointGroupContainer container= new BreakpointGroupContainer(
					(IBreakpoint[]) other.toArray(new IBreakpoint[0]),
					factory,
					DebugUIViewsMessages.getString("BreakpointGroupContainerFactory.0")); //$NON-NLS-1$
			containers.add(container);
		}
		return (IBreakpointContainer[]) containers.toArray(new IBreakpointContainer[containers.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IBreakpointContainerFactoryDelegate#dispose()
	 */
	public void dispose() {
	}

}
