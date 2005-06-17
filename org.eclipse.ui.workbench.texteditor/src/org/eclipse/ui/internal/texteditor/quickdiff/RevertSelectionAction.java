/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action that will revert the added, deleted and changes lines in the selection on the currently
 * displayed document to the state in the reference document.
 *
 * @since 3.0
 */
public class RevertSelectionAction extends QuickDiffRestoreAction {
	/** The first line to be restored. Set in <code>update()</code>. */
	private int fStartLine;
	/** The last line to be restored. Set in <code>update()</code>. */
	private int fEndLine;

	/**
	 * Creates a new instance.
	 *
	 * @param editor the editor this action belongs to
	 * @param isRulerAction <code>true</code> if this is a ruler action
	 */
	public RevertSelectionAction(ITextEditor editor, boolean isRulerAction) {
		super("RevertSelectionAction.", editor, isRulerAction); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.ui.internal.texteditor.quickdiff.QuickDiffRestoreAction#computeEnablement()
	 */
	public boolean computeEnablement() {
		if (!super.computeEnablement())
			return false;

		ITextSelection selection= getSelection();
		if (selection == null)
			return false;
		fStartLine= selection.getStartLine();
		fEndLine= selection.getEndLine();
		// only enable if mouse activity is inside line range
		int activityLine= getLastLine();
		if (activityLine == -1 || activityLine < fStartLine || activityLine > fEndLine + 1)
			// + 1 to cover the case where the selection goes to the offset of the next line
			return false;
		ILineDiffer differ= getDiffer();
		if (differ == null)
			return false;
		// only enable if selection covers at least two lines
		if (fEndLine > fStartLine) {
			for (int i= fStartLine; i <= fEndLine; i++) {
				ILineDiffInfo info= differ.getLineInfo(i);
				if (info != null && info.hasChanges()) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.ui.internal.editors.quickdiff.QuickDiffRestoreAction#runCompoundChange()
	 */
	public void runCompoundChange() {
		// recheck if run without being enabled
		if (!isEnabled())
			return;

		ILineDiffer differ= getDiffer();
		if (differ != null) {
			try {
				differ.revertSelection(fStartLine, fEndLine - fStartLine + 1);
			} catch (BadLocationException e) {
				setStatus(e.getMessage());
			}
		}
	}

}
