/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433608
 *******************************************************************************/

package org.eclipse.jface.tests.wizards;

import org.eclipse.jface.wizard.Wizard;


public class TheTestWizard extends Wizard {
	public TheTestWizardPage page1;
	public TheTestWizardPage page2;
	public TheTestWizardPage page3;
	public static final String page1Name = "PAGE1";
	public static final String page2Name = "PAGE2";
	public static final String page3Name = "PAGE3";
	private boolean throwExceptionOnDispose;

	public TheTestWizard() {
		super();
		setNeedsProgressMonitor(true);
	}


	/**
	 * Adding the page to the wizard.
	 */
	@Override
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
	@Override
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

	@Override
	public void dispose() {
		super.dispose();
		if(throwExceptionOnDispose) {
			throw new NullPointerException();
		}
	}
}