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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.history.SortableRefactoringHistoryControl;
import org.eclipse.ltk.internal.ui.refactoring.util.PixelConverter;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * First page of the create refactoring script wizard.
 *
 * @since 3.2
 */
public final class CreateRefactoringScriptWizardPage extends WizardPage {

	/** The empty descriptors constant */
	private static final RefactoringDescriptorProxy[] EMPTY_DESCRIPTORS= {};

	/** The create refactoring script wizard page name */
	private static final String PAGE_NAME= "CreateRefactoringScriptWizardPage"; //$NON-NLS-1$

	/** The sort dialog setting */
	protected static final String SETTING_SORT= "org.eclipse.ltk.ui.refactoring.sortRefactorings"; //$NON-NLS-1$

	/** Is the wizard page displayed for the first time? */
	private boolean fFirstTime= true;

	/** The refactoring history control */
	private SortableRefactoringHistoryControl fHistoryControl= null;

	/** The refactoring script location control */
	private RefactoringScriptLocationControl fLocationControl= null;

	/** The create script wizard */
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
		final RefactoringHistoryControlConfiguration configuration= new RefactoringHistoryControlConfiguration(null, true, true) {

			public final String getWorkspaceCaption() {
				return ScriptingMessages.CreateRefactoringScriptWizardPage_select_caption;
			}
		};
		fHistoryControl= new SortableRefactoringHistoryControl(composite, configuration) {

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
		boolean sortProjects= true;
		final IDialogSettings settings= fWizard.getDialogSettings();
		if (settings != null)
			sortProjects= settings.getBoolean(SETTING_SORT);
		if (sortProjects)
			fHistoryControl.sortByProjects();
		else
			fHistoryControl.sortByDate();

		GridData data= new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		data.heightHint= new PixelConverter(parent).convertHeightInCharsToPixels(24);
		fHistoryControl.setLayoutData(data);
		final RefactoringHistory history= fWizard.getRefactoringHistory();
		fHistoryControl.setInput(history);
		fHistoryControl.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(final CheckStateChangedEvent event) {
				fWizard.setRefactoringDescriptors(fHistoryControl.getCheckedDescriptors());
			}
		});
		final Group group= new Group(composite, SWT.NONE);
		group.setText(ScriptingMessages.CreateRefactoringScriptWizardPage_destination_caption);
		final GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fLocationControl= new RefactoringScriptLocationControl(fWizard, group) {

			protected void handleBrowseExternalLocation() {
				final FileDialog file= new FileDialog(getShell(), SWT.OPEN);
				file.setText(ScriptingMessages.CreateRefactoringScriptWizardPage_browse_destination);
				file.setFilterNames(new String[] { ScriptingMessages.ScriptLocationControl_filter_name_script, ScriptingMessages.ScriptLocationControl_filter_name_wildcard});
				file.setFilterExtensions(new String[] { ScriptingMessages.ScriptLocationControl_filter_extension_script, ScriptingMessages.ScriptLocationControl_filter_extension_wildcard});
				String path= file.open();
				if (path != null) {
					if (!path.endsWith(ScriptingMessages.CreateRefactoringScriptWizardPage_script_extension))
						path= path + ScriptingMessages.CreateRefactoringScriptWizardPage_script_extension;
					fExternalLocationControl.setText(path);
				}
			}

			protected final void handleClipboardScriptChanged() {
				super.handleClipboardScriptChanged();
				CreateRefactoringScriptWizardPage.this.fWizard.setRefactoringScript(null);
				CreateRefactoringScriptWizardPage.this.fWizard.setUseClipboard(fFromClipboardButton.getSelection());
				setErrorMessage(null);
				setPageComplete(true);
			}

			protected final void handleExternalLocationChanged() {
				super.handleExternalLocationChanged();
				CreateRefactoringScriptWizardPage.this.fWizard.setRefactoringScript(null);
				CreateRefactoringScriptWizardPage.this.fWizard.setUseClipboard(false);
				setErrorMessage(null);
				setPageComplete(true);
				handleLocationChanged();
			}
		};
		fLocationControl.loadHistory();
		if (history == null || history.isEmpty())
			setErrorMessage(ScriptingMessages.CreateRefactoringScriptWizardPage_no_refactorings);
		fFirstTime= false;
		setPageComplete(false);
		setControl(composite);
		Dialog.applyDialogFont(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IRefactoringHelpContextIds.REFACTORING_CREATE_SCRIPT_PAGE);
	}

	/**
	 * Handles the location changed event.
	 */
	private void handleLocationChanged() {
		final URI uri= fLocationControl.getRefactoringScript();
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

	/**
	 * Gets called if the wizard is finished.
	 */
	public void performFinish() {
		final IDialogSettings settings= fWizard.getDialogSettings();
		if (settings != null)
			settings.put(SETTING_SORT, fHistoryControl.isSortByProjects());
		fLocationControl.saveHistory();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setErrorMessage(final String message) {
		if (!fFirstTime)
			super.setErrorMessage(message);
		else
			setMessage(message, NONE);
	}
}