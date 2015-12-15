/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.reconciler;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.junit.Test;

public abstract class ModelReconcilerMenuContributionsTest extends
		ModelReconcilerTest {

	@Test
	public void testMenuContributions_MenuContributions_Add() {
		MApplication application = createApplication();

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MMenuContribution contribution = ems.createModelElement(MMenuContribution.class);
		contribution.setElementId("contributionId");
		application.getMenuContributions().add(contribution);

		Object state = reconciler.serialize();

		application = createApplication();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, application.getMenuContributions().size());

		applyAll(deltas);

		assertEquals(1, application.getMenuContributions().size());

		contribution = application.getMenuContributions().get(0);
		assertEquals("contributionId", contribution.getElementId());
	}

	@Test
	public void testMenuContributions_MenuContributions_Remove() {
		MApplication application = createApplication();

		MMenuContribution contribution = ems.createModelElement(MMenuContribution.class);
		contribution.setElementId("contributionId");
		application.getMenuContributions().add(contribution);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		application.getMenuContributions().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		contribution = application.getMenuContributions().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getMenuContributions().size());
		assertEquals(contribution, application.getMenuContributions().get(0));
		assertEquals("contributionId", contribution.getElementId());

		applyAll(deltas);

		assertEquals(0, application.getMenuContributions().size());
	}
}
