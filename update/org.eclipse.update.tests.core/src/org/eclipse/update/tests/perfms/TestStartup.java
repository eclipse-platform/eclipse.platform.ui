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

import java.io.*;
import java.net.*;

import org.eclipse.update.configurator.*;
import org.eclipse.update.internal.configurator.*;
import org.eclipse.test.performance.*;
import org.eclipse.update.tests.*;



public class TestStartup extends UpdateManagerTestCase {
	public TestStartup(String arg0) {
		super(arg0);
	}
	

	public void testConfigurationCreation() {
		Performance perf= Performance.getDefault();
		PerformanceMeter performanceMeter= perf.createPerformanceMeter("Parse update configuration");
		try {
			for (int i= 0; i < 10; i++) {
				performanceMeter.start();
				try {
					URL platformXml = new URL("file", "",dataPath + "/" + "perf/platform.xml");
					IPlatformConfiguration config = new PlatformConfigurationFactory().getPlatformConfiguration(platformXml);
				} catch (IOException e) {
					System.out.println("Cannot create configuration for performance measurement");
				}
				performanceMeter.stop();
	 		}
			performanceMeter.commit();
			perf.assertPerformance(performanceMeter);
	 	} finally {
			performanceMeter.dispose();
	 	}
	}
	
	public void testConfigurationDetection() {
		Performance perf= Performance.getDefault();
		PerformanceMeter performanceMeter= perf.createPerformanceMeter("Parse features directory");
		try {
			for (int i= 0; i < 10; i++) {
				performanceMeter.start();
				try {
					URL siteURL = new URL("file", "",dataPath + "/" + "perf/eclipse");
					SiteEntry site = new SiteEntry(siteURL);
					site.loadFromDisk(0);
				} catch (Exception e) {
					System.out.println("Cannot create site entry for performance measurement");
				}
				performanceMeter.stop();
	 		}
			performanceMeter.commit();
			perf.assertPerformance(performanceMeter);
	 	} finally {
			performanceMeter.dispose();
	 	}
	}
}
