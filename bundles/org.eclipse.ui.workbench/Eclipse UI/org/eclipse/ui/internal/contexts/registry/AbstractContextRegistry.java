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

package org.eclipse.ui.internal.contexts.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class AbstractContextRegistry implements IContextRegistry {

	private IContextRegistryEvent contextRegistryEvent;
	private List contextRegistryListeners;
	
	protected List contextDefinitions = Collections.EMPTY_LIST;
	
	protected AbstractContextRegistry() {
	}

	public void addContextRegistryListener(IContextRegistryListener contextRegistryListener) {
		if (contextRegistryListener == null)
			throw new NullPointerException();
			
		if (contextRegistryListeners == null)
			contextRegistryListeners = new ArrayList();
		
		if (!contextRegistryListeners.contains(contextRegistryListener))
			contextRegistryListeners.add(contextRegistryListener);
	}

	public List getContextDefinitions() {
		return contextDefinitions;
	}
	
	public void removeContextRegistryListener(IContextRegistryListener contextRegistryListener) {
		if (contextRegistryListener == null)
			throw new NullPointerException();
			
		if (contextRegistryListeners != null) {
			contextRegistryListeners.remove(contextRegistryListener);
			
			if (contextRegistryListeners.isEmpty())
				contextRegistryListeners = null;
		}
	}

	protected void fireContextRegistryChanged() {
		if (contextRegistryListeners != null) {
			for (int i = 0; i < contextRegistryListeners.size(); i++) {
				if (contextRegistryEvent == null)
					contextRegistryEvent = new ContextRegistryEvent(this);
							
				((IContextRegistryListener) contextRegistryListeners.get(i)).contextRegistryChanged(contextRegistryEvent);
			}				
		}	
	}
}	
