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

import org.eclipse.e4.core.services.injector.IObjectDescriptor;

// TBD separate InjectionProperties into 2 classes: what we need for the key
// and the extra info for injection.
public class InjectionProperties implements IObjectDescriptor {

	private boolean inject;
	private boolean optional;

	private String propertyToInject;
	private Object provider; // <= shouldn't this be IObjectProvider?
	private Class qualifier;
	private Class elementClass;
	private String handlesEvent;
	private boolean eventHeadless;
	private boolean groupUpdates = false;

	public InjectionProperties(boolean inject, String propertyToInject, boolean optional,
			Class elementClass) {
		super();
		this.inject = inject;
		this.propertyToInject = propertyToInject;
		this.optional = optional;
		this.elementClass = elementClass;
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

	public void setElementClass(Class elementClass) {
		this.elementClass = elementClass;
	}

	public Class getElementClass() {
		return elementClass;
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
