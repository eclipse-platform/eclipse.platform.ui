/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *       Oliver Schaefer <oliver.schaefer@mbtech-services.com> - Fix for
 *     		 Bug 221649 [Import/Export] ZipFileImportWizard has no option to change the FILE_IMPORT_MASK
 *******************************************************************************/
package org.eclipse.ui.wizards.datatransfer;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.WizardArchiveFileResourceImportPage1;
import org.osgi.framework.FrameworkUtil;

/**
 * Standard workbench wizard for importing resources from a zip file into the
 * workspace.
 * <p>
 * This class may be instantiated and used without further configuration; this
 * class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * IWizard wizard = new ZipFileImportWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 *
 * <p>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, the user-selected zip file is imported into
 * the workspace, the dialog closes, and the call to <code>open</code> returns.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ZipFileImportWizard extends Wizard implements IImportWizard {
	private IWorkbench workbench;

	private IStructuredSelection selection;

	private WizardArchiveFileResourceImportPage1 mainPage;

	/**
	 * Creates a wizard for importing resources into the workspace from a zip
	 * file.
	 */
	public ZipFileImportWizard() {
		IDialogSettings workbenchSettings = PlatformUI
				.getDialogSettingsProvider(FrameworkUtil.getBundle(WorkbenchPlugin.class)).getDialogSettings();
		IDialogSettings section = workbenchSettings
				.getSection("ZipFileImportWizard");//$NON-NLS-1$
		if (section == null) {
			section = workbenchSettings.addNewSection("ZipFileImportWizard");//$NON-NLS-1$
		}
		setDialogSettings(section);
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new WizardArchiveFileResourceImportPage1(workbench,
				selection, getFileImportMask());
		addPage(mainPage);
	}

	/**
	 * Get the file import mask used by the receiver. By default use null so
	 * that there is no mask.
	 *
	 * @return String[] or <code>null</code>
	 * @since 3.4
	 */
	protected String[] getFileImportMask() {
		return null;
	}

	@Override
	public void init(IWorkbench currentWorkbench, IStructuredSelection currentSelection) {
		this.workbench = currentWorkbench;
		this.selection = currentSelection;
		List<IResource> selectedResources = IDE.computeSelectedResources(currentSelection);
		if (!selectedResources.isEmpty()) {
			this.selection = new StructuredSelection(selectedResources);
		}

		setWindowTitle(DataTransferMessages.DataTransfer_importTitle);
		setDefaultPageImageDescriptor(IDEWorkbenchPlugin
				.getIDEImageDescriptor("wizban/importzip_wiz.png"));//$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}

	@Override
	public boolean performCancel() {
		return mainPage.cancel();
	}

	@Override
	public boolean performFinish() {
		return mainPage.finish();
	}
}
