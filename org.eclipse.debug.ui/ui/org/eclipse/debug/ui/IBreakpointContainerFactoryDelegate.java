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
import org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainer;
import org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory;

/**
 * A delegate which creates containers for a breakpoint container factory.
 * 
 * Clients may implement this interface, but they are encouraged to extend
 * AbstractBreakpointContainerFactoryDelegate instead.
 * 
 * @since 3.1
 */
public interface IBreakpointContainerFactoryDelegate {
	
	/**
	 * Returns a set of containers which divides up the given breakpoints.
	 * Each of the given breakpoints must exist in one and only one of the
	 * returned containers.
	 * @param breakpoints the breakpoints
	 * @return a set of containers which divides up the given breakpoints
	 */
	public IBreakpointContainer[] createContainers(IBreakpoint[] breakpoints);
	
	/**
	 * Returns the breakpoint container factory associated with this delegate.
	 * @return the factory associated with this delegate
	 */
	public IBreakpointContainerFactory getFactory();
	
	/**
	 * Sets the breakpoint container factory associated with this delegate.
	 * This method should only be called by the factory in question during
	 * initialization.
	 * @param factory the factory
	 */
	public void setFactory(IBreakpointContainerFactory factory);
	
	/**
	 * Disposes this delegate. Allows the delegate to clean up any
	 * resources related to images that it may have used for containers 
	 * it created.
	 */
	public void dispose();
}
