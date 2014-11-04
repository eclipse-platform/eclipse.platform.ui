/*******************************************************************************
 * Copyright (c) 2006, 2010 Soyatec(http://www.soyatec.com) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Soyatec - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.project;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.internal.ui.wizards.plugin.AbstractFieldData;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class E4NewProjectWizardPage extends org.eclipse.pde.internal.ui.wizards.plugin.NewProjectCreationPage {

	public E4NewProjectWizardPage(String pageName, AbstractFieldData data, boolean fragment,
		IStructuredSelection selection) {
		super(pageName, data, fragment, selection);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		fOSGIButton.setSelection(true);
		fEclipseButton.setSelection(false);
		fEclipseButton.setEnabled(false);
		fEclipseCombo.setEnabled(false);
	}
}
