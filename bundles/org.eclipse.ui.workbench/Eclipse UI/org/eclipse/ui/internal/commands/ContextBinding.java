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

import org.eclipse.ui.commands.IContextBinding;
import org.eclipse.ui.internal.util.Util;

final class ContextBinding implements Comparable, IContextBinding {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ContextBinding.class.getName().hashCode();

	private String commandId;
	private String contextId;
	private String pluginId;

	ContextBinding(String commandId, String contextId, String pluginId) {
		super();
		
		if (commandId == null || contextId == null)
			throw new NullPointerException();
		
		this.commandId = commandId;
		this.contextId = contextId;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		ContextBinding contextBinding = (ContextBinding) object;
		int compareTo = commandId.compareTo(contextBinding.commandId);
		
		if (compareTo == 0) {		
			compareTo = contextId.compareTo(contextBinding.contextId);			
		
			if (compareTo == 0)
				compareTo = Util.compare(pluginId, contextBinding.pluginId);								
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ContextBinding))
			return false;

		ContextBinding contextBinding = (ContextBinding) object;	
		return commandId.equals(contextBinding.commandId) && contextId.equals(contextBinding.contextId) && Util.equals(pluginId, contextBinding.pluginId);
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
