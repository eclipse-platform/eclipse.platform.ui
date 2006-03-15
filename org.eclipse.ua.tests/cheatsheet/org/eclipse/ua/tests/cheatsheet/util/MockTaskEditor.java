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

package org.eclipse.ua.tests.cheatsheet.util;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.provisional.cheatsheets.IEditableTask;
import org.eclipse.ui.internal.provisional.cheatsheets.TaskEditor;

// Task Editor used in the persistence tests

public class MockTaskEditor extends TaskEditor {
	
	public static final String NO_MEMENTO = "No Memento";
	
	private String value;
	
	private static final String KEY = "key";

	public Control getControl() {
		// Not used by tests
		return null;
	}

	public void setInput(IEditableTask task, IMemento memento) {
		if (memento == null) {
			setValue(NO_MEMENTO);
		} else {
			setValue(memento.getString(KEY));
	    }
	}
	
	public void saveState(IMemento memento) {
		memento.putString(KEY, getValue());
	}

	public void createControl(Composite parent, FormToolkit toolkit) {
		// TODO Auto-generated method stub		
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}


}
