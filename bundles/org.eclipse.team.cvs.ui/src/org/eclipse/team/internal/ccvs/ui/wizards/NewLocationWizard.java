/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.wizards;


import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.util.KnownRepositories;
import org.eclipse.team.internal.ccvs.ui.*;

public class NewLocationWizard extends Wizard {
	private ConfigurationWizardMainPage mainPage;

	private Properties properties = null;
	
	/**
	 * Return the settings used for all location pages
	 */
	public static IDialogSettings getLocationDialogSettings() {
		IDialogSettings workbenchSettings = CVSUIPlugin.getPlugin().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("NewLocationWizard");//$NON-NLS-1$
		if (section == null) {
			section = workbenchSettings.addNewSection("NewLocationWizard");//$NON-NLS-1$
		}
		return section;
	}
	
	public NewLocationWizard() {
		IDialogSettings section = getLocationDialogSettings();
		setDialogSettings(section);
		setWindowTitle(Policy.bind("NewLocationWizard.title")); //$NON-NLS-1$
	}
	

	public NewLocationWizard(Properties initialProperties) {
		this();
		this.properties = initialProperties;
	}

	/**
	 * Creates the wizard pages
	 */
	public void addPages() {
		mainPage = new ConfigurationWizardMainPage("repositoryPage1", Policy.bind("NewLocationWizard.heading"), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_NEW_LOCATION)); //$NON-NLS-1$ //$NON-NLS-2$
		if (properties != null) {
			mainPage.setProperties(properties);
		}
		mainPage.setShowValidate(true);
		mainPage.setDescription(Policy.bind("NewLocationWizard.description")); //$NON-NLS-1$
		mainPage.setDialogSettings(getDialogSettings());
		addPage(mainPage);
	}
	/*
	 * @see IWizard#performFinish
	 */
	public boolean performFinish() {
		final ICVSRepositoryLocation[] location = new ICVSRepositoryLocation[] { null };
		boolean keepLocation = false;
		try {
			// Create a handle to a repository location
			location[0] = mainPage.getLocation();
			// Add the location quitely so we can validate
			location[0] = KnownRepositories.getInstance().addRepository(location[0], false /* don't tell anybody */);
			
			if (mainPage.getValidate()) {
				try {
					new ProgressMonitorDialog(getShell()).run(true, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try {
								location[0].validateConnection(monitor);
							} catch (TeamException e) {
								throw new InvocationTargetException(e);
							}
						}
					});
					keepLocation = true;
				} catch (InterruptedException e) {
					// Cancelled by user. Fall through to dispose of location
				} catch (InvocationTargetException e) {
					Throwable t = e.getTargetException();
					if (t instanceof TeamException) {
						throw (TeamException)t;
					} else if (t instanceof Exception) {
						throw CVSException.wrapException((Exception)t);
					} else {
						throw CVSException.wrapException(e);
					}
				}
			} else {
				keepLocation = true;
			}
		} catch (TeamException e) {
			if (location[0] == null) {
				// Exception creating the root, we cannot continue
				CVSUIPlugin.openError(getContainer().getShell(), Policy.bind("NewLocationWizard.exception"), null, e); //$NON-NLS-1$
				return false;
			} else {
				// Exception validating. We can continue if the user wishes.
				IStatus error = e.getStatus();
				if (error.isMultiStatus() && error.getChildren().length == 1) {
					error = error.getChildren()[0];
				}
					
				if (error.isMultiStatus()) {
					CVSUIPlugin.openError(getContainer().getShell(), Policy.bind("NewLocationWizard.validationFailedTitle"), null, e); //$NON-NLS-1$
				} else {
					keepLocation = MessageDialog.openQuestion(getContainer().getShell(),
						Policy.bind("NewLocationWizard.validationFailedTitle"), //$NON-NLS-1$
						Policy.bind("NewLocationWizard.validationFailedText", new Object[] {error.getMessage()})); //$NON-NLS-1$
				}
			}
		}
		if (keepLocation) {
			KnownRepositories.getInstance().addRepository(location[0], true /* let the world know */);
		} else {
			KnownRepositories.getInstance().disposeRepository(location[0]);
		}
		return keepLocation;	
	}
}
