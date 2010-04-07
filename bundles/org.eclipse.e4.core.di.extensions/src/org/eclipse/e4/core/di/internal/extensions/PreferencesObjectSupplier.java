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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.e4.core.di.AbstractObjectSupplier;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.IObjectDescriptor;
import org.eclipse.e4.core.di.IRequestor;
import org.eclipse.e4.core.di.extensions.Preferences;
import org.osgi.framework.FrameworkUtil;

public class PreferencesObjectSupplier extends AbstractObjectSupplier {

	private Map<String, List<IRequestor>> listenerCache = new HashMap<String, List<IRequestor>>();

	public PreferencesObjectSupplier() {
		// placeholder
	}

	@Override
	public Object get(IObjectDescriptor descriptor, final IRequestor requestor) {
		if (descriptor == null)
			return null;
		String key = getKey(descriptor);
		String nodePath = getNodePath(descriptor, requestor.getRequestingObject());
		if (key == null || nodePath == null || key.length() == 0 || nodePath.length() == 0)
			return IInjector.NOT_A_VALUE;

		addListener(nodePath, requestor);
		// TBD add auto-conversion depending on the descriptor's desired type
		Object result = getPreferencesService().getString(nodePath, key, null, null);
		return result;
	}

	@Override
	public Object[] get(IObjectDescriptor[] descriptors, IRequestor requestor) {
		Object[] result = new Object[descriptors.length];
		for (int i = 0; i < descriptors.length; i++) {
			result[i] = get(descriptors[i], requestor);
		}
		return result;
	}

	private String getKey(IObjectDescriptor descriptor) {
		if (descriptor == null)
			return null;
		Object qualifier = descriptor.getQualifier(Preferences.class);
		return ((Preferences) qualifier).value();
	}

	private String getNodePath(IObjectDescriptor descriptor, Object requestingObject) {
		if (descriptor == null)
			return null;
		Object qualifier = descriptor.getQualifier(Preferences.class);
		String nodePath = ((Preferences) qualifier).nodePath();

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
		if (requestor == null || !requestor.shouldTrack())
			return;
		synchronized (listenerCache) {
			if (listenerCache.containsKey(nodePath)) {
				for (IRequestor previousRequestor : listenerCache.get(nodePath)) {
					if (previousRequestor.equals(requestor))
						return; // avoid adding duplicate listeners
				}
			}
		}
		IEclipsePreferences node = new InstanceScope().getNode(nodePath);
		node.addPreferenceChangeListener(new IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				IInjector requestorInjector = requestor.getInjector();
				if (requestorInjector != null)
					requestorInjector.update(new IRequestor[] { requestor }, requestor
							.getPrimarySupplier());
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
