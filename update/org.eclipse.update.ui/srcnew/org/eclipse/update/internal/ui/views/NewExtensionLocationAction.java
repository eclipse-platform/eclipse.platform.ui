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

import java.io.File;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.update.configuration.IInstallConfiguration;
import org.eclipse.update.internal.operations.UpdateManager;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.wizards.*;

public class NewExtensionLocationAction extends Action {

	public NewExtensionLocationAction(String text, ImageDescriptor desc) {
		super(text, desc);
	}

	public void run() {
		ElementTreeSelectionDialog dialog =
			new ElementTreeSelectionDialog(
				UpdateUI.getActiveWorkbenchShell(),
				new MyComputerLabelProvider(),
				new MyComputerContentProvider());

		dialog.setInput(new MyComputer());
		dialog.setAllowMultiple(false);
		dialog.addFilter(new ViewerFilter() {
			public boolean select(
				Viewer viewer,
				Object parentElement,
				Object element) {
				return !(element instanceof MyComputerFile);
			}
		});
		dialog.setValidator(new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				if (selection.length == 1
					&& selection[0] instanceof ExtensionRoot)
					return new Status(
						IStatus.OK,
						UpdateUI.getPluginId(),
						IStatus.OK,
						"",
						null);
				return new Status(
					IStatus.ERROR,
					UpdateUI.getPluginId(),
					IStatus.ERROR,
					"",
					null);
			}
		});
		dialog.setTitle("Extension Location");
		dialog.setMessage("&Select an extension location:");
		if (dialog.open() == ElementTreeSelectionDialog.OK) {
			addExtensionLocation((ExtensionRoot) dialog.getFirstResult());
		}

	}

	private void addExtensionLocation(ExtensionRoot root) {
		File dir = root.getInstallableDirectory();
		try {
			IInstallConfiguration config =
				UpdateManager.createInstallConfiguration();
			if (UnifiedTargetPage
				.addConfiguredSite(UpdateUI.getActiveWorkbenchShell(), config, dir, true)
				!= null) {
				UpdateManager.makeConfigurationCurrent(config, null);
				UpdateManager.saveLocalSite();
				UpdateUI.requestRestart();
			}
		} catch (CoreException e) {
			UpdateUI.logException(e);
		}
	}

}
