/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.internal;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.internal.expressions.EqualsExpression;
import org.eclipse.core.internal.expressions.WithExpression;
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

	@Override
	protected void doSetUp() throws Exception {
		window = (WorkbenchWindow) openTestWindow();
		processEvents();
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
			assertEquals(EvaluationResult.FALSE, with.evaluate(context));
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}

	public void testIsStatusLineVisible() {
		IEvaluationService service = (IEvaluationService) window
				.getService(IEvaluationService.class);
		IEvaluationContext context = service.getCurrentState();

		WithExpression with = new WithExpression(
				ISources.ACTIVE_WORKBENCH_WINDOW_NAME + ".isStatusLineVisible");
		boolean current = window.getStatusLineVisible();
		EqualsExpression test = new EqualsExpression(current ? Boolean.TRUE
				: Boolean.FALSE);
		with.add(test);

		try {
			assertEquals(EvaluationResult.TRUE, with.evaluate(context));
		} catch (CoreException e) {
			fail(e.getMessage());
		}

		window.setStatusLineVisible(current = !current);
		try {
			assertEquals(EvaluationResult.FALSE, with.evaluate(context));
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
			assertEquals(EvaluationResult.FALSE, with.evaluate(context));
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}

	private static class PerspectiveL implements IPropertyChangeListener {
		Boolean val = null;

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			val = (Boolean) event.getNewValue();
		}
	}

	public void testPerspectiveId() throws Exception {
		IEvaluationService service = (IEvaluationService) window
				.getService(IEvaluationService.class);
		WithExpression with = new WithExpression(
				ISources.ACTIVE_WORKBENCH_WINDOW_ACTIVE_PERSPECTIVE_NAME);
		IPerspectiveDescriptor currentPerspective = window.getActivePage().getPerspective();
		String id = currentPerspective.getId();
		EqualsExpression test = new EqualsExpression(id);
		with.add(test);
		PerspectiveL listener = new PerspectiveL();
		service.addEvaluationListener(with, listener,
				ISources.ACTIVE_WORKBENCH_WINDOW_ACTIVE_PERSPECTIVE_NAME);
		assertEquals(Boolean.TRUE, listener.val);
		listener.val = null;

		final IPerspectiveRegistry registry = WorkbenchPlugin.getDefault()
				.getPerspectiveRegistry();
		final IPerspectiveDescriptor perspective1 = registry
				.findPerspectiveWithId("org.eclipse.ui.tests.api.ViewPerspective");
		window.getActivePage().setPerspective(perspective1);
		assertEquals(Boolean.FALSE, listener.val);
		listener.val = null;
		window.getActivePage().closePerspective(perspective1, false, false);
		assertEquals(Boolean.TRUE, listener.val);
	}
}
