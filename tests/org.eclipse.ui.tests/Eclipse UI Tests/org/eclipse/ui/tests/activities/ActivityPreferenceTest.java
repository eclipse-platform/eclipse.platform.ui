/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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

package org.eclipse.ui.tests.activities;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.junit.Test;

public class ActivityPreferenceTest {
	/**
	 * Preference prefix - must match the one specified in ActivityPreferenceHelper
	 */
	private static String PREFIX = "UIActivities."; //$NON-NLS-1$
	/**
	 * The activity id
	 */
	private static String ID = "org.eclipse.ui.PT.A2"; //$NON-NLS-1$

	/**
	 * Tests whether activity preferences are persisted as soon as the activity set changes.
	 */
	@Test
	public void testActivityPreference() {
		IActivityManager manager = PlatformUI.getWorkbench().getActivitySupport().getActivityManager();

		boolean initialState = manager.getEnabledActivityIds().contains(ID);
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		assertEquals(initialState, store.getBoolean(PREFIX + ID));

		PlatformUI.getWorkbench().getActivitySupport()
				.setEnabledActivityIds(initialState ? Collections.emptySet() : Collections.singleton(ID));
		assertEquals(!initialState, store.getBoolean(PREFIX + ID));
	}

}
