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
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerContributionTest extends
		ModelReconcilerTest {

	private void testContribution_PersistedState(String applicationState,
			String userChange, String newApplicationState) {
		MApplication application = createApplication();

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setPersistedState(applicationState);
		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setPersistedState(userChange);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);
		part.setPersistedState(newApplicationState);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(newApplicationState, part.getPersistedState());

		applyAll(deltas);

		if (applicationState == null) {
			if (userChange == null) {
				assertEquals(newApplicationState, part.getPersistedState());
			} else {
				assertEquals(userChange, part.getPersistedState());
			}
		} else {
			if (userChange == null || !applicationState.equals(userChange)) {
				assertEquals(userChange, part.getPersistedState());
			} else {
				assertEquals(newApplicationState, part.getPersistedState());
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
}
