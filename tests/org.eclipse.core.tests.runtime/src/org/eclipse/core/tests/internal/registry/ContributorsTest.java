/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.registry;

import java.io.IOException;
import junit.framework.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.BundleTestingHelper;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * Tests contributor resolution for Bundle-based contributors.
 * 
 * @since 3.3
 */
public class ContributorsTest extends TestCase {

	public ContributorsTest() {
		super();
	}

	public ContributorsTest(String name) {
		super(name);
	}

	public void testResolution() throws IOException, BundleException {
		Bundle bundle = null;
		Bundle fragment = null;
		try {
			bundle = BundleTestingHelper.installBundle("0.1", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/contributors/A");
			fragment = BundleTestingHelper.installBundle("0.2", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/contributors/B");
			BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundle, fragment});

			IExtensionRegistry registry = RegistryFactory.getRegistry();
			IExtensionPoint bundleExtPoint = registry.getExtensionPoint("testContributors.xptContibutorsA");
			IContributor bundleContributor = bundleExtPoint.getContributor();
			Bundle contributingBundle = ContributorFactoryOSGi.resolve(bundleContributor);
			assertNotNull(contributingBundle);
			assertTrue(contributingBundle.equals(bundle));

			IExtensionPoint fragmentExtPoint = registry.getExtensionPoint("testContributors.contrFragment");
			IContributor fragmentContributor = fragmentExtPoint.getContributor();
			Bundle contributingFragment = ContributorFactoryOSGi.resolve(fragmentContributor);
			assertNotNull(contributingFragment);
			assertTrue(contributingFragment.equals(fragment));
		} finally {
			if (bundle != null)
				bundle.uninstall();
			if (fragment != null)
				fragment.uninstall();
		}
	}

	/**
	 * bundleA, bundleB, and fragment on bundleA all use the same namespace. Verify that getting
	 * elements by contributor returns all elements from the contributor and only from that
	 * contributor.
	 * 
	 * @throws IOException
	 * @throws BundleException
	 */
	public void testByContributor() throws IOException, BundleException {
		Bundle bundleA = null;
		Bundle bundleB = null;
		Bundle fragment = null;
		try {
			bundleA = BundleTestingHelper.installBundle("0.1", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/elementsByContributor/A");
			bundleB = BundleTestingHelper.installBundle("0.2", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/elementsByContributor/B");
			fragment = BundleTestingHelper.installBundle("0.2", RuntimeTestsPlugin.getContext(), RuntimeTestsPlugin.TEST_FILES_ROOT + "registry/elementsByContributor/Afragment");
			BundleTestingHelper.refreshPackages(RuntimeTestsPlugin.getContext(), new Bundle[] {bundleA, bundleB, fragment});

			IExtensionRegistry registry = RegistryFactory.getRegistry();

			// verify bundleA (bundle B is the same - will work if this works)
			IContributor contributorA = ContributorFactoryOSGi.createContributor(bundleA);

			IExtensionPoint[] extPointsA = registry.getExtensionPoints(contributorA);
			assertNotNull(extPointsA);
			assertTrue(extPointsA.length == 1);
			assertTrue(extPointsA[0].getUniqueIdentifier().equals("org.eclipse.test.registryByContrib.PointA"));

			IExtension[] extsA = registry.getExtensions(contributorA);
			assertNotNull(extsA);
			assertTrue(extsA.length == 1);
			assertTrue(extsA[0].getUniqueIdentifier().equals("org.eclipse.test.registryByContrib.ExtensionA"));

			// verify fragment
			IContributor contributorAF = ContributorFactoryOSGi.createContributor(fragment);
			IExtensionPoint[] extPointsFragmentA = registry.getExtensionPoints(contributorAF);
			assertNotNull(extPointsFragmentA);
			assertTrue(extPointsFragmentA.length == 1);
			assertTrue(extPointsFragmentA[0].getUniqueIdentifier().equals("org.eclipse.test.registryByContrib.PointFA"));

			IExtension[] extsFragmentA = registry.getExtensions(contributorAF);
			assertNotNull(extsFragmentA);
			assertTrue(extsFragmentA.length == 1);
			assertTrue(extsFragmentA[0].getUniqueIdentifier().equals("org.eclipse.test.registryByContrib.ExtensionFA"));

		} finally {
			if (bundleA != null)
				bundleA.uninstall();
			if (bundleB != null)
				bundleB.uninstall();
			if (fragment != null)
				fragment.uninstall();
		}
	}

	public static Test suite() {
		return new TestSuite(ContributorsTest.class);
	}

}
