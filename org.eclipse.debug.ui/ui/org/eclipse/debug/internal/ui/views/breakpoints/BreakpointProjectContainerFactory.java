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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A breakpoint container factory that divides breakpoints based on their
 * containing project.
 */
public class BreakpointProjectContainerFactory extends AbstractBreakpointContainerFactory {
	
	private ILabelProvider fImageProvider= new WorkbenchLabelProvider();
	// Handle to the image for the "other" container. Maintained so it can be disposed.
	private Image fOtherImage= null;
	
	public BreakpointProjectContainerFactory() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#createContainers(org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainer)
	 */
	public IBreakpointContainer[] createContainers(IBreakpointContainer parentContainer) {
	    IBreakpoint[] breakpoints= getBreakpoints(parentContainer);
		HashMap map= new HashMap();
		List other= new ArrayList();
		for (int i = 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint = breakpoints[i];
			IMarker marker = breakpoint.getMarker();
			if (marker != null) {
				IProject project = marker.getResource().getProject();
				if (project != null) {
					List list = (List) map.get(project);
					if (list == null) {
						list= new ArrayList();
						map.put(project, list);
					}
					list.add(breakpoint);
					continue;
				}
			}
			// No project available
			other.add(breakpoint);
		}
		List containers= new ArrayList(map.size());
		Set projects = map.keySet();
		Iterator iter= projects.iterator();
		while (iter.hasNext()) {
			IProject project= (IProject) iter.next();
			List list= (List) map.get(project);
			BreakpointContainer container= new BreakpointContainer(
					(IBreakpoint[]) list.toArray(new IBreakpoint[0]),
					parentContainer,
					this,
					project.getName());
			container.setImage(fImageProvider.getImage(project));
			containers.add(container);
		}
		if (other.size() > 0) {
			BreakpointContainer container= new BreakpointContainer(
					(IBreakpoint[]) other.toArray(new IBreakpoint[0]),
					parentContainer,
					this,
					DebugUIViewsMessages.getString("BreakpointProjectContainerFactory.0")); //$NON-NLS-1$
			fOtherImage= PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT);
			container.setImage(fOtherImage);
			containers.add(container);
		}
		return (IBreakpointContainer[]) containers.toArray(new IBreakpointContainer[containers.size()]);
	}
	
	public void dispose() {
		fImageProvider.dispose();
	}

}
