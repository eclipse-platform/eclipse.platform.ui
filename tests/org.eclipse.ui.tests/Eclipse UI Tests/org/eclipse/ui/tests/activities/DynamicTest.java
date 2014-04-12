/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.activities;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ActivityEvent;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.CategoryEvent;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityListener;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.activities.IActivityPatternBinding;
import org.eclipse.ui.activities.IActivityRequirementBinding;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.ICategoryActivityBinding;
import org.eclipse.ui.activities.ICategoryListener;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IIdentifierListener;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.IdentifierEvent;
import org.eclipse.ui.activities.NotDefinedException;
import org.eclipse.ui.activities.WorkbenchTriggerPointAdvisor;
import org.eclipse.ui.internal.activities.MutableActivityManager;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * 
 * The dynamic test manipualtes the categories, activities and their definitions
 * and ensures that their content and their listeners are behaving properly.
 */
public class DynamicTest extends UITestCase {
    private MutableActivityManager activityManager;

    private DynamicModelActivityRegistry fixedModelRegistry;

    private int listenerType;

    static final int ACTIVITY_ENABLED_CHANGED = 0;

    static final int ACTIVITY_IDS_CHANGED = 1;

    static final int ENABLED_ACTIVITYIDS_CHANGED = 2;

    static final int DEFINED_CATEGORYIDS_CHANGED = 3;

    static final int DEFINED_ACTIVITYIDS_CHANGED = 4;

    static final int DEFINED_CHANGED = 5;

    static final int ENABLED_CHANGED = 6;

    static final int NAME_CHANGED = 7;

    static final int PATTERN_BINDINGS_CHANGED = 8;

    static final int ACTIVITY_ACTIVITY_BINDINGS_CHANGED = 9;

    static final int DESCRIPTION_CHANGED = 10;
    
    static final int DEFAULT_ENABLED_CHANGED = 11;

    /**
     * Constructor.
     * 
     * @param testName
     *            Test's name.
     */
    public DynamicTest(String testName) {
        super(testName);
        fixedModelRegistry = new DynamicModelActivityRegistry();
        activityManager = new MutableActivityManager(new WorkbenchTriggerPointAdvisor(), fixedModelRegistry);
        listenerType = -1;
    }

    /**
     * Test sizes of what has been read.
     *  
     */
    public void testSizes() {
        assertTrue(activityManager.getDefinedCategoryIds().size() == 6);
        assertTrue(activityManager.getDefinedActivityIds().size() == 18);
        assertTrue(activityManager.getEnabledActivityIds().size() == 3);
    }

    /**
     * Test activity bindings.
     *  
     */
    public void testActivityPatternBindings() {
        IActivity first_activity = activityManager
                .getActivity((String) activityManager.getDefinedActivityIds()
                        .toArray()[0]);
        Set initialPatternBindings = first_activity
                .getActivityPatternBindings();
        // Add pattern binding
        String pattern = "org\\.eclipse\\.ui\\.myPattern/.*"; //$NON-NLS-1$
        fixedModelRegistry.addActivityPatternBinding(first_activity.getId(),
                pattern);
        assertFalse(initialPatternBindings.size() == first_activity
                .getActivityPatternBindings().size());
        // Remove pattern binding
        fixedModelRegistry.removeActivityPatternBinding(pattern);
        assertTrue(initialPatternBindings.size() == first_activity
                .getActivityPatternBindings().size());
    }

    /**
     * Test the enabled activities.
     *  
     */
    public void testEnabledActivities() {
        // Add an enabled activity
        Set compareSet;
        Set copySet = new HashSet(activityManager.getEnabledActivityIds());
        copySet.add(activityManager.getDefinedActivityIds().toArray()[0]);
        activityManager.setEnabledActivityIds(copySet);
        compareSet = activityManager.getEnabledActivityIds();
        assertTrue(compareSet.size() == copySet.size());
        // Remove an enabled activity
        copySet.remove(activityManager.getDefinedActivityIds().toArray()[0]);
        activityManager.setEnabledActivityIds(copySet);
        compareSet = activityManager.getEnabledActivityIds();
        assertTrue(compareSet.size() == copySet.size());
    }

