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
 * @see IBreakpointContainer
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
	
	/**
	 * Creates a new container with the given name, created for the given factory,
	 * which contains the given breakpoints.
	 * @param breakpoints the breakpoints to put in this container
	 * @param creatingFactory the parent factory that created this container
	 * @param name this container's name
	 */
	public BreakpointContainer(IBreakpoint[] breakpoints,
	        IBreakpointContainerFactory creatingFactory,
	        String name) {
	    fBreakpoints= breakpoints;
	    fCreatingFactory= creatingFactory;
	    fName= name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainer#getParentContainer()
	 */
	public IBreakpointContainer getParentContainer() {
	    return fParentContainer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainer#setParentContainer(org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainer)
	 */
	public void setParentContainer(IBreakpointContainer parentContainer) {
		fParentContainer= parentContainer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainer#getContainers()
	 */
	public IBreakpointContainer[] getContainers() {
	    return fContainers;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainer#setContainers(org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainer[])
	 */
	public void setContainers(IBreakpointContainer[] containers) {
	    fContainers= containers;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainer#setContainerImage(org.eclipse.swt.graphics.Image)
	 */
	public void setContainerImage(Image image) {
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
	 * same parent container.
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
