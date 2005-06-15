/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.PluginActionSet;
import org.eclipse.ui.internal.WWinPluginAction;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSet;

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
        assertNull(getActionSetRegistry().findActionSet(ACTION_SET_ID));
        getBundle();
        WorkbenchWindow window = (WorkbenchWindow) openTestWindow();
        assertNotNull(getActionSetRegistry().findActionSet(ACTION_SET_ID));
        IActionSet [] sets = window.getActionPresentation().getActionSets();
        boolean found = false;
        WWinPluginAction action = null;
        for (int i = 0; i < sets.length; i++) {
            if (((PluginActionSet)sets[i]).getDesc().getId().equals("org.eclipse.newActionSet1.newActionSet2")) {
                found = true;
                IAction [] pluginActions = ((PluginActionSet)sets[i]).getPluginActions();
                for (int j = 0; j < pluginActions.length; j++) {
                    if (pluginActions[j].getId().equals("org.eclipse.ui.tests.action1"))
                        action = (WWinPluginAction) pluginActions[j];
                }
                break;
            }
        }
        assertTrue("Action set not found", found);
        assertNotNull("Action not found", action);
//        ReferenceQueue queue = new ReferenceQueue();
//        WeakReference ref = new WeakReference(action, queue);
//        action = null;
        removeBundle();
        
        assertNull(window.getActionBars().getMenuManager().findMenuUsingPath("menu1"));
//        LeakTests.checkRef(queue, ref);
        
        assertNull(getActionSetRegistry().findActionSet(ACTION_SET_ID));
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
        return IWorkbenchConstants.PL_ACTION_SETS;
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
