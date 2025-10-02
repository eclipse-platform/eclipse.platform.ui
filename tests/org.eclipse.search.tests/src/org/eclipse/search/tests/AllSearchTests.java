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
package org.eclipse.search.tests;

import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SelectClasses;

import org.eclipse.search.core.tests.AllSearchModelTests;
import org.eclipse.search.tests.filesearch.AllFileSearchTests;

@Suite
@SelectClasses({
		AllFileSearchTests.class,
		AllSearchModelTests.class,
		TextSearchRegistryTest.class
})
public class AllSearchTests {
	// see @SelectClasses
}
