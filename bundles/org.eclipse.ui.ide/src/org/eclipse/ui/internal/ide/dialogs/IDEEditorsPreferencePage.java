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
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.ide.IDEEncoding;
import org.eclipse.ui.ide.dialogs.ResourceEncodingFieldEditor;

import org.eclipse.ui.internal.dialogs.EditorsPreferencePage;
import org.eclipse.ui.internal.dialogs.PreferenceLinkArea;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Extends the Editors preference page with IDE-specific settings.
 * 
 * Note: want IDE settings to appear in main Editors preference page (via
 * subclassing), however the superclass, EditorsPreferencePage, is internal
 */
public class IDEEditorsPreferencePage extends EditorsPreferencePage {

	//A boolean to indicate if the user settings were cleared.
	private boolean clearUserSettings = false;

	private ResourceEncodingFieldEditor encodingEditor;

	protected Control createContents(Composite parent) {
		Composite composite = createComposite(parent);

		PreferenceLinkArea area = new PreferenceLinkArea(composite, SWT.BORDER,
				"org.eclipse.ui.preferencePages.FileEditors");//$NON-NLS-1$

		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		data.heightHint = 25;
		data.widthHint = 100;
		area.setLayoutData(data);

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
		encodingEditor.setPropertyChangeListener(new IPropertyChangeListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID))
					updateValidState();

			}
		});

		// @issue need IDE-level help for this page
		//		WorkbenchHelp.setHelp(parent,
		// IHelpContextIds.WORKBENCH_EDITOR_PREFERENCE_PAGE);

		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.EditorsPreferencePage#performDefaults()
	 */
	protected void performDefaults() {

		clearUserSettings = true;

		List encodings = WorkbenchEncoding.getDefinedEncodings();
		Collections.sort(encodings);

		encodingEditor.loadDefault();

		super.performDefaults();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.dialogs.EditorsPreferencePage#updateValidState()
	 */
	protected void updateValidState() {
		if (!encodingEditor.isValid()) {
			setValid(false);
			return;
		}
		//Do the updating of super last we want to
		//inherit the clearing code.
		super.updateValidState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.dialogs.FileEditorsPreferencePage#performOk()
	 */
	public boolean performOk() {

		if (clearUserSettings)
			IDEEncoding.clearUserEncodings();
		encodingEditor.store();

		return super.performOk();
	}

}