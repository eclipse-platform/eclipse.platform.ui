package org.eclipse.ui.internal.commands.ws;

import org.eclipse.ui.commands.CommandHandlerServiceFactory;
import org.eclipse.ui.commands.ICompoundCommandHandlerService;
import org.eclipse.ui.commands.IWorkbenchPageCommandSupport;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.WorkbenchPage;

public class WorkbenchPageCommandSupport
	implements IWorkbenchPageCommandSupport {
	private ICompoundCommandHandlerService compoundCommandHandlerService;
	private WorkbenchPage workbenchPage;

	public WorkbenchPageCommandSupport(WorkbenchPage workbenchPage) {
		if (workbenchPage == null)
			throw new NullPointerException();

		this.workbenchPage = workbenchPage;
		compoundCommandHandlerService =
			CommandHandlerServiceFactory.getCompoundCommandHandlerService();
	}

	public ICompoundCommandHandlerService getCompoundCommandHandlerService() {
		Perspective perspective = workbenchPage.getActivePerspective();

		if (perspective != null)
			return perspective.getCompoundCommandHandlerService();
		else
			return compoundCommandHandlerService;
	}
}
