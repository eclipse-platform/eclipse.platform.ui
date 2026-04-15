/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.tests.rules.WorkbenchContextExtension;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ToolControlRendererTest {

	private static final String BUNDLE_URI = "bundleclass://org.eclipse.e4.ui.tests/"
			+ ToolControlRendererTest.class.getName() + "$";

	@RegisterExtension
	public WorkbenchContextExtension contextRule = new WorkbenchContextExtension();

	@Inject
	private EModelService ems;

	@Inject
	private MApplication application;

	private MTrimmedWindow window;
	private MTrimBar trimBar;

	@BeforeEach
	void setUp() {
		window = ems.createModelElement(MTrimmedWindow.class);
		application.getChildren().add(window);

		trimBar = ems.createModelElement(MTrimBar.class);
		window.getTrimBars().add(trimBar);
	}

	@Test
	void testWidgetCreated() {
		MToolControl toolControl = createToolControl("SimpleControl");
		trimBar.getChildren().add(toolControl);

		contextRule.createAndRunWorkbench(window);

		assertNotNull(toolControl.getWidget(), "Widget must be created for a MToolControl");
	}

	@Test
	void testNoChildren_widgetIsNull() {
		MToolControl toolControl = createToolControl("EmptyControl");
		trimBar.getChildren().add(toolControl);

		contextRule.createAndRunWorkbench(window);

		assertNull(toolControl.getWidget(),
				"Widget must be null when the contribution creates no SWT children");
	}

	@Test
	void testHiddenExplicitly_hidesControl() {
		MToolControl toolControl = createToolControl("SimpleControl");
		trimBar.getChildren().add(toolControl);

		contextRule.createAndRunWorkbench(window);

		assertTrue(toolControl.isVisible(), "Control should be visible initially");

		toolControl.getTags().add(IPresentationEngine.HIDDEN_EXPLICITLY);
		contextRule.spinEventLoop();

		assertFalse(toolControl.isVisible(), "Control should be hidden after HIDDEN_EXPLICITLY tag is added");
	}

	@Test
	void testHiddenExplicitly_removeTag_restoresVisibility() {
		MToolControl toolControl = createToolControl("SimpleControl");
		trimBar.getChildren().add(toolControl);

		contextRule.createAndRunWorkbench(window);

		toolControl.getTags().add(IPresentationEngine.HIDDEN_EXPLICITLY);
		contextRule.spinEventLoop();
		assertFalse(toolControl.isVisible());

		toolControl.getTags().remove(IPresentationEngine.HIDDEN_EXPLICITLY);
		contextRule.spinEventLoop();

		assertTrue(toolControl.isVisible(), "Visibility must be restored after HIDDEN_EXPLICITLY tag is removed");
	}

	@Test
	void testHideableControls_haveIndependentMenus() {
		MToolControl tc1 = createToolControl("SimpleControl");
		tc1.getTags().add("HIDEABLE");
		trimBar.getChildren().add(tc1);

		MToolControl tc2 = createToolControl("SimpleControl");
		tc2.getTags().add("HIDEABLE");
		trimBar.getChildren().add(tc2);

		contextRule.createAndRunWorkbench(window);

		Control widget1 = (Control) tc1.getWidget();
		Control widget2 = (Control) tc2.getWidget();

		assertNotNull(widget1.getMenu(), "First HIDEABLE control must have a context menu");
		assertNotNull(widget2.getMenu(), "Second HIDEABLE control must have a context menu");
		assertNotSame(widget1.getMenu(), widget2.getMenu(),
				"Each HIDEABLE control must have its own independent menu instance");
		assertFalse(widget1.getMenu().isDisposed(),
				"First control's menu must not be disposed after second control is rendered");
	}

	private MToolControl createToolControl(String innerClassName) {
		MToolControl tc = ems.createModelElement(MToolControl.class);
		tc.setContributionURI(BUNDLE_URI + innerClassName);
		return tc;
	}

	// --- Contribution classes used by the tests ---

	public static class SimpleControl {
		@PostConstruct
		public void create(Composite parent) {
			new Label(parent, SWT.NONE).setText("test");
		}
	}

	public static class EmptyControl {
		@PostConstruct
		public void create(@SuppressWarnings("unused") Composite parent) {
			// intentionally creates no SWT children
		}
	}
}
