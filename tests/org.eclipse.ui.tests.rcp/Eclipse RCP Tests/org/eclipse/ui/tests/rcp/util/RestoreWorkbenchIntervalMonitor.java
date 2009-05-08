/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.rcp.util;

import org.eclipse.swt.widgets.Display;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.tests.harness.util.RCPTestWorkbenchAdvisor;


/**
 * This implementation of the workbench advisor tracks performance for the intervals between
 * lifecycle events.
 * 
 * @since 3.1
 */
public class RestoreWorkbenchIntervalMonitor extends RCPTestWorkbenchAdvisor {

    private PerformanceMeter startupMeter; 
    private PerformanceMeter shutdownMeter; 
	
	private boolean createRestorableWorkbench = false;
	
    private IWorkbenchConfigurer workbenchConfigurer;

    /**
     * The default behavior is to create a workbench that can be restored later.  This
     * constructor starts that behavior by setting a flag that will be checked in the
     * appropriate methods.
     */
    public RestoreWorkbenchIntervalMonitor(PerformanceMeter startupMeter, PerformanceMeter shutdownMeter, boolean createRestorableWorkbench) {
        super(2);
        this.startupMeter = startupMeter;
        this.shutdownMeter = shutdownMeter;
        this.createRestorableWorkbench = createRestorableWorkbench;
    }

    public void initialize(IWorkbenchConfigurer configurer) {
        super.initialize(configurer);
        workbenchConfigurer = configurer;
        workbenchConfigurer.setSaveAndRestore(true);
    }

    public void postStartup() {
    	startupMeter.stop();
        // no reason to track performance between when startup completes and shutdown starts
        // since that is just testing overhead
        super.postStartup();
    }

    public boolean preShutdown() {
        boolean ret = super.preShutdown();
        shutdownMeter.start();
        return ret;
    }

    public void eventLoopIdle(Display d) {
        if (createRestorableWorkbench)
            workbenchConfigurer.getWorkbench().restart();
        else
            super.eventLoopIdle(d);
    }
}
