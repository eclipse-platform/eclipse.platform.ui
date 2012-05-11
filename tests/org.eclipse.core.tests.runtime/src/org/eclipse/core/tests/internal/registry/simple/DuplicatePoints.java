/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.registry.simple;

import java.io.File;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.spi.RegistryStrategy;
import org.eclipse.core.tests.internal.registry.simple.utils.HiddenLogRegistryStrategy;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;
import org.osgi.framework.Bundle;

/**
 * Tests addition of extensions and extension points with duplicate IDs.
 * The duplicate extension points should be ignored.
 * The duplicate extensions should be added. 
 * The rest of the XML contribution should not be affected.
 * 
 * @since 3.2
 */
public class DuplicatePoints extends BaseExtensionRegistryRun {

	private final static String errMsg1 = "Error:  Ignored duplicate extension point \"testDuplicates.duplicateExtensionPoint\" supplied by \"2\"." + "Warning:  Extensions supplied by \"2\" and \"1\" have the same Id: \"testDuplicates.duplicateExtension\".";
	private final static String errMsg2 = "Error:  Ignored duplicate extension point \"testSame.duplicateExtensionPointSame\" supplied by \"3\"." + "Warning:  Extensions supplied by \"3\" and \"3\" have the same Id: \"testSame.duplicateExtensionSame\".";

	public DuplicatePoints() {
		super();
	}

	public DuplicatePoints(String name) {
		super(name);
	}

	/**
	 * Use registry strategy with modified logging
	 * @return - open extension registry
	 */
	protected IExtensionRegistry startRegistry() {
		// use plugin's metadata directory to save cache data
		Bundle theBundle = RuntimeTestsPlugin.getContext().getBundle();
		IPath userDataPath = Platform.getStateLocation(theBundle);
		File[] registryLocations = new File[] {new File(userDataPath.toOSString())};
		boolean[] readOnly = new boolean[] {false};
		RegistryStrategy registryStrategy = new HiddenLogRegistryStrategy(registryLocations, readOnly);
		return RegistryFactory.createRegistry(registryStrategy, masterToken, userToken);
	}

	public void testDuplicates() {
		HiddenLogRegistryStrategy.output = ""; //$NON-NLS-1$
		IContributor contributor1 = ContributorFactorySimple.createContributor("1"); //$NON-NLS-1$
		processXMLContribution(contributor1, getXML("DuplicatePoints1.xml")); //$NON-NLS-1$

		IContributor contributor2 = ContributorFactorySimple.createContributor("2"); //$NON-NLS-1$
		processXMLContribution(contributor2, getXML("DuplicatePoints2.xml")); //$NON-NLS-1$

		checkRegistryDifferent("testDuplicates"); //$NON-NLS-1$

		HiddenLogRegistryStrategy.output = ""; //$NON-NLS-1$
		IContributor contributor3 = ContributorFactorySimple.createContributor("3"); //$NON-NLS-1$
		processXMLContribution(contributor3, getXML("DuplicatePointsSame.xml")); //$NON-NLS-1$

		checkRegistrySame("testSame"); //$NON-NLS-1$
	}

	private void checkRegistryDifferent(String namespace) {
		assertTrue(errMsg1.equals(HiddenLogRegistryStrategy.output));

		IExtensionPoint[] extensionPoints = simpleRegistry.getExtensionPoints(namespace);
		assertTrue(extensionPoints.length == 2);

		IExtension[] extensions = simpleRegistry.getExtensions(namespace);
		assertTrue(extensions.length == 3);

		IExtension extension = simpleRegistry.getExtension(qualifiedName(namespace, "nonDuplicateExtension")); //$NON-NLS-1$
		assertNotNull(extension);
	}

	private void checkRegistrySame(String namespace) {
		assertTrue(errMsg2.equals(HiddenLogRegistryStrategy.output));

		IExtensionPoint[] extensionPoints = simpleRegistry.getExtensionPoints(namespace);
		assertTrue(extensionPoints.length == 1);

		IExtension[] extensions = simpleRegistry.getExtensions(namespace);
		assertTrue(extensions.length == 2);
	}

	public static Test suite() {
		return new TestSuite(DuplicatePoints.class);
	}

}
