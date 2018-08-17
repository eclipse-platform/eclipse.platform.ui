/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.browser.internal;

import static org.junit.Assert.assertNotNull;

import org.eclipse.ui.internal.browser.WebBrowserUIPlugin;
import org.junit.Test;

public class ExistenceTestCase {
	@Test
	public void testPluginExists() {
		assertNotNull(WebBrowserUIPlugin.getInstance());
	}
}