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
		String applicationId = createId();
		String windowId = createId();
		String partId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setName("name");
		part.setId(partId);

		window.getChildren().add(part);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setName("customName");

		Object serializedState = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setName("name2");
		part.setId(partId);

		window.getChildren().add(part);

		Collection<ModelDelta> deltas = constructDeltas(application,
				serializedState);

		assertEquals("name2", part.getName());

		applyAll(deltas);

		assertEquals("customName", part.getName());
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
		String applicationId = createId();
		String windowId = createId();
		String partId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setName("name");
		part.setId(partId);
		part.setVisible(true);

		window.getChildren().add(part);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setName("customName");

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setName("name2");
		part.setId(partId);
		part.setVisible(false);

		window.getChildren().add(part);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertFalse(part.isVisible());
		assertEquals("name2", part.getName());

		applyAll(deltas);

		// the user's change should not have made this part visible
		assertFalse(part.isVisible());
		// the user's change should have been applied
		assertEquals("customName", part.getName());
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
		String applicationId = createId();
		String windowId = createId();
		String partId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setName("name");
		part.setId(partId);
		part.setVisible(true);

		window.getChildren().add(part);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setVisible(false);

		Object serializedState = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setName("name2");
		part.setId(partId);
		part.setVisible(true);

		window.getChildren().add(part);

		Collection<ModelDelta> deltas = constructDeltas(application,
				serializedState);

		assertTrue(part.isVisible());
		assertEquals("name2", part.getName());

		applyAll(deltas);

		assertFalse(part.isVisible());
		// the application's change should not have been overridden
		assertEquals("name2", part.getName());
	}

	public void testPart_Addition_PlacedAfterHiddenPart_UserWins() {
		String applicationId = createId();
		String windowId = createId();
		String partAId = createId();
		String partBId = createId();
		String partCId = createId();
		String partDId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId(partAId);
		partA.setVisible(true);
		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setId(partBId);
		partB.setVisible(true);
		MPart partD = MApplicationFactory.eINSTANCE.createPart();
		partD.setId(partDId);
		partD.setVisible(true);

		window.getChildren().add(partA);
		window.getChildren().add(partB);
		window.getChildren().add(partD);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		partB.setVisible(false);

		Object serializedState = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId(partAId);
		partA.setVisible(true);
		partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setId(partBId);
		partB.setVisible(true);
		MPart partC = MApplicationFactory.eINSTANCE.createPart();
		partC.setId(partCId);
		partC.setVisible(true);
		partD = MApplicationFactory.eINSTANCE.createPart();
		partD.setId(partDId);
		partD.setVisible(true);

		window.getChildren().add(partA);
		window.getChildren().add(partB);
		window.getChildren().add(partC);
		window.getChildren().add(partD);

		Collection<ModelDelta> deltas = constructDeltas(application,
				serializedState);

		assertTrue(partA.isVisible());
		assertTrue(partB.isVisible());
		assertTrue(partC.isVisible());
		assertTrue(partD.isVisible());

		applyAll(deltas);

		assertTrue(partA.isVisible());
		assertFalse(partB.isVisible());
		assertTrue(partC.isVisible());
		assertTrue(partD.isVisible());
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
		String applicationId = createId();
		String windowId = createId();
		String partAId = createId();
		String partBId = createId();
		String partCId = createId();
		String partDId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId(partAId);
		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setId(partBId);
		MPart partC = MApplicationFactory.eINSTANCE.createPart();
		partC.setId(partCId);

		window.getChildren().add(partA);
		window.getChildren().add(partB);
		window.getChildren().add(partC);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.getChildren().remove(partB);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId(partAId);
		partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setId(partBId);
		partC = MApplicationFactory.eINSTANCE.createPart();
		partC.setId(partCId);
		MPart partD = MApplicationFactory.eINSTANCE.createPart();
		partD.setId(partDId);

		window.getChildren().add(partA);
		window.getChildren().add(partB);
		window.getChildren().add(partC);
		window.getChildren().add(partD);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

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
		String applicationId = createId();
		String windowId = createId();
		String partAId = createId();
		String partBId = createId();
		String partCId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId(partAId);
		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setId(partBId);

		window.getChildren().add(partA);
		window.getChildren().add(partB);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.getChildren().remove(partA);
		window.getChildren().remove(partB);

		Object serializedState = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId(partAId);
		partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setId(partBId);
		MPart partC = MApplicationFactory.eINSTANCE.createPart();
		partC.setId(partCId);

		window.getChildren().add(partA);
		window.getChildren().add(partB);
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

	public void testBindingContainer_NewWithBindings() {
		String applicationId = createId();
		String windowId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MKeyBinding keyBinding = MApplicationFactory.eINSTANCE
				.createKeyBinding();
		window.getBindings().add(keyBinding);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(0, application.getChildren().size());
		assertEquals(0, application.getBindings().size());

		applyAll(deltas);

		window = application.getChildren().get(0);
		assertEquals(windowId, window.getId());
		assertEquals(1, window.getBindings().size());
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
		String applicationId = createId();
		String windowId = createId();
		String commandId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);
		MWindow window = createWindow(application);
		window.setId(windowId);

		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
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

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		applicationKeyBinding.setKeySequence(userApplicationKeyBindingSequence);
		windowKeyBinding.setKeySequence(userWindowKeyBindingSequence);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);
		window = createWindow(application);
		window.setId(windowId);

		command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		applicationKeyBinding = MApplicationFactory.eINSTANCE
				.createKeyBinding();
		applicationKeyBinding.setCommand(command);
		applicationKeyBinding
				.setKeySequence(originalApplicationKeyBindingSequence);

		windowKeyBinding = MApplicationFactory.eINSTANCE.createKeyBinding();
		windowKeyBinding.setCommand(command);
		windowKeyBinding.setKeySequence(originalWindowKeyBindingSequence);

		application.getBindings().add(applicationKeyBinding);
		window.getBindings().add(windowKeyBinding);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

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
