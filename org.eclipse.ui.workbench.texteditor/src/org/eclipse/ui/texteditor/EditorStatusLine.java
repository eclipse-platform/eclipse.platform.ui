/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;

import org.eclipse.swt.graphics.Image;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * An editor status line.
 * The selection provider of the editor triggers the status line to be cleared.
 * @since 2.1
 */
class EditorStatusLine implements IEditorStatusLine {

	/**
	 * Clears the status line on selection changed.
	 */
	private class StatusLineClearer implements ISelectionChangedListener {
		/*
		 * @see ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			fStatusLineManager.setErrorMessage(null, null);
			fStatusLineManager.setMessage(null, null);

			Assert.isTrue(this == fStatusLineClearer);
			uninstallStatusLineClearer();
		}
	}

	/** The status line manager. */
	private final IStatusLineManager fStatusLineManager;

	/** The selection provider. */
	private final ISelectionProvider fSelectionProvider;

	/** The status line clearer, <code>null</code> if not installed. */
	private StatusLineClearer fStatusLineClearer;

	/**
	 * Constructor for EditorStatusLine.
	 *
	 * @param statusLineManager the status line manager
	 * @param selectionProvider the selection provider
	 */
	public EditorStatusLine(IStatusLineManager statusLineManager, ISelectionProvider selectionProvider) {

		Assert.isNotNull(statusLineManager);
		Assert.isNotNull(selectionProvider);

		fStatusLineManager= statusLineManager;
		fSelectionProvider= selectionProvider;
	}

	/**
	 * Returns the status line manager.
	 *
	 * @return the status line manager
	 */
	public IStatusLineManager getStatusLineManager() {
		return fStatusLineManager;
	}

	/**
	 * Returns the selection provider.
	 *
	 * @return the selection provider
	 */
	public ISelectionProvider getSelectionProvider() {
		return fSelectionProvider;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IStatusLine#setMessage(boolean, String, Image)
	 */
	public void setMessage(boolean error, String message, Image image) {

		if (error)
			fStatusLineManager.setErrorMessage(image, message);
		else {
			// Clear error message
			fStatusLineManager.setErrorMessage(null, null);

			fStatusLineManager.setMessage(image, message);
		}

		if (isMessageEmpty(message))
			uninstallStatusLineClearer();
		else
			installStatusLineClearer();
	}

	/**
	 * Returns whether this given string is empty.
	 *
	 * @param message a string
	 * @return <code>true</code> if the string is <code>null</code>, has 0 length or only white space characters.
	 */
	private static boolean isMessageEmpty(String message) {
		return message == null || message.trim().length() == 0;
	}

	/**
	 * Uninstalls the status line clearer.
	 */
	private void uninstallStatusLineClearer() {
		if (fStatusLineClearer == null)
			return;

		fSelectionProvider.removeSelectionChangedListener(fStatusLineClearer);
		fStatusLineClearer= null;
	}

	/**
	 * Installs the status line clearer.
	 */
	private void installStatusLineClearer() {
		if (fStatusLineClearer != null)
			return;

		StatusLineClearer statusLineClearer= new StatusLineClearer();
		fSelectionProvider.addSelectionChangedListener(statusLineClearer);
		fStatusLineClearer= statusLineClearer;
	}
}
