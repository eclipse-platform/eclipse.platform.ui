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

import org.eclipse.debug.core.model.IBreakpoint;

/**
 * 
 */
public abstract class AbstractBreakpointContainerFactory implements IBreakpointContainerFactory {
	
	protected String fLabel;
	protected String fIdentifier;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#getContainers(org.eclipse.debug.core.model.IBreakpoint[], java.lang.String)
	 */
	public abstract IBreakpointContainer[] getContainers(IBreakpoint[] breakpoints, String parentId);

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#getLabel()
	 */
	public String getLabel() {
		return fLabel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#setLabel(java.lang.String)
	 */
	public String setLabel(String label) {
		return fLabel= label;
	}
	
	public void setIdentifier(String identifier) {
		fIdentifier= identifier;
	}
	
	public String getIdentifier() {
		return fIdentifier;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#dispose()
	 */
	public void dispose() {
	}

}
