/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.wizards;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

/**
 * This is the wizard page to create a new Hockeyleague model file.
 * 
 * @author Anthony Hunter
 */
public class NewHockeyleagueFileWizardPage
	extends WizardNewFileCreationPage {

	/**
	 * The Hockey league file.
	 */
	protected IFile hockeyleagueFile;

	/**
	 * @param pageName
	 * @param selection
	 */
	public NewHockeyleagueFileWizardPage(String pageName,
			IStructuredSelection selection) {
		super(pageName, selection);
	}

	/**
	 * The framework calls this to see if the file is correct.
	 */
	protected boolean validatePage() {
		if (super.validatePage()) {
			// Make sure the file ends in ".hockeyleague".
			//
			String requiredExtStatic = "hockeyleague"; //$NON-NLS-1$
			String enteredExt = new Path(getFileName()).getFileExtension();
			if (enteredExt == null
				|| !(enteredExt.equals(requiredExtStatic))) {
				setErrorMessage(MessageFormat.format(
					"The filename must end in \".{0}\"",//$NON-NLS-1$
					new Object[] {requiredExtStatic}));
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * Store the dialog field settings upon completion.
	 */
	public boolean performFinish() {
		hockeyleagueFile = getHockeyleagueFile();
		return true;
	}

	/**
	 */
	public IFile getHockeyleagueFile() {
		return hockeyleagueFile == null ? ResourcesPlugin.getWorkspace()
			.getRoot().getFile(getContainerFullPath().append(getFileName()))
			: hockeyleagueFile;
	}
}