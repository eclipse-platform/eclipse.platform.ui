package org.eclipse.update.internal.ui.pages;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.update.internal.ui.forms.ExtensionRootForm;
import org.eclipse.update.internal.ui.views.DetailsView;
import org.eclipse.update.ui.forms.internal.IForm;


public class ExtensionRootPage extends UpdateFormPage {
	
	public ExtensionRootPage(DetailsView view, String title) {
		super(view, title);
	}
	
	public IForm createForm() {
		return new ExtensionRootForm(this);
	}
}