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
package org.eclipse.ui;


/**
 * Interface for listening to a perspective lifecycle events.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IPageService#addPerspectiveListener
 */
public interface IPerspectiveListener {
/**
 * Notifies this listener that the given page's perspective
 * has been activated.
 *
 * @param page the page whose perspective was activated
 * @param perspective the perspective descriptor that was activated
 * @see IWorkbenchPage#setPerspective
 */
public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective);
/**
 * Notifies this listener that the given page's perspective
 * has change in some way (e.g. editor area hidden, perspective reset,
 * view show/hide, editor open/close, ...).
 *
 * @param page the page whose perspective was reset
 * @param perspective the perspective descriptor
 * @param changeId one of the <code>CHANGE_*</code> constants on IWorkbenchPage
 */
public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId);
}
