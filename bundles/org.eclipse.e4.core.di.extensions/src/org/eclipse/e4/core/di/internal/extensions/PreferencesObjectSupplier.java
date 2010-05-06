/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
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

	private Map<String, List<IRequestor>> listenerCache = new HashMap<String, List<IRequestor>>();

	public PreferencesObjectSupplier() {
		// placeholder
	}

	@Override
	public Object get(IObjectDescriptor descriptor, IRequestor requestor, boolean track, boolean group) {
		if (descriptor == null)
			return null;
		String key = getKey(descriptor);
		String nodePath = getNodePath(descriptor, requestor.getRequestingObject());
		if (key == null || nodePath == null || key.length() == 0 || nodePath.length() == 0)
			return IInjector.NOT_A_VALUE;

		if (track)
			addListener(nodePath, requestor);

		Class<?> descriptorsClass = getDesiredClass(descriptor.getDesiredType());
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

	private String getNodePath(IObjectDescriptor descriptor, Object requestingObject) {
		if (descriptor == null)
			return null;
		Preference qualifier = descriptor.getQualifier(Preference.class);
		String nodePath = qualifier.nodePath();

		if (nodePath == null || nodePath.length() == 0) {
			if (requestingObject == null)
				return null;
			nodePath = FrameworkUtil.getBundle(requestingObject.getClass()).getSymbolicName();
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
				for (IRequestor previousRequestor : listenerCache.get(nodePath)) {
					if (previousRequestor.equals(requestor))
						return; // avoid adding duplicate listeners
				}
			}
		}
		final IEclipsePreferences node = new InstanceScope().getNode(nodePath);
		node.addPreferenceChangeListener(new IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				if (!requestor.isValid()) {
					node.removePreferenceChangeListener(this);
					return;
				}

				requestor.resolveArguments();
				requestor.execute();
			}
		});
		synchronized (listenerCache) {
			if (listenerCache.containsKey(nodePath))
				listenerCache.get(nodePath).add(requestor);
			else {
				List<IRequestor> listeningRequestors = new ArrayList<IRequestor>();
				listeningRequestors.add(requestor);
				listenerCache.put(nodePath, listeningRequestors);
			}
		}
	}

}
