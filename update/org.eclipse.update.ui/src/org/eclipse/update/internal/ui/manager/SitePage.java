package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.UpdateFormPage;
import org.eclipse.update.ui.forms.*;

public class SitePage extends UpdateFormPage {
	
	public SitePage(DetailsView view, String title) {
		super(view, title);
	}
	
	public IForm createForm() {
		return new SiteForm(this);
	}
}