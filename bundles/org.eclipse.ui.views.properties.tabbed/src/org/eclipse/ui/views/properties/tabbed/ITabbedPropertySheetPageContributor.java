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
 * Interface for a workbench part to contribute content to the tabbed property
 * view.
 * <p>
 * It is expected that the contributor ID is unique for a configuration of tabs
 * and sections. Editors and views can share a configuration by sharing a
 * contributor ID. Editors and views cannot share tabs and sections from
 * multiple contributors.
 * </p>
 * <p>
 * As a workaround, if all the elements in a structured selection implement
 * ITabbedPropertySheetPageContributor and they all return the same unique
 * contributor ID, then that configuration of tabs and sections will be used by
 * the tabbed property view for that selection.
 * </p>
 *
 * @author Anthony Hunter
 */
public interface ITabbedPropertySheetPageContributor {

	/**
	 * Returns the contributor ID for the tabbed property sheet page.
	 *
	 * @return the contributor ID for the tabbed property sheet page.
	 */
	public String getContributorId();
}
