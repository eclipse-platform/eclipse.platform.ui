package org.eclipse.update.internal.ui.parts;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.ui.forms.internal.WebForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;

public class UpdateWebForm extends WebForm implements IUpdateForm {
	private IUpdateFormPage page;
	
	public UpdateWebForm(IUpdateFormPage page) {
		this.page = page;
	}
	
	public IUpdateFormPage getPage() {
		return page;
	}
	
	public void initialize(Object model) {
		super.initialize(model);
		refreshSize();
	}
	protected void refreshSize() {
		((Composite)getControl()).layout();
		updateSize();
	}
}

