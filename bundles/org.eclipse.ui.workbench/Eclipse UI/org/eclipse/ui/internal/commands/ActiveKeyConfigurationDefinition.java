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

public final class ActiveKeyConfigurationDefinition
	implements IActiveKeyConfigurationDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		ActiveKeyConfigurationDefinition.class.getName().hashCode();

	private transient int hashCode;
	private transient boolean hashCodeComputed;

	private String keyConfigurationId;
	private String pluginId;
	private transient String string;

	public ActiveKeyConfigurationDefinition(
		String keyConfigurationId,
		String pluginId) {
		this.keyConfigurationId = keyConfigurationId;
		this.pluginId = pluginId;
	}

	public int compareTo(Object object) {
		ActiveKeyConfigurationDefinition castedObject =
			(ActiveKeyConfigurationDefinition) object;
		int compareTo =
			Util.compare(keyConfigurationId, castedObject.keyConfigurationId);

		if (compareTo == 0)
			compareTo = Util.compare(pluginId, castedObject.pluginId);

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof ActiveKeyConfigurationDefinition))
			return false;

		ActiveKeyConfigurationDefinition castedObject =
			(ActiveKeyConfigurationDefinition) object;
		boolean equals = true;
		equals
			&= Util.equals(keyConfigurationId, castedObject.keyConfigurationId);
		equals &= Util.equals(pluginId, castedObject.pluginId);
		return equals;
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
			hashCode =
				hashCode * HASH_FACTOR + Util.hashCode(keyConfigurationId);
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
			stringBuffer.append(',');
			stringBuffer.append(pluginId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
