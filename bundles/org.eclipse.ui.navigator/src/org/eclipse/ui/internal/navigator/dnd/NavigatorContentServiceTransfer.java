/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.dnd;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * Caches instances of {@link INavigatorContentService} to facilitate the
 * handling of drop operations in other viewers.
 *
 * @since 3.2
 */
public class NavigatorContentServiceTransfer {

	private static final NavigatorContentServiceTransfer instance = new NavigatorContentServiceTransfer();

	/**
	 *
	 * @return The singleton instance of the transfer service.
	 */
	public static NavigatorContentServiceTransfer getInstance() {
		return instance;
	}

	private final Set<WeakReference<INavigatorContentService>> registeredContentServices = new HashSet<>();

	/**
	 *
	 * @param aContentService The Content Service to register.
	 */
	public synchronized void registerContentService(INavigatorContentService aContentService) {
		if(findService(aContentService.getViewerId()) == null) {
			registeredContentServices.add(new WeakReference<>(aContentService));
		}
	}

	/**
	 *
	 * @param aContentService The Content Service to unregister.
	 */
	public synchronized void unregisterContentService(INavigatorContentService aContentService) {

		for (Iterator<WeakReference<INavigatorContentService>> iter = registeredContentServices.iterator(); iter.hasNext();) {
			WeakReference ref = iter.next();
			if(ref.get() == null) {
				iter.remove();
			} else if(ref.get() == aContentService) {
				iter.remove();
				return;
			}
		}
	}

	/**
	 *
	 * @param aViewerId A viewer id that should have previously been registered with the service.
	 * @return The registered content service for the given viewer id.
	 */
	public synchronized INavigatorContentService findService(String aViewerId) {
		if(aViewerId == null || aViewerId.length() == 0) {
			return null;
		}
		for (Iterator<WeakReference<INavigatorContentService>> iter = registeredContentServices.iterator(); iter.hasNext();) {
			WeakReference<INavigatorContentService> ref = iter.next();
			INavigatorContentService contentService = ref.get();
			if (contentService == null) {
				iter.remove();
			} else if (aViewerId.equals(contentService.getViewerId())) {
				return contentService;
			}
		}
		return null;
	}


}
