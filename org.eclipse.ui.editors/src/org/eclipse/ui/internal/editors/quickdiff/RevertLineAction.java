/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
package org.eclipse.ui.internal.editors.quickdiff;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action that will revert a line in the currently displayed document to the state in the
 * reference document.
 *
 * @since 3.1
 */
public class RevertLineAction extends QuickDiffRestoreAction {

	/** The line to be restored. Set in <code>update()</code>. */
	private int fLine;

	/**
	 * Creates a new instance.
	 *
	 * @param editor the editor this action belongs to
	 * @param isRulerAction <code>true</code> if this is a ruler action
	 */
	public RevertLineAction(ITextEditor editor, boolean isRulerAction) {
		super(QuickDiffMessages.getResourceBundle(), "RevertLineAction.", editor, isRulerAction); //$NON-NLS-1$
	}

	@Override
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

		if (info.getChangeType() == ILineDiffInfo.ADDED)
			setText(QuickDiffMessages.getString("RevertLineAction.delete.label")); //$NON-NLS-1$
		else
			setText(QuickDiffMessages.getString("RevertLineAction.label")); //$NON-NLS-1$
		return true;
	}

	@Override
	public void runCompoundChange() {
		if (!isEnabled())
			return;
		ILineDiffer differ= getDiffer();
		if (differ != null) {
			try {
				differ.revertLine(fLine);
			} catch (BadLocationException e) {
				setStatus(e.getMessage());
			}
		}
	}
}
