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
import org.eclipse.swt.graphics.Image;

/**
 * 
 */
public class BreakpointContainer implements IBreakpointContainer {
	
	private IBreakpoint[] fBreakpoints;
	private IBreakpointContainerFactory fParentFactory;
	private Image fImage;
	private String fName;
	private String fParentId;
	
	private BreakpointContainer() {
		// Do not call
	}

	/**
	 * Creates a new breakpoint container with the given breakpoints. The
	 * breakpoint container factory which creates this container must pass
	 * itself in as the parentFactory.
	 * @param breakpoints
	 * @param parentFactory
	 */
	public BreakpointContainer(IBreakpoint[] breakpoints, IBreakpointContainerFactory parentFactory, String name, String parentId) {
		fBreakpoints= breakpoints;
		fParentFactory= parentFactory;
		fName= name;
		fParentId= parentId;
	}
	
	public void setImage(Image image) {
		fImage= image;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainer#getBreakpoints()
	 */
	public IBreakpoint[] getBreakpoints() {
		return fBreakpoints;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainer#getParentFactory()
	 */
	public IBreakpointContainerFactory getParentFactory() {
		return fParentFactory;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainer#getContainerImage()
	 */
	public Image getContainerImage() {
		return fImage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainer#getName()
	 */
	public String getName() {
		return fName;
	}
	
	public String getParentId() {
		return fParentId;
	}
	
	public boolean equals(Object object) {
		if (object instanceof BreakpointContainer) {
			BreakpointContainer container = ((BreakpointContainer) object);
			return container.getName().equals(getName()) && container.getParentId().equals(getParentId());
		}
		return false;
	}

}
