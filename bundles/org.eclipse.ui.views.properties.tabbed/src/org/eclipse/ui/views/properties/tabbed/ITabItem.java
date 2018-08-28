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

import org.eclipse.swt.graphics.Image;

/**
 * Represents a tab to be displayed in the tab list in the tabbed property sheet
 * page.
 *
 * @author Anthony Hunter
 */
public interface ITabItem {

	/**
	 * Get the icon image for the tab.
	 *
	 * @return the icon image for the tab.
	 */
	public Image getImage();

	/**
	 * Get the text label for the tab.
	 *
	 * @return the text label for the tab.
	 */
	public String getText();

	/**
	 * Determine if this tab is selected.
	 *
	 * @return <code>true</code> if this tab is selected.
	 */
	public boolean isSelected();

	/**
	 * Determine if this tab is indented.
	 *
	 * @return <code>true</code> if this tab is indented.
	 */
	public boolean isIndented();
}
