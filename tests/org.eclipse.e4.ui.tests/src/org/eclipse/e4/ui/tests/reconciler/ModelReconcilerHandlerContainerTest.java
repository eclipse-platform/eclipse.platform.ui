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

public abstract class ModelReconcilerHandlerContainerTest extends
		ModelReconcilerTest {

	public void testHandlerContainer_Handlers_Add_UnboundHandler() {
		String applicationId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		String handlerId = createId();
		MHandler handler = MApplicationFactory.eINSTANCE.createHandler();
		handler.setId(handlerId);
		application.getHandlers().add(handler);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		assertEquals(0, application.getHandlers().size());

		applyAll(operations);

		assertEquals(1, application.getHandlers().size());

		handler = application.getHandlers().get(0);
		assertEquals(handlerId, handler.getId());
		assertEquals(null, handler.getCommand());
	}

	public void testHandlerContainer_Handlers_Add_BoundHandler() {
		String applicationId = createId();
		String commandId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		String handlerId = createId();
		MHandler handler = MApplicationFactory.eINSTANCE.createHandler();
		handler.setId(handlerId);
		handler.setCommand(command);
		application.getHandlers().add(handler);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		assertEquals(0, application.getHandlers().size());

		applyAll(operations);

		assertEquals(1, application.getHandlers().size());

		handler = application.getHandlers().get(0);
		assertEquals(handlerId, handler.getId());
		assertEquals(command, handler.getCommand());
	}

	public void testHandlerContainer_Handlers_Remove_UnboundHandler() {
		String applicationId = createId();
		String handlerId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MHandler handler = MApplicationFactory.eINSTANCE.createHandler();
		handler.setId(handlerId);
		application.getHandlers().add(handler);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		application.getHandlers().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		handler = MApplicationFactory.eINSTANCE.createHandler();
		handler.setId(handlerId);
		application.getHandlers().add(handler);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		assertEquals(1, application.getHandlers().size());

		handler = application.getHandlers().get(0);
		assertEquals(handlerId, handler.getId());

		applyAll(operations);

		assertEquals(0, application.getHandlers().size());
	}

	public void testHandlerContainer_Handlers_Remove_BoundHandler() {
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

		application.getHandlers().remove(handler);

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

		assertEquals(1, application.getHandlers().size());

		handler = application.getHandlers().get(0);
		assertEquals(handlerId, handler.getId());
		assertEquals(command, handler.getCommand());

		applyAll(operations);

		assertEquals(0, application.getHandlers().size());
	}
}
