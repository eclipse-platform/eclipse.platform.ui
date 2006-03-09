/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.provisional;

import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;

/**
 * Adapter for disconnecting.
 * 
 * @since 3.2
 */
public interface IAsynchronousDropToFrameAdapter {

	/**
	 * Asynchronously determines whether the given element can perform a drop to frame.
	 *  
	 * @param element element
	 * @param monitor request monitor
	 */
	public void canDropToFrame(Object element, IBooleanRequestMonitor monitor);
	
	/**
	 * Asynchronously drops to the given frame.
	 *  
	 * @param element element
	 * @param monitor request monitor
	 */
	public void dropToFrame(Object element, IAsynchronousRequestMonitor monitor);	
}
