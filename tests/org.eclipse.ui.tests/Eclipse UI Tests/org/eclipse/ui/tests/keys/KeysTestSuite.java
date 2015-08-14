/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	BindingPersistenceTest.class,
	// TODO This no longer works due to focus issues related to key bindings
	//Bug36420Test.class,
	Bug36537Test.class,
	//		TODO Intermittent failure.  SWT Bug 44344.  XGrabPointer?
	//		Bug40023Test.class,
	Bug42024Test.class,
	Bug42035Test.class,
	//		TODO Logging piece of fix did not go in M4.
	//		Bug42627Test.class,
	Bug43168Test.class,
	Bug43321Test.class,
	Bug43538Test.class,
	Bug43597Test.class,
	Bug43610Test.class,
	Bug43800Test.class,
	KeysCsvTest.class,
	//		TODO disabled since it refers to the Java builder and nature,
	//      which are not available in an RCP build
	//		Bug44460Test.class,
	/* TODO disabled as it fails on the Mac.
	 * Ctrl+S doesn't save the editor, and posting MOD1+S also doesn't seem to work.
	 */
	//Bug53489Test.class,
	Bug189167Test.class,
	KeysPreferenceModelTest.class
 })
public class KeysTestSuite {

}
