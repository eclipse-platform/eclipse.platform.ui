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

package org.eclipse.ui.internal.commands.registry;

import org.eclipse.ui.commands.IContextBindingDefinition;
import org.eclipse.ui.internal.util.Util;

final class ContextBindingDefinition implements Comparable, IContextBindingDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ContextBindingDefinition.class.getName().hashCode();

	private String commandId;
	private String contextId;
	private String pluginId;

	ContextBindingDefinition(String commandId, String contextId, String pluginId) {
		super();
		
		if (commandId == null || contextId == null)
			throw new NullPointerException();
		
		this.commandId = commandId;
		this.contextId = contextId;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		ContextBindingDefinition contextBindingDefinition = (ContextBindingDefinition) object;
		int compareTo = commandId.compareTo(contextBindingDefinition.commandId);
		
		if (compareTo == 0) {		
			compareTo = contextId.compareTo(contextBindingDefinition.contextId);			
		
			if (compareTo == 0)
				compareTo = Util.compare(pluginId, contextBindingDefinition.pluginId);								
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ContextBindingDefinition))
			return false;

		ContextBindingDefinition contextBindingDefinition = (ContextBindingDefinition) object;	
		return commandId.equals(contextBindingDefinition.commandId) && contextId.equals(contextBindingDefinition.contextId) && Util.equals(pluginId, contextBindingDefinition.pluginId);
	}

	public String getCommandId() {
		return commandId;
	}

	public String getContextId() {
		return contextId;
	}

	public String getPluginId() {
		return pluginId;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + commandId.hashCode();
		result = result * HASH_FACTOR + contextId.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(pluginId);
		return result;
	}

	public String toString() {
		return '[' + commandId + ',' + contextId + ',' + pluginId + ']';
	}
}
