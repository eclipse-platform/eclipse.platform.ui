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

package org.eclipse.text.tests.link;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


/**
 * Test Suite org.eclipse.text.tests.link.
 *
 * @since 3.0
 */
@RunWith(Suite.class)
@SuiteClasses({
		LinkedPositionGroupTest.class,
		LinkedPositionTest.class,
		InclusivePositionUpdaterTest.class,
		LinkedModeModelTest.class
})
public class LinkTestSuite {
	// see @SuiteClasses
}
