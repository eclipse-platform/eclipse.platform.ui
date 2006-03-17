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

package org.eclipse.jface.internal.databinding.internal.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservableCollection;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.MappingDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.ObservableMultiMappingWithDomain;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.Policy;

/**
 * @since 1.0
 * 
 */
public class JavaBeanObservableMultiMapping extends
		ObservableMultiMappingWithDomain {

	private final PropertyDescriptor[] propertyDescriptors;

	private PropertyChangeListener elementListener = new PropertyChangeListener() {
		public void propertyChange(final java.beans.PropertyChangeEvent event) {
			if (!updating) {
				for (int i = 0; i < propertyDescriptors.length; i++) {
					if (propertyDescriptors[i].getName().equals(
							event.getPropertyName())) {
						final int[] indices = new int[] { i };
						final Set elements = Collections.singleton(event
								.getSource());
						fireMappingValueChange(new MappingDiff() {
							public Set getElements() {
								return elements;
							}

							public int[] getAffectedIndices() {
								return indices;
							}

							public Object[] getOldMappingValues(Object element,
									int[] indices) {
								return new Object[] { event.getOldValue() };
							}

							public Object[] getNewMappingValues(Object element,
									int[] indices) {
								return new Object[] { event.getNewValue() };
							}
						});
					}
				}
			}
		}
	};

	private ListenerSupport listenerSupport = new ListenerSupport(
			elementListener);

	private boolean updating = false;

	/**
	 * @param domain
	 * @param propertyDescriptors
	 */
	public JavaBeanObservableMultiMapping(IObservableCollection domain,
			PropertyDescriptor[] propertyDescriptors) {
		this.propertyDescriptors = propertyDescriptors;
		for (int i = 0; i < propertyDescriptors.length; i++) {
			Assert.isTrue(propertyDescriptors[i]!=null);
		}
		initDomain(domain);
	}

	protected void addListenerTo(Object domainElement) {
		listenerSupport.hookListener(domainElement);
	}

	protected void removeListenerFrom(Object domainElement) {
		listenerSupport.unhookListener(domainElement);
	}

	private Object doGetMappingValue(Object element,
			PropertyDescriptor propertyDescriptor) {
		try {
			return propertyDescriptor.getReadMethod().invoke(element,
					new Object[0]);
		} catch (Exception e) {
			Policy.getLog().log(
					new Status(IStatus.ERROR, Policy.JFACE, IStatus.ERROR,
							"cannot get value", e)); //$NON-NLS-1$
		}
		return null;
	}

	public void setMappingValues(Object element, int[] indices, Object[] values) {
		for (int i = 0; i < indices.length; i++) {
			setMappingValue(element, propertyDescriptors[indices[i]], values[i]);
		}
	}

	private void setMappingValue(Object element,
			PropertyDescriptor propertyDescriptor, Object value) {
		try {
			propertyDescriptor.getWriteMethod().invoke(element,
					new Object[] { value });
		} catch (Exception e) {
			Policy.getLog().log(
					new Status(IStatus.ERROR, Policy.JFACE, IStatus.ERROR,
							"cannot set value", e)); //$NON-NLS-1$
		}
	}

	protected Object[] doGetMappingValues(Object element, int[] indices) {
		Object[] result = new Object[indices.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = doGetMappingValue(element,
					propertyDescriptors[indices[i]]);
		}
		return result;
	}

	public Object[] getValueTypes() {
		Class[] result = new Class[propertyDescriptors.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = propertyDescriptors[i].getPropertyType();
		}
		return result;
	}

}
