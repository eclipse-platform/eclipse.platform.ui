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
package org.eclipse.ui.internal.ide.dialogs;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.ide.IDEEncoding;
import org.eclipse.ui.ide.dialogs.ResourceEncodingFieldEditor;

import org.eclipse.ui.internal.dialogs.EditorsPreferencePage;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Extends the Editors preference page with IDE-specific settings.
 *
 * Note: want IDE settings to appear in main Editors preference page (via subclassing),
 *   however the superclass, EditorsPreferencePage, is internal
 */
public class IDEEditorsPreferencePage extends EditorsPreferencePage {

	//A boolean to indicate if the user settings were cleared.
	private boolean clearUserSettings = false;

	private ResourceEncodingFieldEditor encodingEditor;

	protected Control createContents(Composite parent) {
		Composite composite = createComposite(parent);

		createEditorHistoryGroup(composite);

		createSpace(composite);
		createShowMultipleEditorTabsPref(composite);
		createCloseEditorsOnExitPref(composite);
		createEditorReuseGroup(composite);

		createSpace(composite);
		encodingEditor = new ResourceEncodingFieldEditor(IDEWorkbenchMessages
				.getString("WorkbenchPreference.encoding"), composite, ResourcesPlugin //$NON-NLS-1$
				.getWorkspace().getRoot());

		encodingEditor.setPreferencePage(this);
		encodingEditor.load();
		updateValidState();

		// @issue need IDE-level help for this page
		//		WorkbenchHelp.setHelp(parent, IHelpContextIds.WORKBENCH_EDITOR_PREFERENCE_PAGE);

		return composite;
	}

	/**
	 * The default button has been pressed. 
	 */
	protected void performDefaults() {

		clearUserSettings = true;

		List encodings = WorkbenchEncoding.getDefinedEncodings();
		Collections.sort(encodings);

		encodingEditor.loadDefault();

		super.performDefaults();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.FileEditorsPreferencePage#performOk()
	 */
	public boolean performOk() {

		if (clearUserSettings)
			IDEEncoding.clearUserEncodings();
		encodingEditor.store();

		return super.performOk();
	}

}