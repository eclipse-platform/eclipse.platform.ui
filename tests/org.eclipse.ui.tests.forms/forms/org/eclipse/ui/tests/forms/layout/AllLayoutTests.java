/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
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
 *     Ralf M Petter<ralf.petter@gmail.com> - Bug 510241
 *******************************************************************************/

package org.eclipse.ui.tests.forms.layout;

import org.eclipse.ui.tests.forms.widgets.HintAdjustmentTest;
import org.eclipse.ui.tests.forms.widgets.SizeCacheTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test all form layouts
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	HintAdjustmentTest.class,
	SizeCacheTest.class,
	TestColumnWrapLayout.class,
	TestTableWrapLayout.class,
})
public class AllLayoutTests {

}
