/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.reconciler;

import java.util.Collection;
import java.util.List;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MPerspective;
import org.eclipse.e4.ui.model.application.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.model.application.MWindowTrim;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerElementContainerTest extends
		ModelReconcilerTest {

	public void testElementContainer_Children_Add() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setLabel("newPart");
		window.getChildren().add(part);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, window.getChildren().size());

		applyAll(deltas);

		assertEquals(1, window.getChildren().size());

		part = (MPart) window.getChildren().get(0);
		assertEquals("newPart", part.getLabel());
	}

	public void testElementContainer_Children_Add2() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setLabel("newPart");
		window.getChildren().add(part);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);
		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, window.getChildren().size());
		assertEquals(part, window.getChildren().get(0));

		part = (MPart) window.getChildren().get(0);

		applyAll(deltas);

		assertEquals(2, window.getChildren().size());
		assertEquals(part, window.getChildren().get(0));

		part = (MPart) window.getChildren().get(1);
		assertEquals("newPart", part.getLabel());
	}

	public void testElementContainer_Children_Add3() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();

		window.getChildren().add(stack);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setLabel("newPart");
		stack.getChildren().add(part);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack = (MPartStack) window.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, stack.getChildren().size());

		applyAll(deltas);

		assertEquals(1, stack.getChildren().size());

		part = stack.getChildren().get(0);
		assertEquals("newPart", part.getLabel());
	}

	public void testElementContainer_Children_Add4() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPart editor = MApplicationFactory.eINSTANCE.createPart();
		editor.setLabel("newEditor");
		window.getChildren().add(editor);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, window.getChildren().size());

		applyAll(deltas);

		assertEquals(1, window.getChildren().size());

		editor = (MPart) window.getChildren().get(0);
		assertEquals("newEditor", editor.getLabel());
	}

	public void testElementContainer_Children_Add5() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPerspectiveStack stack = MApplicationFactory.eINSTANCE
				.createPerspectiveStack();

		MPerspective perspective = MApplicationFactory.eINSTANCE
				.createPerspective();
		perspective.setLabel("newEditor");
		stack.getChildren().add(perspective);
		window.getChildren().add(stack);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, window.getChildren().size());

		applyAll(deltas);

		assertEquals(1, window.getChildren().size());

		stack = (MPerspectiveStack) window.getChildren().get(0);
		assertEquals(1, stack.getChildren().size());

		perspective = stack.getChildren().get(0);
		assertEquals("newEditor", perspective.getLabel());
	}

	public void testElementContainer_Children_Add6() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPerspectiveStack perspectiveStack = MApplicationFactory.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, window.getChildren().size());

		applyAll(deltas);

		assertEquals(1, window.getChildren().size());

		assertTrue(window.getChildren().get(0) instanceof MPerspectiveStack);
	}

	public void testElementContainer_Children_Add7() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPartDescriptor part = MApplicationFactory.eINSTANCE
				.createPartDescriptor();
		window.getChildren().add(part);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, window.getChildren().size());

		applyAll(deltas);

		assertEquals(1, window.getChildren().size());

		assertTrue(window.getChildren().get(0) instanceof MPartDescriptor);
	}

	public void testElementContainer_Children_Remove() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, window.getChildren().size());

		assertEquals(part, window.getChildren().get(0));

		applyAll(deltas);

		assertEquals(0, window.getChildren().size());
	}

	public void testElementContainer_Children_Remove2() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(part1);

		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(part2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		part1 = (MPart) window.getChildren().get(0);
		part2 = (MPart) window.getChildren().get(1);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(2, window.getChildren().size());

		assertEquals(part1, window.getChildren().get(0));
		assertEquals(part2, window.getChildren().get(1));

		part2 = (MPart) window.getChildren().get(1);

		applyAll(deltas);

		assertEquals(1, window.getChildren().size());
		assertEquals(part2, window.getChildren().get(0));
	}

	public void testElementContainer_Children_Remove3() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		stack.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack = (MPartStack) window.getChildren().get(0);
		part = stack.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, stack.getChildren().size());

		assertEquals(part, stack.getChildren().get(0));

		applyAll(deltas);

		assertEquals(0, stack.getChildren().size());
	}

	public void testElementContainer_Children_Remove4() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		stack.getChildren().add(part1);

		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		stack.getChildren().add(part2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack = (MPartStack) window.getChildren().get(0);
		part1 = stack.getChildren().get(0);
		part2 = stack.getChildren().get(1);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(2, stack.getChildren().size());
		assertEquals(part1, stack.getChildren().get(0));
		assertEquals(part2, stack.getChildren().get(1));

		applyAll(deltas);

		assertEquals(1, stack.getChildren().size());
		assertEquals(part2, stack.getChildren().get(0));
	}

	public void testElementContainer_Children_MovedFromOneStackToAnother() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPartStack stack1 = MApplicationFactory.eINSTANCE.createPartStack();
		MPartStack stack2 = MApplicationFactory.eINSTANCE.createPartStack();

		window.getChildren().add(stack1);
		window.getChildren().add(stack2);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		stack1.getChildren().add(part1);

		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		stack2.getChildren().add(part2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack1.getChildren().remove(part1);
		stack2.getChildren().add(part1);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		stack1 = (MPartStack) window.getChildren().get(0);
		stack2 = (MPartStack) window.getChildren().get(1);

		part1 = stack1.getChildren().get(0);
		part2 = stack2.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, stack1.getChildren().size());
		assertEquals(part1, stack1.getChildren().get(0));
		assertEquals(1, stack2.getChildren().size());
		assertEquals(part2, stack2.getChildren().get(0));

		applyAll(deltas);

		assertEquals(0, stack1.getChildren().size());

		List<MPart> stack2Children = stack2.getChildren();
		assertEquals(2, stack2Children.size());
		assertTrue(stack2Children.contains(part1));
		assertTrue(stack2Children.contains(part2));
	}

	public void testElementContainer_Children_Repositioned() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		stack.getChildren().add(part1);

		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		stack.getChildren().add(part2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack.getChildren().move(0, 1);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		stack = (MPartStack) window.getChildren().get(0);

		part1 = stack.getChildren().get(0);
		part2 = stack.getChildren().get(1);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(2, stack.getChildren().size());
		assertEquals(part1, stack.getChildren().get(0));
		assertEquals(part2, stack.getChildren().get(1));

		applyAll(deltas);

		assertEquals(2, stack.getChildren().size());
		assertEquals(part2, stack.getChildren().get(0));
		assertEquals(part1, stack.getChildren().get(1));
	}

	public void testElementContainer_Children_Add_Multiple() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPartSashContainer partSashContainer = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(partSashContainer);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		partSashContainer.getChildren().add(part);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(0, window.getChildren().size());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getChildren().size());
		assertTrue(window.getChildren().get(0) instanceof MPartSashContainer);

		partSashContainer = (MPartSashContainer) window.getChildren().get(0);

		assertEquals(1, partSashContainer.getChildren().size());
		assertTrue(partSashContainer.getChildren().get(0) instanceof MPart);
	}

	public void testElementContainer_Children_Add_PartSashContainer() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		MPartSashContainer partSashContainer1 = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(partSashContainer1);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		partSashContainer1.getChildren().add(part1);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPartSashContainer partSashContainer2 = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(partSashContainer2);

		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		partSashContainer2.getChildren().add(part2);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		partSashContainer1 = (MPartSashContainer) window.getChildren().get(0);
		part1 = (MPart) partSashContainer1.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getChildren().size());
		assertEquals(partSashContainer1, window.getChildren().get(0));

		assertEquals(1, partSashContainer1.getChildren().size());
		assertEquals(part1, partSashContainer1.getChildren().get(0));

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(2, window.getChildren().size());
		assertEquals(partSashContainer1, window.getChildren().get(0));

		partSashContainer2 = (MPartSashContainer) window.getChildren().get(1);

		assertEquals(1, partSashContainer1.getChildren().size());
		assertEquals(part1, partSashContainer1.getChildren().get(0));

		assertEquals(1, partSashContainer2.getChildren().size());
		assertNotNull(partSashContainer2.getChildren().get(0));
		assertTrue(partSashContainer2.getChildren().get(0) instanceof MPart);
	}

	public void testElementContainer_Children_Add_WindowTrim() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MWindowTrim windowTrim = MApplicationFactory.eINSTANCE
				.createWindowTrim();
		window.getChildren().add(windowTrim);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(0, window.getChildren().size());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getChildren().size());
		assertNotNull(window.getChildren().get(0));
		assertTrue(window.getChildren().get(0) instanceof MWindowTrim);
	}

	public void testElementContainer_Children_Remove_WindowTrim() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		MWindowTrim windowTrim = MApplicationFactory.eINSTANCE
				.createWindowTrim();
		window.getChildren().add(windowTrim);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		windowTrim = (MWindowTrim) window.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getChildren().size());
		assertEquals(windowTrim, window.getChildren().get(0));

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(0, window.getChildren().size());
	}

	public void testElementContainer_Children_Add_ToolBar() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		MWindowTrim windowTrim = MApplicationFactory.eINSTANCE
				.createWindowTrim();
		window.getChildren().add(windowTrim);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MToolBar toolBar = MApplicationFactory.eINSTANCE.createToolBar();
		windowTrim.getChildren().add(toolBar);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		windowTrim = (MWindowTrim) window.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getChildren().size());
		assertEquals(windowTrim, window.getChildren().get(0));

		assertEquals(0, windowTrim.getChildren().size());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getChildren().size());
		assertEquals(windowTrim, window.getChildren().get(0));

		assertEquals(1, windowTrim.getChildren().size());
		assertTrue(windowTrim.getChildren().get(0) instanceof MToolBar);
	}

	public void testElementContainer_Children_Remove_ToolBar() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		MWindowTrim windowTrim = MApplicationFactory.eINSTANCE
				.createWindowTrim();
		window.getChildren().add(windowTrim);

		MToolBar toolBar = MApplicationFactory.eINSTANCE.createToolBar();
		windowTrim.getChildren().add(toolBar);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		windowTrim.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		windowTrim = (MWindowTrim) window.getChildren().get(0);
		toolBar = (MToolBar) windowTrim.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getChildren().size());
		assertEquals(windowTrim, window.getChildren().get(0));

		assertEquals(1, windowTrim.getChildren().size());
		assertEquals(toolBar, windowTrim.getChildren().get(0));

		assertEquals(0, toolBar.getChildren().size());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getChildren().size());
		assertEquals(windowTrim, window.getChildren().get(0));

		assertEquals(0, windowTrim.getChildren().size());
	}

	public void testElementContainer_Children_SwitchParent_ToolBar() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		MWindowTrim windowTrim1 = MApplicationFactory.eINSTANCE
				.createWindowTrim();
		window.getChildren().add(windowTrim1);
		MWindowTrim windowTrim2 = MApplicationFactory.eINSTANCE
				.createWindowTrim();
		window.getChildren().add(windowTrim2);

		MToolBar toolBar = MApplicationFactory.eINSTANCE.createToolBar();
		windowTrim1.getChildren().add(toolBar);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		windowTrim2.getChildren().add(windowTrim1.getChildren().remove(0));

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		windowTrim1 = (MWindowTrim) window.getChildren().get(0);
		windowTrim2 = (MWindowTrim) window.getChildren().get(1);
		toolBar = (MToolBar) windowTrim1.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(2, window.getChildren().size());
		assertEquals(windowTrim1, window.getChildren().get(0));
		assertEquals(windowTrim2, window.getChildren().get(1));

		assertEquals(1, windowTrim1.getChildren().size());
		assertEquals(toolBar, windowTrim1.getChildren().get(0));
		assertEquals(0, windowTrim2.getChildren().size());

		assertEquals(0, toolBar.getChildren().size());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(2, window.getChildren().size());
		assertEquals(windowTrim1, window.getChildren().get(0));
		assertEquals(windowTrim2, window.getChildren().get(1));

		assertEquals(0, windowTrim1.getChildren().size());
		assertEquals(1, windowTrim2.getChildren().size());
		assertEquals(toolBar, windowTrim2.getChildren().get(0));

		assertEquals(0, toolBar.getChildren().size());
	}

	public void testElementContainer_ActiveChild() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		stack.getChildren().add(part1);

		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		stack.getChildren().add(part2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack.setSelectedElement(part1);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack = (MPartStack) window.getChildren().get(0);

		part1 = stack.getChildren().get(0);
		part2 = stack.getChildren().get(1);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertNull(stack.getSelectedElement());

		applyAll(deltas);

		assertEquals(part1, stack.getSelectedElement());
	}

	public void testElementContainer_ActiveChild2() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		stack.getChildren().add(part1);

		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		stack.getChildren().add(part2);

		stack.setSelectedElement(part1);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack.setSelectedElement(part2);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack = (MPartStack) window.getChildren().get(0);

		part1 = stack.getChildren().get(0);
		part2 = stack.getChildren().get(1);

		stack.setSelectedElement(part1);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(part1, stack.getSelectedElement());

		applyAll(deltas);

		assertEquals(part2, stack.getSelectedElement());
	}

	private void testElementContainer_ActiveChild3(boolean setActiveChildFirst) {
		// create an application with one window
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		// create a part sash container and add it to the window
		MPartSashContainer partSashContainer = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(partSashContainer);

		// add a new part as a child of the container and also set it as the
		// active child
		MPart part = MApplicationFactory.eINSTANCE.createPart();
		if (setActiveChildFirst) {
			partSashContainer.setSelectedElement(part);
			partSashContainer.getChildren().add(part);
		} else {
			partSashContainer.getChildren().add(part);
			partSashContainer.setSelectedElement(part);
		}

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(0, window.getChildren().size());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getChildren().size());
		assertTrue(window.getChildren().get(0) instanceof MPartSashContainer);

		partSashContainer = (MPartSashContainer) window.getChildren().get(0);

		assertEquals(1, partSashContainer.getChildren().size());
		assertTrue(partSashContainer.getChildren().get(0) instanceof MPart);

		part = (MPart) partSashContainer.getChildren().get(0);
		assertEquals(part, partSashContainer.getSelectedElement());
	}

	public void testElementContainer_ActiveChild3_True() {
		testElementContainer_ActiveChild3(true);
	}

	public void testElementContainer_ActiveChild3_False() {
		testElementContainer_ActiveChild3(false);
	}
}
