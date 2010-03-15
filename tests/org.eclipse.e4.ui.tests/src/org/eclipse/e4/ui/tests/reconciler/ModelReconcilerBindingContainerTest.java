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
import org.eclipse.e4.ui.model.application.MBindingTable;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MKeyBinding;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerBindingContainerTest extends
		ModelReconcilerTest {

	private void testBindingContainer_Add_KeyBinding(String keySequence)
			throws Exception {
		MApplication application = createApplication();

		MBindingTable bindingTable = MApplicationFactory.eINSTANCE
				.createBindingTable();
		application.getBindingTables().add(bindingTable);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MKeyBinding keyBinding = MApplicationFactory.eINSTANCE
				.createKeyBinding();
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

	public void testBindingContainer_Add_KeyBinding_Null() throws Exception {
		testBindingContainer_Add_KeyBinding(null);
	}

	public void testBindingContainer_Add_KeyBinding_Empty() throws Exception {
		testBindingContainer_Add_KeyBinding("");
	}

	public void testBindingContainer_Add_KeyBinding_String() throws Exception {
		testBindingContainer_Add_KeyBinding("Ctrl+S");
	}

	private void testBindingContainer_Remove_KeyBinding(String keySequence)
			throws Exception {
		MApplication application = createApplication();

		MBindingTable bindingTable = MApplicationFactory.eINSTANCE
				.createBindingTable();
		application.getBindingTables().add(bindingTable);

		MKeyBinding keyBinding = MApplicationFactory.eINSTANCE
				.createKeyBinding();
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

	public void testBindingContainer_Remove_KeyBinding_Null() throws Exception {
		testBindingContainer_Remove_KeyBinding(null);
	}

	public void testBindingContainer_Remove_KeyBinding_Empty() throws Exception {
		testBindingContainer_Remove_KeyBinding("");
	}

	public void testBindingContainer_Remove_KeyBinding_String()
			throws Exception {
		testBindingContainer_Remove_KeyBinding("Ctrl+S");
	}

	private void testBindingContainer_Add_BoundKeyBinding(String keySequence)
			throws Exception {
		MApplication application = createApplication();

		MBindingTable bindingTable = MApplicationFactory.eINSTANCE
				.createBindingTable();
		application.getBindingTables().add(bindingTable);

		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		application.getCommands().add(command);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MKeyBinding keyBinding = MApplicationFactory.eINSTANCE
				.createKeyBinding();
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

	public void testBindingContainer_Add_BoundKeyBinding_Null()
			throws Exception {
		testBindingContainer_Add_BoundKeyBinding(null);
	}

	public void testBindingContainer_Add_BoundKeyBinding_Empty()
			throws Exception {
		testBindingContainer_Add_BoundKeyBinding("");
	}

	public void testBindingContainer_Add_BoundKeyBinding_String()
			throws Exception {
		testBindingContainer_Add_BoundKeyBinding("Ctrl+S");
	}

	private void testBindingContainer_Remove_BoundKeyBinding(String keySequence)
			throws Exception {
		MApplication application = createApplication();

		MBindingTable bindingTable = MApplicationFactory.eINSTANCE
				.createBindingTable();
		application.getBindingTables().add(bindingTable);

		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		application.getCommands().add(command);

		MKeyBinding keyBinding = MApplicationFactory.eINSTANCE
				.createKeyBinding();
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

	public void testBindingContainer_Remove_BoundKeyBinding_Null()
			throws Exception {
		testBindingContainer_Remove_BoundKeyBinding(null);
	}

	public void testBindingContainer_Remove_BoundKeyBinding_Empty()
			throws Exception {
		testBindingContainer_Remove_BoundKeyBinding("");
	}

	public void testBindingContainer_Remove_BoundKeyBinding_String()
			throws Exception {
		testBindingContainer_Remove_BoundKeyBinding("Ctrl+S");
	}

}
