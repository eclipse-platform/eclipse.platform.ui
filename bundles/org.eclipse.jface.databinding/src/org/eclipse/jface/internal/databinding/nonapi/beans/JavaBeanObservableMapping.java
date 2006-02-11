/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.nonapi.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.internal.databinding.api.observable.mapping.IMappingDiff;
import org.eclipse.jface.internal.databinding.api.observable.mapping.ObservableMapping;
import org.eclipse.jface.internal.databinding.api.observable.set.IObservableSet;
import org.eclipse.jface.util.Policy;

/**
 * @since 3.2
 * 
 */
public class JavaBeanObservableMapping extends ObservableMapping {

	private final PropertyDescriptor propertyDescriptor;

	private PropertyChangeListener elementListener = new PropertyChangeListener() {
		public void propertyChange(final java.beans.PropertyChangeEvent event) {
			if (!updating) {
				if (propertyDescriptor.getName()
						.equals(event.getPropertyName())) {
					fireMappingValueChange(new IMappingDiff() {
						public Set getElements() {
							return Collections.singleton(event.getSource());
						}

						public Object getOldMappingValue(Object element) {
							return event.getOldValue();
						}

						public Object getNewMappingValue(Object element) {
							return event.getNewValue();
						}
					});
				}
			}
		}
	};

	private ListenerSupport listenerSupport = new ListenerSupport(
			elementListener);

	private boolean updating = false;

	/**
	 * @param domain
	 * @param propertyDescriptor
	 */
	public JavaBeanObservableMapping(IObservableSet domain,
			PropertyDescriptor propertyDescriptor) {
		this.propertyDescriptor = propertyDescriptor;
		initDomain(domain);
	}

	protected void addListenerTo(Object domainElement) {
		listenerSupport.hookListener(domainElement);
	}

	protected void removeListenerFrom(Object domainElement) {
		listenerSupport.unhookListener(domainElement);
	}

	protected Object doGetMappingValue(Object element) {
		Method getter;
		try {
			String propertyName = propertyDescriptor.getName();
			getter = element
					.getClass()
					.getMethod(
							"get"	+ propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1), new Class[0]); //$NON-NLS-1$
			return getter.invoke(element, new Object[0]);
		} catch (Exception e) {
			Policy.getLog().log(
					new Status(IStatus.ERROR, Policy.JFACE, IStatus.ERROR,
							"cannot get value", e)); //$NON-NLS-1$
		}
		return null;
	}

	public void setMappingValue(Object element, Object value) {
		Method setter;
		try {
			String propertyName = propertyDescriptor.getName();
			setter = element
					.getClass()
					.getMethod(
							"set"	+ propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1), new Class[] { propertyDescriptor.getPropertyType() }); //$NON-NLS-1$
			setter.invoke(element, new Object[] { value });
		} catch (Exception e) {
			Policy.getLog().log(
					new Status(IStatus.ERROR, Policy.JFACE, IStatus.ERROR,
							"cannot set value", e)); //$NON-NLS-1$
		}
	}

}
