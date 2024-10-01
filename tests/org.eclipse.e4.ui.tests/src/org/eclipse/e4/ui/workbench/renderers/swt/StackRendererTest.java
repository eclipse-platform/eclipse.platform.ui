/*******************************************************************************
 * Copyright (c) 2013, 2023 IBM Corporation and others.
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
 *     Ole Osterhagen <ole@osterhagen.info> - Issue 230
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import jakarta.inject.Inject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.PartStackUtil;
import org.eclipse.e4.ui.internal.workbench.swt.CSSConstants;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
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
		private final MPart part;
		private final List<String> methods;

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
			return obj instanceof CTabItem item && part.getLabel().equals(item.getText());
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

	@Test
	public void testBug573518_SharedPartToolbarShown1() {
		MPart part1 = ems.createModelElement(MPart.class);
		window.getSharedElements().add(part1);

		MPart part2 = ems.createModelElement(MPart.class);
		window.getSharedElements().add(part2);

		MToolBar toolbar = ems.createModelElement(MToolBar.class);
		part2.setToolbar(toolbar);

		MPlaceholder ph1 = ems.createModelElement(MPlaceholder.class);
		ph1.setRef(part1);
		partStack.getChildren().add(ph1);
		partStack.setSelectedElement(ph1);

		MPlaceholder ph2 = ems.createModelElement(MPlaceholder.class);
		ph2.setRef(part2);
		partStack.getChildren().add(ph2);

		contextRule.createAndRunWorkbench(window);

		// Current reference is not pointing to ph2, toolbar is marked visible but not
		// rendereded
		assertTrue(toolbar.isVisible());
		assertNull(toolbar.getWidget());

		partStack.setSelectedElement(ph2);

		assertTrue(toolbar.isVisible());
		assertNotNull(toolbar.getWidget());
	}

	@Test
	public void testBug573518_SharedPartToolbarShown2() {
		MPart part1 = ems.createModelElement(MPart.class);
		window.getSharedElements().add(part1);

		MPart part2 = ems.createModelElement(MPart.class);
		window.getSharedElements().add(part2);

		MToolBar toolbar = ems.createModelElement(MToolBar.class);
		part2.setToolbar(toolbar);

		MPlaceholder ph1 = ems.createModelElement(MPlaceholder.class);
		ph1.setRef(part1);
		partStack.getChildren().add(ph1);
		partStack.setSelectedElement(ph1);

		MPlaceholder ph2 = ems.createModelElement(MPlaceholder.class);
		ph2.setRef(part2);
		part2.setCurSharedRef(ph2);
		partStack.getChildren().add(ph2);

		contextRule.createAndRunWorkbench(window);

		assertFalse(toolbar.isVisible());
		assertNull(toolbar.getWidget());

		partStack.setSelectedElement(ph2);

		assertTrue(toolbar.isVisible());
		assertNotNull(toolbar.getWidget());
	}

	@Test
	public void testOnboardingRenderedWithCorrectSizeForEditorStack() {
		PartStackUtil.makeEditorStack(partStack);

		contextRule.createAndRunWorkbench(window);

		Composite uiContainer = (Composite) ((StackRenderer) partStack.getRenderer()).getUIContainer(partStack);
		CTabFolder tabFolder = (CTabFolder) ((Composite) uiContainer.getChildren()[0]).getChildren()[0];
		assertNotNull(tabFolder.getChildren());
		assertEquals(3, tabFolder.getChildren().length);

		Composite outerOnboardingComposite = (Composite) tabFolder.getChildren()[2];
		Rectangle expected = new Rectangle(StackRenderer.ONBOARDING_SPACING, StackRenderer.ONBOARDING_TOP_SPACING,
				tabFolder.getSize().x - 2 * StackRenderer.ONBOARDING_SPACING,
				tabFolder.getSize().y - StackRenderer.ONBOARDING_TOP_SPACING - StackRenderer.ONBOARDING_SPACING);
		assertEquals(expected, outerOnboardingComposite.getBounds());
		Composite innerOnboardingComposite = (Composite) outerOnboardingComposite.getChildren()[0];
		assertEquals(2, innerOnboardingComposite.getChildren().length);
		assertNull(((Label) innerOnboardingComposite.getChildren()[0]).getImage());
		assertEquals("", ((Label) innerOnboardingComposite.getChildren()[1]).getText());
	}

	@Test
	public void testOnboardingNotRenderedForNonEditorStack() {
		partStack.getTags().add("OtherStack");
		contextRule.createAndRunWorkbench(window);

		Composite uiContainer = (Composite) ((StackRenderer) partStack.getRenderer()).getUIContainer(partStack);
		CTabFolder tabFolder = (CTabFolder) ((Composite) uiContainer.getChildren()[0]).getChildren()[0];
		assertNotNull(tabFolder.getChildren());
		assertEquals(2, tabFolder.getChildren().length);
	}

	@Test
	public void testOnboardingIsFilled() {
		MPerspective perspective = createPerspective();
		PartStackUtil.makeEditorStack(partStack);

		contextRule.createAndRunWorkbench(window);
		switchToPerspective(perspective);

		CTabFolder tabFolder = (CTabFolder) partStack.getWidget();
		assertFilledOnboardingInformation(tabFolder);
	}

	private MPerspective createPerspective() {
		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspective.getTags().add("persp.editorOnboardingText:Onboarding text");
		perspective.getTags().add("persp.editorOnboardingImageUri:" + PART_ICON);
		perspective.getTags().add("persp.editorOnboardingCommand:Find Actions$$$STRG+3");

		// "connect" the perspective with the application more or less like in
		// productive environment
		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		perspectiveStack.getChildren().add(perspective);
		window.getChildren().add(perspectiveStack);
		MPlaceholder placeholder = ems.createModelElement(MPlaceholder.class);
		placeholder.setRef(partStack);
		perspective.getChildren().add(placeholder);
		return perspective;
	}

	private void switchToPerspective(MPerspective perspective) {
		HashMap<String, Object> params = new HashMap<>();
		params.put(UIEvents.EventTags.ELEMENT, perspective);
		context.get(EventBroker.class).send(UIEvents.UILifeCycle.PERSPECTIVE_SWITCHED, params);
	}

	private void assertFilledOnboardingInformation(CTabFolder tabFolder) {
		assertNotNull(tabFolder.getChildren());
		Composite innerOnboardingComposite = null;
		for (Control child : tabFolder.getChildren()) {
			if (child instanceof Composite outerComposite) {
				if (outerComposite.getChildren().length > 0 && outerComposite.getChildren()[0] instanceof Composite innerComposite) {
					if (innerComposite.getChildren().length == 4) {
						innerOnboardingComposite = innerComposite;
						break;
					}
				}
			}
		}
		assertNotNull(innerOnboardingComposite);
		assertNotNull(((Label) innerOnboardingComposite.getChildren()[0]).getImage());
		assertEquals("Onboarding text", ((Label) innerOnboardingComposite.getChildren()[1]).getText());
		assertEquals("Find Actions", ((Label) innerOnboardingComposite.getChildren()[2]).getText());
		assertEquals("STRG+3", ((Label) innerOnboardingComposite.getChildren()[3]).getText());
	}

	@Test
	public void testOnboardingIsFilledForEveryEditorStack() {
		MPerspective perspective = createPerspective();
		PartStackUtil.makeEditorStack(partStack);

		contextRule.createAndRunWorkbench(window);

		// Create second editor stack
		MPartStack secondPartStack = ems.createModelElement(MPartStack.class);
		PartStackUtil.makeEditorStack(secondPartStack);
		MPlaceholder placeholder = ems.createModelElement(MPlaceholder.class);
		placeholder.setRef(secondPartStack);
		perspective.getChildren().add(placeholder);
		window.getChildren().add(secondPartStack);

		switchToPerspective(perspective);

		CTabFolder tabFolder = (CTabFolder) secondPartStack.getWidget();
		assertFilledOnboardingInformation(tabFolder);
	}

	@Test
	public void testOnboardingIsHiddenWhenEditorOpened() {
		PartStackUtil.makeEditorStack(partStack);

		contextRule.createAndRunWorkbench(window);

		Composite uiContainer = (Composite) ((StackRenderer) partStack.getRenderer()).getUIContainer(partStack);
		CTabFolder tabFolder = (CTabFolder) ((Composite) uiContainer.getChildren()[0]).getChildren()[0];
		assertNotNull(tabFolder.getChildren());
		assertEquals(3, tabFolder.getChildren().length);

		Composite outerOnboardingComposite = (Composite) tabFolder.getChildren()[2];
		Rectangle expected = new Rectangle(StackRenderer.ONBOARDING_SPACING, StackRenderer.ONBOARDING_TOP_SPACING,
				tabFolder.getSize().x - 2 * StackRenderer.ONBOARDING_SPACING,
				tabFolder.getSize().y - StackRenderer.ONBOARDING_TOP_SPACING - StackRenderer.ONBOARDING_SPACING);
		assertEquals(expected, outerOnboardingComposite.getBounds());

		MPart part1 = ems.createModelElement(MPart.class);
		partStack.getChildren().add(part1);
		partStack.setSelectedElement(part1);

		tabFolder.notifyListeners(SWT.Paint, new Event());

		expected = new Rectangle(StackRenderer.ONBOARDING_SPACING, StackRenderer.ONBOARDING_TOP_SPACING, 0, 0);
		assertEquals(expected, outerOnboardingComposite.getBounds());
	}

	/**
	 * https://github.com/eclipse-platform/eclipse.platform.ui/issues/230
	 */
	@Test
	public void testToolbarIsReparentedToNewCompositeForTopRightOfTabFolder() {
		MPartStack partStack2 = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack2);

		MPart part = ems.createModelElement(MPart.class);
		part.setToolbar(ems.createModelElement(MToolBar.class));

		MPlaceholder placeHolder1 = ems.createModelElement(MPlaceholder.class);
		placeHolder1.setRef(part);
		partStack.getChildren().add(placeHolder1);

		MPlaceholder placeHolder2 = ems.createModelElement(MPlaceholder.class);
		placeHolder2.setRef(part);
		partStack2.getChildren().add(placeHolder2);

		// Ensure that the placeholder for the part is selected. Otherwise it would be
		// selected by the framework which triggers a selection event. This event would
		// re-parent the toolbar anyway.
		partStack2.setSelectedElement(placeHolder2);

		contextRule.createAndRunWorkbench(window);

		CTabFolder tabFolder = (CTabFolder) partStack2.getWidget();
		Control toolbarControl = (Control) part.getToolbar().getWidget();
		assertSame(tabFolder.getTopRight(), toolbarControl.getParent());
	}

	// helper functions

	/*
	 * TODO tests: 1. switching tabs: are toolbars hidden, shown 2. add visible
	 * toolbar to hidden part, add invisible toolbar to shown part 3. shared part
	 * with visible toolbar on hidden part (do-render) (initial, and add tab after
	 * render)
	 */

}
