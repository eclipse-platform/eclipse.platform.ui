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

import java.net.URI;

import org.eclipse.core.resources.IFile;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryWizard;

/**
 * Wizard to apply a refactoring script.
 * 
 * @since 3.2
 */
public final class ApplyRefactoringScriptWizard extends RefactoringHistoryWizard implements IWorkbenchWizard {

	/** Proxy which encapsulates a refactoring history */
	private final class RefactoringHistoryProxy extends RefactoringHistory {

		/**
		 * {@inheritDoc}
		 */
		public RefactoringDescriptorProxy[] getDescriptors() {
			if (fRefactoringHistory != null)
				return fRefactoringHistory.getDescriptors();
			return new RefactoringDescriptorProxy[0];
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isEmpty() {
			final RefactoringDescriptorProxy[] proxies= getDescriptors();
			if (proxies != null)
				return proxies.length == 0;
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		public RefactoringHistory removeAll(final RefactoringHistory history) {
			throw new UnsupportedOperationException();
		}
	}

	/** The dialog settings key */
	private static String DIALOG_SETTINGS_KEY= "ApplyRefactoringScriptWizard"; //$NON-NLS-1$

	/** Has the wizard new dialog settings? */
	private boolean fNewSettings;

	/** The refactoring history, or <code>null</code> */
	private RefactoringHistory fRefactoringHistory= null;

	/** The URI of the script file, or <code>null</code> */
	private URI fScriptURI= null;

	/**
	 * Creates a new apply refactoring script wizard.
	 */
	public ApplyRefactoringScriptWizard() {
		super(ScriptingMessages.ApplyRefactoringScriptWizard_caption, ScriptingMessages.ApplyRefactoringScriptWizard_title, ScriptingMessages.ApplyRefactoringScriptWizard_description);
		setInput(new RefactoringHistoryProxy());
		final IDialogSettings settings= RefactoringUIPlugin.getDefault().getDialogSettings();
		final IDialogSettings section= settings.getSection(DIALOG_SETTINGS_KEY);
		if (section == null)
			fNewSettings= true;
		else {
			fNewSettings= false;
			setDialogSettings(section);
		}
		setConfiguration(new RefactoringHistoryControlConfiguration(null, false, false) {

			public String getProjectPattern() {
				return ScriptingMessages.ApplyRefactoringScriptWizard_project_pattern;
			}

			public String getWorkspaceCaption() {
				return ScriptingMessages.ApplyRefactoringScriptWizard_workspace_caption;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	protected void addUserDefinedPages() {
		addPage(new ApplyRefactoringScriptWizardPage(this));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canFinish() {
		return super.canFinish() && fRefactoringHistory != null;
	}

	/**
	 * Returns the refactoring history to apply.
	 * 
	 * @return the refactoring history to apply, or <code>null</code>
	 */
	public RefactoringHistory getRefactoringHistory() {
		return fRefactoringHistory;
	}

	/**
	 * Returns the location of the refactoring script.
	 * 
	 * @return the location of the script, or <code>null</code>
	 */
	public URI getRefactoringScript() {
		return fScriptURI;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(final IWorkbench workbench, final IStructuredSelection selection) {
		if (selection != null && selection.size() == 1) {
			final Object element= selection.getFirstElement();
			if (element instanceof IFile) {
				final IFile file= (IFile) element;
				if (file.getFileExtension().equals(ScriptingMessages.CreateRefactoringScriptWizardPage_script_extension))
					fScriptURI= file.getRawLocationURI();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		final boolean result= super.performFinish();
		if (fNewSettings) {
			final IDialogSettings settings= RefactoringUIPlugin.getDefault().getDialogSettings();
			IDialogSettings section= settings.getSection(DIALOG_SETTINGS_KEY);
			section= settings.addNewSection(DIALOG_SETTINGS_KEY);
			setDialogSettings(section);
		}
		return result;
	}

	/**
	 * Sets the refactoring history to apply.
	 * 
	 * @param history
	 *            the refactoring history to apply, or <code>null</code>
	 */
	public void setRefactoringHistory(final RefactoringHistory history) {
		fRefactoringHistory= history;
		getContainer().updateButtons();
	}

	/**
	 * Sets the location of the refactoring script.
	 * 
	 * @param uri
	 *            the location of the script
	 */
	public void setRefactoringScript(final URI uri) {
		fScriptURI= uri;
		getContainer().updateButtons();
	}
}