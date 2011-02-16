package org.eclipse.e4.demo.e4photo;

import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class PreferencesHandler {
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		MessageDialog.openInformation(shell, "e4photo", 
				"This should open the e4photo preferences");
	}

}
