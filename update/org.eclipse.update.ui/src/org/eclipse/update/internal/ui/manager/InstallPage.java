package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.UpdateFormPage;
import org.eclipse.update.ui.forms.*;

public class InstallPage extends UpdateFormPage {
	
	public InstallPage(UpdateManager manager, String title) {
		super(manager, title);
	}
	
	public Form createForm() {
		return new InstallForm(this);
	}
}