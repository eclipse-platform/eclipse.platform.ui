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
package org.eclipse.team.internal.ccvs.ui.wizards;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class UpdateWizardPage extends CVSWizardPage {
	
	private Button fetchAbsentDirectories;
	private Button pruneEmptyFolders;

	public UpdateWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		setDescription(Policy.bind("UpdateWizardPage.description"));
	}
	
	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 2);		
		fetchAbsentDirectories = createCheckBox(composite, "&Fetch absent directories");
		pruneEmptyFolders = createCheckBox(composite, "&Prune empty directories");
		initializeValues();
		
        Dialog.applyDialogFont(parent);
        setControl(composite);
	}
	
	private void initializeValues() {
		fetchAbsentDirectories.setSelection(true);
		pruneEmptyFolders.setSelection(CVSProviderPlugin.getPlugin().getPruneEmptyDirectories());
	}
	
	public LocalOption[] getLocalOptions() {
		List localOptions = new ArrayList();
		if (fetchAbsentDirectories.getSelection()) {
			localOptions.add(Update.RETRIEVE_ABSENT_DIRECTORIES);
		}
		if (pruneEmptyFolders.getSelection()) {
			localOptions.add(Update.PRUNE_EMPTY_DIRECTORIES);
		}
		return (LocalOption[]) localOptions.toArray(new LocalOption[localOptions.size()]);
	}
}
