/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 171616
 ******************************************************************************/

package org.eclipse.core.internal.databinding.internal.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.map.ComputedObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 1.0
 * 
 */
public class JavaBeanObservableMap extends ComputedObservableMap implements IBeanObservable {

	private PropertyDescriptor propertyDescriptor;
	
	private PropertyChangeListener elementListener = new PropertyChangeListener() {
		public void propertyChange(final java.beans.PropertyChangeEvent event) {
			if (!updating) {
				if (propertyDescriptor.getName()
						.equals(event.getPropertyName())) {
					fireMapChange(Diffs.createMapDiffSingleChange(event
							.getSource(), event.getOldValue(), event
							.getNewValue()));
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
	public JavaBeanObservableMap(IObservableSet domain,
			PropertyDescriptor propertyDescriptor) {
		super(domain);
		
		this.propertyDescriptor = propertyDescriptor;
		init();
	}

	protected void hookListener(Object domainElement) {
		listenerSupport.hookListener(domainElement);
	}

	protected void unhookListener(Object domainElement) {
		listenerSupport.unhookListener(domainElement);
	}

	protected Object doGet(Object key) {
		try {
			Method readMethod = propertyDescriptor.getReadMethod();
			if (!readMethod.isAccessible()) {
				readMethod.setAccessible(true);
			}
			return readMethod.invoke(key, new Object[0]);
		} catch (Exception e) {
			Policy.getLog().log(
					new Status(IStatus.ERROR, Policy.JFACE_DATABINDING,
							IStatus.ERROR, "cannot get value", e)); //$NON-NLS-1$
			throw new RuntimeException(e);
		}
	}

	protected Object doPut(Object key, Object value) {
		try {
			Object oldValue = get(key);
			propertyDescriptor.getWriteMethod().invoke(key,
					new Object[] { value });
			keySet().add(key);
			return oldValue;
		} catch (Exception e) {
			Policy.getLog().log(
					new Status(IStatus.ERROR, Policy.JFACE_DATABINDING,
							IStatus.ERROR, "cannot set value", e)); //$NON-NLS-1$
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.databinding.beans.IBeanObservable#getObserved()
	 */
	public Object getObserved() {
		return keySet();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.databinding.beans.IBeanObservable#getPropertyDescriptor()
	 */
	public PropertyDescriptor getPropertyDescriptor() {
		return propertyDescriptor;
	}
}
