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

import org.eclipse.ui.commands.IActiveKeyConfiguration;
import org.eclipse.ui.internal.util.Util;

final class ActiveKeyConfiguration implements Comparable, IActiveKeyConfiguration {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ActiveKeyConfiguration.class.getName().hashCode();

	private String keyConfigurationId;
	private String pluginId;

	ActiveKeyConfiguration(String keyConfigurationId, String pluginId) {
		super();
		
		if (keyConfigurationId == null)
			throw new NullPointerException();
		
		this.keyConfigurationId = keyConfigurationId;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		ActiveKeyConfiguration activeKeyConfiguration = (ActiveKeyConfiguration) object;
		int compareTo = keyConfigurationId.compareTo(activeKeyConfiguration.keyConfigurationId);			

		if (compareTo == 0)
			compareTo = Util.compare(pluginId, activeKeyConfiguration.pluginId);								

		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ActiveKeyConfiguration))
			return false;

		ActiveKeyConfiguration activeKeyConfiguration = (ActiveKeyConfiguration) object;	
		return keyConfigurationId.equals(activeKeyConfiguration.keyConfigurationId) && Util.equals(pluginId, activeKeyConfiguration.pluginId);
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
