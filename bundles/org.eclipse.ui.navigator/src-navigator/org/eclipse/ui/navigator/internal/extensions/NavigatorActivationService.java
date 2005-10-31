/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.extensions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ui.navigator.NavigatorContentService;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorActivationService {

	private static final NavigatorActivationService INSTANCE = new NavigatorActivationService();
	private static final NavigatorContentDescriptorRegistry CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorRegistry.getInstance();
	private static final String ACTIVATED_EXTENSIONS = "activatedExtensions"; //$NON-NLS-1$

	/*
	 * Map of activated extensions stored by the Viewer ID as a Set. That is, (ViewerID, Set of
	 * Extension Activations)-pairs.
	 */
	private final Map activatedExtensionsMap;
	private final Map contentServiceListenersMap;

	/**
	 *  
	 */
	private NavigatorActivationService() {
		super();
		activatedExtensionsMap = new HashMap();
		contentServiceListenersMap = new HashMap();
	}

	/**
	 *  
	 */
	public static NavigatorActivationService getInstance() {
		return INSTANCE;
	}

	/*
	 * 
	 * (non-Javadoc) @param navigatorExtensionId The Unique identifier associated with a given
	 * NavigatorContentDescriptor
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.INavigatorExtensionSite#isNavigatorExtensionEnabled(org.eclipse.wst.common.navigator.internal.views.navigator.INavigatorContentExtension)
	 */
	public boolean isNavigatorExtensionActive(String aViewerId, String aNavigatorExtensionId) {
		return getActiveExtensions(aViewerId, true).contains(getExtensionActivationPreferenceKey(aNavigatorExtensionId));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.INavigatorExtensionSite#enableNavigatorExtension(java.lang.String,
	 *      boolean)
	 */
	public void activateNavigatorExtension(String aViewerId, String aNavigatorExtensionId, boolean toEnable) {

		boolean currentlyActive = isNavigatorExtensionActive(aViewerId, aNavigatorExtensionId);
		if (currentlyActive == toEnable)
			return;

		Set activatedExtensions = getActiveExtensions(aViewerId, true);
		if (toEnable)
			activatedExtensions.add(getExtensionActivationPreferenceKey(aNavigatorExtensionId));
		else
			activatedExtensions.remove(getExtensionActivationPreferenceKey(aNavigatorExtensionId));
		notifyListeners(aViewerId, aNavigatorExtensionId, toEnable);

	}


	public void persistExtensionActivations(String aViewerId) {

		Set activatedExtensions = getActiveExtensions(aViewerId, false);
		if (activatedExtensions == null)
			return;

		Preferences preferences = NavigatorPlugin.getDefault().getPluginPreferences();

		synchronized (activatedExtensions) {
			Iterator activatedExtensionsIterator = activatedExtensions.iterator();
			/* ensure that the preference will be non-empty */
			StringBuffer activatedExtensionsStringBuffer = new StringBuffer(";"); //$NON-NLS-1$
			while (activatedExtensionsIterator.hasNext()) {
				activatedExtensionsStringBuffer.append(activatedExtensionsIterator.next()).append(";"); //$NON-NLS-1$
			}

			preferences.setValue(getPreferenceKey(aViewerId), activatedExtensionsStringBuffer.toString());  
		}
		NavigatorPlugin.getDefault().savePluginPreferences();
	}

	public void addExtensionActivationListener(String aViewerId, IExtensionActivationListener aListener) {
		synchronized (contentServiceListenersMap) {
			Set listeners = getExtensionActivationListeners(aViewerId, true);
			listeners.add(aListener);
		}
	}

	public void removeExtensionActivationListener(String aViewerId, IExtensionActivationListener aListener) {
		synchronized (contentServiceListenersMap) {
			Set listeners = getExtensionActivationListeners(aViewerId, true);
			listeners.remove(aListener);
		}
	}

	/**
	 * @param viewerId
	 * @param navigatorExtensionId
	 * @param toEnable
	 */
	private void notifyListeners(String aViewerId, String aNavigatorExtensionId, boolean toEnable) {
		synchronized (contentServiceListenersMap) {
			Set listeners = (Set) contentServiceListenersMap.get(aViewerId);
			for (Iterator iter = listeners.iterator(); iter.hasNext();) {
				NavigatorContentService element = (NavigatorContentService) iter.next();
				element.onExtensionActivation(aViewerId, aNavigatorExtensionId, toEnable);
			}
		}
	}

	private Set getExtensionActivationListeners(String aViewerId, boolean initializeIfNecessary) {
		Set listeners = (Set) contentServiceListenersMap.get(aViewerId);
		if (listeners != null || !initializeIfNecessary)
			return listeners;

		synchronized (contentServiceListenersMap) {
			listeners = (Set) contentServiceListenersMap.get(aViewerId);
			if (listeners == null)
				contentServiceListenersMap.put(aViewerId, (listeners = new HashSet()));
		}
		return listeners;
	}


	private Set getActiveExtensions(String aViewerId, boolean initializeIfNecessary) {
		/* enabled by default if no setting is present ... */
		Set activatedExtensions = (Set) activatedExtensionsMap.get(aViewerId);
		if (activatedExtensions != null || !initializeIfNecessary)
			return activatedExtensions;

		synchronized (activatedExtensionsMap) {
			activatedExtensions = (Set) activatedExtensionsMap.get(aViewerId);
			if (activatedExtensions == null) {
				activatedExtensions = revertExtensionActivations(aViewerId);
				activatedExtensionsMap.put(aViewerId, activatedExtensions);
			}
		}
		return activatedExtensions;
	}

	private Set revertExtensionActivations(String aViewerId) {

		Preferences preferences = NavigatorPlugin.getDefault().getPluginPreferences();

		Set activatedExtensions = new HashSet();
		String activatedExtensionsString = preferences.getString(getPreferenceKey(aViewerId));  

		if (activatedExtensionsString != null && activatedExtensionsString.length() > 0) {
			String activatedExtensionKey = null;
			StringTokenizer tokenizer = new StringTokenizer(activatedExtensionsString, ";"); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				activatedExtensionKey = tokenizer.nextToken();
				if (activatedExtensionKey.length() > 0)
					activatedExtensions.add(activatedExtensionKey);
			}
		} else {
			NavigatorContentDescriptor[] contentDescriptors = CONTENT_DESCRIPTOR_REGISTRY.getAllContentDescriptors();
			for (int i = 0; i < contentDescriptors.length; i++)
				if (contentDescriptors[i].isEnabledByDefault())
					activatedExtensions.add(getExtensionActivationPreferenceKey(contentDescriptors[i].getId()));
		}
		return activatedExtensions;
	}

	private String getPreferenceKey(String aViewerId) {
		return aViewerId + "." + ACTIVATED_EXTENSIONS; //$NON-NLS-1$
	}

	/**
	 * @param string
	 * @return
	 */
	private String getExtensionActivationPreferenceKey(String aNavigatorExtensionId) {
		return aNavigatorExtensionId + ".extensionActivated"; //$NON-NLS-1$  
	}

}