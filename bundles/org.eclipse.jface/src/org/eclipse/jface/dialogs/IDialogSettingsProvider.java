/*******************************************************************************
 * Copyright (c) 2020, 2020 Remain Software.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wim Jongman - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.dialogs;

/**
 * Provides dialog settings without assumption on how the dialog settings should
 * be saved or restored. This is up to the implementation of the interface.
 *
 * @see IDialogSettings
 * @since 3.22
 */
public interface IDialogSettingsProvider {

	/**
	 * Reloads the persisted dialog settings or provides a new
	 * {@link IDialogSettings} if there is no existing dialog settings.
	 *
	 * @return an instance of {@link IDialogSettings}
	 */
	IDialogSettings loadDialogSettings();

	/**
	 * Saves the dialog settings.
	 */
	void saveDialogSettings();

	/**
	 * @return the dialog settings, never null.
	 */
	IDialogSettings getDialogSettings();

}
