/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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
package org.eclipse.ant.internal.ui.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;

/**
 * Synchronizes the Ant editor with the state of its linked mode
 * 
 * @since 3.1
 */
public class EditorSynchronizer implements ILinkedModeListener {

	private final AntEditor fEditor;
	private final boolean fWasOccurrencesOn;

	/**
	 * Creates a new synchronizer.
	 * 
	 * @param editor
	 *            the Ant editor that will be synchonized with the linked mode
	 */
	public EditorSynchronizer(AntEditor editor) {
		Assert.isLegal(editor != null);
		fEditor = editor;
		fWasOccurrencesOn = fEditor.isMarkingOccurrences();
		fEditor.setInLinkedMode(true, fWasOccurrencesOn);
	}

	/*
	 * @see org.eclipse.jface.text.link.ILinkedModeListener#left(org.eclipse.jface.text.link.LinkedModeModel, int)
	 */
	@Override
	public void left(LinkedModeModel environment, int flags) {
		fEditor.setInLinkedMode(false, fWasOccurrencesOn);
	}

	/*
	 * @see org.eclipse.jface.text.link.ILinkedModeListener#suspend(org.eclipse.jface.text.link.LinkedModeModel)
	 */
	@Override
	public void suspend(LinkedModeModel environment) {
		// do nothing
	}

	/*
	 * @see org.eclipse.jface.text.link.ILinkedModeListener#resume(org.eclipse.jface.text.link.LinkedModeModel, int)
	 */
	@Override
	public void resume(LinkedModeModel environment, int flags) {
		// do nothing
	}
}