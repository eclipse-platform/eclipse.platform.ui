/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorInput;

/**
 * Data structure representing an edit position.
 * 
 * @since 2.1
 */
public final class EditPosition {

	/** The editor input */
	private final IEditorInput fEditorInput;
	/** The editor ID */
	private final String fEditorId;
	/** The selection */
	private final ISelection fSelection;
	/** The position */
	private final Position fPosition;
	
	/**
	 * Creates a new edit position.
	 * 
	 * @param editorInput the editor input
	 * @param editorId the editor ID
	 * @param selection the selection
	 * @param pos the position
	 */
	public EditPosition(IEditorInput editorInput, String editorId, ISelection selection, Position pos) {
		Assert.isNotNull(editorInput);
		Assert.isNotNull(editorId);
		fEditorId= editorId;
		fEditorInput= editorInput;
		fSelection= selection;
		fPosition= pos;
	}

	/**
	 * Returns the editor input for this edit position.
	 *
	 * @return the editor input of this edit position
	 */
	IEditorInput getEditorInput() {
		return fEditorInput;
	}

	/**
	 * Returns the editor id for this edit position.
	 *
	 * @return the editor input of this edit position
	 */
	String getEditorId() {
		return fEditorId;
	}

	/**
	 * Returns the selection of this edit position.
	 *
	 * @return the selection of this edit position
	 */
	ISelection getSelection() {
		return fSelection;
	}

	/**
	 * Returns the the position.
	 * 
	 * @return the position
	 */
	Position getPosition() {
		return fPosition;
	}
}
