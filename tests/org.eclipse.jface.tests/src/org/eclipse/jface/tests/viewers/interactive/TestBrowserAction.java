/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.jface.tests.viewers.interactive;

import org.eclipse.jface.action.Action;

public abstract class TestBrowserAction extends Action {
	private final TestBrowser browser;

	public TestBrowserAction(String label, TestBrowser browser) {
		super(label);
		this.browser = browser;
	}

	/**
	 * Returns the test browser.
	 *
	 * @return The browser
	 */
	public TestBrowser getBrowser() {
		return browser;
	}
}
