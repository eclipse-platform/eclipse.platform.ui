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
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IContextManagerEvent;
import org.eclipse.ui.contexts.IContextManagerListener;
import org.eclipse.ui.handles.IHandle;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.contexts.registry.IContextDefinition;
import org.eclipse.ui.internal.contexts.registry.IContextRegistry;
import org.eclipse.ui.internal.contexts.registry.IContextRegistryEvent;
import org.eclipse.ui.internal.contexts.registry.IContextRegistryListener;
import org.eclipse.ui.internal.contexts.registry.PluginContextRegistry;
import org.eclipse.ui.internal.contexts.registry.PreferenceContextRegistry;
import org.eclipse.ui.internal.handles.Handle;
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
	private PluginContextRegistry pluginContextRegistry;
	private PreferenceContextRegistry preferenceContextRegistry;

	private ContextManager() {
		if (pluginContextRegistry == null)
			pluginContextRegistry = new PluginContextRegistry(Platform.getPluginRegistry());
			
		loadPluginContextRegistry();		

		pluginContextRegistry.addContextRegistryListener(new IContextRegistryListener() {
			public void contextRegistryChanged(IContextRegistryEvent contextRegistryEvent) {
				update();
			}
		});

		if (preferenceContextRegistry == null)
			preferenceContextRegistry = new PreferenceContextRegistry(WorkbenchPlugin.getDefault().getPreferenceStore());	

		loadPreferenceContextRegistry();

		preferenceContextRegistry.addContextRegistryListener(new IContextRegistryListener() {
			public void contextRegistryChanged(IContextRegistryEvent contextRegistryEvent) {
				update();
			}
		});
		
		update();
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

	public IHandle getContextHandle(String contextId) {
		if (contextId == null)
			throw new NullPointerException();
			
		IHandle handle = (IHandle) contextHandlesById.get(contextId);
		
		if (handle == null) {
			handle = new Handle(contextId);
			contextHandlesById.put(contextId, handle);
		}
		
		return handle;
	}

	public SortedSet getDefinedContextIds() {
		return Collections.unmodifiableSortedSet(new TreeSet(contextsById.keySet()));
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
		
		if (!activeContextIds.equals(this.activeContextIds)) {
			this.activeContextIds = activeContextIds;	
			update();
		}
	}

	IContextRegistry getPluginContextRegistry() {
		return pluginContextRegistry;
	}

	IContextRegistry getPreferenceContextRegistry() {
		return preferenceContextRegistry;
	}

	void loadPluginContextRegistry() {
		try {
			pluginContextRegistry.load();
		} catch (IOException eIO) {
			// TODO proper catch
		}
	}
	
	void loadPreferenceContextRegistry() {
		try {
			preferenceContextRegistry.load();
		} catch (IOException eIO) {
			// TODO proper catch
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
	
	private void update() {
		List contextDefinitions = new ArrayList();
		contextDefinitions.addAll(pluginContextRegistry.getContextDefinitions());
		contextDefinitions.addAll(preferenceContextRegistry.getContextDefinitions());
		SortedMap contextsById = new TreeMap();
		Iterator iterator = contextDefinitions.iterator();
		
		while (iterator.hasNext()) {
			IContextDefinition contextDefinition = (IContextDefinition) iterator.next();
			IContext context = new Context(activeContextIds.contains(contextDefinition.getId()), contextDefinition.getDescription(), contextDefinition.getId(), contextDefinition.getName(), contextDefinition.getParentId());		
			contextsById.put(context.getId(), context);
		}

		SortedSet contextChanges = new TreeSet();
		Util.diff(contextsById, this.contextsById, contextChanges, contextChanges, contextChanges);
		boolean contextManagerChanged = false;
				
		if (!contextChanges.isEmpty()) {
			this.contextsById = contextsById;		
			contextManagerChanged = true;
		}

		if (contextManagerChanged)
			fireContextManagerChanged();

		if (!contextChanges.isEmpty()) {
			iterator = contextChanges.iterator();
		
			while (iterator.hasNext()) {
				String contextId = (String) iterator.next();					
				Handle handle = (Handle) contextHandlesById.get(contextId);
			
				if (handle != null) {			
					if (contextsById.containsKey(contextId))
						handle.define((IContext) contextsById.get(contextId));
					else
						handle.undefine();
				}
			}			
		}
	}		
}
