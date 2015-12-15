/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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

import java.util.Collection;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.junit.Test;

public abstract class ModelReconcilerMenuItemTest extends ModelReconcilerTest {

	private void testMenuItem_Mnemonics(String before, String after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MMenu menu = ems.createModelElement(MMenu.class);
		window.setMainMenu(menu);

		MMenuItem menuItem = ems.createModelElement(MDirectMenuItem.class);
		menu.getChildren().add(menuItem);
		menuItem.setMnemonics(before);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		menuItem.setMnemonics(after);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		menu = window.getMainMenu();
		menuItem = (MMenuItem) menu.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(menu, window.getMainMenu());
		assertEquals(1, menu.getChildren().size());
		assertEquals(menuItem, menu.getChildren().get(0));
		assertEquals(before, menuItem.getMnemonics());

		applyAll(deltas);

		assertEquals(after, menuItem.getMnemonics());
	}

	@Test
	public void testMenuItem_Mnemonics_NullNull() {
		testMenuItem_Mnemonics(null, null);
	}

	@Test
	public void testMenuItem_Mnemonics_NullEmpty() {
		testMenuItem_Mnemonics(null, "");
	}

	@Test
	public void testMenuItem_Mnemonics_NullString() {
		testMenuItem_Mnemonics(null, "m");
	}

	@Test
	public void testMenuItem_Mnemonics_EmptyNull() {
		testMenuItem_Mnemonics("", null);
	}

	@Test
	public void testMenuItem_Mnemonics_EmptyEmpty() {
		testMenuItem_Mnemonics("", "");
	}

	@Test
	public void testMenuItem_Mnemonics_EmptyString() {
		testMenuItem_Mnemonics("", "m");
	}

	@Test
	public void testMenuItem_Mnemonics_StringNull() {
		testMenuItem_Mnemonics("m", null);
	}

	@Test
	public void testMenuItem_Mnemonics_StringEmpty() {
		testMenuItem_Mnemonics("m", "");
	}

	@Test
	public void testMenuItem_Mnemonics_StringStringUnchanged() {
		testMenuItem_Mnemonics("m", "m");
	}

	@Test
	public void testMenuItem_Mnemonics_StringStringChanged() {
		testMenuItem_Mnemonics("m", "n");
	}
}
