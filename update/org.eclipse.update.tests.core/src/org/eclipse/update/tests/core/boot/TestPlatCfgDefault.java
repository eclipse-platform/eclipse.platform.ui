/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.core.boot;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.boot.IPlatformConfiguration.ISiteEntry;
import org.eclipse.core.boot.IPlatformConfiguration.ISitePolicy;

public class TestPlatCfgDefault
	extends PlatformConfigurationTestCase {
		
	public TestPlatCfgDefault(String arg0) {
		super(arg0);
	}
	
	public void testInitial() throws Exception {
		IPlatformConfiguration cfig = null;
		cfig = BootLoader.getCurrentPlatformConfiguration();
		ISiteEntry se = cfig.getConfiguredSites()[0];
		ISitePolicy sp = cfig.createSitePolicy(ISitePolicy.USER_EXCLUDE, new String[] {"1", "2","3","4","5","6","7","8","9","10","11","12"});
		se.setSitePolicy(sp);
		cfig.save();
		System.out.println("done ...");
	}
}

