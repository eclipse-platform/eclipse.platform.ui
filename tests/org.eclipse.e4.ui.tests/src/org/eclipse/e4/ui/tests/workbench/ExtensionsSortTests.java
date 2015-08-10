/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.ui.internal.workbench.ExtensionsSort;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Test for the {@link ExtensionsSort} class.
 */
public class ExtensionsSortTests extends TestCase {

	Bundle root;
	Bundle intermediate;
	Bundle leaf;

	@Override
	protected void setUp() throws Exception {
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

	/** Bundle#installBundle() doesn't like platform:/plugin/-style URLs */
	private String toFileURL(String url) throws MalformedURLException,
			IOException {
		return FileLocator.toFileURL(new URL(url)).toString();
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

	@Override
	protected void tearDown() throws Exception {
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
