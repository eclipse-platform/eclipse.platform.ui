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

package org.eclipse.ui.tests.services;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.expressions.WorkbenchWindowExpression;
import org.eclipse.ui.internal.services.IEvaluationReference;
import org.eclipse.ui.internal.services.IEvaluationService;
import org.eclipse.ui.tests.commands.ActiveContextExpression;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.3
 * 
 */
public class EvaluationServiceTest extends UITestCase {
	private static final String CONTEXT_ID1 = "org.eclipse.ui.command.contexts.evaluationService1";

	/**
	 * @param testName
	 */
	public EvaluationServiceTest(String testName) {
		super(testName);
	}

	private static class MyEval implements IPropertyChangeListener {
		public int count = 0;
		public boolean currentValue;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			count++;
			if (event.getProperty() == IEvaluationService.RESULT
					&& event.getNewValue() instanceof Boolean) {
				currentValue = ((Boolean) event.getNewValue()).booleanValue();
			}
		}
	}

	public void testBasicService() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IEvaluationService service = (IEvaluationService) window
				.getService(IEvaluationService.class);
		assertNotNull(service);

		MyEval listener = new MyEval();
		IContextActivation context1 = null;
		IEvaluationReference evalRef = null;
		IContextService contextService = null;
		try {
			evalRef = service.addEvaluationListener(
					new ActiveContextExpression(CONTEXT_ID1,
							new String[] { ISources.ACTIVE_CONTEXT_NAME }),
					listener, IEvaluationService.RESULT, null);
			assertEquals(1, listener.count);
			assertFalse(listener.currentValue);

			contextService = (IContextService) window
					.getService(IContextService.class);
			context1 = contextService.activateContext(CONTEXT_ID1);
			assertEquals(2, listener.count);
			assertTrue(listener.currentValue);

			contextService.deactivateContext(context1);
			context1 = null;
			assertEquals(3, listener.count);
			assertFalse(listener.currentValue);

			service.removeEvaluationListener(evalRef);
			evalRef = null;
			assertEquals(4, listener.count);

			context1 = contextService.activateContext(CONTEXT_ID1);
			assertEquals(4, listener.count);
			assertFalse(listener.currentValue);
			contextService.deactivateContext(context1);
			context1 = null;
			assertEquals(4, listener.count);
			assertFalse(listener.currentValue);
		} finally {
			if (context1 != null) {
				contextService.deactivateContext(context1);
			}
			if (evalRef != null) {
				service.removeEvaluationListener(evalRef);
			}
		}
	}

	public void testTwoEvaluations() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IEvaluationService service = (IEvaluationService) window
				.getService(IEvaluationService.class);
		assertNotNull(service);

		MyEval listener1 = new MyEval();
		MyEval listener2 = new MyEval();
		IContextActivation context1 = null;
		IEvaluationReference evalRef1 = null;
		IEvaluationReference evalRef2 = null;
		IContextService contextService = null;
		try {
			evalRef1 = service.addEvaluationListener(
					new ActiveContextExpression(CONTEXT_ID1,
							new String[] { ISources.ACTIVE_CONTEXT_NAME }),
					listener1, IEvaluationService.RESULT, null);
			assertEquals(1, listener1.count);
			assertFalse(listener1.currentValue);

			evalRef2 = service.addEvaluationListener(
					new ActiveContextExpression(CONTEXT_ID1,
							new String[] { ISources.ACTIVE_CONTEXT_NAME }),
					listener2, IEvaluationService.RESULT, null);
			assertEquals(1, listener2.count);
			assertFalse(listener2.currentValue);
			evalRef2.setResult(true);

			contextService = (IContextService) window
					.getService(IContextService.class);
			context1 = contextService.activateContext(CONTEXT_ID1);
			assertEquals(2, listener1.count);
			assertTrue(listener1.currentValue);
			// we already set this guy to true, he should skip
			assertEquals(1, listener2.count);
			assertFalse(listener2.currentValue);

			evalRef1.setResult(false);
			contextService.deactivateContext(context1);
			context1 = null;
			assertEquals(2, listener2.count);
			assertFalse(listener2.currentValue);
			
			// we already set this guy to false, so he should be the old
			// values
			assertEquals(2, listener1.count);
			assertTrue(listener1.currentValue);
			
		} finally {
			if (context1 != null) {
				contextService.deactivateContext(context1);
			}
			if (evalRef1 != null) {
				service.removeEvaluationListener(evalRef1);
			}
			if (evalRef2 != null) {
				service.removeEvaluationListener(evalRef2);
			}
		}
	}
	
	public void testRestriction() {
		IWorkbenchWindow window = openTestWindow();
		IEvaluationService evaluationService = (IEvaluationService) window
				.getService(IEvaluationService.class);
		assertNotNull(evaluationService);
		IContextService contextService = (IContextService) window
				.getService(IContextService.class);
		assertNotNull(contextService);

		Expression expression = new ActiveContextExpression(CONTEXT_ID1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });
		Expression restriction = new WorkbenchWindowExpression(window);

		final boolean[] propertyChanged = new boolean[1];

		IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals("foo"))
					propertyChanged[0] = true;

			}
		};
		evaluationService.addEvaluationListener(expression,
				propertyChangeListener, "foo", restriction);
		assertFalse(contextService.getActiveContextIds().contains(CONTEXT_ID1));
		IContextActivation activation = contextService.activateContext(CONTEXT_ID1);
		assertTrue(propertyChanged[0]);
		contextService.deactivateContext(activation);
		assertFalse(contextService.getActiveContextIds().contains(CONTEXT_ID1));
		propertyChanged[0] = false;
		
		// open second window
		openTestWindow();
		// ensure that our context expression was not re-evaluated
		activation = contextService.activateContext(CONTEXT_ID1);
		assertFalse(propertyChanged[0]);

	}
}
