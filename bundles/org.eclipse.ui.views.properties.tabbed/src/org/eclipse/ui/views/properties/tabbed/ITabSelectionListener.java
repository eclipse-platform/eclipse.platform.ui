/*******************************************************************************
 * Copyright (c) 2001, 2015 IBM Corporation and others.
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
package org.eclipse.ui.views.properties.tabbed;

/**
 * A listener interested in tab selection events that occur for the tabbed
 * property sheet page.
 *
 * @author Anthony Hunter
 */
public interface ITabSelectionListener {

	/**
	 * Notifies this listener that the selected tab has changed.
	 *
	 * @param tabDescriptor
	 *            the selected tab descriptor.
	 * @since 3.4
	 */
	public void tabSelected(ITabDescriptor tabDescriptor);
}
