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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.ui.contexts.IContextActivationService;
import org.eclipse.ui.contexts.IContextActivationServiceEvent;
import org.eclipse.ui.contexts.IContextActivationServiceListener;
import org.eclipse.ui.internal.util.Util;

public final class ContextActivationService implements IContextActivationService {

	private SortedSet activeContextIds;
	private IContextActivationServiceEvent contextActivationServiceEvent;
	private List contextActivationServiceListeners;

	public ContextActivationService() {
	}

	public void activateContext(String contextId) {
		if (contextId == null)
			throw new NullPointerException();

		if (activeContextIds == null)
			activeContextIds = new TreeSet();
			
		if (activeContextIds.add(contextId))
			fireContextActivationServiceChanged();
	}

	public void addContextActivationServiceListener(IContextActivationServiceListener contextActivationServiceListener) {
		if (contextActivationServiceListener == null)
			throw new NullPointerException();
			
		if (contextActivationServiceListeners == null)
			contextActivationServiceListeners = new ArrayList();
		
		if (!contextActivationServiceListeners.contains(contextActivationServiceListener))
			contextActivationServiceListeners.add(contextActivationServiceListener);
	}

	public void deactivateContext(String contextId) {
		if (contextId == null)
			throw new NullPointerException();

		if (activeContextIds != null && activeContextIds.remove(contextId)) {			
			if (activeContextIds.isEmpty())
				activeContextIds = null;

			fireContextActivationServiceChanged();
		}			
	}

	public SortedSet getActiveContextIds() {
		return activeContextIds != null ? Collections.unmodifiableSortedSet(activeContextIds) : Util.EMPTY_SORTED_SET;
	}
	
	public void removeContextActivationServiceListener(IContextActivationServiceListener contextActivationServiceListener) {
		if (contextActivationServiceListener == null)
			throw new NullPointerException();
			
		if (contextActivationServiceListeners != null) {
			contextActivationServiceListeners.remove(contextActivationServiceListener);
			
			if (contextActivationServiceListeners.isEmpty())
				contextActivationServiceListeners = null;
		}
	}
	
	private void fireContextActivationServiceChanged() {
		if (contextActivationServiceListeners != null) {
			// TODO copying to avoid ConcurrentModificationException
			Iterator iterator = new ArrayList(contextActivationServiceListeners).iterator();					
			
			if (iterator.hasNext()) {
				if (contextActivationServiceEvent == null)
					contextActivationServiceEvent = new ContextActivationServiceEvent(this);
				
				while (iterator.hasNext())	
					((IContextActivationServiceListener) iterator.next()).contextActivationServiceChanged(contextActivationServiceEvent);
			}							
		}			
	}	
}
