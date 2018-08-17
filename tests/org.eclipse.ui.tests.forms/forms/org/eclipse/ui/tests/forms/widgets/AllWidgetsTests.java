/*******************************************************************************
 * Copyright (c) 2016, 2017 Ralf M Petter<ralf.petter@gmail.com> and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ralf M Petter<ralf.petter@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms.widgets;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests forms widgets (automated).
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ ExpandableCompositeTest.class, FormTextModelTest.class, ScrolledFormTest.class })
public class AllWidgetsTests {

}
