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

package org.eclipse.jface.action;

import org.eclipse.pde.api.tools.annotations.NoImplement;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

/**
 * A contribution item represents a contribution to a shared UI resource such as
 * a menu or tool bar. More generally, contribution items are managed by a
 * contribution manager. For instance, in a tool bar a contribution item is a
 * tool bar button or a separator. In a menu bar a contribution item is a menu,
 * and in a menu a contribution item is a menu item or separator.
 * <p>
 * A contribution item can realize itself in different SWT widgets, using the
 * different <code>fill</code> methods. The same type of contribution item can
 * be used with a <code>MenuBarManager</code>, <code>ToolBarManager</code>,
 * <code>CoolBarManager</code>, or a <code>StatusLineManager</code>.
 * </p>
 * <p>
 * This interface is internal to the framework; it should not be implemented
 * outside the framework.
 * </p>
 *
 * @see IContributionManager
 */
@NoImplement
public interface IContributionItem {

	/**
	 * Disposes of this contribution item. Called by the parent
	 * contribution manager when the manager is being disposed.
	 * Clients should not call this method directly, unless they
	 * have removed this contribution item from the containing
	 * IContributionManager before the contribution lifecycle
	 * has ended.
	 *
	 * @since 2.1
	 */
	void dispose();

	/**
	 * Fills the given composite control with controls representing this
	 * contribution item.  Used by <code>StatusLineManager</code>.
	 *
	 * @param parent the parent control
	 */
	void fill(Composite parent);

	/**
	 * Fills the given menu with controls representing this contribution item.
	 * Used by <code>MenuManager</code>.
	 *
	 * @param parent the parent menu
	 * @param index the index where the controls are inserted,
	 *   or <code>-1</code> to insert at the end
	 */
	void fill(Menu parent, int index);

	/**
	 * Fills the given tool bar with controls representing this contribution item.
	 * Used by <code>ToolBarManager</code>.
	 *
	 * @param parent the parent tool bar
	 * @param index the index where the controls are inserted,
	 *   or <code>-1</code> to insert at the end
	 */
	void fill(ToolBar parent, int index);

	/**
	 * Fills the given cool bar with controls representing this contribution item.
	 * Used by <code>CoolBarManager</code>.
	 *
	 * @param parent the parent cool bar
	 * @param index the index where the controls are inserted,
	 *   or <code>-1</code> to insert at the end
	 * @since 3.0
	 */
	void fill(CoolBar parent, int index);

	/**
	 * Returns the identifier of this contribution item.
	 * The id is used for retrieving an item from its manager.
	 *
	 * @return the contribution item identifier, or <code>null</code>
	 *   if none
	 */
	String getId();

	/**
	 * Returns whether this contribution item is enabled.
	 *
	 * @return <code>true</code> if this item is enabled
	 */
	boolean isEnabled();

	/**
	 * Returns whether this contribution item is dirty. A dirty item will be
	 * recreated when the action bar is updated.
	 *
	 * @return <code>true</code> if this item is dirty
	 */
	boolean isDirty();

	/**
	 * Returns whether this contribution item is dynamic. A dynamic contribution
	 * item contributes items conditionally, dependent on some internal state.
	 *
	 * @return <code>true</code> if this item is dynamic, and
	 *  <code>false</code> for normal items
	 */
	boolean isDynamic();

	/**
	 * Returns whether this contribution item is a group marker.
	 * This information is used when adding items to a group.
	 *
	 * @return <code>true</code> if this item is a group marker, and
	 *  <code>false</code> for normal items
	 *
	 * @see GroupMarker
	 * @see IContributionManager#appendToGroup(String, IContributionItem)
	 * @see IContributionManager#prependToGroup(String, IContributionItem)
	 */
	boolean isGroupMarker();

	/**
	 * Returns whether this contribution item is a separator.
	 * This information is used to enable hiding of unnecessary separators.
	 *
	 * @return <code>true</code> if this item is a separator, and
	 *  <code>false</code> for normal items
	 * @see Separator
	 */
	boolean isSeparator();

	/**
	 * Returns whether this contribution item is visibile within its manager.
	 *
	 * @return <code>true</code> if this item is visible, and
	 *  <code>false</code> otherwise
	 */
	boolean isVisible();

	/**
	 * Saves any state information of the control(s) owned by this contribution item.
	 * The contribution manager calls this method before disposing of the controls.
	 *
	 * @since 3.0
	 */
	void saveWidgetState();

	/**
	 * Sets the parent manager of this item
	 *
	 * @param parent the parent contribution manager
	 * @since 2.0
	 */
	void setParent(IContributionManager parent);

	/**
	 * Sets whether this contribution item is visibile within its manager.
	 *
	 * @param visible <code>true</code> if this item should be visible, and
	 *  <code>false</code> otherwise
	 */
	void setVisible(boolean visible);

	/**
	 * Updates any SWT controls cached by this contribution item with any
	 * changes which have been made to this contribution item since the last update.
	 * Called by contribution manager update methods.
	 */
	void update();

	/**
	 * Updates any SWT controls cached by this contribution item with changes
	 * for the the given property.
	 *
	 * @param id the id of the changed property
	 * @since 2.0
	 */
	void update(String id);
}
