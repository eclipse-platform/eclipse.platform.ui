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
package org.eclipse.update.tests.core.boot;

import org.eclipse.update.configurator.*;


public class TestPlatCfgDefault
	extends PlatformConfigurationTestCase {
		
	public TestPlatCfgDefault(String arg0) {
		super(arg0);
	}
	
	public void testInitial() throws Exception {
		IPlatformConfiguration cfig = null;
		cfig = ConfiguratorUtils.getCurrentPlatformConfiguration();
		IPlatformConfiguration.ISiteEntry se = cfig.getConfiguredSites()[0];
		IPlatformConfiguration.ISitePolicy sp = cfig.createSitePolicy(IPlatformConfiguration.ISitePolicy.USER_EXCLUDE, new String[] {"1", "2","3","4","5","6","7","8","9","10","11","12"});
		se.setSitePolicy(sp);
		cfig.save();
		System.out.println("done ...");
	}
}

