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
 * adds support for listening to part-specific perspective lifecycle events.
 * For example, this allows a perspective listener to determine which view
 * is being hidden during a <code>CHANGE_VIEW_HIDE</code> event.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IPageService#addPerspectiveListener(IPerspectiveListener)
 * @see PerspectiveAdapter
 * @since 3.0
 */
public interface IPerspectiveListener2 extends IPerspectiveListener {

    /**
     * Notifies this listener that a part in the given page's perspective
     * has changed in some way (for example, view show/hide, editor open/close, etc).
     *
     * @param page the workbench page containing the perspective
     * @param perspective the descriptor for the changed perspective
     * @param partRef the reference to the affected part
     * @param changeId one of the <code>CHANGE_*</code> constants on IWorkbenchPage
     */
    public void perspectiveChanged(IWorkbenchPage page,
            IPerspectiveDescriptor perspective,
            IWorkbenchPartReference partRef, String changeId);
}
