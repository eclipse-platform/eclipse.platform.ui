/*******************************************************************************
 *  Copyright (c) 2008, 2016 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.intro.other;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.ui.internal.intro.impl.util.ReopenUtil;
import org.junit.Test;

/*
 * Tests the intro parser on valid intro content.
 */
public class ReopenStateTest {
	@Test
	public void testReopenState() {
		ReopenUtil.setReopenPreference(true);
		assertTrue(ReopenUtil.isReopenPreference());
		ReopenUtil.setReopenPreference(false);
		assertFalse(ReopenUtil.isReopenPreference());
		ReopenUtil.setReopenPreference(false);
		assertFalse(ReopenUtil.isReopenPreference());
		ReopenUtil.setReopenPreference(true);
		assertTrue(ReopenUtil.isReopenPreference());
		ReopenUtil.setReopenPreference(false);
		assertFalse(ReopenUtil.isReopenPreference());
	}


}
