/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerHandledItemTest extends
		ModelReconcilerTest {

	public void testHandledToolItem_Command_Set() {
		MApplication application = createApplication();

		MCommand command = CommandsFactoryImpl.eINSTANCE.createCommand();
		application.getCommands().add(command);

		MWindow window = createWindow(application);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		part.setToolbar(toolBar);

		MHandledToolItem handledToolItem = MenuFactoryImpl.eINSTANCE
				.createHandledToolItem();
		toolBar.getChildren().add(handledToolItem);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		handledToolItem.setCommand(command);

		Object state = reconciler.serialize();

		application = createApplication();
		command = application.getCommands().get(0);
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);
		toolBar = part.getToolbar();

		handledToolItem = (MHandledToolItem) toolBar.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertNull(handledToolItem.getCommand());

		applyAll(deltas);

		assertEquals(command, handledToolItem.getCommand());
	}

	public void testHandledToolItem_Command_Unset() {
		MApplication application = createApplication();

		MCommand command = CommandsFactoryImpl.eINSTANCE.createCommand();
		application.getCommands().add(command);

		MWindow window = createWindow(application);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		part.setToolbar(toolBar);

		MHandledToolItem handledToolItem = MenuFactoryImpl.eINSTANCE
				.createHandledToolItem();
		toolBar.getChildren().add(handledToolItem);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		handledToolItem.setCommand(command);

		Object state = reconciler.serialize();

		application = createApplication();
		command = application.getCommands().get(0);
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);
		toolBar = part.getToolbar();

		handledToolItem = (MHandledToolItem) toolBar.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertNull(handledToolItem.getCommand());

		applyAll(deltas);

		assertEquals(command, handledToolItem.getCommand());
	}

	public void testHandledMenuItem_Command_Set() {
		MApplication application = createApplication();

		MCommand command = CommandsFactoryImpl.eINSTANCE.createCommand();
		application.getCommands().add(command);

		MWindow window = createWindow(application);

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		window.setMainMenu(menu);

		MHandledMenuItem handledMenuItem = MenuFactoryImpl.eINSTANCE
				.createHandledMenuItem();
		menu.getChildren().add(handledMenuItem);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		handledMenuItem.setCommand(command);

		Object state = reconciler.serialize();

		application = createApplication();
		command = application.getCommands().get(0);
		window = application.getChildren().get(0);

		menu = window.getMainMenu();
		handledMenuItem = (MHandledMenuItem) menu.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertNull(handledMenuItem.getCommand());

		applyAll(deltas);

		assertEquals(command, handledMenuItem.getCommand());
	}

	public void testHandledMenuItem_Command_Unset() {
		MApplication application = createApplication();

		MCommand command = CommandsFactoryImpl.eINSTANCE.createCommand();
		application.getCommands().add(command);

		MWindow window = createWindow(application);

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		window.setMainMenu(menu);

		MHandledMenuItem handledMenuItem = MenuFactoryImpl.eINSTANCE
				.createHandledMenuItem();
		handledMenuItem.setCommand(command);
		menu.getChildren().add(handledMenuItem);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		handledMenuItem.setCommand(null);

		Object state = reconciler.serialize();

		application = createApplication();
		command = application.getCommands().get(0);
		window = application.getChildren().get(0);

		menu = window.getMainMenu();
		handledMenuItem = (MHandledMenuItem) menu.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(command, handledMenuItem.getCommand());

		applyAll(deltas);

		assertNull(handledMenuItem.getCommand());
	}
}
