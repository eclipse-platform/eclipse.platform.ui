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

import java.util.Collection;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.junit.Test;

public abstract class ModelReconcilerMenuTest extends ModelReconcilerTest {

	private void testPartMenu_Children_Add(MMenuElement menuItem) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = ems.createModelElement(MPart.class);
		window.getChildren().add(part);

		MMenu menu = ems.createModelElement(MMenu.class);
		part.getMenus().add(menu);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		menu.getChildren().add(menuItem);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		part = (MPart) window.getChildren().get(0);
		menu = part.getMenus().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, menu.getChildren().size());

		applyAll(deltas);

		assertEquals(1, menu.getChildren().size());
		assertEquals(menuItem.getClass(), menu.getChildren().get(0).getClass());
	}

	@Test
	public void testPartMenu_Children_Add_MenuSeparator() {
		testPartMenu_Children_Add(ems.createModelElement(MMenuSeparator.class));
	}

	@Test
	public void testPartMenu_Children_Add_DirectMenuItem() {
		testPartMenu_Children_Add(ems.createModelElement(MDirectMenuItem.class));
	}

	@Test
	public void testPartMenu_Children_Add_HandledMenuItem() {
		testPartMenu_Children_Add(ems.createModelElement(MHandledMenuItem.class));
	}

	@Test
	public void testPartMenu_Children_Remove() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = ems.createModelElement(MPart.class);
		window.getChildren().add(part);

		MMenu menu = ems.createModelElement(MMenu.class);
		part.getMenus().add(menu);

		MMenuItem menuItem = ems.createModelElement(MDirectMenuItem.class);
		menu.getChildren().add(menuItem);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		menu.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);

		menu = part.getMenus().get(0);

		menuItem = (MMenuItem) menu.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, menu.getChildren().size());

		assertEquals(menuItem, menu.getChildren().get(0));

		applyAll(deltas);

		assertEquals(0, menu.getChildren().size());
	}
}
