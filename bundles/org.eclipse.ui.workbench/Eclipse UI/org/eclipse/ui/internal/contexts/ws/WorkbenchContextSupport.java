package org.eclipse.ui.internal.contexts.ws;

import org.eclipse.ui.contexts.ContextActivationServiceFactory;
import org.eclipse.ui.contexts.ContextManagerFactory;
import org.eclipse.ui.contexts.ICompoundContextActivationService;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IMutableContextManager;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;

public class WorkbenchContextSupport implements IWorkbenchContextSupport {
	private ICompoundContextActivationService compoundContextActivationService;
	private IMutableContextManager mutableContextManager;

	public WorkbenchContextSupport() {
		mutableContextManager =
			ContextManagerFactory.getMutableContextManager();
		compoundContextActivationService =
			ContextActivationServiceFactory
				.getCompoundContextActivationService();
	}

	public ICompoundContextActivationService getCompoundContextActivationService() {
		return compoundContextActivationService;
	}

	public IContextManager getContextManager() {
		// TODO need to proxy this to prevent casts to IMutableContextManager
		return mutableContextManager;
	}
}
