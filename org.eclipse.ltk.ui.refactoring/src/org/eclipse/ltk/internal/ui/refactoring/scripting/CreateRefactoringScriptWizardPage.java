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
package org.eclipse.ltk.internal.ui.refactoring.scripting;

import java.io.File;

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.history.SelectRefactoringHistoryControl;
import org.eclipse.ltk.internal.ui.refactoring.util.SWTUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * First page of the create refactoring script wizard.
 * 
 * @since 3.2
 */
public final class CreateRefactoringScriptWizardPage extends WizardPage {

	/** The create refactoring script wizard page name */
	private static final String PAGE_NAME= "CreateRefactoringScriptWizardPage"; //$NON-NLS-1$

	/** The refactoring history control */
	private SelectRefactoringHistoryControl fHistoryControl= null;

	/** The location text field */
	private Text fLocationField= null;

	/** The associated wizard */
	private final CreateRefactoringScriptWizard fWizard;

	/**
	 * Creates a new create refactoring script wizard page.
	 * 
	 * @param wizard
	 *            the associated wizard
	 */
	public CreateRefactoringScriptWizardPage(final CreateRefactoringScriptWizard wizard) {
		super(PAGE_NAME);
		Assert.isNotNull(wizard);
		fWizard= wizard;
		setTitle(ScriptingMessages.CreateRefactoringScriptWizard_title);
		setDescription(ScriptingMessages.CreateRefactoringScriptWizard_description);
		setImageDescriptor(RefactoringPluginImages.DESC_WIZBAN_REFACTOR);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		final Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		fHistoryControl= new SelectRefactoringHistoryControl(composite, new RefactoringHistoryControlConfiguration(null, true, true));
		fHistoryControl.createControl();
		fHistoryControl.setInput(fWizard.getRefactoringHistory());
		fHistoryControl.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(final CheckStateChangedEvent event) {
				fWizard.setRefactoringDescriptors(fHistoryControl.getCheckedDescriptors());
			}
		});
		fHistoryControl.getSelectAllButton().addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(final SelectionEvent event) {
				final RefactoringDescriptorProxy[] descriptors= fHistoryControl.getInput().getDescriptors();
				fHistoryControl.setCheckedDescriptors(descriptors);
				fWizard.setRefactoringDescriptors(descriptors);
			}
		});
		fHistoryControl.getDeselectAllButton().addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(final SelectionEvent event) {
				final RefactoringDescriptorProxy[] descriptors= new RefactoringDescriptorProxy[0];
				fHistoryControl.setCheckedDescriptors(descriptors);
				fWizard.setRefactoringDescriptors(descriptors);
			}
		});
		createLocationGroup(composite);
		setControl(composite);
		Dialog.applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IRefactoringHelpContextIds.REFACTORING_CREATE_SCRIPT_PAGE);
	}

	/**
	 * Creates a new grid data.
	 * 
	 * @param flag
	 *            the flags to use
	 * @param hspan
	 *            the horizontal span
	 * @param indent
	 *            the indent
	 * @return the grid data
	 */
	private GridData createGridData(final int flag, final int hspan, final int indent) {
		final GridData data= new GridData(flag);
		data.horizontalIndent= indent;
		data.horizontalSpan= hspan;
		return data;
	}

	/**
	 * Creates the location group.
	 * 
	 * @param parent
	 *            the parent control
	 */
	private void createLocationGroup(final Composite parent) {
		Assert.isNotNull(parent);
		Label label= new Label(parent, SWT.NONE);
		label.setText(ScriptingMessages.ApplyRefactoringScriptWizardPage_location_caption);
		GridData data= createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END, 1, 0);
		data.verticalIndent= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		label.setLayoutData(data);
		final Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 6, 0));
		composite.setLayout(new GridLayout(3, false));
		label= new Label(composite, SWT.NONE);
		label.setText(ScriptingMessages.ApplyRefactoringScriptWizardPage_location_label);
		data= createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 1, 0);
		label.setLayoutData(data);
		fLocationField= new Text(composite, SWT.SINGLE | SWT.BORDER);
		fLocationField.setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 1, 0));
		fLocationField.addModifyListener(new ModifyListener() {

			public final void modifyText(final ModifyEvent event) {
				handleLocationChanged();
			}
		});
		fLocationField.setFocus();
		final Button button= new Button(composite, SWT.PUSH);
		button.setText(ScriptingMessages.ApplyRefactoringScriptWizardPage_browse_label);
		button.setLayoutData(createGridData(GridData.HORIZONTAL_ALIGN_FILL, 1, 0));
		SWTUtil.setButtonDimensionHint(button);
		button.addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				handleBrowseButtonSelected();
			}
		});
		setPageComplete(false);
	}

	/**
	 * Handles the browse button selected event.
	 */
	private void handleBrowseButtonSelected() {
		final FileDialog file= new FileDialog(getShell(), SWT.OPEN);
		file.setText(ScriptingMessages.CreateRefactoringScriptWizardPage_browse_caption);
		file.setFilterNames(new String[] { ScriptingMessages.ApplyRefactoringScriptWizardPage_filter_name_script, ScriptingMessages.ApplyRefactoringScriptWizardPage_filter_name_wildcard});
		file.setFilterExtensions(new String[] { ScriptingMessages.ApplyRefactoringScriptWizardPage_filter_extension_script, ScriptingMessages.ApplyRefactoringScriptWizardPage_filter_extension_wildcard});
		String path= file.open();
		if (path != null) {
			if (!path.endsWith(ScriptingMessages.CreateRefactoringScriptWizardPage_script_extension))
				path= path + ScriptingMessages.CreateRefactoringScriptWizardPage_script_extension;
			fLocationField.setText(path);
		}
	}

	/**
	 * Handles the location changed event.
	 */
	private void handleLocationChanged() {
		if (fLocationField != null) {
			final String path= fLocationField.getText();
			if ("".equals(path)) { //$NON-NLS-1$
				setErrorMessage(ScriptingMessages.ApplyRefactoringScriptWizardPage_invalid_location);
				setPageComplete(false);
				fWizard.setScriptLocation(null);
			} else {
				fWizard.setScriptLocation(new File(path).toURI());
				setErrorMessage(null);
				setPageComplete(true);
			}
		}
	}
}