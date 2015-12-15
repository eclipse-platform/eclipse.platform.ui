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
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.junit.Test;

public abstract class ModelReconcilerHandledItemTest extends
		ModelReconcilerTest {

	@Test
	public void testHandledToolItem_Command_Set() {
		MApplication application = createApplication();

		MCommand command = ems.createModelElement(MCommand.class);
		application.getCommands().add(command);

		MWindow window = createWindow(application);

		MPart part = ems.createModelElement(MPart.class);
		window.getChildren().add(part);

		MToolBar toolBar = ems.createModelElement(MToolBar.class);
		part.setToolbar(toolBar);

		MHandledToolItem handledToolItem = ems.createModelElement(MHandledToolItem.class);
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

	@Test
	public void testHandledToolItem_Command_Unset() {
		MApplication application = createApplication();

		MCommand command = ems.createModelElement(MCommand.class);
		application.getCommands().add(command);

		MWindow window = createWindow(application);

		MPart part = ems.createModelElement(MPart.class);
		window.getChildren().add(part);

		MToolBar toolBar = ems.createModelElement(MToolBar.class);
		part.setToolbar(toolBar);

		MHandledToolItem handledToolItem = ems.createModelElement(MHandledToolItem.class);
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

	@Test
	public void testHandledToolItem_Parameters_Add() {
		MApplication application = createApplication();

		MCommand command = ems.createModelElement(MCommand.class);
		application.getCommands().add(command);

		MWindow window = createWindow(application);

		MPart part = ems.createModelElement(MPart.class);
		window.getChildren().add(part);

		MToolBar toolBar = ems.createModelElement(MToolBar.class);
		part.setToolbar(toolBar);

		MHandledToolItem handledToolItem = ems.createModelElement(MHandledToolItem.class);
		toolBar.getChildren().add(handledToolItem);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MParameter parameter = ems.createModelElement(MParameter.class);
		parameter.setName("parameterName");
		handledToolItem.getParameters().add(parameter);

		Object state = reconciler.serialize();

		application = createApplication();
		command = application.getCommands().get(0);
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);
		toolBar = part.getToolbar();

		handledToolItem = (MHandledToolItem) toolBar.getChildren().get(0);

		assertEquals(0, handledToolItem.getParameters().size());

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, handledToolItem.getParameters().size());

		applyAll(deltas);

		assertEquals(1, handledToolItem.getParameters().size());
		assertEquals("parameterName", handledToolItem.getParameters().get(0)
				.getName());
	}

	@Test
	public void testHandledToolItem_Parameters_Remove() {
		MApplication application = createApplication();

		MCommand command = ems.createModelElement(MCommand.class);
		application.getCommands().add(command);

		MWindow window = createWindow(application);

		MPart part = ems.createModelElement(MPart.class);
		window.getChildren().add(part);

		MToolBar toolBar = ems.createModelElement(MToolBar.class);
		part.setToolbar(toolBar);

		MHandledToolItem handledToolItem = ems.createModelElement(MHandledToolItem.class);
		toolBar.getChildren().add(handledToolItem);

		MParameter parameter = ems.createModelElement(MParameter.class);
		parameter.setName("parameterName");
		handledToolItem.getParameters().add(parameter);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		handledToolItem.getParameters().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		command = application.getCommands().get(0);
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);
		toolBar = part.getToolbar();

		handledToolItem = (MHandledToolItem) toolBar.getChildren().get(0);
		parameter = handledToolItem.getParameters().get(0);

		assertEquals(1, handledToolItem.getParameters().size());
		assertEquals(parameter, handledToolItem.getParameters().get(0));
		assertEquals("parameterName", parameter.getName());

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, handledToolItem.getParameters().size());
		assertEquals(parameter, handledToolItem.getParameters().get(0));
		assertEquals("parameterName", parameter.getName());

		applyAll(deltas);

		assertEquals(0, handledToolItem.getParameters().size());
	}

	@Test
	public void testHandledMenuItem_Command_Set() {
		MApplication application = createApplication();

		MCommand command = ems.createModelElement(MCommand.class);
		application.getCommands().add(command);

		MWindow window = createWindow(application);

		MMenu menu = ems.createModelElement(MMenu.class);
		window.setMainMenu(menu);

		MHandledMenuItem handledMenuItem = ems.createModelElement(MHandledMenuItem.class);
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

	@Test
	public void testHandledMenuItem_Command_Unset() {
		MApplication application = createApplication();

		MCommand command = ems.createModelElement(MCommand.class);
		application.getCommands().add(command);

		MWindow window = createWindow(application);

		MMenu menu = ems.createModelElement(MMenu.class);
		window.setMainMenu(menu);

		MHandledMenuItem handledMenuItem = ems.createModelElement(MHandledMenuItem.class);
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

	@Test
	public void testHandledMenuItem_Parameters_Add() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MMenu menu = ems.createModelElement(MMenu.class);
		window.setMainMenu(menu);

		MHandledMenuItem handledMenuItem = ems.createModelElement(MHandledMenuItem.class);
		menu.getChildren().add(handledMenuItem);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MParameter parameter = ems.createModelElement(MParameter.class);
		parameter.setName("parameterName");
		handledMenuItem.getParameters().add(parameter);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		menu = window.getMainMenu();
		handledMenuItem = (MHandledMenuItem) menu.getChildren().get(0);

		assertEquals(0, handledMenuItem.getParameters().size());

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, handledMenuItem.getParameters().size());

		applyAll(deltas);

		assertEquals(1, handledMenuItem.getParameters().size());
		assertEquals("parameterName", handledMenuItem.getParameters().get(0)
				.getName());
	}

	@Test
	public void testHandledMenuItem_Parameters_Remove() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MMenu menu = ems.createModelElement(MMenu.class);
		window.setMainMenu(menu);

		MHandledMenuItem handledMenuItem = ems.createModelElement(MHandledMenuItem.class);
		menu.getChildren().add(handledMenuItem);

		MParameter parameter = ems.createModelElement(MParameter.class);
		parameter.setName("parameterName");
		handledMenuItem.getParameters().add(parameter);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		handledMenuItem.getParameters().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		menu = window.getMainMenu();
		handledMenuItem = (MHandledMenuItem) menu.getChildren().get(0);
		parameter = handledMenuItem.getParameters().get(0);

		assertEquals(1, handledMenuItem.getParameters().size());
		assertEquals("parameterName", parameter.getName());

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, handledMenuItem.getParameters().size());
		assertEquals(parameter, handledMenuItem.getParameters().get(0));
		assertEquals("parameterName", parameter.getName());

		applyAll(deltas);

		assertEquals(0, handledMenuItem.getParameters().size());
	}
}
