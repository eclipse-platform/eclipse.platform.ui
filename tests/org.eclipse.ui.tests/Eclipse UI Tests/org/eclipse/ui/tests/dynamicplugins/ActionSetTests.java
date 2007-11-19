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
package org.eclipse.ui.tests.dynamicplugins;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.eclipse.core.runtime.IRegistryChangeListener;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.internal.PluginActionSet;
import org.eclipse.ui.internal.WWinPluginAction;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSet;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.tests.leaks.LeakTests;

/**
 * Tests to ensure the addition of new action sets with dynamic plug-ins.
 */

public class ActionSetTests extends DynamicTestCase implements
        IRegistryChangeListener {

    /**
     * 
     */
    private static final String ACTION_SET_ID = "org.eclipse.newActionSet1.newActionSet1";
    private static final String PART_ID = "org.eclipse.ui.tests.part1";

    public ActionSetTests(String testName) {
        super(testName);
    }

    public void testActionSets() throws Exception {
        WorkbenchWindow window = (WorkbenchWindow) openTestWindow();
        boolean [] found = new boolean[] {false};
        WWinPluginAction [] action = new WWinPluginAction[1];
        
        assertNull(window.getActionBars().getMenuManager().findUsingPath("menu1"));
        assertNull(getActionSetRegistry().findActionSet(ACTION_SET_ID));
        findInPresentation(window, action, found);
        assertFalse("Action set found", found[0]);
        assertNull("Action found", action[0]);
        
        action[0] = null;
        found[0] = false;
        getBundle();
        
        assertNotNull(window.getActionBars().getMenuManager().findUsingPath("menu1"));
        assertNotNull(getActionSetRegistry().findActionSet(ACTION_SET_ID));
        findInPresentation(window, action, found);
        assertTrue("Action set not found", found[0]);
        assertNotNull("Action not found", action[0]);
        
        ReferenceQueue queue = new ReferenceQueue();
        WeakReference ref = new WeakReference(action[0], queue);
        
        action[0] = null;
        found[0] = false;
        removeBundle();
        
        assertNull(window.getActionBars().getMenuManager().findUsingPath("menu1"));
        assertNull(getActionSetRegistry().findActionSet(ACTION_SET_ID));
        LeakTests.checkRef(queue, ref);
        findInPresentation(window, action, found);
        assertFalse("Action set found", found[0]);
        assertNull("Action found", action[0]);
        
    }

    private void findInPresentation(WorkbenchWindow window,
            WWinPluginAction[] action, boolean[] found) {
        IActionSet[] sets = window.getActionPresentation().getActionSets();

        for (int i = 0; i < sets.length; i++) {
            if (((PluginActionSet) sets[i]).getDesc().getId().equals(
                "org.eclipse.newActionSet1.newActionSet2")) {
                found[0] = true;
                IAction[] pluginActions = ((PluginActionSet) sets[i])
                    .getPluginActions();
                for (int j = 0; j < pluginActions.length; j++) {
                    if (pluginActions[j].getId().equals(
                        "org.eclipse.ui.tests.action1"))
                        action[0] = (WWinPluginAction) pluginActions[j];
                }
                break;
            }
        }
    }

    /**
     * @return
     */
    private ActionSetRegistry getActionSetRegistry() {
        return WorkbenchPlugin.getDefault().getActionSetRegistry();
    }

    public void testActionSetPartAssociations() {
        assertEquals(0, getActionSetRegistry().getActionSetsFor(PART_ID).length);
        getBundle();
        assertEquals(1, getActionSetRegistry().getActionSetsFor(PART_ID).length);
        removeBundle();
        assertEquals(0, getActionSetRegistry().getActionSetsFor(PART_ID).length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionId()
     */
    protected String getExtensionId() {
        return "newActionSet1.testDynamicActionSetAddition";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getExtensionPoint()
     */
    protected String getExtensionPoint() {
        return IWorkbenchRegistryConstants.PL_ACTION_SETS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.tests.dynamicplugins.DynamicTestCase#getInstallLocation()
     */
    protected String getInstallLocation() {
        return "data/org.eclipse.newActionSet1";
    }
}
