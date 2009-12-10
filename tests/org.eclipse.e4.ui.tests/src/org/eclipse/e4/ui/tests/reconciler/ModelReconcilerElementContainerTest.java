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
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MWindow;
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

		stack.setActiveChild(part1);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack = (MPartStack) window.getChildren().get(0);

		part1 = stack.getChildren().get(0);
		part2 = stack.getChildren().get(1);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertNull(stack.getActiveChild());

		applyAll(deltas);

		assertEquals(part1, stack.getActiveChild());
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

		stack.setActiveChild(part1);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack.setActiveChild(part2);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack = (MPartStack) window.getChildren().get(0);

		part1 = stack.getChildren().get(0);
		part2 = stack.getChildren().get(1);

		stack.setActiveChild(part1);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(part1, stack.getActiveChild());

		applyAll(deltas);

		assertEquals(part2, stack.getActiveChild());
	}
}
