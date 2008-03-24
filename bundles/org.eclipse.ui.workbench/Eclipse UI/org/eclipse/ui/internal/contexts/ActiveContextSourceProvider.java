/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.contexts;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.commands.contexts.ContextManagerEvent;
import org.eclipse.core.commands.contexts.IContextManagerListener;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.services.IServiceLocator;

/**
 * <p>
 * This listens to changes to the list of active contexts, and propagates them
 * through the <code>ISourceProvider</code> framework (a common language in
 * which events are communicated to services).
 * </p>
 * 
 * @since 3.2
 */
public final class ActiveContextSourceProvider extends AbstractSourceProvider
		implements IContextManagerListener {

	/**
	 * The names of the sources supported by this source provider.
	 */
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] { ISources.ACTIVE_CONTEXT_NAME };

	/**
	 * The context service with which this source provider should communicate.
	 * This value is never <code>null</code>.
	 */
	private IContextService contextService;

	public final void contextManagerChanged(final ContextManagerEvent event) {
		if (event.isActiveContextsChanged()) {
			final Map currentState = getCurrentState();

			if (DEBUG) {
				logDebuggingInfo("Contexts changed to " //$NON-NLS-1$
						+ currentState.get(ISources.ACTIVE_CONTEXT_NAME));
			}

			fireSourceChanged(ISources.ACTIVE_CONTEXT, currentState);
		}
	}

	public final void dispose() {
		contextService.removeContextManagerListener(this);
	}

	public final Map getCurrentState() {
		final Map currentState = new TreeMap();
		final Collection activeContextIds = contextService
				.getActiveContextIds();
		currentState.put(ISources.ACTIVE_CONTEXT_NAME, activeContextIds);
		return currentState;
	}

	public final String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.AbstractSourceProvider#initialize(org.eclipse.ui.services.IServiceLocator)
	 */
	public void initialize(IServiceLocator locator) {
		contextService = (IContextService) locator
				.getService(IContextService.class);
		contextService.addContextManagerListener(this);
	}
}
