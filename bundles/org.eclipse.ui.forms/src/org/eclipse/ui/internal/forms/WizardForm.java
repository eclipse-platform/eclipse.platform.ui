/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.*;

/**
 *@since 3.0
 */
public class WizardForm extends ManagedForm {
	/**
	 * @param parent
	 */
	public WizardForm(WizardPage page, Composite parent) {
		super(parent);
		setContainer(page);
	}
	/**
	 * @param toolkit
	 * @param form
	 */
	public WizardForm(WizardPage page, FormToolkit toolkit, ScrolledForm form) {
		super(toolkit, form);
		setContainer(page);
	}
	public WizardPage getPage() {
		return (WizardPage)getContainer();
	}
}