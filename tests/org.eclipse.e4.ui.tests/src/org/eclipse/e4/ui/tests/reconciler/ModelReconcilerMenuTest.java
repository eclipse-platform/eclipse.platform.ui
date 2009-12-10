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
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(part);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		part.getMenus().add(menu);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MMenuItem menuItem = MApplicationFactory.eINSTANCE.createMenuItem();
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
	}

	public void testPartMenu_Children_Remove() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(part);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		part.getMenus().add(menu);

		MMenuItem menuItem = MApplicationFactory.eINSTANCE.createMenuItem();
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

		menuItem = menu.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, menu.getChildren().size());

		assertEquals(menuItem, menu.getChildren().get(0));

		applyAll(deltas);

		assertEquals(0, menu.getChildren().size());
	}
}
