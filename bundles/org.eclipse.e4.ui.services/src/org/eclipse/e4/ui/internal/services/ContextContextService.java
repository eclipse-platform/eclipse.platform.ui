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

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.contexts.IEclipseContext;
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
		LinkedList<String> locals = (LinkedList<String>) eclipseContext
				.getLocal(LOCAL_CONTEXTS);
		if (locals == null) {
			locals = new LinkedList<String>();
			locals.add(id);
			eclipseContext.set(LOCAL_CONTEXTS, locals);
		} else {
			boolean contained = locals.contains(id);
			if (locals.add(id) && !contained) {
				// copy the set so the change is propagated
				eclipseContext.set(LOCAL_CONTEXTS, locals.clone());
			}
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
		LinkedList<String> locals = (LinkedList<String>) eclipseContext
				.getLocal(LOCAL_CONTEXTS);
		if (locals != null && locals.remove(id)) {
			boolean contained = locals.contains(id);
			if (!contained) {
				// copy the set so the change is propagated
				eclipseContext.set(LOCAL_CONTEXTS, locals.clone());
			}
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
