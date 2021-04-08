/*******************************************************************************
 * Copyright (c) 2013, 2020 IBM Corporation and others.
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
 *     Rolf Theunissen <rolf.theunissen@gmail.com> - Bug 546632, 564561
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.CSSConstants;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.services.internal.events.EventBroker;
import org.eclipse.e4.ui.tests.rules.WorkbenchContextRule;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Widget;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class StackRendererTest {

	private static final String PART_DESC_ICON = "platform:/plugin/org.eclipse.e4.ui.tests/icons/pinned_ovr.png";
	private static final String PART_ICON = "platform:/plugin/org.eclipse.e4.ui.tests/icons/filenav_nav.png";

	@Rule
	public WorkbenchContextRule contextRule = new WorkbenchContextRule();

	@Inject
	private IEclipseContext context;

	@Inject
	private EModelService ems;

	@Inject
	private MApplication application;

	private MWindow window;
	private MPartStack partStack;

	@Before
	public void setUp() throws Exception {
		window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		partStack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack);
	}

	@Test
	public void testTabStateHandlerWhenOneOfSupportedTagChangeEvents() throws Exception {
		MPart part = ems.createModelElement(MPart.class);
		partStack.getChildren().add(part);
		part.setLabel("some title");

		CTabItemStylingMethodsListener executedMethodsListener = new CTabItemStylingMethodsListener(part);

		context.set(IStylingEngine.class, (IStylingEngine) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { IStylingEngine.class }, executedMethodsListener));

		contextRule.createAndRunWorkbench(window);

		// given
		HashMap<String, Object> params = new HashMap<>();
		params.put(UIEvents.EventTags.ELEMENT, part);
		params.put(UIEvents.EventTags.NEW_VALUE, CSSConstants.CSS_BUSY_CLASS);
		params.put(UIEvents.EventTags.OLD_VALUE, null);

		// when
		context.get(EventBroker.class).send(
				UIEvents.ApplicationElement.TOPIC_TAGS.replace(UIEvents.ALL_SUB_TOPICS, UIEvents.EventTypes.SET),
				params);

		// then
		assertEquals(1, executedMethodsListener.getMethodExecutionCount("setClassnameAndId(.+)"));
	}

	@Test
	public void testTabStateHandlerWhenSelectionChangedEvent() throws Exception {
		MPart part = ems.createModelElement(MPart.class);
		partStack.getChildren().add(part);
		part.setLabel("some title");

		CTabItemStylingMethodsListener executedMethodsListener = new CTabItemStylingMethodsListener(part);

		context.set(IStylingEngine.class, (IStylingEngine) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { IStylingEngine.class }, executedMethodsListener));

		contextRule.createAndRunWorkbench(window);

		// given
		MPlaceholder placeHolder = ems.createModelElement(MPlaceholder.class);
		placeHolder.setRef(part);

		HashMap<String, Object> params = new HashMap<>();
		params.put(UIEvents.EventTags.ELEMENT, partStack);
		params.put(UIEvents.EventTags.NEW_VALUE, placeHolder);
		params.put(UIEvents.EventTags.OLD_VALUE, null);

		// when
		context.get(EventBroker.class).send(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT
				.replace(UIEvents.ALL_SUB_TOPICS, UIEvents.EventTypes.SET), params);

		// then
		assertEquals(1, executedMethodsListener.getMethodExecutionCount("setClassnameAndId(.+)"));
	}

	private static class CTabItemStylingMethodsListener implements InvocationHandler {
		private MPart part;
		private List<String> methods;

		public CTabItemStylingMethodsListener(MPart part) {
			this.part = part;
			methods = new ArrayList<>();
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (isTabItemForPart(args[0])) {
				methods.add(String.format("%s(%s)", method.getName(), Arrays.toString(args)));
			}
			return null;
		}

		private boolean isTabItemForPart(Object obj) {
			return obj instanceof CTabItem && part.getLabel().equals(((CTabItem) obj).getText());
		}

		public int getMethodExecutionCount(String methodPattern) {
			int result = 0;
			for (String method : methods) {
				if (method.matches(methodPattern)) {
					result++;
				}
			}
			return result;
		}
	}

	@Test
	public void testBug475357_IconChanges() throws Exception {
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("myelementid");
		partDescriptor.setLabel("some title");
		partDescriptor.setIconURI(PART_DESC_ICON);
		application.getDescriptors().add(partDescriptor);

		MPart part1 = ems.createPart(partDescriptor);
		MPart part2 = ems.createPart(partDescriptor);

		partStack.getChildren().add(part1);
		partStack.getChildren().add(part2);

		contextRule.createAndRunWorkbench(window);

		part1.setIconURI(PART_DESC_ICON);
		CTabItem item = ((CTabFolder) partStack.getWidget()).getItem(0);
		Image image = item.getImage();

		part1.setIconURI(PART_ICON);
		assertNotEquals(item.getImage(), image);
	}

	@Test
	public void testBug475357_PartIconOverridesDescriptor() throws Exception {
		MPartDescriptor partDescriptor = ems.createModelElement(MPartDescriptor.class);
		partDescriptor.setElementId("myelementid");
		partDescriptor.setLabel("some title");
		partDescriptor.setIconURI(PART_DESC_ICON);
		application.getDescriptors().add(partDescriptor);

		MPart part1 = ems.createPart(partDescriptor);
		MPart part2 = ems.createPart(partDescriptor);

		partStack.getChildren().add(part1);
		partStack.getChildren().add(part2);

		contextRule.createAndRunWorkbench(window);

		// check that Renderer uses Part's icon over PartDescriptor's icon
		CTabItem item = ((CTabFolder) partStack.getWidget()).getItem(1);
		Image descImage = item.getImage();

		part2.setIconURI(PART_ICON);
		Image partIcon = item.getImage();
		assertNotEquals(partIcon, descImage);

		part2.setIconURI(null);
		Image ovrwriteIcon = item.getImage();
		assertNotEquals(ovrwriteIcon, partIcon);
		assertEquals(ovrwriteIcon, descImage);
	}

	@Test
	public void testBug564561_ToolbarVisible_initial() {
		MPart part1 = ems.createModelElement(MPart.class);
		MPart part2 = ems.createModelElement(MPart.class);

		partStack.getChildren().add(part1);
		partStack.getChildren().add(part2);
		partStack.setSelectedElement(part1);

		MToolBar toolbar1 = ems.createModelElement(MToolBar.class);
		toolbar1.setVisible(false);
		part1.setToolbar(toolbar1);

		MToolBar toolbar2 = ems.createModelElement(MToolBar.class);
		toolbar2.setVisible(true);
		part2.setToolbar(toolbar2);

		contextRule.createAndRunWorkbench(window);

		assertTrue(toolbar1.isVisible());
		assertFalse(toolbar2.isVisible());

		partStack.setSelectedElement(part2);

		assertFalse(toolbar1.isVisible());
		assertTrue(toolbar2.isVisible());
	}

	@Test
	public void testBug564561_ToolbarVisible_added1() {
		MPart part1 = ems.createModelElement(MPart.class);

		partStack.getChildren().add(part1);
		partStack.setSelectedElement(part1);

		contextRule.createAndRunWorkbench(window);

		MPart part2 = ems.createModelElement(MPart.class);
		MToolBar toolbar2 = ems.createModelElement(MToolBar.class);
		toolbar2.setVisible(true);
		part2.setToolbar(toolbar2);

		partStack.getChildren().add(part2);

		assertFalse(toolbar2.isVisible());
	}

	@Test
	public void testBug564561_ToolbarVisible_added2() {
		MPart part1 = ems.createModelElement(MPart.class);
		MPart part2 = ems.createModelElement(MPart.class);

		partStack.getChildren().add(part1);
		partStack.getChildren().add(part2);
		partStack.setSelectedElement(part1);

		contextRule.createAndRunWorkbench(window);

		MToolBar toolbar1 = ems.createModelElement(MToolBar.class);
		toolbar1.setVisible(false);
		part1.setToolbar(toolbar1);

		MToolBar toolbar2 = ems.createModelElement(MToolBar.class);
		toolbar2.setVisible(true);
		part2.setToolbar(toolbar2);

		assertTrue(toolbar1.isVisible());
		assertFalse(toolbar2.isVisible());
	}

	@Test
	public void testBug572598_SharedPartAndToolbarNotDisposed() {
		MPart part = ems.createModelElement(MPart.class);
		window.getSharedElements().add(part);

		MToolBar toolbar = ems.createModelElement(MToolBar.class);
		part.setToolbar(toolbar);

		MPlaceholder ph1 = ems.createModelElement(MPlaceholder.class);
		ph1.setRef(part);
		partStack.getChildren().add(ph1);
		partStack.setSelectedElement(ph1);

		MPartStack partStack2 = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack2);
		window.setSelectedElement(partStack2);

		MPlaceholder ph2 = ems.createModelElement(MPlaceholder.class);
		ph2.setRef(part);
		partStack2.getChildren().add(ph2);
		partStack2.setSelectedElement(ph2);

		contextRule.createAndRunWorkbench(window);

		assertNotNull(part.getWidget());
		assertFalse(((Widget) part.getWidget()).isDisposed());
		assertNotNull(toolbar.getWidget());
		assertFalse(((Widget) toolbar.getWidget()).isDisposed());

		// Destroy the second partstack; This should not dispose the shared elements
		partStack2.setToBeRendered(false);

		assertNotNull(part.getWidget());
		assertFalse(((Widget) part.getWidget()).isDisposed());
		assertNotNull(toolbar.getWidget());
		assertFalse(((Widget) toolbar.getWidget()).isDisposed());
	}

	// helper functions

	/*
	 * TODO tests: 1. switching tabs: are toolbars hidden, shown 2. add visible
	 * toolbar to hidden part, add invisible toolbar to shown part 3. shared part
	 * with visible toolbar on hidden part (do-render) (initial, and add tab after
	 * render)
	 */

}
