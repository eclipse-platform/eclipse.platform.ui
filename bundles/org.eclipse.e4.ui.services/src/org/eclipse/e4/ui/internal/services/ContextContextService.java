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
	
	private boolean deferUpdates = false;

	private int cachingRef = 0;

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
		if (deferUpdates) {
			deferActivateContext(id);
			return;
		}
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
	
	/**
	 * Informs the manager that a batch operation has started.
	 * <p>
	 * <b>Note:</b> You must insure that if you call
	 * <code>deferUpdates(true)</code> that nothing in your batched operation
	 * will prevent the matching call to <code>deferUpdates(false)</code>.
	 * </p>
	 * 
	 * @param defer
	 *            true when starting a batch operation false when ending the
	 *            operation
	 * 
	 * @since 4.3
	 */
	public void deferUpdates(boolean defer) {
		if(defer) {
			cachingRef ++;
			if (cachingRef==1) {
				setEventCaching(true);
			}
		}else {
			cachingRef--;
			if (cachingRef==0) {
				setEventCaching(false);
			}
		}
	}

	private void deferActivateContext(String id) {
		LinkedList<String> locals = (LinkedList<String>) eclipseContext
				.getLocal(LOCAL_CONTEXTS+".a");
		if (locals == null) {
			locals = new LinkedList<String>();
			eclipseContext.set(LOCAL_CONTEXTS+ ".a", locals);
		}
		locals.add(id);
	}

	private void setEventCaching(boolean cache) {
		if (cache) {
			deferUpdates = true;
			return;
		}

		deferUpdates = false;
		LinkedList<String> locals = (LinkedList<String>) eclipseContext
				.getLocal(LOCAL_CONTEXTS);
		if (locals == null) {
			locals = new LinkedList<String>();
		}
		LinkedList<String> activates = (LinkedList<String>) eclipseContext
				.getLocal(LOCAL_CONTEXTS + ".a");
		if (activates != null) {
			eclipseContext.remove(LOCAL_CONTEXTS + ".a");
			for (String id : activates) {
				locals.add(id);
			}
		}
		LinkedList<String> deactivates = (LinkedList<String>) eclipseContext
				.getLocal(LOCAL_CONTEXTS + ".d");
		if (deactivates != null) {
			eclipseContext.remove(LOCAL_CONTEXTS + ".d");
			for (String id : deactivates) {
				locals.remove(id);
			}
		}
		eclipseContext.set(LOCAL_CONTEXTS, locals.clone());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.services.EContextService#deactivateContext(java.lang
	 * .String)
	 */
	public void deactivateContext(String id) {
		if (deferUpdates) {
			deferDeactivateContext(id);
			return;
		}
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

	private void deferDeactivateContext(String id) {
		LinkedList<String> locals = (LinkedList<String>) eclipseContext
				.getLocal(LOCAL_CONTEXTS+".d");
		if (locals == null) {
			locals = new LinkedList<String>();
			eclipseContext.set(LOCAL_CONTEXTS+ ".d", locals);
		}
		locals.add(id);
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
