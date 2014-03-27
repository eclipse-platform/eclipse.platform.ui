/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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

    @Override
	public IMemento createChild(String type) {
        return null;
    }

    @Override
	public IMemento createChild(String type, String id) {
        return null;
    }

    @Override
	public IMemento getChild(String type) {
        IConfigurationElement[] configurationElements = configurationElement
                .getChildren(type);

        if (configurationElements != null && configurationElements.length >= 1) {
			return new ConfigurationElementMemento(configurationElements[0]);
		}

        return null;
    }

	@Override
	public IMemento[] getChildren() {
		IConfigurationElement[] configurationElements = configurationElement.getChildren();

		return getMementoArray(configurationElements);
	}

    @Override
	public IMemento[] getChildren(String type) {
        IConfigurationElement[] configurationElements = configurationElement
                .getChildren(type);

        return getMementoArray(configurationElements);
    }

	private IMemento[] getMementoArray(IConfigurationElement[] configurationElements) {
		if (configurationElements != null && configurationElements.length > 0) {
            IMemento mementos[] = new ConfigurationElementMemento[configurationElements.length];

            for (int i = 0; i < configurationElements.length; i++) {
				mementos[i] = new ConfigurationElementMemento(
                        configurationElements[i]);
			}

            return mementos;
        }

        return new IMemento[0];
	}

    @Override
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

    @Override
	public String getType() {
        return configurationElement.getName();
    }

    @Override
	public String getID() {
        return configurationElement.getAttribute(TAG_ID);
    }

    @Override
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

    @Override
	public String getString(String key) {
        return configurationElement.getAttribute(key);
    }

    @Override
	public Boolean getBoolean(String key) {
        String string = configurationElement.getAttribute(key);
        if (string==null) {
        	return null;
        }
        return Boolean.valueOf(string);
    }

    @Override
	public String getTextData() {
        return configurationElement.getValue();
    }
    
    @Override
	public String[] getAttributeKeys() {
    	return configurationElement.getAttributeNames();
    }

    @Override
	public void putFloat(String key, float value) {
    }

    @Override
	public void putInteger(String key, int value) {
    }

    @Override
	public void putMemento(IMemento memento) {
    }

    @Override
	public void putString(String key, String value) {
    }
    
    @Override
	public void putBoolean(String key, boolean value) {
    }

    @Override
	public void putTextData(String data) {
    }
    
    public String getContributorName() {
    	return configurationElement.getContributor().getName();
    }
    
    public String getExtensionID() {
    	return configurationElement.getDeclaringExtension().getUniqueIdentifier();
    }
}
