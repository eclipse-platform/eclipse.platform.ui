/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

import java.util.*;

import org.eclipse.jface.text.WhitespaceCharacterPainter;

import org.eclipse.ui.texteditor.AbstractTextEditor;

public class ShowWhitespaceAction extends TextEditorPropertyAction {

	private Map fPainters;
	private boolean isWhitespaceShowing;
	private boolean[] fNeedsPainters;
	
	public ShowWhitespaceAction(MergeSourceViewer[] viewers, boolean[] needsPainters) {
		super(CompareMessages.ShowWhitespaceAction_0, viewers, AbstractTextEditor.PREFERENCE_SHOW_WHITESPACE_CHARACTERS);
		fNeedsPainters = needsPainters;
		synchronizeWithPreference();
	}
	
	protected boolean toggleState(boolean checked) {
		if (fNeedsPainters == null)
			return false; // Not initialized yet
		if (checked) {
			showWhitespace();
		} else {
			hideWhitespace();
		}
		return true;
	}
	
	private synchronized Map getPainters() {
		if (fPainters == null)
			fPainters = new HashMap();
		return fPainters;
	}
	
	private void showWhitespace() {
		if (isWhitespaceShowing)
			return;
		try {
			Map painters = getPainters();
			MergeSourceViewer[] viewers = getViewers();
			for (int i = 0; i < viewers.length; i++) {
				if (fNeedsPainters[i]) {
					MergeSourceViewer viewer = viewers[i];
					WhitespaceCharacterPainter painter= new WhitespaceCharacterPainter(viewer.getSourceViewer());
					viewer.getSourceViewer().addPainter(painter);
					painters.put(viewer, painter);
				}
			}
		} finally {
			isWhitespaceShowing = true;
		}
	}
	
	private void hideWhitespace() {
		Map painters = getPainters();
		for (Iterator iterator = painters.keySet().iterator(); iterator.hasNext();) {
			MergeSourceViewer viewer = (MergeSourceViewer) iterator.next();
			WhitespaceCharacterPainter painter = (WhitespaceCharacterPainter)painters.get(viewer);
			if (painter != null) {
				viewer.getSourceViewer().removePainter(painter);
				painter.deactivate(true);
			}
		}
		painters.clear();
		isWhitespaceShowing = false;
	}
		
}
