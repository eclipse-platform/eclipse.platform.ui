/*******************************************************************************
 * Copyright (c) 2009, 2019 IBM Corporation and others.
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
 *     Rolf Theunissen <rolf.theunissen@gmail.com> - Bug 546632
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import jakarta.inject.Inject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.tests.rules.WorkbenchContextExtension;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.renderers.swt.CTabRendering;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class MSaveablePartTest {

	@RegisterExtension
	public WorkbenchContextExtension contextRule = new WorkbenchContextExtension();

	@Inject
	private EModelService ems;

	@Inject
	private MApplication application;

	@Test
	public void testCreateView() {
		// This test verifies the textual '*' dirty prefix, which is only used
		// when the graphical dirty indicator is disabled.
		IEclipsePreferences prefs = InstanceScope.INSTANCE
				.getNode(CTabRendering.PREF_QUALIFIER_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT);
		boolean previous = prefs.getBoolean(CTabRendering.SHOW_DIRTY_INDICATOR_ON_TABS,
				CTabRendering.SHOW_DIRTY_INDICATOR_ON_TABS_DEFAULT);
		prefs.putBoolean(CTabRendering.SHOW_DIRTY_INDICATOR_ON_TABS, false);
		try {
			final MWindow window = createWindowWithOneView("Part Name");

			application.getChildren().add(window);
			contextRule.createAndRunWorkbench(window);

			MPartSashContainer container = (MPartSashContainer) window
					.getChildren().get(0);
			MPartStack stack = (MPartStack) container.getChildren().get(0);
			MPart part = (MPart) stack.getChildren().get(0);

			CTabFolder folder = (CTabFolder) stack.getWidget();
			CTabItem item = folder.getItem(0);
			assertEquals("Part Name", item.getText());

			assertFalse(part.isDirty());

			part.setDirty(true);
			assertEquals("*Part Name", item.getText());

			part.setDirty(false);
			assertEquals("Part Name", item.getText());
		} finally {
			prefs.putBoolean(CTabRendering.SHOW_DIRTY_INDICATOR_ON_TABS, previous);
		}
	}

	private MWindow createWindowWithOneView(String partName) {
		final MWindow window = ems.createModelElement(MWindow.class);
		window.setHeight(300);
		window.setWidth(400);
		window.setLabel("MyWindow");
		MPartSashContainer sash = ems.createModelElement(MPartSashContainer.class);
		window.getChildren().add(sash);
		MPartStack stack = ems.createModelElement(MPartStack.class);
		sash.getChildren().add(stack);
		MPart contributedPart = ems.createModelElement(MPart.class);
		stack.getChildren().add(contributedPart);
		contributedPart.setLabel(partName);
		contributedPart
				.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		return window;
	}

}
