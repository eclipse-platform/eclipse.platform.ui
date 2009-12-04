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

public abstract class ModelReconcilerApplicationTest extends
		ModelReconcilerTest {

	public void testApplication_Commands_Add() {
		String applicationId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		String commandId = createId();
		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		command.setCommandName("newCommand");
		application.getCommands().add(command);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, application.getCommands().size());

		applyAll(deltas);

		assertEquals(1, application.getCommands().size());

		command = application.getCommands().get(0);
		assertEquals(commandId, command.getId());
		assertEquals("newCommand", command.getCommandName());
	}

	public void testApplication_Commands_Remove() {
		String applicationId = createId();
		String commandId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		application.getCommands().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getCommands().size());
		command = application.getCommands().get(0);
		assertEquals(commandId, command.getId());

		applyAll(deltas);

		assertEquals(0, application.getCommands().size());
	}
}
