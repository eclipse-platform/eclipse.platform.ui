package org.eclipse.update.internal.ui.parts;

import org.eclipse.update.ui.forms.ScrollableForm;

public class UpdateForm extends ScrollableForm {
	private UpdateFormPage page;
	
	public UpdateForm(UpdateFormPage page) {
		this.page = page;
	}
	
	public UpdateFormPage getPage() {
		return page;
	}
}

