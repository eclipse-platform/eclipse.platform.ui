/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.internal;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import org.eclipse.core.expressions.EqualsExpression;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.expressions.WithExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.ISources;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests various sources keyed off the workbench window.
 *
 * @since 3.3
 */
@RunWith(JUnit4.class)
@Ignore("Disabled due 544032, see also 485167")
public class WorkbenchWindowSubordinateSourcesTests extends UITestCase {

	private WorkbenchWindow window;

	public WorkbenchWindowSubordinateSourcesTests() {
		super(WorkbenchWindowSubordinateSourcesTests.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		window = (WorkbenchWindow) openTestWindow();
		processEvents();
	}

	@Test
	public void testIsCoolbarVisible() throws CoreException {
		IEvaluationService service = window.getService(IEvaluationService.class);
		IEvaluationContext context = service.getCurrentState();

		WithExpression with = new WithExpression(
				ISources.ACTIVE_WORKBENCH_WINDOW_IS_COOLBAR_VISIBLE_NAME);
		boolean current = window.getCoolBarVisible();
		EqualsExpression test = new EqualsExpression(current ? Boolean.TRUE
				: Boolean.FALSE);
		with.add(test);
		assertEquals(EvaluationResult.TRUE, with.evaluate(context));

		window.setCoolBarVisible(!current);
		assertEquals(EvaluationResult.FALSE, with.evaluate(context));
	}

	@Test
	public void testIsStatusLineVisible() throws CoreException {
		IEvaluationService service = window.getService(IEvaluationService.class);
		IEvaluationContext context = service.getCurrentState();

		WithExpression with = new WithExpression(
				ISources.ACTIVE_WORKBENCH_WINDOW_NAME + ".isStatusLineVisible");
		boolean current = window.getStatusLineVisible();
		EqualsExpression test = new EqualsExpression(current ? Boolean.TRUE : Boolean.FALSE);
		with.add(test);
		assertEquals(EvaluationResult.TRUE, with.evaluate(context));

		window.setStatusLineVisible(!current);
		assertEquals(EvaluationResult.FALSE, with.evaluate(context));
	}

	@Test
	public void testIsPerspectiveBarVisible() throws CoreException {
		IEvaluationService service = window.getService(IEvaluationService.class);
		IEvaluationContext context = service.getCurrentState();

		WithExpression with = new WithExpression(
				ISources.ACTIVE_WORKBENCH_WINDOW_IS_PERSPECTIVEBAR_VISIBLE_NAME);
		boolean current = window.getPerspectiveBarVisible();
		EqualsExpression test = new EqualsExpression(current ? Boolean.TRUE : Boolean.FALSE);
		with.add(test);
		assertEquals(EvaluationResult.TRUE, with.evaluate(context));

		window.setPerspectiveBarVisible(!current);
		assertEquals(EvaluationResult.FALSE, with.evaluate(context));
	}

	private static class PerspectiveL implements IPropertyChangeListener {
		Boolean val = null;

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			val = (Boolean) event.getNewValue();
		}
	}

	@Test
	public void testPerspectiveId() throws Exception {
		IEvaluationService service = window.getService(IEvaluationService.class);
		WithExpression with = new WithExpression(ISources.ACTIVE_WORKBENCH_WINDOW_ACTIVE_PERSPECTIVE_NAME);
		IPerspectiveDescriptor currentPerspective = window.getActivePage().getPerspective();
		String id = currentPerspective.getId();
		EqualsExpression test = new EqualsExpression(id);
		with.add(test);
		PerspectiveL listener = new PerspectiveL();
		service.addEvaluationListener(with, listener, ISources.ACTIVE_WORKBENCH_WINDOW_ACTIVE_PERSPECTIVE_NAME);
		assertEquals(Boolean.TRUE, listener.val);
		listener.val = null;

		final IPerspectiveRegistry registry = WorkbenchPlugin.getDefault().getPerspectiveRegistry();
		final IPerspectiveDescriptor perspective1 = registry
				.findPerspectiveWithId("org.eclipse.ui.tests.api.ViewPerspective");
		window.getActivePage().setPerspective(perspective1);
		assertEquals(Boolean.FALSE, listener.val);
		listener.val = null;
		window.getActivePage().closePerspective(perspective1, false, false);
		assertEquals(Boolean.TRUE, listener.val);
	}
}
