package org.eclipse.ui.internal.commands.ws;

import org.eclipse.ui.contexts.ContextActivationServiceFactory;
import org.eclipse.ui.contexts.IMutableContextActivationService;
import org.eclipse.ui.contexts.IWorkbenchPartSiteContextSupport;

public class WorkbenchPartSiteCommandSupport
	implements IWorkbenchPartSiteContextSupport {
	private IMutableContextActivationService mutableContextActivationService;

	public WorkbenchPartSiteCommandSupport() {
		mutableContextActivationService =
			ContextActivationServiceFactory
				.getMutableContextActivationService();
	}

	public IMutableContextActivationService getMutableContextActivationService() {
		return mutableContextActivationService;
	}
}
