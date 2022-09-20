/*******************************************************************************
 * Copyright (c) 2022 Enda O'Brien and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers;

/**
 * @since 3.19
 *
 */
public interface IFilterHelpHandler {
	/**
	 * Implement to open a help page when the help link is clicked.
	 */
	void handleHelpClick();
}
