/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.contexts;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * The suite of tests related to the "org.eclipse.ui.contexts" extension point,
 * and the "org.eclipse.ui.contexts" Java API. This includes tests dealing with
 * other extension points or elements in other extension points that have been
 * deprecated to be replaced by this one.
 *
 * @since 3.0
 */
@Suite
@SelectClasses({
	Bug74990Test.class,
	Bug84763Test.class,
	ExtensionTestCase.class,
	PartContextTest.class,
})
public final class ContextsTestSuite {
}
