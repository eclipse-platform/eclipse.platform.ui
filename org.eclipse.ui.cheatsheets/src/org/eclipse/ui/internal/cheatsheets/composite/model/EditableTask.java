/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.composite.model;

/**
 * The concrete implementation of an editable task
 */

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.cheatsheets.composite.parser.EditableTaskParseStrategy;
import org.eclipse.ui.internal.cheatsheets.composite.parser.ITaskParseStrategy;
import org.eclipse.ui.internal.provisional.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.internal.provisional.cheatsheets.IEditableTask;
import org.eclipse.ui.internal.provisional.cheatsheets.TaskEditor;

public class EditableTask extends AbstractTask implements IEditableTask {
	
	private TaskEditor editor;

	private ITaskParseStrategy parserStrategy;
	
	private boolean editorInitialized = false;
	
	private boolean underReview = false;

	public EditableTask(CompositeCheatSheetModel model, String id, String name, String kind) {
		super(model, id, name, kind);
		parserStrategy = new EditableTaskParseStrategy();
	}
	
	public ITaskParseStrategy getParserStrategy() {
		return parserStrategy;
	}

	public ICompositeCheatSheetTask[] getSubtasks() {
		return EMPTY;
	}

	public void setStarted() {	
		if (state==NOT_STARTED) {
			setState(IN_PROGRESS);
		}
	}	
	
	public void setEditor(TaskEditor editor) {
		this.editor = editor;
	}

	public TaskEditor getEditor() {
		return editor;
	}

	public void reset() {
		setStateNoNotify(NOT_STARTED);	
		editorInitialized = false;
	}
	
	public void setInput(IMemento memento) {
		if (editor != null) {
			editor.setInput(this, memento);
			editorInitialized = true;
		}
	}

	public boolean isEditorInitialized() {
		return editorInitialized;
	}

	public void setUnderReview(boolean underReview) {
		this.underReview = underReview;
	}

	public boolean isUnderReview() {
		return underReview;
	}	
	
}
