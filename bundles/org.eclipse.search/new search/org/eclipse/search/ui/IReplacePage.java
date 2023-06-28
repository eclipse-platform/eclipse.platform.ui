/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.search.ui;

/**
 * An extension interface to <code>ISearchPage</code>. If clients implement
 * <code>IReplacePage</code> in addition to <code>ISearchPage</code>, a
 * "Replace" button will be shown in the search dialog.
 *
 * @since 3.0
 */
public interface IReplacePage {

	/**
	 * Performs the replace action for this page.
	 * The search dialog calls this method when the Replace
	 * button is pressed.
	 *
	 * @return <code>true</code> if the dialog can be closed after execution
	 */
	public boolean performReplace();

}
