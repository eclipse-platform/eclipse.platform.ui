/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.contexts;

import java.util.SortedSet;

import org.eclipse.ui.contexts.ContextManagerEvent;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IContextManagerListener;

public final class ProxyContextManager extends AbstractContextManager {
	private IContextManager contextManager;

	public ProxyContextManager(IContextManager contextManager) {
		if (contextManager == null)
			throw new NullPointerException();

		this.contextManager = contextManager;

		this
			.contextManager
			.addContextManagerListener(new IContextManagerListener() {
			public void contextManagerChanged(ContextManagerEvent contextManagerEvent) {
				ContextManagerEvent proxyContextManagerEvent =
					new ContextManagerEvent(
						ProxyContextManager.this,
						contextManagerEvent.haveDefinedContextIdsChanged(),
						contextManagerEvent.haveEnabledContextIdsChanged(),
						contextManagerEvent.getPreviouslyDefinedContextIds(),
						contextManagerEvent.getPreviouslyEnabledContextIds());
				fireContextManagerChanged(proxyContextManagerEvent);
			}
		});
	}

	public IContext getContext(String contextId) {
		return contextManager.getContext(contextId);
	}

	public SortedSet getDefinedContextIds() {
		return contextManager.getDefinedContextIds();
	}

	public SortedSet getEnabledContextIds() {
		return contextManager.getEnabledContextIds();
	}
}
