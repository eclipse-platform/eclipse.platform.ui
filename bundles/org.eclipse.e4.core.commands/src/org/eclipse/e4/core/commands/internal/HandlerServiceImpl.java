/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.commands.internal;

import java.util.Iterator;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;

/**
 *
 */
public class HandlerServiceImpl implements EHandlerService {
	private static final String TMP_STATIC_CONTEXT = "tmp-staticContext"; //$NON-NLS-1$
	public final static String H_ID = "handler::"; //$NON-NLS-1$
	public final static String PARM_MAP = "parmMap::"; //$NON-NLS-1$
	public final static String CAN_EXECUTE = "HandlerServiceImpl.canExecute"; //$NON-NLS-1$
	public final static String NOT_HANDLED = "HandlerServiceImpl.notHandled"; //$NON-NLS-1$

	/**
	 * @param context
	 *            the context to start the lookup process
	 * @param commandId
	 * @return a handler, or <code>null</code>
	 */
	public static Object lookUpHandler(IEclipseContext context, String commandId) {
		return context.getActiveLeaf().get(H_ID + commandId);
	}

	private IEclipseContext context;

	@Inject
	@Optional
	private Logger logger;

	public static Object preExecute = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.core.commands.EHandlerService#activateHandler(java.lang.String,
	 * java.lang.Object)
	 */
	public void activateHandler(String commandId, Object handler) {
		String handlerId = H_ID + commandId;
		context.set(handlerId, handler);
	}

	/**
	 * Fill in a temporary static context for execution.
	 * 
	 * @param command
	 * @return a context not part of the normal hierarchy
	 */
	private void addParms(ParameterizedCommand command, IEclipseContext staticContext) {
		final Map parms = command.getParameterMap();
		Iterator i = parms.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			staticContext.set((String) entry.getKey(), entry.getValue());
		}
		staticContext.set(PARM_MAP, parms);
		staticContext.set(ParameterizedCommand.class, command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.core.commands.EHandlerService#canExecute(org.eclipse.core.commands.
	 * ParameterizedCommand)
	 */
	public boolean canExecute(ParameterizedCommand command) {
		final IEclipseContext staticContext = EclipseContextFactory.create(TMP_STATIC_CONTEXT);
		try {
			return canExecute(command, staticContext);
		} finally {
			staticContext.dispose();
		}
	}

	public boolean canExecute(ParameterizedCommand command, IEclipseContext staticContext) {
		String commandId = command.getId();
		Object handler = lookUpHandler(context, commandId);
		if (handler == null) {
			return false;
		}

		final IEclipseContext executionContext = getExecutionContext();
		addParms(command, staticContext);

		try {
			Boolean result = ((Boolean) ContextInjectionFactory.invoke(handler, CanExecute.class,
					executionContext, staticContext, Boolean.TRUE));
			staticContext.set(CAN_EXECUTE, result);
			return result.booleanValue();
		} catch (Exception e) {
			if (Command.DEBUG_HANDLERS && logger != null) {
				StringBuilder message = new StringBuilder();
				message.append("Handler (").append(handler).append(") "); //$NON-NLS-1$ //$NON-NLS-2$
				message.append(" for command (").append(commandId).append(") "); //$NON-NLS-1$ //$NON-NLS-2$
				message.append("threw unexpected exception"); //$NON-NLS-1$
				logger.trace(e, message.toString());
			}
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.core.commands.EHandlerService#deactivateHandler(java.lang.String,
	 * java.lang.Object)
	 */
	public void deactivateHandler(String commandId, Object handler) {
		context.remove(H_ID + commandId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.core.commands.EHandlerService#executeHandler(org.eclipse.core.commands.
	 * ParameterizedCommand)
	 */
	public Object executeHandler(ParameterizedCommand command) {
		final IEclipseContext staticContext = EclipseContextFactory.create(TMP_STATIC_CONTEXT);
		try {
			return executeHandler(command, staticContext);
		} finally {
			staticContext.dispose();
		}
	}

	public Object executeHandler(ParameterizedCommand command, IEclipseContext staticContext) {
		String commandId = command.getId();
		final IEclipseContext executionContext = getExecutionContext();
		addParms(command, staticContext);
		if (preExecute != null) {
			ContextInjectionFactory.invoke(preExecute, Execute.class, executionContext,
					staticContext, null);
		}
		Object handler = lookUpHandler(context, commandId);
		if (handler == null) {
			staticContext.set(NOT_HANDLED, Boolean.TRUE);
			return null;
		}
		staticContext.remove(NOT_HANDLED);

		Object rc = ContextInjectionFactory.invoke(handler, CanExecute.class, executionContext,
				staticContext, Boolean.TRUE);
		staticContext.set(CAN_EXECUTE, rc);
		if (Boolean.FALSE.equals(rc))
			return null;
		return ContextInjectionFactory.invoke(handler, Execute.class, executionContext,
				staticContext, null);
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
