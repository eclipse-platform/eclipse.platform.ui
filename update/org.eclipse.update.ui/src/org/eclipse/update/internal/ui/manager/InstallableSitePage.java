package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.internal.ui.parts.UpdateFormPage;
import org.eclipse.update.ui.forms.internal.*;

public class InstallableSitePage extends UpdateFormPage {
	
	public InstallableSitePage(DetailsView view, String title) {
		super(view, title);
	}
	
	public IForm createForm() {
		return new InstallableSiteForm(this);
	}
}