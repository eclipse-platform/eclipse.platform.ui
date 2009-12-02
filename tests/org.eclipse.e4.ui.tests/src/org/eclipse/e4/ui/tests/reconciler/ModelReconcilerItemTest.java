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
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.ModelDeltaOperation;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerItemTest extends ModelReconcilerTest {

	private void testItem_Enabled(boolean applicationState, boolean userChange,
			boolean newApplicationState) {
		String applicationId = createId();
		String windowId = createId();

		String menuId = createId();
		String menuItemId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		MMenuItem item = MApplicationFactory.eINSTANCE.createMenuItem();
		menu.setId(menuId);
		item.setId(menuItemId);

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setEnabled(applicationState);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		item.setEnabled(userChange);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		menu = MApplicationFactory.eINSTANCE.createMenu();
		item = MApplicationFactory.eINSTANCE.createMenuItem();
		menu.setId(menuId);
		item.setId(menuItemId);

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setEnabled(newApplicationState);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		assertEquals(newApplicationState, item.isEnabled());

		applyAll(operations);

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
		String applicationId = createId();
		String windowId = createId();

		String menuId = createId();
		String menuItemId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		MMenuItem item = MApplicationFactory.eINSTANCE.createMenuItem();
		menu.setId(menuId);
		item.setId(menuItemId);

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setSelected(applicationState);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		item.setSelected(userChange);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		menu = MApplicationFactory.eINSTANCE.createMenu();
		item = MApplicationFactory.eINSTANCE.createMenuItem();
		menu.setId(menuId);
		item.setId(menuItemId);

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setSelected(newApplicationState);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		assertEquals(newApplicationState, item.isSelected());

		applyAll(operations);

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

	private void testItem_Separator(boolean applicationState,
			boolean userChange, boolean newApplicationState) {
		String applicationId = createId();
		String windowId = createId();

		String menuId = createId();
		String menuItemId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		MMenuItem item = MApplicationFactory.eINSTANCE.createMenuItem();
		menu.setId(menuId);
		item.setId(menuItemId);

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setSeparator(applicationState);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		item.setSeparator(userChange);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		menu = MApplicationFactory.eINSTANCE.createMenu();
		item = MApplicationFactory.eINSTANCE.createMenuItem();
		menu.setId(menuId);
		item.setId(menuItemId);

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setSeparator(newApplicationState);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		assertEquals(newApplicationState, item.isSeparator());

		applyAll(operations);

		if (userChange == applicationState) {
			// no change from the user, the new state is applied
			assertEquals(newApplicationState, item.isSeparator());
		} else {
			// user change must override application state
			assertEquals(userChange, item.isSeparator());
		}
	}

	public void testItem_Separator_TrueTrueTrue() {
		testItem_Separator(true, true, true);
	}

	public void testItem_Separator_TrueTrueFalse() {
		testItem_Separator(true, true, false);
	}

	public void testItem_Separator_TrueFalseTrue() {
		testItem_Separator(true, false, true);
	}

	public void testItem_Separator_TrueFalseFalse() {
		testItem_Separator(true, false, false);
	}

	public void testItem_Separator_FalseTrueTrue() {
		testItem_Separator(false, true, true);
	}

	public void testItem_Separator_FalseTrueFalse() {
		testItem_Separator(false, true, false);
	}

	public void testItem_Separator_FalseFalseTrue() {
		testItem_Separator(false, false, true);
	}

	public void testItem_Separator_FalseFalseFalse() {
		testItem_Separator(false, false, false);
	}
}
