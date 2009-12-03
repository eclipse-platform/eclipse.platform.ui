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
import java.util.List;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerGenericTileTest extends
		ModelReconcilerTest {

	public void testGenericTile_Weights_Add() {
		String applicationId = createId();
		String windowId = createId();

		String partSashContainerId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPartSashContainer partSashContainer = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		partSashContainer.setId(partSashContainerId);
		window.getChildren().add(partSashContainer);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		partSashContainer.getWeights().add(new Integer(50));

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		partSashContainer = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		partSashContainer.setId(partSashContainerId);
		window.getChildren().add(partSashContainer);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(0, partSashContainer.getWeights().size());

		applyAll(deltas);

		List<Integer> weights = partSashContainer.getWeights();
		assertEquals(1, weights.size());
		assertEquals(new Integer(50), weights.get(0));
	}

	public void testGenericTile_Weights_Add2() {
		String applicationId = createId();
		String windowId = createId();

		String partSashContainerId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPartSashContainer partSashContainer = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		partSashContainer.setId(partSashContainerId);
		partSashContainer.getWeights().add(new Integer(50));
		window.getChildren().add(partSashContainer);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		partSashContainer.getWeights().add(new Integer(100));

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		partSashContainer = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		partSashContainer.setId(partSashContainerId);
		partSashContainer.getWeights().add(new Integer(50));
		window.getChildren().add(partSashContainer);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(1, partSashContainer.getWeights().size());
		assertEquals(new Integer(50), partSashContainer.getWeights().get(0));

		applyAll(deltas);

		List<Integer> weights = partSashContainer.getWeights();
		assertEquals(2, weights.size());
		assertEquals(new Integer(50), weights.get(0));
		assertEquals(new Integer(100), weights.get(1));
	}

	public void testGenericTile_Weights_Remove() {
		String applicationId = createId();
		String windowId = createId();

		String partSashContainerId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPartSashContainer partSashContainer = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		partSashContainer.setId(partSashContainerId);
		window.getChildren().add(partSashContainer);

		partSashContainer.getWeights().add(new Integer(50));

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		partSashContainer.getWeights().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		partSashContainer = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		partSashContainer.setId(partSashContainerId);
		window.getChildren().add(partSashContainer);

		partSashContainer.getWeights().add(new Integer(50));

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(1, partSashContainer.getWeights().size());
		assertEquals(new Integer(50), partSashContainer.getWeights().get(0));

		applyAll(deltas);

		assertEquals(0, partSashContainer.getWeights().size());
	}

	public void testGenericTile_Weights_Remove2() {
		String applicationId = createId();
		String windowId = createId();

		String partSashContainerId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPartSashContainer partSashContainer = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		partSashContainer.setId(partSashContainerId);
		partSashContainer.getWeights().add(new Integer(50));
		partSashContainer.getWeights().add(new Integer(100));
		window.getChildren().add(partSashContainer);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		partSashContainer.getWeights().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		partSashContainer = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		partSashContainer.setId(partSashContainerId);
		partSashContainer.getWeights().add(new Integer(50));
		partSashContainer.getWeights().add(new Integer(100));
		window.getChildren().add(partSashContainer);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(2, partSashContainer.getWeights().size());
		assertEquals(new Integer(50), partSashContainer.getWeights().get(0));
		assertEquals(new Integer(100), partSashContainer.getWeights().get(1));

		applyAll(deltas);

		List<Integer> weights = partSashContainer.getWeights();
		assertEquals(1, weights.size());
		assertEquals(new Integer(100), weights.get(0));
	}

	private void testGenericTile_Horizontal(boolean applicationState,
			boolean userChange, boolean newApplicationState) {
		String applicationId = createId();
		String windowId = createId();

		String partSashContainerId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPartSashContainer partSashContainer = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		partSashContainer.setId(partSashContainerId);
		window.getChildren().add(partSashContainer);

		partSashContainer.setHorizontal(applicationState);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		partSashContainer.setHorizontal(userChange);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		partSashContainer = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		partSashContainer.setId(partSashContainerId);
		window.getChildren().add(partSashContainer);

		partSashContainer.setHorizontal(newApplicationState);

		Collection<ModelDelta> deltas = constructDeltas(application,
				state);

		assertEquals(newApplicationState, partSashContainer.isHorizontal());

		applyAll(deltas);

		if (userChange == applicationState) {
			// no change from the user, the new state is applied
			assertEquals(newApplicationState, partSashContainer.isHorizontal());
		} else {
			// user change must override application state
			assertEquals(userChange, partSashContainer.isHorizontal());
		}
	}

	public void testGenericTile_Horizontal_TrueTrueTrue() {
		testGenericTile_Horizontal(true, true, true);
	}

	public void testGenericTile_Horizontal_TrueTrueFalse() {
		testGenericTile_Horizontal(true, true, false);
	}

	public void testGenericTile_Horizontal_TrueFalseTrue() {
		testGenericTile_Horizontal(true, false, true);
	}

	public void testGenericTile_Horizontal_TrueFalseFalse() {
		testGenericTile_Horizontal(true, false, false);
	}

	public void testGenericTile_Horizontal_FalseTrueTrue() {
		testGenericTile_Horizontal(false, true, true);
	}

	public void testGenericTile_Horizontal_FalseTrueFalse() {
		testGenericTile_Horizontal(false, true, false);
	}

	public void testGenericTile_Horizontal_FalseFalseTrue() {
		testGenericTile_Horizontal(false, false, true);
	}

	public void testGenericTile_Horizontal_FalseFalseFalse() {
		testGenericTile_Horizontal(false, false, false);
	}
}
