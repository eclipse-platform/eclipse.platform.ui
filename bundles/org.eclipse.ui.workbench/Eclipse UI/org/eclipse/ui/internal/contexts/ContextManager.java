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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextHandle;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IContextManagerEvent;
import org.eclipse.ui.contexts.IContextManagerListener;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;

public final class ContextManager implements IContextManager {

	private static ContextManager instance;

	public static ContextManager getInstance() {
		if (instance == null)
			instance = new ContextManager();
			
		return instance;
	}

	private SortedSet activeContextIds = new TreeSet();
	private IContextManagerEvent contextManagerEvent;
	private List contextManagerListeners;
	private SortedMap contextHandlesById = new TreeMap();
	private SortedMap contextsById = new TreeMap();
	private SortedSet definedContextIds = new TreeSet();
	private IRegistry pluginRegistry;
	private IMutableRegistry preferenceRegistry;

	private ContextManager() {
		super();
		loadPluginRegistry();
		loadPreferenceRegistry();
		updateFromRegistries();
	}

	public void addContextManagerListener(IContextManagerListener contextManagerListener) {
		if (contextManagerListener == null)
			throw new NullPointerException();
			
		if (contextManagerListeners == null)
			contextManagerListeners = new ArrayList();
		
		if (!contextManagerListeners.contains(contextManagerListener))
			contextManagerListeners.add(contextManagerListener);
	}

	public SortedSet getActiveContextIds() {
		return Collections.unmodifiableSortedSet(activeContextIds);
	}

	public IContextHandle getContextHandle(String contextId) {
		if (contextId == null)
			throw new NullPointerException();
			
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

	public void removeContextManagerListener(IContextManagerListener contextManagerListener) {
		if (contextManagerListener == null)
			throw new NullPointerException();
			
		if (contextManagerListeners != null) {
			contextManagerListeners.remove(contextManagerListener);
			
			if (contextManagerListeners.isEmpty())
				contextManagerListeners = null;
		}
	}

	public void setActiveContextIds(SortedSet activeContextIds) {
		activeContextIds = Util.safeCopy(activeContextIds, String.class);
		
		if (activeContextIds.equals(this.activeContextIds)) {
			this.activeContextIds = activeContextIds;	
			fireContextManagerChanged();
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
	
	private void loadPluginRegistry() {
		if (pluginRegistry == null)
			pluginRegistry = new PluginRegistry(Platform.getPluginRegistry());
		
		try {
			pluginRegistry.load();
		} catch (IOException eIO) {
			// TODO proper catch
		}
	}
	
	private void loadPreferenceRegistry() {
		if (preferenceRegistry == null)
			preferenceRegistry = new PreferenceRegistry(WorkbenchPlugin.getDefault().getPreferenceStore());
		
		try {
			preferenceRegistry.load();
		} catch (IOException eIO) {
			// TODO proper catch
		}
	}

	private void updateFromRegistries() {
		if (pluginRegistry == null)
			pluginRegistry = new PluginRegistry(Platform.getPluginRegistry());
		
		try {
			pluginRegistry.load();
		} catch (IOException eIO) {
			// TODO proper catch
		}

		List contexts = new ArrayList();
		contexts.addAll(pluginRegistry.getContexts());
		contexts.addAll(preferenceRegistry.getContexts());
		SortedMap contextsById = Context.sortedMapById(contexts);			
		SortedSet contextChanges = new TreeSet();
		Util.diff(contextsById, this.contextsById, contextChanges, contextChanges, contextChanges);
		boolean contextManagerChanged = false;
				
		if (!contextChanges.isEmpty()) {
			this.contextsById = contextsById;		
			SortedSet definedContextIds = new TreeSet(contextsById.keySet());
	
			if (!Util.equals(definedContextIds, this.definedContextIds)) {	
				this.definedContextIds = definedContextIds;
				contextManagerChanged = true;
			}
		}

		if (contextManagerChanged)
			fireContextManagerChanged();

		if (!contextChanges.isEmpty()) {
			Iterator iterator = contextChanges.iterator();
		
			while (iterator.hasNext()) {
				String contextId = (String) iterator.next();					
				ContextHandle contextHandle = (ContextHandle) contextHandlesById.get(contextId);
			
				if (contextHandle != null) {			
					if (contextsById.containsKey(contextId))
						contextHandle.define((IContext) contextsById.get(contextId));
					else
						contextHandle.undefine();
				}
			}			
		}
	}			
}
