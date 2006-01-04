/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptorManager;

/**
 * 
 * The activation service determines if an extension is 'active' within the
 * context of a given viewer. If an extension is 'active' then the extension
 * will contribute functionality to the viewer. If an extension is not 'active',
 * then the extension will not be given opportunities to contribute
 * functionality to the given viewer. See {@link INavigatorContentService} for
 * more detail on what 'states' are associated with a content extension.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public final class NavigatorActivationService {

	private static final String ACTIVATED_EXTENSIONS = ".activatedExtensions"; //$NON-NLS-1$

	private static final String EXTENSION_ACTIVATED_SUFFIX = ".extensionActivated"; //$NON-NLS-1$

	private static final NavigatorActivationService INSTANCE = new NavigatorActivationService();

	private static final NavigatorContentDescriptorManager CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorManager
			.getInstance();

	/*
	 * Map of activated extensions stored by the Viewer ID as a Set.
	 * 
	 * That is, (ViewerID, Set of Extension Activations)-pairs.
	 */
	private final Map activatedExtensionsMap = new HashMap();

	/*
	 * Map of (ViewerID, ListenerLists of IExtensionActivationListener)-pairs.
	 */
	private final Map contentServiceListenersMap = new HashMap();

	/**
	 * Create an instance of the service.
	 */
	private NavigatorActivationService() {
		super();
	}

	/**
	 * Returns the singleton instance of the known activations.
	 * 
	 * @return The single instance of the activation service.
	 */
	public static NavigatorActivationService getInstance() {
		return INSTANCE;
	}

	/**
	 * 
	 * Checks the known activation state for the given viewer id to determine if
	 * the given navigator extension is 'active'.
	 * 
	 * @param aViewerId
	 *            The unique identifier for a defined viewer that uses an
	 *            INavigatorContentService for its content.
	 * @param aNavigatorExtensionId
	 *            The unique identifier associated with a given extension.
	 * 
	 * @return True if the extension is active in the context of the viewer id.
	 */
	public boolean isNavigatorExtensionActive(String aViewerId,
			String aNavigatorExtensionId) {
		return getActiveExtensions(aViewerId, true).contains(
				getExtensionActivationPreferenceKey(aNavigatorExtensionId));
	}

	/**
	 * Set the activation state for the given extension in the context of the
	 * given viewer id. Each instance of an INavigatorContentService listens for
	 * the activation service to update; and if those instances were created
	 * with viewers, they will issue a refresh. Otherwise, clients are
	 * responsible for refreshing the viewers.
	 * 
	 * <p>
	 * Clients must call {@link #persistExtensionActivations(String)} to save
	 * the the activation state.
	 * </p>
	 * 
	 * <p>
	 * When clients are updating a batch of extensions, consider using
	 * {@link #activateNavigatorExtension(String, String[], boolean)} when
	 * possible to avoid unnecessary notifications.
	 * </p>
	 * 
	 * @param aViewerId
	 *            The unique identifier for a defined viewer that uses an
	 *            INavigatorContentService for its content.
	 * @param aNavigatorExtensionId
	 *            The unique identifier associated with a given extension.
	 * @param toEnable
	 *            True indicates the extension should be enabled; False
	 *            indicates otherwise.
	 * 
	 */
	public void activateNavigatorExtension(String aViewerId,
			String aNavigatorExtensionId, boolean toEnable) {

		boolean currentlyActive = isNavigatorExtensionActive(aViewerId,
				aNavigatorExtensionId);
		if (currentlyActive == toEnable)
			return;

		Set activatedExtensions = getActiveExtensions(aViewerId, true);
		if (toEnable)
			activatedExtensions
					.add(getExtensionActivationPreferenceKey(aNavigatorExtensionId));
		else
			activatedExtensions
					.remove(getExtensionActivationPreferenceKey(aNavigatorExtensionId));
		notifyListeners(aViewerId, new String[] { aNavigatorExtensionId },
				toEnable);

	}

	/**
	 * Set the activation state for the given extension in the context of the
	 * given viewer id. Each instance of an INavigatorContentService listens for
	 * the activation service to update; and if those instances were created
	 * with viewers, they will issue a refresh. Otherwise, clients are
	 * responsible for refreshing the viewers.
	 * 
	 * <p>
	 * Clients must call {@link #persistExtensionActivations(String)} to save
	 * the the activation state.
	 * </p>
	 * 
	 * @param aViewerId
	 *            The unique identifier for a defined viewer that uses an
	 *            INavigatorContentService for its content.
	 * @param aNavigatorExtensionIds
	 *            An array of unique identifiers associated with existing
	 *            extension.
	 * @param toEnable
	 *            True indicates the extension should be enabled; False
	 *            indicates otherwise.
	 * 
	 */
	public void activateNavigatorExtension(String aViewerId,
			String[] aNavigatorExtensionIds, boolean toEnable) {

		Set activatedExtensions = getActiveExtensions(aViewerId, true);
		if (toEnable)
			for (int i = 0; i < aNavigatorExtensionIds.length; i++)
				activatedExtensions
						.add(getExtensionActivationPreferenceKey(aNavigatorExtensionIds[i]));

		else
			for (int i = 0; i < aNavigatorExtensionIds.length; i++)
				activatedExtensions
						.remove(getExtensionActivationPreferenceKey(aNavigatorExtensionIds[i]));
		notifyListeners(aViewerId, aNavigatorExtensionIds, toEnable);

	}

	/**
	 * Save the activation state for the given viewer.
	 * 
	 * @param aViewerId
	 *            The unique identifier for a defined viewer that uses an
	 *            INavigatorContentService for its content.
	 */
	public void persistExtensionActivations(String aViewerId) {

		Set activatedExtensions = getActiveExtensions(aViewerId, false);
		if (activatedExtensions == null)
			return;

		Preferences preferences = NavigatorPlugin.getDefault()
				.getPluginPreferences();

		synchronized (activatedExtensions) {
			Iterator activatedExtensionsIterator = activatedExtensions
					.iterator();
			/* ensure that the preference will be non-empty */
			StringBuffer activatedExtensionsStringBuffer = new StringBuffer(";"); //$NON-NLS-1$
			while (activatedExtensionsIterator.hasNext()) {
				activatedExtensionsStringBuffer.append(
						activatedExtensionsIterator.next()).append(";"); //$NON-NLS-1$
			}

			preferences.setValue(getPreferenceKey(aViewerId),
					activatedExtensionsStringBuffer.toString());
		}
		NavigatorPlugin.getDefault().savePluginPreferences();
	}

	/**
	 * Request notification when the activation state changes for the given
	 * viewer id.
	 * 
	 * @param aViewerId
	 *            The unique identifier for a defined viewer that uses an
	 *            INavigatorContentService for its content.
	 * @param aListener
	 *            An implementation of {@link IExtensionActivationListener}
	 */
	public void addExtensionActivationListener(String aViewerId,
			IExtensionActivationListener aListener) {
		synchronized (contentServiceListenersMap) {
			ListenerList listeners = getExtensionActivationListeners(aViewerId,
					true);
			listeners.add(aListener);
		}
	}

	/**
	 * No longer receive notification when activation state changes.
	 * 
	 * @param aViewerId
	 *            The unique identifier for a defined viewer that uses an
	 *            INavigatorContentService for its content.
	 * @param aListener
	 *            An implementation of {@link IExtensionActivationListener}
	 */
	public void removeExtensionActivationListener(String aViewerId,
			IExtensionActivationListener aListener) {
		synchronized (contentServiceListenersMap) {
			ListenerList listeners = getExtensionActivationListeners(aViewerId,
					true);
			listeners.remove(aListener);
		}
	}

	private void notifyListeners(String aViewerId,
			String[] navigatorExtensionIds, boolean toEnable) {
		synchronized (contentServiceListenersMap) {
			ListenerList listeners = (ListenerList) contentServiceListenersMap
					.get(aViewerId);
			if (listeners != null) {
				Object[] listenerArray = listeners.getListeners();
				for (int i = 0; i < listenerArray.length; i++)
					((IExtensionActivationListener) listenerArray[i])
							.onExtensionActivation(aViewerId,
									navigatorExtensionIds, toEnable);

			}
		}
	}

	private ListenerList getExtensionActivationListeners(String aViewerId,
			boolean initializeIfNecessary) {
		ListenerList listeners = (ListenerList) contentServiceListenersMap
				.get(aViewerId);
		if (listeners != null || !initializeIfNecessary)
			return listeners;

		synchronized (contentServiceListenersMap) {
			listeners = (ListenerList) contentServiceListenersMap
					.get(aViewerId);
			if (listeners == null)
				contentServiceListenersMap.put(aViewerId,
						(listeners = new ListenerList()));
		}
		return listeners;
	}

	private Set getActiveExtensions(String aViewerId,
			boolean initializeIfNecessary) {
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

		Preferences preferences = NavigatorPlugin.getDefault()
				.getPluginPreferences();

		Set activatedExtensions = new HashSet();
		String activatedExtensionsString = preferences
				.getString(getPreferenceKey(aViewerId));

		if (activatedExtensionsString != null
				&& activatedExtensionsString.length() > 0) {
			String activatedExtensionKey = null;
			StringTokenizer tokenizer = new StringTokenizer(
					activatedExtensionsString, ";"); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				activatedExtensionKey = tokenizer.nextToken();
				if (activatedExtensionKey.length() > 0)
					activatedExtensions.add(activatedExtensionKey);
			}
		} else {
			INavigatorContentDescriptor[] contentDescriptors = CONTENT_DESCRIPTOR_REGISTRY
					.getAllContentDescriptors();
			for (int i = 0; i < contentDescriptors.length; i++)
				if (contentDescriptors[i].isEnabledByDefault())
					activatedExtensions
							.add(getExtensionActivationPreferenceKey(contentDescriptors[i]
									.getId()));
		}
		return activatedExtensions;
	}

	private String getPreferenceKey(String aViewerId) {
		return aViewerId + ACTIVATED_EXTENSIONS;
	}

	private String getExtensionActivationPreferenceKey(
			String aNavigatorExtensionId) {
		return aNavigatorExtensionId + EXTENSION_ACTIVATED_SUFFIX;
	}

}
