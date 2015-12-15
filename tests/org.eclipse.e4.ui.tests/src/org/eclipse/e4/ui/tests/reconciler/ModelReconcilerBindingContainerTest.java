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
import static org.junit.Assert.assertNull;

import java.util.Collection;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.junit.Test;

public abstract class ModelReconcilerBindingContainerTest extends
		ModelReconcilerTest {

	@Test
	public void testBindingContainer_Add() {
		MApplication application = createApplication();

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MBindingTable bindingTable = ems.createModelElement(MBindingTable.class);
		application.getBindingTables().add(bindingTable);

		Object state = reconciler.serialize();

		application = createApplication();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, application.getBindingTables().size());

		applyAll(deltas);

		assertEquals(1, application.getBindingTables().size());
	}

	@Test
	public void testBindingContainer_Remove() {
		MApplication application = createApplication();

		MBindingTable bindingTable = ems.createModelElement(MBindingTable.class);
		application.getBindingTables().add(bindingTable);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		application.getBindingTables().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getBindingTables().size());

		applyAll(deltas);

		assertEquals(0, application.getBindingTables().size());
	}

	private void testBindingContainer_Add_KeyBinding(String keySequence)
			throws Exception {
		MApplication application = createApplication();

		MBindingTable bindingTable = ems.createModelElement(MBindingTable.class);
		application.getBindingTables().add(bindingTable);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MKeyBinding keyBinding = ems.createModelElement(MKeyBinding.class);
		keyBinding.setKeySequence(keySequence);
		bindingTable.getBindings().add(keyBinding);

		Object state = reconciler.serialize();

		application = createApplication();
		bindingTable = application.getBindingTables().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, bindingTable.getBindings().size());

		applyAll(deltas);

		assertEquals(1, bindingTable.getBindings().size());

		keyBinding = bindingTable.getBindings().get(0);
		assertNull(keyBinding.getCommand());
		assertEquals(keySequence, keyBinding.getKeySequence());
		assertEquals(0, keyBinding.getParameters().size());
	}

	@Test
	public void testBindingContainer_Add_KeyBinding_Null() throws Exception {
		testBindingContainer_Add_KeyBinding(null);
	}

	@Test
	public void testBindingContainer_Add_KeyBinding_Empty() throws Exception {
		testBindingContainer_Add_KeyBinding("");
	}

	@Test
	public void testBindingContainer_Add_KeyBinding_String() throws Exception {
		testBindingContainer_Add_KeyBinding("Ctrl+S");
	}

	private void testBindingContainer_Remove_KeyBinding(String keySequence)
			throws Exception {
		MApplication application = createApplication();

		MBindingTable bindingTable = ems.createModelElement(MBindingTable.class);
		application.getBindingTables().add(bindingTable);

		MKeyBinding keyBinding = ems.createModelElement(MKeyBinding.class);
		keyBinding.setKeySequence(keySequence);
		bindingTable.getBindings().add(keyBinding);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		bindingTable.getBindings().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		bindingTable = application.getBindingTables().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, bindingTable.getBindings().size());

		keyBinding = bindingTable.getBindings().get(0);
		assertNull(keyBinding.getCommand());
		assertEquals(keySequence, keyBinding.getKeySequence());
		assertEquals(0, keyBinding.getParameters().size());

		applyAll(deltas);

		assertEquals(0, bindingTable.getBindings().size());
	}

	@Test
	public void testBindingContainer_Remove_KeyBinding_Null() throws Exception {
		testBindingContainer_Remove_KeyBinding(null);
	}

	@Test
	public void testBindingContainer_Remove_KeyBinding_Empty() throws Exception {
		testBindingContainer_Remove_KeyBinding("");
	}

	@Test
	public void testBindingContainer_Remove_KeyBinding_String()
			throws Exception {
		testBindingContainer_Remove_KeyBinding("Ctrl+S");
	}

	private void testBindingContainer_Add_BoundKeyBinding(String keySequence)
			throws Exception {
		MApplication application = createApplication();

		MBindingTable bindingTable = ems.createModelElement(MBindingTable.class);
		application.getBindingTables().add(bindingTable);

		MCommand command = ems.createModelElement(MCommand.class);
		application.getCommands().add(command);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MKeyBinding keyBinding = ems.createModelElement(MKeyBinding.class);
		keyBinding.setKeySequence(keySequence);
		keyBinding.setCommand(command);
		bindingTable.getBindings().add(keyBinding);

		Object state = reconciler.serialize();

		application = createApplication();
		bindingTable = application.getBindingTables().get(0);
		command = application.getCommands().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, bindingTable.getBindings().size());

		applyAll(deltas);

		assertEquals(1, bindingTable.getBindings().size());

		keyBinding = bindingTable.getBindings().get(0);
		assertEquals(command, keyBinding.getCommand());
		assertEquals(keySequence, keyBinding.getKeySequence());
	}

	@Test
	public void testBindingContainer_Add_BoundKeyBinding_Null()
			throws Exception {
		testBindingContainer_Add_BoundKeyBinding(null);
	}

	@Test
	public void testBindingContainer_Add_BoundKeyBinding_Empty()
			throws Exception {
		testBindingContainer_Add_BoundKeyBinding("");
	}

	@Test
	public void testBindingContainer_Add_BoundKeyBinding_String()
			throws Exception {
		testBindingContainer_Add_BoundKeyBinding("Ctrl+S");
	}

	private void testBindingContainer_Remove_BoundKeyBinding(String keySequence)
			throws Exception {
		MApplication application = createApplication();

		MBindingTable bindingTable = ems.createModelElement(MBindingTable.class);
		application.getBindingTables().add(bindingTable);

		MCommand command = ems.createModelElement(MCommand.class);
		application.getCommands().add(command);

		MKeyBinding keyBinding = ems.createModelElement(MKeyBinding.class);
		keyBinding.setKeySequence(keySequence);
		keyBinding.setCommand(command);
		bindingTable.getBindings().add(keyBinding);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		bindingTable.getBindings().remove(keyBinding);

		Object state = reconciler.serialize();

		application = createApplication();
		bindingTable = application.getBindingTables().get(0);
		command = application.getCommands().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, bindingTable.getBindings().size());

		keyBinding = bindingTable.getBindings().get(0);
		assertEquals(command, keyBinding.getCommand());
		assertEquals(keySequence, keyBinding.getKeySequence());
		assertEquals(0, keyBinding.getParameters().size());

		applyAll(deltas);

		assertEquals(0, bindingTable.getBindings().size());
	}

	@Test
	public void testBindingContainer_Remove_BoundKeyBinding_Null()
			throws Exception {
		testBindingContainer_Remove_BoundKeyBinding(null);
	}

	@Test
	public void testBindingContainer_Remove_BoundKeyBinding_Empty()
			throws Exception {
		testBindingContainer_Remove_BoundKeyBinding("");
	}

	@Test
	public void testBindingContainer_Remove_BoundKeyBinding_String()
			throws Exception {
		testBindingContainer_Remove_BoundKeyBinding("Ctrl+S");
	}

}
