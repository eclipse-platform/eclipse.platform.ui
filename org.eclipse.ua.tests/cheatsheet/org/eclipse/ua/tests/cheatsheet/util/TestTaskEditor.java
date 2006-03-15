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

/**
 *  A task editor with no interesting behavior.
 *  Used by parser tests
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.provisional.cheatsheets.IEditableTask;
import org.eclipse.ui.internal.provisional.cheatsheets.TaskEditor;

public class TestTaskEditor extends TaskEditor {

	private Composite control;
	
	public void createControl(Composite parent, FormToolkit toolkit) {
		control = new Composite(parent, SWT.NULL);
		control.setLayout(new GridLayout());
		Label label = new Label(control, SWT.NULL);
		label.setText("Task editor used by JUnit tests");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	public Control getControl() {
		return control;
	}

	public void setInput(IEditableTask task, IMemento memento) {
		// Do nothing
	}

	public void saveState(IMemento memento) {
		// Do nothing
	}

}
