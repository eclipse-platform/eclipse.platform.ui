/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;


import org.eclipse.ui.*;

/**
 * Interface for listening to a perspective lifecycle events.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IPerspectiveService
 */
public interface IInternalPerspectiveListener extends IPerspectiveListener {
	
	/**
	 * Notifies this listener that the given page's perspective
	 * has been closed.
	 *
	 * @param page the page whose perspective was closed
	 * @param perspective the descriptor of the perspective that was closed
	 */
	public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective);

	/**
	 * Notifies this listener that the given page's perspective
	 * has been opened. The perspective is not active yet.
	 *
	 * @param page the page whose perspective was opened
	 * @param perspective the descriptor of the perspective that was opened
	 */
	public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective);
}
