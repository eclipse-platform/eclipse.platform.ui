package org.eclipse.ui.internal.contexts.ws;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.contexts.ContextActivationServiceFactory;
import org.eclipse.ui.contexts.ContextManagerFactory;
import org.eclipse.ui.contexts.ICompoundContextActivationService;
import org.eclipse.ui.contexts.IContextActivationService;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IMutableContextManager;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;

public class WorkbenchContextSupport implements IWorkbenchContextSupport {
	private IMutableContextManager mutableContextManager;
	private WorkbenchContextActivationService workbenchContextActivationService;	
	private ICompoundContextActivationService compoundContextActivationService;
	
	public WorkbenchContextSupport(IWorkbench workbench) {
		if (workbench == null)
			throw new NullPointerException();
			
		mutableContextManager =
			ContextManagerFactory.getMutableContextManager();
		compoundContextActivationService =
		ContextActivationServiceFactory.getCompoundContextActivationService();
		workbenchContextActivationService = new WorkbenchContextActivationService(workbench);
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
