/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * Interface for listening to perspective lifecycle events.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IPageService#addPerspectiveListener(IPerspectiveListener)
 * @see PerspectiveAdapter
 */
public interface IPerspectiveListener {
	/**
	 * Notifies this listener that a perspective in the given page has been
	 * activated.
	 *
	 * @param page        the page containing the activated perspective
	 * @param perspective the perspective descriptor that was activated
	 * @see IWorkbenchPage#setPerspective
	 */
	void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective);

	/**
	 * Notifies this listener that a perspective has changed in some way (for
	 * example, editor area hidden, perspective reset, view show/hide, editor
	 * open/close, etc).
	 *
	 * @param page        the page containing the affected perspective
	 * @param perspective the perspective descriptor
	 * @param changeId    one of the <code>CHANGE_*</code> constants on
	 *                    IWorkbenchPage
	 */
	void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId);
}
