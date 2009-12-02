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
import org.eclipse.e4.ui.model.application.MViewSashContainer;
import org.eclipse.e4.ui.model.application.MViewStack;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.ModelDeltaOperation;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerViewSashContainerTest extends
		ModelReconcilerTest {

	public void testViewSashContainer_Children_Add() {
		String applicationId = createId();
		String windowId = createId();
		String viewSashContainerId = createId();
		String viewStackId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MViewSashContainer viewSashContainer = MApplicationFactory.eINSTANCE
				.createViewSashContainer();
		viewSashContainer.setId(viewSashContainerId);
		window.getChildren().add(viewSashContainer);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MViewStack viewStack = MApplicationFactory.eINSTANCE.createViewStack();
		viewStack.setId(viewStackId);
		viewSashContainer.getChildren().add(viewStack);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		viewSashContainer = MApplicationFactory.eINSTANCE
				.createViewSashContainer();
		viewSashContainer.setId(viewSashContainerId);
		window.getChildren().add(viewSashContainer);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		assertEquals(0, viewSashContainer.getChildren().size());

		applyAll(operations);

		assertEquals(1, viewSashContainer.getChildren().size());

		viewStack = (MViewStack) viewSashContainer.getChildren().get(0);
		assertEquals(viewStackId, viewStack.getId());
	}

	public void testViewStack_Children_Remove() {
		String applicationId = createId();
		String windowId = createId();
		String viewSashContainerId = createId();
		String viewStackId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MViewSashContainer viewSashContainer = MApplicationFactory.eINSTANCE
				.createViewSashContainer();
		viewSashContainer.setId(viewSashContainerId);
		window.getChildren().add(viewSashContainer);

		MViewStack viewStack = MApplicationFactory.eINSTANCE.createViewStack();
		viewStack.setId(viewStackId);
		viewSashContainer.getChildren().add(viewStack);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		viewSashContainer.getChildren().remove(0);

		Object state = reconciler.serialize();
		print(state);

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		viewSashContainer = MApplicationFactory.eINSTANCE
				.createViewSashContainer();
		viewSashContainer.setId(viewSashContainerId);
		window.getChildren().add(viewSashContainer);

		viewStack = MApplicationFactory.eINSTANCE.createViewStack();
		viewStack.setId(viewStackId);
		viewSashContainer.getChildren().add(viewStack);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		assertEquals(1, viewSashContainer.getChildren().size());
		assertEquals(viewStack, viewSashContainer.getChildren().get(0));
		assertEquals(viewStackId, viewStack.getId());

		applyAll(operations);

		assertEquals(0, viewSashContainer.getChildren().size());
	}
}
