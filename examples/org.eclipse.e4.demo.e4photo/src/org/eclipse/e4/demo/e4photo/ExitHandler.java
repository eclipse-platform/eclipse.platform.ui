package org.eclipse.e4.demo.e4photo;

import org.eclipse.e4.workbench.ui.IWorkbench;

public class ExitHandler {
	public void execute(IWorkbench workbench) {
		workbench.close();
	}
}
