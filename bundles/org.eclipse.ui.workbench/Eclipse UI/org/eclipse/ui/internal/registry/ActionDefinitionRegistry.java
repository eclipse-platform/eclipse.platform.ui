/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Platform;

/**
 * Provides access to a list of action defintions.
 */
public class ActionDefinitionRegistry {
	private List actionDefinitions;
	/**
	 * Create an instance of Accelerator and initializes it.
	 */
	public ActionDefinitionRegistry() {
		actionDefinitions = new ArrayList();		
	}
	/**
	 * Adds an action definition to the registry.
	 */	
	public boolean add(ActionDefinition a) {
		return actionDefinitions.add(a);	
	}
	/**
	 * Returns the action definition with the same ID;
	 */
	public ActionDefinition getDefinition(String id) {
		for (Iterator iterator = actionDefinitions.iterator(); iterator.hasNext();) {
			ActionDefinition element = (ActionDefinition)iterator.next();
			if(element.getId().equals(id))
				return element;
		}
		return null;
	}
	/**
	 * Loads the action definition registry from the platform's plugin registry.
	 */	
	public void load() {
		ActionDefinitionRegistryReader reader = new ActionDefinitionRegistryReader();
		reader.readActionDefinitions(Platform.getPluginRegistry(), this);
	}
}
