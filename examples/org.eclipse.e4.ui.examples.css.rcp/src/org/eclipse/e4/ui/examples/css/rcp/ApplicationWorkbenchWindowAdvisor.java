package org.eclipse.e4.ui.examples.css.rcp;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private final Point INITIAL_SIZE = new Point(600, 400);
	
    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }
    
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(INITIAL_SIZE);
        configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(false);
    }

    public void postWindowOpen() {
    	//workaround needed for bug #260791
    	getWindowConfigurer().getWindow().getShell().setSize(INITIAL_SIZE);
    }

}
