/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

	private Map painters = new HashMap();
	private boolean isWhitespaceShowing;
	
	public ShowWhitespaceAction(MergeSourceViewer[] viewers) {
		super(viewers, AbstractTextEditor.PREFERENCE_SHOW_WHITESPACE_CHARACTERS);
	}
	
	protected void toggleState(boolean checked) {
		if (checked) {
			showWhitespace();
		} else {
			hideWhitespace();
		}
	}
	
	private void showWhitespace() {
		if (isWhitespaceShowing)
			return;
		try {
			MergeSourceViewer[] viewers = getViewers();
			for (int i = 0; i < viewers.length; i++) {
				MergeSourceViewer viewer = viewers[i];
				WhitespaceCharacterPainter painter= new WhitespaceCharacterPainter(viewer);
				viewer.addPainter(painter);
				painters.put(viewer, painter);
			}
		} finally {
			isWhitespaceShowing = true;
		}
	}
	
	private void hideWhitespace() {
		for (Iterator iterator = painters.keySet().iterator(); iterator.hasNext();) {
			MergeSourceViewer viewer = (MergeSourceViewer) iterator.next();
			WhitespaceCharacterPainter painter = (WhitespaceCharacterPainter)painters.get(viewer);
			if (painter != null) {
				viewer.removePainter(painter);
				painter.deactivate(true);	
			}
		}
		painters.clear();
		isWhitespaceShowing = false;
	}
		
}
