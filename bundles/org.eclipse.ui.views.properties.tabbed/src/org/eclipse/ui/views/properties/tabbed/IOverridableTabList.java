/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 * Represents a list of tabs. Used by a section that overrides the tabs that are
 * provided by the tabbed property registry with a new list of tabs.
 * <p>
 * The overridable tab list is a content provider that provides both the
 * sections and the tab labels.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public interface IOverridableTabList {

	/**
	 * Returns the list of tabs.
	 *
	 * @return the list of tabs.
	 */
	public ITabItem[] getTabs();

	/**
	 * Select the tab at the provided index.
	 *
	 * @param index
	 *            the index in the list of tabs to select.
	 */
	public void selectTab(int index);
}
