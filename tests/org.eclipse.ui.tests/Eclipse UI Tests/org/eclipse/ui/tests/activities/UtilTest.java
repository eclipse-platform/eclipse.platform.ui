/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.activities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.internal.expressions.TestExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityPatternBinding;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.services.IEvaluationService;

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
	
	
	public static final String EXPRESSION_ACTIVITY_ID = "org.eclipse.ui.tests.filter1.enabled";
	public static final String EXPRESSION_ACTIVITY_ID_2 = "org.eclipse.ui.tests.filter2.enabled";
	public static final String EXPRESSION_ACTIVITY_ID_3 = "org.eclipse.ui.tests.filter3.enabled";
	public static final String EXPRESSION_ACTIVITY_ID_4 = "org.eclipse.ui.tests.filter4.enabled";
	public static final String EXPRESSION_ACTIVITY_ID_5 = "org.eclipse.ui.tests.filter5.enabled";
	public static final String EXPRESSION_ACTIVITY_ID_6 = "org.eclipse.ui.tests.filter6.enabled";
	public static final String EXPRESSION_ACTIVITY_ID_7 = "org.eclipse.ui.tests.filter7.enabled";

	public static final String EXPRESSION_VALUE = "org.eclipse.ui.command.contexts.enablement_test1";

	class TestSourceProvider extends AbstractSourceProvider {
		public static final String VARIABLE = "arbitraryVariable";
		public static final String VALUE = "arbitraryValue";

		private Map sourceState = new HashMap(1);

		public TestSourceProvider() {
			super();
			clearVariable();
		}

		@Override
		public Map getCurrentState() {
			return sourceState;
		}

		@Override
		public String[] getProvidedSourceNames() {
			return new String[] { VARIABLE };
		}

		@Override
		public void dispose() {
		}

		/**
		 * @see {@link #fireSourceChanged(int, Map)}
		 */
		public void fireSourceChanged() {
			fireSourceChanged(0, sourceState);
		}

		/**
		 * Sets variable to value. Triggers no fireSourceChanged() update.
		 */
		public void setVariable() {
			sourceState.put(VARIABLE, VALUE);
		}
		
		/**
		 * Clears variable to empty string. Triggers no fireSourceChanged()
		 * update.
		 */
		public void clearVariable() {
			sourceState.put(VARIABLE, "");
		}
	};
	
	public void testExpressionEnablement() throws Exception {
		IPluginContribution filterExp = new IPluginContribution() {
			@Override
			public String getLocalId() {
				return "filter";
			}
			@Override
			public String getPluginId() {
				return "org";
			}
		};
		IPluginContribution filterExp2 = new IPluginContribution() {
			@Override
			public String getLocalId() {
				return "filter2";
			}
			@Override
			public String getPluginId() {
				return "org";
			}
		};
		IPluginContribution noExp = new IPluginContribution() {
			@Override
			public String getLocalId() {
				return "donotfilter";
			}
			@Override
			public String getPluginId() {
				return "org";
			}
		};
		assertTrue(WorkbenchActivityHelper.filterItem(filterExp));
		assertTrue(WorkbenchActivityHelper.filterItem(noExp));
		assertTrue(WorkbenchActivityHelper.restrictUseOf(filterExp));
		assertFalse(WorkbenchActivityHelper.restrictUseOf(noExp));
		
		// The EXPRESSION_ACTIVITY_ID_3 is always true, and therefore it must
		// be in the enabledActivityIds list - right from the beginning.
		IWorkbenchActivitySupport support = PlatformUI.getWorkbench()
				.getActivitySupport();
		Set enabledActivityIds = support.getActivityManager()
				.getEnabledActivityIds();
		assertTrue(enabledActivityIds.contains(EXPRESSION_ACTIVITY_ID_3));
		
		// Test activityRequirmentBinding ignored on expression controlled
		// activities.
		// Test conventional activity depends on expression activity. 
		assertFalse(enabledActivityIds.contains(EXPRESSION_ACTIVITY_ID_4));
		assertFalse(enabledActivityIds.contains(EXPRESSION_ACTIVITY_ID_5));
		enabledActivityIds = new HashSet(enabledActivityIds);
		enabledActivityIds.add(EXPRESSION_ACTIVITY_ID_5);
		support.setEnabledActivityIds(enabledActivityIds);
		enabledActivityIds = support.getActivityManager()
				.getEnabledActivityIds();
		assertFalse(enabledActivityIds.contains(EXPRESSION_ACTIVITY_ID_4));
		assertTrue(enabledActivityIds.contains(EXPRESSION_ACTIVITY_ID_5));
		
		// Test expression activity depends on conventional activity.
		assertFalse(enabledActivityIds.contains(EXPRESSION_ACTIVITY_ID_6));
		assertTrue(enabledActivityIds.contains(EXPRESSION_ACTIVITY_ID_7));
		
		
		// need to enable the normal activity, org.eclipse.ui.tests.filter1.normal
		// and change the context to enable org.eclipse.ui.tests.filter1.enabled:
		// context: org.eclipse.ui.command.contexts.enablement_test1
		
		IContextService localService = PlatformUI
				.getWorkbench().getService(IContextService.class);
		IContextActivation activation = localService.activateContext(EXPRESSION_VALUE);
		try {
		// Not restricted anymore.
		assertFalse(WorkbenchActivityHelper.restrictUseOf(filterExp));

		// Test recognition of disabled expression which is already filtered.
		localService.deactivateContext(activation);
		assertTrue(WorkbenchActivityHelper.restrictUseOf(filterExp));		

		//
		// Testing with an arbitrary self-declared test variable.
		//
		TestSourceProvider testSourceProvider = new TestSourceProvider();
		IEvaluationService evalService = PlatformUI
				.getWorkbench().getService(IEvaluationService.class);
		evalService.addSourceProvider(testSourceProvider);
		testSourceProvider.fireSourceChanged();

		// Non-set variable.
		assertTrue(WorkbenchActivityHelper.restrictUseOf(filterExp2));

		// Set variable.
		testSourceProvider.setVariable();
		testSourceProvider.fireSourceChanged();
		assertFalse(WorkbenchActivityHelper.restrictUseOf(filterExp2));
		
		//------------------------
		// Rerun last test with a "twist" - "twist" described in next comment.
		//------------------------
		// Clear variable again.
		testSourceProvider.clearVariable();
		testSourceProvider.fireSourceChanged();
		
		// Put the activity in the enabledActivity list, so it would run into
		// problems if it not correctly recognizes the difference when already
		// marked as enabled (by being in the list) while the expression, which
		// controls the activity, becomes in reality only later enabled.		
		Set set = new HashSet(support.getActivityManager().getEnabledActivityIds());
		set.add(EXPRESSION_ACTIVITY_ID_2);
		support.setEnabledActivityIds(set);
		
		// Set variable again.
		testSourceProvider.setVariable();
		testSourceProvider.fireSourceChanged();
		assertFalse(WorkbenchActivityHelper.restrictUseOf(filterExp2));

		evalService.removeSourceProvider(testSourceProvider);
		}
		finally {
			localService.deactivateContext(activation);
		}
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
    
    /**
     * Tests non-regular Expression Pattern bindings.
     */
    public void testNonRegExpressionPattern() {    	
    	final String ACTIVITY_NON_REG_EXP = "org.eclipse.activityNonRegExp";
    	
    	// Check Activity -> Binding connection.
    	IActivityManager manager = getActivityManager();    	
    	IActivity activity = manager.getActivity(ACTIVITY_NON_REG_EXP);
    	Set bindings = activity.getActivityPatternBindings();
    	assertTrue(bindings.size() == 1);
    	IActivityPatternBinding binding = 
    		(IActivityPatternBinding)bindings.iterator().next();
    	assertTrue(binding.isEqualityPattern());
    	
    	// Check Binding -> Activity connection.
    	final String IDENTIFIER = "org.eclipse.ui.tests.activity{No{Reg(Exp[^d]";
    	IIdentifier identifier = manager.getIdentifier(IDENTIFIER);
    	Set boundActivities = identifier.getActivityIds();
    	assertTrue(boundActivities.size() == 1);
    	String id = boundActivities.iterator().next().toString();
    	assertTrue(id.equals(ACTIVITY_NON_REG_EXP));
    	
    	// Check conversion from normal string to regular expression string
    	// for <code>Pattern()</code> constructing.
    	Pattern pattern = binding.getPattern();    	
    	assertTrue(pattern.pattern().equals(
				Pattern.compile("\\Q" + IDENTIFIER + "\\E").pattern()));
    }    
    
    /**
	 * Tests to ensure that setting enabled of an activity disabled by
	 * expression and setting disabled of an activity enabled by expression both
	 * behave as expected. Ie: it's a no-op.
	 */
    public void testSetEnabledExpressionActivity() {
    	try {
    		TestSourceProvider testSourceProvider = new TestSourceProvider();
    		IEvaluationService evalService = PlatformUI
    				.getWorkbench().getService(IEvaluationService.class);
    		evalService.addSourceProvider(testSourceProvider);
    		testSourceProvider.fireSourceChanged();
    		
    		
    		IWorkbenchActivitySupport support = PlatformUI.getWorkbench()
    			.getActivitySupport();		
    		support.setEnabledActivityIds(new HashSet());
    		Set set = new HashSet(support.getActivityManager().getEnabledActivityIds());
    		Set previousSet = new HashSet(support.getActivityManager().getEnabledActivityIds());
    		set.add(EXPRESSION_ACTIVITY_ID_2);
    		support.setEnabledActivityIds(set);
    		assertEquals(previousSet, support.getActivityManager().getEnabledActivityIds());
    		
    		testSourceProvider.setVariable();
    		testSourceProvider.fireSourceChanged();
    		
    		set = new HashSet(support.getActivityManager().getEnabledActivityIds());
    		assertFalse(set.equals(previousSet));
    		
    		set.remove(EXPRESSION_ACTIVITY_ID_2);
    		support.setEnabledActivityIds(set);
    		
    		assertFalse(support.getActivityManager().getEnabledActivityIds().equals(previousSet));

    		evalService.removeSourceProvider(testSourceProvider);
    	}
    	finally {
    		
    	}
    }
    
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		rememberedSet = getActivityManager().getEnabledActivityIds();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		PlatformUI.getWorkbench().getActivitySupport().setEnabledActivityIds(
				rememberedSet);
	}
}
