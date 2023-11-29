/*******************************************************************************
 * Copyright (c) 2003, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptorManager;
import org.eclipse.ui.navigator.IExtensionActivationListener;
import org.eclipse.ui.navigator.INavigatorActivationService;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 *
 * The activation service determines if an extension is <i>active</i> within the
 * context of a given viewer. If an extension is <i>active</i> then the extension
 * will contribute functionality to the viewer. If an extension is not <i>active</i>,
 * then the extension will not be given opportunities to contribute
 * functionality to the given viewer. See {@link INavigatorContentService} for
 * more detail on what states are associated with a content extension.
 *
 * @since 3.2
 */
public final class NavigatorActivationService implements
		INavigatorActivationService {

	private static final String ACTIVATED_EXTENSIONS = ".activatedExtensions"; //$NON-NLS-1$

	private static final NavigatorContentDescriptorManager CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorManager
			.getInstance();

	private static final INavigatorContentDescriptor[] NO_DESCRIPTORS = new INavigatorContentDescriptor[0];

	private static final String DELIM = ";"; //$NON-NLS-1$

	private static final char EQUALS = '=';

	/*
	 * Map of ids of activated extensions. Note this is only synchronized when
	 * modifying it structurally (that is adding or deleting entries in it). For
	 * changing of the activated extension state, no synchronization is
	 * necessary.  Though this is semantically functioning as a Set, it's implemented
	 * as a Map to avoid the synchronization during the frequent checking of
	 * extension status.
	 */
	private final Map<String, Boolean> activatedExtensionsMap = new HashMap<>();

	private final ListenerList<IExtensionActivationListener> listeners = new ListenerList<>();

	private INavigatorContentService contentService;

	/**
	 * Create an instance of the service.
	 *
	 * @param aContentService
	 *            The associated content service.
	 */
	public NavigatorActivationService(INavigatorContentService aContentService) {
		contentService = aContentService;
		revertExtensionActivations();
	}

	/**
	 *
	 * Checks the known activation state for the given viewer id to determine if
	 * the given navigator extension is 'active'.
	 *
	 * @param aNavigatorExtensionId
	 *            The unique identifier associated with a given extension.
	 *
	 * @return True if the extension is active in the context of the viewer id.
	 */
	@Override
	public boolean isNavigatorExtensionActive(String aNavigatorExtensionId) {
		Boolean b = activatedExtensionsMap.get(aNavigatorExtensionId);
		if(b != null)
			return b.booleanValue();
		synchronized (activatedExtensionsMap) {
			NavigatorContentDescriptor descriptor = CONTENT_DESCRIPTOR_REGISTRY.getContentDescriptor(aNavigatorExtensionId);
			if (descriptor == null)
				return false;
			if(descriptor.isActiveByDefault())
				activatedExtensionsMap.put(aNavigatorExtensionId, Boolean.TRUE);
			else
				activatedExtensionsMap.put(aNavigatorExtensionId, Boolean.FALSE);
			return descriptor.isActiveByDefault();
		}
	}

	/**
	 * Set the activation state for the given extension in the context of the
	 * given viewer id. Each instance of an INavigatorContentService listens for
	 * the activation service to update; and if those instances were created
	 * with viewers, they will issue a refresh. Otherwise, clients are
	 * responsible for refreshing the viewers.
	 *
	 * <p>
	 * Clients must call {@link #persistExtensionActivations()} to save
	 * the the activation state.
	 * </p>
	 *
	 * <p>
	 * When clients are updating a batch of extensions, consider using
	 * {@link #setActive(String[], boolean)} when
	 * possible to avoid unnecessary notifications.
	 * </p>
	 *
	 * @param aNavigatorExtensionId
	 *            The unique identifier associated with a given extension.
	 * @param toEnable
	 *            True indicates the extension should be enabled; False
	 *            indicates otherwise.
	 */
	public void setActive(
			String aNavigatorExtensionId, boolean toEnable) {

		boolean currentlyActive = isNavigatorExtensionActive(aNavigatorExtensionId);
		if (currentlyActive == toEnable) {
			return;
		}

		if (toEnable) {
			activatedExtensionsMap.put(aNavigatorExtensionId, Boolean.TRUE);
		} else {
			activatedExtensionsMap.put(aNavigatorExtensionId, Boolean.FALSE);
		}
		notifyListeners(new String[] { aNavigatorExtensionId }, toEnable);

	}

	/**
	 * Set the activation state for the given extension in the context of the
	 * given viewer id. Each instance of an INavigatorContentService listens for
	 * the activation service to update; and if those instances were created
	 * with viewers, they will issue a refresh. Otherwise, clients are
	 * responsible for refreshing the viewers.
	 *
	 * <p>
	 * Clients must call {@link #persistExtensionActivations()} to save
	 * the the activation state.
	 * </p>
	 *
	 * @param aNavigatorExtensionIds
	 *            An array of unique identifiers associated with existing
	 *            extension.
	 * @param toEnable
	 *            True indicates the extension should be enabled; False
	 *            indicates otherwise.
	 */
	public void setActive(String[] aNavigatorExtensionIds,
			boolean toEnable) {

		if (toEnable) {
			for (String aNavigatorExtensionId : aNavigatorExtensionIds) {
				activatedExtensionsMap.put(aNavigatorExtensionId, Boolean.TRUE);
			}
		} else {
			for (String aNavigatorExtensionId : aNavigatorExtensionIds) {
				activatedExtensionsMap.put(aNavigatorExtensionId, Boolean.FALSE);
			}
		}
		notifyListeners(aNavigatorExtensionIds, toEnable);

	}

	/**
	 * Save the activation state for the given viewer.
	 */
	@Override
	public void persistExtensionActivations() {
		IEclipsePreferences prefs = NavigatorContentService.getPreferencesRoot();

		synchronized (activatedExtensionsMap) {
			Iterator<String> activatedExtensionsIterator = activatedExtensionsMap.keySet().iterator();

			/* ensure that the preference will be non-empty */
			StringBuilder preferenceValue = new StringBuilder();
			String navigatorExtensionId = null;
			boolean isActive = false;
			while (activatedExtensionsIterator.hasNext()) {
				navigatorExtensionId = activatedExtensionsIterator.next();
				isActive = isNavigatorExtensionActive(navigatorExtensionId);
				preferenceValue.append(navigatorExtensionId)
									.append(EQUALS)
										.append( isActive ? Boolean.TRUE : Boolean.FALSE )
											.append(DELIM);
			}
			prefs.put(getPreferenceKey(), preferenceValue.toString());
		}

		NavigatorContentService.flushPreferences(prefs);
	}

	/**
	 * Request notification when the activation state changes for the given
	 * viewer id.
	 *
	 * @param aListener
	 *            An implementation of {@link IExtensionActivationListener}
	 */
	@Override
	public void addExtensionActivationListener(
			IExtensionActivationListener aListener) {
		listeners.add(aListener);
	}

	/**
	 * No longer receive notification when activation state changes.
	 *
	 * @param aListener
	 *            An implementation of {@link IExtensionActivationListener}
	 */
	@Override
	public void removeExtensionActivationListener(
			IExtensionActivationListener aListener) {
		listeners.remove(aListener);
	}

	private void notifyListeners(String[] navigatorExtensionIds,
			boolean toEnable) {

		if(navigatorExtensionIds != null) { // should really never be null, but just in case
			if(navigatorExtensionIds.length > 1)
				Arrays.sort(navigatorExtensionIds);

			for (IExtensionActivationListener element : listeners) {
				element.onExtensionActivation(contentService.getViewerId(), navigatorExtensionIds, toEnable);
			}
		}

	}

	private void revertExtensionActivations() {

		IEclipsePreferences prefs = NavigatorContentService.getPreferencesRoot();

		String activatedExtensionsString = prefs
				.get(getPreferenceKey(), null);

		if (activatedExtensionsString != null
				&& activatedExtensionsString.length() > 0) {
			String[] contentExtensionIds = activatedExtensionsString
					.split(DELIM);

			String id = null;
			String booleanString = null;
			int indx=0;
			for (String contentExtensionId : contentExtensionIds) {
				if( (indx = contentExtensionId.indexOf(EQUALS)) > -1) {
					// up to but not including the equals
					id = contentExtensionId.substring(0, indx);
					booleanString = contentExtensionId.substring(indx+1, contentExtensionId.length());
					activatedExtensionsMap.put(id, Boolean.valueOf(booleanString));
				} else {
					// IS THIS THE RIGHT WAY TO HANDLE THIS CASE?
					NavigatorContentDescriptor descriptor = CONTENT_DESCRIPTOR_REGISTRY.getContentDescriptor(contentExtensionId);
					if(descriptor != null)
						activatedExtensionsMap.put(id, Boolean.valueOf(descriptor.isActiveByDefault()));
				}
			}

		} else {
			/*
			 * We add the default activation of every known extension, even
			 * though some may not be bound to the associated content service;
			 * this is because they could be bound at a later time through the
			 * programmatic binding mechanism in INavigatorContentService.
			 */
			INavigatorContentDescriptor[] contentDescriptors = CONTENT_DESCRIPTOR_REGISTRY
					.getAllContentDescriptors();
			for (INavigatorContentDescriptor contentDescriptor : contentDescriptors) {
				if (contentDescriptor.isActiveByDefault()) {
					activatedExtensionsMap.put(contentDescriptor.getId(), Boolean.TRUE);
				}
			}
		}
	}

	private String getPreferenceKey() {
		return contentService.getViewerId() + ACTIVATED_EXTENSIONS;
	}


	@Override
	public INavigatorContentDescriptor[] activateExtensions(
			String[] extensionIds, boolean toDeactivateAllOthers) {

		Set<NavigatorContentDescriptor> activatedDescriptors = new HashSet<>();
		setActive(extensionIds, true);
		for (String extensionId : extensionIds) {
			activatedDescriptors.add(CONTENT_DESCRIPTOR_REGISTRY
					.getContentDescriptor(extensionId));
		}

		if (toDeactivateAllOthers) {
			NavigatorContentDescriptor[] descriptors = CONTENT_DESCRIPTOR_REGISTRY
					.getAllContentDescriptors();
			List<NavigatorContentDescriptor> descriptorList = new ArrayList<>(Arrays.asList(descriptors));

			for (NavigatorContentDescriptor descriptor : descriptors) {
				for (String extensionId : extensionIds) {
					if (descriptor.getId().equals(extensionId)) {
						descriptorList.remove(descriptor);
					}
				}
			}

			String[] deactivatedExtensions = new String[descriptorList.size()];
			for (int i = 0; i < descriptorList.size(); i++) {
				INavigatorContentDescriptor descriptor = descriptorList
						.get(i);
				deactivatedExtensions[i] = descriptor.getId();
			}
			setActive(deactivatedExtensions, false);
		}

		if (activatedDescriptors.isEmpty()) {
			return NO_DESCRIPTORS;
		}
		return activatedDescriptors
				.toArray(new NavigatorContentDescriptor[activatedDescriptors
						.size()]);
	}

	@Override
	public INavigatorContentDescriptor[] deactivateExtensions(
			String[] extensionIds, boolean toEnableAllOthers) {

		Set<NavigatorContentDescriptor> activatedDescriptors = new HashSet<>();
		setActive(extensionIds, false);

		if (toEnableAllOthers) {
			NavigatorContentDescriptor[] descriptors = CONTENT_DESCRIPTOR_REGISTRY
					.getAllContentDescriptors();
			List<NavigatorContentDescriptor> descriptorList = new ArrayList<>(Arrays.asList(descriptors));

			for (NavigatorContentDescriptor descriptor : descriptors) {
				for (String extensionId : extensionIds) {
					if (descriptor.getId().equals(extensionId)) {
						descriptorList.remove(descriptor);
					}
				}
			}

			String[] activatedExtensions = new String[descriptorList.size()];
			for (int i = 0; i < descriptorList.size(); i++) {
				NavigatorContentDescriptor descriptor = descriptorList
						.get(i);
				activatedExtensions[i] = descriptor.getId();
				activatedDescriptors.add(descriptor);
			}
			setActive(activatedExtensions,	true);
		}
		if (activatedDescriptors.isEmpty()) {
			return NO_DESCRIPTORS;
		}

		return activatedDescriptors
				.toArray(new NavigatorContentDescriptor[activatedDescriptors
						.size()]);
	}


}
