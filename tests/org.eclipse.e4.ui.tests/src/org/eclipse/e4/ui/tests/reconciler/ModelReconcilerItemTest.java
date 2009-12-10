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
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerItemTest extends ModelReconcilerTest {

	private void testItem_Enabled(boolean applicationState, boolean userChange,
			boolean newApplicationState) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		MMenuItem item = MApplicationFactory.eINSTANCE.createMenuItem();

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
		item = menu.getChildren().get(0);

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

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		MMenuItem item = MApplicationFactory.eINSTANCE.createMenuItem();

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
		item = menu.getChildren().get(0);

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

	private void testItem_Separator(boolean applicationState,
			boolean userChange, boolean newApplicationState) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		MMenuItem item = MApplicationFactory.eINSTANCE.createMenuItem();

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setSeparator(applicationState);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		item.setSeparator(userChange);

		saveModel();

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		menu = window.getMainMenu();
		item = menu.getChildren().get(0);

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setSeparator(newApplicationState);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(newApplicationState, item.isSeparator());

		applyAll(deltas);

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
