/*******************************************************************************
 *  Copyright (c) 2006, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.registry.simple;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.*;

/**
 * Tests merging static and dynamic contributions. 
 * 
 * @since 3.2
 */
public class MergeContribution extends BaseExtensionRegistryRun {

	public MergeContribution() {
		super();
	}

	public MergeContribution(String name) {
		super(name);
	}

	public void testMergeStaticDynamic() {
		// Test with non-bundle contributor
		IContributor nonBundleContributor = ContributorFactorySimple.createContributor("ABC"); //$NON-NLS-1$
		String namespace = nonBundleContributor.getName();

		fillRegistryStatic(nonBundleContributor);
		checkRegistry(namespace, 3);
		fillRegistryDynamic(nonBundleContributor);
		checkRegistry(namespace, 6);

		stopRegistry();
		simpleRegistry = startRegistry();

		checkRegistry(namespace, 3);
		fillRegistryDynamic(nonBundleContributor);
		checkRegistry(namespace, 6);
	}

	private void fillRegistryStatic(IContributor contributor) {
		processXMLContribution(contributor, getXML("MergeStatic.xml"), true); //$NON-NLS-1$
	}

	private void fillRegistryDynamic(IContributor contributor) {
		processXMLContribution(contributor, getXML("MergeDynamic.xml"), false); //$NON-NLS-1$
	}

	private void checkRegistry(String namespace, int expectedExtensions) {
		IExtensionPoint extensionPoint = simpleRegistry.getExtensionPoint(qualifiedName(namespace, "MergeStatic")); //$NON-NLS-1$
		assertNotNull(extensionPoint);
		IExtension[] extensions = simpleRegistry.getExtensions(namespace);
		assertNotNull(extensions);
		assertEquals(expectedExtensions, extensions.length);
	}

	public static Test suite() {
		return new TestSuite(MergeContribution.class);
	}

}
