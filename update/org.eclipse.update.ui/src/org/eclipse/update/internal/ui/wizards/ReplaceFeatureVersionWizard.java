/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.ui.UpdateUIImages;
import org.eclipse.update.internal.ui.UpdateUIMessages;


public class ReplaceFeatureVersionWizard extends Wizard {
	private ReplaceFeatureVersionWizardPage page;

	public ReplaceFeatureVersionWizard(IFeature currentFeature, IFeature[] features) {
		setWindowTitle(UpdateUIMessages.SwapFeatureWizard_title); 
		setDefaultPageImageDescriptor(UpdateUIImages.DESC_UPDATE_WIZ);
		page = new ReplaceFeatureVersionWizardPage(currentFeature, features);
	}

	public void addPages() {
		addPage(page);
	}
	
	public boolean performFinish() {
		return page.performFinish();
	}

}
