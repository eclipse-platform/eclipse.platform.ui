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
package org.eclipse.ltk.internal.ui.refactoring.history;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Preferences;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringPreferenceConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public final class WorkInProgressPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	static {
		RefactoringCore.internalGetPreferences().setDefault(RefactoringPreferenceConstants.PREFERENCE_ENABLE_WORKSPACE_REFACTORING_HISTORY, false);
	}

	private List fCheckBoxes;

	private List fRadioButtons;

	private List fTextControls;

	public WorkInProgressPreferencePage() {
		fRadioButtons= new ArrayList();
		fCheckBoxes= new ArrayList();
		fTextControls= new ArrayList();
	}

	private Button addCheckBox(Composite parent, String label, String key) {
		GridData data= new GridData(GridData.HORIZONTAL_ALIGN_FILL);

		Button button= new Button(parent, SWT.CHECK);
		button.setText(label);
		button.setData(key);
		button.setLayoutData(data);

		button.setSelection(RefactoringCore.internalGetPreferences().getBoolean(key));

		fCheckBoxes.add(button);
		return button;
	}

	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);

		Composite result= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth= 0;
		layout.verticalSpacing= convertVerticalDLUsToPixels(10);
		layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		result.setLayout(layout);

		Button button= addCheckBox(result, "&Record refactorings performed on workspace (after restart)", RefactoringPreferenceConstants.PREFERENCE_ENABLE_WORKSPACE_REFACTORING_HISTORY); //$NON-NLS-1$
		button.setSelection(RefactoringCore.internalGetPreferences().getBoolean(RefactoringPreferenceConstants.PREFERENCE_ENABLE_WORKSPACE_REFACTORING_HISTORY));

		applyDialogFont(result);
		return result;
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), "WORK_IN_PROGRESS_PREFERENCE_PAGE"); //$NON-NLS-1$
	}

	public void init(IWorkbench workbench) {
		// Do nothing
	}

	protected void performDefaults() {
		Preferences preferences= RefactoringCore.internalGetPreferences();
		for (int i= 0; i < fCheckBoxes.size(); i++) {
			Button button= (Button) fCheckBoxes.get(i);
			String key= (String) button.getData();
			button.setSelection(preferences.getDefaultBoolean(key));
		}
		for (int i= 0; i < fRadioButtons.size(); i++) {
			Button button= (Button) fRadioButtons.get(i);
			String[] info= (String[]) button.getData();
			button.setSelection(info[1].equals(preferences.getDefaultString(info[0])));
		}
		for (int i= 0; i < fTextControls.size(); i++) {
			Text text= (Text) fTextControls.get(i);
			String key= (String) text.getData();
			text.setText(preferences.getDefaultString(key));
		}
		super.performDefaults();
	}

	public boolean performOk() {
		Preferences preferences= RefactoringCore.internalGetPreferences();
		for (int i= 0; i < fCheckBoxes.size(); i++) {
			Button button= (Button) fCheckBoxes.get(i);
			String key= (String) button.getData();
			preferences.setValue(key, button.getSelection());
		}
		for (int i= 0; i < fRadioButtons.size(); i++) {
			Button button= (Button) fRadioButtons.get(i);
			if (button.getSelection()) {
				String[] info= (String[]) button.getData();
				preferences.setValue(info[0], info[1]);
			}
		}
		for (int i= 0; i < fTextControls.size(); i++) {
			Text text= (Text) fTextControls.get(i);
			String key= (String) text.getData();
			preferences.setValue(key, text.getText());
		}
		RefactoringCore.internalSavePreferences();
		return super.performOk();
	}
}
