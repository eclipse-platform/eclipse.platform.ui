/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
package org.eclipse.ui.tests.adaptable;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The AdaptableTestSuite is the TestSuite for the
 * adaptable support in the UI.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	AdaptableDecoratorTestCase.class,
	MarkerImageProviderTest.class,
	WorkingSetTestCase.class,
	SelectionAdapterTest.class,
})
public class AdaptableTestSuite {
}