    /**
     * Test the identifier listener.
     *  
     */
    public void testIdentifiersListener() {
        final IIdentifier enabledIdentifier = activityManager
                .getIdentifier("org.eclipse.pattern3"); //$NON-NLS-1$
        assertTrue(enabledIdentifier.isEnabled());
        enabledIdentifier.addIdentifierListener(new IIdentifierListener() {
            public void identifierChanged(IdentifierEvent identifierEvent) {
                switch (listenerType) {
                case ACTIVITY_ENABLED_CHANGED:
                    assertTrue(identifierEvent.hasEnabledChanged());
                    break;
                case ACTIVITY_IDS_CHANGED:
                    assertTrue(identifierEvent.hasActivityIdsChanged());
                    break;
                }
                listenerType = -1;
            }
        });
        // Test correcteness of identifier
        IIdentifier activitiesIdentifier = activityManager
                .getIdentifier("org.eclipse.pattern4"); //$NON-NLS-1$
        Set identifiedActivities = activitiesIdentifier.getActivityIds(); //$NON-NLS-1$
        assertTrue(identifiedActivities.size() == 1);
        assertTrue(((String) identifiedActivities.toArray()[0])
                .equals("org.eclipse.activity4")); //$NON-NLS-1$
        assertFalse(activitiesIdentifier.isEnabled());
        // Disable Enabled activity
        listenerType = 0;
        Set copySet = new HashSet(activityManager.getEnabledActivityIds());
        copySet.remove(enabledIdentifier.getActivityIds().toArray()[0]);
        activityManager.setEnabledActivityIds(copySet);
        assertTrue(listenerType == -1);
        // Enable Disabled activity
        listenerType = 0;
        copySet.add("org.eclipse.activity3"); //$NON-NLS-1$
        activityManager.setEnabledActivityIds(copySet);
        assertTrue(listenerType == -1);
        // Add pattern binding
        listenerType = 1;
        fixedModelRegistry.addActivityPatternBinding("org.eclipse.activity1", //$NON-NLS-1$
                "org.eclipse.pattern3"); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(listenerType == -1);
        // Test correctenesss of identifier
        Set manipulatedIdentifiers = activityManager.getIdentifier(
                "org.eclipse.pattern3").getActivityIds(); //$NON-NLS-1$
        assertTrue(manipulatedIdentifiers.size() == 2);
        // Remove pattern binding
        listenerType = 1;
        fixedModelRegistry.removeActivityPatternBinding("org.eclipse.pattern3"); //$NON-NLS-1$
        assertTrue(listenerType == -1);
        manipulatedIdentifiers = activityManager.getIdentifier(
                "org.eclipse.pattern3").getActivityIds(); //$NON-NLS-1$
        assertTrue(manipulatedIdentifiers.size() == 1);
    }

    /**
     * Test the activity manager listener.
     *  
     */
    public void testActivityManagerListener() {
        activityManager
                .addActivityManagerListener(new IActivityManagerListener() {
                    public void activityManagerChanged(
                            ActivityManagerEvent activityManagerEvent) {
                        switch (listenerType) {
                        case ENABLED_ACTIVITYIDS_CHANGED:
                            assertTrue(activityManagerEvent
                                    .haveEnabledActivityIdsChanged());
                            break;
                        case DEFINED_CATEGORYIDS_CHANGED:
                            assertTrue(activityManagerEvent
                                    .haveDefinedCategoryIdsChanged());
                            break;
                        case DEFINED_ACTIVITYIDS_CHANGED:
                            assertTrue(activityManagerEvent
                                    .haveDefinedActivityIdsChanged());
                            break; 
                        }
                        listenerType = -1;
                    }
                });
        // Add an enabled activity
        listenerType = 2;
        Set enabledSet = new HashSet(activityManager.getEnabledActivityIds());
        enabledSet.add("org.eclipse.activity19"); //$NON-NLS-1$
        activityManager.setEnabledActivityIds(enabledSet);
        assertTrue(listenerType == -1);
        // Remove an enabled activity
        listenerType = 2;
        enabledSet.remove("org.eclipse.activity19"); //$NON-NLS-1$
        activityManager.setEnabledActivityIds(enabledSet);
        assertTrue(listenerType == -1);
        // Add categroy
        listenerType = 3;
        fixedModelRegistry.addCategory("org.eclipse.category7", "Category 7"); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(listenerType == -1);
        // Remove category
        listenerType = 3;
        fixedModelRegistry.removeCategory("org.eclipse.category7", //$NON-NLS-1$
                "Category 7"); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(listenerType == -1);
        // Add activity
        listenerType = 4;
        fixedModelRegistry.addActivity("org.eclipse.activity19", "Activity 19"); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(listenerType == -1);
        // Remove activity
        listenerType = 4;
        fixedModelRegistry.removeActivity("org.eclipse.activity19", //$NON-NLS-1$
                "Activity 19"); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(listenerType == -1);
    }

