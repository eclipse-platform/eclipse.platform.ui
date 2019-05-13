/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474132
 *******************************************************************************/
package org.eclipse.ui.tests.activities;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The ActivitiesTestSuite class runs the activities' test suites.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	ImagesTest.class,
	UtilTest.class,
	StaticTest.class,
	DynamicTest.class,
	PersistanceTest.class,
	ActivityPreferenceTest.class,
	MenusTest.class,
	PatternUtilTest.class
})
public class ActivitiesTestSuite {

}
