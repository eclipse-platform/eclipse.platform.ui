package org.eclipse.ui.internal.commands.ws;

import org.eclipse.ui.commands.CommandHandlerServiceFactory;
import org.eclipse.ui.commands.IMutableCommandHandlerService;
import org.eclipse.ui.commands.IWorkbenchPartSiteCommandSupport;

public class WorkbenchPartSiteCommandSupport
	implements IWorkbenchPartSiteCommandSupport {
	private IMutableCommandHandlerService mutableCommandHandlerService;

	public WorkbenchPartSiteCommandSupport() {
		mutableCommandHandlerService =
			CommandHandlerServiceFactory
				.getMutableCommandHandlerService();
	}

	public IMutableCommandHandlerService getMutableCommandHandlerService() {
		return mutableCommandHandlerService;
	}
}
