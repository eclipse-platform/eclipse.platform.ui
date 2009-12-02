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
import org.eclipse.e4.ui.model.application.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.ModelDeltaOperation;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerHandledItemTest extends
		ModelReconcilerTest {

	// public void testHandledToolItem_Command_Set() {
	// String applicationId = createId();
	// String windowId = createId();
	// String commandId = createId();
	// String partId = createId();
	// String toolBarId = createId();
	// String handledToolItemId = createId();
	//
	// MApplication application = createApplication();
	// application.setId(applicationId);
	//
	// MCommand command = MApplicationFactory.eINSTANCE.createCommand();
	// command.setId(commandId);
	// application.getCommands().add(command);
	//
	// MWindow window = createWindow(application);
	// window.setId(windowId);
	//
	// MPart part = MApplicationFactory.eINSTANCE.createPart();
	// part.setId(partId);
	// window.getChildren().add(part);
	//
	// MToolBar toolBar = MApplicationFactory.eINSTANCE.createToolBar();
	// toolBar.setId(toolBarId);
	// part.setToolbar(toolBar);
	//
	// MHandledToolItem handledToolItem = MApplicationFactory.eINSTANCE
	// .createHandledToolItem();
	// handledToolItem.setId(handledToolItemId);
	// toolBar.getChildren().add(handledToolItem);
	//
	// ModelReconciler reconciler = createModelReconciler();
	// reconciler.recordChanges(application);
	//
	// handledToolItem.setCommand(command);
	//
	// Object state = reconciler.serialize();
	//
	// application = createApplication();
	// application.setId(applicationId);
	//
	// command = MApplicationFactory.eINSTANCE.createCommand();
	// command.setId(commandId);
	// application.getCommands().add(command);
	//
	// window = createWindow(application);
	// window.setId(windowId);
	//
	// part = MApplicationFactory.eINSTANCE.createPart();
	// part.setId(partId);
	// window.getChildren().add(part);
	//
	// toolBar = MApplicationFactory.eINSTANCE.createToolBar();
	// toolBar.setId(toolBarId);
	// part.setToolbar(toolBar);
	//
	// handledToolItem = MApplicationFactory.eINSTANCE.createHandledToolItem();
	// handledToolItem.setId(handledToolItemId);
	// toolBar.getChildren().add(handledToolItem);
	//
	// Collection<ModelDeltaOperation> operations = applyDeltas(application,
	// state);
	//
	// assertEquals(command, handledToolItem.getCommand());
	// }
	//
	// public void testHandledToolItem_Command_Unset() {
	// String applicationId = createId();
	// String windowId = createId();
	// String commandId = createId();
	// String partId = createId();
	// String toolBarId = createId();
	// String handledToolItemId = createId();
	//
	// MApplication application = createApplication();
	// application.setId(applicationId);
	//
	// MCommand command = MApplicationFactory.eINSTANCE.createCommand();
	// command.setId(commandId);
	// application.getCommands().add(command);
	//
	// MWindow window = createWindow(application);
	// window.setId(windowId);
	//
	// MPart part = MApplicationFactory.eINSTANCE.createPart();
	// part.setId(partId);
	// window.getChildren().add(part);
	//
	// MToolBar toolBar = MApplicationFactory.eINSTANCE.createToolBar();
	// toolBar.setId(toolBarId);
	// part.setToolbar(toolBar);
	//
	// MHandledToolItem handledToolItem = MApplicationFactory.eINSTANCE
	// .createHandledToolItem();
	// handledToolItem.setId(handledToolItemId);
	// handledToolItem.setCommand(command);
	// toolBar.getChildren().add(handledToolItem);
	//
	// ModelReconciler reconciler = createModelReconciler();
	// reconciler.recordChanges(application);
	//
	// handledToolItem.setCommand(null);
	//
	// Object state = reconciler.serialize();
	//
	// application = createApplication();
	// application.setId(applicationId);
	//
	// command = MApplicationFactory.eINSTANCE.createCommand();
	// command.setId(commandId);
	// application.getCommands().add(command);
	//
	// window = createWindow(application);
	// window.setId(windowId);
	//
	// part = MApplicationFactory.eINSTANCE.createPart();
	// part.setId(partId);
	// window.getChildren().add(part);
	//
	// toolBar = MApplicationFactory.eINSTANCE.createToolBar();
	// toolBar.setId(toolBarId);
	// part.setToolbar(toolBar);
	//
	// handledToolItem = MApplicationFactory.eINSTANCE.createHandledToolItem();
	// handledToolItem.setId(handledToolItemId);
	// toolBar.getChildren().add(handledToolItem);
	//
	// Collection<ModelDeltaOperation> operations = applyDeltas(application,
	// state);
	//
	// assertNull(handledToolItem.getCommand());
	// }

	public void testHandledMenuItem_Command_Set() {
		String applicationId = createId();
		String windowId = createId();
		String commandId = createId();
		String menuId = createId();
		String handledMenuItemId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		window.setMainMenu(menu);

		MHandledMenuItem handledMenuItem = MApplicationFactory.eINSTANCE
				.createHandledMenuItem();
		handledMenuItem.setId(handledMenuItemId);
		menu.getChildren().add(handledMenuItem);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		handledMenuItem.setCommand(command);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		window = createWindow(application);
		window.setId(windowId);

		menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		window.setMainMenu(menu);

		handledMenuItem = MApplicationFactory.eINSTANCE.createHandledMenuItem();
		handledMenuItem.setId(handledMenuItemId);
		menu.getChildren().add(handledMenuItem);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		assertNull(handledMenuItem.getCommand());

		applyAll(operations);

		assertEquals(command, handledMenuItem.getCommand());
	}

	public void testHandledMenuItem_Command_Unset() {
		String applicationId = createId();
		String windowId = createId();
		String commandId = createId();
		String menuId = createId();
		String handledMenuItemId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MCommand command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		window.setMainMenu(menu);

		MHandledMenuItem handledMenuItem = MApplicationFactory.eINSTANCE
				.createHandledMenuItem();
		handledMenuItem.setId(handledMenuItemId);
		handledMenuItem.setCommand(command);
		menu.getChildren().add(handledMenuItem);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		handledMenuItem.setCommand(null);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		command = MApplicationFactory.eINSTANCE.createCommand();
		command.setId(commandId);
		application.getCommands().add(command);

		window = createWindow(application);
		window.setId(windowId);

		menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		window.setMainMenu(menu);

		handledMenuItem = MApplicationFactory.eINSTANCE.createHandledMenuItem();
		handledMenuItem.setId(handledMenuItemId);
		handledMenuItem.setCommand(command);
		menu.getChildren().add(handledMenuItem);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		assertEquals(command, handledMenuItem.getCommand());

		applyAll(operations);

		assertNull(handledMenuItem.getCommand());
	}
}
