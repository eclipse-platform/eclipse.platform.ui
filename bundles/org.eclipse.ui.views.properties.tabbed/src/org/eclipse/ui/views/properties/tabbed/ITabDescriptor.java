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
 ******************************************************************************/

package org.eclipse.ui.views.properties.tabbed;

import java.util.List;

/**
 * Represents a tab descriptor for the tabbed property view.
 * <p>
 * This interface should not be extended or implemented. New instances should be
 * created using <code>AbstractTabDescriptor</code>.
 * </p>
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public interface ITabDescriptor extends ITabItem {

	/**
	 * If afterTab is not specified in the descriptor, we default to be the top
	 * tab.
	 */
	public static final String TOP = "top"; //$NON-NLS-1$

	/**
	 * Instantiate this tab's sections.
	 *
	 * @return The tab contents for this section.
	 */
	public TabContents createTab();

	/**
	 * Get the identifier of the tab after which this tab should be displayed.
	 * When two or more tabs belong to the same category, they are sorted by the
	 * after tab values.
	 *
	 * @return the identifier of the tab.
	 */
	public String getAfterTab();

	/**
	 * Get the category this tab belongs to.
	 *
	 * @return Get the category this tab belongs to.
	 */
	public String getCategory();

	/**
	 * Get the unique identifier for the tab.
	 *
	 * @return the unique identifier for the tab.
	 */
	public String getId();

	/**
	 * Get the text label for the tab.
	 *
	 * @return the text label for the tab.
	 */
	public String getLabel();

	/**
	 * Get the list of section descriptors for the tab.
	 *
	 * @return the list of section descriptors for the tab.
	 */
	public List getSectionDescriptors();
}
