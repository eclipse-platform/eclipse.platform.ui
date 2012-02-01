/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.di.internal.extensions;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.di.suppliers.ExtendedObjectSupplier;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.osgi.framework.FrameworkUtil;

/**
 * Note: we do not support byte arrays in preferences at this time.
 */
public class PreferencesObjectSupplier extends ExtendedObjectSupplier {

	static private class PrefInjectionListener implements IPreferenceChangeListener {

		final private IRequestor requestor;
		final private IEclipsePreferences node;

		public PrefInjectionListener(IEclipsePreferences node, IRequestor requestor) {
			this.node = node;
			this.requestor = requestor;
		}

		public void preferenceChange(PreferenceChangeEvent event) {
			if (!requestor.isValid()) {
				node.removePreferenceChangeListener(this);
				return;
			}
			requestor.resolveArguments(false);
			requestor.execute();
		}

		public IRequestor getRequestor() {
			return requestor;
		}

		public void stopListening() {
			node.removePreferenceChangeListener(this);
		}
	}

	private Map<String, List<PrefInjectionListener>> listenerCache = new HashMap<String, List<PrefInjectionListener>>();

	public PreferencesObjectSupplier() {
		DIEActivator.getDefault().registerPreferencesSupplier(this);
	}

	@Override
	public Object get(IObjectDescriptor descriptor, IRequestor requestor, boolean track, boolean group) {
		if (descriptor == null)
			return null;
		Class<?> descriptorsClass = getDesiredClass(descriptor.getDesiredType());
		String nodePath = getNodePath(descriptor, requestor.getRequestingObjectClass());
		if (IEclipsePreferences.class.equals(descriptorsClass)) {
			return InstanceScope.INSTANCE.getNode(nodePath);
		}

		String key = getKey(descriptor);
		if (key == null || nodePath == null || key.length() == 0 || nodePath.length() == 0)
			return IInjector.NOT_A_VALUE;
		if (track)
			addListener(nodePath, requestor);

		if (descriptorsClass.isPrimitive()) {
			if (descriptorsClass.equals(boolean.class))
				return getPreferencesService().getBoolean(nodePath, key, false, null);
			else if (descriptorsClass.equals(int.class))
				return getPreferencesService().getInt(nodePath, key, 0, null);
			else if (descriptorsClass.equals(double.class))
				return getPreferencesService().getDouble(nodePath, key, 0.0d, null);
			else if (descriptorsClass.equals(float.class))
				return getPreferencesService().getFloat(nodePath, key, 0.0f, null);
			else if (descriptorsClass.equals(long.class))
				return getPreferencesService().getLong(nodePath, key, 0L, null);
		}

		if (String.class.equals(descriptorsClass))
			return getPreferencesService().getString(nodePath, key, null, null);
		else if (Boolean.class.equals(descriptorsClass))
			return getPreferencesService().getBoolean(nodePath, key, false, null);
		else if (Boolean.class.equals(descriptorsClass))
			return getPreferencesService().getBoolean(nodePath, key, false, null);
		else if (Integer.class.equals(descriptorsClass))
			return getPreferencesService().getInt(nodePath, key, 0, null);
		else if (Double.class.equals(descriptorsClass))
			return getPreferencesService().getDouble(nodePath, key, 0.0d, null);
		else if (Float.class.equals(descriptorsClass))
			return getPreferencesService().getFloat(nodePath, key, 0.0f, null);
		else if (Long.class.equals(descriptorsClass))
			return getPreferencesService().getLong(nodePath, key, 0L, null);

		return getPreferencesService().getString(nodePath, key, null, null);
	}

	private Class<?> getDesiredClass(Type desiredType) {
		if (desiredType instanceof Class<?>)
			return (Class<?>) desiredType;
		if (desiredType instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType) desiredType).getRawType();
			if (rawType instanceof Class<?>)
				return (Class<?>) rawType;
		}
		return null;
	}

	private String getKey(IObjectDescriptor descriptor) {
		if (descriptor == null)
			return null;
		Preference qualifier = descriptor.getQualifier(Preference.class);
		return qualifier.value();
	}

	private String getNodePath(IObjectDescriptor descriptor, Class<?> requestingObject) {
		if (descriptor == null)
			return null;
		Preference qualifier = descriptor.getQualifier(Preference.class);
		String nodePath = qualifier.nodePath();

		if (nodePath == null || nodePath.length() == 0) {
			if (requestingObject == null)
				return null;
			nodePath = FrameworkUtil.getBundle(requestingObject).getSymbolicName();
		}
		return nodePath;
	}

	private IPreferencesService getPreferencesService() {
		return DIEActivator.getDefault().getPreferencesService();
	}

	private void addListener(String nodePath, final IRequestor requestor) {
		if (requestor == null)
			return;
		synchronized (listenerCache) {
			if (listenerCache.containsKey(nodePath)) {
				for (PrefInjectionListener listener : listenerCache.get(nodePath)) {
					IRequestor previousRequestor = listener.getRequestor();
					if (previousRequestor.equals(requestor))
						return; // avoid adding duplicate listeners
				}
			}
		}
		final IEclipsePreferences node = InstanceScope.INSTANCE.getNode(nodePath);
		PrefInjectionListener listener = new PrefInjectionListener(node, requestor);
		node.addPreferenceChangeListener(listener);

		synchronized (listenerCache) {
			if (listenerCache.containsKey(nodePath))
				listenerCache.get(nodePath).add(listener);
			else {
				List<PrefInjectionListener> listeningRequestors = new ArrayList<PrefInjectionListener>();
				listeningRequestors.add(listener);
				listenerCache.put(nodePath, listeningRequestors);
			}
		}
	}

	public void removeAllListeners() {
		synchronized (listenerCache) {
			for (List<PrefInjectionListener> listeners : listenerCache.values()) {
				if (listeners == null)
					continue;
				for (PrefInjectionListener listener : listeners) {
					listener.stopListening();
				}
			}
			listenerCache.clear();
		}
	}

}
