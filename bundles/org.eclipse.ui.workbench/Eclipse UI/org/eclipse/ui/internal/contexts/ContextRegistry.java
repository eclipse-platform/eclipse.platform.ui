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
import org.eclipse.ui.contexts.IContextDefinition;
import org.eclipse.ui.contexts.IContextDefinitionHandle;
import org.eclipse.ui.contexts.IContextRegistry;
import org.eclipse.ui.contexts.IContextRegistryEvent;
import org.eclipse.ui.contexts.IContextRegistryListener;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;

public final class ContextRegistry implements IContextRegistry {

	private static ContextRegistry instance;

	public static ContextRegistry getInstance() {
		if (instance == null)
			instance = new ContextRegistry();
			
		return instance;
	}

	private IContextRegistryEvent contextRegistryEvent;
	private List contextRegistryListeners;
	private SortedMap contextDefinitionHandlesById = new TreeMap();
	private SortedMap contextDefinitionsById = new TreeMap();
	private IRegistry pluginRegistry;
	private IMutableRegistry preferenceRegistry;

	private ContextRegistry() {
		super();
		loadPluginRegistry();
		loadPreferenceRegistry();
		update();
	}

	public void addContextRegistryListener(IContextRegistryListener contextRegistryListener) {
		if (contextRegistryListener == null)
			throw new NullPointerException();
			
		if (contextRegistryListeners == null)
			contextRegistryListeners = new ArrayList();
		
		if (!contextRegistryListeners.contains(contextRegistryListener))
			contextRegistryListeners.add(contextRegistryListener);
	}

	public IContextDefinitionHandle getContextDefinitionHandle(String contextDefinitionId) {
		if (contextDefinitionId == null)
			throw new NullPointerException();
			
		IContextDefinitionHandle contextDefinitionHandle = (IContextDefinitionHandle) contextDefinitionHandlesById.get(contextDefinitionId);
		
		if (contextDefinitionHandle == null) {
			contextDefinitionHandle = new ContextDefinitionHandle(contextDefinitionId);
			contextDefinitionHandlesById.put(contextDefinitionId, contextDefinitionHandle);
		}
		
		return contextDefinitionHandle;
	}

	public SortedMap getContextDefinitionsById() {
		return Collections.unmodifiableSortedMap(contextDefinitionsById);
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

	private void fireContextRegistryChanged() {
		if (contextRegistryListeners != null) {
			// TODO copying to avoid ConcurrentModificationException
			Iterator iterator = new ArrayList(contextRegistryListeners).iterator();	
			
			if (iterator.hasNext()) {
				if (contextRegistryEvent == null)
					contextRegistryEvent = new ContextRegistryEvent(this);
				
				while (iterator.hasNext())	
					((IContextRegistryListener) iterator.next()).contextRegistryChanged(contextRegistryEvent);
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

	private void update() {
		if (pluginRegistry == null)
			pluginRegistry = new PluginRegistry(Platform.getPluginRegistry());
		
		try {
			pluginRegistry.load();
		} catch (IOException eIO) {
			// TODO proper catch
		}

		List contextDefinitions = new ArrayList();
		contextDefinitions.addAll(pluginRegistry.getContextDefinitions());
		contextDefinitions.addAll(preferenceRegistry.getContextDefinitions());
		SortedMap contextDefinitionsById = ContextDefinition.sortedMapById(contextDefinitions);			
		SortedSet contextDefinitionChanges = new TreeSet();
		Util.diff(contextDefinitionsById, this.contextDefinitionsById, contextDefinitionChanges, contextDefinitionChanges, contextDefinitionChanges);
		boolean contextRegistryChanged = false;
				
		if (!contextDefinitionChanges.isEmpty()) {
			this.contextDefinitionsById = contextDefinitionsById;		
			contextRegistryChanged = true;
		}

		if (contextRegistryChanged)
			fireContextRegistryChanged();

		if (!contextDefinitionChanges.isEmpty()) {
			Iterator iterator = contextDefinitionChanges.iterator();
		
			while (iterator.hasNext()) {
				String contextDefinitionId = (String) iterator.next();					
				ContextDefinitionHandle contextDefinitionHandle = (ContextDefinitionHandle) contextDefinitionHandlesById.get(contextDefinitionId);
			
				if (contextDefinitionHandle != null) {			
					if (contextDefinitionsById.containsKey(contextDefinitionId))
						contextDefinitionHandle.define((IContextDefinition) contextDefinitionsById.get(contextDefinitionId));
					else
						contextDefinitionHandle.undefine();
				}
			}			
		}
	}			
}
