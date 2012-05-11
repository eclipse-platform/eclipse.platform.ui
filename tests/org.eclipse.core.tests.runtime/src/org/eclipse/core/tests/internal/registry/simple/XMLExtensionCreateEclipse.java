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

import java.io.IOException;
import java.net.URL;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;
import org.osgi.framework.Bundle;

/**
 * Check dynamic contribution into the Eclipse registry itself.
 * @since 3.2
 */
public class XMLExtensionCreateEclipse extends BaseExtensionRegistryRun {

	public void testDynamicContribution() {
		// specify this bundle as a contributor
		Bundle thisBundle = RuntimeTestsPlugin.getContext().getBundle();
		IContributor thisContributor = ContributorFactoryOSGi.createContributor(thisBundle);
		fillRegistry(thisContributor);
		checkRegistry(thisContributor.getName());
	}

	private void fillRegistry(IContributor contributor) {
		try {
			Object userKey = ((ExtensionRegistry) RegistryFactory.getRegistry()).getTemporaryUserToken();
			URL xmlURL = getXML("DynamicExtension.xml"); //$NON-NLS-1$
			RegistryFactory.getRegistry().addContribution(xmlURL.openStream(), contributor, false, xmlURL.getFile(), null, userKey);
		} catch (IOException eFile) {
			fail(eFile.getMessage());
			return;
		}
	}

	private void checkRegistry(String namespace) {
		IExtensionRegistry eclipseRegistry = RegistryFactory.getRegistry();
		String uniqueId = qualifiedName(namespace, "XMLDirectExtPoint"); //$NON-NLS-1$
		IExtensionPoint dynamicExtensionPoint = eclipseRegistry.getExtensionPoint(uniqueId);
		assertNotNull(dynamicExtensionPoint);
		IConfigurationElement[] elements = eclipseRegistry.getConfigurationElementsFor(uniqueId);
		assertTrue(elements.length == 1);
		for (int i = 0; i < elements.length; i++) {
			assertTrue("org.eclipse.core.tests.internal.registry.simple.utils.ExecutableRegistryObject".equals(elements[i].getAttribute("class")));
		}
	}

	public static Test suite() {
		return new TestSuite(XMLExtensionCreateEclipse.class);
	}

}
