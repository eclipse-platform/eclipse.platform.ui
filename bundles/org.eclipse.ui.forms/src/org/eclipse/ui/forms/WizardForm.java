/*
 * Created on Mar 3, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
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