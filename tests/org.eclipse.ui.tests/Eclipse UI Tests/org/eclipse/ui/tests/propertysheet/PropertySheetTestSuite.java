/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Paul Pazderski - Bug 546546: migrate to JUnit4 suite
 *******************************************************************************/
package org.eclipse.ui.tests.propertysheet;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test areas of the Property Sheet API.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	PropertyShowInContextTest.class,
	MultiInstancePropertySheetTest.class,
	ShowInPropertySheetTest.class,
	NewPropertySheetHandlerTest.class,
	PropertySheetAuto.class,
	ComboBoxPropertyDescriptorTest.class,
	DirtyStatePropertySheetTest.class,
})
public class PropertySheetTestSuite {
}
