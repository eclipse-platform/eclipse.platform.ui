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
package org.eclipse.ui.tests.forms.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests forms utility (automated).
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	FormColorsTest.class,
	FormFontsTest.class,
	FormImagesTest.class,
	FormToolkitTest.class,
	ImageHyperlinkTest.class
})
public class AllUtilityTests {

}
