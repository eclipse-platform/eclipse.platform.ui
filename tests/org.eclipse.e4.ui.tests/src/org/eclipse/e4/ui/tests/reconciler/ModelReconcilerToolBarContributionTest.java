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
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.junit.Test;

public abstract class ModelReconcilerToolBarContributionTest extends
		ModelReconcilerTest {

	private void testToolBarContribution_PositionInParent(String before,
			String after) {
		MApplication application = createApplication();

		MToolBarContribution contribution = ems.createModelElement(MToolBarContribution.class);
		application.getToolBarContributions().add(contribution);
		contribution.setPositionInParent(before);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		contribution.setPositionInParent(after);

		Object state = reconciler.serialize();

		application = createApplication();
		contribution = application.getToolBarContributions().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getToolBarContributions().size());
		assertEquals(contribution, application.getToolBarContributions().get(0));
		assertEquals(before, contribution.getPositionInParent());

		applyAll(deltas);

		assertEquals(1, application.getToolBarContributions().size());
		assertEquals(contribution, application.getToolBarContributions().get(0));
		assertEquals(after, contribution.getPositionInParent());
	}

	@Test
	public void testToolBarContribution_PositionInParent_NullNull() {
		testToolBarContribution_PositionInParent(null, null);
	}

	@Test
	public void testToolBarContribution_PositionInParent_NullEmpty() {
		testToolBarContribution_PositionInParent(null, "");
	}

	@Test
	public void testToolBarContribution_PositionInParent_NullString() {
		testToolBarContribution_PositionInParent(null, "id");
	}

	@Test
	public void testToolBarContribution_PositionInParent_EmptyNull() {
		testToolBarContribution_PositionInParent("", null);
	}

	@Test
	public void testToolBarContribution_PositionInParent_EmptyEmpty() {
		testToolBarContribution_PositionInParent("", "");
	}

	@Test
	public void testToolBarContribution_PositionInParent_EmptyString() {
		testToolBarContribution_PositionInParent("", "id");
	}

	@Test
	public void testToolBarContribution_PositionInParent_StringNull() {
		testToolBarContribution_PositionInParent("id", null);
	}

	@Test
	public void testToolBarContribution_PositionInParent_StringEmpty() {
		testToolBarContribution_PositionInParent("id", "");
	}

	@Test
	public void testToolBarContribution_PositionInParent_StringStringUnchanged() {
		testToolBarContribution_PositionInParent("id", "id");
	}

	@Test
	public void testToolBarContribution_PositionInParent_StringStringChanged() {
		testToolBarContribution_PositionInParent("id", "id2");
	}

	private void testToolBarContribution_ParentId(String before, String after) {
		MApplication application = createApplication();

		MToolBarContribution contribution = ems.createModelElement(MToolBarContribution.class);
		application.getToolBarContributions().add(contribution);
		contribution.setParentId(before);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		contribution.setParentId(after);

		Object state = reconciler.serialize();

		application = createApplication();
		contribution = application.getToolBarContributions().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getToolBarContributions().size());
		assertEquals(contribution, application.getToolBarContributions().get(0));
		assertEquals(before, contribution.getParentId());

		applyAll(deltas);

		assertEquals(1, application.getToolBarContributions().size());
		assertEquals(contribution, application.getToolBarContributions().get(0));
		assertEquals(after, contribution.getParentId());
	}

	@Test
	public void testToolBarContribution_ParentId_NullNull() {
		testToolBarContribution_ParentId(null, null);
	}

	@Test
	public void testToolBarContribution_ParentId_NullEmpty() {
		testToolBarContribution_ParentId(null, "");
	}

	@Test
	public void testToolBarContribution_ParentId_NullString() {
		testToolBarContribution_ParentId(null, "id");
	}

	@Test
	public void testToolBarContribution_ParentId_EmptyNull() {
		testToolBarContribution_ParentId("", null);
	}

	@Test
	public void testToolBarContribution_ParentId_EmptyEmpty() {
		testToolBarContribution_ParentId("", "");
	}

	@Test
	public void testToolBarContribution_ParentId_EmptyString() {
		testToolBarContribution_ParentId("", "id");
	}

	@Test
	public void testToolBarContribution_ParentId_StringNull() {
		testToolBarContribution_ParentId("id", null);
	}

	@Test
	public void testToolBarContribution_ParentId_StringEmpty() {
		testToolBarContribution_ParentId("id", "");
	}

	@Test
	public void testToolBarContribution_ParentId_StringStringUnchanged() {
		testToolBarContribution_ParentId("id", "id");
	}

	@Test
	public void testToolBarContribution_ParentId_StringStringChanged() {
		testToolBarContribution_ParentId("id", "id2");
	}
}
