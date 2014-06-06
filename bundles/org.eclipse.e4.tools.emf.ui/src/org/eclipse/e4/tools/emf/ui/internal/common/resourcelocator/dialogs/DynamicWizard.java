/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs;

import java.util.ArrayList;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;

/**
 * Adds features for wizards with dynamic pages. The <em>pages</em> member is
 * private in the base class...
 *
 * @author Steven Spungin
 *
 */
public class DynamicWizard extends Wizard {

	protected ArrayList<IWizardPage> pages = new ArrayList<IWizardPage>();
	private String message;

	public DynamicWizard() {
		setForcePreviousAndNextButtons(true);
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public boolean canFinish() {
		for (IWizardPage page : pages) {
			if (page.isPageComplete() == false) {
				return false;
			}
		}
		IWizardPage cur = getContainer().getCurrentPage();
		return getNextPage(cur) == null;
	}

	@Override
	public void addPage(IWizardPage page) {
		page.setWizard(this);
		pages.add(page);

		// Only add the first page to the base class
		if (super.getPageCount() == 0) {
			super.addPage(page);
			updateMessage();
		}

	}

	/**
	 * Override the message on the first page if specified
	 *
	 * @param page
	 */
	private void updateMessage() {
		if (E.notEmpty(message) && pages.size() > 0) {
			// TODO file bug: IWizardPage is missing the setMessage method!
			IWizardPage page = pages.get(0);
			if (page instanceof WizardPage) {
				WizardPage wizPage = (WizardPage) page;
				wizPage.setMessage(message);
			}
		}
	}

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		IWizardPage cur = getContainer().getCurrentPage();
		int index = pages.indexOf(cur);
		if (index > 0) {
			return pages.get(index - 1);
		} else {
			return null;
		}
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		int index = pages.indexOf(page);
		if (index < pages.size() - 1) {
			return pages.get(index + 1);
		} else {
			return null;
		}
	}

	public void clearDynamicPages() {
		while (pages.size() > 1) {
			pages.remove(pages.size() - 1);
		}

	}

	/**
	 * Overrides the message of the first page if not <em>empty</em>
	 *
	 * @param message
	 */
	public void setMessage(String message) {
		this.message = message;
		updateMessage();
	}

}
