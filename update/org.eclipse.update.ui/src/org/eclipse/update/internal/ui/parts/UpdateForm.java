package org.eclipse.update.internal.ui.parts;

import org.eclipse.update.ui.forms.ScrollableSectionForm;

public class UpdateForm extends ScrollableSectionForm implements IUpdateForm {
	private IUpdateFormPage page;
	
	public UpdateForm(IUpdateFormPage page) {
		this.page = page;
	}
	
	public IUpdateFormPage getPage() {
		return page;
	}
}

