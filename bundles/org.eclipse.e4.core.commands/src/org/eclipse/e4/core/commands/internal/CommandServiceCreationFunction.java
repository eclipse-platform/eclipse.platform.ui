/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.commands.internal;

import org.eclipse.core.commands.CommandManager;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 *
 */
public class CommandServiceCreationFunction extends ContextFunction {
	private CommandManager manager = null;
	private CommandServiceImpl service = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.core.services.context.spi.ContextFunction#compute(org.eclipse.e4.core.services
	 * .context.IEclipseContext, java.lang.Object[])
	 */
	@Override
	public Object compute(IEclipseContext context, Object[] arguments) {
		if (service == null) {
			IEclipseContext root = getRootContext(context);
			manager = new CommandManager();
			root.set(CommandManager.class.getName(), manager);
			service = (CommandServiceImpl) ContextInjectionFactory.make(CommandServiceImpl.class,
					root);
		}
		return service;
	}

	/**
	 * @param context
	 * @return the topmost "root" context
	 */
	private IEclipseContext getRootContext(IEclipseContext context) {
		IEclipseContext current = (IEclipseContext) context.get(IContextConstants.ROOT_CONTEXT);
		if (current != null) {
			return current;
		}
		current = context;
		IEclipseContext parent = (IEclipseContext) current.getLocal(IContextConstants.PARENT);
		while (parent != null) {
			current = parent;
			parent = (IEclipseContext) current.getLocal(IContextConstants.PARENT);
		}
		if (current != null) {
			current.set(IContextConstants.ROOT_CONTEXT, current);
		}
		return current;
	}

}
