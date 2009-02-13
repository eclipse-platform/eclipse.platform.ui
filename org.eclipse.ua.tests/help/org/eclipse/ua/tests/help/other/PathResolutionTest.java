/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.other;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.util.ProductPreferences;
import org.osgi.framework.Bundle;

public class PathResolutionTest extends TestCase {

	public static Test suite() {
		return new TestSuite(PathResolutionTest.class);
	}

	public void testResolveNull() {
		assertEquals(null, ProductPreferences.resolveSpecialIdentifiers(null));
	}

	public void testResolveSimplePath() {
		assertEquals("/a.b.c/toc.xml", ProductPreferences.resolveSpecialIdentifiers("/a.b.c/toc.xml"));
	}
	
	public void testResolvePluginsRoot() {
		assertEquals("/a.b.c/toc.xml", ProductPreferences.resolveSpecialIdentifiers("PLUGINS_ROOT/a.b.c/toc.xml"));
	}

	public void testResolveSlashPluginsRoot() {
		assertEquals("/a.b.c/toc.xml", ProductPreferences.resolveSpecialIdentifiers("/PLUGINS_ROOT/a.b.c/toc.xml"));
	}
	
	public void testResolveEmbeddedPluginsRoot() {
		assertEquals("/a.b.c/toc.xml", ProductPreferences.resolveSpecialIdentifiers("../PLUGINS_ROOT/a.b.c/toc.xml"));
	}

	public void testResolvePluginsRootProductPlugin() {
		IProduct product = Platform.getProduct();
		if (product != null) {
			Bundle productBundle = product.getDefiningBundle();
			if (productBundle != null) {
				String bundleName = productBundle.getSymbolicName();
				assertEquals('/' + bundleName + "/toc.xml", ProductPreferences.resolveSpecialIdentifiers("PLUGINS_ROOT/PRODUCT_PLUGIN/toc.xml"));				
			}
		}
	}
	
	public void testResolveProductPlugin() {
		IProduct product = Platform.getProduct();
		if (product != null) {
			Bundle productBundle = product.getDefiningBundle();
			if (productBundle != null) {
				String bundleName = productBundle.getSymbolicName();
				assertEquals('/' + bundleName + "/toc.xml", ProductPreferences.resolveSpecialIdentifiers("PRODUCT_PLUGIN/toc.xml"));				
			}
		}
	}
	
	public void testResolveSlashProductPlugin() {
		IProduct product = Platform.getProduct();
		if (product != null) {
			Bundle productBundle = product.getDefiningBundle();
			if (productBundle != null) {
				String bundleName = productBundle.getSymbolicName();
				assertEquals('/' + bundleName + "/toc.xml", ProductPreferences.resolveSpecialIdentifiers("/PRODUCT_PLUGIN/toc.xml"));				
			}
		}
	}
		
	
}