    /**
     * Test the activity listener.
     *  
     */
    public void testActivityListener() {
        final String activity_to_listen_name = "Activity 18"; //$NON-NLS-1$
        final IActivity activity_to_listen = activityManager
                .getActivity("org.eclipse.activity18"); //$NON-NLS-1$
        activity_to_listen.addActivityListener(new IActivityListener() {
            public void activityChanged(ActivityEvent activityEvent) {
                switch (listenerType) {
                case DEFINED_CHANGED:
                    assertTrue(activityEvent.hasDefinedChanged());
                    break;
                case ENABLED_CHANGED:
                    assertTrue(activityEvent.hasEnabledChanged());
                    break;
                case NAME_CHANGED:
                    assertTrue(activityEvent.hasNameChanged());
                    break;
                case PATTERN_BINDINGS_CHANGED:
                    assertTrue(activityEvent
                            .haveActivityPatternBindingsChanged());
                    break;
                case ACTIVITY_ACTIVITY_BINDINGS_CHANGED:
                    assertTrue(activityEvent
                            .haveActivityRequirementBindingsChanged());
                    break;
                case DESCRIPTION_CHANGED:
                    assertTrue(activityEvent.hasDescriptionChanged());
                    break;
                case DEFAULT_ENABLED_CHANGED:
                    assertTrue(activityEvent.hasDefaultEnabledChanged());
                    break;
                }
                listenerType = -1;
            }
        });
        // Remove activity and change name consequently
        fixedModelRegistry.removeActivity(activity_to_listen.getId(),
                activity_to_listen_name);
        assertTrue(listenerType == -1);
        // Add activity
        listenerType = 5;
        fixedModelRegistry.addActivity(activity_to_listen.getId(),
                activity_to_listen_name);
        assertTrue(listenerType == -1);
        // Add to enabled activity
        listenerType = 6;
        Set enabledSet = new HashSet(activityManager.getEnabledActivityIds());
        enabledSet.add(activity_to_listen.getId());
        activityManager.setEnabledActivityIds(enabledSet);
        assertTrue(listenerType == -1);
        // Remove from enabled activity
        listenerType = 6;
        enabledSet.remove(activity_to_listen.getId());
        activityManager.setEnabledActivityIds(enabledSet);
        assertTrue(listenerType == -1);
        // Add pattern binding
        listenerType = 8;
        fixedModelRegistry.addActivityPatternBinding("org.eclipse.activity18", //$NON-NLS-1$
                "org.eclipse.pattern3"); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(listenerType == -1);
        // Remove pattern binding
        listenerType = 8;
        fixedModelRegistry.removeActivityPatternBinding("org.eclipse.pattern3");//$NON-NLS-1$
        assertTrue(listenerType == -1);
        // Add activity activity binding as parent
        listenerType = 9;
        fixedModelRegistry.addActivityRequirementBinding(
                "org.eclipse.activity9", //$NON-NLS-1$
                activity_to_listen.getId());//$NON-NLS-1$
        assertTrue(listenerType == -1);
        // Remove activity activity binding as parent
        listenerType = 9;
        fixedModelRegistry.removeActivityRequirementBinding(
                "org.eclipse.activity9", activity_to_listen.getId());//$NON-NLS-1$
        assertTrue(listenerType == -1);
        //		 Update activity name
        listenerType = 7;
        fixedModelRegistry.updateActivityName(activity_to_listen.getId(),
                "name_change"); //$NON-NLS-1$
        assertTrue(listenerType == -1);
        // Update activity description
        listenerType = 10;
        fixedModelRegistry.updateActivityDescription(
                activity_to_listen.getId(), "description_change"); //$NON-NLS-1$
        assertTrue(listenerType == -1);

        // check default enablement
        listenerType = DEFAULT_ENABLED_CHANGED;
        fixedModelRegistry.addDefaultEnabledActivity(activity_to_listen.getId());
        assertTrue(listenerType == -1);
        try {
            assertTrue(activity_to_listen.isDefaultEnabled());
        } catch (NotDefinedException e1) {
            fail(e1.getMessage());
        }
        
        listenerType = DEFAULT_ENABLED_CHANGED;
        fixedModelRegistry.removeDefaultEnabledActivity(activity_to_listen.getId());
        assertTrue(listenerType == -1);
        try {
            assertFalse(activity_to_listen.isDefaultEnabled());
        } catch (NotDefinedException e1) {
            fail(e1.getMessage());
        }
    }

