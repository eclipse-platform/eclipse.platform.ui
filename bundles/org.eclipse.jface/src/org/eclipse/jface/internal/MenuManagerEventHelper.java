/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal;

import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.MenuManager;

/**
 * @since 3.8.100
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 */
public final class MenuManagerEventHelper {

	/**
	 * Uses IMenuListener2 to do some processing before (menuAboutToShow) and
	 * after (menuAboutToHide) the SWT.Show event.
	 */
	public static IMenuListener2 showHelper;

	/**
	 * Uses IMenuListener2 to do some processing before (menuAboutToShow) and
	 * after (menuAboutToHide) the SWT.Hide event.
	 */
	public static IMenuListener2 hideHelper;

	/**
	 * Do show pre-processing.
	 * 
	 * @param manager
	 */
	public static void showEventPreHelper(MenuManager manager) {
		if (showHelper != null) {
			showHelper.menuAboutToShow(manager);
		}
	}

	/**
	 * Do show post-processing.
	 * 
	 * @param manager
	 */
	public static void showEventPostHelper(MenuManager manager) {
		if (showHelper != null) {
			showHelper.menuAboutToHide(manager);
		}
	}

	/**
	 * Do hide pre-processing.
	 * 
	 * @param manager
	 */
	public static void hideEventPreHelper(MenuManager manager) {
		if (hideHelper != null) {
			hideHelper.menuAboutToShow(manager);
		}
	}

	/**
	 * Do hide post-processing.
	 * 
	 * @param manager
	 */
	public static void hideEventPostHelper(MenuManager manager) {
		if (hideHelper != null) {
			hideHelper.menuAboutToHide(manager);
		}
	}

	private MenuManagerEventHelper() {
	}
}
