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
package org.eclipse.ui.internal.texteditor.quickdiff;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;

import org.eclipse.ui.internal.texteditor.NLSUtility;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action that will restore a block of deleted lines at the current caret position in an editor.
 *
 * @since 3.0
 */
public class RestoreAction extends QuickDiffRestoreAction {
	/** Resource key prefix. */
	private static final String PREFIX= "RestoreAction."; //$NON-NLS-1$

	/** The line to be restored. Set in <code>update()</code>. */
	private int fLine;

	/**
	 * Creates a new instance.
	 *
	 * @param editor the editor this action belongs to
	 * @param isRulerAction <code>true</code> if this is a ruler action
	 */
	public RestoreAction(ITextEditor editor, boolean isRulerAction) {
		super(PREFIX, editor, isRulerAction);
	}

	/*
	 * @see org.eclipse.ui.internal.texteditor.quickdiff.QuickDiffRestoreAction#computeEnablement()
	 */
	public boolean computeEnablement() {
		if (!super.computeEnablement())
			return false;

		fLine= getLastLine();
		if (fLine == -1)
			return false;
		ILineDiffer differ= getDiffer();
		if (differ == null)
			return false;
		ILineDiffInfo info= differ.getLineInfo(fLine);
		if (info == null || (info.getRemovedLinesAbove() <= 0 && info.getRemovedLinesBelow() <= 0))
			return false;

		if (info.getRemovedLinesBelow() == 0) {
			fLine--;
		} else if (info.getRemovedLinesAbove() != 0) {
			// take the line below
		}
		info= differ.getLineInfo(fLine);
		if (info == null)
			return false;
		if (info.getRemovedLinesBelow() == 1)
			setText(QuickDiffMessages.RestoreAction_label);
		else
			setText(NLSUtility.format(QuickDiffMessages.RestoreAction_multiple_label, String.valueOf(info.getRemovedLinesBelow())));
		return true;
	}

	/*
	 * @see org.eclipse.ui.internal.editors.quickdiff.QuickDiffRestoreAction#runCompoundChange()
	 */
	public void runCompoundChange() {
		if (!isEnabled())
			return;
		ILineDiffer differ= getDiffer();
		if (differ != null) {
			try {
				differ.restoreAfterLine(fLine);
			} catch (BadLocationException e) {
				setStatus(e.getMessage());
			}
		}
	}
}
