package org.eclipse.update.internal.ui.pages;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.update.internal.ui.forms.MultipleSelectionForm;
import org.eclipse.update.internal.ui.views.DetailsView;
import org.eclipse.update.ui.forms.internal.IForm;


public class MultipleSelectionPage extends UpdateFormPage {
	
	public MultipleSelectionPage(DetailsView view, String title) {
		super(view, title);
	}
	
	public IForm createForm() {
		return new MultipleSelectionForm(this);
	}
}