    /**
     * Test the category listener.
     *  
     */
    public void testCategoryListener() {
        final ICategory category_to_listen = activityManager
                .getCategory((String) activityManager.getDefinedCategoryIds()
                        .toArray()[0]);
        category_to_listen.addCategoryListener(new ICategoryListener() {
            public void categoryChanged(CategoryEvent categoryEvent) {
                switch (listenerType) {
                case DEFINED_CHANGED:
                    assertTrue(categoryEvent.hasDefinedChanged());
                    break;
                case NAME_CHANGED:
                    assertTrue(categoryEvent.hasNameChanged());
                    break;
                case PATTERN_BINDINGS_CHANGED:
                    assertTrue(categoryEvent
                            .haveCategoryActivityBindingsChanged());
                    break;
                case DESCRIPTION_CHANGED:
                    //	assertTrue(categoryEvent.hasDescriptionChanged());
                    break;
                }
                listenerType = -1;
            }
        });
        // Remove category, and change name
        try {
            fixedModelRegistry.removeCategory(category_to_listen.getId(),
                    category_to_listen.getName()); //$NON-NLS-1$
        } catch (NotDefinedException e) {
            e.printStackTrace(System.err);
        }
        assertTrue(listenerType == -1);
        // Add category
        listenerType = 5;
        fixedModelRegistry
                .addCategory(category_to_listen.getId(), "Category 6"); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(listenerType == -1);
        // Add category activity binding
        listenerType = 8;
        fixedModelRegistry.addCategoryActivityBinding((String) activityManager
                .getDefinedActivityIds().toArray()[4], category_to_listen
                .getId()); //$NON-NLS-1$
        assertTrue(listenerType == -1);
        // Remove activity activity binding
        listenerType = 8;
        fixedModelRegistry.removeCategoryActivityBinding(
                (String) activityManager.getDefinedActivityIds().toArray()[4],
                category_to_listen.getId());//$NON-NLS-1$
        // Change category description
        listenerType = 10;
        fixedModelRegistry.updateCategoryDescription(
                category_to_listen.getId(), "description_change"); //$NON-NLS-1$
        try {
            assertTrue(category_to_listen.getDescription().equals(
                    "description_change")); //$NON-NLS-1$
        } catch (NotDefinedException e) {
            e.printStackTrace(System.err);
        }
        assertTrue(listenerType == -1);
        // Change category name
        listenerType = 7;
        fixedModelRegistry.updateCategoryName(category_to_listen.getId(),
                "name_change"); //$NON-NLS-1$
        try {
            assertTrue(category_to_listen.getName().equals("name_change")); //$NON-NLS-1$
        } catch (NotDefinedException e) {
            e.printStackTrace(System.err);
        }
        assertTrue(listenerType == -1);
    }
    
