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

import org.eclipse.ui.commands.GestureSequence;
import org.eclipse.ui.commands.IGestureBinding;
import org.eclipse.ui.internal.util.Util;

final class GestureBinding implements Comparable, IGestureBinding {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = GestureBinding.class.getName().hashCode();

	private String commandId;
	private String contextId;
	private String gestureConfigurationId;
	private GestureSequence gestureSequence;	
	private String locale;
	private String platform;
	private String pluginId;

	GestureBinding(String commandId, String contextId, String gestureConfigurationId, GestureSequence gestureSequence, String locale, String platform, String pluginId) {
		super();
		
		if (commandId == null || contextId == null || gestureConfigurationId == null || gestureSequence == null || locale == null || platform == null)
			throw new NullPointerException();
		
		this.commandId = commandId;
		this.contextId = contextId;
		this.gestureConfigurationId = gestureConfigurationId;
		this.gestureSequence = gestureSequence;
		this.locale = locale;
		this.platform = platform;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		GestureBinding gestureBinding = (GestureBinding) object;
		int compareTo = commandId.compareTo(gestureBinding.commandId);
		
		if (compareTo == 0) {		
			compareTo = contextId.compareTo(gestureBinding.contextId);			

			if (compareTo == 0) {		
				compareTo = gestureConfigurationId.compareTo(gestureBinding.gestureConfigurationId);			

				if (compareTo == 0) {
					// TODO		
					//compareTo = gestureSequence.compareTo(gestureBinding.gestureSequence);

					if (compareTo == 0) {		
						compareTo = locale.compareTo(gestureBinding.locale);			
	
						if (compareTo == 0) {		
							compareTo = platform.compareTo(gestureBinding.platform);			
			
							if (compareTo == 0)
								compareTo = Util.compare(pluginId, gestureBinding.pluginId);								
						}
					}
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof GestureBinding))
			return false;

		GestureBinding gestureBinding = (GestureBinding) object;	
		return commandId.equals(gestureBinding.commandId) && contextId.equals(gestureBinding.contextId) && gestureConfigurationId.equals(gestureBinding.gestureConfigurationId) && gestureSequence.equals(gestureBinding.gestureSequence) && locale.equals(gestureBinding.locale) && platform.equals(gestureBinding.platform) && Util.equals(pluginId, gestureBinding.pluginId);
	}

	public String getCommandId() {
		return commandId;
	}

	public String getContextId() {
		return contextId;
	}

	public String getGestureConfigurationId() {
		return gestureConfigurationId;
	}

	public GestureSequence getGestureSequence() {
		return gestureSequence;
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
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + commandId.hashCode();
		result = result * HASH_FACTOR + contextId.hashCode();
		result = result * HASH_FACTOR + gestureConfigurationId.hashCode();
		result = result * HASH_FACTOR + gestureSequence.hashCode();
		result = result * HASH_FACTOR + locale.hashCode();
		result = result * HASH_FACTOR + platform.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(pluginId);
		return result;
	}

	public String toString() {
		return '[' + commandId + ',' + contextId + ',' + gestureConfigurationId + ',' + gestureSequence + ',' + locale + ',' + platform + ',' + pluginId + ']';
	}
}
