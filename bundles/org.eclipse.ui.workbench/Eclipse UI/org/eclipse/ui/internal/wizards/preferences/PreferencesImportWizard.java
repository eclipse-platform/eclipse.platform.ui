/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Lucas Bullen (Red Hat Inc.) - Bug 525343: importPreferencesremoves preferenceChangedListeners
 *******************************************************************************/
package org.eclipse.ui.internal.wizards.preferences;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Standard workbench wizard for importing resources from the local file system
 * into the workspace.
 * <p>
 * This class may be instantiated and used without further configuration; this
 * class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * 
 * <pre>
 * IWizard wizard = new PreferencesImportWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * 
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, the user-selected files are imported into
 * the workspace, the dialog closes, and the call to <code>open</code> returns.
 * </p>
 *
 * @since 3.1
 *
 */
public class PreferencesImportWizard extends Wizard implements IImportWizard {

	public static final String EVENT_IMPORT_END = "org/eclipse/ui/internal/wizards/preferences/import/end"; //$NON-NLS-1$

	private WizardPreferencesImportPage1 mainPage;

	private IEventBroker eventBroker;

	/**
	 * Creates a wizard for importing resources into the workspace from the file
	 * system.
	 */
	public PreferencesImportWizard() {
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("PreferencesImportWizard");//$NON-NLS-1$
		if (section == null) {
			section = workbenchSettings.addNewSection("PreferencesImportWizard");//$NON-NLS-1$
		}
		setDialogSettings(section);
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new WizardPreferencesImportPage1();
		addPage(mainPage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		eventBroker = workbench.getService(IEventBroker.class);
		setWindowTitle(PreferencesMessages.PreferencesImportWizard_import);
		setDefaultPageImageDescriptor(
				WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORT_PREF_WIZ));
		setNeedsProgressMonitor(true);
	}

	@Override
	public boolean performFinish() {
		boolean success = mainPage.finish();
		sendEvent(EVENT_IMPORT_END);
		showRestartDialog();
		return success;
	}

	private void showRestartDialog() {
		if (0 == MessageDialog.open(MessageDialog.CONFIRM, mainPage.getShell(),
				PreferencesMessages.WizardPreferencesImportRestartDialog_title,
				PreferencesMessages.WizardPreferencesImportRestartDialog_message, SWT.NONE,
				PreferencesMessages.WizardPreferencesImportRestartDialog_restart, IDialogConstants.CANCEL_LABEL)) {
			Display.getDefault().asyncExec(() -> {
				PlatformUI.getWorkbench().restart();
			});
		}
	}

	private void sendEvent(String topic) {
		if (eventBroker != null) {
			eventBroker.send(topic, null);
		}
	}

}
