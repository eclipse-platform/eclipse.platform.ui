package org.eclipse.update.internal.ui.parts;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.ui.forms.internal.ScrollableSectionForm;

public class UpdateForm extends ScrollableSectionForm implements IUpdateForm {
	private IUpdateFormPage page;
	
	public UpdateForm(IUpdateFormPage page) {
		this.page = page;
	}
	
	public IUpdateFormPage getPage() {
		return page;
	}
}

