/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

/**
 * @version 	1.0
 * @author
 */
public class WizardProjectConfigurePage extends WizardPage {

	/**
	 * Constructor for WizardProjectConfigurePage.
	 * @param pageName
	 */
	protected WizardProjectConfigurePage(String pageName) {
		super(pageName);
	}

	/**
	 * Constructor for WizardProjectConfigurePage.
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	protected WizardProjectConfigurePage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
	}

}
