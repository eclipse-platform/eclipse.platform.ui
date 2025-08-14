/*******************************************************************************
 * Copyright (c) 2009, 2016 Remy Chi Jian Suen and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.internal;

import static org.eclipse.ui.PlatformUI.getWorkbench;

import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.tests.harness.util.PreferenceMementoRule;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PerspectiveSwitcherTest extends UITestCase {

	@Rule
	public final PreferenceMementoRule preferenceMemento = new PreferenceMementoRule();

	public PerspectiveSwitcherTest() {
		super(PerspectiveSwitcherTest.class.getSimpleName());
	}

	/**
	 * This test ensures that our workbench window's perspective bar can opened if
	 * the 'Open Perspective' contribution item is not there.
	 */
	@Test
	public void testCreatePerspectiveSwithcerInToolbar() {
		IPreferenceStore apiPreferenceStore = PrefUtil.getAPIPreferenceStore();

		WorkbenchWindow window = (WorkbenchWindow) getWorkbench().getActiveWorkbenchWindow();
		assertNotNull("We should have a perspective bar in the beginning", getPerspectiveSwitcher(window)); //$NON-NLS-1$

		// turn off the 'Open Perspective' item
		preferenceMemento.setPreference(apiPreferenceStore, IWorkbenchPreferenceConstants.SHOW_OPEN_ON_PERSPECTIVE_BAR,
				false);

		// check that we still have a perspective bar
		assertNotNull("The perspective bar should have been created successfully", getPerspectiveSwitcher(window)); //$NON-NLS-1$

	}

	private static Object getPerspectiveSwitcher(WorkbenchWindow window) {
		EModelService modelService = window.getService(EModelService.class);
		return modelService.find("PerspectiveSwitcher", window.getModel());
	}
}
