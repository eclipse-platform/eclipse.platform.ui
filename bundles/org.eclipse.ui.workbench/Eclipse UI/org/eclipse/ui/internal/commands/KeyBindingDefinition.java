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

final class KeyBindingDefinition implements Comparable, IKeyBindingDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = KeyBindingDefinition.class.getName().hashCode();

	private String commandId;
	private String contextId;
	private String keyConfigurationId;
	private KeySequence keySequence;	
	private String locale;
	private String platform;
	private String pluginId;
	private int rank;

	KeyBindingDefinition(String commandId, String contextId, String keyConfigurationId, KeySequence keySequence, String locale, String platform, String pluginId, int rank) {
		super();
		
		if (commandId == null || contextId == null || keyConfigurationId == null || keySequence == null || locale == null || platform == null || rank < 0)
			throw new NullPointerException();
		
		this.commandId = commandId;
		this.contextId = contextId;
		this.keyConfigurationId = keyConfigurationId;
		this.keySequence = keySequence;
		this.locale = locale;
		this.platform = platform;
		this.pluginId = pluginId;
		this.rank = rank;
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
			
							if (compareTo == 0) {
								compareTo = Util.compare(pluginId, keyBindingDefinition.pluginId);
							
								if (compareTo == 0)
									compareTo = rank - keyBindingDefinition.rank;	
							}								
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
		return commandId.equals(keyBindingDefinition.commandId) && contextId.equals(keyBindingDefinition.contextId) && keyConfigurationId.equals(keyBindingDefinition.keyConfigurationId) && keySequence.equals(keyBindingDefinition.keySequence) && locale.equals(keyBindingDefinition.locale) && platform.equals(keyBindingDefinition.platform) && Util.equals(pluginId, keyBindingDefinition.pluginId) && rank == keyBindingDefinition.rank;
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
	
	public int getRank() {
		return rank;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + commandId.hashCode();
		result = result * HASH_FACTOR + contextId.hashCode();
		result = result * HASH_FACTOR + keyConfigurationId.hashCode();
		result = result * HASH_FACTOR + keySequence.hashCode();
		result = result * HASH_FACTOR + locale.hashCode();
		result = result * HASH_FACTOR + platform.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(pluginId);
		result = result * HASH_FACTOR + rank;		
		return result;
	}

	public String toString() {
		return '[' + commandId + ',' + contextId + ',' + keyConfigurationId + ',' + keySequence + ',' + locale + ',' + platform + ',' + pluginId + ',' + rank + ']';
	}
}
