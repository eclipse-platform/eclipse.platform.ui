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
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;

public class NewWizard extends Wizard {
	protected BaseNewWizardPage page;

	public NewWizard(BaseNewWizardPage page, ImageDescriptor descriptor) {
		this.page = page;
		setDefaultPageImageDescriptor(descriptor);
	}
	
	public void addPages() {
		addPage(page);
	}

	public boolean performFinish() {
		return page.finish();
	}
}
