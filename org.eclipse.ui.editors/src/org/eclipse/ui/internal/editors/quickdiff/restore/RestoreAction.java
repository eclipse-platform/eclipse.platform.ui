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

import java.text.MessageFormat;

import org.eclipse.ui.internal.editors.quickdiff.QuickDiffTestPlugin;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;
import org.eclipse.jface.text.source.ILineRestorer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

import org.eclipse.ui.texteditor.ITextEditor;

public class RestoreAction extends QuickDiffRestoreAction {
	private static final String PREFIX= "RestoreAction."; //$NON-NLS-1$
	private static final String SINGLE_KEY= "label"; //$NON-NLS-1$
	private static final String MULTIPLE_KEY= "multiple.label"; //$NON-NLS-1$

	private int fLine;

	public RestoreAction(ITextEditor editor) {
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
		if (info != null && (info.getRemovedLinesAbove() > 0 || info.getRemovedLinesBelow() > 0)) {
			if (info.getRemovedLinesBelow() == 0) {
				fLine--;
			} else if (info.getRemovedLinesAbove() != 0) {
//				// if there are deleted lines above and below the line, take the closer one;
//				int lineHeight= fCachedTextWidget.getLineHeight();
//				if (fMousePosition != null
//					&& fMousePosition.y % lineHeight <= lineHeight / 2) {
//					fLine--;
//				}
				// take the one below for now TODO adjust to old viewer-dependent behaviour
			}
			info= differ.getLineInfo(fLine);
			if (info.getRemovedLinesBelow() == 1)
				setText(QuickDiffTestPlugin.getResourceString(PREFIX + SINGLE_KEY));
			else
				setText(MessageFormat.format(QuickDiffTestPlugin.getResourceString(PREFIX + MULTIPLE_KEY), new Object[] { new Integer(info.getRemovedLinesBelow())})); //$NON-NLS-1$
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
				restorer.restoreAfterLine(fLine);
			} catch (BadLocationException e) {
			}
		}
	}
}
