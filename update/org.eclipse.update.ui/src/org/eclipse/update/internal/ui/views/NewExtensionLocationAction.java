/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;

import java.io.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;

public class NewExtensionLocationAction extends Action {

	public NewExtensionLocationAction(String text, ImageDescriptor desc) {
		super(text, desc);
	}

	public void run() {
		DirectoryDialog dialog =
			new DirectoryDialog(UpdateUI.getActiveWorkbenchShell());
		dialog.setMessage(UpdateUI.getString("NewExtensionLocationAction.selectExtLocation")); //$NON-NLS-1$

		String dir = dialog.open();
		while (dir != null) {
			File dirFile = new File(dir);
			if (isExtensionRoot(dirFile)) {
				if (addExtensionLocation(dirFile))
					return;
				else {
					// re-open the directory dialog
					dialog.setFilterPath(dir);
					dir = dialog.open();
				}	
			} else {
				MessageDialog.openInformation(
					UpdateUI.getActiveWorkbenchShell(),
					UpdateUI.getString(
						"NewExtensionLocationAction.extInfoTitle"),
					UpdateUI.getString(
						"NewExtensionLocationAction.extInfoMessage"));
				// re-open the directory dialog
				dialog.setFilterPath(dir);
				dir = dialog.open();
			}
		}
	}

	static boolean isExtensionRoot(File directory) {
		File marker = new File(directory, ".eclipseextension"); //$NON-NLS-1$
		if (!marker.exists() || marker.isDirectory())
			return false;
		return true;
	}

	private boolean addExtensionLocation(File dir) {
		try {
			IInstallConfiguration config =
			UpdateUtils.createInstallConfiguration();
			IConfiguredSite csite = config.createLinkedConfiguredSite(dir);
			config.addConfiguredSite(csite);
			UpdateUtils.makeConfigurationCurrent(config, null);
			UpdateUtils.saveLocalSite();
			UpdateUI.requestRestart();
			return true;
		} catch (CoreException e) {
			String title = UpdateUI.getString("InstallWizard.TargetPage.location.error.title"); //$NON-NLS-1$
			ErrorDialog.openError(UpdateUI.getActiveWorkbenchShell(), title, null, e.getStatus());
			UpdateUI.logException(e,false);
			return false;
		}
	}

}
