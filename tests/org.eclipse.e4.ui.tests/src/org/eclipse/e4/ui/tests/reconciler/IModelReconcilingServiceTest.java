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
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;

public abstract class IModelReconcilingServiceTest extends ModelReconcilerTest {

	public void testCreateModelReconciler() {
		assertNotNull(service.createModelReconciler());
	}

	public void testApplyDeltasUnfiltered() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setLabel("windowName");

		saveModel();

		ModelReconciler reconciler = service.createModelReconciler();
		reconciler.recordChanges(application);

		window.setLabel("newName");

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals("windowName", window.getLabel());

		service.applyDeltas(deltas);

		assertEquals("newName", window.getLabel());
	}

	public void testApplyDeltasUnfiltered2() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setLabel("windowName");

		saveModel();

		ModelReconciler reconciler = service.createModelReconciler();
		reconciler.recordChanges(application);

		window.setLabel("newName");

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals("windowName", window.getLabel());

		service.applyDeltas(deltas, null);

		assertEquals("newName", window.getLabel());
	}

	public void testApplyDeltasUnfiltered3() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setLabel("windowName");

		saveModel();

		ModelReconciler reconciler = service.createModelReconciler();
		reconciler.recordChanges(application);

		window.setLabel("newName");

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals("windowName", window.getLabel());

		service.applyDeltas(deltas, new String[0]);

		assertEquals("newName", window.getLabel());
	}

	public void testApplyDeltasFiltered() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setLabel("windowName");

		saveModel();

		ModelReconciler reconciler = service.createModelReconciler();
		reconciler.recordChanges(application);

		window.setLabel("newName");

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals("windowName", window.getLabel());

		service.applyDeltas(deltas,
				new String[] { ModelReconciler.UILABEL_LABEL_ATTNAME });

		assertEquals("windowName", window.getLabel());
	}
}
