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

import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.services.EHandlerService;

public class ContextHandlerService implements EHandlerService {
	private static final String PREFIX = "HDL_";

	private IEclipseContext context;

	public ContextHandlerService(IEclipseContext context) {
		this.context = context;
	}

	public void activateHandler(String commandId, Object handler) {
		context.set(PREFIX + commandId, handler);
	}

	public Object executeHandler(String commandId) {
		Object handler = context.get(commandId, new Object[] { "handler" });
		if (handler == null) {
			return null;
		}
		IContributionFactory factory = (IContributionFactory) context.get(
				"org.eclipse.e4.core.services.IContributionFactory",
				new Object[] { handler });
		Object rc = factory.call(handler, null, "canExecute", context,
				Boolean.TRUE);
		if (Boolean.FALSE.equals(rc)) {
			return null;
		}
		return factory.call(handler, null, "execute", context, null);
	}

	public void deactivateHandler(String commandId, Object handler) {
		Object object = context.get(PREFIX + commandId);
		if (object == handler) {
			context.remove(PREFIX + commandId);
		}
	}

	public boolean canExecute(String commandId) {
		Object handler = context.get(commandId, new Object[] { "handler" });
		if (handler == null) {
			return false;
		}
		IContributionFactory factory = (IContributionFactory) context.get(
				"org.eclipse.e4.core.services.IContributionFactory",
				new Object[] { handler });
		Object rc = factory.call(handler, null, "canExecute", context,
				Boolean.TRUE);
		if (Boolean.FALSE.equals(rc)) {
			return false;
		}
		return true;
	}

}
