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

import org.eclipse.ui.commands.IKeyBindingDefinition;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

final class KeyBindingDefinition implements IKeyBindingDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = KeyBindingDefinition.class.getName().hashCode();

	private String commandId;
	private String contextId;
	private String keyConfigurationId;
	private KeySequence keySequence;	
	private String locale;
	private String platform;
	private String pluginId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;

	KeyBindingDefinition(String commandId, String contextId, String keyConfigurationId, KeySequence keySequence, String locale, String platform, String pluginId) {	
		if (commandId == null || contextId == null || keyConfigurationId == null || keySequence == null || locale == null || platform == null)
			throw new NullPointerException();
		
		this.commandId = commandId;
		this.contextId = contextId;
		this.keyConfigurationId = keyConfigurationId;
		this.keySequence = keySequence;
		this.locale = locale;
		this.platform = platform;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		KeyBindingDefinition keyBindingDefinition = (KeyBindingDefinition) object;
		int compareTo = commandId.compareTo(keyBindingDefinition.commandId);
		
		if (compareTo == 0) {		
			compareTo = contextId.compareTo(keyBindingDefinition.contextId);			

			if (compareTo == 0) {		
				compareTo = keyConfigurationId.compareTo(keyBindingDefinition.keyConfigurationId);			

				if (compareTo == 0) {
					compareTo = keySequence.compareTo(keyBindingDefinition.keySequence);

					if (compareTo == 0) {		
						compareTo = locale.compareTo(keyBindingDefinition.locale);			
	
						if (compareTo == 0) {		
							compareTo = platform.compareTo(keyBindingDefinition.platform);			
			
							if (compareTo == 0)
								compareTo = Util.compare(pluginId, keyBindingDefinition.pluginId);
						}
					}
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof KeyBindingDefinition))
			return false;

		KeyBindingDefinition keyBindingDefinition = (KeyBindingDefinition) object;	
		boolean equals = true;
		equals &= commandId.equals(keyBindingDefinition.commandId);
		equals &= contextId.equals(keyBindingDefinition.contextId);
		equals &= keyConfigurationId.equals(keyBindingDefinition.keyConfigurationId);
		equals &= keySequence.equals(keyBindingDefinition.keySequence);
		equals &= locale.equals(keyBindingDefinition.locale);
		equals &= platform.equals(keyBindingDefinition.platform);
		equals &= Util.equals(pluginId, keyBindingDefinition.pluginId);
		return equals;
	}

	public String getCommandId() {
		return commandId;
	}

	public String getContextId() {
		return contextId;
	}

	public String getKeyConfigurationId() {
		return keyConfigurationId;
	}

	public KeySequence getKeySequence() {
		return keySequence;
	}

	public String getLocale() {
		return locale;
	}

	public String getPlatform() {
		return platform;
	}
	
	public String getPluginId() {
		return pluginId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + commandId.hashCode();
			hashCode = hashCode * HASH_FACTOR + contextId.hashCode();
			hashCode = hashCode * HASH_FACTOR + keyConfigurationId.hashCode();
			hashCode = hashCode * HASH_FACTOR + keySequence.hashCode();
			hashCode = hashCode * HASH_FACTOR + locale.hashCode();
			hashCode = hashCode * HASH_FACTOR + platform.hashCode();
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
			stringBuffer.append(keyConfigurationId);
			stringBuffer.append(',');
			stringBuffer.append(keySequence);
			stringBuffer.append(',');
			stringBuffer.append(locale);
			stringBuffer.append(',');
			stringBuffer.append(platform);
			stringBuffer.append(',');
			stringBuffer.append(pluginId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;
	}
}
