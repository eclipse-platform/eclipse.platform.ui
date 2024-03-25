/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import jakarta.inject.Inject;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.basic.MCompositePart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.tests.rules.WorkbenchContextRule;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.junit.Rule;
import org.junit.Test;

public class CompositePartClosingTest {

	@Rule
	public WorkbenchContextRule contextRule = new WorkbenchContextRule();

	@Inject
	private EModelService ems;

	@Inject
	private MApplication application;

	@Inject
	private EPartService partService;

	@Test
	public void test_partClosing() {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MArea rootArea = ems.createModelElement(MArea.class);
		window.getChildren().add(rootArea);
		window.setSelectedElement(rootArea);

		MPartStack partStack1 = ems.createModelElement(MPartStack.class);
		rootArea.getChildren().add(partStack1);
		MPartStack partStack2 = ems.createModelElement(MPartStack.class);
		rootArea.getChildren().add(partStack2);

		MPart part1 = createPart();
		MCompositePart compositePart = createCompositePart();
		MPart part2 = createPart();

		partStack1.getChildren().add(compositePart);
		partStack1.getChildren().add(part1);
		partStack1.setSelectedElement(part1);

		partStack2.getChildren().add(part2);
		partStack2.setSelectedElement(part2);

		contextRule.createAndRunWorkbench(window);

		partService.activate(part2);
		partService.hidePart(part2);

		assertNotEquals("Composite part got activated", compositePart, partStack1.getSelectedElement());
		assertEquals("Wrong part got activated", part1, partStack1.getSelectedElement());

	}

	private MCompositePart createCompositePart() {

		MCompositePart compositePart = ems.createModelElement(MCompositePart.class);
		MPartSashContainer partSashContainer = ems.createModelElement(MPartSashContainer.class);
		MPartStack partStack = ems.createModelElement(MPartStack.class);

		compositePart.getChildren().add(partSashContainer);
		partSashContainer.getChildren().add(partStack);
		partStack.getChildren().add(createPart());
		partStack.getChildren().add(createPart());

		return compositePart;
	}

	private MPart createPart() {
		MPart part = ems.createModelElement(MPart.class);
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		return part;
	}

}
