/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.toc;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.help.internal.util.ProductPreferences;

public class HelpData extends TestCase {

	public void testHelpDataInPlugin() {
		List result = ProductPreferences.getTocOrdering("org.eclipse.ua.tests", "data/help/toc/helpData.xml", "");
	    assertEquals(2, result.size());
	    assertEquals("/org.eclipse.platform.doc.user/toc.xml", result.get(0));
	    assertEquals("/org.eclipse.platform.doc.isv/toc.xml", result.get(1));
	}
	
	public void testPluginsRoot() {
		List result = ProductPreferences.getTocOrdering("org.eclipse.sdk", "PLUGINS_ROOT/org.eclipse.ua.tests/data/help/toc/helpData.xml", "");
	    assertEquals(2, result.size());
	    assertEquals("/org.eclipse.platform.doc.user/toc.xml", result.get(0));
	    assertEquals("/org.eclipse.platform.doc.isv/toc.xml", result.get(1));
	}

	public void testHelpDataOverridesBaseTocs() {
		List result = ProductPreferences.getTocOrdering("org.eclipse.ua.tests", "data/help/toc/helpData.xml", "org.eclipse.help");
	    assertEquals(2, result.size());
	    assertEquals("/org.eclipse.platform.doc.user/toc.xml", result.get(0));
	    assertEquals("/org.eclipse.platform.doc.isv/toc.xml", result.get(1));
	}
	
	public void testBaseTocs() {
		List result = ProductPreferences.getTocOrdering("", "", "/org.eclipse.help/toc.xml,/org.eclipse.test/toc.xml");
	    assertEquals(2, result.size());
	    assertEquals("/org.eclipse.help/toc.xml", result.get(0));
	    assertEquals("/org.eclipse.test/toc.xml", result.get(1));
	}
	
}
