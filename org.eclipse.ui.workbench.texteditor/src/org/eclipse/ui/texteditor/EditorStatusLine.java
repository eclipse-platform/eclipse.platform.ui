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

package org.eclipse.ui.texteditor;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.text.Assert;

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
		public void selectionChanged(SelectionChangedEvent event) {
			fStatusLineManager.setErrorMessage(null);

			Assert.isTrue(this == fStatusLineClearer);
			uninstallStatusLineClearer();
		}
	};

	/** The status line manager. */
	private final IStatusLineManager fStatusLineManager;

	/** The selection provider. */
	private final ISelectionProvider fSelectionProvider;
	
	/** The status line clearer, <code>null</code> if not installed. */
	private StatusLineClearer fStatusLineClearer;

	/**
	 * Constructor for EditorStatusLine.
	 */
	public EditorStatusLine(IStatusLineManager statusLineManager, ISelectionProvider selectionProvider) {

		Assert.isNotNull(statusLineManager);
		Assert.isNotNull(selectionProvider);		

		fStatusLineManager= statusLineManager;		
		fSelectionProvider= selectionProvider;
	}
	
	/**
	 * Returns the status line manager.
	 */
	public IStatusLineManager getStatusLineManager() {
		return fStatusLineManager;	
	}

	/**
	 * Returns the selection provider.
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
		else
			fStatusLineManager.setMessage(image, message);

		if (isMessageEmpty(message))
			uninstallStatusLineClearer();			
		else
			installStatusLineClearer();
	}

	private static boolean isMessageEmpty(String message) {
		return message == null || message.trim().length() == 0;
	}

	private void uninstallStatusLineClearer() {
		if (fStatusLineClearer == null)
			return;

		fSelectionProvider.removeSelectionChangedListener(fStatusLineClearer);
		fStatusLineClearer= null;
	}
	
	private void installStatusLineClearer() {
		if (fStatusLineClearer != null)
			return;
			
		StatusLineClearer statusLineClearer= new StatusLineClearer();
		fSelectionProvider.addSelectionChangedListener(statusLineClearer);
		fStatusLineClearer= statusLineClearer;		
	}
}
