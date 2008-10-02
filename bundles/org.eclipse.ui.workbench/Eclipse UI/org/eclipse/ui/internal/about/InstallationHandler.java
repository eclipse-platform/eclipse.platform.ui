package org.eclipse.ui.internal.about;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class InstallationHandler extends AbstractHandler{

	public Object execute(ExecutionEvent event) {		
		IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		if (workbenchWindow == null)
			workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		InstallationDialog dialog = new InstallationDialog(HandlerUtil.getActiveShell(event), workbenchWindow);
		dialog.open();
		return null;
	}

}
