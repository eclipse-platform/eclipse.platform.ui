/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.ant.ui.internal.toolscripts;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * For use in a tool script wizard.  Prompts for:
 *  - script name, which can be any file in the filesystem or workbench.  
 *  - script parameters, which is an arbitrary string
 * Enables finishing when any script name has been provided.
 */
public class ChooseToolScriptPage extends WizardPage {
/**
 * Constructor for ChooseToolScriptPage.
 * @param pageName
 */
protected ChooseToolScriptPage(String pageName) {
	super(pageName);
}

/**
 * Constructor for ChooseToolScriptPage.
 * @param pageName
 * @param title
 * @param titleImage
 */
protected ChooseToolScriptPage(String pageName, String title, ImageDescriptor titleImage) {
	super(pageName, title, titleImage);
}
/*
 * @see IDialogPage#createControl(Composite)
 */
public void createControl(Composite parent) {
	Composite topLevel = new Composite(parent, SWT.NONE);
	
	setControl(topLevel);
	
}
}
