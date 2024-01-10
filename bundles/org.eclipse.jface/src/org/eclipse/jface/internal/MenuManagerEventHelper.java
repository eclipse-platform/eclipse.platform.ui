/*******************************************************************************
 * Copyright (c) 2012, 2014 IBM Corporation and others.
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

package org.eclipse.jface.internal;

import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.pde.api.tools.annotations.NoInstantiate;
import org.eclipse.pde.api.tools.annotations.NoReference;

/**
 * @since 3.8.100
 */
@NoInstantiate
@NoReference
public final class MenuManagerEventHelper {

	private IMenuListener2 showHelper;

	private IMenuListener2 hideHelper;

	private static MenuManagerEventHelper INSTANCE;

	/**
	 * @return singleton instance
	 */
	public static MenuManagerEventHelper getInstance() {
		if( INSTANCE == null ) {
			INSTANCE = new MenuManagerEventHelper();
		}
		return INSTANCE;
	}

	/**
	 * Uses IMenuListener2 to do some processing before (menuAboutToShow) and
	 * after (menuAboutToHide) the SWT.Show event.
	 */
	public void setShowHelper(IMenuListener2 showHelper) {
		this.showHelper = showHelper;
	}

	/**
	 * @return the show helper
	 */
	public IMenuListener2 getShowHelper() {
		return showHelper;
	}

	/**
	 * Uses IMenuListener2 to do some processing before (menuAboutToShow) and
	 * after (menuAboutToHide) the SWT.Hide event.
	 */
	public void setHideHelper(IMenuListener2 hideHelper) {
		this.hideHelper = hideHelper;
	}

	/**
	 * @return the hide helper
	 */
	public IMenuListener2 getHideHelper() {
		return this.hideHelper;
	}

	/**
	 * Do show pre-processing.
	 */
	public void showEventPreHelper(MenuManager manager) {
		if (showHelper != null) {
			showHelper.menuAboutToShow(manager);
		}
	}

	/**
	 * Do show post-processing.
	 */
	public void showEventPostHelper(MenuManager manager) {
		if (showHelper != null) {
			showHelper.menuAboutToHide(manager);
		}
	}

	/**
	 * Do hide pre-processing.
	 */
	public void hideEventPreHelper(MenuManager manager) {
		if (hideHelper != null) {
			hideHelper.menuAboutToShow(manager);
		}
	}

	/**
	 * Do hide post-processing.
	 */
	public void hideEventPostHelper(MenuManager manager) {
		if (hideHelper != null) {
			hideHelper.menuAboutToHide(manager);
		}
	}

	private MenuManagerEventHelper() {
	}

}
