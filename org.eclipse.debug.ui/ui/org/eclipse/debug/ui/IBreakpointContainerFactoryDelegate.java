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

/**
 * A delegate which creates containers for a breakpoint container factory.
 * Breakpoint container factories are contributed via the
 * <code>org.eclipse.debug.ui.breakpointContainterFactories</code> extension point.
 * <p>
 * Clients are intended to implement this interface.
 * </p>
 * @since 3.1
 */
public interface IBreakpointContainerFactoryDelegate {
	
	/**
	 * Returns a set of containers which divides up the given breakpoints.
	 * Each of the given breakpoints must exist in one and only one of the
	 * returned containers.
	 * @param breakpoints the breakpoints
	 * @param factory the factory that this delegate is working for
	 * @return a set of containers which divides up the given breakpoints
	 */
	public IBreakpointContainer[] createContainers(IBreakpoint[] breakpoints, IBreakpointContainerFactory factory);
	
	/**
	 * Disposes this delegate. Allows the delegate to clean up any
	 * resources related to images that it may have used for containers 
	 * it created.
	 */
	public void dispose();
}
