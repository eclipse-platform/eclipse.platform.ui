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

package org.eclipse.ui.contexts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * TODO javadoc
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public abstract class AbstractContextService implements IContextService {
	
	private ContextServiceEvent contextServiceEvent;
	private List contextServiceListeners;

	/**
	 * TODO javadoc
	 */		
	public AbstractContextService() {
		super();
	}

	/**
	 * Registers an IContextServiceListener instance with this context service.
	 *
	 * @param contextServiceListener the IContextServiceListener instance to register.
	 */	
	public void addContextServiceListener(IContextServiceListener contextServiceListener) {
		if (contextServiceListeners == null)
			contextServiceListeners = new ArrayList();
		
		if (!contextServiceListeners.contains(contextServiceListener))
			contextServiceListeners.add(contextServiceListener);
	}

	/**
	 * Unregisters an IContextServiceListener instance with this context service.
	 *
	 * @param contextServiceListener the IContextServiceListener instance to unregister.
	 */
	public void removeContextServiceListener(IContextServiceListener contextServiceListener) {
		if (contextServiceListeners != null) {
			contextServiceListeners.remove(contextServiceListener);
			
			if (contextServiceListeners.isEmpty())
				contextServiceListeners = null;
		}
	}

	/**
	 * TODO javadoc
	 */
	protected void fireContextServiceChanged() {
		if (contextServiceListeners != null) {
			Iterator iterator = contextServiceListeners.iterator();
			
			if (iterator.hasNext()) {
				if (contextServiceEvent == null)
					contextServiceEvent = new ContextServiceEvent(this);
				
				while (iterator.hasNext())	
					((IContextServiceListener) iterator.next()).contextServiceChanged(contextServiceEvent);
			}							
		}			
	}
}
