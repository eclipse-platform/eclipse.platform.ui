/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeremie Bresson <jbr@bsiag.com> - Allow to specify format for date variable - https://bugs.eclipse.org/75981
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 486889, 486903
 *******************************************************************************/

package org.eclipse.text.tests.templates;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test Suite for the org.eclipse.text plug-in
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
		TemplateTranslatorTest.class,
		TemplateVariablesWordSelectionTest.class,
		GlobalTemplateVariablesDateTest.class
})
public class TemplatesTestSuite {
	// see @SuiteClasses
}
