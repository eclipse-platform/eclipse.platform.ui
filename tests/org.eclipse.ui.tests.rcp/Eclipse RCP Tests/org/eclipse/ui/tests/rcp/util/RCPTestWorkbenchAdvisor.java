/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.rcp.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
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
     * @see org.eclipse.ui.application.WorkbenchAdvisor#getInitialWindowPerspectiveId()
     */
    public String getInitialWindowPerspectiveId() {
        return EmptyPerspective.PERSP_ID;
    }

    public void eventLoopIdle(final Display display) {
        // bug 73184: On the mac the parent eventLoopIdle will put the display to sleep
        //            until there are events (e.g., mouse jiggled).
        if (!"carbon".equals(SWT.getPlatform()))
            super.eventLoopIdle(display);

        if (idleBeforeExit == -1)
            return;

        if (--idleBeforeExit <= 0)
            PlatformUI.getWorkbench().close();
    }
}
