/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 * Tests removal APIs using a simple registry.
 * @since 3.2
 */
public class DirectExtensionRemove extends BaseExtensionRegistryRun {
	
	private final static String pointA = "PointA"; //$NON-NLS-1$
	private final static String pointB = "PointB"; //$NON-NLS-1$
	
	private final static String extensionA1 = "TestExtensionA1"; //$NON-NLS-1$
	private final static String extensionA2 = "TestExtensionA2"; //$NON-NLS-1$

	public DirectExtensionRemove() {
		super();
	}

	public DirectExtensionRemove(String name) {
		super(name);
	}

	// Fill the registry; remove half; check listener; check what's left
	public void testExtensionPointAddition() {
		IContributor nonBundleContributor = ContributorFactorySimple.createContributor("DirectRemoveProvider"); //$NON-NLS-1$
		String namespace = nonBundleContributor.getName();
		fillRegistry(nonBundleContributor);
		checkRegistryFull(namespace);

		SimpleRegistryListener listener = new SimpleRegistryListener();
		listener.register(simpleRegistry);
		remove(namespace);
		
		checkListener(listener);
		checkRegistryRemoved(namespace);
		listener.unregister(simpleRegistry);
	}

	private void fillRegistry(IContributor contributor) {
		processXMLContribution(contributor, getXML("RemovalTest.xml")); //$NON-NLS-1$
	}

	private void checkRegistryFull(String namespace) {
		IExtensionPoint extensionPointA = simpleRegistry.getExtensionPoint(qualifiedName(namespace, pointA));
		assertNotNull(extensionPointA);
		IExtensionPoint extensionPointB = simpleRegistry.getExtensionPoint(qualifiedName(namespace, pointB));
		assertNotNull(extensionPointB);
		IExtension[] extensionsA = extensionPointA.getExtensions();
		assertTrue(extensionsA.length == 2);
		IExtension[] extensionsB = extensionPointB.getExtensions();
		assertTrue(extensionsB.length == 2);
	}

	private void remove(String namespace) {
		IExtensionPoint extensionPointB = simpleRegistry.getExtensionPoint(qualifiedName(namespace, pointB));
		assertTrue(simpleRegistry.removeExtensionPoint(extensionPointB, userToken));

		IExtension extension = simpleRegistry.getExtension(qualifiedName(namespace, extensionA1));
		assertTrue(simpleRegistry.removeExtension(extension, userToken));
	}

	private void checkRegistryRemoved(String namespace) {
		IExtensionPoint extensionPointA = simpleRegistry.getExtensionPoint(qualifiedName(namespace, pointA));
		assertNotNull(extensionPointA);
		IExtensionPoint extensionPointB = simpleRegistry.getExtensionPoint(qualifiedName(namespace, pointB));
		assertNull(extensionPointB);
		IExtension[] extensionsA = extensionPointA.getExtensions();
		assertTrue(extensionsA.length == 1);
		String Id = extensionsA[0].getUniqueIdentifier();
		assertTrue(qualifiedName(namespace, extensionA2).equals(Id));
	}

	private void checkListener(SimpleRegistryListener listener) {
		IRegistryChangeEvent event = listener.getEvent(5000);
		IExtensionDelta[] deltas = event.getExtensionDeltas();
		assertTrue(deltas.length == 2);
		for (int i = 0; i < deltas.length; i++) {
			assertTrue(deltas[i].getKind() == IExtensionDelta.REMOVED);
			assertNotNull(deltas[i].getExtension());
			assertNotNull(deltas[i].getExtensionPoint());
		}
	}

	public static Test suite() {
		return new TestSuite(DirectExtensionRemove.class);
	}

}
