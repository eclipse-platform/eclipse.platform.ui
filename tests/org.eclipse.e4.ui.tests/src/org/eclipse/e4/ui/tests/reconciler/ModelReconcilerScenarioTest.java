/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerScenarioTest extends ModelReconcilerTest {

	public void testApplicationElement_Id_Changed() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setLabel("name");

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setLabel("customName");

		Object serializedState = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		window.setElementId("id");

		Collection<ModelDelta> deltas = constructDeltas(application,
				serializedState);

		assertEquals("name", window.getLabel());

		applyAll(deltas);

		assertEquals("customName", window.getLabel());
	}

	public void testApplicationElement_Id_Changed2() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setElementId("id");
		window.setLabel("name");

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setLabel("customName");

		Object serializedState = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		window.setElementId("id2");

		Collection<ModelDelta> deltas = constructDeltas(application,
				serializedState);

		assertEquals("name", window.getLabel());

		applyAll(deltas);

		assertEquals("customName", window.getLabel());
	}

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

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
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

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
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

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
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

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setToBeRendered(true);
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setToBeRendered(true);
		MPart partD = BasicFactoryImpl.eINSTANCE.createPart();
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

		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
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

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();

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

		MPart partD = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partD);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		List<MWindowElement> children = window.getChildren();
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

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();

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

		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partC);

		Collection<ModelDelta> deltas = constructDeltas(application,
				serializedState);

		assertEquals(3, window.getChildren().size());
		assertEquals(partA, window.getChildren().get(0));
		assertEquals(partB, window.getChildren().get(1));
		assertEquals(partC, window.getChildren().get(2));

		applyAll(deltas);

		List<MWindowElement> children = window.getChildren();
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

		MPartStack stack1 = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack1);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		stack1.getChildren().add(part1);
		stack1.getChildren().add(part2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPartStack stack2 = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(0, stack2);
		stack2.getChildren().add(part1);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack1 = (MPartStack) window.getChildren().get(0);
		part1 = (MPart) stack1.getChildren().get(0);
		part2 = (MPart) stack1.getChildren().get(1);

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

		MPartStack stack1 = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack1);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		stack1.getChildren().add(part1);
		stack1.getChildren().add(part2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPartStack stack2 = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack2);
		stack2.getChildren().add(part1);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack1 = (MPartStack) window.getChildren().get(0);
		part1 = (MPart) stack1.getChildren().get(0);
		part2 = (MPart) stack1.getChildren().get(1);

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

	/**
	 * <ol>
	 * <li>The application has a window with a stack that houses three parts, A,
	 * B, and C.</li>
	 * <li>The user creates a new stack to the right of the original stack and
	 * places part A in it.</li>
	 * <li>The new application has a window with two stacks, the new stack in
	 * front of the original housing part C, and the original stack now housing
	 * parts A and B only.</li>
	 * </ol>
	 */
	private void testPartStack_AdditionInBack_ApplicationHasNewStackInFront(
			boolean performMoveFirst) {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		MPartStack stack1 = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack1);

		// stack with three children in it
		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
		stack1.getChildren().add(partA);
		stack1.getChildren().add(partB);
		stack1.getChildren().add(partC);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPartStack stack2 = BasicFactoryImpl.eINSTANCE.createPartStack();
		// add a new stack at the end of the existing stack
		window.getChildren().add(stack2);
		// put A in it
		stack2.getChildren().add(partA);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack1 = (MPartStack) window.getChildren().get(0);
		partA = (MPart) stack1.getChildren().get(0);
		partB = (MPart) stack1.getChildren().get(1);
		partC = (MPart) stack1.getChildren().get(2);

		// create a new stack
		MPartStack stack3 = BasicFactoryImpl.eINSTANCE.createPartStack();

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

		assertEquals(stack3, window.getChildren().get(0));
		assertEquals(stack1, window.getChildren().get(1));

		assertEquals(1, stack1.getChildren().size());
		assertEquals(partB, stack1.getChildren().get(0));

		assertEquals(1, stack3.getChildren().size());
		assertEquals(partC, stack3.getChildren().get(0));

		stack2 = (MPartStack) window.getChildren().get(2);
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

		MPartStack stack1 = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack1);

		// stack with three children in it
		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
		stack1.getChildren().add(partA);
		stack1.getChildren().add(partB);
		stack1.getChildren().add(partC);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPartStack stack2 = BasicFactoryImpl.eINSTANCE.createPartStack();
		// add a new stack to the left of the existing stack
		window.getChildren().add(0, stack2);
		// put A in it
		stack2.getChildren().add(partA);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack1 = (MPartStack) window.getChildren().get(0);
		partA = (MPart) stack1.getChildren().get(0);
		partB = (MPart) stack1.getChildren().get(1);
		partC = (MPart) stack1.getChildren().get(2);

		// create a new stack
		MPartStack stack3 = BasicFactoryImpl.eINSTANCE.createPartStack();

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

		MPartStack stack1 = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack1);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		stack1.getChildren().add(partA);
		stack1.getChildren().add(partB);

		MPartStack stack2 = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack2);

		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
		stack2.getChildren().add(partC);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		stack2.getChildren().add(partB);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		stack1 = (MPartStack) window.getChildren().get(0);
		partA = (MPart) stack1.getChildren().get(0);
		partB = (MPart) stack1.getChildren().get(1);

		stack2 = (MPartStack) window.getChildren().get(1);
		partC = (MPart) stack2.getChildren().get(0);

		MPart partD = BasicFactoryImpl.eINSTANCE.createPart();
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

	public void testElementContainer_ActiveChild_New() {
		MApplication application = createApplication();
		MWindow window1 = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window1);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MWindow window2 = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window2);
		application.setSelectedElement(window2);

		Object state = reconciler.serialize();

		application = createApplication();
		window1 = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertNull(application.getSelectedElement());
		assertEquals(window1, application.getChildren().get(0));

		applyAll(deltas);

		assertEquals(2, application.getChildren().size());

		assertEquals(window1, application.getChildren().get(0));
		assertNotNull(application.getChildren().get(1));
		assertEquals(application.getChildren().get(1),
				application.getSelectedElement());
	}

	public void testElementContainer_ActiveChild_Removed() {
		MApplication application = createApplication();
		MWindow window1 = BasicFactoryImpl.eINSTANCE.createWindow();
		MWindow window2 = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window1);
		application.getChildren().add(window2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		application.setSelectedElement(window2);

		Object state = reconciler.serialize();

		application = createApplication();
		window1 = application.getChildren().get(0);

		application.getChildren().remove(1);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertNull(application.getSelectedElement());
		assertEquals(window1, application.getChildren().get(0));

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertNull(application.getSelectedElement());
		assertEquals(window1, application.getChildren().get(0));
	}

	public void testElementContainer_ActiveChild_Removed2() {
		MApplication application = createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack partStack1 = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStack1);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		partStack1.getChildren().add(part1);
		partStack1.getChildren().add(part2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPartStack partStack2 = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStack2);

		partStack2.getChildren().add(part2);
		partStack2.setSelectedElement(part2);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		partStack1 = (MPartStack) window.getChildren().get(0);
		part1 = (MPart) partStack1.getChildren().get(0);

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
		assertNull(partStack2.getSelectedElement());
	}

	public void testElementContainer_Children_Move_IdenticalToUserChange() {
		MApplication application = createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack partStack1 = BasicFactoryImpl.eINSTANCE.createPartStack();
		MPartStack partStack2 = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStack1);
		window.getChildren().add(partStack2);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		partStack1.getChildren().add(part1);
		partStack1.getChildren().add(part2);

		MPart part3 = BasicFactoryImpl.eINSTANCE.createPart();
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

		part1 = (MPart) partStack1.getChildren().get(0);
		part2 = (MPart) partStack1.getChildren().get(1);
		part3 = (MPart) partStack2.getChildren().get(0);

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

	public void testElementContainer_Children_Move_NewHasSameChildren() {
		MApplication application = createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack partStack1 = BasicFactoryImpl.eINSTANCE.createPartStack();
		MPartStack partStack2 = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStack1);
		window.getChildren().add(partStack2);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		partStack1.getChildren().add(part1);
		partStack1.getChildren().add(part2);

		MPart part3 = BasicFactoryImpl.eINSTANCE.createPart();
		partStack2.getChildren().add(part3);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		partStack2.getChildren().add(0, part2);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		partStack1 = (MPartStack) window.getChildren().get(0);
		partStack2 = (MPartStack) window.getChildren().get(1);

		part1 = (MPart) partStack1.getChildren().get(0);
		part2 = (MPart) partStack1.getChildren().get(1);
		part3 = (MPart) partStack2.getChildren().get(0);

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
		assertEquals(part2, partStack2.getChildren().get(0));
		assertEquals(part3, partStack2.getChildren().get(1));
	}

	/**
	 * <ol>
	 * <li>Initially, the application has one window, A.</li>
	 * <li>The adds a window B, and places an editor in window B.</li>
	 * <li>The user then removes window B.</li>
	 * <li>The merged outcome should be only one window, window A.</li>
	 * </ol>
	 */
	public void testElementContainer_Children_AddMultipleThenRemove() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MWindow window2 = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window2);

		MPart editor = BasicFactoryImpl.eINSTANCE.createPart();
		window2.getChildren().add(editor);

		application.getChildren().remove(window2);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);
		assertEquals(0, deltas.size());

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(0, window.getChildren().size());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(0, window.getChildren().size());
	}

	/**
	 * <ol>
	 * <li>Initially, the application has one window, A.</li>
	 * <li>The adds a window B, and sets it as the active window.</li>
	 * <li>The user then removes window B.</li>
	 * <li>The merged outcome should be only one window, window A, and the
	 * application should not have an active window.</li>
	 * </ol>
	 */
	public void testElementContainer_Children_AddMultipleThenRemove2() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MWindow window2 = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window2);
		application.setSelectedElement(window2);

		application.getChildren().remove(window2);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertNull(application.getSelectedElement());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(0, window.getChildren().size());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertNull(application.getSelectedElement());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(0, window.getChildren().size());
	}

	/**
	 * <ol>
	 * <li>Initially, the application has one window, A.</li>
	 * <li>The adds a window B, and places an editor in window B.</li>
	 * <li>The user then sets a name to the editor, "<code>editor</code>".</li>
	 * <li>The user then removes window B.</li>
	 * <li>The merged outcome should be only one window, window A.</li>
	 * </ol>
	 */
	public void testElementContainer_Children_AddMultipleThenRemove3() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MWindow window2 = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window2);

		MPart editor = BasicFactoryImpl.eINSTANCE.createPart();
		window2.getChildren().add(editor);
		editor.setLabel("editor");

		application.getChildren().remove(window2);

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

		assertEquals(0, window.getChildren().size());
	}

	public void testMenu_MenuOrdering() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);
		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		window.setMainMenu(menu);

		MMenuItem fileMenuItem = MenuFactoryImpl.eINSTANCE
				.createDirectMenuItem();
		fileMenuItem.setLabel("File");

		MMenuItem editMenuItem = MenuFactoryImpl.eINSTANCE
				.createDirectMenuItem();
		editMenuItem.setLabel("Edit");

		MMenuItem helpMenuItem = MenuFactoryImpl.eINSTANCE
				.createDirectMenuItem();
		helpMenuItem.setLabel("Help");

		menu.getChildren().add(fileMenuItem);
		menu.getChildren().add(editMenuItem);
		menu.getChildren().add(helpMenuItem);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MMenuItem cvsMenuItem = MenuFactoryImpl.eINSTANCE
				.createDirectMenuItem();
		cvsMenuItem.setLabel("CVS");
		menu.getChildren().add(2, cvsMenuItem);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		menu = window.getMainMenu();
		fileMenuItem = (MMenuItem) menu.getChildren().get(0);
		editMenuItem = (MMenuItem) menu.getChildren().get(1);
		helpMenuItem = (MMenuItem) menu.getChildren().get(2);

		MMenuItem e4MenuItem = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		e4MenuItem.setLabel("e4");
		menu.getChildren().add(2, e4MenuItem);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(menu, window.getMainMenu());

		assertEquals(4, menu.getChildren().size());
		assertEquals(fileMenuItem, menu.getChildren().get(0));
		assertEquals(editMenuItem, menu.getChildren().get(1));
		assertEquals(e4MenuItem, menu.getChildren().get(2));
		assertEquals(helpMenuItem, menu.getChildren().get(3));

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(menu, window.getMainMenu());

		assertEquals(5, menu.getChildren().size());
		assertEquals(fileMenuItem, menu.getChildren().get(0));
		assertEquals(editMenuItem, menu.getChildren().get(1));
		assertEquals("CVS", menu.getChildren().get(2).getLabel());
		assertEquals(e4MenuItem, menu.getChildren().get(3));
		assertEquals(helpMenuItem, menu.getChildren().get(4));
	}

	/**
	 * Tests that the addition of a part to a window and the alteration of the
	 * window's main menu will be reconciled appropriately.
	 */
	public void testWindow_AddPartAndChangeMenu() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);
		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		window.setMainMenu(menu);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		menu.setLabel("menuLabel");

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		menu = window.getMainMenu();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(menu, window.getMainMenu());
		assertNull(menu.getLabel());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(menu, window.getMainMenu());
		assertEquals("menuLabel", menu.getLabel());

		assertEquals(1, window.getChildren().size());
		assertTrue(window.getChildren().get(0) instanceof MPart);
	}

	public void testBug338707() {
		MApplication application = createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartSashContainer container = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(container);
		window.setSelectedElement(container);

		MPartStack partStackA = BasicFactoryImpl.eINSTANCE.createPartStack();
		container.getChildren().add(partStackA);

		MPartStack partStackB = BasicFactoryImpl.eINSTANCE.createPartStack();
		container.getChildren().add(partStackB);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPartSashContainer newContainer = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		MPartStack partStackC = BasicFactoryImpl.eINSTANCE.createPartStack();
		newContainer.getChildren().add(partStackC);
		newContainer.getChildren().add(partStackB);

		container.getChildren().add(newContainer);
		container.getChildren().remove(partStackA);

		container.getChildren().remove(newContainer);
		window.getChildren().add(newContainer);
		window.getChildren().remove(container);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		container = (MPartSashContainer) window.getChildren().get(0);
		partStackA = (MPartStack) container.getChildren().get(0);
		partStackB = (MPartStack) container.getChildren().get(1);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getChildren().size());
		assertEquals(container, window.getChildren().get(0));

		assertEquals(2, container.getChildren().size());
		assertEquals(partStackA, container.getChildren().get(0));
		assertEquals(partStackB, container.getChildren().get(1));

		applyAll(deltas);

		newContainer = (MPartSashContainer) window.getChildren().get(0);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));

		assertEquals(1, window.getChildren().size());
		assertNotNull(newContainer);
		assertFalse(container == newContainer);

		assertEquals(0, container.getChildren().size());

		assertEquals(2, newContainer.getChildren().size());
		assertNotNull(newContainer.getChildren().get(0));
		assertFalse(partStackA == newContainer.getChildren().get(0));
		assertEquals(partStackB, newContainer.getChildren().get(1));
	}

	/**
	 * Test to ensure that the <tt>persistedState</tt> attribute of an
	 * <tt>MAddon</tt> is recognized as a delta.
	 */
	private void testBug361851(String originalValue, String newValue) {
		MApplication application = createApplication();

		MAddon addon = ApplicationFactoryImpl.eINSTANCE.createAddon();
		application.getAddons().add(addon);
		addon.getPersistedState().put("key", originalValue);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		addon.getPersistedState().put("key", newValue);

		Object state = reconciler.serialize();

		application = createApplication();
		addon = application.getAddons().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(originalValue, addon.getPersistedState().get("key"));

		applyAll(deltas);

		assertEquals(newValue, addon.getPersistedState().get("key"));
	}

	public void testBug361851_NullNull() {
		testBug361851(null, null);
	}

	public void testBug361851_NullEmpty() {
		testBug361851(null, "");
	}

	public void testBug361851_NullString() {
		testBug361851(null, "string");
	}

	public void testBug361851_EmptyNull() {
		testBug361851("", null);
	}

	public void testBug361851_EmptyEmpty() {
		testBug361851("", "");
	}

	public void testBug361851_EmptyString() {
		testBug361851("", "string");
	}

	public void testBug361851_StringNull() {
		testBug361851("string", null);
	}

	public void testBug361851_StringEmpty() {
		testBug361851("string", "");
	}

	public void testBug361851_StringStringUnchanged() {
		testBug361851("string", "string");
	}

	public void testBug361851_StringStringChanged() {
		testBug361851("string", "string2");
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

		MBindingTable bindingTable = CommandsFactoryImpl.eINSTANCE
				.createBindingTable();
		MBindingTable bindingTable2 = CommandsFactoryImpl.eINSTANCE
				.createBindingTable();
		application.getBindingTables().add(bindingTable);
		application.getBindingTables().add(bindingTable2);

		MCommand command = CommandsFactoryImpl.eINSTANCE.createCommand();
		application.getCommands().add(command);

		MKeyBinding keyBinding = CommandsFactoryImpl.eINSTANCE
				.createKeyBinding();
		keyBinding.setCommand(command);
		keyBinding.setKeySequence(originalApplicationKeyBindingSequence);

		MKeyBinding keyBinding2 = CommandsFactoryImpl.eINSTANCE
				.createKeyBinding();
		keyBinding2.setCommand(command);
		keyBinding2.setKeySequence(originalWindowKeyBindingSequence);

		bindingTable.getBindings().add(keyBinding);
		bindingTable2.getBindings().add(keyBinding2);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		keyBinding.setKeySequence(userApplicationKeyBindingSequence);
		keyBinding2.setKeySequence(userWindowKeyBindingSequence);

		Object state = reconciler.serialize();

		application = createApplication();
		bindingTable = application.getBindingTables().get(0);
		bindingTable2 = application.getBindingTables().get(1);

		command = application.getCommands().get(0);

		keyBinding = bindingTable.getBindings().get(0);
		keyBinding2 = bindingTable2.getBindings().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(originalApplicationKeyBindingSequence,
				keyBinding.getKeySequence());
		assertEquals(originalWindowKeyBindingSequence,
				keyBinding2.getKeySequence());

		applyAll(deltas);

		assertEquals(userApplicationKeyBindingSequence,
				keyBinding.getKeySequence());
		assertEquals(userWindowKeyBindingSequence, keyBinding2.getKeySequence());
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
