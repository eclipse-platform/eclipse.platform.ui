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
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerItemTest extends ModelReconcilerTest {

	private void testItem_Enabled(boolean applicationState, boolean userChange,
			boolean newApplicationState) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		MMenuItem item = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setEnabled(applicationState);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		item.setEnabled(userChange);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		menu = window.getMainMenu();
		item = (MMenuItem) menu.getChildren().get(0);

		item.setEnabled(newApplicationState);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(newApplicationState, item.isEnabled());

		applyAll(deltas);

		if (userChange == applicationState) {
			// no change from the user, the new state is applied
			assertEquals(newApplicationState, item.isEnabled());
		} else {
			// user change must override application state
			assertEquals(userChange, item.isEnabled());
		}
	}

	public void testItem_Enabled_TrueTrueTrue() {
		testItem_Enabled(true, true, true);
	}

	public void testItem_Enabled_TrueTrueFalse() {
		testItem_Enabled(true, true, false);
	}

	public void testItem_Enabled_TrueFalseTrue() {
		testItem_Enabled(true, false, true);
	}

	public void testItem_Enabled_TrueFalseFalse() {
		testItem_Enabled(true, false, false);
	}

	public void testItem_Enabled_FalseTrueTrue() {
		testItem_Enabled(false, true, true);
	}

	public void testItem_Enabled_FalseTrueFalse() {
		testItem_Enabled(false, true, false);
	}

	public void testItem_Enabled_FalseFalseTrue() {
		testItem_Enabled(false, false, true);
	}

	public void testItem_Enabled_FalseFalseFalse() {
		testItem_Enabled(false, false, false);
	}

	private void testItem_Selected(boolean applicationState,
			boolean userChange, boolean newApplicationState) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		MMenuItem item = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setSelected(applicationState);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		item.setSelected(userChange);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		menu = window.getMainMenu();
		item = (MMenuItem) menu.getChildren().get(0);

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setSelected(newApplicationState);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(newApplicationState, item.isSelected());

		applyAll(deltas);

		if (userChange == applicationState) {
			// no change from the user, the new state is applied
			assertEquals(newApplicationState, item.isSelected());
		} else {
			// user change must override application state
			assertEquals(userChange, item.isSelected());
		}
	}

	public void testItem_Selected_TrueTrueTrue() {
		testItem_Selected(true, true, true);
	}

	public void testItem_Selected_TrueTrueFalse() {
		testItem_Selected(true, true, false);
	}

	public void testItem_Selected_TrueFalseTrue() {
		testItem_Selected(true, false, true);
	}

	public void testItem_Selected_TrueFalseFalse() {
		testItem_Selected(true, false, false);
	}

	public void testItem_Selected_FalseTrueTrue() {
		testItem_Selected(false, true, true);
	}

	public void testItem_Selected_FalseTrueFalse() {
		testItem_Selected(false, true, false);
	}

	public void testItem_Selected_FalseFalseTrue() {
		testItem_Selected(false, false, true);
	}

	public void testItem_Selected_FalseFalseFalse() {
		testItem_Selected(false, false, false);
	}

	private void testItem_Type(ItemType applicationState, ItemType userChange,
			ItemType newApplicationState) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		MMenuItem item = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setType(applicationState);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		item.setType(userChange);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		menu = window.getMainMenu();
		item = (MMenuItem) menu.getChildren().get(0);

		item.setType(newApplicationState);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(newApplicationState, item.getType());

		applyAll(deltas);

		if (userChange == applicationState) {
			// no change from the user, the new state is applied
			assertEquals(newApplicationState, item.getType());
		} else {
			// user change must override application state
			assertEquals(userChange, item.getType());
		}
	}

	public void testItem_Type_PushPushPush() {
		testItem_Type(ItemType.PUSH, ItemType.PUSH, ItemType.PUSH);
	}

	public void testItem_Type_PushPushCheck() {
		testItem_Type(ItemType.PUSH, ItemType.PUSH, ItemType.CHECK);
	}

	public void testItem_Type_PushCheckPush() {
		testItem_Type(ItemType.PUSH, ItemType.CHECK, ItemType.PUSH);
	}

	public void testItem_Type_PushCheckCheck() {
		testItem_Type(ItemType.PUSH, ItemType.CHECK, ItemType.CHECK);
	}

	public void testItem_Type_CheckCheckCheck() {
		testItem_Type(ItemType.CHECK, ItemType.CHECK, ItemType.CHECK);
	}

	public void testItem_Type_CheckCheckRadio() {
		testItem_Type(ItemType.CHECK, ItemType.CHECK, ItemType.RADIO);
	}

	public void testItem_Type_CheckRadioCheck() {
		testItem_Type(ItemType.CHECK, ItemType.RADIO, ItemType.CHECK);
	}

	public void testItem_Type_CheckRadioRadio() {
		testItem_Type(ItemType.CHECK, ItemType.RADIO, ItemType.RADIO);
	}

	public void testItem_Type_RadioRadioRadio() {
		testItem_Type(ItemType.RADIO, ItemType.RADIO, ItemType.RADIO);
	}
}
