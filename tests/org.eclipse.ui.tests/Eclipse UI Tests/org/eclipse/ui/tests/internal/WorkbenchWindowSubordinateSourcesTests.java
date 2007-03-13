/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.internal;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.internal.expressions.EqualsExpression;
import org.eclipse.core.internal.expressions.WithExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.ISources;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.services.IEvaluationService;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Tests various sources keyed off the workbench window.
 * 
 * @since 3.3
 * 
 */
public class WorkbenchWindowSubordinateSourcesTests extends UITestCase {

	private WorkbenchWindow window;

	/**
	 * @param testName
	 */
	public WorkbenchWindowSubordinateSourcesTests(String testName) {
		super(testName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		window = (WorkbenchWindow) getWorkbench().openWorkbenchWindow(
				getPageInput());
	}

	public void testIsCoolbarVisible() {
		IEvaluationService service = (IEvaluationService) window
				.getService(IEvaluationService.class);
		IEvaluationContext context = service.getCurrentState();

		WithExpression with = new WithExpression(
				ISources.ACTIVE_WORKBENCH_WINDOW_IS_COOLBAR_VISIBLE_NAME);
		boolean current = window.getCoolBarVisible();
		EqualsExpression test = new EqualsExpression(current ? Boolean.TRUE
				: Boolean.FALSE);
		with.add(test);

		try {
			assertEquals(EvaluationResult.TRUE, with.evaluate(context));
		} catch (CoreException e) {
			fail(e.getMessage());
		}

		window.setCoolBarVisible(current = !current);
		try {
			assertEquals(EvaluationResult.TRUE, with.evaluate(context));
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}

	public void testIsPerspectiveBarVisible() {
		IEvaluationService service = (IEvaluationService) window
				.getService(IEvaluationService.class);
		IEvaluationContext context = service.getCurrentState();

		WithExpression with = new WithExpression(
				ISources.ACTIVE_WORKBENCH_WINDOW_IS_PERSPECTIVEBAR_VISIBLE_NAME);
		boolean current = window.getPerspectiveBarVisible();
		EqualsExpression test = new EqualsExpression(current ? Boolean.TRUE
				: Boolean.FALSE);
		with.add(test);

		try {
			assertEquals(EvaluationResult.TRUE, with.evaluate(context));
		} catch (CoreException e) {
			fail(e.getMessage());
		}

		window.setPerspectiveBarVisible(current = !current);
		try {
			assertEquals(EvaluationResult.TRUE, with.evaluate(context));
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}
}
