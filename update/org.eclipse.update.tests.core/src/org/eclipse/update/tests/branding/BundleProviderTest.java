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
package org.eclipse.update.tests.branding;


import org.eclipse.core.runtime.*;
import org.eclipse.update.internal.configurator.branding.*;
import org.eclipse.update.tests.UpdateManagerTestCase;
import org.osgi.framework.*;

public class BundleProviderTest extends UpdateManagerTestCase {

	public BundleProviderTest(String testcase){
		super(testcase);
	}

	public void testMain() throws Exception {
		
		IBundleGroupProvider[] bundleGroupProviders = Platform.getBundleGroupProviders();
		for (int i=0; i<bundleGroupProviders.length; i++) {
			System.out.println("BundleGroupProvider:"+bundleGroupProviders[i].getName());
			IBundleGroup[] bundleGroups = bundleGroupProviders[i].getBundleGroups();
			for (int j=0; j<bundleGroups.length; j++) {
				System.out.println("BundleGroup name:"+bundleGroups[j].getName() +"\n" +
						"description:" + bundleGroups[j].getDescription() + "\n"+
						"id:" + bundleGroups[j].getIdentifier() + "\n" +
						"provider:" + bundleGroups[j].getProviderName() + "\n" +
						"version:" + bundleGroups[j].getVersion() + "\n" +
						"license:" + bundleGroups[j].getProperty(IBundleGroupConstants.LICENSE_HREF) + "\n" +
						"featureImage:" + bundleGroups[j].getProperty(IBundleGroupConstants.FEATURE_IMAGE) + "\n" +
						"tips and tricks:" + bundleGroups[j].getProperty(IBundleGroupConstants.TIPS_AND_TRICKS_HREF) + "\n" +
						"welcomePage:" + bundleGroups[j].getProperty(IBundleGroupConstants.WELCOME_PAGE) + "\n" +
						"welcomePerspective:" + bundleGroups[j].getProperty(IBundleGroupConstants.WELCOME_PERSPECTIVE));
				System.out.println("bundles:");
				Bundle[] bundles = bundleGroups[j].getBundles();
				for (int b=0; b<bundles.length; b++)
					System.out.println(bundles[b].getSymbolicName());
				System.out.println();
			}
		}
	}
}
