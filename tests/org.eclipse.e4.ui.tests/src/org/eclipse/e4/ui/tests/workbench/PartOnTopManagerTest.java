/*******************************************************************************
 * Copyright (c) 2016, 2019 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 *     Rolf Theunissen <rolf.theunissen@gmail.com> - Bug 546632
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.inject.Inject;
import org.eclipse.e4.ui.internal.workbench.PartOnTopManager;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.tests.rules.WorkbenchContextRule;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.junit.Rule;
import org.junit.Test;

/**
 * This test class is used to validate the correctness of the
 * {@link PartOnTopManager}.
 */
public class PartOnTopManagerTest {

	@Rule
	public WorkbenchContextRule contextRule = new WorkbenchContextRule();

	@Inject
	private EModelService ems;

	@Inject
	private MApplication application;

	private boolean isPartOnTop(MContext context) {
		if (context.getContext() != null) {
			Object object = context.getContext().get(IWorkbench.ON_TOP);
			return Boolean.TRUE.equals(object);
		}

		return false;
	}

	@Test
	public void test_PartOnTop() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = ems.createModelElement(MPart.class);
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getChildren().add(part);
		window.setSelectedElement(part);

		contextRule.createAndRunWorkbench(window);

		assertTrue(isPartOnTop(part));
	}

	@Test
	public void test_PlaceholderOnTop() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart part = ems.createModelElement(MPart.class);
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getSharedElements().add(part);

		MPlaceholder placeholder = ems.createModelElement(MPlaceholder.class);
		placeholder.setRef(part);

		perspective.getChildren().add(placeholder);
		perspective.setSelectedElement(placeholder);

		contextRule.createAndRunWorkbench(window);

		assertTrue(isPartOnTop(part));
	}

	@Test
	public void test_PartOnTopStackSwitch() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack);
		window.setSelectedElement(partStack);

		MPart part = ems.createModelElement(MPart.class);
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(part);
		partStack.setSelectedElement(part);
		MPart secondPart = ems.createModelElement(MPart.class);
		secondPart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(secondPart);

		contextRule.createAndRunWorkbench(window);

		assertTrue(isPartOnTop(part));
		assertFalse(isPartOnTop(secondPart));

		partStack.setSelectedElement(secondPart);

		assertFalse(isPartOnTop(part));
		assertTrue(isPartOnTop(secondPart));
	}

	@Test
	public void test_PlaceholderOnTopStackSwitch() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		MPart part = ems.createModelElement(MPart.class);
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getSharedElements().add(part);

		MPlaceholder placeholder = ems.createModelElement(MPlaceholder.class);
		placeholder.setRef(part);

		partStack.getChildren().add(placeholder);
		partStack.setSelectedElement(placeholder);
		MPart secondPart = ems.createModelElement(MPart.class);
		secondPart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(secondPart);

		contextRule.createAndRunWorkbench(window);

		assertTrue(isPartOnTop(part));
		assertFalse(isPartOnTop(secondPart));

		partStack.setSelectedElement(secondPart);

		assertFalse(isPartOnTop(part));
		assertTrue(isPartOnTop(secondPart));

		partStack.setSelectedElement(placeholder);

		assertTrue(isPartOnTop(part));
		assertFalse(isPartOnTop(secondPart));

	}

	@Test
	public void test_PartOnTopPerspectiveSwitch() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPerspective secondPerspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(secondPerspective);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		MPart part = ems.createModelElement(MPart.class);
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(part);
		partStack.setSelectedElement(part);
		MPart secondPart = ems.createModelElement(MPart.class);
		secondPart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(secondPart);

		MPart secondPerspectivePart = ems.createModelElement(MPart.class);
		secondPerspectivePart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		secondPerspective.getChildren().add(secondPerspectivePart);

		contextRule.createAndRunWorkbench(window);

		assertTrue(isPartOnTop(part));
		assertFalse(isPartOnTop(secondPart));

		EPartService partService = window.getContext().get(EPartService.class);
		partService.switchPerspective(secondPerspective);

		assertFalse(isPartOnTop(part));
		assertFalse(isPartOnTop(secondPart));

		assertTrue(isPartOnTop(secondPerspectivePart));
	}

	@Test
	public void test_PlaceholderOnTopPerspectiveSwitch() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart sharedPart = ems.createModelElement(MPart.class);
		sharedPart.setElementId("sharedPart");
		sharedPart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getSharedElements().add(sharedPart);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);

		MPerspective firstPerspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(firstPerspective);
		perspectiveStack.setSelectedElement(firstPerspective);

		MPlaceholder firstPerspectivePlaceholder = ems.createModelElement(MPlaceholder.class);
		firstPerspectivePlaceholder.setElementId("firstPerspectivePlaceholder");
		firstPerspectivePlaceholder.setRef(sharedPart);

		firstPerspective.getChildren().add(firstPerspectivePlaceholder);

		MPerspective secondPerspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(secondPerspective);

		MPartSashContainer secondPerspectiveSash = ems.createModelElement(MPartSashContainer.class);

		secondPerspective.getChildren().add(secondPerspectiveSash);
		secondPerspective.setSelectedElement(secondPerspectiveSash);

		MPartStack secondPerspectivePartStack = ems.createModelElement(MPartStack.class);
		secondPerspectiveSash.getChildren().add(secondPerspectivePartStack);
		secondPerspectiveSash.setSelectedElement(secondPerspectivePartStack);

		MPlaceholder secondPerspectivePartStackPlaceholder = ems.createModelElement(MPlaceholder.class);
		secondPerspectivePartStackPlaceholder.setElementId("secondPerspectivePartStackPlaceholder");
		secondPerspectivePartStackPlaceholder.setRef(sharedPart);

		secondPerspectivePartStack.getChildren().add(secondPerspectivePartStackPlaceholder);

		MPart secondPart = ems.createModelElement(MPart.class);
		secondPart.setElementId("secondPart");
		secondPart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		secondPerspectivePartStack.getChildren().add(secondPart);
		secondPerspectivePartStack.setSelectedElement(secondPart);

		MPart secondPerspectiveOnTopPart = ems.createModelElement(MPart.class);
		secondPerspectiveOnTopPart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		secondPerspectiveSash.getChildren().add(secondPerspectiveOnTopPart);

		contextRule.createAndRunWorkbench(window);

		assertTrue(isPartOnTop(sharedPart));
		assertFalse(isPartOnTop(secondPart));
		assertFalse(isPartOnTop(secondPerspectiveOnTopPart));

		EPartService partService = window.getContext().get(EPartService.class);
		partService.switchPerspective(secondPerspective);

		// TODO this might fail once
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=490780 is fixed
		assertTrue(isPartOnTop(sharedPart));
		assertTrue(isPartOnTop(secondPerspectiveOnTopPart));
		assertFalse(isPartOnTop(secondPart));

		secondPerspectivePartStack.setSelectedElement(secondPerspectivePartStackPlaceholder);

		assertTrue(isPartOnTop(sharedPart));
		assertFalse(isPartOnTop(secondPart));
		assertTrue(isPartOnTop(secondPerspectiveOnTopPart));

		partService.switchPerspective(firstPerspective);

		assertTrue(isPartOnTop(sharedPart));
		assertFalse(isPartOnTop(secondPart));
		assertFalse(isPartOnTop(secondPerspectiveOnTopPart));
	}
}
