/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.perfms;
//import java.net.*;

//import org.eclipse.update.configurator.*;
import org.eclipse.update.tests.*;

//import org.eclipse.perfmsr.core.PerfMsrCorePlugin;
//import org.eclipse.perfmsr.core.Upload;


public class TestStartup extends UpdateManagerTestCase {
	public TestStartup(String arg0) {
		super(arg0);
	}
	

	public void testPerfms() {
		/*
		 * this test takes snapshots before (1) and after (2) opening the Java
		 * Perspective. The delta between snapshots can be used to calculate the time required to open
		 * the Java perspective.  Disabled for now since the EclipseTestRunner is instrumented for performance
		 * monitoring.
		 */
		try {
	 	//	PerfMsrCorePlugin.getPerformanceMonitor(true).snapshot(1);
//			long s = System.currentTimeMillis();
//			URL perfURL = new URL("file", "", SOURCE_FILE_SITE + "perf");
//			IPlatformConfiguration config = ConfiguratorUtils.getPlatformConfiguration(perfURL);
//			long e = System.currentTimeMillis();
//			System.out.println("time=" + (e-s));
		//	PerfMsrCorePlugin.getPerformanceMonitor(true).snapshot(2);
		} catch (Exception e) {
		} 
	}
}
