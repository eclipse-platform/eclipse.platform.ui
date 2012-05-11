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

	/**
	 * Tests that configuration elements associated with the removed extension
	 * are removed. 
	 */
	public void testAssociatedConfigElements() {
		IContributor nonBundleContributor = ContributorFactorySimple.createContributor("CETest"); //$NON-NLS-1$
		String namespace = nonBundleContributor.getName();
		processXMLContribution(nonBundleContributor, getXML("CERemovalTest.xml")); //$NON-NLS-1$

		IExtensionPoint extensionPointA = simpleRegistry.getExtensionPoint(qualifiedName(namespace, "PointA")); //$NON-NLS-1$
		assertNotNull(extensionPointA);
		IExtension[] extensionsA = extensionPointA.getExtensions();
		assertTrue(extensionsA.length == 2);

		// check first extension
		IExtension ext1 = extensionPointA.getExtension(qualifiedName(namespace, "TestExtensionA1")); //$NON-NLS-1$
		assertNotNull(ext1);
		IConfigurationElement[] ces11 = ext1.getConfigurationElements(); // this will be used later
		assertNotNull(ces11);
		assertEquals(1, ces11.length);
		String[] attrs1 = ces11[0].getAttributeNames();
		assertNotNull(attrs1);
		assertEquals(1, attrs1.length);
		assertEquals("class", attrs1[0]); //$NON-NLS-1$
		IConfigurationElement[] ces12 = ces11[0].getChildren(); // this will be used later
		assertNotNull(ces12);
		assertEquals(1, ces12.length);
		String[] attrs2 = ces12[0].getAttributeNames();
		assertNotNull(attrs2);
		assertEquals(1, attrs2.length);
		assertEquals("value", attrs2[0]); //$NON-NLS-1$

		// check second extension
		IExtension ext2 = extensionPointA.getExtension(qualifiedName(namespace, "TestExtensionA2")); //$NON-NLS-1$
		assertNotNull(ext2);
		IConfigurationElement[] ces21 = ext2.getConfigurationElements(); // this will be used later
		IConfigurationElement[] ces22 = ces21[0].getChildren(); // this will be used later
		String[] attrs22 = ces22[0].getAttributeNames();
		assertNotNull(attrs22);
		assertEquals(1, attrs22.length);
		assertEquals("value", attrs22[0]); //$NON-NLS-1$

		// remove extension1
		// listener to verify that valid CEs are included in the notification
		IRegistryChangeListener listener = new IRegistryChangeListener() {
			public void registryChanged(IRegistryChangeEvent event) {
				IExtensionDelta[] deltas = event.getExtensionDeltas();
				assertTrue(deltas.length == 1);
				for (int i = 0; i < deltas.length; i++) {
					assertTrue(deltas[i].getKind() == IExtensionDelta.REMOVED);
					IExtension extension = deltas[i].getExtension();
					assertNotNull(extension);

					IConfigurationElement[] l_ces11 = extension.getConfigurationElements();
					assertNotNull(l_ces11);
					assertEquals(1, l_ces11.length);
					String[] l_attrs1 = l_ces11[0].getAttributeNames();
					assertNotNull(l_attrs1);
					assertEquals(1, l_attrs1.length);
					assertEquals("class", l_attrs1[0]); //$NON-NLS-1$
					IConfigurationElement[] l_ces12 = l_ces11[0].getChildren();
					assertNotNull(l_ces12);
					assertEquals(1, l_ces12.length);
					String[] l_attrs2 = l_ces12[0].getAttributeNames();
					assertNotNull(l_attrs2);
					assertEquals(1, l_attrs2.length);
					assertEquals("value", l_attrs2[0]); //$NON-NLS-1$
				}
			}
		};

		//SimpleRegistryListener listener = new SimpleRegistryListener() {};
		simpleRegistry.addRegistryChangeListener(listener);
		try {
			simpleRegistry.removeExtension(ext1, userToken);
		} finally {
			simpleRegistry.removeRegistryChangeListener(listener);
		}

		// basic checks
		IExtension[] extensionsRemoved = extensionPointA.getExtensions();
		assertTrue(extensionsRemoved.length == 1);

		// re-check configuration elements
		boolean exceptionFound = false;
		try {
			ces11[0].getAttributeNames(); // should produce an exception
		} catch (InvalidRegistryObjectException e) {
			exceptionFound = true;
		}
		assertTrue(exceptionFound);

		exceptionFound = false;
		try {
			ces12[0].getAttributeNames(); // should produce an exception
		} catch (InvalidRegistryObjectException e) {
			exceptionFound = true;
		}

		assertTrue(exceptionFound);
		// the non-removed extension CEs should still be valid
		String[] attrs22removed = ces22[0].getAttributeNames();
		assertNotNull(attrs22removed);
		assertEquals(1, attrs22removed.length);
		assertEquals("value", attrs22removed[0]); //$NON-NLS-1$
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
