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
package org.eclipse.debug.ui;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.swt.graphics.Image;

/**
 * A breakpoint container holds breakpoints and, optionally, other
 * containers. Each breakpoint in this container exists in one and
 * only one of the child containers, if any. A container with no
 * child containers is a "leaf node."
 * A container with no parent container is a root node. The root
 * containers divide up the breakpoints from the breakpoint manager.
 */
public interface IBreakpointContainer {
	
    /**
     * Returns the breakpoints in this container
     * @return the breakpoints in this container
     */
	public IBreakpoint[] getBreakpoints();
	
	/**
	 * Returns the factory that created this container.
	 * @return the factory that created this container
	 */
	public IBreakpointContainerFactory getCreatingFactory();
	
	/**
	 * Returns this container's parent container or <code>null</code>
	 * if this is a root container. All of the breakpoints
	 * in this container are also in the parent container.
	 * @return this container's parent container or <code>null</code>
	 */
	public IBreakpointContainer getParentContainer();
	
	/**
	 * Sets this container's parent to the given container or
	 * <code>null</code> if this is a root container.
	 * @param parentContainer the parent container or <code>null</code>
	 */
	public void setParentContainer(IBreakpointContainer parentContainer);
	
	/**
	 * Returns the containers within this container, if any. This
	 * container's breakpoints are divided up among the returned
	 * containers.
	 * @return this containers within this container
	 */
	public IBreakpointContainer[] getContainers();
	
	/**
	 * Sets the containers within this container. This container's
	 * breakpoints must each exist in one and only one of the
	 * given container.
	 * @param containers the containers
	 */
	public void setContainers(IBreakpointContainer[] containers);
	
	/**
	 * Returns an image to use for this container of <code>null</code>
	 * if none
	 * @return an image to use with this container or <code>null</code>
	 *  if none
	 */
	public Image getContainerImage();
	
	/**
	 * Sets this container's image to the given image.
	 * @param image the image
	 */
	public void setContainerImage(Image image);
	
	/**
	 * Returns this container's name
	 * @return this container's name
	 */
	public String getName();

}
