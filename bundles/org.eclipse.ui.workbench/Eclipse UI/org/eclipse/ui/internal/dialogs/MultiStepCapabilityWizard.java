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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.InstallCapabilityStep.IProjectProvider;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public abstract class MultiStepCapabilityWizard extends MultiStepWizard implements IProjectProvider {
	/**
	 * Creates an empty wizard
	 */
	protected MultiStepCapabilityWizard() {
		super();
	}
	
	/**
	 * Returns the IDs of the perspectives to present
	 * as choices to the user.
	 */
	protected abstract String[] getPerspectiveChoices();
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		if (!super.performFinish())
			return false;
			
		// Allow the user to choose which perspective to
		// switch to.
		if (isConfigureStepMode()) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			String[] perspIds = getPerspectiveChoices();
			if (perspIds.length > 0) {
				ProjectPerspectiveChoiceDialog dialog;
				dialog = new ProjectPerspectiveChoiceDialog(window, perspIds);
				dialog.open();
				if (dialog.getReturnCode() == Dialog.OK)
					window = dialog.showChosenPerspective();
			}
			
			IProject project = getProject();
			if (project != null) {
				BasicNewResourceWizard.selectAndReveal(project, window);
			}
		}
			
		return true;
	}
}
