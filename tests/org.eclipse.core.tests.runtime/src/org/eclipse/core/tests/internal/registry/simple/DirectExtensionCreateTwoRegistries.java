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
import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.runtime.*;

/**
 * Test simultaneous work of two extension registries.
 * @since 3.2
 */
public class DirectExtensionCreateTwoRegistries extends BaseExtensionRegistryRun {

	private String extensionPointId = "AAAid"; //$NON-NLS-1$
	private String extensionPointAltId = "BBBid"; //$NON-NLS-1$
	private String extensionPointSchemaRef = "schema/schema.exsd"; //$NON-NLS-1$

	private IExtensionRegistry theDeviceRegistryA;
	private IExtensionRegistry theDeviceRegistryB;

	public DirectExtensionCreateTwoRegistries() {
		super();
	}

	public DirectExtensionCreateTwoRegistries(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		startRegistries();
	}

	protected void tearDown() throws Exception {
		stopRegistries();
	}

	private void startRegistries() {
		theDeviceRegistryA = startRegistry("A"); //$NON-NLS-1$
		theDeviceRegistryB = startRegistry("B"); //$NON-NLS-1$
	}

	private void stopRegistries() {
		assertNotNull(theDeviceRegistryA);
		theDeviceRegistryA.stop(masterToken);

		assertNotNull(theDeviceRegistryB);
		theDeviceRegistryB.stop(masterToken);
	}

	public void testExtensionPointAddition() {
		// Test with non-bundle contributor
		IContributor nonBundleContributor = ContributorFactorySimple.createContributor("ABC"); //$NON-NLS-1$
		String namespace = nonBundleContributor.getName();
		checkEmptyRegistries(namespace); // make sure we don't have any leftovers

		fillRegistries(nonBundleContributor); // add one extension point in each registry
		checkRegistries(namespace); // check that they got into right places

		stopRegistries(); // check caches
		startRegistries();
		checkEmptyRegistries(namespace); // confirm that both registries got re-populated from caches
	}

	private void checkEmptyRegistries(String namespace) {
		// see what's in the registry A:
		IExtensionPoint extensionPoint = theDeviceRegistryA.getExtensionPoint(qualifiedName(namespace, extensionPointId));
		IExtensionPoint extensionPointAlt = theDeviceRegistryA.getExtensionPoint(qualifiedName(namespace, extensionPointAltId));
		assertNull(extensionPoint);
		assertNull(extensionPointAlt);
	}

	private void fillRegistries(IContributor contributor) {
		assertTrue(((ExtensionRegistry) theDeviceRegistryA).addExtensionPoint(extensionPointId, contributor, false, "LabelA", extensionPointSchemaRef, userToken)); //$NON-NLS-1$
		assertTrue(((ExtensionRegistry) theDeviceRegistryB).addExtensionPoint(extensionPointAltId, contributor, false, "LabelB", extensionPointSchemaRef, userToken)); //$NON-NLS-1$
	}

	private void checkRegistries(String namespace) {
		// see what's in the registry A:
		IExtensionPoint extensionPoint = theDeviceRegistryA.getExtensionPoint(qualifiedName(namespace, extensionPointId));
		IExtensionPoint extensionPointAlt = theDeviceRegistryA.getExtensionPoint(qualifiedName(namespace, extensionPointAltId));
		assertNotNull(extensionPoint);
		assertNull(extensionPointAlt);

		// see what's in the registry B:
		extensionPoint = theDeviceRegistryB.getExtensionPoint(qualifiedName(namespace, extensionPointId));
		extensionPointAlt = theDeviceRegistryB.getExtensionPoint(qualifiedName(namespace, extensionPointAltId));
		assertNull(extensionPoint);
		assertNotNull(extensionPointAlt);
	}

	public static Test suite() {
		return new TestSuite(DirectExtensionCreateTwoRegistries.class);
	}

}
