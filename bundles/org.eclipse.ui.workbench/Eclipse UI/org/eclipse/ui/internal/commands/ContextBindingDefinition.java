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

import org.eclipse.ui.internal.commands.api.IContextBindingDefinition;
import org.eclipse.ui.internal.util.Util;

final class ContextBindingDefinition implements IContextBindingDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ContextBindingDefinition.class.getName().hashCode();

	private String commandId;
	private String contextId;
	private String pluginId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;

	ContextBindingDefinition(String commandId, String contextId, String pluginId) {
		this.commandId = commandId;
		this.contextId = contextId;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		ContextBindingDefinition contextBindingDefinition = (ContextBindingDefinition) object;
		int compareTo = Util.compare(commandId, contextBindingDefinition.commandId);
		
		if (compareTo == 0) {		
			compareTo = Util.compare(contextId, contextBindingDefinition.contextId);			
		
			if (compareTo == 0)
				compareTo = Util.compare(pluginId, contextBindingDefinition.pluginId);								
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ContextBindingDefinition))
			return false;

		ContextBindingDefinition contextBindingDefinition = (ContextBindingDefinition) object;	
		boolean equals = true;
		equals &= Util.equals(commandId, contextBindingDefinition.commandId);
		equals &= Util.equals(contextId, contextBindingDefinition.contextId);
		equals &= Util.equals(pluginId, contextBindingDefinition.pluginId);
		return equals;
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
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(commandId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(contextId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(pluginId);
			hashCodeComputed = true;
		}
			
		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(commandId);
			stringBuffer.append(',');
			stringBuffer.append(contextId);
			stringBuffer.append(',');
			stringBuffer.append(pluginId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;
	}	
}
