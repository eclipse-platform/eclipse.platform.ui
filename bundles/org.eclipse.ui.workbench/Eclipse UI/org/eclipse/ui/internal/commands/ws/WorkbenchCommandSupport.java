package org.eclipse.ui.internal.commands.ws;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.contexts.ContextActivationServiceFactory;
import org.eclipse.ui.contexts.ContextManagerFactory;
import org.eclipse.ui.contexts.ICompoundContextActivationService;
import org.eclipse.ui.contexts.IContextActivationService;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IMutableContextManager;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;

public class WorkbenchCommandSupport implements IWorkbenchContextSupport {
	private IMutableContextManager mutableContextManager;
	private WorkbenchCommandHandlerService workbenchContextActivationService;	
	private ICompoundContextActivationService compoundContextActivationService;
	
	public WorkbenchCommandSupport(IWorkbench workbench) {
		if (workbench == null)
			throw new NullPointerException();
			
		mutableContextManager =
			ContextManagerFactory.getMutableContextManager();
		compoundContextActivationService =
		ContextActivationServiceFactory.getCompoundContextActivationService();
		workbenchContextActivationService = new WorkbenchCommandHandlerService(workbench);
	}

	public IContextManager getContextManager() {
		// TODO need to proxy this to prevent casts to IMutableContextManager
		return mutableContextManager;
	}
	
	public IContextActivationService getContextActivationService() {
		return workbenchContextActivationService;		
	}

	public ICompoundContextActivationService getCompoundContextActivationService() {
		return compoundContextActivationService;
	}
}
