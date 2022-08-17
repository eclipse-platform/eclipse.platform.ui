/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.views.properties;

/**
 * Listener for changes in objects of type <code>IPropertySheetEntry</code>.
 * <p>
 * This interface is public since it appears in the api of
 * <code>IPropertySheetEntry</code>. It is not intended to be implemented
 * outside of this package.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPropertySheetEntryListener {
	/**
	 * A node's children have changed (children added or removed)
	 *
	 * @param node the node whose's children have changed
	 */
	void childEntriesChanged(IPropertySheetEntry node);

	/**
	 * A entry's error message has changed
	 *
	 * @param entry the entry whose's error message has changed
	 */
	void errorMessageChanged(IPropertySheetEntry entry);

	/**
	 * A entry's value has changed
	 *
	 * @param entry the entry whose's value has changed
	 */
	void valueChanged(IPropertySheetEntry entry);
}
