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
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class IModelReconcilingServiceTest extends ModelReconcilerTest {

	public void testCreateModelReconciler() {
		assertNotNull(service.createModelReconciler());
	}

	public void testApplyDeltasUnfiltered() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setX(100);

		saveModel();

		ModelReconciler reconciler = service.createModelReconciler();
		reconciler.recordChanges(application);

		window.setX(200);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getX());

		service.applyDeltas(deltas);

		assertEquals(200, window.getX());
	}

	public void testApplyDeltasUnfiltered2() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setX(100);

		saveModel();

		ModelReconciler reconciler = service.createModelReconciler();
		reconciler.recordChanges(application);

		window.setX(200);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getX());

		service.applyDeltas(deltas, null);

		assertEquals(200, window.getX());
	}

	public void testApplyDeltasUnfiltered3() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setX(100);

		saveModel();

		ModelReconciler reconciler = service.createModelReconciler();
		reconciler.recordChanges(application);

		window.setX(200);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getX());

		service.applyDeltas(deltas, new String[0]);

		assertEquals(200, window.getX());
	}

	public void testApplyDeltasFiltered() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setX(100);

		saveModel();

		ModelReconciler reconciler = service.createModelReconciler();
		reconciler.recordChanges(application);

		window.setX(200);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getX());

		service.applyDeltas(deltas, new String[] { ModelReconciler.WINDOW_X_ATTNAME });

		assertEquals(100, window.getX());
	}
}
