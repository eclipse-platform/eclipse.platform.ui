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
public interface IBreakpointContainerFactory {
	
	/**
	 * Returns containers for the given set of breakpoints. Each of the
	 * given breakpoints will appear in exactly one of the returned containers.
	 * 
	 * @param breakpoints the breakpoints to put into containers
	 * @return containers for the given set of breakpoints
	 */
	public IBreakpointContainer[] getContainers(IBreakpoint[] breakpoints, String parentId);
	
	public String getLabel();
	
	public String setLabel(String label);
	
	public String getIdentifier();
	
	public void setIdentifier(String identifier);
	
	/**
	 * Disposes this container factory. Allows the factory to clean up any
	 * resources related to images that it may have used for containers 
	 * it created.
	 */
	public void dispose();

}
