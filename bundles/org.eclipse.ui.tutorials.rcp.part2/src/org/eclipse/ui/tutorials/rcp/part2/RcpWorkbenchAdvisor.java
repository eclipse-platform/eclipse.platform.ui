package org.eclipse.ui.tutorials.rcp.part2;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;

public class RcpWorkbenchAdvisor extends WorkbenchAdvisor {

	public String getInitialWindowPerspectiveId() {
		return "org.eclipse.ui.tutorials.rcp.part2.RcpPerspective"; //$NON-NLS-1$
	}

	public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
		super.preWindowOpen(configurer);
        configurer.setInitialSize(new Point(400, 300));
        configurer.setShowCoolBar(false);
        configurer.setShowStatusLine(false);
		configurer.setTitle(Messages.getString("Hello_RCP")); //$NON-NLS-1$
	}
}
