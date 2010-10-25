/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerElementContainerTest extends
		ModelReconcilerTest {

	public void testElementContainer_Children_Add() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
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

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part = BasicFactoryImpl.eINSTANCE.createPart();
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

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();

		window.getChildren().add(stack);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
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

		part = (MPart) stack.getChildren().get(0);
		assertEquals("newPart", part.getLabel());
	}

	public void testElementContainer_Children_Add4() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPart editor = BasicFactoryImpl.eINSTANCE.createPart();
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

		MPerspectiveStack stack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();

		MPerspective perspective = AdvancedFactoryImpl.eINSTANCE
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

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
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

	public void testElementContainer_Children_Remove() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
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

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part1);

		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
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

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		stack.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack = (MPartStack) window.getChildren().get(0);
		part = (MPart) stack.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, stack.getChildren().size());

		assertEquals(part, stack.getChildren().get(0));

		applyAll(deltas);

		assertEquals(0, stack.getChildren().size());
	}

	public void testElementContainer_Children_Remove4() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		stack.getChildren().add(part1);

		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		stack.getChildren().add(part2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack = (MPartStack) window.getChildren().get(0);
		part1 = (MPart) stack.getChildren().get(0);
		part2 = (MPart) stack.getChildren().get(1);

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

		MPartStack stack1 = BasicFactoryImpl.eINSTANCE.createPartStack();
		MPartStack stack2 = BasicFactoryImpl.eINSTANCE.createPartStack();

		window.getChildren().add(stack1);
		window.getChildren().add(stack2);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		stack1.getChildren().add(part1);

		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
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

		part1 = (MPart) stack1.getChildren().get(0);
		part2 = (MPart) stack2.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, stack1.getChildren().size());
		assertEquals(part1, stack1.getChildren().get(0));
		assertEquals(1, stack2.getChildren().size());
		assertEquals(part2, stack2.getChildren().get(0));

		applyAll(deltas);

		assertEquals(0, stack1.getChildren().size());

		List<MStackElement> stack2Children = stack2.getChildren();
		assertEquals(2, stack2Children.size());
		assertTrue(stack2Children.contains(part1));
		assertTrue(stack2Children.contains(part2));
	}

	public void testElementContainer_Children_Repositioned() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		stack.getChildren().add(part1);

		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		stack.getChildren().add(part2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack.getChildren().remove(part1);
		stack.getChildren().add(part1);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		stack = (MPartStack) window.getChildren().get(0);

		part1 = (MPart) stack.getChildren().get(0);
		part2 = (MPart) stack.getChildren().get(1);

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

		MPartSashContainer partSashContainer = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(partSashContainer);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
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

		MPartSashContainer partSashContainer1 = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(partSashContainer1);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		partSashContainer1.getChildren().add(part1);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPartSashContainer partSashContainer2 = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(partSashContainer2);

		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
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

	public void testElementContainer_Children_Add_TrimBar() {
		MApplication application = createApplication();
		MTrimmedWindow window = createTrimmedWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MTrimBar trimBar = BasicFactoryImpl.eINSTANCE.createTrimBar();
		window.getTrimBars().add(trimBar);

		Object state = reconciler.serialize();

		application = createApplication();
		window = (MTrimmedWindow) application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(0, window.getChildren().size());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getTrimBars().size());
		assertNotNull(window.getTrimBars().get(0));
	}

	public void testElementContainer_Children_Remove_TrimBar() {
		MApplication application = createApplication();
		MTrimmedWindow window = createTrimmedWindow(application);

		MTrimBar trimBar = BasicFactoryImpl.eINSTANCE.createTrimBar();
		window.getTrimBars().add(trimBar);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.getTrimBars().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		window = (MTrimmedWindow) application.getChildren().get(0);
		trimBar = window.getTrimBars().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getTrimBars().size());
		assertEquals(trimBar, window.getTrimBars().get(0));

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(0, window.getTrimBars().size());
	}

	public void testElementContainer_Children_Add_ToolBar() {
		MApplication application = createApplication();
		MTrimmedWindow window = createTrimmedWindow(application);

		MTrimBar trimBar = BasicFactoryImpl.eINSTANCE.createTrimBar();
		window.getTrimBars().add(trimBar);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		trimBar.getChildren().add(toolBar);

		Object state = reconciler.serialize();

		application = createApplication();
		window = (MTrimmedWindow) application.getChildren().get(0);
		trimBar = window.getTrimBars().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getTrimBars().size());
		assertEquals(trimBar, window.getTrimBars().get(0));

		assertEquals(0, trimBar.getChildren().size());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getTrimBars().size());
		assertEquals(trimBar, window.getTrimBars().get(0));

		assertEquals(1, trimBar.getChildren().size());
		assertTrue(trimBar.getChildren().get(0) instanceof MToolBar);
	}

	public void testElementContainer_Children_Remove_ToolBar() {
		MApplication application = createApplication();
		MTrimmedWindow window = createTrimmedWindow(application);

		MTrimBar trimBar = BasicFactoryImpl.eINSTANCE.createTrimBar();
		window.getTrimBars().add(trimBar);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		trimBar.getChildren().add(toolBar);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		trimBar.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		window = (MTrimmedWindow) application.getChildren().get(0);
		trimBar = window.getTrimBars().get(0);
		toolBar = (MToolBar) trimBar.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getTrimBars().size());
		assertEquals(trimBar, window.getTrimBars().get(0));

		assertEquals(1, trimBar.getChildren().size());
		assertEquals(toolBar, trimBar.getChildren().get(0));

		assertEquals(0, toolBar.getChildren().size());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getTrimBars().size());
		assertEquals(trimBar, window.getTrimBars().get(0));

		assertEquals(0, trimBar.getChildren().size());
	}

	public void testElementContainer_Children_SwitchParent_ToolBar() {
		MApplication application = createApplication();
		MTrimmedWindow window = createTrimmedWindow(application);

		MTrimBar trimBar1 = BasicFactoryImpl.eINSTANCE.createTrimBar();
		window.getTrimBars().add(trimBar1);

		MTrimBar trimBar2 = BasicFactoryImpl.eINSTANCE.createTrimBar();
		window.getTrimBars().add(trimBar2);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		trimBar1.getChildren().add(toolBar);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		trimBar2.getChildren().add(trimBar1.getChildren().remove(0));

		Object state = reconciler.serialize();

		application = createApplication();
		window = (MTrimmedWindow) application.getChildren().get(0);
		trimBar1 = window.getTrimBars().get(0);
		trimBar2 = window.getTrimBars().get(1);
		toolBar = (MToolBar) trimBar1.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(2, window.getTrimBars().size());
		assertEquals(trimBar1, window.getTrimBars().get(0));
		assertEquals(trimBar2, window.getTrimBars().get(1));

		assertEquals(1, trimBar1.getChildren().size());
		assertEquals(toolBar, trimBar1.getChildren().get(0));
		assertEquals(0, trimBar2.getChildren().size());

		assertEquals(0, toolBar.getChildren().size());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(2, window.getTrimBars().size());
		assertEquals(trimBar1, window.getTrimBars().get(0));
		assertEquals(trimBar2, window.getTrimBars().get(1));

		assertEquals(0, trimBar1.getChildren().size());
		assertEquals(1, trimBar2.getChildren().size());
		assertEquals(toolBar, trimBar2.getChildren().get(0));

		assertEquals(0, toolBar.getChildren().size());
	}

	public void testElementContainer_ActiveChild() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		stack.getChildren().add(part1);

		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		stack.getChildren().add(part2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack.setSelectedElement(part1);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack = (MPartStack) window.getChildren().get(0);

		part1 = (MPart) stack.getChildren().get(0);
		part2 = (MPart) stack.getChildren().get(1);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertNull(stack.getSelectedElement());

		applyAll(deltas);

		assertEquals(part1, stack.getSelectedElement());
	}

	public void testElementContainer_ActiveChild2() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		stack.getChildren().add(part1);

		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
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

		part1 = (MPart) stack.getChildren().get(0);
		part2 = (MPart) stack.getChildren().get(1);

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
		MPartSashContainer partSashContainer = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(partSashContainer);

		// add a new part as a child of the container and also set it as the
		// active child
		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
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

	public void testElementContainer_ActiveChild3_False() {
		testElementContainer_ActiveChild3(false);
	}
}
