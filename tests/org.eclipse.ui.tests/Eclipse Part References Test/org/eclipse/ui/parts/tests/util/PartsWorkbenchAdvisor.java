/******************************************************************************* 
 * Copyright (c) 2003, 2004 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *     IBM Corporation - initial API and implementation 
 *******************************************************************************/
package org.eclipse.ui.parts.tests.util;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.ide.WorkbenchActionBuilder;
import org.eclipse.ui.tests.util.EmptyPerspective;
import org.eclipse.ui.tests.util.PlatformUtil;

/**
 * Workbench advisor for the parts test suite.
 */
public class PartsWorkbenchAdvisor extends WorkbenchAdvisor {
    private static final String ACTION_BUILDER = "ActionBuilder"; //$NON-NLS-1$

    /**
     * Constructor.
     *  
     */
    protected PartsWorkbenchAdvisor() {
        // Do nothing
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.application.WorkbenchAdvisor#fillActionBars(org.eclipse.ui.IWorkbenchWindow,
     *      org.eclipse.ui.application.IActionBarConfigurer, int)
     */
    public void fillActionBars(IWorkbenchWindow window,
            IActionBarConfigurer actionConfigurer, int flags) {

        // setup the action builder to populate the toolbar and menubar in the
        // configurer
        WorkbenchActionBuilder actionBuilder = null;
        IWorkbenchWindowConfigurer windowConfigurer = getWorkbenchConfigurer()
                .getWindowConfigurer(window);

        // For proxy calls to this method it is important that we use the same
        // object
        // associated with the windowConfigurer
        actionBuilder = (WorkbenchActionBuilder) windowConfigurer
                .getData(ACTION_BUILDER);
        if (actionBuilder == null) {
            actionBuilder = new WorkbenchActionBuilder(window);
        }

        if ((flags & FILL_PROXY) != 0) {
            // Filling in fake actionbars
            if ((flags & FILL_MENU_BAR) != 0) {
                actionBuilder.populateMenuBar(actionConfigurer);
            }
            if ((flags & FILL_COOL_BAR) != 0) {
                actionBuilder.populateCoolBar(actionConfigurer);
            }
        } else {
            // make, fill, and hook listeners to action builder
            // reference to IWorkbenchConfigurer is need for the ABOUT action
            windowConfigurer.setData(ACTION_BUILDER, actionBuilder);
            actionBuilder.makeAndPopulateActions(getWorkbenchConfigurer(),
                    actionConfigurer);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.application.WorkbenchAdvisor#openIntro(org.eclipse.ui.application.IWorkbenchWindowConfigurer)
     */
    public void openIntro(IWorkbenchWindowConfigurer windowConfigurer) {
        // Do not open any intro part
    }

}