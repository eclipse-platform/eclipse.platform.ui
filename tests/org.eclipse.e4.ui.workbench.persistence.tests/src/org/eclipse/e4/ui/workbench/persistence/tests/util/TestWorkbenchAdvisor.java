package org.eclipse.e4.ui.workbench.persistence.tests.util;

import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.tests.harness.util.RCPTestWorkbenchAdvisor;

public class TestWorkbenchAdvisor extends RCPTestWorkbenchAdvisor{

	public TestWorkbenchAdvisor() {
		super();
	}

	@Override
	public String getInitialWindowPerspectiveId() {
		return TestPerspective.ID;
	}
	
	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		
	}
}