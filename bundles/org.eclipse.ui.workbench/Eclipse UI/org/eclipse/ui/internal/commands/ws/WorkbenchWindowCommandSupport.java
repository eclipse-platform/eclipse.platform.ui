package org.eclipse.ui.internal.commands.ws;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandHandlerService;
import org.eclipse.ui.commands.IWorkbenchWindowCommandSupport;

public class WorkbenchWindowCommandSupport
	implements IWorkbenchWindowCommandSupport {
	private WorkbenchWindowCommandHandlerService workbenchWindowCommandHandlerService;

	public WorkbenchWindowCommandSupport(IWorkbenchWindow workbenchWindow) {
		if (workbenchWindow == null)
			throw new NullPointerException();

		workbenchWindowCommandHandlerService =
			new WorkbenchWindowCommandHandlerService(workbenchWindow);
	}

	public ICommandHandlerService getCommandHandlerService() {
		return workbenchWindowCommandHandlerService;
	}
}
