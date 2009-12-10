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
import org.eclipse.e4.ui.model.application.MView;
import org.eclipse.e4.ui.model.application.MViewSashContainer;
import org.eclipse.e4.ui.model.application.MViewStack;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerViewStackTest extends ModelReconcilerTest {

	public void testViewStack_Children_Add() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MViewSashContainer viewSashContainer = MApplicationFactory.eINSTANCE
				.createViewSashContainer();
		window.getChildren().add(viewSashContainer);

		MViewStack viewStack = MApplicationFactory.eINSTANCE.createViewStack();
		viewSashContainer.getChildren().add(viewStack);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MView view = MApplicationFactory.eINSTANCE.createView();
		viewStack.getChildren().add(view);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		viewSashContainer = (MViewSashContainer) window.getChildren().get(0);
		viewStack = (MViewStack) viewSashContainer.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, viewStack.getChildren().size());

		applyAll(deltas);

		assertEquals(1, viewStack.getChildren().size());
		assertNotNull(viewStack.getChildren().get(0));
	}

	public void testViewStack_Children_Remove() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MViewSashContainer viewSashContainer = MApplicationFactory.eINSTANCE
				.createViewSashContainer();
		window.getChildren().add(viewSashContainer);

		MViewStack viewStack = MApplicationFactory.eINSTANCE.createViewStack();
		viewSashContainer.getChildren().add(viewStack);

		MView view = MApplicationFactory.eINSTANCE.createView();
		viewStack.getChildren().add(view);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		viewStack.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		viewSashContainer = (MViewSashContainer) window.getChildren().get(0);
		viewStack = (MViewStack) viewSashContainer.getChildren().get(0);
		view = viewStack.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, viewStack.getChildren().size());
		assertEquals(view, viewStack.getChildren().get(0));

		applyAll(deltas);

		assertEquals(0, viewStack.getChildren().size());
	}
}
