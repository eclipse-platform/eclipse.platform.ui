/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.wizards.datatransfer;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;
import org.osgi.framework.FrameworkUtil;

/**
 * Standard workbench wizard for importing projects defined outside of the
 * currently defined projects into Eclipse.
 * <p>
 * This class may be instantiated and used without further configuration; this
 * class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * IWizard wizard = new ExternalProjectImportWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * <p>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, a project is created with the location
 * specified by the user.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */

public class ExternalProjectImportWizard extends Wizard implements
		IImportWizard {
	private static final String EXTERNAL_PROJECT_SECTION = "ExternalProjectImportWizard";//$NON-NLS-1$
	private WizardProjectsImportPage mainPage;
	private IStructuredSelection currentSelection = null;
	private String initialPath = null;

	/**
	 * Constructor for ExternalProjectImportWizard.
	 */
	public ExternalProjectImportWizard() {
		this(null);
	}

	/**
	 * Constructor for ExternalProjectImportWizard.
	 *
	 * @param initialPath Default path for wizard to import
	 * @since 3.5
	 */
	public ExternalProjectImportWizard(String initialPath)
	{
		super();
		this.initialPath = initialPath;
		setNeedsProgressMonitor(true);
		IDialogSettings workbenchSettings = PlatformUI
				.getDialogSettingsProvider(FrameworkUtil.getBundle(ExternalProjectImportWizard.class))
				.getDialogSettings();

		IDialogSettings wizardSettings = workbenchSettings
				.getSection(EXTERNAL_PROJECT_SECTION);
		if (wizardSettings == null) {
			wizardSettings = workbenchSettings
					.addNewSection(EXTERNAL_PROJECT_SECTION);
		}
		setDialogSettings(wizardSettings);
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new WizardProjectsImportPage(
				"wizardExternalProjectsPage", initialPath, currentSelection); //$NON-NLS-1$
		addPage(mainPage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(DataTransferMessages.DataTransfer_importTitle);
		setDefaultPageImageDescriptor(
				IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/importproj_wiz.png")); //$NON-NLS-1$
		this.currentSelection = selection;
	}

	@Override
	public boolean performCancel() {
		mainPage.performCancel();
		return true;
	}

	@Override
	public boolean performFinish() {
		return mainPage.createProjects();
	}

}
