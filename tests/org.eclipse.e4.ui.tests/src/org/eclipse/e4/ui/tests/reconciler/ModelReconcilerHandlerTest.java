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
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.workbench.modeling.ModelDeltaOperation;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerHandlerTest extends ModelReconcilerTest {

	public void testHandler_Command_Set() {
		String applicationId = createId();
		String commandId = createId();
		String handlerId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		MHandler handler = MApplicationFactory.eINSTANCE.createHandler();
		handler.setId(handlerId);
		application.getHandlers().add(handler);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		handler.setCommand(command);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		handler = MApplicationFactory.eINSTANCE.createHandler();
		handler.setId(handlerId);
		application.getHandlers().add(handler);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		handler = application.getHandlers().get(0);
		assertNull(handler.getCommand());

		applyAll(operations);

		handler = application.getHandlers().get(0);
		assertEquals(command, handler.getCommand());
	}

	public void testHandler_Command_Unset() {
		String applicationId = createId();
		String commandId = createId();
		String handlerId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		MHandler handler = MApplicationFactory.eINSTANCE.createHandler();
		handler.setId(handlerId);
		handler.setCommand(command);
		application.getHandlers().add(handler);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		handler.setCommand(null);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		handler = MApplicationFactory.eINSTANCE.createHandler();
		handler.setId(handlerId);
		handler.setCommand(command);
		application.getHandlers().add(handler);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		handler = application.getHandlers().get(0);
		assertEquals(command, handler.getCommand());

		applyAll(operations);

		handler = application.getHandlers().get(0);
		assertNull(handler.getCommand());
	}
}
