/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.api;

import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Test cases for the <code>IPageLayout</code> API.
 *
 * @since 3.2
 */
public class IPageLayoutTest extends UITestCase {

	public IPageLayoutTest(String testName) {
		super(testName);
	}

	public void testGetDescriptor() {
		EmptyPerspective.setLastPerspective(null);
		openTestWindow(EmptyPerspective.PERSP_ID);
		assertEquals(EmptyPerspective.PERSP_ID, EmptyPerspective.getLastPerspective());
	}
}
