package org.eclipse.update.internal.ui.parts;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.ui.forms.internal.*;
import java.util.Hashtable;

public class UpdateWebForm extends WebForm implements IUpdateForm {
	private IUpdateFormPage page;
	
	public UpdateWebForm(IUpdateFormPage page) {
		this.page = page;
	}
	
	public IUpdateFormPage getPage() {
		return page;
	}
}

