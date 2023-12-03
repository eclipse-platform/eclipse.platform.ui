/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
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

import jakarta.inject.Inject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.ParameterValueConversionException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;

public class HandlerServiceImpl implements EHandlerService {
	/**
	 * The static context key under which a command 'trigger' from legacy code is stored during
	 * calls to {@link #executeHandler(ParameterizedCommand, IEclipseContext)}
	 *
	 * @see IEclipseContext
	 * @see HandlerServiceImpl#executeHandler(ParameterizedCommand, IEclipseContext)
	 */
	private static final String SWT_TRIGGER = "org.eclipse.swt.widgets.Event"; //$NON-NLS-1$
	static final String TMP_STATIC_CONTEXT = "tmp-staticContext"; //$NON-NLS-1$
	public static final String H_ID = "handler::"; //$NON-NLS-1$
	public static final String PARM_MAP = "parmMap::"; //$NON-NLS-1$
	public static final String CAN_EXECUTE = "HandlerServiceImpl.canExecute"; //$NON-NLS-1$
	public static final String NOT_HANDLED = "HandlerServiceImpl.notHandled"; //$NON-NLS-1$
	public static final String STATIC_CONTEXT = "HandlerServiceImpl.staticContext"; //$NON-NLS-1$
	public static final String HANDLER_EXCEPTION = "HandlerServiceImpl.exception"; //$NON-NLS-1$

	private static LinkedList<ExecutionContexts> contextStack = new LinkedList<>();

	public static ContextFunction handlerGenerator = null;

	public static IHandler getHandler(String commandId) {
		if (handlerGenerator != null) {
			return (IHandler) handlerGenerator.compute(null, commandId);
		}
		return new HandlerServiceHandler(commandId);
	}

	static class ExecutionContexts {
		public IEclipseContext context;
		public IEclipseContext staticContext;

		public ExecutionContexts(IEclipseContext ctx, IEclipseContext staticCtx) {
			context = ctx;
			staticContext = staticCtx;
		}
	}

	static LinkedList<ExecutionContexts> getContextStack() {
		return contextStack;
	}

	public static void push(IEclipseContext ctx, IEclipseContext staticCtx) {
		getContextStack().addFirst(new ExecutionContexts(ctx, staticCtx));
	}

	public static ExecutionContexts pop() {
		return getContextStack().poll();
	}

	static ExecutionContexts peek() {
		return getContextStack().peek();
	}

	/**
	 * @param context
	 *            the context to start the lookup process
	 * @return a handler, or <code>null</code>
	 */
	public static Object lookUpHandler(IEclipseContext context, String commandId) {
		return context.getActiveLeaf().get(H_ID + commandId);
	}

	/**
	 * Fill in a temporary static context for execution.
	 */
	@SuppressWarnings("rawtypes")
	private void addParms(ParameterizedCommand command, IEclipseContext staticContext) {
		final Map parms = command.getParameterMap();
		Iterator i = parms.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			String parameterId = (String) entry.getKey();
			staticContext.set(parameterId,
					convertParameterValue(command.getCommand(), parameterId,
							(String) entry.getValue()));
		}
		staticContext.set(PARM_MAP, parms);
		staticContext.set(ParameterizedCommand.class, command);
	}

	/**
	 * Convert the parameter's value according to it's type.
	 *
	 * @return converted value
	 * @see org.eclipse.e4.ui.model.application.commands.MCommandParameter#getTypeId()
	 */
	private Object convertParameterValue(Command command, String parameterId, String value) {
		try {
			ParameterType parameterType = command.getParameterType(parameterId);

			if (parameterType != null) {
				AbstractParameterValueConverter valueConverter = parameterType.getValueConverter();
				if (valueConverter != null) {
					return valueConverter.convertToObject(value);
				}
			}
		} catch (NotDefinedException | ParameterValueConversionException e) {
		}
		return value;
	}

	private IEclipseContext context;

	@Inject
	@Optional
	Logger logger;

	@Override
	public void activateHandler(String commandId, Object handler) {
		String handlerId = H_ID + commandId;
		context.set(handlerId, handler);
	}

	@Override
	public boolean canExecute(ParameterizedCommand command) {
		final IEclipseContext staticContext = EclipseContextFactory.create(TMP_STATIC_CONTEXT);
		try {
			return canExecute(command, staticContext);
		} finally {
			staticContext.dispose();
		}
	}

	@Override
	public boolean canExecute(ParameterizedCommand command, IEclipseContext staticContext) {
		final IEclipseContext executionContext = getExecutionContext();
		addParms(command, staticContext);
		push(executionContext, staticContext);
		try {
			Command cmd = command.getCommand();
			cmd.setEnabled(new ExpressionContext(peek().context));
			return cmd.isEnabled();
		} finally {
			pop();
		}
	}

	@Override
	public void deactivateHandler(String commandId, Object handler) {
		context.remove(H_ID + commandId);
	}

	@Override
	public Object executeHandler(ParameterizedCommand command) {
		final IEclipseContext staticContext = EclipseContextFactory.create(TMP_STATIC_CONTEXT);
		try {
			return executeHandler(command, staticContext);
		} finally {
			Object obj = staticContext.get(HandlerServiceImpl.HANDLER_EXCEPTION);
			if (obj instanceof ExecutionException) {
				if (logger != null) {
					logger.error((Throwable) obj, "Command '" + command.getId() + "' failed"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			staticContext.dispose();
		}
	}

	@Override
	public Object executeHandler(ParameterizedCommand command, IEclipseContext staticContext) {
		final IEclipseContext executionContext = getExecutionContext();
		addParms(command, staticContext);
		push(executionContext, staticContext);
		try {
			return command.executeWithChecks(staticContext.get(SWT_TRIGGER), new ExpressionContext(
					peek().context));
		} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
			staticContext.set(HANDLER_EXCEPTION, e);
		} finally {
			pop();
		}
		return null;
	}

	@Inject
	public void setContext(IEclipseContext c) {
		context = c;
	}

	public IEclipseContext getContext() {
		return context;
	}

	public IEclipseContext getExecutionContext() {
		return context.getActiveLeaf();
	}
}
