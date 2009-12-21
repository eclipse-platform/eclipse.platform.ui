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
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MToolItem;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerToolBarTest extends ModelReconcilerTest {

	private void testToolBar_Children_Add(MToolItem toolItem) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(part);

		MToolBar toolBar = MApplicationFactory.eINSTANCE.createToolBar();
		part.setToolbar(toolBar);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		toolBar.getChildren().add(toolItem);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		part = (MPart) window.getChildren().get(0);
		toolBar = part.getToolbar();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, toolBar.getChildren().size());

		applyAll(deltas);

		assertEquals(1, toolBar.getChildren().size());
		assertEquals(toolItem.getClass(), toolBar.getChildren().get(0)
				.getClass());
	}

	public void testToolBar_Children_Add_ToolItem() {
		testToolBar_Children_Add(MApplicationFactory.eINSTANCE.createToolItem());
	}

	public void testToolBar_Children_Add_DirectToolItem() {
		testToolBar_Children_Add(MApplicationFactory.eINSTANCE
				.createDirectToolItem());
	}

	public void testToolBar_Children_Add_HandledToolItem() {
		testToolBar_Children_Add(MApplicationFactory.eINSTANCE
				.createHandledToolItem());
	}

	public void testToolBar_Children_Remove() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(part);

		MToolBar toolBar = MApplicationFactory.eINSTANCE.createToolBar();
		part.setToolbar(toolBar);

		MToolItem toolItem = MApplicationFactory.eINSTANCE.createToolItem();
		toolBar.getChildren().add(toolItem);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		toolBar.getChildren().remove(0);

		Object state = reconciler.serialize();
		print(state);

		application = createApplication();
		window = application.getChildren().get(0);

		part = (MPart) window.getChildren().get(0);
		toolBar = part.getToolbar();
		toolItem = toolBar.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, toolBar.getChildren().size());
		assertEquals(toolItem, toolBar.getChildren().get(0));

		applyAll(deltas);

		assertEquals(0, toolBar.getChildren().size());
	}
}
