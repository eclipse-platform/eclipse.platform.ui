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
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.operations.*;

public class NewExtensionLocationAction extends Action {

	public NewExtensionLocationAction(String text, ImageDescriptor desc) {
		super(text, desc);
	}

	public void run() {
		
		IStatus status = OperationsManager.getValidator().validatePlatformConfigValid();
		if (status != null) {
			ErrorDialog.openError(
					UpdateUI.getActiveWorkbenchShell(),
					null,
					null,
					status);
			return;
		}
		
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

	/**
	 * @param directory
	 * @return true when directory is an eclipse exstension
	 */
	static boolean isExtensionRoot(File directory) {
		// Check the eclipse folder
		File parent = new File(directory, "eclipse"); //$NON-NLS-1$
		if (!parent.exists() || !parent.isDirectory())
			return false;

		// check the marker
		File marker = new File(parent, ".eclipseextension"); //$NON-NLS-1$
		if (!marker.exists() || marker.isDirectory())
			return false;
		return true;
	}

	private boolean addExtensionLocation(File dir) {
		try {
			dir = new File(dir, "eclipse");
			IInstallConfiguration config = SiteManager.getLocalSite().getCurrentConfiguration();
			IConfiguredSite csite = config.createLinkedConfiguredSite(dir);
			config.addConfiguredSite(csite);
			boolean restartNeeded = SiteManager.getLocalSite().save();
			UpdateUI.requestRestart(restartNeeded);
			return true;
		} catch (CoreException e) {
			String title = UpdateUI.getString("InstallWizard.TargetPage.location.error.title"); //$NON-NLS-1$
			ErrorDialog.openError(UpdateUI.getActiveWorkbenchShell(), title, null, e.getStatus());
			UpdateUI.logException(e,false);
			return false;
		}
	}

}
