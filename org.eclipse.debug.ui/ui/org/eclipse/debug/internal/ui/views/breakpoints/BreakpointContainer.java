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
	private IBreakpointContainerFactory fCreatingFactory;
	private Image fImage;
	private String fName;
	private IBreakpointContainer fParentContainer;
	private IBreakpointContainer[] fContainers= new IBreakpointContainer[0];
	
	private BreakpointContainer() {
		// Do not call
	}
	
	public BreakpointContainer(IBreakpoint[] breakpoints,
	        IBreakpointContainer parentContainer,
	        IBreakpointContainerFactory creatingFactory,
	        String name) {
	    fBreakpoints= breakpoints;
	    fParentContainer= parentContainer;
	    fCreatingFactory= creatingFactory;
	    fName= name;
	}
	
	public IBreakpointContainer getParentContainer() {
	    return fParentContainer;
	}
	
	public IBreakpointContainer[] getContainers() {
	    return fContainers;
	}
	
	public void setContainers(IBreakpointContainer[] containers) {
	    fContainers= containers;
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
	public IBreakpointContainerFactory getCreatingFactory() {
		return fCreatingFactory;
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
	
	/**
	 * Breakpoint containers are equal if they have the same name and the
	 * same parent.
	 */
	public boolean equals(Object object) {
		if (object instanceof BreakpointContainer) {
			BreakpointContainer container = ((BreakpointContainer) object);
			if (container.getName().equals(getName())) {
			    IBreakpointContainer parent = getParentContainer();
			    IBreakpointContainer otherParent = container.getParentContainer();
			    return parent == otherParent || (parent != null && parent.equals(otherParent));
			}
		}
		return false;
	}

}
