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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.internal.registry.simple.utils.SimpleRegistryListener;

/**
 * Tests addition of extension point and the extension to the registry via
 * XML contribution. Makes sure that items are actually added; checks
 * listener notification; reloads registry from cache and re-checks the data.
 * 
 * @since 3.2
 */
public class XMLExtensionCreate extends BaseExtensionRegistryRun {

	public XMLExtensionCreate() {
		super();
	}

	public XMLExtensionCreate(String name) {
		super(name);
	}

	public void testExtensionPointAddition() {
		SimpleRegistryListener listener = new SimpleRegistryListener();
		listener.register(simpleRegistry);

		// Test with non-bundle contributor
		IContributor nonBundleContributor = ContributorFactorySimple.createContributor("ABC"); //$NON-NLS-1$
		fillRegistry(nonBundleContributor);

		String namespace = nonBundleContributor.getName();
		checkListener(namespace, listener);
		checkRegistry(nonBundleContributor.getName());

		listener.unregister(simpleRegistry);

		// check the cache: stop -> re-start
		stopRegistry();
		startRegistry();
		checkRegistry(nonBundleContributor.getName());
	}

	private void fillRegistry(IContributor contributor) {
		// Add extension point
		processXMLContribution(contributor, getXML("ExtensionPoint.xml")); //$NON-NLS-1$
		// Add extension
		processXMLContribution(contributor, getXML("Extension.xml")); //$NON-NLS-1$
	}

	private void checkRegistry(String namespace) {
		IExtensionPoint extensionPoint = simpleRegistry.getExtensionPoint(qualifiedName(namespace, "XMLDirectExtPoint")); //$NON-NLS-1$
		assertNotNull(extensionPoint);
		IExtension[] namespaceExtensions = simpleRegistry.getExtensions(namespace);
		assertNotNull(namespaceExtensions);
		assertTrue(namespaceExtensions.length == 1);
		IExtension[] extensions = extensionPoint.getExtensions();
		assertNotNull(extensions);
		assertTrue(extensions.length == 1);
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			String extensionId = extension.getUniqueIdentifier();
			assertTrue(extensionId.equals(qualifiedName(namespace, "XMLDirectExtensionID"))); //$NON-NLS-1$
			String extensionNamespace = extension.getNamespaceIdentifier();
			assertTrue(extensionNamespace.equals(namespace));
			String extensionContributor = extension.getContributor().getName();
			assertTrue(extensionContributor.equals(namespace));
			IConfigurationElement[] configElements = extension.getConfigurationElements();
			assertNotNull(configElements);
			for (int j = 0; j < configElements.length; j++) {
				IConfigurationElement configElement = configElements[j];
				String configElementName = configElement.getName();
				assertTrue(configElementName.equals("StorageDevice")); //$NON-NLS-1$
				String[] attributeNames = configElement.getAttributeNames();
				assertTrue(attributeNames.length == 1);
				IConfigurationElement[] configElementChildren = configElement.getChildren();
				assertTrue(configElementChildren.length == 2);
			}
		}
	}

	private void checkListener(String namespace, SimpleRegistryListener listener) {
		IRegistryChangeEvent event = listener.getEvent(5000);
		IExtensionDelta[] deltas = event.getExtensionDeltas();
		assertTrue(deltas.length == 1); // only one notification
		for (int i = 0; i < deltas.length; i++) {
			IExtensionDelta delta = deltas[i];
			assertTrue(delta.getKind() == IExtensionDelta.ADDED);
			IExtensionPoint theExtensionPoint = delta.getExtensionPoint();
			IExtension theExtension = delta.getExtension();
			String Id1 = theExtension.getExtensionPointUniqueIdentifier();
			String Id2 = theExtensionPoint.getUniqueIdentifier();
			assertTrue(Id1.equals(Id2)); // check connectivity
			assertTrue(Id1.equals(qualifiedName(namespace, "XMLDirectExtPoint"))); //$NON-NLS-1$
		}
	}

	public static Test suite() {
		return new TestSuite(XMLExtensionCreate.class);
	}

}
