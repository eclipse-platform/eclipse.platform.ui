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
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IContextManagerEvent;
import org.eclipse.ui.contexts.IContextManagerListener;
import org.eclipse.ui.internal.util.Util;

public final class ContextManager implements IContextManager {

	private SortedSet activeContextIds = new TreeSet();
	private SortedMap contextElementsById = new TreeMap();
	private IContextManagerEvent contextManagerEvent;
	private List contextManagerListeners;
	private SortedMap contextsById = new TreeMap();
	private SortedSet definedContextIds = new TreeSet();
	private RegistryReader registryReader;

	public ContextManager() {
		super();
		updateDefinedContextIds();
	}

	public void addContextManagerListener(IContextManagerListener contextManagerListener)
		throws IllegalArgumentException {
		if (contextManagerListener == null)
			throw new IllegalArgumentException();
			
		if (contextManagerListeners == null)
			contextManagerListeners = new ArrayList();
		
		if (!contextManagerListeners.contains(contextManagerListener))
			contextManagerListeners.add(contextManagerListener);
	}

	public SortedSet getActiveContextIds() {
		return Collections.unmodifiableSortedSet(activeContextIds);
	}

	public IContext getContext(String contextId)
		throws IllegalArgumentException {
		if (contextId == null)
			throw new IllegalArgumentException();
			
		IContext context = (IContext) contextsById.get(contextId);
		
		if (context == null) {
			context = new Context(this, contextId);
			contextsById.put(contextId, context);
		}
		
		return context;
	}

	public SortedSet getDefinedContextIds() {
		return Collections.unmodifiableSortedSet(definedContextIds);
	}

	public void removeContextManagerListener(IContextManagerListener contextManagerListener)
		throws IllegalArgumentException {
		if (contextManagerListener == null)
			throw new IllegalArgumentException();
			
		if (contextManagerListeners != null) {
			contextManagerListeners.remove(contextManagerListener);
			
			if (contextManagerListeners.isEmpty())
				contextManagerListeners = null;
		}
	}

	public void setActiveContextIds(SortedSet activeContextIds)
		throws IllegalArgumentException {
		activeContextIds = Util.safeCopy(activeContextIds, String.class);
		SortedSet activatingContextIds = new TreeSet();
		SortedSet deactivatingContextIds = new TreeSet();
		Iterator iterator = activeContextIds.iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
		
			if (!this.activeContextIds.contains(id))
				activatingContextIds.add(id);
		}

		iterator = this.activeContextIds.iterator();
		
		while (iterator.hasNext()) {
			String id = (String) iterator.next();
		
			if (!activeContextIds.contains(id))
				deactivatingContextIds.add(id);					
		}		

		SortedSet contextChanges = new TreeSet();
		contextChanges.addAll(activatingContextIds);		
		contextChanges.addAll(deactivatingContextIds);			

		if (!contextChanges.isEmpty()) {
			this.activeContextIds = activeContextIds;	
			fireContextManagerChanged();

			iterator = contextChanges.iterator();
		
			while (iterator.hasNext())
				fireContextChanged((String) iterator.next());
		}
	}

	public void updateDefinedContextIds() {
		if (registryReader == null)
			registryReader = new RegistryReader(Platform.getPluginRegistry());
		
		registryReader.load();
		SortedMap contextElementsById = ContextElement.sortedMapById(registryReader.getContextElements());		
		SortedSet contextElementAdditions = new TreeSet();		
		SortedSet contextElementChanges = new TreeSet();
		SortedSet contextElementRemovals = new TreeSet();		
		Iterator iterator = contextElementsById.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String id = (String) entry.getKey();
			ContextElement contextElement = (ContextElement) entry.getValue();
			
			if (!this.contextElementsById.containsKey(id))
				contextElementAdditions.add(id);
			else if (!Util.equals(contextElement, this.contextElementsById.get(id)))
				contextElementChanges.add(id);								
		}

		iterator = this.contextElementsById.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String id = (String) entry.getKey();
			ContextElement contextElement = (ContextElement) entry.getValue();
			
			if (!contextElementsById.containsKey(id))
				contextElementRemovals.add(id);						
		}

		SortedSet contextChanges = new TreeSet();
		contextChanges.addAll(contextElementAdditions);		
		contextChanges.addAll(contextElementChanges);		
		contextChanges.addAll(contextElementRemovals);
		
		if (!contextChanges.isEmpty()) {
			this.contextElementsById = contextElementsById;		
			SortedSet definedContextIds = new TreeSet(contextElementsById.keySet());

			if (!Util.equals(definedContextIds, this.definedContextIds)) {	
				this.definedContextIds = definedContextIds;
				fireContextManagerChanged();
			}

			iterator = contextChanges.iterator();
		
			while (iterator.hasNext())
				fireContextChanged((String) iterator.next());
		}
	}

	ContextElement getContextElement(String contextId) {
		return (ContextElement) contextElementsById.get(contextId);
	}
	
	private void fireContextChanged(String contextId) {
		Context context = (Context) contextsById.get(contextId);
		
		if (context != null) 
			context.fireContextChanged();		
	}

	private void fireContextManagerChanged() {
		if (contextManagerListeners != null) {
			Iterator iterator = contextManagerListeners.iterator();
			
			if (iterator.hasNext()) {
				if (contextManagerEvent == null)
					contextManagerEvent = new ContextManagerEvent(this);
				
				while (iterator.hasNext())	
					((IContextManagerListener) iterator.next()).contextManagerChanged(contextManagerEvent);
			}							
		}			
	}
}
