/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.jface.wizard.*;
import org.eclipse.update.internal.ui.*;

/**
 * @author Wassim Melhem
 */
public class RevertConfigurationWizard extends Wizard {
	
	RevertConfigurationWizardPage page;

	public RevertConfigurationWizard() {
		super();
		setWindowTitle(UpdateUIMessages.RevertConfigurationWizard_wtitle); 
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
