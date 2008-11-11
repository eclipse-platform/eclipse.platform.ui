/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 221704)
 *     Matthew Hall - bug 223164, 226289, 244098, 246103
 *******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.BindingException;
import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.ObservableMap;
import org.eclipse.core.internal.databinding.Util;
import org.eclipse.core.runtime.Assert;

/**
 * @since 1.0
 * 
 */
public class JavaBeanPropertyObservableMap extends ObservableMap implements
		IBeanObservable {

	private final Object object;

	private Object keyType;
	private Object valueType;

	private boolean updating = false;

	private PropertyDescriptor descriptor;

	private ListenerSupport listenerSupport;

	/**
	 * @param realm
	 * @param object
	 * @param descriptor
	 * @param keyType
	 * @param valueType
	 */
	public JavaBeanPropertyObservableMap(Realm realm, Object object,
			PropertyDescriptor descriptor, Object keyType, Object valueType) {
		this(realm, object, descriptor, keyType, valueType, true);
	}

	/**
	 * @param realm
	 * @param object
	 * @param descriptor
	 * @param keyType
	 * @param valueType
	 * @param attachListeners
	 */
	public JavaBeanPropertyObservableMap(Realm realm, Object object,
			PropertyDescriptor descriptor, Object keyType, Object valueType,
			boolean attachListeners) {
		super(realm, new HashMap());
		this.object = object;
		this.descriptor = descriptor;
		this.keyType = keyType;
		this.valueType = valueType;
		if (attachListeners) {
			PropertyChangeListener listener = new PropertyChangeListener() {
				public void propertyChange(final PropertyChangeEvent event) {
					if (!updating) {
						getRealm().exec(new Runnable() {
							public void run() {
								Map oldMap = (Map) event.getOldValue();
								Map newMap = (Map) event.getNewValue();
								if (oldMap == null && newMap == null) {
									oldMap = wrappedMap;
									newMap = getMap();
								}

								if (!Util.equals(oldMap, newMap)) {
									wrappedMap = new HashMap(newMap);
									fireMapChange(Diffs.computeMapDiff(oldMap,
											newMap));
								}
							}
						});
					}
				}
			};

			listenerSupport = new ListenerSupport(listener,
					descriptor.getName());
			listenerSupport.hookListener(this.object);
		}

		wrappedMap.putAll(getMap());
	}

	public Object getKeyType() {
		return keyType;
	}

	public Object getValueType() {
		return valueType;
	}

	private Object primGetMap() {
		try {
			Method readMethod = descriptor.getReadMethod();
			if (!readMethod.isAccessible()) {
				readMethod.setAccessible(true);
			}
			return readMethod.invoke(object, new Object[0]);
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		Assert.isTrue(false, "Could not read collection values"); //$NON-NLS-1$
		return null;
	}

	private void primSetMap(Object newValue) {
		Exception ex = null;
		try {
			Method writeMethod = descriptor.getWriteMethod();
			if (!writeMethod.isAccessible()) {
				writeMethod.setAccessible(true);
			}
			writeMethod.invoke(object, new Object[] { newValue });
			return;
		} catch (IllegalArgumentException e) {
			ex = e;
		} catch (IllegalAccessException e) {
			ex = e;
		} catch (InvocationTargetException e) {
			ex = e;
		}
		throw new BindingException("Could not write collection values", ex); //$NON-NLS-1$
	}

	private Map getMap() {
		Map result = (Map) primGetMap();

		if (result == null)
			result = new HashMap();
		return result;
	}

	private void setMap() {
		primSetMap(new HashMap(wrappedMap));
	}

	public Object put(Object key, Object value) {
		checkRealm();
		updating = true;
		try {
			boolean add = !wrappedMap.containsKey(key);
			Object result = wrappedMap.put(key, value);
			if (!Util.equals(result, value)) {
				setMap();
				if (add) {
					fireMapChange(Diffs.createMapDiffSingleAdd(key, value));
				} else {
					fireMapChange(Diffs.createMapDiffSingleChange(key, result,
							value));
				}
			}
			return result;
		} finally {
			updating = false;
		}
	}

	public void putAll(Map map) {
		checkRealm();
		updating = true;
		try {
			Set addedKeys = new HashSet(map.size());
			Map changes = new HashMap(map.size());
			for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Entry) it.next();
				Object key = entry.getKey();
				Object newValue = entry.getValue();
				boolean add = !wrappedMap.containsKey(key);
				Object oldValue = wrappedMap.put(key, newValue);
				if (add) {
					addedKeys.add(key);
				} else if (!Util.equals(oldValue, newValue)) {
					changes.put(key, oldValue);
				}
			}
			if (!addedKeys.isEmpty() || !changes.isEmpty()) {
				setMap();
				fireMapChange(Diffs.createMapDiff(addedKeys,
						Collections.EMPTY_SET, changes.keySet(), changes,
						wrappedMap));
			}
		} finally {
			updating = false;
		}
	}

	public Object remove(Object key) {
		checkRealm();
		if (!wrappedMap.containsKey(key)) {
			return null;
		}
		updating = true;
		try {
			Object result = wrappedMap.remove(key);
			setMap();
			fireMapChange(Diffs.createMapDiffSingleRemove(key, result));
			return result;
		} finally {
			updating = false;
		}
	}

	public void clear() {
		checkRealm();
		if (wrappedMap.isEmpty())
			return;
		updating = true;
		try {
			Map oldMap = wrappedMap;
			wrappedMap = new HashMap();
			setMap();
			fireMapChange(Diffs.computeMapDiff(oldMap, Collections.EMPTY_MAP));
		} finally {
			updating = false;
		}
	}

	public Object getObserved() {
		return object;
	}

	public PropertyDescriptor getPropertyDescriptor() {
		return descriptor;
	}

	public synchronized void dispose() {
		if (listenerSupport != null) {
			listenerSupport.dispose();
			listenerSupport = null;
		}
		super.dispose();
	}
}
