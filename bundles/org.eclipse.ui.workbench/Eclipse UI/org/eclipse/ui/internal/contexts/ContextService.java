/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.ui.contexts.IContextService;

/**
 * <p>
 * Provides services related to contexts in the Eclipse workbench. This provides
 * access to contexts.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 */
public final class ContextService implements IContextService {

	/**
	 * The context manager that supports this service. This value is never
	 * <code>null</code>.
	 */
	private final ContextManager contextManager;

	/**
	 * Constructs a new instance of <code>ContextService</code> using a
	 * context manager.
	 * 
	 * @param contextManager
	 *            The context manager to use; must not be <code>null</code>.
	 */
	public ContextService(final ContextManager contextManager) {
		if (contextManager == null) {
			throw new NullPointerException(
					"Cannot create a context service with a null manager"); //$NON-NLS-1$
		}
		this.contextManager = contextManager;
	}

	public final Context getContext(final String contextId) {
		/*
		 * TODO Need to put in place protection against the context being
		 * changed.
		 */
		return contextManager.getContext(contextId);
	}

	public final Collection getDefinedContextIds() {
		return contextManager.getDefinedContextIds();
	}
}
