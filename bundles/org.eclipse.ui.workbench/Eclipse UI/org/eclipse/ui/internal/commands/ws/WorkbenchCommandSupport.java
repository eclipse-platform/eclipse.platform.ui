package org.eclipse.ui.internal.commands.ws;

import org.eclipse.ui.commands.CommandHandlerServiceFactory;
import org.eclipse.ui.commands.CommandManagerFactory;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.ICompoundCommandHandlerService;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;

public class WorkbenchCommandSupport implements IWorkbenchCommandSupport {
	private ICompoundCommandHandlerService compoundCommandHandlerService;
	private /* TODO IMutableCommandManager */
	ICommandManager mutableCommandManager;

	public WorkbenchCommandSupport() {
		mutableCommandManager = CommandManagerFactory.getCommandManager();
		/* TODO getMutableCommandManager */
		compoundCommandHandlerService =
			CommandHandlerServiceFactory.getCompoundCommandHandlerService();
	}

	public ICommandManager getCommandManager() {
		// TODO need to proxy this to prevent casts to IMutableCommandManager
		return mutableCommandManager;
	}

	public ICompoundCommandHandlerService getCompoundCommandHandlerService() {
		return compoundCommandHandlerService;
	}
}
