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

import java.util.Iterator;

import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.NotDefinedException;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Tests that the Persistance class is catching malformed registry entries.
 * 
 * @since 3.1
 */
public class PersistanceTest extends UITestCase {

    /**
     * @param testName
     */
    public PersistanceTest(String testName) {
        super(testName);
    }

    public void testCategoryPermutations() {
        try {
	        IActivityManager manager = fWorkbench.getActivitySupport().getActivityManager();
	        ICategory category = manager.getCategory("org.eclipse.ui.PT.C1"); // should not be defined - missing name
	        assertFalse(category.isDefined());
	        
	        category = manager.getCategory("org.eclipse.ui.PT.C2"); // should be defined - missing desc
	        assertTrue(category.isDefined());
	        assertNotNull(category.getDescription());
	        
	        for (Iterator i = manager.getDefinedCategoryIds().iterator(); i.hasNext(); ) {
	            if (manager.getCategory((String) i.next()).getName().equals("org.eclipse.ui.PT.C3"))
	                fail("Found category that should not be.");
	        }
        }
        catch (NotDefinedException e) {
            fail(e.getMessage());
        }
    }    
    
    public void testActivityRequirementBindings() {
        IActivityManager manager = fWorkbench.getActivitySupport().getActivityManager();
        IActivity activity  = manager.getActivity("org.eclipse.ui.PT.A2");
        assertTrue(activity.getActivityRequirementBindings().isEmpty());     
    }
    
    public void testActivityPatternBindings() {
        IActivityManager manager = fWorkbench.getActivitySupport().getActivityManager();
        IActivity activity  = manager.getActivity("org.eclipse.ui.PT.A2");
        assertTrue(activity.getActivityPatternBindings().isEmpty());     
    }

    public void testCategoryActivityBindings() {
        IActivityManager manager = fWorkbench.getActivitySupport().getActivityManager();
        ICategory category  = manager.getCategory("org.eclipse.ui.PT.C2");
        assertTrue(category.getCategoryActivityBindings().isEmpty());     
    } 
    
    public void testActivityPermutations() {
        try {
	        IActivityManager manager = fWorkbench.getActivitySupport().getActivityManager();
	        IActivity activity = manager.getActivity("org.eclipse.ui.PT.A1"); // should not be defined - missing name
	        assertFalse(activity.isDefined());
	        
	        activity = manager.getActivity("org.eclipse.ui.PT.A2"); // should be defined - missing desc
	        assertTrue(activity.isDefined());
	        assertNotNull(activity.getDescription());
	        
	        for (Iterator i = manager.getDefinedActivityIds().iterator(); i.hasNext(); ) {
	            if (manager.getActivity((String) i.next()).getName().equals("org.eclipse.ui.PT.A3"))
	                fail("Found activity that should not be.");
	        }
        }
        catch (NotDefinedException e) {
            fail(e.getMessage());
        }
    }
}
