/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.ide.IDEEncoding;
import org.eclipse.ui.ide.dialogs.ResourceEncodingFieldEditor;
import org.eclipse.ui.internal.dialogs.EditorsPreferencePage;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.LineDelimiterEditor;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

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

	private LineDelimiterEditor lineSeparatorEditor;

	protected Control createContents(Composite parent) {
		Composite composite = createComposite(parent);
        
		PreferenceLinkArea fileEditorsArea = new PreferenceLinkArea(composite, SWT.NONE,
				"org.eclipse.ui.preferencePages.FileEditors", IDEWorkbenchMessages.IDEEditorsPreferencePage_WorkbenchPreference_FileEditorsRelatedLink,//$NON-NLS-1$
				(IWorkbenchPreferenceContainer) getContainer(),null);

		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		fileEditorsArea.getControl().setLayoutData(data);

        PreferenceLinkArea contentTypeArea = new PreferenceLinkArea(composite, SWT.NONE,
                "org.eclipse.ui.preferencePages.ContentTypes", IDEWorkbenchMessages.IDEEditorsPreferencePage_WorkbenchPreference_contentTypesRelatedLink,//$NON-NLS-1$
                (IWorkbenchPreferenceContainer) getContainer(),null);
        
        data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        contentTypeArea.getControl().setLayoutData(data);
        
		PreferenceLinkArea appearanceArea = new PreferenceLinkArea(composite, SWT.NONE,
				"org.eclipse.ui.preferencePages.Views", IDEWorkbenchMessages.IDEEditorsPreferencePage_WorkbenchPreference_viewsRelatedLink,//$NON-NLS-1$
				(IWorkbenchPreferenceContainer) getContainer(),null);

		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		appearanceArea.getControl().setLayoutData(data);
			
		createEditorHistoryGroup(composite);

		createSpace(composite);
		createShowMultipleEditorTabsPref(composite);
		createEditorReuseGroup(composite);

		
		Composite lower = new Composite(composite,SWT.NONE);
		GridLayout lowerLayout = new GridLayout();
		lowerLayout.numColumns = 2;
		lowerLayout.makeColumnsEqualWidth = true;
		lower.setLayout(lowerLayout);
		
		lower.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		
		Composite encodingComposite = new Composite(lower,SWT.NONE);
		encodingComposite.setLayout(new GridLayout());
		encodingComposite.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		
		encodingEditor = new ResourceEncodingFieldEditor(IDEWorkbenchMessages.WorkbenchPreference_encoding, encodingComposite, ResourcesPlugin
				.getWorkspace().getRoot());

		encodingEditor.setPage(this);
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
		
		Composite lineComposite = new Composite(lower,SWT.NONE);
		lineComposite.setLayout(new GridLayout());
		lineComposite.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		
		lineSeparatorEditor = new LineDelimiterEditor(lineComposite);
		lineSeparatorEditor.doLoad();

		// @issue need IDE-level help for this page
		//		WorkbenchHelp.setHelp(parent,
		// IHelpContextIds.WORKBENCH_EDITOR_PREFERENCE_PAGE);

		applyDialogFont(composite);
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
		lineSeparatorEditor.loadDefault();
		
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
		
		lineSeparatorEditor.store();

		return super.performOk();
	}

}
