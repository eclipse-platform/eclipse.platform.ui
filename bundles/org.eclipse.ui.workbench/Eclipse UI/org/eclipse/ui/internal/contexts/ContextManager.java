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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.eclipse.ui.internal.contexts.api.IContextDefinition;
import org.eclipse.ui.internal.contexts.api.IContextRegistry;
import org.eclipse.ui.internal.contexts.api.IContextRegistryEvent;
import org.eclipse.ui.internal.contexts.api.IContextRegistryListener;
import org.eclipse.ui.internal.util.Util;

public final class ContextManager implements IContextManager {

	private static ContextManager instance;

	public static ContextManager getInstance() {
		if (instance == null)
			instance = new ContextManager();
			
		return instance;
	}

	public static boolean isContextDefinitionChildOf(String ancestor, String id, Map contextDefinitionsById) {
		Set visited = new HashSet();

		while (id != null && !visited.contains(id)) {
			IContextDefinition contextDefinition = (IContextDefinition) contextDefinitionsById.get(id);				
			visited.add(id);

			if (contextDefinition != null && Util.equals(id = contextDefinition.getParentId(), ancestor))
				return true;
		}

		return false;
	}	

	private List activeContextIds = new ArrayList();
	private IContextManagerEvent contextManagerEvent;
	private List contextManagerListeners;
	private SortedMap contextDefinitionsById = new TreeMap();
	private SortedMap contextsById = new TreeMap();
	private SortedSet definedContextIds = new TreeSet();
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
		return Collections.unmodifiableSortedSet(definedContextIds);
	}

	public void removeContextManagerListener(IContextManagerListener contextManagerListener) {
		if (contextManagerListener == null)
			throw new NullPointerException();
			
		if (contextManagerListeners != null)
			contextManagerListeners.remove(contextManagerListener);
	}

	public void setActiveContextIds(List activeContextIds) {
		activeContextIds = Util.safeCopy(activeContextIds, String.class);
		boolean contextManagerChanged = false;
		SortedSet updatedContextIds = null;

		if (!this.activeContextIds.equals(activeContextIds)) {
			this.activeContextIds = activeContextIds;
			contextManagerChanged = true;	
			updatedContextIds = updateContexts(this.definedContextIds);	
		}
		
		if (contextManagerChanged)
			fireContextManagerChanged();

		if (updatedContextIds != null)
			notifyContexts(updatedContextIds);	
	}

	// TODO private
	public IContextRegistry getPluginContextRegistry() {
		return pluginContextRegistry;
	}

	// TODO private
	public IContextRegistry getPreferenceContextRegistry() {
		return preferenceContextRegistry;
	}

	private void loadPluginContextRegistry() {
		try {
			pluginContextRegistry.load();
		} catch (IOException eIO) {
			eIO.printStackTrace();
		}
	}
	
	private void loadPreferenceContextRegistry() {
		try {
			preferenceContextRegistry.load();
		} catch (IOException eIO) {
			eIO.printStackTrace();
		}		
	}

	private void fireContextManagerChanged() {
		if (contextManagerListeners != null) {
			for (int i = 0; i < contextManagerListeners.size(); i++) {
				if (contextManagerEvent == null)
					contextManagerEvent = new ContextManagerEvent(this);
								
				((IContextManagerListener) contextManagerListeners.get(i)).contextManagerChanged(contextManagerEvent);
			}				
		}			
	}

	private void notifyContexts(Collection contextIds) {	
		Iterator iterator = contextIds.iterator();
		
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
		SortedMap contextDefinitionsById = new TreeMap(ContextDefinition.contextDefinitionsById(contextDefinitions, false));

		for (Iterator iterator = contextDefinitionsById.values().iterator(); iterator.hasNext();) {
			IContextDefinition contextDefinition = (IContextDefinition) iterator.next();
			String name = contextDefinition.getName();
				
			if (name == null || name.length() == 0)
				iterator.remove();
		}

		for (Iterator iterator = contextDefinitionsById.keySet().iterator(); iterator.hasNext();)
			if (!isContextDefinitionChildOf(null, (String) iterator.next(), contextDefinitionsById))
				iterator.remove();

		SortedSet definedContextIds = new TreeSet(contextDefinitionsById.keySet());		
		boolean contextManagerChanged = false;

		if (!this.definedContextIds.equals(definedContextIds)) {
			this.definedContextIds = definedContextIds;
			contextManagerChanged = true;	
		}

		this.contextDefinitionsById = contextDefinitionsById;
		SortedSet updatedContextIds = updateContexts(this.definedContextIds);	
		
		if (contextManagerChanged)
			fireContextManagerChanged();

		if (updatedContextIds != null)
			notifyContexts(updatedContextIds);		
	}

	private boolean updateContext(Context context) {
		boolean updated = false;
		updated |= context.setActive(activeContextIds.contains(context.getId()));
		IContextDefinition contextDefinition = (IContextDefinition) contextDefinitionsById.get(context.getId());
		updated |= context.setDefined(contextDefinition != null);
		updated |= context.setDescription(contextDefinition != null ? contextDefinition.getDescription() : null);
		updated |= context.setName(contextDefinition != null ? contextDefinition.getName() : null);
		updated |= context.setParentId(contextDefinition != null ? contextDefinition.getParentId() : null);
		return updated;
	}

	private SortedSet updateContexts(Collection contextIds) {
		SortedSet updatedIds = new TreeSet();
		Iterator iterator = contextIds.iterator();
		
		while (iterator.hasNext()) {
			String contextId = (String) iterator.next();					
			Context context = (Context) contextsById.get(contextId);
			
			if (context != null && updateContext(context))
				updatedIds.add(contextId);			
		}
		
		return updatedIds;			
	}
}