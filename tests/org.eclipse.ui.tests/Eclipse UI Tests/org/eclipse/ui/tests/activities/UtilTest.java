/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.activities;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.internal.expressions.TestExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * Tests various utility methods on WorkbenchActivityHelper as well as other misc. activities functionality.
 * 
 * @since 3.1
 */
public class UtilTest extends TestCase {
	
	private Set rememberedSet;
	
	public static final String ID1 = "org.eclipse.ui.tests.util.1";
	public static final String ID2 = "org.eclipse.ui.tests.util.2";
	public static final String ID3 = "org.eclipse.ui.tests.util.3";
	public static final String ID4 = "org.eclipse.ui.tests.util.4";
	public static final String ID5 = "org.eclipse.ui.tests.util.5";
	
	/**
	 * @param name
	 */
	public UtilTest(String name) {
		super(name); 
	}

	/**
	 * Asserts that if you enable cat 1 then cat 3 would also be enabled (they
	 * contain the same activity).
	 */
	public void testGetEnabledCategories1() {
		Set ids = WorkbenchActivityHelper.getEnabledCategories(getActivityManager(), ID1);
		assertEquals(1, ids.size());
		assertTrue(ids.contains(ID3));
	}

	/**
	 * Asserts that if you enable cat 2 then cat 1 and cat 3 would also be
	 * enabled. Cat 2 has activity 2, which depends on activity 1.
	 */
	public void testGetEnabledCategories2() {
		Set ids = WorkbenchActivityHelper.getEnabledCategories(getActivityManager(), ID2);
		assertEquals(2, ids.size());
		assertTrue(ids.contains(ID1));
		assertTrue(ids.contains(ID3));
	}
		
	/**
	 * Asserts that if you enable cat 3 then cat 1 would also be enabled (they
	 * contain the same activity).
	 */
	public void testGetEnabledCategories3() {
		Set ids = WorkbenchActivityHelper.getEnabledCategories(getActivityManager(), ID3);
		assertEquals(1, ids.size());
		assertTrue(ids.contains(ID1));
	}
	
	/**
	 * Asserts that if you enable cat 4 then no other categories would change..
	 */
	public void testGetEnabledCategories4() {
		Set ids = WorkbenchActivityHelper.getEnabledCategories(getActivityManager(), ID4);
		assertEquals(0, ids.size());
	}
	
	/**
	 * Asserts that if you enable cat 5 then cat 4 will become enabled
	 */
	public void testGetEnabledCategories5() {
		Set ids = WorkbenchActivityHelper.getEnabledCategories(getActivityManager(), ID5);
		assertEquals(1, ids.size());
		assertTrue(ids.contains(ID4));
	}

	/**
	 * Asserts that if you enable cat 1 when it's activity is already enabled
	 * then no categories would change.
	 */
	public void testGetEnabledCategories1_A() {
		HashSet set = new HashSet();
		set.add(ID1);
		PlatformUI.getWorkbench().getActivitySupport().setEnabledActivityIds(set);
		assertEquals(0, WorkbenchActivityHelper.getEnabledCategories(getActivityManager(), ID1).size());
	}
	
	/**
	 * Asserts that if you enable cat 2 when it's activity is already enabled
	 * then no categories would change.
	 */
	public void testGetEnabledCategories2_A() {
		HashSet set = new HashSet();
		set.add(ID2);
		PlatformUI.getWorkbench().getActivitySupport().setEnabledActivityIds(set);
		assertEquals(0, WorkbenchActivityHelper.getEnabledCategories(getActivityManager(), ID2).size());
	}
	
	/**
	 * Asserts that if you enable cat 3 when it's activity is already enabled
	 * then no categories would change.
	 */
	public void testGetEnabledCategories3_A() {
		HashSet set = new HashSet();
		set.add(ID1);
		PlatformUI.getWorkbench().getActivitySupport().setEnabledActivityIds(set);
		assertEquals(0, WorkbenchActivityHelper.getEnabledCategories(getActivityManager(), ID3).size());
	}	
	
