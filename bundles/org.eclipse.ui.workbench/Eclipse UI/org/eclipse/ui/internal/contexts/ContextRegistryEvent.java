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

import org.eclipse.ui.contexts.registry.IContextRegistry;
import org.eclipse.ui.contexts.registry.IContextRegistryEvent;
import org.eclipse.ui.internal.util.Util;

final class ContextRegistryEvent implements IContextRegistryEvent {

	private IContextRegistry contextRegistry;

	ContextRegistryEvent(IContextRegistry contextRegistry) {
		super();
		
		if (contextRegistry == null)
			throw new NullPointerException();
		
		this.contextRegistry = contextRegistry;
	}

	public boolean equals(Object object) {
		if (!(object instanceof ContextRegistryEvent))
			return false;

		ContextRegistryEvent contextRegistryEvent = (ContextRegistryEvent) object;	
		return Util.equals(contextRegistry, contextRegistryEvent.contextRegistry);
	}
	
	public IContextRegistry getContextRegistry() {
		return contextRegistry;
	}
}
