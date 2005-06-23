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

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;


/**
 * This implementation of the workbench advisor tracks performance for the intervals between
 * lifecycle events.
 * 
 * @since 3.1
 */
public class OpenWorkbenchIntervalMonitor extends RCPTestWorkbenchAdvisor {

    public static final String[] intervalNames = new String[] {
        "open", //$NON-NLS-1$
        "close" //$NON-NLS-1$
	};
	
	public static final int openInterval = 0;
	public static final int closeInterval = 1;
	
	public static final int firstInterval = openInterval;
	public static final int finalInterval = closeInterval;

//    public static final String[] intervalNames = new String[] {
//            "to initialize", //$NON-NLS-1$
//            "initialize to preStartup", //$NON-NLS-1$
//            "preStartup to preWindowOpen", //$NON-NLS-1$
//            "preWindowOpen to fillActionBars", //$NON-NLS-1$
//            "fillActionBars to postWindowOpen", //$NON-NLS-1$
//            "postWindowOpen to postStartup", //$NON-NLS-1$
//            "preShutdown to postShutdown", //$NON-NLS-1$
//            "postShutdown to complete" //$NON-NLS-1$
//    };
//
//    public static final int initializeInterval = 0;
//    public static final int preStartupInterval = 1;
//    public static final int preWindowOpenInterval = 2;
//    public static final int fillActionBarsInterval = 3;
//    public static final int postWindowOpenInterval = 4;
//    public static final int postStartupInterval = 5;
//    public static final int shutdownInterval = 6;
//    public static final int workbenchDestroyedInterval = 7;
//
//    public static final int firstInterval = initializeInterval;
//    public static final int finalInterval = workbenchDestroyedInterval;

    private IPerformanceMeterArray meters;

    public OpenWorkbenchIntervalMonitor(IPerformanceMeterArray meters) {
        super(2);
        this.meters = meters;
    }

    public void initialize(IWorkbenchConfigurer configurer) {
//        meters.stop(initializeInterval);
//        meters.start(preStartupInterval);
        super.initialize(configurer);
    }

    public void preStartup() {
//        meters.stop(preStartupInterval);
//        meters.start(preWindowOpenInterval);
        super.preStartup();
    }

    public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
//        meters.stop(preWindowOpenInterval);
//        meters.start(fillActionBarsInterval);
        super.preWindowOpen(configurer);
    }

    public void fillActionBars(IWorkbenchWindow window, IActionBarConfigurer configurer, int flags) {
//        meters.stop(fillActionBarsInterval);
//        meters.start(postWindowOpenInterval);
        super.fillActionBars(window, configurer, flags);

    }

    public void postWindowOpen(IWorkbenchWindowConfigurer configurer) {
//        meters.stop(postWindowOpenInterval);
//        meters.start(postStartupInterval);
        super.postWindowOpen(configurer);
    }

    public void postStartup() {
//        meters.stop(postStartupInterval);
    	meters.stop(openInterval);
    	
        // no reason to track performace between when startup completes and shutdown starts
        // since that is just testing overhead

        super.postStartup();
    }

    public boolean preShutdown() {
//        meters.start(shutdownInterval);
    	meters.start(closeInterval);
        return super.preShutdown();
    }

    public void postShutdown() {
//        meters.stop(shutdownInterval);
//        meters.start(workbenchDestroyedInterval);
        super.postShutdown();
    }
}
