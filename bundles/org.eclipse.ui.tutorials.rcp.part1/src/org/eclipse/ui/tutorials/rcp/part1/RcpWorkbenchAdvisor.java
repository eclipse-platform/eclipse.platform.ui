package org.eclipse.ui.tutorials.rcp.part1;
import org.eclipse.ui.application.WorkbenchAdvisor;

public class RcpWorkbenchAdvisor extends WorkbenchAdvisor {

	public String getInitialWindowPerspectiveId() {
		return "org.eclipse.ui.tutorials.rcp.part1.RcpPerspective"; //$NON-NLS-1$
	}
}
