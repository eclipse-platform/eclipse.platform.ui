/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		AnnotationManagerTest.class,
		FileSearchTests.class,
		LineAnnotationManagerTest.class,
		PositionTrackerTest.class,
		ResultUpdaterTest.class,
		SearchResultPageTest.class,
		SortingTest.class
})
public class AllFileSearchTests {
	@ClassRule
	public static JUnitSourceSetup fgJUnitSource= new JUnitSourceSetup();
}
