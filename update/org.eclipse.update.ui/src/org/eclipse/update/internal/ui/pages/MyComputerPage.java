package org.eclipse.update.internal.ui.pages;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.update.internal.ui.manager.MyComputerForm;
import org.eclipse.update.internal.ui.parts.UpdateFormPage;
import org.eclipse.update.internal.ui.views.DetailsView;
import org.eclipse.update.ui.forms.internal.*;


public class MyComputerPage extends UpdateFormPage {
	
	public MyComputerPage(DetailsView view, String title) {
		super(view, title);
	}
	
	public IForm createForm() {
		return new MyComputerForm(this);
	}
}