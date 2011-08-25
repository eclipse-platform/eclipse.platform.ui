/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.commands.MParameter;
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

public abstract class ModelReconcilerParameterTest extends ModelReconcilerTest {

	private void testHandledToolItem_Parameters_Name(String before, String after) {
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

		MParameter parameter = CommandsFactoryImpl.eINSTANCE.createParameter();
		parameter.setName(before);
		handledToolItem.getParameters().add(parameter);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		parameter.setName(after);

		Object state = reconciler.serialize();

		application = createApplication();
		command = application.getCommands().get(0);
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);
		toolBar = part.getToolbar();

		handledToolItem = (MHandledToolItem) toolBar.getChildren().get(0);
		parameter = handledToolItem.getParameters().get(0);

		assertEquals(before, parameter.getName());

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, parameter.getName());

		applyAll(deltas);

		assertEquals(after, parameter.getName());
	}

	public void testHandledToolItem_Parameters_Name_NullNull() {
		testHandledToolItem_Parameters_Name(null, null);
	}

	public void testHandledToolItem_Parameters_Name_NullEmpty() {
		testHandledToolItem_Parameters_Name(null, "");
	}

	public void testHandledToolItem_Parameters_Name_NullString() {
		testHandledToolItem_Parameters_Name(null, "name");
	}

	public void testHandledToolItem_Parameters_Name_EmptyNull() {
		testHandledToolItem_Parameters_Name("", null);
	}

	public void testHandledToolItem_Parameters_Name_EmptyEmpty() {
		testHandledToolItem_Parameters_Name("", "");
	}

	public void testHandledToolItem_Parameters_Name_EmptyString() {
		testHandledToolItem_Parameters_Name("", "name");
	}

	public void testHandledToolItem_Parameters_Name_StringNull() {
		testHandledToolItem_Parameters_Name("name", null);
	}

	public void testHandledToolItem_Parameters_Name_StringEmpty() {
		testHandledToolItem_Parameters_Name("name", "");
	}

	public void testHandledToolItem_Parameters_Name_StringStringUnchanged() {
		testHandledToolItem_Parameters_Name("name", "name");
	}

	public void testHandledToolItem_Parameters_Name_StringStringChanged() {
		testHandledToolItem_Parameters_Name("name", "name2");
	}

	private void testHandledToolItem_Parameters_Value(String before,
			String after) {
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

		MParameter parameter = CommandsFactoryImpl.eINSTANCE.createParameter();
		parameter.setValue(before);
		handledToolItem.getParameters().add(parameter);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		parameter.setValue(after);

		Object state = reconciler.serialize();

		application = createApplication();
		command = application.getCommands().get(0);
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);
		toolBar = part.getToolbar();

		handledToolItem = (MHandledToolItem) toolBar.getChildren().get(0);
		parameter = handledToolItem.getParameters().get(0);

		assertEquals(before, parameter.getValue());

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, parameter.getValue());

		applyAll(deltas);

		assertEquals(after, parameter.getValue());
	}

	public void testHandledToolItem_Parameters_Value_NullNull() {
		testHandledToolItem_Parameters_Value(null, null);
	}

	public void testHandledToolItem_Parameters_Value_NullEmpty() {
		testHandledToolItem_Parameters_Value(null, "");
	}

	public void testHandledToolItem_Parameters_Value_NullString() {
		testHandledToolItem_Parameters_Value(null, "name");
	}

	public void testHandledToolItem_Parameters_Value_EmptyNull() {
		testHandledToolItem_Parameters_Value("", null);
	}

	public void testHandledToolItem_Parameters_Value_EmptyEmpty() {
		testHandledToolItem_Parameters_Value("", "");
	}

	public void testHandledToolItem_Parameters_Value_EmptyString() {
		testHandledToolItem_Parameters_Value("", "name");
	}

	public void testHandledToolItem_Parameters_Value_StringNull() {
		testHandledToolItem_Parameters_Value("name", null);
	}

	public void testHandledToolItem_Parameters_Value_StringEmpty() {
		testHandledToolItem_Parameters_Value("name", "");
	}

	public void testHandledToolItem_Parameters_Value_StringStringUnchanged() {
		testHandledToolItem_Parameters_Value("name", "name");
	}

	public void testHandledToolItem_Parameters_Value_StringStringChanged() {
		testHandledToolItem_Parameters_Value("name", "name2");
	}

	private void testHandledMenuItem_Parameters_Name(String before, String after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		window.setMainMenu(menu);

		MHandledMenuItem handledMenuItem = MenuFactoryImpl.eINSTANCE
				.createHandledMenuItem();
		menu.getChildren().add(handledMenuItem);

		MParameter parameter = CommandsFactoryImpl.eINSTANCE.createParameter();
		parameter.setName(before);
		handledMenuItem.getParameters().add(parameter);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		parameter.setName(after);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		menu = window.getMainMenu();
		handledMenuItem = (MHandledMenuItem) menu.getChildren().get(0);
		parameter = handledMenuItem.getParameters().get(0);

		assertEquals(1, handledMenuItem.getParameters().size());
		assertEquals(parameter, handledMenuItem.getParameters().get(0));
		assertEquals(before, parameter.getName());

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, handledMenuItem.getParameters().size());
		assertEquals(parameter, handledMenuItem.getParameters().get(0));
		assertEquals(before, parameter.getName());

		applyAll(deltas);

		assertEquals(1, handledMenuItem.getParameters().size());
		assertEquals(parameter, handledMenuItem.getParameters().get(0));
		assertEquals(after, parameter.getName());
	}

	public void testHandledMenuItem_Parameters_Name_NullNull() {
		testHandledMenuItem_Parameters_Name(null, null);
	}

	public void testHandledMenuItem_Parameters_Name_NullEmpty() {
		testHandledMenuItem_Parameters_Name(null, "");
	}

	public void testHandledMenuItem_Parameters_Name_NullString() {
		testHandledMenuItem_Parameters_Name(null, "name");
	}

	public void testHandledMenuItem_Parameters_Name_EmptyNull() {
		testHandledMenuItem_Parameters_Name("", null);
	}

	public void testHandledMenuItem_Parameters_Name_EmptyEmpty() {
		testHandledMenuItem_Parameters_Name("", "");
	}

	public void testHandledMenuItem_Parameters_Name_EmptyString() {
		testHandledMenuItem_Parameters_Name("", "name");
	}

	public void testHandledMenuItem_Parameters_Name_StringNull() {
		testHandledMenuItem_Parameters_Name("name", null);
	}

	public void testHandledMenuItem_Parameters_Name_StringEmpty() {
		testHandledMenuItem_Parameters_Name("name", "");
	}

	public void testHandledMenuItem_Parameters_Name_StringStringUnchanged() {
		testHandledMenuItem_Parameters_Name("name", "name");
	}

	public void testHandledMenuItem_Parameters_Name_StringStringChanged() {
		testHandledMenuItem_Parameters_Name("name", "name2");
	}

	private void testHandledMenuItem_Parameters_Value(String before,
			String after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		window.setMainMenu(menu);

		MHandledMenuItem handledMenuItem = MenuFactoryImpl.eINSTANCE
				.createHandledMenuItem();
		menu.getChildren().add(handledMenuItem);

		MParameter parameter = CommandsFactoryImpl.eINSTANCE.createParameter();
		parameter.setValue(before);
		handledMenuItem.getParameters().add(parameter);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		parameter.setValue(after);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		menu = window.getMainMenu();
		handledMenuItem = (MHandledMenuItem) menu.getChildren().get(0);
		parameter = handledMenuItem.getParameters().get(0);

		assertEquals(1, handledMenuItem.getParameters().size());
		assertEquals(parameter, handledMenuItem.getParameters().get(0));
		assertEquals(before, parameter.getValue());

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, handledMenuItem.getParameters().size());
		assertEquals(parameter, handledMenuItem.getParameters().get(0));
		assertEquals(before, parameter.getValue());

		applyAll(deltas);

		assertEquals(1, handledMenuItem.getParameters().size());
		assertEquals(parameter, handledMenuItem.getParameters().get(0));
		assertEquals(after, parameter.getValue());
	}

	public void testHandledMenuItem_Parameters_Value_NullNull() {
		testHandledMenuItem_Parameters_Value(null, null);
	}

	public void testHandledMenuItem_Parameters_Value_NullEmpty() {
		testHandledMenuItem_Parameters_Value(null, "");
	}

	public void testHandledMenuItem_Parameters_Value_NullString() {
		testHandledMenuItem_Parameters_Value(null, "name");
	}

	public void testHandledMenuItem_Parameters_Value_EmptyNull() {
		testHandledMenuItem_Parameters_Value("", null);
	}

	public void testHandledMenuItem_Parameters_Value_EmptyEmpty() {
		testHandledMenuItem_Parameters_Value("", "");
	}

	public void testHandledMenuItem_Parameters_Value_EmptyString() {
		testHandledMenuItem_Parameters_Value("", "name");
	}

	public void testHandledMenuItem_Parameters_Value_StringNull() {
		testHandledMenuItem_Parameters_Value("name", null);
	}

	public void testHandledMenuItem_Parameters_Value_StringEmpty() {
		testHandledMenuItem_Parameters_Value("name", "");
	}

	public void testHandledMenuItem_Parameters_Value_StringStringUnchanged() {
		testHandledMenuItem_Parameters_Value("name", "name");
	}

	public void testHandledMenuItem_Parameters_Value_StringStringChanged() {
		testHandledMenuItem_Parameters_Value("name", "name2");
	}
}
