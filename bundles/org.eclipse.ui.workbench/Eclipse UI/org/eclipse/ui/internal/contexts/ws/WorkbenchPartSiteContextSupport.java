package org.eclipse.ui.internal.contexts.ws;

import org.eclipse.ui.contexts.ContextActivationServiceFactory;
import org.eclipse.ui.contexts.IMutableContextActivationService;
import org.eclipse.ui.contexts.IWorkbenchPartSiteContextSupport;

public class WorkbenchPartSiteContextSupport
	implements IWorkbenchPartSiteContextSupport {
	private IMutableContextActivationService mutableContextActivationService;

	public WorkbenchPartSiteContextSupport() {
		mutableContextActivationService =
			ContextActivationServiceFactory
				.getMutableContextActivationService();
	}

	public IMutableContextActivationService getMutableContextActivationService() {
		return mutableContextActivationService;
	}
}
