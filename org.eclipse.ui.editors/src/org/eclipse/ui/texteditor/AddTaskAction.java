/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import org.eclipse.ui.views.tasklist.TaskPropertiesDialog;


/**
 * Creates a new task marker. Uses the Workbench's task properties dialog.
 * @since 2.0
 */
public class AddTaskAction extends AddMarkerAction {

	/**
	 * Creates a new action for the given text editor. The action configures its
	 * visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @see AddMarkerAction#AddMarkerAction(ResourceBundle, String, ITextEditor, String, boolean)
	 * @since 3.0
	 */
	public AddTaskAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor, IMarker.TASK, false);
	}

	@Override
	public void run() {

		IResource resource= getResource();
		if (resource == null)
			return;
		Map<String, Object> attributes= getInitialAttributes();

		TaskPropertiesDialog dialog= new TaskPropertiesDialog(getTextEditor().getSite().getShell());
		dialog.setResource(resource);
		dialog.setInitialAttributes(attributes);
		dialog.open();
	}
}
