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

package org.eclipse.ui.internal.csm.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

final class CommandPatternBindingDefinition implements ICommandPatternBindingDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = CommandPatternBindingDefinition.class.getName().hashCode();

	static Map commandPatternBindingDefinitionsByCommandId(Collection commandPatternBindingDefinitions) {
		if (commandPatternBindingDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();			
		Iterator iterator = commandPatternBindingDefinitions.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ICommandPatternBindingDefinition.class);			
			ICommandPatternBindingDefinition commandPatternBindingDefinition = (ICommandPatternBindingDefinition) object;
			String commandId = commandPatternBindingDefinition.getCommandId();
			
			if (commandId != null) {
				Collection commandPatternBindingDefinitions2 = (Collection) map.get(commandId);
					
				if (commandPatternBindingDefinitions2 == null) {
					commandPatternBindingDefinitions2 = new ArrayList();
					map.put(commandId, commandPatternBindingDefinitions2);					
				}
	
				commandPatternBindingDefinitions2.add(commandPatternBindingDefinition);		
			}											
		}				
	
		return map;
	}	
	
	private String commandId;
	private boolean inclusive;
	private String pattern;
	private String pluginId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;

	CommandPatternBindingDefinition(String commandId, boolean inclusive, String pattern, String pluginId) {
		this.commandId = commandId;
		this.inclusive = inclusive;
		this.pattern = pattern;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		CommandPatternBindingDefinition commandPatternBindingDefinition = (CommandPatternBindingDefinition) object;
		int compareTo = Util.compare(commandId, commandPatternBindingDefinition.commandId);

		if (compareTo == 0) {		
			compareTo = Util.compare(inclusive, commandPatternBindingDefinition.inclusive);			
		
			if (compareTo == 0) {		
				compareTo = Util.compare(pattern, commandPatternBindingDefinition.pattern);				
		
				if (compareTo == 0)
					compareTo = Util.compare(pluginId, commandPatternBindingDefinition.pluginId);							
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof CommandPatternBindingDefinition))
			return false;

		CommandPatternBindingDefinition commandPatternBindingDefinition = (CommandPatternBindingDefinition) object;	
		boolean equals = true;
		equals &= Util.equals(commandId, commandPatternBindingDefinition.commandId);
		equals &= Util.equals(inclusive, commandPatternBindingDefinition.inclusive);
		equals &= Util.equals(pattern, commandPatternBindingDefinition.pattern);
		equals &= Util.equals(pluginId, commandPatternBindingDefinition.pluginId);
		return equals;
	}

	public String getCommandId() {
		return commandId;
	}

	public String getPattern() {
		return pattern;
	}	
	
	public String getPluginId() {
		return pluginId;
	}
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(commandId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(inclusive);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(pattern);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(pluginId);
			hashCodeComputed = true;
		}
			
		return hashCode;
	}

	public boolean isInclusive() {
		return inclusive;
	}	
	
	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(commandId);
			stringBuffer.append(',');
			stringBuffer.append(inclusive);
			stringBuffer.append(',');
			stringBuffer.append(pattern);
			stringBuffer.append(',');
			stringBuffer.append(pluginId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;
	}	
}
