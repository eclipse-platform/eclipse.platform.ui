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

import org.eclipse.ui.commands.registry.IActiveKeyConfigurationDefinition;
import org.eclipse.ui.internal.util.Util;

final class ActiveKeyConfigurationDefinition implements Comparable, IActiveKeyConfigurationDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ActiveKeyConfigurationDefinition.class.getName().hashCode();

	private String keyConfigurationId;
	private String pluginId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;

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
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + keyConfigurationId.hashCode();
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(pluginId);
			hashCodeComputed = true;
		}
			
		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(keyConfigurationId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;
	}
}
