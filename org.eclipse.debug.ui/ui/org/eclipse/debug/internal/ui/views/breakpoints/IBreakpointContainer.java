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
public interface IBreakpointContainer {
	
	public IBreakpoint[] getBreakpoints();
	
	public IBreakpointContainerFactory getParentFactory();
	
	/**
	 * Returns an image to use for this container of <code>null</code>
	 * if none
	 * 
	 * @return an image to use with this container or <code>null</code>
	 *  if none
	 */
	public Image getContainerImage();
	
	public String getName();

}
