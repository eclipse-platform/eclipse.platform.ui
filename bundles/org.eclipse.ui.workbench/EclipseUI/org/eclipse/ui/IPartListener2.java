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

import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;

/**
 * Interface for listening to part lifecycle events.
 * <p>
 * This is a replacement for <code>IPartListener</code>.
 * <p>
 * As of 3.5, if the implementation of this listener also implements
 * {@link IPageChangedListener} then it will also be notified about
 * {@link PageChangedEvent}s from parts that implement
 * {@link IPageChangeProvider}.
 * </p>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IPartService#addPartListener(IPartListener2)
 */
public interface IPartListener2 {

	/**
	 * Notifies this listener that the given part has been activated.
	 *
	 * @param partRef the part that was activated
	 * @see IWorkbenchPage#activate
	 */
	default void partActivated(IWorkbenchPartReference partRef) {
	}

	/**
	 * Notifies this listener that the given part has been brought to the top.
	 * <p>
	 * These events occur when an editor is brought to the top in the editor area,
	 * or when a view is brought to the top in a page book with multiple views. They
	 * are normally only sent when a part is brought to the top programmatically
	 * (via <code>IPerspective.bringToTop</code>). When a part is activated by the
	 * user clicking on it, only <code>partActivated</code> is sent.
	 * </p>
	 *
	 * @param partRef the part that was surfaced
	 * @see IWorkbenchPage#bringToTop
	 */
	default void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	/**
	 * Notifies this listener that the given part has been closed.
	 * <p>
	 * Note that if other perspectives in the same page share the view, this
	 * notification is not sent. It is only sent when the view is being removed from
	 * the page entirely (it is being disposed).
	 * </p>
	 *
	 * @param partRef the part that was closed
	 * @see IWorkbenchPage#hideView
	 */
	default void partClosed(IWorkbenchPartReference partRef) {
	}

	/**
	 * Notifies this listener that the given part has been deactivated.
	 *
	 * @param partRef the part that was deactivated
	 * @see IWorkbenchPage#activate
	 */
	default void partDeactivated(IWorkbenchPartReference partRef) {
	}

	/**
	 * Notifies this listener that the given part has been opened.
	 * <p>
	 * Note that if other perspectives in the same page share the view, this
	 * notification is not sent. It is only sent when the view is being newly opened
	 * in the page (it is being created).
	 * </p>
	 *
	 * @param partRef the part that was opened
	 * @see IWorkbenchPage#showView
	 */
	default void partOpened(IWorkbenchPartReference partRef) {
	}

	/**
	 * Notifies this listener that the given part is hidden or obscured by another
	 * part.
	 *
	 * @param partRef the part that is hidden or obscured by another part
	 */
	default void partHidden(IWorkbenchPartReference partRef) {
	}

	/**
	 * Notifies this listener that the given part is visible.
	 *
	 * @param partRef the part that is visible
	 */
	default void partVisible(IWorkbenchPartReference partRef) {
	}

	/**
	 * Notifies this listener that the given part's input was changed.
	 *
	 * @param partRef the part whose input was changed
	 */
	default void partInputChanged(IWorkbenchPartReference partRef) {
	}
}
