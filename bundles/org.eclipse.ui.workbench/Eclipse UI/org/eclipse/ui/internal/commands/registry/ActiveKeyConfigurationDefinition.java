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

import org.eclipse.ui.commands.IActiveKeyConfigurationDefinition;
import org.eclipse.ui.internal.util.Util;

final class ActiveKeyConfigurationDefinition implements Comparable, IActiveKeyConfigurationDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ActiveKeyConfigurationDefinition.class.getName().hashCode();

	private String keyConfigurationId;
	private String pluginId;

	ActiveKeyConfigurationDefinition(String keyConfigurationId, String pluginId) {
		super();
		
		if (keyConfigurationId == null)
			throw new NullPointerException();
		
		this.keyConfigurationId = keyConfigurationId;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		ActiveKeyConfigurationDefinition activeKeyConfigurationDefinition = (ActiveKeyConfigurationDefinition) object;
		int compareTo = keyConfigurationId.compareTo(activeKeyConfigurationDefinition.keyConfigurationId);			

		if (compareTo == 0)
			compareTo = Util.compare(pluginId, activeKeyConfigurationDefinition.pluginId);								

		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ActiveKeyConfigurationDefinition))
			return false;

		ActiveKeyConfigurationDefinition activeKeyConfigurationDefinition = (ActiveKeyConfigurationDefinition) object;	
		return keyConfigurationId.equals(activeKeyConfigurationDefinition.keyConfigurationId) && Util.equals(pluginId, activeKeyConfigurationDefinition.pluginId);
	}

	public String getKeyConfigurationId() {
		return keyConfigurationId;
	}

	public String getPluginId() {
		return pluginId;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + keyConfigurationId.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(pluginId);
		return result;
	}

	public String toString() {
		return '[' + keyConfigurationId + ']';
	}
}
