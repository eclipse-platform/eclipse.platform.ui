package org.eclipse.ui.internal.contexts.ws;

import org.eclipse.ui.contexts.ContextActivationServiceFactory;
import org.eclipse.ui.contexts.ICompoundContextActivationService;
import org.eclipse.ui.contexts.IWorkbenchPageContextSupport;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.WorkbenchPage;

public class WorkbenchPageContextSupport
	implements IWorkbenchPageContextSupport {
	private ICompoundContextActivationService compoundActivityService;
	private WorkbenchPage workbenchPage;
	
	public WorkbenchPageContextSupport(WorkbenchPage workbenchPage) {
		if (workbenchPage == null)
			throw new NullPointerException();
		
		this.workbenchPage = workbenchPage;
		compoundActivityService =
		ContextActivationServiceFactory
		.getCompoundContextActivationService();
	}

	public ICompoundContextActivationService getCompoundContextActivationService() {
		Perspective perspective = workbenchPage.getActivePerspective();
		
		if (perspective != null)
			return perspective.getCompoundContextActivationService();
		else 
			return compoundActivityService;
	}	
}
