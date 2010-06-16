/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerTrimContributionsTest extends
		ModelReconcilerTest {

	public void testTrimContributions_TrimContributions_Add() {
		MApplication application = createApplication();

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MTrimContribution contribution = MenuFactoryImpl.eINSTANCE
				.createTrimContribution();
		contribution.setElementId("contributionId");
		application.getTrimContributions().add(contribution);

		Object state = reconciler.serialize();

		application = createApplication();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, application.getTrimContributions().size());

		applyAll(deltas);

		assertEquals(1, application.getTrimContributions().size());

		contribution = application.getTrimContributions().get(0);
		assertEquals("contributionId", contribution.getElementId());
	}

	public void testTrimContributions_TrimContributions_Remove() {
		MApplication application = createApplication();

		MTrimContribution contribution = MenuFactoryImpl.eINSTANCE
				.createTrimContribution();
		contribution.setElementId("contributionId");
		application.getTrimContributions().add(contribution);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		application.getTrimContributions().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		contribution = application.getTrimContributions().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getTrimContributions().size());
		assertEquals(contribution, application.getTrimContributions().get(0));
		assertEquals("contributionId", contribution.getElementId());

		applyAll(deltas);

		assertEquals(0, application.getTrimContributions().size());
	}
}
