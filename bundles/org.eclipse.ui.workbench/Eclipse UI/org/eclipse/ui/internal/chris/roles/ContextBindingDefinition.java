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

package org.eclipse.ui.internal.chris.roles;

import org.eclipse.ui.internal.roles.api.IContextBindingDefinition;
import org.eclipse.ui.internal.util.Util;

final class ContextBindingDefinition implements IContextBindingDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ContextBindingDefinition.class.getName().hashCode();

	private String contextId;
	private String pluginId;
	private String roleId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;

	ContextBindingDefinition(String contextId, String pluginId, String roleId) {
		this.contextId = contextId;
		this.pluginId = pluginId;
		this.roleId = roleId;
	}
	
	public int compareTo(Object object) {
		ContextBindingDefinition contextBindingDefinition = (ContextBindingDefinition) object;
		int compareTo = Util.compare(contextId, contextBindingDefinition.contextId);
		
		if (compareTo == 0) {		
			compareTo = Util.compare(pluginId, contextBindingDefinition.pluginId);			
		
			if (compareTo == 0)
				compareTo = Util.compare(roleId, contextBindingDefinition.roleId);								
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ContextBindingDefinition))
			return false;

		ContextBindingDefinition contextBindingDefinition = (ContextBindingDefinition) object;	
		boolean equals = true;
		equals &= Util.equals(contextId, contextBindingDefinition.contextId);
		equals &= Util.equals(pluginId, contextBindingDefinition.pluginId);
		equals &= Util.equals(roleId, contextBindingDefinition.roleId);
		return equals;
	}

	public String getContextId() {
		return contextId;
	}

	public String getPluginId() {
		return pluginId;
	}

	public String getRoleId() {
		return roleId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(contextId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(pluginId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(roleId);
			hashCodeComputed = true;
		}
			
		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(contextId);
			stringBuffer.append(',');
			stringBuffer.append(pluginId);
			stringBuffer.append(',');
			stringBuffer.append(roleId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;
	}	
}
