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
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action that will revert a contiguous block of added, deleted and changes lines in the currently
 * displayed document to the state in the reference document.
 *
 * @since 3.0
 */
public class RevertBlockAction extends QuickDiffRestoreAction {
	/** Resource key prefix. */
	private static final String PREFIX= "RevertBlockAction."; //$NON-NLS-1$

	/** The line to be restored. Set in <code>update()</code>. */
	private int fLine;

	/**
	 * Creates a new instance.
	 *
	 * @param editor the editor this action belongs to
	 * @param isRulerAction <code>true</code> if this is a ruler action
	 */
	public RevertBlockAction(ITextEditor editor, boolean isRulerAction) {
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
		if (info == null || info.getChangeType() == ILineDiffInfo.UNCHANGED)
			return false;

		boolean hasBlock= false;
		if (fLine > 0) {
			info= differ.getLineInfo(fLine - 1);
			hasBlock= info != null && info.hasChanges();
		}
		if (!hasBlock) {
			info= differ.getLineInfo(fLine + 1);
			hasBlock= info != null && info.hasChanges();
		}
		if (hasBlock)
			return true;

		return false;
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
				differ.revertBlock(fLine);
			} catch (BadLocationException e) {
				setStatus(e.getMessage());
			}
		}
	}
}
