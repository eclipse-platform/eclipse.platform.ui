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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * An action which groups all breakpoints according to their resource
 */
public class GroupAllByFileAction extends AbstractGroupBreakpointsAction {
    
    /**
     * @param breakpoint
     * @return
     */
    protected String getGroup(IBreakpoint breakpoint) {
        StringBuffer group= new StringBuffer();
        IMarker marker = breakpoint.getMarker();
        if (marker != null) {
            IResource resource = marker.getResource();
            group.append(resource.getName());
            IContainer parent = resource.getParent();
            if (parent != null) {
                group.append(" ["); //$NON-NLS-1$
                group.append(parent.getFullPath().toString().substring(1));
                group.append(']');
            }
        }
        return group.toString();
    }

}
