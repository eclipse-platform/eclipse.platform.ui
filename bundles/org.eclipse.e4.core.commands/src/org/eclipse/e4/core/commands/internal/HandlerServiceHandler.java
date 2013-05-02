/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.commands.internal;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl.ExecutionContexts;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;

/**
 * Provide an IHandler to delegate calls to.
 */
public class HandlerServiceHandler extends AbstractHandler {

	private static final String FAILED_TO_FIND_HANDLER_DURING_EXECUTION = "Failed to find handler during execution"; //$NON-NLS-1$
	protected String commandId;

	public HandlerServiceHandler(String commandId) {
		this.commandId = commandId;
	}

	@Override
	public boolean isEnabled() {
		ExecutionContexts contexts = HandlerServiceImpl.peek();
		// setEnabled(contexts);
		IEclipseContext executionContext = getExecutionContext(contexts);
		if (executionContext == null) {
			return super.isEnabled();
		}
		Object handler = HandlerServiceImpl.lookUpHandler(executionContext, commandId);
		if (handler == null) {
			setBaseEnabled(false);
			return super.isEnabled();
		}
		IEclipseContext staticContext = getStaticContext(contexts);
		Boolean result = (Boolean) ContextInjectionFactory.invoke(handler, CanExecute.class,
				executionContext, staticContext, Boolean.TRUE);
		setBaseEnabled(result.booleanValue());
		return super.isEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#setEnabled(java.lang.Object)
	 */
	@Override
	public void setEnabled(Object evaluationContext) {
		boolean createContext = false;
		IEclipseContext executionContext = getExecutionContext(evaluationContext);
		if (executionContext == null) {
			return;
		}
		Object handler = HandlerServiceImpl.lookUpHandler(executionContext, commandId);
		if (handler == null) {
			return;
		}
		IEclipseContext staticContext = getStaticContext(evaluationContext);
		if (staticContext == null) {
			staticContext = EclipseContextFactory.create();
			createContext = true;
		}
		ContextInjectionFactory.invoke(handler, SetEnabled.class, executionContext, staticContext,
				Boolean.TRUE);
		if (createContext) {
			staticContext.dispose();
		}
	}

	IEclipseContext getStaticContext(Object evalObj) {
		if (evalObj instanceof ExecutionContexts) {
			return ((ExecutionContexts) evalObj).staticContext;
		}
		if (evalObj instanceof IEclipseContext) {
			return (IEclipseContext) ((IEclipseContext) evalObj)
					.get(HandlerServiceImpl.STATIC_CONTEXT);
		}
		if (evalObj instanceof ExpressionContext) {
			return (IEclipseContext) (((ExpressionContext) evalObj).eclipseContext)
					.get(HandlerServiceImpl.STATIC_CONTEXT);
		}
		if (evalObj instanceof IEvaluationContext) {
			return getStaticContext(((IEvaluationContext) evalObj).getParent());
		}
		final ExecutionContexts pair = HandlerServiceImpl.peek();
		if (pair != null) {
			return pair.staticContext;
		}
		return null;
	}

	protected IEclipseContext getExecutionContext(Object evalObj) {
		if (evalObj instanceof ExecutionContexts) {
			return ((ExecutionContexts) evalObj).context;
		}
		if (evalObj instanceof IEclipseContext) {
			return (IEclipseContext) evalObj;
		}
		if (evalObj instanceof ExpressionContext) {
			return ((ExpressionContext) evalObj).eclipseContext;
		}
		if (evalObj instanceof IEvaluationContext) {
			return getExecutionContext(((IEvaluationContext) evalObj).getParent());
		}
		final ExecutionContexts pair = HandlerServiceImpl.peek();
		if (pair != null) {
			return pair.context;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#isHandled()
	 */
	@Override
	public boolean isHandled() {
		ExecutionContexts contexts = HandlerServiceImpl.peek();
		if (contexts != null) {
			Object handler = HandlerServiceImpl.lookUpHandler(contexts.context, commandId);
			if (handler instanceof IHandler) {
				return ((IHandler) handler).isHandled();
			}
			return handler != null;

		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEclipseContext executionContext = getExecutionContext(event.getApplicationContext());
		if (executionContext == null) {
			throw new ExecutionException(FAILED_TO_FIND_HANDLER_DURING_EXECUTION,
					new NotHandledException(FAILED_TO_FIND_HANDLER_DURING_EXECUTION));
		}

		IEclipseContext staticContext = getStaticContext(event.getApplicationContext());
		Object handler = HandlerServiceImpl.lookUpHandler(executionContext, commandId);
		if (handler == null) {
			return null;
		}
		return ContextInjectionFactory.invoke(handler, Execute.class, executionContext,
				staticContext, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#fireHandlerChanged(org.eclipse.core.commands.
	 * HandlerEvent)
	 */
	@Override
	public void fireHandlerChanged(HandlerEvent handlerEvent) {
		super.fireHandlerChanged(handlerEvent);
	}

	public void overrideEnabled(boolean b) {
		setBaseEnabled(b);
	}
}
