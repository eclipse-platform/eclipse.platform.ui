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
		String applicationId = createId();
		String windowId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);
		window.setX(100);

		ModelReconciler reconciler = service.createModelReconciler();
		reconciler.recordChanges(application);

		window.setX(200);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);
		window.setX(100);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getX());

		service.applyDeltas(deltas);

		assertEquals(200, window.getX());
	}

	public void testApplyDeltasUnfiltered2() {
		String applicationId = createId();
		String windowId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);
		window.setX(100);

		ModelReconciler reconciler = service.createModelReconciler();
		reconciler.recordChanges(application);

		window.setX(200);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);
		window.setX(100);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getX());

		service.applyDeltas(deltas, null);

		assertEquals(200, window.getX());
	}

	public void testApplyDeltasUnfiltered3() {
		String applicationId = createId();
		String windowId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);
		window.setX(100);

		ModelReconciler reconciler = service.createModelReconciler();
		reconciler.recordChanges(application);

		window.setX(200);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);
		window.setX(100);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getX());

		service.applyDeltas(deltas, new String[0]);

		assertEquals(200, window.getX());
	}

	public void testApplyDeltasFiltered() {
		String applicationId = createId();
		String windowId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);
		window.setX(100);

		ModelReconciler reconciler = service.createModelReconciler();
		reconciler.recordChanges(application);

		window.setX(200);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);
		window.setX(100);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getX());

		service.applyDeltas(deltas, new String[] { ModelReconciler.X_ATTNAME });

		assertEquals(100, window.getX());
	}
}
