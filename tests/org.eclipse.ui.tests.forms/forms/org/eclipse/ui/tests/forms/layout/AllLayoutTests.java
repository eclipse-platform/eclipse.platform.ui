/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ralf M Petter<ralf.petter@gmail.com> - Bug 510241
 *******************************************************************************/

package org.eclipse.ui.tests.forms.layout;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test all form layouts
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestColumnWrapLayout.class,
	TestTableWrapLayout.class
})
public class AllLayoutTests {

}
