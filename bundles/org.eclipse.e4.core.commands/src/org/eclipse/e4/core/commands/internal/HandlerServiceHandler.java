/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.e4.core.commands.internal;

import java.lang.ref.WeakReference;
import org.eclipse.core.commands.AbstractHandlerWithState;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IObjectWithState;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.State;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl.ExecutionContexts;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;

/**
 * Provide an IHandler to delegate calls to.
 */
public class HandlerServiceHandler extends AbstractHandlerWithState {

	private static final String FAILED_TO_FIND_HANDLER_DURING_EXECUTION = "Failed to find handler during execution"; //$NON-NLS-1$
	private static final String HANDLER_MISSING_EXECUTE_ANNOTATION = " handler is missing @Execute"; //$NON-NLS-1$
	private static final Object missingExecute = new Object();

	protected final String commandId;
	// Remove state from currentStateHandler when it goes out of scope
	protected WeakReference<IObjectWithState> currentStateHandler = new WeakReference<IObjectWithState>(null);

	public HandlerServiceHandler(String commandId) {
		this.commandId = commandId;
	}

	@Override
	public boolean isEnabled() {
		ExecutionContexts contexts = HandlerServiceImpl.peek();
		// setEnabled(contexts);
		IEclipseContext executionContext = contexts != null ? contexts.context : null; // getExecutionContext(contexts);
		if (executionContext == null) {
			return super.isEnabled();
		}
		Object handler = HandlerServiceImpl.lookUpHandler(executionContext, commandId);
		switchHandler(handler);
		if (handler == null) {
			setBaseEnabled(false);
			return super.isEnabled();
		}
		IEclipseContext staticContext = contexts.staticContext; // getStaticContext(contexts);
		Boolean result = (Boolean) ContextInjectionFactory.invoke(handler, CanExecute.class,
				executionContext, staticContext, Boolean.TRUE);
		setBaseEnabled(result.booleanValue());
		return super.isEnabled();
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		boolean createContext = false;
		IEclipseContext executionContext = getExecutionContext(evaluationContext);
		if (executionContext == null) {
			return;
		}
		Object handler = HandlerServiceImpl.lookUpHandler(executionContext, commandId);
		switchHandler(handler);
		if (handler == null) {
			return;
		}
		IEclipseContext staticContext = getStaticContext(executionContext);
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

	private IEclipseContext getStaticContext(IEclipseContext executionContext) {
		final ExecutionContexts pair = HandlerServiceImpl.peek();
		if (pair != null) {
			if (pair.context != executionContext) {
				// log this
			}
			return pair.staticContext;
		}
		return null;
	}

	protected IEclipseContext getExecutionContext(Object evalObj) {
		// if (evalObj instanceof ExecutionContexts) {
		// return ((ExecutionContexts) evalObj).context;
		// }
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

	@Override
	public boolean isHandled() {
		ExecutionContexts contexts = HandlerServiceImpl.peek();
		if (contexts != null) {
			Object handler = HandlerServiceImpl.lookUpHandler(contexts.context, commandId);
			switchHandler(handler);
			if (handler instanceof IHandler) {
				return ((IHandler) handler).isHandled();
			}
			return handler != null;

		}
		return false;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEclipseContext executionContext = getExecutionContext(event.getApplicationContext());
		if (executionContext == null) {
			throw new ExecutionException(FAILED_TO_FIND_HANDLER_DURING_EXECUTION,
					new NotHandledException(FAILED_TO_FIND_HANDLER_DURING_EXECUTION));
		}

		Object handler = HandlerServiceImpl.lookUpHandler(executionContext, commandId);
		switchHandler(handler);
		if (handler == null) {
			return null;
		}
		IEclipseContext staticContext = getStaticContext(executionContext);
		IEclipseContext localStaticContext = null;
		try {
			if (staticContext == null) {
				staticContext = localStaticContext = EclipseContextFactory
						.create(HandlerServiceImpl.TMP_STATIC_CONTEXT);
				staticContext.set(HandlerServiceImpl.PARM_MAP, event.getParameters());
			}
			Object result = ContextInjectionFactory.invoke(handler, Execute.class,
 executionContext,
					staticContext, missingExecute);
			if (result == missingExecute) {
				throw new ExecutionException(handler.getClass().getName() + HANDLER_MISSING_EXECUTE_ANNOTATION,
						new NotHandledException(getClass().getName()));
			}
			return result;
		} catch (InjectionException e) {
			if (e.getCause() instanceof ExecutionException) {
				ExecutionException executionException = (ExecutionException) e.getCause();
				throw executionException;
			}
			String message = "Error executing '" + commandId + "': " + e.getMessage(); //$NON-NLS-1$ //$NON-NLS-2$
			throw new ExecutionException(message, e);
		} finally {
			if (localStaticContext != null) {
				localStaticContext.dispose();
			}
		}
	}

	@Override
	public void fireHandlerChanged(HandlerEvent handlerEvent) {
		super.fireHandlerChanged(handlerEvent);
	}

	public void overrideEnabled(boolean b) {
		setBaseEnabled(b);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(\"" + commandId + "\")"; //$NON-NLS-1$//$NON-NLS-2$
	}

	@Override
	public void addState(String stateId, State state) {
		super.addState(stateId, state);
		IObjectWithState handler = lookUpHandlerWithState();
		if (handler != null) {
			handler.addState(stateId, state);
		}
	}

	@Override
	public void removeState(String stateId) {
		IObjectWithState handler = lookUpHandlerWithState();
		if (handler != null) {
			handler.removeState(stateId);
		}
		super.removeState(stateId);
	}

	@Override
	public void handleStateChange(State state, Object oldValue) {
	}

	@Override
	public void dispose() {
		switchHandler(null);
		super.dispose();
	}

	private IObjectWithState lookUpHandlerWithState() {
		ExecutionContexts contexts = HandlerServiceImpl.peek();
		if (contexts == null) {
			return null;
		}
		Object handler = HandlerServiceImpl.lookUpHandler(contexts.context, commandId);
		switchHandler(handler);
		if (!(handler instanceof IObjectWithState)) {
			return null;
		}
		if (handler instanceof IHandler) {
			if (!((IHandler) handler).isHandled()) {
				return null;
			}
		}
		return (IObjectWithState) handler;
	}

	private void switchHandler(Object handler) {
		IObjectWithState typed;
		if (handler instanceof IObjectWithState) {
			typed = (IObjectWithState) handler;
		} else {
			typed = null;
		}

		IObjectWithState oldHandler = currentStateHandler.get();
		if (oldHandler == typed) {
			return;
		}
		currentStateHandler = new WeakReference<>(typed);
		for (String id : getStateIds()) {
			if (oldHandler != null) {
				oldHandler.removeState(id);
			}
			if (typed != null) {
				typed.addState(id, getState(id));
			}
		}

		fireHandlerChanged(new HandlerEvent(this, false, false));
	}

}
