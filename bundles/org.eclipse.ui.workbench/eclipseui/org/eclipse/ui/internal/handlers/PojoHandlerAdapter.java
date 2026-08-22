/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Adapts a handler contribution that does not implement
 * {@link org.eclipse.core.commands.IHandler} to the legacy handler API by
 * dispatching to its {@link Execute} and {@link CanExecute} methods through
 * dependency injection.
 */
class PojoHandlerAdapter extends AbstractHandler {

	private static final Object MISSING_EXECUTE = new Object();

	private final Object handler;

	/** The context the handler was injected from, may be <code>null</code>. */
	private final IEclipseContext injectionContext;

	/**
	 * Injects the given handler and prepares it for dispatch.
	 *
	 * @throws InjectionException if the handler cannot be injected
	 */
	PojoHandlerAdapter(Object handler, IEclipseContext injectionContext) {
		this.handler = handler;
		this.injectionContext = injectionContext;
		if (injectionContext != null) {
			ContextInjectionFactory.inject(handler, injectionContext);
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEclipseContext executionContext = executionContextOf(event.getApplicationContext());
		if (executionContext == null) {
			throw new ExecutionException("No IEclipseContext available to execute " + handler.getClass().getName()); //$NON-NLS-1$
		}
		IEclipseContext staticContext = EclipseContextFactory.create();
		try {
			staticContext.set(HandlerServiceImpl.PARM_MAP, event.getParameters());
			staticContext.set(ExecutionEvent.class, event);
			if (event.getTrigger() instanceof Event trigger) {
				staticContext.set(Event.class, trigger);
			}
			Object result = ContextInjectionFactory.invoke(handler, Execute.class, executionContext, staticContext,
					MISSING_EXECUTE);
			if (result == MISSING_EXECUTE) {
				throw new ExecutionException(handler.getClass().getName() + " handler is missing @Execute", //$NON-NLS-1$
						new NotHandledException(handler.getClass().getName()));
			}
			return result;
		} catch (InjectionException e) {
			if (e.getCause() instanceof ExecutionException cause) {
				throw cause;
			}
			throw new ExecutionException("Error executing " + handler.getClass().getName(), e); //$NON-NLS-1$
		} finally {
			staticContext.dispose();
		}
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		IEclipseContext executionContext = executionContextOf(evaluationContext);
		if (executionContext == null) {
			return;
		}
		IEclipseContext staticContext = EclipseContextFactory.create();
		try {
			Object result = ContextInjectionFactory.invoke(handler, CanExecute.class, executionContext, staticContext,
					Boolean.TRUE);
			// a @CanExecute that does not return boolean leaves enablement untouched
			if (result instanceof Boolean enabled) {
				setBaseEnabled(enabled.booleanValue());
			}
		} catch (InjectionException e) {
			WorkbenchPlugin.log("Error while evaluating @CanExecute of " + handler.getClass().getName(), e); //$NON-NLS-1$
			setBaseEnabled(false);
		} finally {
			staticContext.dispose();
		}
	}

	/**
	 * Unwraps the {@link IEclipseContext} the given evaluation object was created
	 * from, falling back to the active leaf of the injection context.
	 */
	private IEclipseContext executionContextOf(Object evaluationObject) {
		if (evaluationObject instanceof IEclipseContext context) {
			return context;
		}
		if (evaluationObject instanceof ExpressionContext context) {
			return context.eclipseContext;
		}
		if (evaluationObject instanceof IEvaluationContext context) {
			return executionContextOf(context.getParent());
		}
		return injectionContext == null ? null : injectionContext.getActiveLeaf();
	}

	@Override
	public void dispose() {
		if (injectionContext != null) {
			try {
				ContextInjectionFactory.uninject(handler, injectionContext);
			} catch (InjectionException e) {
				WorkbenchPlugin.log("Error while uninjecting " + handler.getClass().getName(), e); //$NON-NLS-1$
			}
		}
		super.dispose();
	}

	Object getHandler() {
		return handler;
	}

	@Override
	public String toString() {
		return handler.toString();
	}
}
