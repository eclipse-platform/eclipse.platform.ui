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
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action that will restore a block of deleted lines at the current caret position in an editor.
 * 
 * @since 3.0
 */
public class RestoreAction extends QuickDiffRestoreAction {
	/** Resource key prefix. */
	private static final String PREFIX= "RestoreAction."; //$NON-NLS-1$
	/** Resource key for a single deleted line. */
	private static final String SINGLE_KEY= PREFIX + "label"; //$NON-NLS-1$
	/** Resource key for multiple deleted lines. */
	private static final String MULTIPLE_KEY= PREFIX + "multiple.label"; //$NON-NLS-1$

	/** The line to be restored. Set in <code>update()</code>. */
	private int fLine;

	/**
	 * Creates a new instance.
	 * 
	 * @param editor the editor this action belongs to
	 */
	public RestoreAction(ITextEditor editor) {
		super(QuickDiffMessages.getResourceBundle(), PREFIX, editor);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		super.update();
		
		if (!isEnabled())
			return;
		
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
				setText(QuickDiffMessages.getString(SINGLE_KEY));
			else
				setText(QuickDiffMessages.getFormattedString(MULTIPLE_KEY, String.valueOf(info.getRemovedLinesBelow()))); //$NON-NLS-1$
			setEnabled(true);
		}
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
