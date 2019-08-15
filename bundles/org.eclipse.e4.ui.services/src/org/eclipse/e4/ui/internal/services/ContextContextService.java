/*******************************************************************************
 * Copyright (c) 2009, 2019 IBM Corporation and others.
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
	static final String DEFERRED_UPDATES = "localContexts.updates";

	private IEclipseContext eclipseContext;
	private ContextManager contextManager;

	private boolean deferUpdates;

	private int cachingRef;

	public ContextContextService(IEclipseContext context) {
		eclipseContext = context;
		contextManager = context.get(ContextManager.class);
	}

	@Override
	public void activateContext(String id) {
		if (deferUpdates) {
			deferActivateContext(id);
			return;
		}
		@SuppressWarnings("unchecked")
		LinkedList<String> locals = (LinkedList<String>) eclipseContext.getLocal(LOCAL_CONTEXTS);
		if (locals == null) {
			locals = new LinkedList<>();
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
	 * @since 4.2.2
	 */
	@Override
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
		@SuppressWarnings("unchecked")
		LinkedList<String> locals = (LinkedList<String>) eclipseContext.getLocal(DEFERRED_UPDATES);
		if (locals == null) {
			locals = new LinkedList<>();
			eclipseContext.set(DEFERRED_UPDATES, locals);
		}
		locals.add("+" + id);
	}

	private void setEventCaching(boolean cache) {
		if (cache) {
			deferUpdates = true;
			return;
		}

		deferUpdates = false;
		@SuppressWarnings("unchecked")
		LinkedList<String> locals = (LinkedList<String>) eclipseContext.getLocal(LOCAL_CONTEXTS);
		if (locals == null) {
			locals = new LinkedList<>();
		}
		@SuppressWarnings("unchecked")
		LinkedList<String> updates = (LinkedList<String>) eclipseContext.getLocal(DEFERRED_UPDATES);
		if (updates != null) {
			for (String update : updates) {
				if (update.startsWith("+")) {
					locals.add(update.substring(1));
				} else if (update.startsWith("-")) {
					locals.remove(update.substring(1));
				}
			}
			eclipseContext.remove(DEFERRED_UPDATES);
		}
		eclipseContext.set(LOCAL_CONTEXTS, locals.clone());
	}

	@Override
	public void deactivateContext(String id) {
		if (deferUpdates) {
			deferDeactivateContext(id);
			return;
		}
		LinkedList<?> locals = (LinkedList<?>) eclipseContext.getLocal(LOCAL_CONTEXTS);
		if (locals != null && locals.remove(id)) {
			boolean contained = locals.contains(id);
			if (!contained) {
				// copy the set so the change is propagated
				eclipseContext.set(LOCAL_CONTEXTS, locals.clone());
			}
		}
	}

	private void deferDeactivateContext(String id) {
		@SuppressWarnings("unchecked")
		LinkedList<String> locals = (LinkedList<String>) eclipseContext.getLocal(DEFERRED_UPDATES);
		if (locals == null) {
			locals = new LinkedList<>();
			eclipseContext.set(DEFERRED_UPDATES, locals);
		}
		locals.add("-" + id);
	}

	@Override
	public Collection<String> getActiveContextIds() {
		@SuppressWarnings("unchecked")
		Set<String> set = (Set<String>) eclipseContext.get(IServiceConstants.ACTIVE_CONTEXTS);
		if (set != null) {
			contextManager.setActiveContextIds(set);
		}
		return set;
	}

	@Override
	public Context getContext(String id) {
		return contextManager.getContext(id);
	}

}
