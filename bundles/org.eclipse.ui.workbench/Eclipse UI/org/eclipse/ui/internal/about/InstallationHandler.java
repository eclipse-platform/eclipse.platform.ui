package org.eclipse.ui.internal.about;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.handlers.HandlerUtil;

public class InstallationHandler extends AbstractHandler{

	public Object execute(ExecutionEvent event) {		
		InstallationDialog dialog = new InstallationDialog(HandlerUtil.getActiveShell(event), HandlerUtil.getActiveWorkbenchWindow(event));
		dialog.open();
		return null;
	}

}
