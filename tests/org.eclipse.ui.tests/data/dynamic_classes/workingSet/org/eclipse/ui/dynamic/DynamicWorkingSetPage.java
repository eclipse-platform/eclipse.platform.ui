/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dynamic;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.IWorkingSetPage;

/**
 * @since 3.1
 */
public class DynamicWorkingSetPage implements IWorkingSetPage {

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	@Override
	public IWorkingSet getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSelection(IWorkingSet workingSet) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canFlipToNextPage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWizardPage getNextPage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWizardPage getPreviousPage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWizard getWizard() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPageComplete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPreviousPage(IWizardPage page) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWizard(IWizard newWizard) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Control getControl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void performHelp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setImageDescriptor(ImageDescriptor image) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVisible(boolean visible) {
		// TODO Auto-generated method stub

	}

}
