package org.eclipse.update.internal.ui.parts;

import org.eclipse.update.ui.forms.*;

public abstract class UpdateSection extends FormSection {
	private UpdateFormPage page;
	
	public UpdateSection(UpdateFormPage page) {
		this.page = page;
	}
	
	public UpdateFormPage getPage() {
		return page;
	}
}

