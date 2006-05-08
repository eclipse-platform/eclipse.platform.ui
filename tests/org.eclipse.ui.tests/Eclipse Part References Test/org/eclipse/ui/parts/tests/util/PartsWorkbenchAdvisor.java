/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *******************************************************************************/
package org.eclipse.ui.parts.tests.util;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.harness.util.PlatformUtil;

/**
 * Workbench advisor for the parts test suite.
 */
public abstract class PartsWorkbenchAdvisor extends WorkbenchAdvisor {

    /**
     * Constructor.
     */
    public PartsWorkbenchAdvisor() {
        // Do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor#createWorkbenchWindowAdvisor(org.eclipse.ui.application.IWorkbenchWindowConfigurer)
     */
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new PartsWorkbenchWindowAdvisor(this, configurer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.application.WorkbenchAdvisor#eventLoopIdle(org.eclipse.swt.widgets.Display)
     */
    public void eventLoopIdle(Display display) {
        if(!PlatformUtil.onMac()) {
            super.eventLoopIdle(display);
        }
        PlatformUI.getWorkbench().close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.application.WorkbenchAdvisor#initialize
     */
    public void initialize(IWorkbenchConfigurer configurer) {
        // make sure we always save and restore workspace state
        configurer.setSaveAndRestore(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.application.WorkbenchAdvisor#preStartup()
     */
    public void preStartup() {
        // Overwrite preference to set empty pespective as our default
        IPerspectiveRegistry fReg = PlatformUI.getWorkbench()
                .getPerspectiveRegistry();
        fReg.setDefaultPerspective(EmptyPerspective.PERSP_ID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.application.WorkbenchAdvisor
     */
    public String getInitialWindowPerspectiveId() {
        return EmptyPerspective.PERSP_ID;
    }

    /**
     * Validates the layout of the page in a new window.
     * Tests override this.
     */
    protected abstract void validate(IWorkbenchPage page);
}
