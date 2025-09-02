package org.eclipse.ui.tests.dnd;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public abstract class TestDragSource implements IWorkbenchWindowProvider {
	private IWorkbenchPage page;

	@Override
	public abstract String toString();

	public abstract void drag(TestDropLocation target);

	public void setPage(IWorkbenchPage page) {
		this.page = page;
	}

	@Override
	public IWorkbenchWindow getWorkbenchWindow() {
		return getPage().getWorkbenchWindow();
	}

	public IWorkbenchPage getPage() {
		if (page == null) {
			page = PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		}
		return page;
	}


}