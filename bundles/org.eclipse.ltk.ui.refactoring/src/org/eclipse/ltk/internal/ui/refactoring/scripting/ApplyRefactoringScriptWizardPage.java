/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.scripting;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.internal.core.refactoring.IRefactoringSerializationConstants;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;

/**
 * First page of the apply refactoring script wizard.
 *
 * @since 3.2
 */
public final class ApplyRefactoringScriptWizardPage extends WizardPage {

	/** The apply refactoring script wizard page name */
	private static final String PAGE_NAME= "ApplyRefactoringScriptWizardPage"; //$NON-NLS-1$

	/** Is the wizard page displayed for the first time? */
	private boolean fFirstTime= true;

	/** The refactoring script location control */
	private RefactoringScriptLocationControl fLocationControl= null;

	/** The apply script wizard */
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
	}

	@Override
	public boolean canFlipToNextPage() {
		return super.canFlipToNextPage();
	}

	@Override
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		final Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		final Group group= new Group(composite, SWT.NONE);
		group.setText(ScriptingMessages.ApplyRefactoringScriptWizardPage_location_caption);
		final GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fLocationControl= new RefactoringScriptLocationControl(fWizard, group) {

			@Override
			protected final void handleClipboardScriptChanged() {
				super.handleClipboardScriptChanged();
				ApplyRefactoringScriptWizardPage.this.fWizard.setRefactoringHistory(null);
				setErrorMessage(null);
				setPageComplete(true);
				handleClipboardChanged();
			}

			@Override
			protected final void handleExternalLocationChanged() {
				super.handleExternalLocationChanged();
				ApplyRefactoringScriptWizardPage.this.fWizard.setRefactoringHistory(null);
				setErrorMessage(null);
				setPageComplete(true);
				handleLocationChanged();
			}
		};
		setPageComplete(false);
		fLocationControl.loadHistory();
		fFirstTime= false;
		setControl(composite);
		Dialog.applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IRefactoringHelpContextIds.REFACTORING_APPLY_SCRIPT_PAGE);
	}

	/**
	 * Handles the clipboard changed event.
	 */
	private void handleClipboardChanged() {
		Clipboard clipboard= null;
		try {
			clipboard= new Clipboard(fLocationControl.getDisplay());
			final Object contents= clipboard.getContents(TextTransfer.getInstance());
			if (contents == null) {
				setErrorMessage(ScriptingMessages.ApplyRefactoringScriptWizardPage_empty_clipboard);
				setPageComplete(false);
				return;
			}
			if (contents instanceof String) {
				final String script= (String) contents;
				try {
					final ByteArrayInputStream stream= new ByteArrayInputStream(script.getBytes(IRefactoringSerializationConstants.OUTPUT_ENCODING));
					fWizard.setRefactoringHistory(RefactoringCore.getHistoryService().readRefactoringHistory(stream, RefactoringDescriptor.NONE));
				} catch (UnsupportedEncodingException exception) {
					// Does not happen
				} catch (CoreException exception) {
					setErrorMessage(ScriptingMessages.ApplyRefactoringScriptWizardPage_no_script_clipboard);
					setPageComplete(false);
					return;
				}
			} else {
				setErrorMessage(ScriptingMessages.ApplyRefactoringScriptWizardPage_no_text_clipboard);
				setPageComplete(false);
			}
		} finally {
			if (clipboard != null)
				clipboard.dispose();
		}
	}

	/**
	 * Handles the location changed event.
	 */
	private void handleLocationChanged() {
		final URI uri= fLocationControl.getRefactoringScript();
		if (uri == null) {
			setErrorMessage(ScriptingMessages.ApplyRefactoringScriptWizardPage_invalid_location);
			setPageComplete(false);
			return;
		}
		final File file= new File(uri);
		if (!file.exists()) {
			setErrorMessage(ScriptingMessages.ApplyRefactoringScriptWizardPage_invalid_script_file);
			setPageComplete(false);
			return;
		}
		try (InputStream stream= new BufferedInputStream(new FileInputStream(file))) {
			fWizard.setRefactoringHistory(RefactoringCore.getHistoryService().readRefactoringHistory(stream, RefactoringDescriptor.NONE));
		} catch (IOException exception) {
			setErrorMessage(ScriptingMessages.ApplyRefactoringScriptWizardPage_error_cannot_read);
			setPageComplete(false);
			return;
		} catch (CoreException exception) {
			setErrorMessage(ScriptingMessages.ApplyRefactoringScriptWizardPage_invalid_format);
			setPageComplete(false);
			return;
		}
	}

	/**
	 * Gets called if the wizard is finished.
	 */
	public void performFinish() {
		fLocationControl.saveHistory();
	}

	@Override
	public void setErrorMessage(final String message) {
		if (!fFirstTime)
			super.setErrorMessage(message);
		else
			setMessage(message, NONE);
	}

	@Override
	public void setVisible(final boolean visible) {
		if (visible) {
			final URI uri= fWizard.getRefactoringScript();
			if (uri != null) {
				fWizard.setRefactoringScript(null);
				try {
					fLocationControl.setRefactoringScript(uri);
					handleLocationChanged();
				} catch (IllegalArgumentException exception) {
					RefactoringUIPlugin.log(exception);
				}
			}
		}
		super.setVisible(visible);
	}
}
