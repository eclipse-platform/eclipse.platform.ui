/*******************************************************************************
 * Copyright (c) 2024 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 				Raghunandana Murthappa
 *******************************************************************************/

package org.eclipse.ui.tests.quickaccess;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.quickaccess.providers.ActionProvider;
import org.eclipse.ui.internal.quickaccess.providers.CommandProvider;
import org.eclipse.ui.quickaccess.QuickAccessElement;
import org.junit.Test;

/**
 * Tests the quick access providers.
 */
public class QuickAccessProvidersTest {
	private static final String ACTIVITY_ID = "org.eclipse.ui.tests.activitySupportTest.issue1832";
	private final String COMMAND_ID = "org.eclipse.ui.tests.activitySupportTest.commands.issue1832";
	private static final String ACTION_LABEL = "Create Test Markers";

	@Test
	public void testCommandProvider() {
		IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
		CommandProvider cmdProvider = new CommandProvider();
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		WorkbenchWindow workbenchWindow = (WorkbenchWindow) window;
		final MWindow model = workbenchWindow.getModel();
		cmdProvider.setContext(model.getContext().getActiveLeaf());

		QuickAccessElement[] elementsBefore = cmdProvider.getElements();
		assertCommandAbsent(elementsBefore);

		// enable the test activity id
		Set<String> enabledActivityIds = workbenchActivitySupport.getActivityManager().getEnabledActivityIds();
		Set<String> toBeEnabled = new HashSet<>();
		toBeEnabled.add(ACTIVITY_ID);
		toBeEnabled.addAll(enabledActivityIds);
		workbenchActivitySupport.setEnabledActivityIds(toBeEnabled);

		CommandProvider cmdProvider1 = new CommandProvider();
		cmdProvider1.setContext(model.getContext().getActiveLeaf());
		QuickAccessElement[] elementsAfter = cmdProvider1.getElements();
		assertCommandPresent(elementsAfter);

		// restore to previous
		workbenchActivitySupport.setEnabledActivityIds(enabledActivityIds);
	}

	@Test
	public void testActionProvider() {
		IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
		ActionProvider actionProvider = new ActionProvider();
		QuickAccessElement[] actionElements = actionProvider.getElements();
		assertActionAbsent(actionElements);

		// enable the test activity id
		Set<String> toBeEnabled = new HashSet<>();
		toBeEnabled.add(ACTIVITY_ID);
		toBeEnabled.addAll(workbenchActivitySupport.getActivityManager().getEnabledActivityIds());
		workbenchActivitySupport.setEnabledActivityIds(toBeEnabled);

		ActionProvider actionProvider1 = new ActionProvider();
		QuickAccessElement[] actionElements1 = actionProvider1.getElements();
		assertActionPresent(actionElements1);
	}

	private void assertActionAbsent(QuickAccessElement[] actionElements) {
		boolean present = false;
		for (QuickAccessElement quickAccessElement : actionElements) {
			if (ACTION_LABEL.equals(quickAccessElement.getLabel())) {
				present = true;
				break;
			}
		}
		assertFalse("Action present", present);

	}

	private void assertActionPresent(QuickAccessElement[] actionElements) {
		boolean present = false;
		for (QuickAccessElement quickAccessElement : actionElements) {
			if (quickAccessElement.getLabel().equals(ACTION_LABEL)) {
				present = true;
				break;
			}
		}
		assertTrue("Action absent", present);

	}

	private void assertCommandAbsent(QuickAccessElement[] elementsBefore) {
		boolean present = false;
		for (QuickAccessElement quickAccessElement : elementsBefore) {
			if (quickAccessElement.getId().equals(COMMAND_ID)) {
				present = true;
				break;
			}
		}
		assertFalse("command present", present);
	}

	private void assertCommandPresent(QuickAccessElement[] elementsAfter) {
		boolean present = false;
		for (QuickAccessElement quickAccessElement : elementsAfter) {
			if (quickAccessElement.getId().equals(COMMAND_ID)) {
				present = true;
				break;
			}
		}
		assertTrue("command absent", present);
	}
}
