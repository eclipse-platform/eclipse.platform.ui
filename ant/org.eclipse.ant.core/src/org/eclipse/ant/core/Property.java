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
package org.eclipse.ant.core;



/**
 * Represents a Ant property.
 * @since 2.1
 */
public class Property {

	private String name;
	private String value;
	private IAntPropertyValueProvider valueProvider;
	private String pluginLabel;

	public Property(String name, String value) {
		this.name= name;
		this.value= value;
	}

	public Property() {
	}
	/**
	 * Gets the name
	 * @return Returns a String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name= name;
	}
	
	/*
	 * @see Object#equals()
	 */	
	public boolean equals(Object other) {
		if (other.getClass().equals(getClass())) {
			Property elem= (Property)other;
			return name.equals(elem.getName());
		}
		return false;
	}
	
	/*
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return name.hashCode();
	}	
	/**
	 * Returns the value.
	 * @return String
	 */
	public String getValue() {
		if (valueProvider != null) {
			return valueProvider.getAntPropertyValue(name);
		}
		
		return value;
	}

	/**
	 * Sets the value.
	 * @param value The value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Returns whether this Ant property has been created because of an extension
	 * point definition.
	 * 
	 * @return boolean
	 * @since 3.0
	 */
	public boolean isDefault() {
		return pluginLabel != null;
	}

	/**
	 * Sets the label of the plugin that contributed this Ant property via an extension
	 * point.
	 * 
	 * @param pluginLabel The label of the plugin
	 * @since 3.0
	 */
	public void setPluginLabel(String pluginLabel) {
		this.pluginLabel = pluginLabel;
	}
	
	/**
	 * Returns the label of the plugin that contributed this Ant property via an extension
	 * point.
	 * 
	 * @return pluginLabel The label of the plugin
	 * @since 3.0
	 */
	public String getPluginLabel() {
		return this.pluginLabel;
	}
	
	/**
	 * Sets the <code>IAntPropertyValueProvider</code> to be used to dynamically provide a 
	 * value for this property.
	 * 
	 * @param valueProvider The value provider to use to resolve the value of this property
	 * @since 3.0
	 */
	public void setValueProvider(IAntPropertyValueProvider valueProvider) {
		this.valueProvider= valueProvider;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buff= new StringBuffer("\""); //$NON-NLS-1$
		buff.append(getName());
		buff.append("\"= \""); //$NON-NLS-1$
		buff.append(getValue());
		buff.append("\""); //$NON-NLS-1$
		return buff.toString();
	}
}
