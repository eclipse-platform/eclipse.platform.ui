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

import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

//TODO private
public final class KeyBindingDefinition implements IKeyBindingDefinition {

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

	// TODO private
	public KeyBindingDefinition(String commandId, String contextId, String keyConfigurationId, KeySequence keySequence, String locale, String platform, String pluginId) {	
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
		int compareTo = Util.compare(commandId, keyBindingDefinition.commandId);
		
		if (compareTo == 0) {		
			compareTo = Util.compare(contextId, keyBindingDefinition.contextId);			

			if (compareTo == 0) {		
				compareTo = Util.compare(keyConfigurationId, keyBindingDefinition.keyConfigurationId);			

				if (compareTo == 0) {
					compareTo = Util.compare(keySequence, keyBindingDefinition.keySequence);

					if (compareTo == 0) {		
						compareTo = Util.compare(locale, keyBindingDefinition.locale);			
	
						if (compareTo == 0) {		
							compareTo = Util.compare(platform, keyBindingDefinition.platform);			
			
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
		equals &= Util.equals(commandId, keyBindingDefinition.commandId);
		equals &= Util.equals(contextId, keyBindingDefinition.contextId);
		equals &= Util.equals(keyConfigurationId, keyBindingDefinition.keyConfigurationId);
		equals &= Util.equals(keySequence, keyBindingDefinition.keySequence);
		equals &= Util.equals(locale, keyBindingDefinition.locale);
		equals &= Util.equals(platform, keyBindingDefinition.platform);
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
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(commandId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(contextId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(keyConfigurationId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(keySequence);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(locale);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(platform);
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
