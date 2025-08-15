/*******************************************************************************
 * Copyright (c) 2014, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.ui.internal.workbench.ExtensionsSort;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Test for the {@link ExtensionsSort} class.
 */
public class ExtensionsSortTests {

	Bundle root;
	Bundle intermediate;
	Bundle leaf;

	@Before
	public void setUp() throws Exception {
		BundleContext context = FrameworkUtil.getBundle(getClass())
				.getBundleContext();

		// root has no dependencies on other test bundles
		// intermediate depends on root, plus it re-exports
		// leaf depends on intermediate, and should also then
		// depend on root via the re-export
		root = context
				.installBundle(toFileURL("platform:/plugin/"
						+ context.getBundle().getSymbolicName()
						+ "/data/org.eclipse.extensionsSortTests/tests.extensions.root/"));
		intermediate = context
				.installBundle(toFileURL("platform:/plugin/"
						+ context.getBundle().getSymbolicName()
						+ "/data/org.eclipse.extensionsSortTests/tests.extensions.intermediate/"));
		leaf = context
				.installBundle(toFileURL("platform:/plugin/"
						+ context.getBundle().getSymbolicName()
						+ "/data/org.eclipse.extensionsSortTests/tests.extensions.leaf/"));
		root.start(Bundle.START_TRANSIENT);
		intermediate.start(Bundle.START_TRANSIENT);
		leaf.start(Bundle.START_TRANSIENT);
	}

	@Test
	public void testSortOrder() {
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry
				.getExtensionPoint("org.eclipse.e4.workbench.model");
		IExtension[] extensions = new ExtensionsSort().sort(extPoint
				.getExtensions());
		int rootIndex = indexOf(extensions, "test.extensions.root.model");
		int intermediateIndex = indexOf(extensions,
				"test.extensions.intermediate.model");
		int leafIndex = indexOf(extensions, "test.extensions.leaf.model");
		assertTrue(rootIndex < intermediateIndex);
		assertTrue(intermediateIndex < leafIndex);
	}

	/**
	 * Bundle#installBundle() doesn't like platform:/plugin/-style URLs
	 */
	private String toFileURL(String url) throws MalformedURLException,
			IOException, URISyntaxException {
		return FileLocator.toFileURL(new URI(url).toURL()).toString();
	}

	private int indexOf(IExtension[] extensions, String id) {

		for (int i = 0; i < extensions.length; i++) {
			if (id.equals(extensions[i].getUniqueIdentifier())) {
				return i;
			}
		}
		fail("Could not find extensions with id " + id);
		/* NOTREACHED */
		return Integer.MIN_VALUE; // keep JDT happy
	}

	@After
	public void tearDown() throws Exception {
		if (root != null) {
			root.uninstall();
		}
		if (intermediate != null) {
			intermediate.uninstall();
		}
		if (leaf != null) {
			leaf.uninstall();
		}
	}

}
