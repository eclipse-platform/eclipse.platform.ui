package org.eclipse.ui.internal.performance;

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
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * The PerformancePrefererencePage is the preference page to allow the user to
 * disable features that can affect performance adversely.
 * 
 */
public class PerformancePrefererencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	BooleanFieldEditor codeFolding;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		
		setDescription("Features that can cause performance issues for users with slower machines");//$NON-NLS-1$

		Composite workArea = new Composite(parent, SWT.NONE);
		workArea.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
				true));
		
		Label description = new Label(workArea, SWT.WRAP);
		description.setText("The performance preference page is intended to provide an opportunity \nto run this product in high efficiency mode by enabling or disabling options \nthat might impact performance."); //$NON-NLS-1$

		Label spacing = new Label(workArea, SWT.NONE);
		
		codeFolding = new BooleanFieldEditor(
				"org.eclipse.jdt.ui.code.folding", "Code Folding", SWT.CHECK,//$NON-NLS-1$//$NON-NLS-2$
				workArea);
		codeFolding.setPreferenceName("editor_folding_enabled");//$NON-NLS-1$
		codeFolding.setPreferenceStore(new ScopedPreferenceStore(
				new InstanceScope(), "org.eclipse.jdt.ui"));//$NON-NLS-1$
		codeFolding.load();
			
		PreferenceLinkArea link = new PreferenceLinkArea(workArea, SWT.NONE,
				"org.eclipse.jdt.ui.preferences.JavaEditorPreferencePage", //$NON-NLS-1$
				"	**  See <a>''{0}''</a> for all Editor preferences. **", //$NON-NLS-1$
				(IWorkbenchPreferenceContainer) getContainer(),null);

		int cols = codeFolding.getNumberOfControls() + 1;
		
		workArea.setLayout(new GridLayout(cols, false));
		
		GridData labelData = new GridData(SWT.FILL, SWT.FILL, true, false);
		labelData.horizontalSpan = cols;
		description.setLayoutData(labelData);

		labelData = new GridData(SWT.FILL, SWT.FILL, true, false);
		labelData.horizontalSpan = cols;
		spacing.setLayoutData(labelData);

		link.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		
		return workArea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		codeFolding.store();
		return super.performOk();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		codeFolding.loadDefault();
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {

	}

}
