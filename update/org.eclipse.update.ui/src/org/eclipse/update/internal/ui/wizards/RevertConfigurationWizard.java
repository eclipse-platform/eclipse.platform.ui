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
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.jface.wizard.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 * @author Wassim Melhem
 */
public class RevertConfigurationWizard extends Wizard {
	
	RevertConfigurationWizardPage page;

	public RevertConfigurationWizard() {
		super();
		setWindowTitle(UpdateUI.getString("RevertConfigurationWizard.wtitle")); //$NON-NLS-1$
		setDefaultPageImageDescriptor(UpdateUIImages.DESC_CONFIG_WIZ);
	}
	
	public void addPages() {
		page = new RevertConfigurationWizardPage();
		addPage(page);
	}

	public boolean performFinish() {
		return page.performFinish();
	}

}