	/**
	 * Tests to ensure dynamism with regard to the extension registry.
	 */
	public void testDynamicRegistry() {
		IWorkbenchActivitySupport was = PlatformUI.getWorkbench()
				.getActivitySupport();
		IActivity activity = was.getActivityManager().getActivity(
				"dynamic.activity");
		ICategory category = was.getActivityManager().getCategory(
				"dynamic.category");
		assertFalse(activity.isDefined());
		assertFalse(category.isDefined());
		// set to true when the activity/category in question have had an event
		// fired
		final boolean[] registryChanged = new boolean[] { false, false };
		activity.addActivityListener(new IActivityListener() {

			public void activityChanged(ActivityEvent activityEvent) {
				registryChanged[0] = true;

			}
		});
		category.addCategoryListener(new ICategoryListener() {

			public void categoryChanged(CategoryEvent categoryEvent) {
				System.err.println("categoryChanged");
				registryChanged[1] = true;

			}
		});

		try {
			String ACTIVITY = "<plugin><extension point=\"org.eclipse.ui.activities\">"
					+ "<category id=\"dynamic.category\" name=\"Dynamic Activity Category\"/>"
					+ "<activity id=\"dynamic.activity\" name=\"Dynamic Activity\"/>"
					+ "<activity id=\"dynamic.parent\" name=\"Dynamic Parent Activity\"/>"
					+ "<activityRequirementBinding requiredActivityId = \"dynamic.parent\" activityId = \"dynamic.activity\" />"
					+ "<categoryActivityBinding categoryId = \"dynamic.category\" activityId = \"dynamic.activity\" />"
					+ "<activityPatternBinding activityId=\"dynamic.activity\"  pattern=\"dynamic.activity/.*\"/>"
					+ "<defaultEnablement id=\"dynamic.activity\"/>"
					+ "</extension></plugin>";
			byte[] bytes = ACTIVITY.toString().getBytes("UTF-8");
			InputStream is = new ByteArrayInputStream(bytes);
			IContributor contrib = ContributorFactoryOSGi
					.createContributor(TestPlugin.getDefault().getBundle());
			ExtensionRegistry registry = (ExtensionRegistry) RegistryFactory
					.getRegistry();
			if (!registry.addContribution(is, contrib, false, null, null,
					registry.getTemporaryUserToken()))
				throw new RuntimeException();
		} catch (UnsupportedEncodingException e) {
			fail(e.getMessage(), e);
		}

		// spin the event loop and ensure that the changes come down the pipe.
		// 20 seconds should be more than enough
		long endTime = System.currentTimeMillis() + 20000;
		while (!(registryChanged[0] && registryChanged[1])
				&& System.currentTimeMillis() < endTime) {

			Display display = PlatformUI.getWorkbench().getDisplay();
			if (display != null && !display.isDisposed())
				while (display.readAndDispatch())
					;
			display.sleep();

		}

		assertTrue("Activity Listener not called", registryChanged[0]);
		assertTrue("Category Listener not called", registryChanged[1]);

		assertTrue(activity.isDefined());
		Set patternBindings = activity.getActivityPatternBindings();
		assertEquals(1, patternBindings.size());

		IActivityPatternBinding patternBinding = (IActivityPatternBinding) patternBindings
				.iterator().next();

		assertEquals("dynamic.activity/.*", patternBinding.getPattern()
				.pattern());
		assertEquals("dynamic.activity", patternBinding.getActivityId());

		try {
			assertTrue(activity.isDefaultEnabled());
		} catch (NotDefinedException e) {
			fail(e.getMessage(), e);
		}

		Set requirementBindings = activity.getActivityRequirementBindings();
		assertEquals(1, requirementBindings.size());

		IActivityRequirementBinding requirementBinding = (IActivityRequirementBinding) requirementBindings
				.iterator().next();
		assertEquals("dynamic.parent", requirementBinding
				.getRequiredActivityId());
		assertEquals("dynamic.activity", requirementBinding.getActivityId());

		assertTrue(category.isDefined());
		Set categoryBindings = category.getCategoryActivityBindings();
		assertEquals(1, categoryBindings.size());
		ICategoryActivityBinding categoryBinding = (ICategoryActivityBinding) categoryBindings
				.iterator().next();
		assertEquals("dynamic.activity", categoryBinding.getActivityId());
		assertEquals("dynamic.category", categoryBinding.getCategoryId());

	}
}
