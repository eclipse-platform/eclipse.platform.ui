/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.rcp.util;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;


/**
 * This advisor adds the ability to exit the workbench after it has started up.  This is done
 * with a call to close from within the advisor's event idle loop.  The number of times the
 * idle is called before exiting can be configured.  Test cases should subclass this advisor
 * and add their own callback methods if needed.
 * 
 * @since 3.1
 */
public class RCPTestWorkbenchAdvisor extends WorkbenchAdvisor {

    /** Default value of -1 causes the option to be ignored. */
    private int idleBeforeExit = -1;

    public RCPTestWorkbenchAdvisor() {
        // default value means the advisor will not trigger the workbench to close
        this.idleBeforeExit = -1;
    }

    public RCPTestWorkbenchAdvisor(int idleBeforeExit) {
        this.idleBeforeExit = idleBeforeExit;
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#initialize(org.eclipse.ui.application.IWorkbenchConfigurer)
	 */
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);

		// The RCP tests are currently run in the context of the Platform product, which specifies
		// the Resource perspective as the default, and also reports progress on startup.  
		// We don't want either in effect when running the RCP tests.
		// Also disable intro.
		IPreferenceStore prefs = PlatformUI.getPreferenceStore();
		prefs.setValue(IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID, "");
		prefs.setValue(IWorkbenchPreferenceConstants.SHOW_PROGRESS_ON_STARTUP, false);
		prefs.setValue(IWorkbenchPreferenceConstants.SHOW_INTRO, false);
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#getInitialWindowPerspectiveId()
     */
    public String getInitialWindowPerspectiveId() {
        return EmptyPerspective.PERSP_ID;
    }

    public void eventLoopIdle(final Display display) {
    	// Bug 107369: RCP test suite hangs on GTK
        if (idleBeforeExit!=-1 && --idleBeforeExit <= 0)
            PlatformUI.getWorkbench().close();

        // bug 73184: On the mac the parent eventLoopIdle will put the display to sleep
        //            until there are events (e.g., mouse jiggled).
        if (!"carbon".equals(SWT.getPlatform()))
            super.eventLoopIdle(display);

        if (idleBeforeExit == -1)
            return;
    }
}
