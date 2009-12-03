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
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerCommandTest extends ModelReconcilerTest {

	private void testCommand_CommandName(String before, String after) {
		String applicationId = createId();
		String commandId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		command.setCommandName(before);
		application.getCommands().add(command);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		command.setCommandName(after);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		command.setCommandName(before);
		application.getCommands().add(command);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(before, command.getCommandName());

		applyAll(deltas);

		assertEquals(after, command.getCommandName());
	}

	public void testCommand_CommandName_NullNull() {
		testCommand_CommandName(null, null);
	}

	public void testCommand_CommandName_NullEmpty() {
		testCommand_CommandName(null, "");
	}

	public void testCommand_CommandName_NullString() {
		testCommand_CommandName(null, "name");
	}

	public void testCommand_CommandName_EmptyNull() {
		testCommand_CommandName("", null);
	}

	public void testCommand_CommandName_EmptyEmpty() {
		testCommand_CommandName("", "");
	}

	public void testCommand_CommandName_EmptyString() {
		testCommand_CommandName("", "name");
	}

	public void testCommand_CommandName_StringNull() {
		testCommand_CommandName("name", null);
	}

	public void testCommand_CommandName_StringEmpty() {
		testCommand_CommandName("name", "");
	}

	public void testCommand_CommandName_StringStringUnchanged() {
		testCommand_CommandName("name", "name");
	}

	public void testCommand_CommandName_StringStringChanged() {
		testCommand_CommandName("name", "name2");
	}

	private void testCommand_Description(String before, String after) {
		String applicationId = createId();
		String commandId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);
		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		command.setDescription(before);
		application.getCommands().add(command);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		command.setDescription(after);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);
		command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		command.setDescription(before);
		application.getCommands().add(command);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(before, command.getDescription());

		applyAll(deltas);

		assertEquals(after, command.getDescription());
	}

	public void testCommand_Description_NullNull() {
		testCommand_Description(null, null);
	}

	public void testCommand_Description_NullEmpty() {
		testCommand_Description(null, "");
	}

	public void testCommand_Description_NullString() {
		testCommand_Description(null, "description");
	}

	public void testCommand_Description_EmptyNull() {
		testCommand_Description("", null);
	}

	public void testCommand_Description_EmptyEmpty() {
		testCommand_Description("", "");
	}

	public void testCommand_Description_EmptyString() {
		testCommand_Description("", "description");
	}

	public void testCommand_Description_StringNull() {
		testCommand_Description("description", null);
	}

	public void testCommand_Description_StringEmpty() {
		testCommand_Description("description", "");
	}

	public void testCommand_Description_StringStringUnchanged() {
		testCommand_Description("description", "description");
	}

	public void testCommand_Description_StringStringChanged() {
		testCommand_Description("description", "description2");
	}
}
