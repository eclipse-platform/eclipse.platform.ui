/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui;

/**
 * Extension interface to <code>IPerspectiveListener</code> which adds support
 * for listening to perspective pre-deactivate events.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * 
 * @see IPageService#addPerspectiveListener(IPerspectiveListener)
 * @see PerspectiveAdapter
 * @since 3.2
 * 
 */
public interface IPerspectiveListener4 extends IPerspectiveListener3 {
	/**
	 * <p>
	 * Notifies this listener that a perspective in the given page is about to
	 * be deactivated.
	 * </p>
	 * <p>
	 * Note: This does not have the ability to veto a perspective deactivation.
	 * </p>
	 * 
	 * @param page
	 *            the page containing the deactivated perspective
	 * @param perspective
	 *            the perspective descriptor that was deactivated
	 * @see IWorkbenchPage#setPerspective(IPerspectiveDescriptor)
	 */
	public void perspectivePreDeactivate(IWorkbenchPage page,
			IPerspectiveDescriptor perspective);

}
