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
 *     Alena Laskavaia - added ExpandableCompositeTest (Bug 481604)
 *     Ralf M Petter<ralf.petter@gmail.com> - Bug 259846, Bug 510241
 *******************************************************************************/

package org.eclipse.ui.tests.forms;

import org.eclipse.ui.tests.forms.layout.AllLayoutTests;
import org.eclipse.ui.tests.forms.util.AllUtilityTests;
import org.eclipse.ui.tests.forms.widgets.AllWidgetsTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests all forms functionality (automated).
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	AllLayoutTests.class,
	AllUtilityTests.class,
	AllWidgetsTests.class
})
public class AllFormsTests {

}
