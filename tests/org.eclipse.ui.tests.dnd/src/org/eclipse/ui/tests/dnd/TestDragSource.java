package org.eclipse.ui.tests.dnd;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;

public abstract class TestDragSource implements IWorkbenchWindowProvider {
	private WorkbenchPage page;

	@Override
	public abstract String toString();

	public abstract void drag(TestDropLocation target);

	public void setPage(WorkbenchPage page) {
		this.page = page;
	}

	@Override
	public IWorkbenchWindow getWorkbenchWindow() {
		return getPage().getWorkbenchWindow();
	}

	public WorkbenchPage getPage() {
		if (page == null) {
			page = (WorkbenchPage) ((WorkbenchWindow) PlatformUI
					.getWorkbench().getActiveWorkbenchWindow()).getActivePage();
		}
		return page;
	}


}