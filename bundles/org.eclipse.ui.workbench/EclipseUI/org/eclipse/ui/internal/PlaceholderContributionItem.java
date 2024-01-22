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

package org.eclipse.ui.internal;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

/**
 * A contribution item that is intended to hold the place of a tool bar
 * contribution item that has been disposed. This is to ensure that tool bar
 * contribution items are disposed (freeing their resources), but that layout
 * information about the item is not lost.
 *
 * @since 3.0
 */
final class PlaceholderContributionItem implements IContributionItem {

	/**
	 * The identifier for the replaced contribution item.
	 */
	private final String id;

	/**
	 * The height of the SWT widget corresponding to the replaced contribution item.
	 */
	private final int storedHeight;

	/**
	 * The minimum number of items to display on the replaced contribution item.
	 */
	private final int storedMinimumItems;

	/**
	 * Whether the replaced contribution item would display chevrons.
	 */
	private final boolean storedUseChevron;

	/**
	 * The width of the SWT widget corresponding to the replaced contribution item.
	 */
	private final int storedWidth;

	/**
	 * Constructs a new instance of <code>PlaceholderContributionItem</code> from
	 * the item it is intended to replace.
	 *
	 * @param item The item to be replaced; must not be <code>null</code>.
	 */
	PlaceholderContributionItem(final IToolBarContributionItem item) {
		item.saveWidgetState();
		id = item.getId();
		storedHeight = item.getCurrentHeight();
		storedWidth = item.getCurrentWidth();
		storedMinimumItems = item.getMinimumItemsToShow();
		storedUseChevron = item.getUseChevron();
	}

	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public void fill(Composite parent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void fill(CoolBar parent, int index) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void fill(Menu parent, int index) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void fill(ToolBar parent, int index) {
		throw new UnsupportedOperationException();

	}

	/**
	 * The height of the replaced contribution item.
	 *
	 * @return The height.
	 */
	int getHeight() {
		return storedHeight;
	}

	@Override
	public String getId() {
		return id;
	}

	/**
	 * The width of the replaced contribution item.
	 *
	 * @return The width.
	 */
	int getWidth() {
		return storedWidth;
	}

	/**
	 * Returns the minimum number of tool items to show in the cool item.
	 *
	 * @return the minimum number of tool items to show, or
	 *         <code>SHOW_ALL_ITEMS</code> if a value was not set
	 * @see #setMinimumItemsToShow(int)
	 * @since 3.2
	 */
	int getMinimumItemsToShow() {
		return storedMinimumItems;
	}

	/**
	 * Returns whether chevron support is enabled.
	 *
	 * @return <code>true</code> if chevron support is enabled, <code>false</code>
	 *         otherwise
	 * @since 3.2
	 */
	boolean getUseChevron() {
		return storedUseChevron;
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		// XXX Auto-generated method stub
		return false;
	}

	@Override
	public boolean isGroupMarker() {
		return false;
	}

	@Override
	public boolean isSeparator() {
		return false;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public void saveWidgetState() {
		// Do nothing.

	}

	@Override
	public void setParent(IContributionManager parent) {
		// Do nothing

	}

	@Override
	public void setVisible(boolean visible) {
		// Do nothing.
	}

	/**
	 * Displays a string representation of this contribution item, which is really
	 * just a function of its identifier.
	 */
	@Override
	public String toString() {
		return "PlaceholderContributionItem(" + id + ")"; //$NON-NLS-1$//$NON-NLS-2$
	}

	@Override
	public void update() {
		update(null);

	}

	@Override
	public void update(String identifier) {
		// Do nothing
	}
}
