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
package org.eclipse.ui.internal.texteditor.quickdiff;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

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
	 */
	public RevertSelectionAction(ITextEditor editor) {
		super(QuickDiffMessages.getResourceBundle(), "RevertSelectionAction.", editor); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		super.update();
		
		if (!isEnabled())
			return;
		
		setEnabled(false);

		ITextSelection selection= getSelection();
		if (selection == null)
			return;
		fStartLine= selection.getStartLine();
		fEndLine= selection.getEndLine();

		// only enable if mouse activity is inside line range
		IVerticalRulerInfo ruler= getRuler();
		if (ruler == null)
			return;
		int activityLine= ruler.getLineOfLastMouseButtonActivity();
		if (activityLine < fStartLine || activityLine > fEndLine + 1)
			// + 1 to cover the case where the selection goes to the offset of the next line
			return;

		ILineDiffer differ= getDiffer();
		if (differ == null)
			return;

		// only enable if selection covers at least two lines
		if (fEndLine > fStartLine) {
			for (int i= fStartLine; i <= fEndLine; i++) {
				ILineDiffInfo info= differ.getLineInfo(i);
				if (info != null && info.hasChanges()) {
					setEnabled(true);
				}
			}
		}
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
