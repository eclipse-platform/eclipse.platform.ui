/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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

import org.eclipse.ui.IActionBars;

/**
 * Allows a tabbed properties view to make contributions to action bars.
 * <p>
 * An action provider is called when the tabbed properties view becomes the
 * active view. It is at this point where the action provider can override the
 * action bars.
 * </p>
 * <p>
 * Normally {@link TabbedPropertySheetPage#setActionBars(IActionBars)} is
 * sufficient, but this is only called once and is not sufficient for a
 * contributor that is selection based. An example is the Project Explorer where
 * different providers contribute different action sets and properties
 * configurations.
 * </p>
 * <p>
 * The most frequent use of setActionBars() is to retarget the global actions
 * for undo and redo based on the active tabbed properties view contributor.
 * </p>
 *
 * @author Anthony Hunter
 * @since 3.2.1
 */
public interface IActionProvider {

    /**
     * Allows the page to make contributions to the given action bars. The
     * contributions will be visible when the page is visible.
     *
     * @param contributor
     *            the tabbed property sheet page contributor.
     * @param actionBars
     *            the action bars for this page
     */
    public void setActionBars(ITabbedPropertySheetPageContributor contributor,
            IActionBars actionBars);
}
