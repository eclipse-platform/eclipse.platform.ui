package org.eclipse.ui.internal.contexts.ws;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextActivationService;
import org.eclipse.ui.contexts.IWorkbenchWindowContextSupport;

public class WorkbenchWindowContextSupport
	implements IWorkbenchWindowContextSupport {
	private WorkbenchWindowContextActivationService workbenchWindowContextActivationService;

	public WorkbenchWindowContextSupport(IWorkbenchWindow workbenchWindow) {
		if (workbenchWindow == null)
			throw new NullPointerException();

		workbenchWindowContextActivationService =
			new WorkbenchWindowContextActivationService(workbenchWindow);
	}

	public IContextActivationService getContextActivationService() {
		return workbenchWindowContextActivationService;
	}
}
