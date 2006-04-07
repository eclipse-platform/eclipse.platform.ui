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

package org.eclipse.ui.navigator;

/**
 * 
 * Determines if an extension is <i>active</i> within the context of a given
 * viewer and manages the persistence of this information. If an extension is
 * <i>active</i> then the extension will contribute functionality to the
 * viewer. If an extension is not <i>active</i>, then the extension will not be
 * given opportunities to contribute functionality to the given viewer. See
 * {@link INavigatorContentService} for more detail on what states are
 * associated with a content extension.
 * 
 * @since 3.2
 * 
 */
public interface INavigatorActivationService {

	/**
	 * Activate the extensions specified by the extensionIds array. Clients may
	 * also choose to disable all other extensions. The set of descriptors
	 * returned is the set that were activated as a result of this call. In the
	 * case of this method, that means that a descriptor will be returned for
	 * each extensionId in the array, regardless of whether that extension is
	 * already enabled.
	 * 
	 * <p>
	 * Clients must call {@link #persistExtensionActivations()} to save the the
	 * activation state after activating or deactivating extensions.
	 * </p>
	 * 
	 * @param extensionIds
	 *            The list of extensions to activate
	 * @param toDeactivateAllOthers
	 *            True will deactivate all other extensions; False will leave
	 *            the other activations as-is
	 * @return A list of all INavigatorContentDescriptors that were activated as
	 *         a result of this call. This will be the set of
	 *         INavigatorContentDescriptors that corresponds exactly to the set
	 *         of given extensionIds.
	 */
	public INavigatorContentDescriptor[] activateExtensions(
			String[] extensionIds, boolean toDeactivateAllOthers);

	/**
	 * Deactivate the extensions specified by the extensionIds. Clients may
	 * choose to activate all other extensions which are not explicitly
	 * disabled. If toActivateAllOthers is true, the array of returned
	 * descriptors will be the collection of all extensions not specified in the
	 * extensionIds array. If it is false, the array will be empty.
	 * 
	 * <p>
	 * Clients must call {@link #persistExtensionActivations()} to save the the
	 * activation state after activating or deactivating extensions.
	 * </p>
	 * 
	 * @param extensionIds
	 *            The list of extensions to activate
	 * @param toActivateAllOthers
	 *            True will activate all other extensions; False will leave the
	 *            other activations as-is
	 * @return A list of all INavigatorContentDescriptors that were activated as
	 *         a result of this call. If toActivateAllOthers is false, the
	 *         result will be an empty array. Otherwise, it will be the set of
	 *         all visible extensions minus those given in the 'extensionIds'
	 *         parameter.
	 */
	public INavigatorContentDescriptor[] deactivateExtensions(
			String[] extensionIds, boolean toActivateAllOthers);

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
	public boolean isNavigatorExtensionActive(String aNavigatorExtensionId);

	/**
	 * Save the activation state of each content extension for the associated
	 * content service. Clients should persist the activation state after any
	 * call to {@link #activateExtensions(String[], boolean)} or
	 * {@link #deactivateExtensions(String[], boolean)}.
	 * 
	 */
	public void persistExtensionActivations();

	/**
	 * Request notification when the activation state changes.
	 * 
	 * @param aListener
	 *            An implementation of {@link IExtensionActivationListener}
	 */
	public void addExtensionActivationListener(
			IExtensionActivationListener aListener);

	/**
	 * No longer receive notification when activation state changes.
	 * 
	 * @param aListener
	 *            An implementation of {@link IExtensionActivationListener}
	 */
	public void removeExtensionActivationListener(
			IExtensionActivationListener aListener);
}
