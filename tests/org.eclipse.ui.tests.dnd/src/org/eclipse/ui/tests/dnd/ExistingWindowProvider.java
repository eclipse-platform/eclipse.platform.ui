package org.eclipse.ui.tests.dnd;

import org.eclipse.ui.IWorkbenchWindow;

public class ExistingWindowProvider implements IWorkbenchWindowProvider {

	private final IWorkbenchWindow window;

	public ExistingWindowProvider(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public IWorkbenchWindow getWorkbenchWindow() {
		return window;
	}

}