/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.ui;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.wizards.*;

/**
 * Entry point into update manager UI.
 * Clients can use this class to launch the configuration manager window or the install wizard.
 * @since 3.0
 */
public class UpdateManagerUI {

	/**
	 * Opens the configuration manager window. This is used to manage a current installation 
	 * configuration: browser sites, enable/disable features, etc.
	 * @param shell the parent shell to use
	 */
	public static void openConfigurationManager(Shell shell) {

		ApplicationWindow appWindow = new ConfigurationManagerWindow(shell);
		appWindow.create();
		appWindow.open();
	}
	
	/**
	 * Opens the install wizard dialog. This wizard is used to find and install updates to existing
	 * features, or to find and install new features.
	 * @param shell the dialog parent shell
	 */
	public static void openInstaller(Shell shell) {
		if (InstallWizard.isRunning()) {
			MessageDialog.openInformation(shell, UpdateUI.getString("InstallWizard.isRunningTitle"), UpdateUI.getString("InstallWizard.isRunningInfo"));
			return;
		}
		InstallWizard wizard = new InstallWizard();
		WizardDialog dialog = new ResizableInstallWizardDialog(shell, wizard, UpdateUI.getString("InstallWizardAction.title")); //$NON-NLS-1$
		dialog.create();
		dialog.open();
	}
}
