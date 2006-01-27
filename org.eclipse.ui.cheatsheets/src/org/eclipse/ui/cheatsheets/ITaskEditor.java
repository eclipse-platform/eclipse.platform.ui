/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.cheatsheets;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * An editor which provides the UI for a task within a composite cheat sheet.
 * A task editor is responsible for saving the state of the task whenever
 * it changes.
 * 
 * @since 3.2
 */

public interface ITaskEditor {
/**
 * Creates the widget
 * @param parent
 * @param toolkit
 */
	public void createControl(Composite parent, FormToolkit toolkit);
	
/**
 * @return the Control created by a previous call to CreateControl()
 */
	public Control getControl();
	
/**
 * Starts editing the provided task. The editor is responsible
 * for setting the 'percentage complete' state of the task and 
 * saving its state. createControl() will always be called before start().
 * @param task
 */
	public void start(ICompositeCheatSheetTask task);
}
