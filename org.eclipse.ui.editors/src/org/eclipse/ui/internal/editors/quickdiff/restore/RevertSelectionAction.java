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
package org.eclipse.ui.internal.editors.quickdiff.restore;

import org.eclipse.ui.internal.editors.quickdiff.QuickDiffTestPlugin;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;
import org.eclipse.jface.text.source.ILineRestorer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 */
public class RevertSelectionAction extends QuickDiffRestoreAction {
	private int fStartLine;
	private int fEndLine;

	/**
	 * @param editor
	 */
	public RevertSelectionAction(ITextEditor editor) {
		super(QuickDiffTestPlugin.getDefault().getResourceBundle(), "RevertSelectionAction.", editor); //$NON-NLS-1$
	}

	public void update() {
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
		if (activityLine < fStartLine || activityLine > fEndLine)
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

	public void runCompoundChange() {
		// recheck if run without being enabled
		if (!isEnabled())
			return;

		ILineRestorer restorer= getRestorer();
		if (restorer != null) {
			try {
				restorer.revertSelection(fStartLine, fEndLine - fStartLine + 1);
			} catch (BadLocationException e) {
			}
		}
	}

}
