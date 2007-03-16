/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * Extension interface to <code>IPerspectiveListener</code> which 
 * adds support for listening to perspective open and close events.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IPageService#addPerspectiveListener(IPerspectiveListener)
 * @see PerspectiveAdapter
 * @since 3.1
 */
public interface IPerspectiveListener3 extends IPerspectiveListener2 {

    /**
     * Notifies this listener that a perspective in the given page has been
     * opened.
     * 
     * @param page
     *            the page containing the opened perspective
     * @param perspective
     *            the perspective descriptor that was opened
     * @see IWorkbenchPage#setPerspective(IPerspectiveDescriptor)
     */
    public void perspectiveOpened(IWorkbenchPage page,
            IPerspectiveDescriptor perspective);

    /**
     * Notifies this listener that a perspective in the given page has been
     * closed.
     * 
     * @param page
     *            the page containing the closed perspective
     * @param perspective
     *            the perspective descriptor that was closed
     * @see IWorkbenchPage#closePerspective(IPerspectiveDescriptor, boolean, boolean)
     * @see IWorkbenchPage#closeAllPerspectives(boolean, boolean)
     */
    public void perspectiveClosed(IWorkbenchPage page,
            IPerspectiveDescriptor perspective);

    /**
     * Notifies this listener that a perspective in the given page has been
     * deactivated.
     * 
     * @param page
     *            the page containing the deactivated perspective
     * @param perspective
     *            the perspective descriptor that was deactivated
     * @see IWorkbenchPage#setPerspective(IPerspectiveDescriptor)
     */
    public void perspectiveDeactivated(IWorkbenchPage page,
            IPerspectiveDescriptor perspective);

    /**
     * Notifies this listener that a perspective in the given page has been
     * saved as a new perspective with a different perspective descriptor.
     * 
     * @param page
     *            the page containing the saved perspective
     * @param oldPerspective
     *            the old perspective descriptor
     * @param newPerspective
     *            the new perspective descriptor
     * @see IWorkbenchPage#savePerspectiveAs(IPerspectiveDescriptor)
     */
    public void perspectiveSavedAs(IWorkbenchPage page,
            IPerspectiveDescriptor oldPerspective,
            IPerspectiveDescriptor newPerspective);
}
