/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.dynamicplugins;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;

import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.renderers.swt.ContributionRecord;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.internal.WWinPluginAction;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.tests.leaks.LeakTests;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests to ensure the addition of new action sets with dynamic plug-ins.
 */

@RunWith(JUnit4.class)
public class ActionSetTests extends DynamicTestCase {

	private static final String ACTION_SET_ID = "org.eclipse.newActionSet1.newActionSet1";
	private static final String PART_ID = "org.eclipse.ui.tests.part1";

	public ActionSetTests() {
		super(ActionSetTests.class.getSimpleName());
	}

	@Test
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

		ReferenceQueue<WWinPluginAction> queue = new ReferenceQueue<>();
		WeakReference<WWinPluginAction> ref = new WeakReference<>(action[0], queue);

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
		IRendererFactory factory = window.getService(IRendererFactory.class);
		MWindow mwindow = window.getModel();
		AbstractPartRenderer obj = factory.getRenderer(mwindow.getMainMenu(), null);
		if (!(obj instanceof MenuManagerRenderer menuManagerRenderer)) {
			return;
		}

		ContributionRecord[] records = menuManagerRenderer.getContributionRecords();
		for (ContributionRecord rec : records) {
			String id = rec.getMenuContribution().getElementId();
			if (id != null && id.startsWith("org.eclipse.newActionSet1.newActionSet2")) {
				found[0] = true;
				Collection<MMenuElement> elements = rec.getGeneratedElements();
				for (MMenuElement element : elements) {
					if ("org.eclipse.ui.tests.action1".equals(element.getElementId())) {
						// FIXME return the plugin action
						action[0] = new WWinPluginAction(null, window, element.getElementId(), IAction.AS_PUSH_BUTTON);
					}
				}
			}
		}
//		for (int i = 0; i < sets.length; i++) {
//			if (((PluginActionSet) sets[i]).getDesc().getId()
//					.equals("org.eclipse.newActionSet1.newActionSet2")) {
//				found[0] = true;
//				IAction[] pluginActions = ((PluginActionSet) sets[i])
//						.getPluginActions();
//				for (int j = 0; j < pluginActions.length; j++) {
//					if (pluginActions[j].getId().equals(
//							"org.eclipse.ui.tests.action1"))
//						action[0] = (WWinPluginAction) pluginActions[j];
//				}
//				break;
//			}
//		}
	}

	private ActionSetRegistry getActionSetRegistry() {
		return WorkbenchPlugin.getDefault().getActionSetRegistry();
	}

	@Test
	public void testActionSetPartAssociations() {
		assertEquals(0, getActionSetRegistry().getActionSetsFor(PART_ID).length);
		getBundle();
		assertEquals(1, getActionSetRegistry().getActionSetsFor(PART_ID).length);
		removeBundle();
		assertEquals(0, getActionSetRegistry().getActionSetsFor(PART_ID).length);
	}

	@Override
	protected String getExtensionId() {
		return "newActionSet1.testDynamicActionSetAddition";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_ACTION_SETS;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newActionSet1";
	}
}
