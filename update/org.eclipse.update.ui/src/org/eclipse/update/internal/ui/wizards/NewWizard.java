package org.eclipse.update.internal.ui.wizards;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.internal.SetPagePerspectiveAction;
import org.eclipse.update.internal.ui.model.BookmarkFolder;
import org.eclipse.jface.resource.ImageDescriptor;

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