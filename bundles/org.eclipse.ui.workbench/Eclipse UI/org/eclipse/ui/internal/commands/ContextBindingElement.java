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

package org.eclipse.ui.internal.commands;

import org.eclipse.ui.internal.util.Util;

final class ContextBindingElement implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ContextBindingElement.class.getName().hashCode();

	static ContextBindingElement create(String commandId, String contextId, String pluginId)
		throws IllegalArgumentException {
		return new ContextBindingElement(commandId, contextId, pluginId);
	}

	private String commandId;
	private String contextId;
	private String pluginId;
	
	private ContextBindingElement(String commandId, String contextId, String pluginId)
		throws IllegalArgumentException {
		super();
		
		if (commandId == null || contextId == null)
			throw new IllegalArgumentException();
		
		this.commandId = commandId;
		this.contextId = contextId;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		ContextBindingElement contextBindingElement = (ContextBindingElement) object;
		int compareTo = commandId.compareTo(contextBindingElement.commandId);
		
		if (compareTo == 0) {	
			compareTo = contextId.compareTo(contextBindingElement.contextId);			
		
			if (compareTo == 0)
				compareTo = Util.compare(pluginId, contextBindingElement.pluginId);								
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ContextBindingElement))
			return false;

		ContextBindingElement contextBindingElement = (ContextBindingElement) object;	
		return commandId.equals(contextBindingElement.commandId) && contextId.equals(contextBindingElement.contextId) && Util.equals(pluginId, contextBindingElement.pluginId);
	}

	String getCommandId() {
		return commandId;	
	}
	
	String getContextId() {
		return contextId;	
	}
	
	String getPluginId() {
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
