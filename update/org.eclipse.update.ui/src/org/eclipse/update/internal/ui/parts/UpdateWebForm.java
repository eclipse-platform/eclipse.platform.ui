package org.eclipse.update.internal.ui.parts;

import org.eclipse.update.ui.forms.*;

public class UpdateWebForm extends WebForm implements IUpdateForm {
	private IUpdateFormPage page;
	
	public UpdateWebForm(IUpdateFormPage page) {
		this.page = page;
	}
	
	public IUpdateFormPage getPage() {
		return page;
	}
}

