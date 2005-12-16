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
package org.eclipse.ltk.internal.ui.refactoring.scripting;

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
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
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.ui.PlatformUI;

/**
 * First page of the apply refactoring script wizard.
 * 
 * @since 3.2
 */
public final class ApplyRefactoringScriptWizardPage extends WizardPage {

	/** The apply refactoring script wizard page name */
	private static final String PAGE_NAME= "ApplyRefactoringScriptWizardPage"; //$NON-NLS-1$

	/** The location text field */
	private Text fLocationField= null;

	/** The associated wizard */
	private final ApplyRefactoringScriptWizard fWizard;

	/**
	 * Creates a new apply refactoring script wizard page.
	 * 
	 * @param wizard
	 *            the associated wizard
	 */
	public ApplyRefactoringScriptWizardPage(final ApplyRefactoringScriptWizard wizard) {
		super(PAGE_NAME);
		Assert.isNotNull(wizard);
		fWizard= wizard;
		setTitle(ScriptingMessages.ApplyRefactoringScriptWizard_title);
		setDescription(ScriptingMessages.ApplyRefactoringScriptWizard_description);
		setImageDescriptor(RefactoringPluginImages.DESC_WIZBAN_REFACTOR);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		final Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		createLocationGroup(composite);
		setControl(composite);
		Dialog.applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IRefactoringHelpContextIds.REFACTORING_APPLY_SCRIPT_PAGE);
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
	protected GridData createGridData(final int flag, final int hspan, final int indent) {
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
	protected void createLocationGroup(final Composite parent) {
		Assert.isNotNull(parent);
		new Label(parent, SWT.NONE).setText("Location of the refactoring script file:");
		final Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 6, 0));
		composite.setLayout(new GridLayout(3, false));
		final Label label= new Label(composite, SWT.NONE);
		label.setText("Script File:");
		label.setLayoutData(createGridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 1, 0));
		fLocationField= new Text(composite, SWT.SINGLE | SWT.BORDER);
		fLocationField.setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 1, 0));
		fLocationField.addModifyListener(new ModifyListener() {

			public final void modifyText(final ModifyEvent event) {
				handleInputChanged();
			}
		});
		fLocationField.setFocus();
		final Button button= new Button(composite, SWT.PUSH);
		button.setText("&Browse...");
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
	protected void handleBrowseButtonSelected() {
		final FileDialog file= new FileDialog(getShell(), SWT.OPEN);
		file.setText("Browse for Script File");
		file.setFilterNames(new String[] { "*.xml", "*.*"});
		file.setFilterExtensions(new String[] { "*.xml", "*.*"});
		final String path= file.open();
		if (path != null)
			fLocationField.setText(path);
	}

	/**
	 * Handles the input changed event.
	 */
	protected void handleInputChanged() {
		fWizard.setRefactoringHistory(null);
		setErrorMessage(null);
		setPageComplete(true);
		handleScriptFileChanged();

	}

	/**
	 * Handles the script file changed event.
	 */
	protected void handleScriptFileChanged() {
		if (fLocationField != null) {
			final String path= fLocationField.getText();
			if ("".equals(path)) { //$NON-NLS-1$
				setErrorMessage("Script file location must not be empty.");
				setPageComplete(false);
				return;
			} else {
				// TODO: implement
			}
		}
	}

}