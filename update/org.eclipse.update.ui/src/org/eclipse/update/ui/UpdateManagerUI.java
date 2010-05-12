/*******************************************************************************
 *  Copyright (c) 2000, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.ui;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.wizards.*;
import org.eclipse.update.search.*;

/**
 * Entry point into update manager UI.
 * Clients can use this class to launch the configuration manager window or the install wizard.
 * @since 3.0
 * @deprecated The org.eclipse.update component has been replaced by Equinox p2.
 * This API will be deleted in a future release. See bug 311590 for details.
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
			MessageDialog.openInformation(shell, UpdateUIMessages.InstallWizard_isRunningTitle, UpdateUIMessages.InstallWizard_isRunningInfo);
			return;
		}
		InstallWizard wizard = new InstallWizard((UpdateSearchRequest) null);
		WizardDialog dialog = new ResizableInstallWizardDialog(shell, wizard, UpdateUIMessages.InstallWizardAction_title); 
		dialog.create();
		dialog.open();
	}

	/**
	 * Opens the install wizard dialog. This wizard is used to find and install updates to existing
	 * features, or to find and install new features as described by the given update job
	 * @param shell the dialog parent shell	
	 * @param job the job to run
	 * @since 3.1
	 */
	public static void openInstaller(Shell shell, UpdateJob job) {
		new InstallWizardOperation().run(shell, job);
	}
}
