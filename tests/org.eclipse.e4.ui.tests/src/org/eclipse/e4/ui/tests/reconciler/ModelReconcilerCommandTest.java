/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.reconciler;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.junit.Test;

public abstract class ModelReconcilerCommandTest extends ModelReconcilerTest {

	private void testCommand_CommandName(String before, String after) {
		MApplication application = createApplication();

		MCommand command = ems.createModelElement(MCommand.class);
		command.setCommandName(before);
		application.getCommands().add(command);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		command.setCommandName(after);

		Object state = reconciler.serialize();

		application = createApplication();
		command = application.getCommands().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, command.getCommandName());

		applyAll(deltas);

		assertEquals(after, command.getCommandName());
	}

	@Test
	public void testCommand_CommandName_NullNull() {
		testCommand_CommandName(null, null);
	}

	@Test
	public void testCommand_CommandName_NullEmpty() {
		testCommand_CommandName(null, "");
	}

	@Test
	public void testCommand_CommandName_NullString() {
		testCommand_CommandName(null, "name");
	}

	@Test
	public void testCommand_CommandName_EmptyNull() {
		testCommand_CommandName("", null);
	}

	@Test
	public void testCommand_CommandName_EmptyEmpty() {
		testCommand_CommandName("", "");
	}

	@Test
	public void testCommand_CommandName_EmptyString() {
		testCommand_CommandName("", "name");
	}

	@Test
	public void testCommand_CommandName_StringNull() {
		testCommand_CommandName("name", null);
	}

	@Test
	public void testCommand_CommandName_StringEmpty() {
		testCommand_CommandName("name", "");
	}

	@Test
	public void testCommand_CommandName_StringStringUnchanged() {
		testCommand_CommandName("name", "name");
	}

	@Test
	public void testCommand_CommandName_StringStringChanged() {
		testCommand_CommandName("name", "name2");
	}

	private void testCommand_Description(String before, String after) {
		MApplication application = createApplication();
		MCommand command = ems.createModelElement(MCommand.class);
		command.setDescription(before);
		application.getCommands().add(command);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		command.setDescription(after);

		Object state = reconciler.serialize();

		application = createApplication();
		command = application.getCommands().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, command.getDescription());

		applyAll(deltas);

		assertEquals(after, command.getDescription());
	}

	@Test
	public void testCommand_Description_NullNull() {
		testCommand_Description(null, null);
	}

	@Test
	public void testCommand_Description_NullEmpty() {
		testCommand_Description(null, "");
	}

	@Test
	public void testCommand_Description_NullString() {
		testCommand_Description(null, "description");
	}

	@Test
	public void testCommand_Description_EmptyNull() {
		testCommand_Description("", null);
	}

	@Test
	public void testCommand_Description_EmptyEmpty() {
		testCommand_Description("", "");
	}

	@Test
	public void testCommand_Description_EmptyString() {
		testCommand_Description("", "description");
	}

	@Test
	public void testCommand_Description_StringNull() {
		testCommand_Description("description", null);
	}

	@Test
	public void testCommand_Description_StringEmpty() {
		testCommand_Description("description", "");
	}

	@Test
	public void testCommand_Description_StringStringUnchanged() {
		testCommand_Description("description", "description");
	}

	@Test
	public void testCommand_Description_StringStringChanged() {
		testCommand_Description("description", "description2");
	}

	@Test
	public void testCommand_Parameters_Add() {
		MApplication application = createApplication();
		MCommand command = ems.createModelElement(MCommand.class);
		application.getCommands().add(command);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MCommandParameter parameter = ems.createModelElement(MCommandParameter.class);
		parameter.setName("parameterName");
		command.getParameters().add(parameter);

		Object state = reconciler.serialize();

		application = createApplication();
		command = application.getCommands().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, command.getParameters().size());

		applyAll(deltas);

		assertEquals(1, command.getParameters().size());
		assertEquals("parameterName", command.getParameters().get(0).getName());
	}

	@Test
	public void testCommand_Parameters_Remove() {
		MApplication application = createApplication();
		MCommand command = ems.createModelElement(MCommand.class);
		application.getCommands().add(command);

		MCommandParameter parameter = ems.createModelElement(MCommandParameter.class);
		parameter.setName("parameterName");
		command.getParameters().add(parameter);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		command.getParameters().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		command = application.getCommands().get(0);
		parameter = command.getParameters().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, command.getParameters().size());
		assertEquals(parameter, command.getParameters().get(0));
		assertEquals("parameterName", parameter.getName());

		applyAll(deltas);

		assertEquals(0, command.getParameters().size());
	}
}
