package org.eclipse.team.tests.ccvs.core;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.ui.PlatformUI;

public class CVSUITestSetup extends CVSTestSetup implements Test {

	public CVSUITestSetup(Test test) {
		super(test);
	}
	
	/* (non-Javadoc)
	 * @see junit.extensions.TestSetup#setUp()
	 */
	public void setUp() throws CoreException {
		super.setUp();
		PlatformUI.getWorkbench().getDecoratorManager().setEnabled(CVSUIPlugin.DECORATOR_ID, true);
	}
}
