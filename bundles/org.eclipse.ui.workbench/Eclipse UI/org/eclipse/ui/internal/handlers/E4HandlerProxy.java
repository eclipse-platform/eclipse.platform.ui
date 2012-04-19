/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import javax.inject.Named;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.MakeHandlersGo;
import org.eclipse.ui.internal.Workbench;

/**
 * @since 3.5
 * 
 */
public class E4HandlerProxy implements IHandlerListener {
	public HandlerActivation activation = null;
	private Command command;
	private IHandler handler;

	public E4HandlerProxy(Command command, IHandler handler) {
		this.command = command;
		this.handler = handler;
		handler.addHandlerListener(this);
	}

	@CanExecute
	public boolean canExecute(IEclipseContext context, @Optional IEvaluationContext staticContext) {
		if (handler instanceof IHandler2) {
			((IHandler2) handler).setEnabled(staticContext == null ? new ExpressionContext(context)
					: staticContext);
		}
		return handler.isEnabled();
	}

	@Execute
	public Object execute(IEclipseContext context, CommandManager manager,
			@Optional @Named(HandlerServiceImpl.PARM_MAP) Map parms, @Optional Event trigger,
			@Optional IEvaluationContext staticContext) throws ExecutionException,
			NotDefinedException, NotHandledException {
		Activator.trace(Policy.DEBUG_CMDS, "execute " + command + " and " //$NON-NLS-1$ //$NON-NLS-2$
				+ handler + " with: " + context, null); //$NON-NLS-1$
		IEvaluationContext appContext = staticContext;
		if (appContext == null) {
			appContext = new ExpressionContext(context);
		}
		ExecutionEvent event = new ExecutionEvent(command, parms, trigger, appContext);
		if (!command.isDefined()) {
			final NotDefinedException exception = new NotDefinedException(
					"Trying to execute a command that is not defined. " //$NON-NLS-1$
							+ command.getId());
			manager.fireNotDefined(command.getId(), exception);
			throw exception;
		}
		if (handler != null && handler.isHandled()) {
			try {
				final Object returnValue = handler.execute(event);
				manager.firePostExecuteSuccess(command.getId(), returnValue);
				return returnValue;
			} catch (final ExecutionException e) {
				manager.firePostExecuteFailure(command.getId(), e);
				throw e;
			}
		}
		final NotHandledException e = new NotHandledException(
				"There is no handler to execute for command " + command.getId()); //$NON-NLS-1$
		fireNotHandled(manager, e);
		throw e;
	}

	private Method fireNotHandled = null;

	private void fireNotHandled(CommandManager manager, final NotHandledException e) {
		if (fireNotHandled == null) {
			try {
				fireNotHandled = CommandManager.class.getMethod("fireNotHandled", String.class, //$NON-NLS-1$
						NotHandledException.class);
				fireNotHandled.setAccessible(true);
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try {
			fireNotHandled.invoke(manager, command.getId(), e);
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public IHandler getHandler() {
		return handler;
	}

	public void handlerChanged(HandlerEvent handlerEvent) {
		IHandler handler = command.getHandler();
		if (handler instanceof MakeHandlersGo) {
			IEclipseContext appContext = ((Workbench) PlatformUI.getWorkbench()).getApplication()
					.getContext();
			if (HandlerServiceImpl.lookUpHandler(appContext, command.getId()) == this) {
				((MakeHandlersGo) handler).fireHandlerChanged(handlerEvent);
			}
		}
	}
}
