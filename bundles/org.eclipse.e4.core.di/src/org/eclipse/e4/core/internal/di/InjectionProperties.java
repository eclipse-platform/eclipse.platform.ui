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
package org.eclipse.e4.core.internal.di;

import java.lang.annotation.Annotation;

import org.eclipse.e4.core.di.annotations.Optional;

public class InjectionProperties {

	private boolean inject;

	private String propertyToInject;
	private Object provider; // <= shouldn't this be Provider<T>?
	private Annotation[] qualifiers;
	private String handlesEvent;
	private boolean eventHeadless;
	private boolean groupUpdates = false;

	public InjectionProperties(boolean inject, String propertyToInject) {
		super();
		this.inject = inject;
		this.propertyToInject = propertyToInject;
	}

	public String getPropertyName() {
		return propertyToInject;
	}

	public boolean isOptional() {
		return hasQualifier(Optional.class);
	}
	
	private boolean hasQualifier(Class<? extends Annotation> clazz) {
		if (clazz == null)
			return false;
		if (qualifiers == null)
			return false;
		for(Annotation annotation : qualifiers) {
			if (annotation.annotationType().equals(clazz))
				return true;
		}
		return false;
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

	public void setQualifiers(Annotation[] qualifiers) {
		this.qualifiers = qualifiers;
	}

	public Annotation[] getQualifiers() {
		return qualifiers;
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
