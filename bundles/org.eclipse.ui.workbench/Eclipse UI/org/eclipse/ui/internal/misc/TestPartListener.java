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
package org.eclipse.ui.internal.misc;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Prints out part listener events
 */
public class TestPartListener implements IPartListener {
	/**
	 * TestPartListener constructor comment.
	 */
	// TODO seems like garbage - no one using it
	public TestPartListener() {
		super();
	}

	/**
	 * Notifies this listener that the given part has been activated.
	 *
	 * @param part the part that was activated
	 * @see IWorkbenchPage#activate
	 */
	@Override
	public void partActivated(IWorkbenchPart part) {
		System.out.println("partActivated(" + part + ")");//$NON-NLS-2$//$NON-NLS-1$
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
	 * @param part the part that was surfaced
	 * @see IWorkbenchPage#bringToTop
	 */
	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		System.out.println("partBroughtToTop(" + part + ")");//$NON-NLS-2$//$NON-NLS-1$
	}

	/**
	 * Notifies this listener that the given part has been closed.
	 *
	 * @param part the part that was closed
	 * @see IWorkbenchPage#close
	 */
	@Override
	public void partClosed(IWorkbenchPart part) {
		System.out.println("partClosed(" + part + ")");//$NON-NLS-2$//$NON-NLS-1$
	}

	/**
	 * Notifies this listener that the given part has been deactivated.
	 *
	 * @param part the part that was deactivated
	 * @see IWorkbenchPage#activate
	 */
	@Override
	public void partDeactivated(IWorkbenchPart part) {
		System.out.println("partDeactivated(" + part + ")");//$NON-NLS-2$//$NON-NLS-1$
	}

	/**
	 * Notifies this listener that the given part has been opened.
	 *
	 * @param part the part that was opened
	 */
	@Override
	public void partOpened(IWorkbenchPart part) {
		System.out.println("partOpened(" + part + ")");//$NON-NLS-2$//$NON-NLS-1$
	}
}
