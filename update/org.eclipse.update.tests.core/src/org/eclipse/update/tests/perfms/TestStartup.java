/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.perfms;
//import java.net.*;

import java.io.IOException;
import java.net.URL;

import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.update.internal.configurator.PlatformConfigurationFactory;
import org.eclipse.update.internal.configurator.SiteEntry;
import org.eclipse.update.tests.UpdateManagerTestCase;

public class TestStartup extends UpdateManagerTestCase {
	public TestStartup(String arg0) {
		super(arg0);
	}
	

	public void testConfigurationCreation() {
		Performance perf= Performance.getDefault();
		PerformanceMeter performanceMeter= perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
		perf.tagAsGlobalSummary(performanceMeter, "Configuration creation", Dimension.ELAPSED_PROCESS);
		
		try {
			for (int i= 0; i < 10; i++) {
				performanceMeter.start();
				try {
					URL platformXml = new URL("file", "",dataPath + "/" + "perf/platform.xml");
					new PlatformConfigurationFactory().getPlatformConfiguration(platformXml);
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
		PerformanceMeter performanceMeter= perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
		perf.tagAsGlobalSummary(performanceMeter, "Configuration detection", Dimension.ELAPSED_PROCESS);

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
