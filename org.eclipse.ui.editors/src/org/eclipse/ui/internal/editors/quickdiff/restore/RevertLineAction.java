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
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;
import org.eclipse.jface.text.source.ILineRestorer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

import org.eclipse.ui.texteditor.ITextEditor;

public class RevertLineAction extends QuickDiffRestoreAction {
	private static final String DELETE_KEY= "delete.label"; //$NON-NLS-1$
	private static final String REVERT_KEY= "label"; //$NON-NLS-1$
	private static final String PREFIX= "RevertLineAction."; //$NON-NLS-1$
	private int fLine;

	public RevertLineAction(ITextEditor editor) {
		super(QuickDiffTestPlugin.getDefault().getResourceBundle(), PREFIX, editor);
	}

	public void update() {
		setEnabled(false);
		IVerticalRulerInfo ruler= getRuler();
		if (ruler == null)
			return;
		fLine= ruler.getLineOfLastMouseButtonActivity();
		ILineDiffer differ= getDiffer();
		if (differ == null)
			return;
		ILineDiffInfo info= differ.getLineInfo(fLine);
		if (info != null && info.getType() != ILineDiffInfo.UNCHANGED) {
			if (info.getType() == ILineDiffInfo.ADDED)
				setText(QuickDiffTestPlugin.getResourceString(PREFIX + DELETE_KEY));
			else
				setText(QuickDiffTestPlugin.getResourceString(PREFIX + REVERT_KEY));
			setEnabled(true);
		}
	}

	/*
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void runCompoundChange() {
		if (!isEnabled())
			return;
		ILineRestorer restorer= getRestorer();
		if (restorer != null) {
			try {
				restorer.revertLine(fLine);
			} catch (BadLocationException e) {
			}
		}
	}
}
