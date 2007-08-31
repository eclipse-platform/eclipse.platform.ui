/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.util;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IMemento;

public final class ConfigurationElementMemento implements IMemento {

    private IConfigurationElement configurationElement;

    public ConfigurationElementMemento(
            IConfigurationElement configurationElement) {
        if (configurationElement == null) {
			throw new NullPointerException();
		}

        this.configurationElement = configurationElement;
    }

    public IMemento createChild(String type) {
        return null;
    }

    public IMemento createChild(String type, String id) {
        return null;
    }

    public IMemento getChild(String type) {
        IConfigurationElement[] configurationElements = configurationElement
                .getChildren(type);

        if (configurationElements != null && configurationElements.length >= 1) {
			return new ConfigurationElementMemento(configurationElements[0]);
		}

        return null;
    }

    public IMemento[] getChildren(String type) {
        IConfigurationElement[] configurationElements = configurationElement
                .getChildren(type);

        if (configurationElements != null && configurationElements.length >= 1) {
            IMemento mementos[] = new ConfigurationElementMemento[configurationElements.length];

            for (int i = 0; i < configurationElements.length; i++) {
				mementos[i] = new ConfigurationElementMemento(
                        configurationElements[i]);
			}

            return mementos;
        }

        return new IMemento[0];
    }

    public Float getFloat(String key) {
        String string = configurationElement.getAttribute(key);

        if (string != null) {
			try {
                return new Float(string);
            } catch (NumberFormatException eNumberFormat) {
            }
		}

        return null;
    }

    public String getType() {
        return configurationElement.getName();
    }

    public String getID() {
        return configurationElement.getAttribute(TAG_ID);
    }

    public Integer getInteger(String key) {
        String string = configurationElement.getAttribute(key);

        if (string != null) {
			try {
                return new Integer(string);
            } catch (NumberFormatException eNumberFormat) {
            }
		}

        return null;
    }

    public String getString(String key) {
        return configurationElement.getAttribute(key);
    }

    public Boolean getBoolean(String key) {
        String string = configurationElement.getAttribute(key);
        if (string==null) {
        	return null;
        }
        return Boolean.valueOf(string);
    }

    public String getTextData() {
        return configurationElement.getValue();
    }
    
    public String[] getAttributeKeys() {
    	return configurationElement.getAttributeNames();
    }

    public void putFloat(String key, float value) {
    }

    public void putInteger(String key, int value) {
    }

    public void putMemento(IMemento memento) {
    }

    public void putString(String key, String value) {
    }
    
    public void putBoolean(String key, boolean value) {
    }

    public void putTextData(String data) {
    }
}
