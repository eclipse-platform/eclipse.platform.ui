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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.WhitespaceCharacterPainter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class ShowWhitespaceAction extends Action implements IPropertyChangeListener {

	private MergeSourceViewer[] viewers;
	private Map painters = new HashMap();
	private boolean isWhitespaceShowing;
	private IPreferenceStore store;
	
	public ShowWhitespaceAction(MergeSourceViewer[] viewers) {
		super(null, IAction.AS_CHECK_BOX);
		this.viewers = viewers;
		this.store = EditorsUI.getPreferenceStore();
		if (store != null)
			store.addPropertyChangeListener(this);
		synchronizeWithPreference();
	}
	
	private void synchronizeWithPreference() {
		boolean checked = false;
		if (store != null) {
			checked = store.getBoolean(AbstractTextEditor.PREFERENCE_SHOW_WHITESPACE_CHARACTERS);
		}
		if (checked != isChecked()) {
			toggleState(checked);
			setChecked(checked);
		}
	}
	
	public void run() {
		toggleState(isChecked());
		if (store != null)
			store.setValue(AbstractTextEditor.PREFERENCE_SHOW_WHITESPACE_CHARACTERS, isChecked());
	}
	
	private void toggleState(boolean checked) {
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

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(AbstractTextEditor.PREFERENCE_SHOW_WHITESPACE_CHARACTERS)) {
			synchronizeWithPreference();
		}
	}
	
	public void dispose() {
		if (store != null)
			store.removePropertyChangeListener(this);
	}
		
}
