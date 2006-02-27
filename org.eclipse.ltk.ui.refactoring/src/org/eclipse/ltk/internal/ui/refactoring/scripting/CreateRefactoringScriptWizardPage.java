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

import java.net.URI;

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.history.SelectRefactoringHistoryControl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

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

	/** The script location control */
	private RefactoringScriptLocationControl fScriptControl= null;

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
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		final Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		fHistoryControl= new SelectRefactoringHistoryControl(composite, new RefactoringHistoryControlConfiguration(null, true, true)) {

			protected final void handleDeselectAll() {
				super.handleDeselectAll();
				fWizard.setRefactoringDescriptors(EMPTY_DESCRIPTORS);
			}

			protected final void handleSelectAll() {
				super.handleSelectAll();
				final RefactoringHistory history= getInput();
				if (history != null)
					fWizard.setRefactoringDescriptors(history.getDescriptors());
			}
		};
		fHistoryControl.createControl();
		fHistoryControl.setInput(fWizard.getRefactoringHistory());
		fHistoryControl.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(final CheckStateChangedEvent event) {
				fWizard.setRefactoringDescriptors(fHistoryControl.getCheckedDescriptors());
			}
		});
		final Label label= new Label(composite, SWT.NONE);
		label.setText(ScriptingMessages.ApplyRefactoringScriptWizardPage_location_caption);
		final GridData data= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_END);
		data.horizontalIndent= 0;
		data.horizontalSpan= 1;
		data.verticalIndent= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		label.setLayoutData(data);
		fScriptControl= new RefactoringScriptLocationControl(composite) {

			protected void handleBrowseExternalLocation() {
				final FileDialog file= new FileDialog(getShell(), SWT.OPEN);
				file.setText(ScriptingMessages.CreateRefactoringScriptWizardPage_browse_caption);
				file.setFilterNames(new String[] { ScriptingMessages.ScriptLocationControl_filter_name_script, ScriptingMessages.ScriptLocationControl_filter_name_wildcard});
				file.setFilterExtensions(new String[] { ScriptingMessages.ScriptLocationControl_filter_extension_script, ScriptingMessages.ScriptLocationControl_filter_extension_wildcard});
				String path= file.open();
				if (path != null) {
					if (!path.endsWith(ScriptingMessages.CreateRefactoringScriptWizardPage_script_extension))
						path= path + ScriptingMessages.CreateRefactoringScriptWizardPage_script_extension;
					fExternalLocationField.setText(path);
				}
			}

			protected final void handleClipboardScriptChanged() {
				super.handleClipboardScriptChanged();
				fWizard.setRefactoringScript(null);
				fWizard.setUseClipboard(fFromClipboardButton.getSelection());
				setErrorMessage(null);
				setPageComplete(true);
			}

			protected final void handleExternalLocationChanged() {
				super.handleExternalLocationChanged();
				fWizard.setRefactoringScript(null);
				fWizard.setUseClipboard(false);
				setErrorMessage(null);
				setPageComplete(true);
				handleLocationChanged();
			}
		};
		setPageComplete(false);
		setControl(composite);
		Dialog.applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IRefactoringHelpContextIds.REFACTORING_CREATE_SCRIPT_PAGE);
	}

	/**
	 * Handles the location changed event.
	 */
	private void handleLocationChanged() {
		final URI uri= fScriptControl.getRefactoringScript();
		if (uri == null) {
			setErrorMessage(ScriptingMessages.ApplyRefactoringScriptWizardPage_invalid_location);
			setPageComplete(false);
			fWizard.setRefactoringScript(null);
		} else {
			fWizard.setRefactoringScript(uri);
			setErrorMessage(null);
			setPageComplete(true);
		}
	}
}