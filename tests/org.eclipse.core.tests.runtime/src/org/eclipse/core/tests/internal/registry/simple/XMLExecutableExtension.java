/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
import org.eclipse.core.tests.internal.registry.simple.utils.*;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;
import org.osgi.framework.Bundle;

/**
 * Tests that executable extensions present in the simple registry actually
 * gets processed.
 * @since 3.2
 */
public class XMLExecutableExtension extends BaseExtensionRegistryRun {

	public XMLExecutableExtension() {
		super();
	}

	public XMLExecutableExtension(String name) {
		super(name);
	}

	/**
	 * Provide own class loader to the registry executable element strategry 
	 * @return - open extension registry
	 */
	protected IExtensionRegistry startRegistry() {
		// use plugin's metadata directory to save cache data
		Bundle theBundle = RuntimeTestsPlugin.getContext().getBundle();
		IPath userDataPath = Platform.getStateLocation(theBundle);
		File[] registryLocations = new File[] {new File(userDataPath.toOSString())};
		boolean[] readOnly = new boolean[] {false};
		RegistryStrategy registryStrategy = new ExeExtensionStrategy(registryLocations, readOnly);
		return RegistryFactory.createRegistry(registryStrategy, masterToken, userToken);
	}

	public void testExecutableExtensionCreation() {
		// Test with non-bundle contributor
		IContributor nonBundleContributor = ContributorFactorySimple.createContributor("ABC"); //$NON-NLS-1$
		assertFalse(ExecutableRegistryObject.createCalled);

		fillRegistry(nonBundleContributor);
		assertFalse(ExecutableRegistryObject.createCalled);
		
		checkRegistry(nonBundleContributor.getName());
		assertTrue(ExecutableRegistryObject.createCalled);
	}

	private void fillRegistry(IContributor contributor) {
		processXMLContribution(contributor, getXML("ExecutableExtension.xml")); //$NON-NLS-1$
	}

	private void checkRegistry(String namespace) {
		IConfigurationElement[] elements = simpleRegistry.getConfigurationElementsFor(qualifiedName(namespace, "XMLExecutableExtPoint")); //$NON-NLS-1$
		assertTrue(elements.length == 1);
		for (int i = 0; i < elements.length; i++) {
			try {
				Object object = elements[i].createExecutableExtension("class"); //$NON-NLS-1$
				assertNotNull(object);
			} catch (CoreException e) {
				assertTrue(false);
				e.printStackTrace();
			}
		}
	}

	public static Test suite() {
		return new TestSuite(XMLExecutableExtension.class);
	}
}
