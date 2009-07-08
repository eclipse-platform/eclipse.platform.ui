/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.services;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.services.EHandlerService;

public class ContextHandlerService implements EHandlerService {
	private static final String PREFIX = "HDL_";
	private static final String PARM_MAP = "legacyParameterMap"; //$NON-NLS-1$


	private IEclipseContext context;

	public ContextHandlerService(IEclipseContext context) {
		this.context = context;
	}

	public void activateHandler(String commandId, Object handler) {
		context.set(PREFIX + commandId, handler);
	}

	public Object executeHandler(ParameterizedCommand command) {
		String commandId = command.getId();
		Object handler = context.get(commandId, new Object[] { "handler" });
		if (handler == null) {
			return null;
		}
		IContributionFactory factory = (IContributionFactory) context.get(
				"org.eclipse.e4.core.services.IContributionFactory",
				new Object[] { handler });
		final Map parms = command.getParameterMap();
		Iterator i = parms.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			context.set((String) entry.getKey(), entry.getValue());
		}
		context.set(PARM_MAP, parms);
		try {
			Object rc = factory.call(handler, null, "canExecute", context,
					Boolean.TRUE);
			if (Boolean.FALSE.equals(rc)) {
				return null;
			}
			return factory.call(handler, null, "execute", context, null);
		} finally {
			context.remove(PARM_MAP);
			i = parms.keySet().iterator();
			while (i.hasNext()) {
				context.remove((String) i.next());
			}
		}
	}

	public void deactivateHandler(String commandId, Object handler) {
		Object object = context.get(PREFIX + commandId);
		if (object == handler) {
			context.remove(PREFIX + commandId);
		}
	}

	public boolean canExecute(ParameterizedCommand command) {
		String commandId = command.getId();
		Object handler = context.get(commandId, new Object[] { "handler" });
		if (handler == null) {
			return false;
		}
		IContributionFactory factory = (IContributionFactory) context.get(
				"org.eclipse.e4.core.services.IContributionFactory",
				new Object[] { handler });
		final Map parms = command.getParameterMap();
		Iterator i = parms.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			context.set((String) entry.getKey(), entry.getValue());
		}
		context.set(PARM_MAP, parms);
		Object rc = null;
		try {
			rc = factory.call(handler, null, "canExecute", context,
					Boolean.TRUE);
		} finally {
			context.remove(PARM_MAP);
			i = parms.keySet().iterator();
			while (i.hasNext()) {
				context.remove((String) i.next());
			}
		}
		if (Boolean.FALSE.equals(rc)) {
			return false;
		}
		return true;
	}

	public IEclipseContext getContext() {
		return context;
	}

}
