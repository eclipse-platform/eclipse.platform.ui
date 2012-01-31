/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.internal.services;

import org.eclipse.e4.core.contexts.IEclipseContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.ui.services.IServiceConstants;

public class ContextContextService implements EContextService {
	static final String LOCAL_CONTEXTS = "localContexts";

	private IEclipseContext eclipseContext;
	private ContextManager contextManager;

	public ContextContextService(IEclipseContext context) {
		eclipseContext = context;
		contextManager = (ContextManager) context.get(ContextManager.class
				.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.services.EContextService#activateContext(java.lang.
	 * String)
	 */
	public void activateContext(String id) {
		Set<String> locals = (Set<String>) eclipseContext
				.getLocal(LOCAL_CONTEXTS);
		if (locals == null) {
			locals = new HashSet<String>();
			locals.add(id);
			eclipseContext.set(LOCAL_CONTEXTS, locals);
		} else if (locals.add(id)) {
			// copy the set so the change is propagated
			eclipseContext.set(LOCAL_CONTEXTS, new HashSet<String>(locals));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.services.EContextService#deactivateContext(java.lang
	 * .String)
	 */
	public void deactivateContext(String id) {
		Set<String> locals = (Set<String>) eclipseContext
				.getLocal(LOCAL_CONTEXTS);
		if (locals != null && locals.remove(id)) {
			// copy the set so the change is propagated
			locals = new HashSet<String>(locals);
			eclipseContext.set(LOCAL_CONTEXTS, locals);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.services.EContextService#getActiveContextIds()
	 */
	public Collection<String> getActiveContextIds() {
		Set<String> set = (Set<String>) eclipseContext
				.get(IServiceConstants.ACTIVE_CONTEXTS);
		if (set != null) {
			contextManager.setActiveContextIds(set);
		}
		return set;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.services.EContextService#getContext(java.lang.String)
	 */
	public Context getContext(String id) {
		Context ctx = contextManager.getContext(id);
		return ctx;
	}

}
