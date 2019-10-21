/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package org.eclipse.ui.tests.keys;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for all areas of the key support for the platform.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	BindingInteractionsTest.class,
	BindingManagerTest.class,
	DispatcherTest.class,
	BindingPersistenceTest.class,
	Bug36420Test.class,
	Bug36537Test.class,
	Bug40023Test.class,
	Bug42024Test.class,
	Bug42035Test.class,
	Bug42627Test.class,
	Bug43168Test.class,
	Bug43321Test.class,
	Bug43538Test.class,
	Bug43597Test.class,
	Bug43610Test.class,
	Bug43800Test.class,
	KeysCsvTest.class,
	Bug44460Test.class,
	Bug53489Test.class,
	Bug189167Test.class,
	KeysPreferenceModelTest.class
 })
public class KeysTestSuite {

}
