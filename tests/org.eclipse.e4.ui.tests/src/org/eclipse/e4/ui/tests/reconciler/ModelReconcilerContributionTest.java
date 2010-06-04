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
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerContributionTest extends
		ModelReconcilerTest {

	private void testContribution_PersistedState(String applicationState,
			String userChange, String newApplicationState) {
		MApplication application = createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.getPersistedState().put("testing", applicationState);
		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.getPersistedState().put("testing", userChange);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);
		part.getPersistedState().put("testing", newApplicationState);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(newApplicationState, part.getPersistedState().get(
				"testing"));

		applyAll(deltas);

		if (applicationState == null) {
			if (userChange == null) {
				assertEquals(newApplicationState, part.getPersistedState().get(
						"testing"));
			} else {
				assertEquals(userChange, part.getPersistedState()
						.get("testing"));
			}
		} else {
			if (userChange == null || !applicationState.equals(userChange)) {
				assertEquals(userChange, part.getPersistedState()
						.get("testing"));
			} else {
				assertEquals(newApplicationState, part.getPersistedState().get(
						"testing"));
			}
		}
	}

	public void testContribution_PersistedState_NullNullNull() {
		testContribution_PersistedState(null, null, null);
	}

	public void testContribution_PersistedState_NullNullEmpty() {
		testContribution_PersistedState(null, null, "");
	}

	public void testContribution_PersistedState_NullNullString() {
		testContribution_PersistedState(null, null, "state");
	}

	public void testContribution_PersistedState_NullEmptyNull() {
		testContribution_PersistedState(null, "", null);
	}

	public void testContribution_PersistedState_NullEmptyEmpty() {
		testContribution_PersistedState(null, "", "");
	}

	public void testContribution_PersistedState_NullEmptyString() {
		testContribution_PersistedState(null, "", "state");
	}

	public void testContribution_PersistedState_NullStringNull() {
		testContribution_PersistedState(null, "state", null);
	}

	public void testContribution_PersistedState_NullStringEmpty() {
		testContribution_PersistedState(null, "state", "");
	}

	public void testContribution_PersistedState_NullStringString() {
		testContribution_PersistedState(null, "state", "state");
	}

	public void testContribution_PersistedState_NullStringString2() {
		testContribution_PersistedState(null, "state", "state2");
	}

	public void testContribution_PersistedState_EmptyNullNull() {
		testContribution_PersistedState("", null, null);
	}

	public void testContribution_PersistedState_EmptyNullEmpty() {
		testContribution_PersistedState("", null, "");
	}

	public void testContribution_PersistedState_EmptyNullString() {
		testContribution_PersistedState("", null, "state");
	}

	public void testContribution_PersistedState_EmptyEmptyNull() {
		testContribution_PersistedState("", "", null);
	}

	public void testContribution_PersistedState_EmptyEmptyEmpty() {
		testContribution_PersistedState("", "", "");
	}

	public void testContribution_PersistedState_EmptyEmptyString() {
		testContribution_PersistedState("", "", "state");
	}

	public void testContribution_PersistedState_EmptyStringNull() {
		testContribution_PersistedState("", "state", null);
	}

	public void testContribution_PersistedState_EmptyStringEmpty() {
		testContribution_PersistedState("", "state", "");
	}

	public void testContribution_PersistedState_EmptyStringString() {
		testContribution_PersistedState("", "state", "state");
	}

	public void testContribution_PersistedState_EmptyStringString2() {
		testContribution_PersistedState("", "state", "state2");
	}

	public void testContribution_PersistedState_StringNullNull() {
		testContribution_PersistedState("state", null, null);
	}

	public void testContribution_PersistedState_StringNullEmpty() {
		testContribution_PersistedState("state", null, "");
	}

	public void testContribution_PersistedState_StringNullString() {
		testContribution_PersistedState("state", null, "state");
	}

	public void testContribution_PersistedState_StringNullString2() {
		testContribution_PersistedState("state", null, "state2");
	}

	public void testContribution_PersistedState_StringEmptyNull() {
		testContribution_PersistedState("state", "", null);
	}

	public void testContribution_PersistedState_StringEmptyEmpty() {
		testContribution_PersistedState("state", "", "");
	}

	public void testContribution_PersistedState_StringEmptyString() {
		testContribution_PersistedState("state", "", "state");
	}

	public void testContribution_PersistedState_StringEmptyString2() {
		testContribution_PersistedState("state", "", "state2");
	}

	public void testContribution_PersistedState_StringStringNull() {
		testContribution_PersistedState("state", "state", null);
	}

	public void testContribution_PersistedState_StringString2Null() {
		testContribution_PersistedState("state", "state2", null);
	}

	public void testContribution_PersistedState_StringStringEmpty() {
		testContribution_PersistedState("state", "state", "");
	}

	public void testContribution_PersistedState_StringString2Empty() {
		testContribution_PersistedState("state", "state2", "");
	}

	public void testContribution_PersistedState_StringStringString() {
		testContribution_PersistedState("state", "state", "state");
	}

	public void testContribution_PersistedState_StringStringString2() {
		testContribution_PersistedState("state", "state", "state2");
	}

	public void testContribution_PersistedState_StringString2String() {
		testContribution_PersistedState("state", "state2", "state");
	}

	public void testContribution_PersistedState_StringString2String2() {
		testContribution_PersistedState("state", "state2", "state2");
	}

	public void testContribution_NewContribution() {
		MApplication application = createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.getPersistedState().put("key", "value");
		window.getChildren().add(part);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, window.getChildren().size());

		applyAll(deltas);

		part = (MPart) window.getChildren().get(0);
		assertEquals(1, part.getPersistedState().size());
		assertEquals("value", part.getPersistedState().get("key"));
	}

	public void testContribution_NewPersistedState() {
		MApplication application = createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.getPersistedState().put("key", "value");

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, window.getChildren().size());
		assertEquals(part, window.getChildren().get(0));
		assertEquals(0, part.getPersistedState().size());

		applyAll(deltas);

		part = (MPart) window.getChildren().get(0);
		assertEquals(1, part.getPersistedState().size());
		assertEquals("value", part.getPersistedState().get("key"));
	}

	private void testContribution_URI(String applicationURI, String userChange,
			String newApplicationURI) {
		MApplication application = createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI(applicationURI);
		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setContributionURI(userChange);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);
		part.setContributionURI(newApplicationURI);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(newApplicationURI, part.getContributionURI());

		applyAll(deltas);

		if (applicationURI == null) {
			if (userChange == null) {
				assertEquals(newApplicationURI, part.getContributionURI());
			} else {
				assertEquals(userChange, part.getContributionURI());
			}
		} else {
			if (userChange == null || !applicationURI.equals(userChange)) {
				assertEquals(userChange, part.getContributionURI());
			} else {
				assertEquals(newApplicationURI, part.getContributionURI());
			}
		}
	}

	public void testContribution_URI_NullNullNull() {
		testContribution_URI(null, null, null);
	}

	public void testContribution_URI_NullNullEmpty() {
		testContribution_URI(null, null, "");
	}

	public void testContribution_URI_NullNullString() {
		testContribution_URI(null, null, "uri");
	}

	public void testContribution_URI_NullEmptyNull() {
		testContribution_URI(null, "", null);
	}

	public void testContribution_URI_NullEmptyEmpty() {
		testContribution_URI(null, "", "");
	}

	public void testContribution_URI_NullEmptyString() {
		testContribution_URI(null, "", "uri");
	}

	public void testContribution_URI_NullStringNull() {
		testContribution_URI(null, "uri", null);
	}

	public void testContribution_URI_NullStringEmpty() {
		testContribution_URI(null, "uri", "");
	}

	public void testContribution_URI_NullStringString() {
		testContribution_URI(null, "uri", "uri");
	}

	public void testContribution_URI_NullStringString2() {
		testContribution_URI(null, "uri", "uri2");
	}

	public void testContribution_URI_EmptyNullNull() {
		testContribution_URI("", null, null);
	}

	public void testContribution_URI_EmptyNullEmpty() {
		testContribution_URI("", null, "");
	}

	public void testContribution_URI_EmptyNullString() {
		testContribution_URI("", null, "uri");
	}

	public void testContribution_URI_EmptyEmptyNull() {
		testContribution_URI("", "", null);
	}

	public void testContribution_URI_EmptyEmptyEmpty() {
		testContribution_URI("", "", "");
	}

	public void testContribution_URI_EmptyEmptyString() {
		testContribution_URI("", "", "uri");
	}

	public void testContribution_URI_EmptyStringNull() {
		testContribution_URI("", "uri", null);
	}

	public void testContribution_URI_EmptyStringEmpty() {
		testContribution_URI("", "uri", "");
	}

	public void testContribution_URI_EmptyStringString() {
		testContribution_URI("", "uri", "uri");
	}

	public void testContribution_URI_EmptyStringString2() {
		testContribution_URI("", "uri", "uri2");
	}

	public void testContribution_URI_StringNullNull() {
		testContribution_URI("uri", null, null);
	}

	public void testContribution_URI_StringNullEmpty() {
		testContribution_URI("uri", null, "");
	}

	public void testContribution_URI_StringNullString() {
		testContribution_URI("uri", null, "uri");
	}

	public void testContribution_URI_StringNullString2() {
		testContribution_URI("uri", null, "uri2");
	}

	public void testContribution_URI_StringEmptyNull() {
		testContribution_URI("uri", "", null);
	}

	public void testContribution_URI_StringEmptyEmpty() {
		testContribution_URI("uri", "", "");
	}

	public void testContribution_URI_StringEmptyString() {
		testContribution_URI("uri", "", "uri");
	}

	public void testContribution_URI_StringEmptyString2() {
		testContribution_URI("uri", "", "uri2");
	}

	public void testContribution_URI_StringStringNull() {
		testContribution_URI("uri", "uri", null);
	}

	public void testContribution_URI_StringString2Null() {
		testContribution_URI("uri", "uri2", null);
	}

	public void testContribution_URI_StringStringEmpty() {
		testContribution_URI("uri", "uri", "");
	}

	public void testContribution_URI_StringString2Empty() {
		testContribution_URI("uri", "uri2", "");
	}

	public void testContribution_URI_StringStringString() {
		testContribution_URI("uri", "uri", "uri");
	}

	public void testContribution_URI_StringStringString2() {
		testContribution_URI("uri", "uri", "uri2");
	}

	public void testContribution_URI_StringString2String() {
		testContribution_URI("uri", "uri2", "uri");
	}

	public void testContribution_URI_StringString2String2() {
		testContribution_URI("uri", "uri2", "uri2");
	}
}
