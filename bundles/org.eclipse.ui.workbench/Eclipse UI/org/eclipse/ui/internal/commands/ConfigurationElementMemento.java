/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IMemento;

final class ConfigurationElementMemento implements IMemento {

	static ConfigurationElementMemento create(IConfigurationElement configurationElement)
		throws IllegalArgumentException {
		return new ConfigurationElementMemento(configurationElement);
	}		

	private IConfigurationElement configurationElement;

	private ConfigurationElementMemento(IConfigurationElement configurationElement)
		throws IllegalArgumentException {
		super();
		
		if (configurationElement == null)
			throw new IllegalArgumentException();
		
		this.configurationElement = configurationElement;
	}

	public IMemento createChild(String type) {	
		return null;
	}

	public IMemento createChild(String type, String id) {
		return null;
	}

	public IMemento getChild(String type) {
		IConfigurationElement[] configurationElements = configurationElement.getChildren(type);
		
		if (configurationElements != null && configurationElements.length >= 1)
			return create(configurationElements[0]);
		
		return null;
	}

	public IMemento[] getChildren(String type) {
		IConfigurationElement[] configurationElements = configurationElement.getChildren(type);
		
		if (configurationElements != null && configurationElements.length >= 1) {
			IMemento mementos[] = new ConfigurationElementMemento[configurationElements.length];
			
			for (int i = 0; i < configurationElements.length; i++)
				mementos[i] = create(configurationElements[i]);
				
			return mementos;			
		}
		
		return new IMemento[0];
	}

	public Float getFloat(String key) {
		String string = configurationElement.getAttribute(key);
		
		if (string != null)
			try {
				return new Float(string);
			} catch (NumberFormatException eNumberFormat) {
			}
		
		return null;
	}

	public String getID() {
		return configurationElement.getAttribute(TAG_ID);
	}

	public Integer getInteger(String key) {
		String string = configurationElement.getAttribute(key);
		
		if (string != null)
			try {
				return new Integer(string);
			} catch (NumberFormatException eNumberFormat) {
			}
		
		return null;
	}

	public String getString(String key) {
		return configurationElement.getAttribute(key);
	}

	public String getTextData() {
		return configurationElement.getValue();
	}

	public void putFloat(String key, float value) {
	}

	public void putInteger(String key, int value) {
	}

	public void putMemento(IMemento memento) {
	}

	public void putString(String key, String value) {
	}

	public void putTextData(String data) {
	}
}
