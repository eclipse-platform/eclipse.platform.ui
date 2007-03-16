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

import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @since 3.1
 */
public class DynamicPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * 
	 */
	public DynamicPreferencePage() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	public Point computeSize() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean okToLeave() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean performCancel() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean performOk() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setContainer(IPreferencePageContainer preferencePageContainer) {
		// TODO Auto-generated method stub
		
	}

	public void setSize(Point size) {
		// TODO Auto-generated method stub
		
	}

	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public Control getControl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	public void performHelp() {
		// TODO Auto-generated method stub
		
	}

	public void setDescription(String description) {
		// TODO Auto-generated method stub
		
	}

	public void setImageDescriptor(ImageDescriptor image) {
		// TODO Auto-generated method stub
		
	}

	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	public void setVisible(boolean visible) {
		// TODO Auto-generated method stub
		
	}

}