	/**
	 * Asserts that if you enable cat 4 when it's activity is already enabled
	 * then no categories would change.
	 */
	public void testGetEnabledCategories4_A() {
		HashSet set = new HashSet();
		set.add(ID4);
		PlatformUI.getWorkbench().getActivitySupport().setEnabledActivityIds(set);
		assertEquals(0, WorkbenchActivityHelper.getEnabledCategories(getActivityManager(), ID4).size());
	}
	
	/**
	 * Asserts that if you enable cat 5 when activity 4 is already enabled
	 * then no categories would change.
	 */
	public void testGetEnabledCategories5_Aa() {
		HashSet set = new HashSet();
		set.add(ID4);
		PlatformUI.getWorkbench().getActivitySupport().setEnabledActivityIds(set);
		assertEquals(0, WorkbenchActivityHelper.getEnabledCategories(getActivityManager(), ID5).size());
	}
	
	/**
	 * Asserts that if you enable cat 5 when activity 5 is already enabled
	 * then cat 4 would change.
	 */
	public void testGetEnabledCategories5_Ab() {
		HashSet set = new HashSet();
		set.add(ID5);
		PlatformUI.getWorkbench().getActivitySupport().setEnabledActivityIds(set);
		Set ids = WorkbenchActivityHelper.getEnabledCategories(getActivityManager(), ID5);
		assertEquals(1, ids.size());
		assertTrue(ids.contains(ID4));
	}	
	
	/**
	 * Asserts that if you disable cat 1 then cat 3 would also be disabled (they
	 * contain the same activity).
	 */
	public void testGetDisabledCategories1() {
		enableAll();
		Set ids = WorkbenchActivityHelper.getDisabledCategories(getActivityManager(), ID1);
		assertEquals(1, ids.size());
		assertTrue(ids.contains(ID3));
	}
	
	/**
	 * Asserts that if you disable cat 2 then cat 1 and cat 3 would also be disabled.
	 */
	public void testGetDisabledCategories2() {
		enableAll();
		Set ids = WorkbenchActivityHelper.getDisabledCategories(getActivityManager(), ID2);
		assertEquals(2, ids.size());
		assertTrue(ids.contains(ID1));
		assertTrue(ids.contains(ID3));
	}
	
	/**
	 * Asserts that if you disable cat 3 then cat 1 would also be disabled.
	 */
	public void testGetDisabledCategories3() {
		enableAll();
		Set ids = WorkbenchActivityHelper.getDisabledCategories(getActivityManager(), ID3);
		assertEquals(1, ids.size());
		assertTrue(ids.contains(ID1));
	}
	
	/**
	 * Asserts that if you disable cat 4 then no other categories would also be disabled.
	 */
	public void testGetDisabledCategories4() {
		enableAll();
		Set ids = WorkbenchActivityHelper.getDisabledCategories(getActivityManager(), ID4);
		assertEquals(0, ids.size());
	}
	
	/**
	 * Asserts that if you disable cat 5 then cat 4 would also be disabled.
	 */
	public void testGetDisabledCategories5() {
		enableAll();
		Set ids = WorkbenchActivityHelper.getDisabledCategories(getActivityManager(), ID5);
		assertEquals(1, ids.size());
		assertTrue(ids.contains(ID4));
	}
	
	/**
	 * Asserts that the enabled category count for activity 1 is 2 (cat 1 and 3).
	 */
	public void testCategoryCount1_A() {
		enableAll();
		Set ids = WorkbenchActivityHelper.getEnabledCategoriesForActivity(getActivityManager(), ID1);
		assertEquals(2, ids.size());
		assertTrue(ids.contains(ID1));
		assertTrue(ids.contains(ID3));
	}
	
