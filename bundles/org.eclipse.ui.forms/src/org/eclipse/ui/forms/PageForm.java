/*
 * Created on Feb 28, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * This class extends managed form and speciealizes it for use
 * inside a multi-page form editor.
 */
public class PageForm extends ManagedForm {
	private FormPage page;
	/**
	 * @param toolkit
	 * @param form
	 */
	public PageForm(FormPage page, ScrolledForm form) {
		super(page.getEditor().getToolkit(), form);
		this.page = page;
	}
	
	public FormPage getPage() {
		return page;
	}
	
	public void markDirty() {
		//page.getEditor();
	}
}
