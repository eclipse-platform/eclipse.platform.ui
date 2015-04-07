/*******************************************************************************
 * Copyright (c) 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.browser.internal;

import static org.eclipse.ui.internal.browser.IBrowserDescriptor.URL_PARAMETER;
import static org.eclipse.ui.internal.browser.WebBrowserUtil.createParameterArray;
import static org.junit.Assert.assertArrayEquals;
import junit.framework.TestCase;

@SuppressWarnings("restriction")
public class WebBrowserUtilTestCase extends TestCase {

	public void testCreateParameterArray() {
		assertArrayEquals(new String[0], createParameterArray(null, null));
		assertArrayEquals(new String[] { "parameters" }, createParameterArray("parameters", null));
		assertArrayEquals(new String[] { "url" }, createParameterArray(null, "url"));
		assertArrayEquals(new String[] { "parameters", "url" }, createParameterArray("parameters ", "url"));
		assertArrayEquals(new String[] { "parameters", "url" }, createParameterArray("parameters", "url"));
		assertArrayEquals(new String[] { "param1", "param2" },
				createParameterArray("param1 " + URL_PARAMETER + " param2", null));
		assertArrayEquals(new String[] { "param1", "url", "param2" },
				createParameterArray("param1 " + URL_PARAMETER + " param2", "url"));
		assertArrayEquals(new String[] { "param1", "url", "param2", "url" },
				createParameterArray("param1 " + URL_PARAMETER + " param2 " + URL_PARAMETER, "url"));
	}

	public void testCreateParameterArrayForMozilla() {
		assertArrayEquals(new String[] { "-remote", "openURL(url)" },
				createParameterArray(" -remote openURL(" + URL_PARAMETER + ")", "url"));
		assertArrayEquals(new String[] { "parameters", "-remote", "openURL(url)" },
				createParameterArray("parameters -remote openURL(" + URL_PARAMETER + ")", "url"));
	}
}
