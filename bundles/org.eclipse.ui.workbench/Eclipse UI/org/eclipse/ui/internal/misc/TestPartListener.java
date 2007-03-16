/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.misc;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Prints out part listener events
 */
public class TestPartListener implements IPartListener {
    /**
     * TestPartListener constructor comment.
     * @issue seems like garbage - no one using it
     */
    public TestPartListener() {
        super();
    }

    /**
     * Notifies this listener that the given part has been activated.
     *
     * @param part the part that was activated
     * @see IPerspective#activate
     */
    public void partActivated(IWorkbenchPart part) {
        System.out.println("partActivated(" + part + ")");//$NON-NLS-2$//$NON-NLS-1$
    }

    /**
     * Notifies this listener that the given part has been brought to the top.
     * <p>
     * These events occur when an editor is brought to the top in the editor area,
     * or when a view is brought to the top in a page book with multiple views.
     * They are normally only sent when a part is brought to the top 
     * programmatically (via <code>IPerspective.bringToTop</code>). When a part is
     * activated by the user clicking on it, only <code>partActivated</code> is sent.
     * </p>
     *
     * @param part the part that was surfaced
     * @see IPerspective#bringToTop
     */
    public void partBroughtToTop(IWorkbenchPart part) {
        System.out.println("partBroughtToTop(" + part + ")");//$NON-NLS-2$//$NON-NLS-1$
    }

    /**
     * Notifies this listener that the given part has been closed.
     *
     * @param part the part that was closed
     * @see IPerspective#close
     */
    public void partClosed(IWorkbenchPart part) {
        System.out.println("partClosed(" + part + ")");//$NON-NLS-2$//$NON-NLS-1$
    }

    /**
     * Notifies this listener that the given part has been deactivated.
     *
     * @param part the part that was deactivated
     * @see IPerspective#activate
     */
    public void partDeactivated(IWorkbenchPart part) {
        System.out.println("partDeactivated(" + part + ")");//$NON-NLS-2$//$NON-NLS-1$
    }

    /**
     * Notifies this listener that the given part has been opened.
     *
     * @param part the part that was opened
     */
    public void partOpened(IWorkbenchPart part) {
        System.out.println("partOpened(" + part + ")");//$NON-NLS-2$//$NON-NLS-1$
    }
}
