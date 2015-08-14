/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
