package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;

/**
 * Provides access to a list of action defintions.
 */
public class ActionDefinitionRegistry {
	private List actionDefinitions;
	
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
	 * Loads the action definition registry from the platform's plugin registry.
	 */	
	public void load() {
		ActionDefinitionRegistryReader reader = new ActionDefinitionRegistryReader();
		reader.readActionDefinitions(Platform.getPluginRegistry(), this);
	}
}
