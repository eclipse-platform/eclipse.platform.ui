/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.contexts;

import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IContextManagerEvent;

final class ContextManagerEvent implements IContextManagerEvent {

	private IContextManager contextManager;

	ContextManagerEvent(IContextManager contextManager) {
		if (contextManager == null)
			throw new NullPointerException();
		
		this.contextManager = contextManager;
	}

	public IContextManager getContextManager() {
		return contextManager;
	}
}
