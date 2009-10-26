/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.context;

// TBD change default "optional" to true
public class InjectionProperties {

	private boolean inject;
	private String propertyToInject;
	private boolean optional;

	public InjectionProperties(Object[] properties) {
		super();
		this.inject = ((Boolean) properties[0]).booleanValue();
		this.propertyToInject = (String) properties[1];
		this.optional = ((Boolean) properties[2]).booleanValue();
	}

	public InjectionProperties(boolean inject, String propertyToInject, boolean optional) {
		super();
		this.inject = inject;
		this.propertyToInject = propertyToInject;
		this.optional = optional;
	}

	public String getPropertyName() {
		return propertyToInject;
	}

	public void setPropertyName(String propertyToInject) {
		this.propertyToInject = propertyToInject;
	}

	public boolean isOptional() {
		return optional;
	}

	public boolean shoudlInject() {
		return inject;
	}

	public void setInject(boolean inject) {
		this.inject = inject;
	}
}
