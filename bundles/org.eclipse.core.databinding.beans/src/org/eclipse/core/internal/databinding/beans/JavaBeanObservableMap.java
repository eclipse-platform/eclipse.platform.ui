/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 171616
 *     Matthew hall - bugs 223164, 241585, 226289, 246103
 *******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.map.ComputedObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.internal.databinding.Util;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 1.0
 * 
 */
public class JavaBeanObservableMap extends ComputedObservableMap implements
		IBeanObservable {

	private PropertyDescriptor propertyDescriptor;
	
	private PropertyChangeListener elementListener = new PropertyChangeListener() {
		public void propertyChange(final java.beans.PropertyChangeEvent event) {
			if (!updating) {
				getRealm().exec(new Runnable() {
					public void run() {
						Object source = event.getSource();
						Object oldValue = event.getOldValue();
						Object newValue = event.getNewValue();
						if (oldValue == null && newValue == null) {
							oldValue = cachedValues.get(new IdentityWrapper(
									source));
							newValue = doGet(source);
						}
						cachedValues.put(new IdentityWrapper(source), newValue);
						if (!Util.equals(oldValue, newValue)) {
							fireMapChange(Diffs.createMapDiffSingleChange(
									source, oldValue, newValue));
						}
					}
				});
			}
		}
	};

	private ListenerSupport listenerSupport;

	private boolean updating = false;

	private boolean attachListeners;

	// Applicable only while hasListeners() == true
	private Map cachedValues;

	/**
	 * @param domain
	 * @param propertyDescriptor
	 */
	public JavaBeanObservableMap(IObservableSet domain,
			PropertyDescriptor propertyDescriptor) {
		this(domain, propertyDescriptor, true);
	}

	/**
	 * @param domain
	 * @param propertyDescriptor
	 * @param attachListeners
	 */
	public JavaBeanObservableMap(IObservableSet domain,
			PropertyDescriptor propertyDescriptor, boolean attachListeners) {
		super(domain, propertyDescriptor.getPropertyType());

		this.propertyDescriptor = propertyDescriptor;
		this.attachListeners = attachListeners;
		if (attachListeners) {
			this.listenerSupport = new ListenerSupport(elementListener,
					propertyDescriptor.getName());
		}
	}

	protected void firstListenerAdded() {
		if (attachListeners) {
			cachedValues = new HashMap();
		}
		super.firstListenerAdded();
	}

	protected void lastListenerRemoved() {
		super.lastListenerRemoved();
		if (attachListeners) {
			cachedValues = null;
		}
	}

	protected void hookListener(Object domainElement) {
		if (attachListeners && domainElement != null) {
			listenerSupport.hookListener(domainElement);
			cachedValues.put(new IdentityWrapper(domainElement),
					doGet(domainElement));
		}
	}

	protected void unhookListener(Object domainElement) {
		if (attachListeners && domainElement != null) {
			cachedValues.remove(new IdentityWrapper(domainElement));
			listenerSupport.unhookListener(domainElement);
		}
	}

	protected Object doGet(Object key) {
		if (key == null) {
			return null;
		}
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
			if (!Util.equals(oldValue, value)) {
				Method writeMethod = propertyDescriptor.getWriteMethod();
				if (!writeMethod.isAccessible()) {
					writeMethod.setAccessible(true);
				}
				writeMethod.invoke(key, new Object[] { value });
			}

			if (hasListeners()) {
				// oldValue contains the live value which may be different from
				// the cached value if the bean does not have listener API or
				// does not fire events properly. For consistency we want to
				// provide the cached value as the old value, rather than the
				// live value so that consumers that hook/unhook listeners can
				// do so without maintaining caches of their own.
				Object newValue = doGet(key);
				oldValue = cachedValues.put(new IdentityWrapper(key), newValue);

				if (!Util.equals(oldValue, newValue)) {
					fireSingleChange(key, oldValue, newValue);
				}
			}
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
