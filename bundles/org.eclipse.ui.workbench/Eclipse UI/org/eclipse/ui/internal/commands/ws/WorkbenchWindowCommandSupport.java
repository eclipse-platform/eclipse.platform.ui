package org.eclipse.ui.internal.commands.ws;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextActivationService;
import org.eclipse.ui.contexts.IWorkbenchWindowContextSupport;

public class WorkbenchWindowCommandSupport
	implements IWorkbenchWindowContextSupport {
	private WorkbenchWindowCommandHandlerService workbenchWindowContextActivationService;	
	
	public WorkbenchWindowCommandSupport(IWorkbenchWindow workbenchWindow) {
		if (workbenchWindow == null)
			throw new NullPointerException();
			
		workbenchWindowContextActivationService = new WorkbenchWindowCommandHandlerService(workbenchWindow);
	}

	public IContextActivationService getContextActivationService() {
		return workbenchWindowContextActivationService;		
	}
}
