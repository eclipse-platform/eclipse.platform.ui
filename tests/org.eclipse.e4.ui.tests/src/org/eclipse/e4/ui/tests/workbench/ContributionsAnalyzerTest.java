/*******************************************************************************
 * Copyright (c) 2019 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertTrue;

import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.ui.MImperativeExpression;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ContributionsAnalyzerTest {

	private IEclipseContext appContext;
	private EModelService ems;

	@Before
	public void setUp() {
		appContext = E4Application.createDefaultContext();
		ems = appContext.get(EModelService.class);
	}

	@After
	public void tearDown() {
		appContext.dispose();
	}

	@Test
	public void testMImperativeExpressionInjectionWithPersistedState() {
		ExpressionContext eContext = new ExpressionContext(appContext);

		MImperativeExpression exp = ems.createModelElement(MImperativeExpression.class);
		exp.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.ImperativeExpressionTestEvaluationPersistedState");
		exp.getPersistedState().put(ImperativeExpressionTestEvaluationPersistedState.PERSISTED_STATE_TEST, "value");

		assertTrue(ContributionsAnalyzer.isVisible(exp, eContext));
	}
}
