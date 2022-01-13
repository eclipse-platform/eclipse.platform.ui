/*******************************************************************************
 * Copyright (c) 2022 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial version
 ******************************************************************************/
package org.eclipse.jface.tests.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.jface.widgets.BrowserFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.junit.Test;

public class TestUnitBrowserFactory extends AbstractFactoryTest {

	@Test
	public void createsBrowser() {
		Browser browser = BrowserFactory.newBrowser(SWT.NONE).create(shell);

		assertEquals(shell, browser.getParent());
		assertEquals(SWT.NONE, browser.getStyle() & SWT.NONE);
	}

	@Test
	public void createsBrowserWithJavaScriptDisabled() {
		Browser browser = BrowserFactory.newBrowser(SWT.NONE).disableJS().create(shell);

		assertFalse(browser.getJavascriptEnabled());
	}
}