/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.activities;

import java.util.Collections;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.1
 */
public class ActivityPreferenceTest extends UITestCase {
    /**
     * Preference prefix - must match the one specified in ActivityPreferenceHelper
     */
    private static String PREFIX = "UIActivities."; //$NON-NLS-1$
    /**
     * The activity id
     */
    private static String ID = "org.eclipse.ui.PT.A2"; //$NON-NLS-1$
    
    /**
     * @param testName
     */
    public ActivityPreferenceTest(String testName) {
        super(testName);
    }
    
    /**
     * Tests whether activity preferences are persisted as soon as the activity set changes.
     */
    public void testActivityPreference() {
        IActivityManager manager = fWorkbench.getActivitySupport().getActivityManager();
        
        boolean initialState = manager.getEnabledActivityIds().contains(ID);
        IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
        assertEquals(initialState, store.getBoolean(PREFIX + ID));
        
        fWorkbench.getActivitySupport().setEnabledActivityIds(initialState ? Collections.EMPTY_SET : Collections.singleton(ID));
        assertEquals(!initialState, store.getBoolean(PREFIX + ID));
    }

}
