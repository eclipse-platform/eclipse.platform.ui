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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.filesystem.URIUtil;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
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
		new Label(parent, SWT.NONE).setText(ScriptingMessages.ApplyRefactoringScriptWizardPage_location_caption);
		final Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayoutData(createGridData(GridData.FILL_HORIZONTAL, 6, 0));
		composite.setLayout(new GridLayout(3, false));
		final Label label= new Label(composite, SWT.NONE);
		label.setText(ScriptingMessages.ApplyRefactoringScriptWizardPage_location_label);
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
	protected void handleBrowseButtonSelected() {
		final FileDialog file= new FileDialog(getShell(), SWT.OPEN);
		file.setText(ScriptingMessages.ApplyRefactoringScriptWizardPage_browse_caption);
		file.setFilterNames(new String[] { ScriptingMessages.ApplyRefactoringScriptWizardPage_filter_name_script, ScriptingMessages.ApplyRefactoringScriptWizardPage_filter_name_wildcard});
		file.setFilterExtensions(new String[] { ScriptingMessages.ApplyRefactoringScriptWizardPage_filter_extension_script, ScriptingMessages.ApplyRefactoringScriptWizardPage_filter_extension_wildcard});
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
				setErrorMessage(ScriptingMessages.ApplyRefactoringScriptWizardPage_invalid_location);
				setPageComplete(false);
				return;
			}
			final File file= new File(path);
			if (!file.exists()) {
				setErrorMessage(ScriptingMessages.ApplyRefactoringScriptWizardPage_invalid_script_file);
				setPageComplete(false);
				return;
			}
			InputStream stream= null;
			try {
				stream= new BufferedInputStream(new FileInputStream(file));
				fWizard.setRefactoringScript(URIUtil.toURI(path));
				fWizard.setRefactoringHistory(RefactoringCore.getRefactoringHistoryService().readRefactoringHistory(stream, RefactoringDescriptor.NONE));
			} catch (IOException exception) {
				setErrorMessage(ScriptingMessages.ApplyRefactoringScriptWizardPage_error_cannot_read);
				setPageComplete(false);
				return;
			} catch (CoreException exception) {
				RefactoringUIPlugin.log(exception);
				setErrorMessage(ScriptingMessages.ApplyRefactoringScriptWizardPage_invalid_format);
				setPageComplete(false);
				return;
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException exception) {
						// Do nothing
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setVisible(final boolean visible) {
		if (visible) {
			final URI uri= fWizard.getRefactoringScript();
			if (uri != null) {
				try {
					final String path= new File(uri).getCanonicalPath();
					if (path != null) {
						fLocationField.setText(path);
						handleScriptFileChanged();
					}
				} catch (IOException exception) {
					RefactoringUIPlugin.log(exception);
				} catch (IllegalArgumentException exception) {
					RefactoringUIPlugin.log(exception);
				}
			}
		}
		super.setVisible(visible);
	}
}