package org.eclipse.update.internal.ui.parts;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.ui.forms.internal.*;

public abstract class UpdateSection extends FormSection {
	private UpdateFormPage page;
	
	public UpdateSection(UpdateFormPage page) {
		this.page = page;
	}
	
	public UpdateFormPage getPage() {
		return page;
	}
}

