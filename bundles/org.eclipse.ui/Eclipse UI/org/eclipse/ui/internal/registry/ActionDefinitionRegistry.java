package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;

/**
 * 
 */
public class ActionDefinitionRegistry {
	private List actionDefinitions;
	
	public ActionDefinitionRegistry() {
		actionDefinitions = new ArrayList();		
	}
	
	public boolean add(ActionDefinition a) {
		return actionDefinitions.add(a);	
	}
	
	public void load() {
		ActionDefinitionRegistryReader reader = new ActionDefinitionRegistryReader();
		reader.readActionDefinitions(Platform.getPluginRegistry(), this);
	}
}
