/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.handlers;

import jakarta.inject.Named;
import java.util.Collections;
import java.util.Map;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.commands.IObjectWithState;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.State;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.commands.internal.HandlerServiceHandler;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.e4.core.commands.internal.SetEnabled;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.menus.UIElement;

/**
 * @since 3.5
 */
public class E4HandlerProxy implements IHandler2, IHandlerListener, IElementUpdater, IObjectWithState {
	public HandlerActivation activation;
	private final Command command;
	private final IHandler handler;
	private boolean logExecute = true;
	private boolean logSetEnabled = true;

	public E4HandlerProxy(Command command, IHandler handler) {
		this.command = command;
		this.handler = handler;
		handler.addHandlerListener(this);
	}

	@CanExecute
	public boolean canExecute(IEclipseContext context, @Optional IEvaluationContext staticContext,
			MApplication application) {
		if (handler instanceof IHandler2) {
			Object ctx = staticContext;
			if (ctx == null) {
				if (context != null) {
					ctx = new ExpressionContext(context != null ? context : application.getContext());
				}
			}
			((IHandler2) handler).setEnabled(ctx);
		}
		return handler.isEnabled();
	}

	@Execute
	public Object execute(IEclipseContext context, @Optional @Named(HandlerServiceImpl.PARM_MAP) Map parms,
			@Optional Event trigger, @Optional IEvaluationContext staticContext)
			throws ExecutionException, NotHandledException {
		if (Policy.DEBUG_CMDS) {
			Activator.trace(Policy.DEBUG_CMDS_FLAG, "execute " + command + " and " //$NON-NLS-1$ //$NON-NLS-2$
					+ handler + " with: " + context, null); //$NON-NLS-1$
		}
		IEvaluationContext appContext = staticContext;
		if (appContext == null) {
			appContext = new ExpressionContext(context);
		}
		ExecutionEvent event = new ExecutionEvent(command, parms == null ? Collections.EMPTY_MAP : parms, trigger,
				appContext);
		if (handler != null) {
			if (handler.isHandled()) {
				return handler.execute(event);
			}
			throw new NotHandledException("Handler " + handler //$NON-NLS-1$
					+ " is not handled for for command " + command); //$NON-NLS-1$
		}
		throw new NotHandledException("There is no handler to execute for command " + command); //$NON-NLS-1$
	}

	public IHandler getHandler() {
		return handler;
	}

	@Override
	public void handlerChanged(HandlerEvent handlerEvent) {
		IHandler handler = command.getHandler();
		if (handler instanceof HandlerServiceHandler) {
			IEclipseContext appContext = ((Workbench) PlatformUI.getWorkbench()).getApplication().getContext();
			if (HandlerServiceImpl.lookUpHandler(appContext, command.getId()) == this) {
				((HandlerServiceHandler) handler).fireHandlerChanged(handlerEvent);
			}
		}
	}

	@Override
	public void updateElement(UIElement element, Map parameters) {
		if (handler instanceof IElementUpdater) {
			((IElementUpdater) handler).updateElement(element, parameters);
		}
	}

	@SetEnabled
	void setEnabled(IEclipseContext context, @Optional IEvaluationContext evalContext) {
		if (evalContext == null) {
			evalContext = new ExpressionContext(context);
		}
		if (handler instanceof IHandler2) {
			((IHandler2) handler).setEnabled(evalContext);
		}
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		handler.addHandlerListener(handlerListener);
	}

	@Override
	public void dispose() {
		handler.dispose();
	}

	@Override
	public Object execute(ExecutionEvent event) {
		if (logExecute) {
			logExecute = false;
			Status status = new Status(IStatus.WARNING, "org.eclipse.ui", //$NON-NLS-1$
					"Called handled proxy execute(*) directly" + command, new Exception()); //$NON-NLS-1$
			WorkbenchPlugin.log(status);
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return handler.isEnabled();
	}

	@Override
	public boolean isHandled() {
		return handler.isHandled();
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		handler.removeHandlerListener(handlerListener);
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		if (logSetEnabled) {
			logSetEnabled = false;
			Status status = new Status(IStatus.WARNING, "org.eclipse.ui", //$NON-NLS-1$
					"Called handled proxy setEnabled(*) directly" + command, new Exception()); //$NON-NLS-1$
			WorkbenchPlugin.log(status);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("E4HandlerProxy ["); //$NON-NLS-1$
		builder.append("handler="); //$NON-NLS-1$
		builder.append(handler);
		if (command != null) {
			builder.append(", "); //$NON-NLS-1$
			builder.append("command="); //$NON-NLS-1$
			builder.append(command);
		}
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}

	@Override
	public void addState(String id, State state) {
		if (handler instanceof IObjectWithState) {
			((IObjectWithState) handler).addState(id, state);
		}
	}

	@Override
	public State getState(String stateId) {
		if (handler instanceof IObjectWithState) {
			return ((IObjectWithState) handler).getState(stateId);
		}
		return null;
	}

	@Override
	public String[] getStateIds() {
		if (handler instanceof IObjectWithState) {
			return ((IObjectWithState) handler).getStateIds();
		}
		return new String[0];
	}

	@Override
	public void removeState(String stateId) {
		if (handler instanceof IObjectWithState) {
			((IObjectWithState) handler).removeState(stateId);
		}
	}

	@Override
	public String getHandlerLabel() {
		return handler.getHandlerLabel();
	}

}
