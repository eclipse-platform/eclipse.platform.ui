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

import org.eclipse.ui.commands.IKeyBinding;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

final class KeyBinding implements Comparable, IKeyBinding {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = KeyBinding.class.getName().hashCode();

	private String commandId;
	private String contextId;
	private String keyConfigurationId;
	private KeySequence keySequence;	
	private String locale;
	private String platform;
	private String pluginId;
	private int rank;

	KeyBinding(String commandId, String contextId, String keyConfigurationId, KeySequence keySequence, String locale, String platform, String pluginId, int rank) {
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
		KeyBinding keyBinding = (KeyBinding) object;
		int compareTo = commandId.compareTo(keyBinding.commandId);
		
		if (compareTo == 0) {		
			compareTo = contextId.compareTo(keyBinding.contextId);			

			if (compareTo == 0) {		
				compareTo = keyConfigurationId.compareTo(keyBinding.keyConfigurationId);			

				if (compareTo == 0) {
					compareTo = keySequence.compareTo(keyBinding.keySequence);

					if (compareTo == 0) {		
						compareTo = locale.compareTo(keyBinding.locale);			
	
						if (compareTo == 0) {		
							compareTo = platform.compareTo(keyBinding.platform);			
			
							if (compareTo == 0) {
								compareTo = Util.compare(pluginId, keyBinding.pluginId);
							
								if (compareTo == 0)
									compareTo = rank - keyBinding.rank;	
							}								
						}
					}
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof KeyBinding))
			return false;

		KeyBinding keyBinding = (KeyBinding) object;	
		return commandId.equals(keyBinding.commandId) && contextId.equals(keyBinding.contextId) && keyConfigurationId.equals(keyBinding.keyConfigurationId) && keySequence.equals(keyBinding.keySequence) && locale.equals(keyBinding.locale) && platform.equals(keyBinding.platform) && Util.equals(pluginId, keyBinding.pluginId) && rank == keyBinding.rank;
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
