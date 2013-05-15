/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.tests.wizards;

import org.eclipse.jface.wizard.Wizard;


public class TheTestWizard extends Wizard {
	public TheTestWizardPage page1;
	public TheTestWizardPage page2;
	public TheTestWizardPage page3;
	public final String page1Name = "PAGE1";
	public final String page2Name = "PAGE2";
	public final String page3Name = "PAGE3";
	private boolean throwExceptionOnDispose; 

	public TheTestWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page1 = new TheTestWizardPage(page1Name);
		addPage(page1);
		page2 = new TheTestWizardPage(page2Name);
		addPage(page2);
		page3 = new TheTestWizardPage(page3Name);
		addPage(page3);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. 
	 */
	public boolean performFinish() {
		WizardTest.DID_FINISH = true;
		return true;
	}
	
	/**
	 * @param throwExceptionOnDispose The throwExceptionOnDispose to set.
	 */
	public void setThrowExceptionOnDispose(boolean throwExceptionOnDispose) {
		this.throwExceptionOnDispose = throwExceptionOnDispose;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#dispose()
	 */
	public void dispose() {
		super.dispose();
		if(throwExceptionOnDispose)
			throw new NullPointerException();
	}
}