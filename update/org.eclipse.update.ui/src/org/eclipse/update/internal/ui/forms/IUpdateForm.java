package org.eclipse.update.internal.ui.forms;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.internal.ui.pages.IUpdateFormPage;
import org.eclipse.update.ui.forms.internal.IForm;

public interface IUpdateForm extends IForm {
	public IUpdateFormPage getPage();

}

