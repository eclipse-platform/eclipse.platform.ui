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

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MKeyBinding;
import org.eclipse.e4.ui.model.application.MPSCElement;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;
import org.eclipse.emf.common.util.EList;

public abstract class ModelReconcilerScenarioTest extends ModelReconcilerTest {

	/**
	 * <ol>
	 * <li>Initially, the application has a part named "name".</li>
	 * <li>The user renames this part as "customName".</li>
	 * <li>Later, the application changes the name to "name2".</li>
	 * <li>The merged outcome should be that the part stay named as
	 * "customName".</li>
	 * </ol>
	 */
	public void testPart_Name_NameChangeFromUser_UserWins() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setLabel("name");

		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setLabel("customName");

		Object serializedState = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		part = (MPart) window.getChildren().get(0);
		part.setLabel("name2");

		window.getChildren().add(part);

		Collection<ModelDelta> deltas = constructDeltas(application,
				serializedState);

		assertEquals("name2", part.getLabel());

		applyAll(deltas);

		assertEquals("customName", part.getLabel());
	}

	/**
	 * <ol>
	 * <li>Initially, the application has a visible part named "name".</li>
	 * <li>The user renames this part as "customName".</li>
	 * <li>Later, the application changes the name to "name2" in addition to
	 * making it invisible.</li>
	 * <li>The merged outcome should be that the part stays invisible but is
	 * renamed "customName" per user intervention.</li>
	 * </ol>
	 */
	public void testPart_Visibility_TrueFalseFromApplication_ApplicationWins() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setLabel("name");
		part.setToBeRendered(true);

		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setLabel("customName");

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);
		part.setLabel("name2");
		part.setToBeRendered(false);

		window.getChildren().add(part);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertFalse(part.isToBeRendered());
		assertEquals("name2", part.getLabel());

		applyAll(deltas);

		// the user's change should not have made this part visible
		assertFalse(part.isToBeRendered());
		// the user's change should have been applied
		assertEquals("customName", part.getLabel());
	}

	/**
	 * <ol>
	 * <li>Initially, the application has a visible part named "name".</li>
	 * <li>The user makes this part invisible.</li>
	 * <li>Later, the application changes the name to "name2".</li>
	 * <li>The merged outcome should be that the part becomes invisible (as the
	 * user performed this action) but the name change from "name" to "name2"
	 * should still occur.</li>
	 * </ol>
	 */
	public void testPart_Visibility_TrueFalseFromUser_UserWins() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setLabel("name");
		part.setToBeRendered(true);

		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setToBeRendered(false);

		Object serializedState = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		part = (MPart) window.getChildren().get(0);
		part.setLabel("name2");

		window.getChildren().add(part);

		Collection<ModelDelta> deltas = constructDeltas(application,
				serializedState);

		assertTrue(part.isToBeRendered());
		assertEquals("name2", part.getLabel());

		applyAll(deltas);

		assertFalse(part.isToBeRendered());
		// the application's change should not have been overridden
		assertEquals("name2", part.getLabel());
	}

	public void testPart_Addition_PlacedAfterHiddenPart_UserWins() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setToBeRendered(true);
		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setToBeRendered(true);
		MPart partD = MApplicationFactory.eINSTANCE.createPart();
		partD.setToBeRendered(true);

		window.getChildren().add(partA);
		window.getChildren().add(partB);
		window.getChildren().add(partD);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		partB.setToBeRendered(false);

		Object serializedState = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		partA = (MPart) window.getChildren().get(0);
		partB = (MPart) window.getChildren().get(1);
		partD = (MPart) window.getChildren().get(2);

		MPart partC = MApplicationFactory.eINSTANCE.createPart();
		partC.setToBeRendered(true);

		window.getChildren().add(2, partC);

		Collection<ModelDelta> deltas = constructDeltas(application,
				serializedState);

		assertTrue(partA.isToBeRendered());
		assertTrue(partB.isToBeRendered());
		assertTrue(partC.isToBeRendered());
		assertTrue(partD.isToBeRendered());

		applyAll(deltas);

		assertTrue(partA.isToBeRendered());
		assertFalse(partB.isToBeRendered());
		assertTrue(partC.isToBeRendered());
		assertTrue(partD.isToBeRendered());
	}

	/**
	 * <ol>
	 * <li>Initially, the application has three parts, A, B, and C.</li>
	 * <li>The user removes part B.</li>
	 * <li>Later, the application is defined as having four parts, A, B, C, and
	 * D.</li>
	 * <li>The merged outcome should be three parts, A, B, and D.</li>
	 * </ol>
	 */
	public void testPart_Addition_PlacedAfterRemovedPart_UserWins() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		MPart partC = MApplicationFactory.eINSTANCE.createPart();

		window.getChildren().add(partA);
		window.getChildren().add(partB);
		window.getChildren().add(partC);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.getChildren().remove(partB);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		partA = (MPart) window.getChildren().get(0);
		partB = (MPart) window.getChildren().get(1);
		partC = (MPart) window.getChildren().get(2);

		MPart partD = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(partD);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		EList<MPSCElement> children = window.getChildren();
		assertEquals(4, children.size());
		assertEquals(partA, children.get(0));
		assertEquals(partB, children.get(1));
		assertEquals(partC, children.get(2));
		assertEquals(partD, children.get(3));

		applyAll(deltas);

		children = window.getChildren();
		assertEquals(3, children.size());
		assertEquals(partA, children.get(0));
		assertEquals(partC, children.get(1));
		assertEquals(partD, children.get(2));
	}

	/**
	 * <ol>
	 * <li>Initially, the application has two parts, A and B.</li>
	 * <li>The user removes both parts A and B.</li>
	 * <li>Later, the application is defined as having three parts, A, B, and C.
	 * </li>
	 * <li>The merged outcome should be only one part, part C.</li>
	 * </ol>
	 */
	public void testPart_Addition_PlacedAfterRemovedPart_UserWins2() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		MPart partB = MApplicationFactory.eINSTANCE.createPart();

		window.getChildren().add(partA);
		window.getChildren().add(partB);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.getChildren().remove(partA);
		window.getChildren().remove(partB);

		Object serializedState = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		partA = (MPart) window.getChildren().get(0);
		partB = (MPart) window.getChildren().get(1);

		MPart partC = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(partC);

		Collection<ModelDelta> deltas = constructDeltas(application,
				serializedState);

		assertEquals(3, window.getChildren().size());
		assertEquals(partA, window.getChildren().get(0));
		assertEquals(partB, window.getChildren().get(1));
		assertEquals(partC, window.getChildren().get(2));

		applyAll(deltas);

		EList<MPSCElement> children = window.getChildren();
		assertEquals(1, children.size());
		assertEquals(partC, children.get(0));
	}

	/**
	 * <ol>
	 * <li>The application has a window with a stack that houses two parts, A
	 * and B.</li>
	 * <li>The user creates a new stack to the left of the original stack and
	 * places part A in it.</li>
	 * </ol>
	 */
	public void testPartStack_Addition_ContainsExistingPart() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		MPartStack stack1 = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack1);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		stack1.getChildren().add(part1);
		stack1.getChildren().add(part2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPartStack stack2 = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(0, stack2);
		stack2.getChildren().add(part1);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack1 = (MPartStack) window.getChildren().get(0);
		part1 = stack1.getChildren().get(0);
		part2 = stack1.getChildren().get(1);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(2, stack1.getChildren().size());
		assertEquals(part1, stack1.getChildren().get(0));
		assertEquals(part2, stack1.getChildren().get(1));

		applyAll(deltas);

		assertEquals(1, stack1.getChildren().size());
		assertEquals(part2, stack1.getChildren().get(0));

		assertEquals(2, window.getChildren().size());

		stack2 = (MPartStack) window.getChildren().get(0);
		assertEquals(part1, stack2.getChildren().get(0));
	}

	/**
	 * <ol>
	 * <li>The application has a window with a stack that houses two parts, A
	 * and B.</li>
	 * <li>The user creates a new stack to the right of the original stack and
	 * places part A in it.</li>
	 * </ol>
	 */
	public void testPartStack_Addition_ContainsExistingPart2() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		MPartStack stack1 = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack1);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		stack1.getChildren().add(part1);
		stack1.getChildren().add(part2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPartStack stack2 = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack2);
		stack2.getChildren().add(part1);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack1 = (MPartStack) window.getChildren().get(0);
		part1 = stack1.getChildren().get(0);
		part2 = stack1.getChildren().get(1);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(2, stack1.getChildren().size());
		assertEquals(part1, stack1.getChildren().get(0));
		assertEquals(part2, stack1.getChildren().get(1));

		applyAll(deltas);

		assertEquals(1, stack1.getChildren().size());
		assertEquals(part2, stack1.getChildren().get(0));

		assertEquals(2, window.getChildren().size());

		stack2 = (MPartStack) window.getChildren().get(1);
		assertEquals(part1, stack2.getChildren().get(0));
	}

	private void testPartStack_AdditionInBack_ApplicationHasNewStackInFront(
			boolean performMoveFirst) {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		MPartStack stack1 = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack1);

		// stack with three children in it
		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		MPart partC = MApplicationFactory.eINSTANCE.createPart();
		stack1.getChildren().add(partA);
		stack1.getChildren().add(partB);
		stack1.getChildren().add(partC);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPartStack stack2 = MApplicationFactory.eINSTANCE.createPartStack();
		// add a new stack at the end of the existing stack
		window.getChildren().add(stack2);
		// put A in it
		stack2.getChildren().add(partA);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack1 = (MPartStack) window.getChildren().get(0);
		partA = stack1.getChildren().get(0);
		partB = stack1.getChildren().get(1);
		partC = stack1.getChildren().get(2);

		// create a new stack
		MPartStack stack3 = MApplicationFactory.eINSTANCE.createPartStack();

		if (performMoveFirst) {
			// place part C in the new stack first
			stack3.getChildren().add(partC);
			// now add the new stack to the window
			window.getChildren().add(0, stack3);
		} else {
			// add the new stack first
			window.getChildren().add(0, stack3);
			// now place part C in the new stack
			stack3.getChildren().add(partC);
		}

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(2, window.getChildren().size());
		assertEquals(stack3, window.getChildren().get(0));
		assertEquals(stack1, window.getChildren().get(1));

		assertEquals(1, stack3.getChildren().size());
		assertEquals(partC, stack3.getChildren().get(0));

		assertEquals(2, stack1.getChildren().size());
		assertEquals(partA, stack1.getChildren().get(0));
		assertEquals(partB, stack1.getChildren().get(1));

		applyAll(deltas);

		assertEquals(3, window.getChildren().size());

		assertEquals(stack1, window.getChildren().get(0));
		assertEquals(stack3, window.getChildren().get(2));

		assertEquals(1, stack1.getChildren().size());
		assertEquals(partB, stack1.getChildren().get(0));

		assertEquals(1, stack3.getChildren().size());
		assertEquals(partC, stack3.getChildren().get(0));

		stack2 = (MPartStack) window.getChildren().get(1);
		assertEquals(partA, stack2.getChildren().get(0));
	}

	public void testPartStack_AdditionInBack_ApplicationHasNewStackInFront_True() {
		testPartStack_AdditionInBack_ApplicationHasNewStackInFront(true);
	}

	public void testPartStack_AdditionInBack_ApplicationHasNewStackInFront_False() {
		testPartStack_AdditionInBack_ApplicationHasNewStackInFront(false);
	}

	/**
	 * <ol>
	 * <li>The application has a window with a stack that houses three parts, A,
	 * B, and C.</li>
	 * <li>The user creates a new stack to the left of the original stack and
	 * places part A in it.</li>
	 * <li>The new version of the application's window has two stacks, the first
	 * one has A, and B, and the second one has C in it.</li>
	 * <li>The merged outcome should be three stacks with A, B, and C each in
	 * one of them from left to right.</li>
	 * </ol>
	 */
	private void testPartStack_AdditionInFront_ApplicationHasNewStackInBack(
			boolean performMoveFirst) {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		MPartStack stack1 = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack1);

		// stack with three children in it
		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		MPart partC = MApplicationFactory.eINSTANCE.createPart();
		stack1.getChildren().add(partA);
		stack1.getChildren().add(partB);
		stack1.getChildren().add(partC);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPartStack stack2 = MApplicationFactory.eINSTANCE.createPartStack();
		// add a new stack to the left of the existing stack
		window.getChildren().add(0, stack2);
		// put A in it
		stack2.getChildren().add(partA);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack1 = (MPartStack) window.getChildren().get(0);
		partA = stack1.getChildren().get(0);
		partB = stack1.getChildren().get(1);
		partC = stack1.getChildren().get(2);

		// create a new stack
		MPartStack stack3 = MApplicationFactory.eINSTANCE.createPartStack();

		if (performMoveFirst) {
			// place part C in the new stack first
			stack3.getChildren().add(partC);
			// now add the new stack to the window
			window.getChildren().add(stack3);
		} else {
			// add the new stack first
			window.getChildren().add(stack3);
			// now place part C in the new stack
			stack3.getChildren().add(partC);
		}

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(2, window.getChildren().size());
		assertEquals(stack1, window.getChildren().get(0));
		assertEquals(stack3, window.getChildren().get(1));

		assertEquals(2, stack1.getChildren().size());
		assertEquals(partA, stack1.getChildren().get(0));
		assertEquals(partB, stack1.getChildren().get(1));

		assertEquals(1, stack3.getChildren().size());
		assertEquals(partC, stack3.getChildren().get(0));

		applyAll(deltas);

		assertEquals(3, window.getChildren().size());
		// moved off by one because of the stack created by the user
		assertEquals(stack1, window.getChildren().get(1));
		assertEquals(stack3, window.getChildren().get(2));

		stack2 = (MPartStack) window.getChildren().get(0);
		assertEquals(partA, stack2.getChildren().get(0));

		assertEquals(1, stack1.getChildren().size());
		assertEquals(partB, stack1.getChildren().get(0));

		assertEquals(1, stack3.getChildren().size());
		assertEquals(partC, stack3.getChildren().get(0));
	}

	public void testPartStack_AdditionInFront_ApplicationHasNewStackInBack_True() {
		testPartStack_AdditionInFront_ApplicationHasNewStackInBack(true);
	}

	public void testPartStack_AdditionInFront_ApplicationHasNewStackInBack_False() {
		testPartStack_AdditionInFront_ApplicationHasNewStackInBack(false);
	}

	/**
	 * <ol>
	 * <li>The application has a window two stacks, the first stack houses parts
	 * A and B, the second stack houses part C.</li>
	 * <li>The user moves part B from the first stack to the second stack, after
	 * part C.</li>
	 * <li>The new version of the application is identical to the original
	 * except that the second stack now has a new part, so it houses parts C and
	 * the new part, part D.</li>
	 * <li>The merged outcome should be two stacks with A in the first one, and
	 * the second stack has C, B, and D, in that order from left to right.</li>
	 * </ol>
	 */
	public void testPart_MoveFromExistingStackToExistingStack_ToStackHasNewPart() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		MPartStack stack1 = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack1);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		stack1.getChildren().add(partA);
		stack1.getChildren().add(partB);

		MPartStack stack2 = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack2);

		MPart partC = MApplicationFactory.eINSTANCE.createPart();
		stack2.getChildren().add(partC);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack2.getChildren().add(partB);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack1 = (MPartStack) window.getChildren().get(0);
		partA = stack1.getChildren().get(0);
		partB = stack1.getChildren().get(1);

		stack2 = (MPartStack) window.getChildren().get(1);
		partC = stack2.getChildren().get(0);

		MPart partD = MApplicationFactory.eINSTANCE.createPart();
		stack2.getChildren().add(partD);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(2, window.getChildren().size());
		assertEquals(stack1, window.getChildren().get(0));
		assertEquals(stack2, window.getChildren().get(1));

		assertEquals(2, stack1.getChildren().size());
		assertEquals(partA, stack1.getChildren().get(0));
		assertEquals(partB, stack1.getChildren().get(1));

		assertEquals(2, stack2.getChildren().size());
		assertEquals(partC, stack2.getChildren().get(0));
		assertEquals(partD, stack2.getChildren().get(1));

		applyAll(deltas);

		assertEquals(2, window.getChildren().size());
		assertEquals(stack1, window.getChildren().get(0));
		assertEquals(stack2, window.getChildren().get(1));

		assertEquals(1, stack1.getChildren().size());
		assertEquals(partA, stack1.getChildren().get(0));

		assertEquals(3, stack2.getChildren().size());
		assertEquals(partC, stack2.getChildren().get(0));
		assertEquals(partB, stack2.getChildren().get(1));
		assertEquals(partD, stack2.getChildren().get(2));
	}

	public void testBindingContainer_NewWithBindings() {
		MApplication application = createApplication();

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MWindow window = createWindow(application);

		MKeyBinding keyBinding = MApplicationFactory.eINSTANCE
				.createKeyBinding();
		window.getBindings().add(keyBinding);

		Object state = reconciler.serialize();

		application = createApplication();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, application.getChildren().size());
		assertEquals(0, application.getBindings().size());

		applyAll(deltas);

		window = application.getChildren().get(0);
		assertEquals(1, window.getBindings().size());
	}

	public void testElementContainer_ActiveChild_Removed() {
		MApplication application = createApplication();
		MWindow window1 = MApplicationFactory.eINSTANCE.createWindow();
		MWindow window2 = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window1);
		application.getChildren().add(window2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		application.setActiveChild(window2);

		Object state = reconciler.serialize();

		application = createApplication();
		window1 = application.getChildren().get(0);

		application.getChildren().remove(1);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertNull(application.getActiveChild());
		assertEquals(window1, application.getChildren().get(0));

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertNull(application.getActiveChild());
		assertEquals(window1, application.getChildren().get(0));
	}

	public void testElementContainer_ActiveChild_Removed2() {
		MApplication application = createApplication();
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack partStack1 = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(partStack1);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		partStack1.getChildren().add(part1);
		partStack1.getChildren().add(part2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPartStack partStack2 = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(partStack2);

		partStack2.getChildren().add(part2);
		partStack2.setActiveChild(part2);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		partStack1 = (MPartStack) window.getChildren().get(0);
		part1 = partStack1.getChildren().get(0);

		partStack1.getChildren().remove(1);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getChildren().size());
		assertEquals(partStack1, window.getChildren().get(0));

		assertEquals(1, partStack1.getChildren().size());
		assertEquals(part1, partStack1.getChildren().get(0));

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(2, window.getChildren().size());
		assertEquals(partStack1, window.getChildren().get(0));

		assertEquals(1, partStack1.getChildren().size());
		assertEquals(part1, partStack1.getChildren().get(0));

		partStack2 = (MPartStack) window.getChildren().get(1);

		assertEquals(0, partStack2.getChildren().size());
		assertNull(partStack2.getActiveChild());
	}

	public void testElementContainer_Children_Move_IdenticalToUserChange() {
		MApplication application = createApplication();
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack partStack1 = MApplicationFactory.eINSTANCE.createPartStack();
		MPartStack partStack2 = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(partStack1);
		window.getChildren().add(partStack2);

		MPart part1 = MApplicationFactory.eINSTANCE.createPart();
		MPart part2 = MApplicationFactory.eINSTANCE.createPart();
		partStack1.getChildren().add(part1);
		partStack1.getChildren().add(part2);

		MPart part3 = MApplicationFactory.eINSTANCE.createPart();
		partStack2.getChildren().add(part3);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		partStack2.getChildren().add(part2);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		partStack1 = (MPartStack) window.getChildren().get(0);
		partStack2 = (MPartStack) window.getChildren().get(1);

		part1 = partStack1.getChildren().get(0);
		part2 = partStack1.getChildren().get(1);
		part3 = partStack2.getChildren().get(0);

		partStack2.getChildren().add(part2);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(2, window.getChildren().size());
		assertEquals(partStack1, window.getChildren().get(0));
		assertEquals(partStack2, window.getChildren().get(1));

		assertEquals(1, partStack1.getChildren().size());
		assertEquals(part1, partStack1.getChildren().get(0));

		assertEquals(2, partStack2.getChildren().size());
		assertEquals(part3, partStack2.getChildren().get(0));
		assertEquals(part2, partStack2.getChildren().get(1));

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(2, window.getChildren().size());
		assertEquals(partStack1, window.getChildren().get(0));
		assertEquals(partStack2, window.getChildren().get(1));

		assertEquals(1, partStack1.getChildren().size());
		assertEquals(part1, partStack1.getChildren().get(0));

		assertEquals(2, partStack2.getChildren().size());
		assertEquals(part3, partStack2.getChildren().get(0));
		assertEquals(part2, partStack2.getChildren().get(1));
	}

	/**
	 * Tests that the changes pertaining to multiple key bindings that map to
	 * the same command gets applied properly.
	 * <p>
	 * The application defines one command with an id of "commandId". The
	 * application also has a child window. Both the application and the window
	 * has a key binding that points to the "commandId" command. The test will
	 * alter the key sequence of the key bindings and verify that when the delta
	 * has been applied that the delta is applied to the correct key binding.
	 * This is an important test as key bindings themselves do not have an id so
	 * they have to rely on their command's id.
	 * </p>
	 * 
	 * @param originalApplicationKeyBindingSequence
	 *            the key binding that has been defined for the application
	 * @param userApplicationKeyBindingSequence
	 *            the application-level key binding that the user has modified
	 * @param originalWindowKeyBindingSequence
	 *            the key binding that has been defined for the application's
	 *            window
	 * @param userWindowKeyBindingSequence
	 *            the window-level key binding that the user has modified
	 */
	private void testApplication_Commands_MultiLevelKeyBindings(
			String originalApplicationKeyBindingSequence,
			String userApplicationKeyBindingSequence,
			String originalWindowKeyBindingSequence,
			String userWindowKeyBindingSequence) {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		application.getCommands().add(command);

		MKeyBinding applicationKeyBinding = MApplicationFactory.eINSTANCE
				.createKeyBinding();
		applicationKeyBinding.setCommand(command);
		applicationKeyBinding
				.setKeySequence(originalApplicationKeyBindingSequence);

		MKeyBinding windowKeyBinding = MApplicationFactory.eINSTANCE
				.createKeyBinding();
		windowKeyBinding.setCommand(command);
		windowKeyBinding.setKeySequence(originalWindowKeyBindingSequence);

		application.getBindings().add(applicationKeyBinding);
		window.getBindings().add(windowKeyBinding);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		applicationKeyBinding.setKeySequence(userApplicationKeyBindingSequence);
		windowKeyBinding.setKeySequence(userWindowKeyBindingSequence);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		command = application.getCommands().get(0);

		applicationKeyBinding = application.getBindings().get(0);
		windowKeyBinding = window.getBindings().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(originalApplicationKeyBindingSequence,
				applicationKeyBinding.getKeySequence());
		assertEquals(originalWindowKeyBindingSequence, windowKeyBinding
				.getKeySequence());

		applyAll(deltas);

		assertEquals(userApplicationKeyBindingSequence, applicationKeyBinding
				.getKeySequence());
		assertEquals(userWindowKeyBindingSequence, windowKeyBinding
				.getKeySequence());
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullNull_NullNull() {
		testApplication_Commands_MultiLevelKeyBindings(null, null, null, null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullNull_NullEmpty() {
		testApplication_Commands_MultiLevelKeyBindings(null, null, null, "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullNull_NullString() {
		testApplication_Commands_MultiLevelKeyBindings(null, null, null,
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullNull_EmptyNull() {
		testApplication_Commands_MultiLevelKeyBindings(null, null, "", null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullNull_EmptyEmpty() {
		testApplication_Commands_MultiLevelKeyBindings(null, null, "", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullNull_EmptyString() {
		testApplication_Commands_MultiLevelKeyBindings(null, null, "", "Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullNull_StringNull() {
		testApplication_Commands_MultiLevelKeyBindings(null, null, "Ctrl+S",
				null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullNull_StringEmpty() {
		testApplication_Commands_MultiLevelKeyBindings(null, null, "Ctrl+S", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullNull_StringStringUnchanged() {
		testApplication_Commands_MultiLevelKeyBindings(null, null, "Ctrl+S",
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullNull_StringStringChanged() {
		testApplication_Commands_MultiLevelKeyBindings(null, null, "Ctrl+S",
				"Ctrl+D");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullEmpty_NullNull() {
		testApplication_Commands_MultiLevelKeyBindings(null, "", null, null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullEmpty_NullEmpty() {
		testApplication_Commands_MultiLevelKeyBindings(null, "", null, "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullEmpty_NullString() {
		testApplication_Commands_MultiLevelKeyBindings(null, "", null, "Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullEmpty_EmptyNull() {
		testApplication_Commands_MultiLevelKeyBindings(null, "", "", null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullEmpty_EmptyEmpty() {
		testApplication_Commands_MultiLevelKeyBindings(null, "", "", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullEmpty_EmptyString() {
		testApplication_Commands_MultiLevelKeyBindings(null, "", "", "Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullEmpty_StringNull() {
		testApplication_Commands_MultiLevelKeyBindings(null, "", "Ctrl+S", null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullEmpty_StringEmpty() {
		testApplication_Commands_MultiLevelKeyBindings(null, "", "Ctrl+S", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullEmpty_StringStringUnchanged() {
		testApplication_Commands_MultiLevelKeyBindings(null, "", "Ctrl+S",
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullEmpty_StringStringChanged() {
		testApplication_Commands_MultiLevelKeyBindings(null, "", "Ctrl+S",
				"Ctrl+D");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullString_NullNull() {
		testApplication_Commands_MultiLevelKeyBindings(null, "Ctrl+S", null,
				null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullString_NullEmpty() {
		testApplication_Commands_MultiLevelKeyBindings(null, "Ctrl+S", null, "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullString_NullString() {
		testApplication_Commands_MultiLevelKeyBindings(null, "Ctrl+S", null,
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullString_EmptyNull() {
		testApplication_Commands_MultiLevelKeyBindings(null, "Ctrl+S", "", null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullString_EmptyEmpty() {
		testApplication_Commands_MultiLevelKeyBindings(null, "Ctrl+S", "", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullString_EmptyString() {
		testApplication_Commands_MultiLevelKeyBindings(null, "Ctrl+S", "",
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullString_StringNull() {
		testApplication_Commands_MultiLevelKeyBindings(null, "Ctrl+S",
				"Ctrl+S", null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullString_StringEmpty() {
		testApplication_Commands_MultiLevelKeyBindings(null, "Ctrl+S",
				"Ctrl+S", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullString_StringStringUnchanged() {
		testApplication_Commands_MultiLevelKeyBindings(null, "Ctrl+S",
				"Ctrl+S", "Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_NullString_StringStringChanged() {
		testApplication_Commands_MultiLevelKeyBindings(null, "Ctrl+S",
				"Ctrl+S", "Ctrl+D");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyNull_NullNull() {
		testApplication_Commands_MultiLevelKeyBindings("", null, null, null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyNull_NullEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("", null, null, "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyNull_NullString() {
		testApplication_Commands_MultiLevelKeyBindings("", null, null, "Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyNull_EmptyNull() {
		testApplication_Commands_MultiLevelKeyBindings("", null, "", null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyNull_EmptyEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("", null, "", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyNull_EmptyString() {
		testApplication_Commands_MultiLevelKeyBindings("", null, "", "Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyNull_StringNull() {
		testApplication_Commands_MultiLevelKeyBindings("", null, "Ctrl+S", null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyNull_StringEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("", null, "Ctrl+S", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyNull_StringStringUnchanged() {
		testApplication_Commands_MultiLevelKeyBindings("", null, "Ctrl+S",
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyNull_StringStringChanged() {
		testApplication_Commands_MultiLevelKeyBindings("", null, "Ctrl+S",
				"Ctrl+D");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyEmpty_NullNull() {
		testApplication_Commands_MultiLevelKeyBindings("", "", null, null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyEmpty_NullEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("", "", null, "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyEmpty_NullString() {
		testApplication_Commands_MultiLevelKeyBindings("", "", null, "Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyEmpty_EmptyNull() {
		testApplication_Commands_MultiLevelKeyBindings("", "", "", null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyEmpty_EmptyEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("", "", "", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyEmpty_EmptyString() {
		testApplication_Commands_MultiLevelKeyBindings("", "", "", "Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyEmpty_StringNull() {
		testApplication_Commands_MultiLevelKeyBindings("", "", "Ctrl+S", null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyEmpty_StringEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("", "", "Ctrl+S", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyEmpty_StringStringUnchanged() {
		testApplication_Commands_MultiLevelKeyBindings("", "", "Ctrl+S",
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyEmpty_StringStringChanged() {
		testApplication_Commands_MultiLevelKeyBindings("", "", "Ctrl+S",
				"Ctrl+D");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyString_NullNull() {
		testApplication_Commands_MultiLevelKeyBindings("", "Ctrl+S", null, null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyString_NullEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("", "Ctrl+S", null, "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyString_NullString() {
		testApplication_Commands_MultiLevelKeyBindings("", "Ctrl+S", null,
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyString_EmptyNull() {
		testApplication_Commands_MultiLevelKeyBindings("", "Ctrl+S", "", null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyString_EmptyEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("", "Ctrl+S", "", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyString_EmptyString() {
		testApplication_Commands_MultiLevelKeyBindings("", "Ctrl+S", "",
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyString_StringNull() {
		testApplication_Commands_MultiLevelKeyBindings("", "Ctrl+S", "Ctrl+S",
				null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyString_StringEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("", "Ctrl+S", "Ctrl+S",
				"");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyString_StringStringUnchanged() {
		testApplication_Commands_MultiLevelKeyBindings("", "Ctrl+S", "Ctrl+S",
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_EmptyString_StringStringChanged() {
		testApplication_Commands_MultiLevelKeyBindings("", "Ctrl+S", "Ctrl+S",
				"Ctrl+D");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringNull_NullNull() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", null, null,
				null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringNull_NullEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", null, null, "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringNull_NullString() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", null, null,
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringNull_EmptyNull() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", null, "", null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringNull_EmptyEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", null, "", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringNull_EmptyString() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", null, "",
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringNull_StringNull() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", null,
				"Ctrl+S", null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringNull_StringEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", null,
				"Ctrl+S", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringNull_StringStringUnchanged() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", null,
				"Ctrl+S", "Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringNull_StringStringChanged() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", null,
				"Ctrl+S", "Ctrl+D");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringEmpty_NullNull() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "", null, null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringEmpty_NullEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "", null, "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringEmpty_NullString() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "", null,
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringEmpty_EmptyNull() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "", "", null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringEmpty_EmptyEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "", "", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringEmpty_EmptyString() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "", "",
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringEmpty_StringNull() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "", "Ctrl+S",
				null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringEmpty_StringEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "", "Ctrl+S",
				"");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringEmpty_StringStringUnchanged() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "", "Ctrl+S",
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringEmpty_StringStringChanged() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "", "Ctrl+S",
				"Ctrl+D");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringUnchanged_NullNull() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+S",
				null, null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringUnchanged_NullEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+S",
				null, "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringUnchanged_NullString() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+S",
				null, "Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringUnchanged_EmptyNull() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+S", "",
				null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringUnchanged_EmptyEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+S", "",
				"");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringUnchanged_EmptyString() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+S", "",
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringUnchanged_StringNull() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+S",
				"Ctrl+S", null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringUnchanged_StringEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+S",
				"Ctrl+S", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringUnchanged_StringStringUnchanged() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+S",
				"Ctrl+S", "Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringUnchanged_StringStringChanged() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+S",
				"Ctrl+S", "Ctrl+D");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringChanged_NullNull() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+D",
				null, null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringChanged_NullEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+D",
				null, "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringChanged_NullString() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+D",
				null, "Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringChanged_EmptyNull() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+D", "",
				null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringChanged_EmptyEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+D", "",
				"");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringChanged_EmptyString() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+D", "",
				"Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringChanged_StringNull() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+D",
				"Ctrl+S", null);
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringChanged_StringEmpty() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+D",
				"Ctrl+S", "");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringChanged_StringStringUnchanged() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+D",
				"Ctrl+S", "Ctrl+S");
	}

	public void testApplication_Commands_MultiLevelKeyBindings_StringStringChanged_StringStringChanged() {
		testApplication_Commands_MultiLevelKeyBindings("Ctrl+S", "Ctrl+D",
				"Ctrl+S", "Ctrl+D");
	}

}
