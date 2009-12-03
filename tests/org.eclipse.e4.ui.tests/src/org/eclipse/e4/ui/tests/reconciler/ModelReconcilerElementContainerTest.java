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
		String applicationId = createId();
		String windowId = createId();

		String partId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		part.setName("newPart");
		window.getChildren().add(part);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(0, window.getChildren().size());

		applyAll(deltas);

		assertEquals(1, window.getChildren().size());

		part = (MPart) window.getChildren().get(0);
		assertEquals(partId, part.getId());
		assertEquals("newPart", part.getName());
	}

	public void testElementContainer_Children_Add2() {
		String applicationId = createId();
		String windowId = createId();

		String part1Id = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(part1Id);
		window.getChildren().add(part);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		String part2Id = createId();

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(part2Id);
		part.setName("newPart");
		window.getChildren().add(part);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(part1Id);
		window.getChildren().add(part);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(1, window.getChildren().size());
		assertEquals(part, window.getChildren().get(0));

		part = (MPart) window.getChildren().get(0);
		assertEquals(part1Id, part.getId());

		applyAll(deltas);

		assertEquals(2, window.getChildren().size());
		assertEquals(part, window.getChildren().get(0));

		part = (MPart) window.getChildren().get(1);
		assertEquals(part2Id, part.getId());
		assertEquals("newPart", part.getName());
	}

	public void testElementContainer_Children_Add3() {
		String applicationId = createId();
		String windowId = createId();

		String stackId = createId();
		String partId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.setId(stackId);

		window.getChildren().add(stack);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		part.setName("newPart");
		stack.getChildren().add(part);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.setId(stackId);

		window.getChildren().add(stack);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(0, stack.getChildren().size());

		applyAll(deltas);

		assertEquals(1, stack.getChildren().size());

		part = stack.getChildren().get(0);
		assertEquals(partId, part.getId());
		assertEquals("newPart", part.getName());
	}

	public void testElementContainer_Children_Remove() {
		String applicationId = createId();
		String windowId = createId();

		String partId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		window.getChildren().add(part);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		window.getChildren().add(part);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(1, window.getChildren().size());

		part = (MPart) window.getChildren().get(0);
		assertEquals(partId, part.getId());

		applyAll(deltas);

		assertEquals(0, window.getChildren().size());
	}

	public void testElementContainer_Children_Remove2() {
		String applicationId = createId();
		String windowId = createId();

		String part1Id = createId();
		String part2Id = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		part1.setId(part1Id);
		window.getChildren().add(part1);

		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		part2.setId(part2Id);
		window.getChildren().add(part2);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		part1 = MApplicationFactory.eINSTANCE.createPart();
		part1.setId(part1Id);
		window.getChildren().add(part1);

		part2 = MApplicationFactory.eINSTANCE.createPart();
		part2.setId(part2Id);
		window.getChildren().add(part2);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(2, window.getChildren().size());

		part1 = (MPart) window.getChildren().get(0);
		assertEquals(part1Id, part1.getId());
		part2 = (MPart) window.getChildren().get(1);
		assertEquals(part2Id, part2.getId());

		applyAll(deltas);

		assertEquals(1, window.getChildren().size());
		assertEquals(part2, window.getChildren().get(0));
	}

	public void testElementContainer_Children_Remove3() {
		String applicationId = createId();
		String windowId = createId();

		String stackId = createId();
		String partId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.setId(stackId);

		window.getChildren().add(stack);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		stack.getChildren().add(part);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.setId(stackId);

		window.getChildren().add(stack);

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		stack.getChildren().add(part);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(1, stack.getChildren().size());

		part = stack.getChildren().get(0);
		assertEquals(partId, part.getId());

		applyAll(deltas);

		assertEquals(0, stack.getChildren().size());
	}

	public void testElementContainer_Children_Remove4() {
		String applicationId = createId();
		String windowId = createId();

		String stackId = createId();
		String part1Id = createId();
		String part2Id = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.setId(stackId);

		window.getChildren().add(stack);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		part1.setId(part1Id);
		stack.getChildren().add(part1);

		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		part2.setId(part2Id);
		stack.getChildren().add(part2);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.setId(stackId);

		window.getChildren().add(stack);

		part1 = MApplicationFactory.eINSTANCE.createPart();
		part1.setId(part1Id);
		stack.getChildren().add(part1);

		part2 = MApplicationFactory.eINSTANCE.createPart();
		part2.setId(part2Id);
		stack.getChildren().add(part2);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(2, stack.getChildren().size());
		assertEquals(part1, stack.getChildren().get(0));
		assertEquals(part2, stack.getChildren().get(1));

		applyAll(deltas);

		assertEquals(1, stack.getChildren().size());
		assertEquals(part2, stack.getChildren().get(0));
	}

	public void testElementContainer_Children_MovedFromOneStackToAnother() {
		String applicationId = createId();
		String windowId = createId();

		String stack1Id = createId();
		String stack2Id = createId();
		String part1Id = createId();
		String part2Id = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPartStack stack1 = MApplicationFactory.eINSTANCE.createPartStack();
		stack1.setId(stack1Id);

		MPartStack stack2 = MApplicationFactory.eINSTANCE.createPartStack();
		stack2.setId(stack2Id);

		window.getChildren().add(stack1);
		window.getChildren().add(stack2);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		part1.setId(part1Id);
		stack1.getChildren().add(part1);

		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		part2.setId(part2Id);
		stack2.getChildren().add(part2);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack1.getChildren().remove(part1);
		stack2.getChildren().add(part1);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		stack1 = MApplicationFactory.eINSTANCE.createPartStack();
		stack1.setId(stack1Id);

		stack2 = MApplicationFactory.eINSTANCE.createPartStack();
		stack2.setId(stack2Id);

		window.getChildren().add(stack1);
		window.getChildren().add(stack2);

		part1 = MApplicationFactory.eINSTANCE.createPart();
		part1.setId(part1Id);
		stack1.getChildren().add(part1);

		part2 = MApplicationFactory.eINSTANCE.createPart();
		part2.setId(part2Id);
		stack2.getChildren().add(part2);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

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
		String applicationId = createId();
		String windowId = createId();

		String stackId = createId();
		String part1Id = createId();
		String part2Id = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.setId(stackId);

		window.getChildren().add(stack);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		part1.setId(part1Id);
		stack.getChildren().add(part1);

		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		part2.setId(part2Id);
		stack.getChildren().add(part2);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack.getChildren().move(0, 1);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.setId(stackId);

		window.getChildren().add(stack);

		part1 = MApplicationFactory.eINSTANCE.createPart();
		part1.setId(part1Id);
		stack.getChildren().add(part1);

		part2 = MApplicationFactory.eINSTANCE.createPart();
		part2.setId(part2Id);
		stack.getChildren().add(part2);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(2, stack.getChildren().size());
		assertEquals(part1, stack.getChildren().get(0));
		assertEquals(part2, stack.getChildren().get(1));

		applyAll(deltas);

		assertEquals(2, stack.getChildren().size());
		assertEquals(part2, stack.getChildren().get(0));
		assertEquals(part1, stack.getChildren().get(1));
	}

	public void testElementContainer_ActiveChild() {
		String applicationId = createId();
		String windowId = createId();

		String stackId = createId();
		String part1Id = createId();
		String part2Id = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.setId(stackId);

		window.getChildren().add(stack);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		part1.setId(part1Id);
		stack.getChildren().add(part1);

		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		part2.setId(part2Id);
		stack.getChildren().add(part2);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack.setActiveChild(part1);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.setId(stackId);

		window.getChildren().add(stack);

		part1 = MApplicationFactory.eINSTANCE.createPart();
		part1.setId(part1Id);
		stack.getChildren().add(part1);

		part2 = MApplicationFactory.eINSTANCE.createPart();
		part2.setId(part2Id);
		stack.getChildren().add(part2);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertNull(stack.getActiveChild());

		applyAll(deltas);

		assertEquals(part1, stack.getActiveChild());
	}

	public void testElementContainer_ActiveChild2() {
		String applicationId = createId();
		String windowId = createId();

		String stackId = createId();
		String part1Id = createId();
		String part2Id = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.setId(stackId);

		window.getChildren().add(stack);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		part1.setId(part1Id);
		stack.getChildren().add(part1);

		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		part2.setId(part2Id);
		stack.getChildren().add(part2);

		stack.setActiveChild(part1);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack.setActiveChild(part2);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.setId(stackId);

		window.getChildren().add(stack);

		part1 = MApplicationFactory.eINSTANCE.createPart();
		part1.setId(part1Id);
		stack.getChildren().add(part1);

		part2 = MApplicationFactory.eINSTANCE.createPart();
		part2.setId(part2Id);
		stack.getChildren().add(part2);

		stack.setActiveChild(part1);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(part1, stack.getActiveChild());

		applyAll(deltas);

		assertEquals(part2, stack.getActiveChild());
	}
}
