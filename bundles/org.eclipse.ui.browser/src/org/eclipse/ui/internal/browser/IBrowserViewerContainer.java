/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import org.eclipse.ui.IActionBars;

public interface IBrowserViewerContainer {
	/**
	 * Closes the container from the inside.
	 * @return true if the container closed successfully
	 */
	boolean close();
	/**
	 * Returns the action bars of the container.
	 * @return action bars of the container or <code>null</code> if
	 * not available.
	 */
	IActionBars getActionBars();
	/**
	 * Opens the url in the external browser if
	 * internal browser failed to create.
	 * @param url
	 */
	void openInExternalBrowser(String url);
}