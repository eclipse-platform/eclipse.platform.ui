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
import org.eclipse.ui.contexts.IContextHandle;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IContextManagerEvent;
import org.eclipse.ui.contexts.IContextManagerListener;
import org.eclipse.ui.internal.util.Util;

public final class ContextManager implements IContextManager {

	private SortedSet activeContextIds = new TreeSet();
	private IContextManagerEvent contextManagerEvent;
	private List contextManagerListeners;
	private SortedMap contextHandlesById = new TreeMap();
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

	public IContextHandle getContextHandle(String contextId)
		throws IllegalArgumentException {
		if (contextId == null)
			throw new IllegalArgumentException();
			
		IContextHandle contextHandle = (IContextHandle) contextHandlesById.get(contextId);
		
		if (contextHandle == null) {
			contextHandle = new ContextHandle(contextId);
			contextHandlesById.put(contextId, contextHandle);
		}
		
		return contextHandle;
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
		
		if (activeContextIds.equals(this.activeContextIds)) {
			this.activeContextIds = activeContextIds;	
			fireContextManagerChanged();
		}
	}

	public void updateDefinedContextIds() {
		if (registryReader == null)
			registryReader = new RegistryReader(Platform.getPluginRegistry());
		
		registryReader.load();
		List contextElements = registryReader.getContextElements();		
		List contexts = new ArrayList();
		Iterator iterator = contextElements.iterator();
		
		while (iterator.hasNext()) {
			ContextElement contextElement = (ContextElement) iterator.next();
			contexts.add(Context.create(contextElement.getDescription(), contextElement.getId(), contextElement.getName(), contextElement.getParentId(), contextElement.getParentId()));			
		}
		
		SortedMap contextsById = Context.sortedMapById(contexts);			
		SortedSet contextAdditions = new TreeSet();		
		SortedSet contextChanges = new TreeSet();
		SortedSet contextRemovals = new TreeSet();		
		iterator = contextsById.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String id = (String) entry.getKey();
			Context context = (Context) entry.getValue();
			
			if (!this.contextsById.containsKey(id))
				contextAdditions.add(id);
			else if (!Util.equals(context, this.contextsById.get(id)))
				contextChanges.add(id);								
		}

		iterator = this.contextsById.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String id = (String) entry.getKey();
			Context context = (Context) entry.getValue();
			
			if (!contextsById.containsKey(id))
				contextRemovals.add(id);						
		}

		SortedSet contextSetChanges = new TreeSet();
		contextSetChanges.addAll(contextAdditions);		
		contextSetChanges.addAll(contextChanges);		
		contextSetChanges.addAll(contextRemovals);
		
		if (!contextSetChanges.isEmpty()) {
			this.contextsById = contextsById;		
			SortedSet definedContextIds = new TreeSet(contextsById.keySet());

			if (!Util.equals(definedContextIds, this.definedContextIds)) {	
				this.definedContextIds = definedContextIds;
				fireContextManagerChanged();
			}

			iterator = contextSetChanges.iterator();
		
			while (iterator.hasNext()) {
				String contextId = (String) iterator.next();					
				ContextHandle contextHandle = (ContextHandle) contextHandlesById.get(contextId);
			
				if (contextHandle != null) {			
					if (contextsById.containsKey(contextId))
						contextHandle.define((Context) contextsById.get(contextId));
					else
						contextHandle.undefine();
				}
			}			
		}
	}
	
	private void fireContextManagerChanged() {
		if (contextManagerListeners != null) {
			// TODO copying to avoid ConcurrentModificationException
			Iterator iterator = new ArrayList(contextManagerListeners).iterator();	
			
			if (iterator.hasNext()) {
				if (contextManagerEvent == null)
					contextManagerEvent = new ContextManagerEvent(this);
				
				while (iterator.hasNext())	
					((IContextManagerListener) iterator.next()).contextManagerChanged(contextManagerEvent);
			}							
		}			
	}
}
