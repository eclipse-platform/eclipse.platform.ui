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
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerMenuTest extends ModelReconcilerTest {

	public void testPartMenu_Children_Add() {
		String applicationId = createId();
		String windowId = createId();
		String partId = createId();
		String menuId = createId();
		String menuItemId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		window.getChildren().add(part);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		part.getMenus().add(menu);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MMenuItem menuItem = MApplicationFactory.eINSTANCE.createMenuItem();
		menuItem.setId(menuItemId);
		menu.getChildren().add(menuItem);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		window.getChildren().add(part);

		menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		part.getMenus().add(menu);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, menu.getChildren().size());

		applyAll(deltas);

		assertEquals(1, menu.getChildren().size());

		menuItem = menu.getChildren().get(0);
		assertEquals(menuItemId, menuItem.getId());
	}

	public void testPartMenu_Children_Remove() {
		String applicationId = createId();
		String windowId = createId();
		String partId = createId();
		String menuId = createId();
		String menuItemId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		window.getChildren().add(part);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		part.getMenus().add(menu);

		MMenuItem menuItem = MApplicationFactory.eINSTANCE.createMenuItem();
		menuItem.setId(menuItemId);
		menu.getChildren().add(menuItem);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		menu.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		window.getChildren().add(part);

		menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		part.getMenus().add(menu);

		menuItem = MApplicationFactory.eINSTANCE.createMenuItem();
		menuItem.setId(menuItemId);
		menu.getChildren().add(menuItem);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, menu.getChildren().size());

		menuItem = menu.getChildren().get(0);
		assertEquals(menuItemId, menuItem.getId());

		applyAll(deltas);

		assertEquals(0, menu.getChildren().size());
	}
}
