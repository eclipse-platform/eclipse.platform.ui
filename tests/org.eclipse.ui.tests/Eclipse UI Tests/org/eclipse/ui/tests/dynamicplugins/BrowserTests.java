/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.junit.Test;

/**
 * @since 3.1
 */
public class BrowserTests extends DynamicTestCase {

	@Test
	public void testBrowserSupport() {
		WorkbenchBrowserSupport support = (WorkbenchBrowserSupport) WorkbenchBrowserSupport.getInstance();
		try {
			support.setDesiredBrowserSupportId(getExtensionId());
			assertFalse(support.hasNonDefaultBrowser());

			getBundle();
			support.setDesiredBrowserSupportId(getExtensionId());
			assertTrue(support.hasNonDefaultBrowser());

			removeBundle();
			support.setDesiredBrowserSupportId(getExtensionId());
			assertFalse(support.hasNonDefaultBrowser());
		}
		finally {
			support.setDesiredBrowserSupportId(null);
		}
	}

	@Override
	protected String getExtensionId() {
		return "newBrowser1.testDynamicBrowserAddition";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_BROWSER_SUPPORT;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newBrowser1";
	}

	@Override
	protected String getMarkerClass() {
		return "org.eclipse.ui.dynamic.DynamicBrowserSupport";
	}

}
