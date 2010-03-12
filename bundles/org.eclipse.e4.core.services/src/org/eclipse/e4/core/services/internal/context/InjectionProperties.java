/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.context;

public class InjectionProperties {

	private boolean inject;
	private boolean optional;

	private String propertyToInject;
	private Object provider; // <= shouldn't this be Provider<T>?
	private Class qualifier;
	private String handlesEvent;
	private boolean eventHeadless;
	private boolean groupUpdates = false;

	public InjectionProperties(boolean inject, String propertyToInject, boolean optional) {
		super();
		this.inject = inject;
		this.propertyToInject = propertyToInject;
		this.optional = optional;
	}

	public String getPropertyName() {
		return propertyToInject;
	}

	public boolean isOptional() {
		return optional;
	}

	public boolean shouldInject() {
		return inject;
	}

	public void setInject(boolean inject) {
		this.inject = inject;
	}

	public void setProvider(Object provider) {
		this.provider = provider;
	}

	public Object getProvider() {
		return provider;
	}

	public void setQualifier(Class qualifier) {
		this.qualifier = qualifier;
	}

	public Class getQualifier() {
		return qualifier;
	}

	public void setHandlesEvent(String handlesEvent) {
		this.handlesEvent = handlesEvent;
	}

	public String getHandlesEvent() {
		return handlesEvent;
	}

	public void setEventHeadless(boolean eventHeadless) {
		this.eventHeadless = eventHeadless;
	}

	public boolean getEventHeadless() {
		return eventHeadless;
	}

	public void setGroupUpdates(boolean groupUpdates) {
		this.groupUpdates = groupUpdates;
	}

	public boolean groupUpdates() {
		return groupUpdates;
	}

}
