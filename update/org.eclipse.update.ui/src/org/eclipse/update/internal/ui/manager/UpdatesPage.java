package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.UpdateFormPage;
import org.eclipse.update.ui.forms.*;

public class UpdatesPage extends UpdateFormPage {
	
	public UpdatesPage(UpdateManager manager, String title) {
		super(manager, title);
	}
	
	public Form createForm() {
		return new UpdatesForm(this);
	}
}