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
package org.eclipse.ui.internal.wizards.preferences;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.osgi.framework.FrameworkUtil;

/**
 * Standard workbench wizard for exporting preferences from the workspace to the
 * local file system.
 * <p>
 * This class may be instantiated and used without further configuration; this
 * class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * IWizard wizard = new PreferencesExportWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * <p>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, the user-selected workspace preferences are
 * exported to the user-specified location in the local file system, the dialog
 * closes, and the call to <code>open</code> returns.
 * </p>
 *
 * @since 3.1
 */
public class PreferencesExportWizard extends Wizard implements IExportWizard {

	private static final String EVENT_TOPIC_BASE = "org/eclipse/ui/internal/wizards/preferences/export/"; //$NON-NLS-1$

	public static final String EVENT_EXPORT_BEGIN = EVENT_TOPIC_BASE + "begin"; //$NON-NLS-1$

	public static final String EVENT_EXPORT_END = EVENT_TOPIC_BASE + "end"; //$NON-NLS-1$

	private WizardPreferencesExportPage1 mainPage;

	private IEventBroker eventBroker;

	/**
	 * Creates a wizard for exporting workspace preferences to the local file
	 * system.
	 */
	public PreferencesExportWizard() {
		IDialogSettings workbenchSettings = PlatformUI
				.getDialogSettingsProvider(FrameworkUtil.getBundle(PreferencesExportWizard.class)).getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("PreferencesExportWizard");//$NON-NLS-1$
		if (section == null) {
			section = workbenchSettings.addNewSection("PreferencesExportWizard");//$NON-NLS-1$
		}
		setDialogSettings(section);
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new WizardPreferencesExportPage1();
		addPage(mainPage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		eventBroker = workbench.getService(IEventBroker.class);
		setWindowTitle(PreferencesMessages.PreferencesExportWizard_export);
		setDefaultPageImageDescriptor(
				WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORT_PREF_WIZ));
		setNeedsProgressMonitor(true);
	}

	@Override
	public boolean performFinish() {
		sendEvent(EVENT_EXPORT_BEGIN);
		boolean success = mainPage.finish();
		sendEvent(EVENT_EXPORT_END);
		return success;
	}

	private void sendEvent(String topic) {
		if (eventBroker != null) {
			eventBroker.send(topic, null);
		}
	}

}
