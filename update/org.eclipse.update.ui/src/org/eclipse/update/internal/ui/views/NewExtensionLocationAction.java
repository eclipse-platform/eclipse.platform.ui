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
import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.wizards.*;

public class NewExtensionLocationAction extends Action {
	
	class ExtensionSiteContentProvider extends MyComputerContentProvider {
		public Object[] getChildren(Object parent) {
			if (parent instanceof MyComputerDirectory) {
				return ((MyComputerDirectory) parent).getChildren(parent, true, false);
			}
			return super.getChildren(parent);
		}
	}

	public NewExtensionLocationAction(String text, ImageDescriptor desc) {
		super(text, desc);
	}

	public void run() {
		ElementTreeSelectionDialog dialog =
			new ElementTreeSelectionDialog(
				UpdateUI.getActiveWorkbenchShell(),
				new MyComputerLabelProvider(),
				new ExtensionSiteContentProvider());

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
						"", //$NON-NLS-1$
						null);
				return new Status(
					IStatus.ERROR,
					UpdateUI.getPluginId(),
					IStatus.ERROR,
					"", //$NON-NLS-1$
					null);
			}
		});
		dialog.setTitle(UpdateUI.getString("NewExtensionLocationAction.extLocation")); //$NON-NLS-1$
		dialog.setMessage(UpdateUI.getString("NewExtensionLocationAction.selectExtLocation")); //$NON-NLS-1$
		if (dialog.open() == ElementTreeSelectionDialog.OK) {
			addExtensionLocation((ExtensionRoot) dialog.getFirstResult());
		}

	}

	private void addExtensionLocation(ExtensionRoot root) {
		File dir = root.getInstallableDirectory();
		try {
			IInstallConfiguration config =
				UpdateUtils.createInstallConfiguration();
			if (TargetPage
				.addConfiguredSite(UpdateUI.getActiveWorkbenchShell(), config, dir, true)
				!= null) {
				UpdateUtils.makeConfigurationCurrent(config, null);
				UpdateUtils.saveLocalSite();
				UpdateUI.requestRestart();
			}
		} catch (CoreException e) {
			UpdateUI.logException(e);
		}
	}

}
