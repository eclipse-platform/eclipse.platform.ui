/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * An action which groups all breakpoints according to their project
 */
public class GroupAllByProjectAction extends AbstractGroupBreakpointsAction {

    /**
     * @param breakpoint
     * @return
     */
    protected String getGroup(IBreakpoint breakpoint) {
        String group= null;
        IMarker marker = breakpoint.getMarker();
        if (marker != null) {
            IProject project = marker.getResource().getProject();
            if (project != null) {
                group = project.getName();
            }
        }
        return group;
    }

}
