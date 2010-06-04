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
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerMenuTest extends ModelReconcilerTest {

	private void testPartMenu_Children_Add(MMenuElement menuItem) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
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

	public void testPartMenu_Children_Add_MenuSeparator() {
		testPartMenu_Children_Add(MenuFactoryImpl.eINSTANCE
				.createMenuSeparator());
	}

	public void testPartMenu_Children_Add_DirectMenuItem() {
		testPartMenu_Children_Add(MenuFactoryImpl.eINSTANCE
				.createDirectMenuItem());
	}

	public void testPartMenu_Children_Add_HandledMenuItem() {
		testPartMenu_Children_Add(MenuFactoryImpl.eINSTANCE
				.createHandledMenuItem());
	}

	public void testPartMenu_Children_Remove() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		part.getMenus().add(menu);

		MMenuItem menuItem = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
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