	/**
	 * Asserts that the enabled category count for activity 2 is 1 (cat 2).
	 */
	public void testCategoryCount2_A() {
		enableAll();
		Set ids = WorkbenchActivityHelper.getEnabledCategoriesForActivity(getActivityManager(), ID2);
		assertEquals(1, ids.size());
		assertTrue(ids.contains(ID2));
	}
	
	/**
	 * Asserts that the enabled category count for activity 4 is 2 (cat 4 and 5).
	 */
	public void testCategoryCount4_A() {
		enableAll();
		Set ids = WorkbenchActivityHelper.getEnabledCategoriesForActivity(getActivityManager(), ID4);
		assertEquals(2, ids.size());
		assertTrue(ids.contains(ID4));
		assertTrue(ids.contains(ID5));
	}
	
	/**
	 * Asserts that the enabled category count for activity 5 is 1 (cat 5).
	 */
	public void testCategoryCount5_A() {
		enableAll();
		Set ids = WorkbenchActivityHelper.getEnabledCategoriesForActivity(getActivityManager(), ID5);
		assertEquals(1, ids.size());
		assertTrue(ids.contains(ID5));
	}
	
	/**
	 * Test the activity property tester.  Test the isActivityEnabled property
	 * 
	 */
	public void testPropertyTester1() {
		enableAll();
		EvaluationContext context = new EvaluationContext(null, PlatformUI.getWorkbench());

		IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI
				.getWorkbench().getActivitySupport();
		IActivityManager activityManager = workbenchActivitySupport
				.getActivityManager();
		
		testPropertyTester1(context, activityManager);
		Set set = new HashSet();
		workbenchActivitySupport.setEnabledActivityIds(set);
		
		testPropertyTester1(context, activityManager);
	}

	/**
	 * @param context
	 * @param activityManager
	 */
	private void testPropertyTester1(EvaluationContext context,
			IActivityManager activityManager) {
		boolean result = activityManager
				.getActivity(ID1).isEnabled();

		TestExpression test = new TestExpression("org.eclipse.ui",
				"isActivityEnabled", new Object[] { ID1 },
				null);
		
		try {
			assertEquals(result ? EvaluationResult.TRUE: EvaluationResult.FALSE, test.evaluate(context));
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test the activity property tester.  Test the isCategoryEnabled property
	 * 
	 */
	public void testPropertyTester2() {
		enableAll();
		EvaluationContext context = new EvaluationContext(null, PlatformUI.getWorkbench());

		IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI
				.getWorkbench().getActivitySupport();
		IActivityManager activityManager = workbenchActivitySupport
				.getActivityManager();
		
		testPropertyTester2(context, activityManager);
		Set set = new HashSet();
		workbenchActivitySupport.setEnabledActivityIds(set);
		
		testPropertyTester2(context, activityManager);
	}
	
	/**
	 * @param context
	 * @param activityManager
	 */
	private void testPropertyTester2(EvaluationContext context,
			IActivityManager activityManager) {
		boolean result = WorkbenchActivityHelper.isEnabled(activityManager, ID1);


		TestExpression test = new TestExpression("org.eclipse.ui",
				"isCategoryEnabled", new Object[] { ID1 },
				null);
		
		try {
			assertEquals(result ? EvaluationResult.TRUE: EvaluationResult.FALSE, test.evaluate(context));
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}
	
	/**
	 * Enable all test activities.
	 */
	private void enableAll() {
		HashSet set = new HashSet();
		set.add(ID1);
		set.add(ID2);
		set.add(ID4);
		set.add(ID5);
		PlatformUI.getWorkbench().getActivitySupport().setEnabledActivityIds(
				set);
	}

    /**
     * Return the system activity manager.
     * 
     * @return the system activity manager
     */
    private IActivityManager getActivityManager() {
        return  PlatformUI.getWorkbench()
        .getActivitySupport().getActivityManager();
    }
    
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		rememberedSet = getActivityManager().getEnabledActivityIds();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		PlatformUI.getWorkbench().getActivitySupport().setEnabledActivityIds(
				rememberedSet);
	}
}
