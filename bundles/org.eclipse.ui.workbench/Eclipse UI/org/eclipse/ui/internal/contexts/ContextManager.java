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
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.contexts.registry.ContextDefinition;
import org.eclipse.ui.internal.contexts.registry.IContextDefinition;
import org.eclipse.ui.internal.contexts.registry.IContextRegistry;
import org.eclipse.ui.internal.contexts.registry.IContextRegistryEvent;
import org.eclipse.ui.internal.contexts.registry.IContextRegistryListener;
import org.eclipse.ui.internal.contexts.registry.PluginContextRegistry;
import org.eclipse.ui.internal.contexts.registry.PreferenceContextRegistry;
import org.eclipse.ui.internal.util.Util;

public final class ContextManager implements IContextManager {

	private static ContextManager instance;

	public static ContextManager getInstance() {
		if (instance == null)
			instance = new ContextManager();
			
		return instance;
	}

	private List activeContextIds = new ArrayList();
	private IContextManagerEvent contextManagerEvent;
	private List contextManagerListeners;
	private SortedMap contextDefinitionsById = new TreeMap();
	private SortedMap contextsById = new TreeMap();
	private PluginContextRegistry pluginContextRegistry;
	private PreferenceContextRegistry preferenceContextRegistry;

	private ContextManager() {
		if (pluginContextRegistry == null)
			pluginContextRegistry = new PluginContextRegistry(Platform.getPluginRegistry());
			
		loadPluginContextRegistry();		

		pluginContextRegistry.addContextRegistryListener(new IContextRegistryListener() {
			public void contextRegistryChanged(IContextRegistryEvent contextRegistryEvent) {
				readRegistry();
			}
		});

		if (preferenceContextRegistry == null)
			preferenceContextRegistry = new PreferenceContextRegistry(WorkbenchPlugin.getDefault().getPreferenceStore());	

		loadPreferenceContextRegistry();

		preferenceContextRegistry.addContextRegistryListener(new IContextRegistryListener() {
			public void contextRegistryChanged(IContextRegistryEvent contextRegistryEvent) {
				readRegistry();
			}
		});
		
		readRegistry();
	}

	public void addContextManagerListener(IContextManagerListener contextManagerListener) {
		if (contextManagerListener == null)
			throw new NullPointerException();
			
		if (contextManagerListeners == null)
			contextManagerListeners = new ArrayList();
		
		if (!contextManagerListeners.contains(contextManagerListener))
			contextManagerListeners.add(contextManagerListener);
	}

	public List getActiveContextIds() {
		return Collections.unmodifiableList(activeContextIds);
	}

	public IContext getContext(String contextId) {
		if (contextId == null)
			throw new NullPointerException();
			
		Context context = (Context) contextsById.get(contextId);
		
		if (context == null) {
			context = new Context(contextId);
			updateContext(context);
			contextsById.put(contextId, context);
		}
		
		return context;
	}
	
	public SortedSet getDefinedContextIds() {
		return Collections.unmodifiableSortedSet(new TreeSet(contextDefinitionsById.keySet()));
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

	public void setActiveContextIds(List activeContextIds) {
		activeContextIds = Util.safeCopy(activeContextIds, String.class);
		SortedSet contextChanges = new TreeSet();
		Util.diff(new TreeSet(activeContextIds), new TreeSet(this.activeContextIds), contextChanges, contextChanges);
		
		if (!contextChanges.isEmpty()) {
			this.activeContextIds = activeContextIds;	
			updateContexts(contextChanges);			
			fireContextManagerChanged();
			notifyContexts(contextChanges);
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

	private void notifyContexts(SortedSet contextChanges) {	
		Iterator iterator = contextChanges.iterator();
		
		while (iterator.hasNext()) {
			String contextId = (String) iterator.next();					
			Context context = (Context) contextsById.get(contextId);
			
			if (context != null)
				context.fireContextChanged();
		}
	}

	private void readRegistry() {
		List contextDefinitions = new ArrayList();
		contextDefinitions.addAll(pluginContextRegistry.getContextDefinitions());
		contextDefinitions.addAll(preferenceContextRegistry.getContextDefinitions());
		SortedMap contextDefinitionsById = ContextDefinition.sortedMapById(contextDefinitions);
		SortedSet contextChanges = new TreeSet();
		Util.diff(contextDefinitionsById, this.contextDefinitionsById, contextChanges, contextChanges, contextChanges);
	
		if (!contextChanges.isEmpty()) {
			this.contextDefinitionsById = contextDefinitionsById;	
			updateContexts(contextChanges);			
			fireContextManagerChanged();
			notifyContexts(contextChanges);
		}
	}

	private void updateContext(Context context) {
		context.setActive(activeContextIds.contains(context.getId()));
		IContextDefinition contextDefinition = (IContextDefinition) contextDefinitionsById.get(context.getId());
		context.setDefined(contextDefinition != null);
		context.setDescription(contextDefinition != null ? contextDefinition.getDescription() : null);
		context.setName(contextDefinition != null ? contextDefinition.getName() : Util.ZERO_LENGTH_STRING);
		context.setParentId(contextDefinition != null ? contextDefinition.getParentId() : null);
	}

	private void updateContexts(SortedSet contextChanges) {
		Iterator iterator = contextChanges.iterator();
		
		while (iterator.hasNext()) {
			String contextId = (String) iterator.next();					
			Context context = (Context) contextsById.get(contextId);
			
			if (context != null)
				updateContext(context);			
		}			
	}
}