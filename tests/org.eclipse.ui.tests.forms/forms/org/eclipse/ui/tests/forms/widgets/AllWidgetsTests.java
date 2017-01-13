/*******************************************************************************
 * Copyright (c) 2016, 2017 Ralf M Petter<ralf.petter@gmail.com> and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
@Suite.SuiteClasses({
	ExpandableCompositeTest.class,
	FormTextModelTest.class
})
public class AllWidgetsTests {

}
