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
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerBindingContainerTest extends
		ModelReconcilerTest {

	private void testBindingContainer_Add_KeyBinding(String keySequence)
			throws Exception {
		String applicationId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MKeyBinding keyBinding = MApplicationFactory.eINSTANCE
				.createKeyBinding();
		keyBinding.setKeySequence(keySequence);
		application.getBindings().add(keyBinding);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(0, application.getBindings().size());

		applyAll(deltas);

		assertEquals(1, application.getBindings().size());

		keyBinding = application.getBindings().get(0);
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
		String applicationId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MKeyBinding keyBinding = MApplicationFactory.eINSTANCE
				.createKeyBinding();
		keyBinding.setKeySequence(keySequence);
		application.getBindings().add(keyBinding);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		application.getBindings().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		keyBinding = MApplicationFactory.eINSTANCE.createKeyBinding();
		keyBinding.setKeySequence(keySequence);
		application.getBindings().add(keyBinding);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(1, application.getBindings().size());

		keyBinding = application.getBindings().get(0);
		assertNull(keyBinding.getCommand());
		assertEquals(keySequence, keyBinding.getKeySequence());
		assertEquals(0, keyBinding.getParameters().size());

		applyAll(deltas);

		assertEquals(0, application.getBindings().size());
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
		String applicationId = createId();
		String commandId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MKeyBinding keyBinding = MApplicationFactory.eINSTANCE
				.createKeyBinding();
		keyBinding.setKeySequence(keySequence);
		keyBinding.setCommand(command);
		application.getBindings().add(keyBinding);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(0, application.getBindings().size());

		applyAll(deltas);

		assertEquals(1, application.getBindings().size());

		keyBinding = application.getBindings().get(0);
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
		String applicationId = createId();
		String commandId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		MKeyBinding keyBinding = MApplicationFactory.eINSTANCE
				.createKeyBinding();
		keyBinding.setKeySequence(keySequence);
		keyBinding.setCommand(command);
		application.getBindings().add(keyBinding);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		application.getBindings().remove(keyBinding);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		keyBinding = MApplicationFactory.eINSTANCE.createKeyBinding();
		keyBinding.setKeySequence(keySequence);
		keyBinding.setCommand(command);
		application.getBindings().add(keyBinding);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(1, application.getBindings().size());

		keyBinding = application.getBindings().get(0);
		assertEquals(command, keyBinding.getCommand());
		assertEquals(keySequence, keyBinding.getKeySequence());
		assertEquals(0, keyBinding.getParameters().size());

		applyAll(deltas);

		assertEquals(0, application.getBindings().size());
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
