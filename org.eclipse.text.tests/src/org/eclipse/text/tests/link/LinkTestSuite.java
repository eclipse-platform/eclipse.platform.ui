/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
