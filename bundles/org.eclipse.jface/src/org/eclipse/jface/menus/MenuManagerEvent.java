/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.menus;

import org.eclipse.core.commands.common.AbstractBitSetEvent;

/**
 * <p>
 * An event indicating that the set of defined menu element identifiers has
 * changed.
 * </p>
 * 
 * @since 3.2
 * @see IMenuManagerListener#menuManagerChanged(MenuManagerEvent)
 */
public final class MenuManagerEvent extends AbstractBitSetEvent {

	/**
	 * The bit used to represent whether the given action set has become
	 * defined. If this bit is not set and there is no action set id, then no
	 * action set has become defined nor undefined. If this bit is not set and
	 * there is a action set id, then the action set has become undefined.
	 */
	private static final int CHANGED_ACTION_SET_DEFINED = 1;

	/**
	 * The bit used to represent whether the given group has become defined. If
	 * this bit is not set and there is no group id, then no group has become
	 * defined nor undefined. If this bit is not set and there is a group id,
	 * then the group has become undefined.
	 */
	private static final int CHANGED_GROUP_DEFINED = 1 << 1;

	/**
	 * The bit used to represent whether the given item has become defined. If
	 * this bit is not set and there is no item id, then no item has become
	 * defined nor undefined. If this bit is not set and there is a item id,
	 * then the item has become undefined.
	 */
	private static final int CHANGED_ITEM_DEFINED = 1 << 2;

	/**
	 * The bit used to represent whether the given menu has become defined. If
	 * this bit is not set and there is no menu id, then no menu has become
	 * defined nor undefined. If this bit is not set and there is a menu id,
	 * then the menu has become undefined.
	 */
	private static final int CHANGED_MENU_DEFINED = 1 << 3;

	/**
	 * The bit used to represent whether the given widget has become defined. If
	 * this bit is not set and there is no widget id, then no widget has become
	 * defined nor undefined. If this bit is not set and there is a widget id,
	 * then the widget has become undefined.
	 */
	private static final int CHANGED_WIDGET_DEFINED = 1 << 4;

	/**
	 * The action set identifier that was added or removed from the list of
	 * defined action set identifiers. This value is <code>null</code> if the
	 * list of defined action set identifiers did not change.
	 */
	private final String actionSetId;

	/**
	 * The group identifier that was added or removed from the list of defined
	 * group identifiers. This value is <code>null</code> if the list of
	 * defined group identifiers did not change.
	 */
	private final String groupId;

	/**
	 * The item identifier that was added or removed from the list of defined
	 * item identifiers. This value is <code>null</code> if the list of
	 * defined item identifiers did not change.
	 */
	private final String itemId;

	/**
	 * The menu identifier that was added or removed from the list of defined
	 * menu identifiers. This value is <code>null</code> if the list of
	 * defined menu identifiers did not change.
	 */
	private final String menuId;

	/**
	 * The menu manager that has changed.
	 */
	private final SMenuManager menuManager;

	/**
	 * The widget identifier that was added or removed from the list of defined
	 * widget identifiers. This value is <code>null</code> if the list of
	 * defined widget identifiers did not change.
	 */
	private final String widgetId;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param menuManager
	 *            the instance of the manager that changed; must not be
	 *            <code>null</code>.
	 * @param groupId
	 *            The group identifier that was added or removed;
	 *            <code>null</code> if a group did not change.
	 * @param groupIdAdded
	 *            Whether the group identifier became defined (otherwise, it
	 *            became undefined).
	 * @param itemId
	 *            The item identifier that was added or removed;
	 *            <code>null</code> if a item did not change.
	 * @param itemIdAdded
	 *            Whether the item identifier became defined (otherwise, it
	 *            became undefined).
	 * @param menuId
	 *            The menu identifier that was added or removed;
	 *            <code>null</code> if a menu did not change.
	 * @param menuIdAdded
	 *            Whether the menu identifier became defined (otherwise, it
	 *            became undefined).
	 * @param widgetId
	 *            The widget identifier that was added or removed;
	 *            <code>null</code> if a widget did not change.
	 * @param widgetIdAdded
	 *            Whether the widget identifier became defined (otherwise, it
	 *            became undefined).
	 * @param actionSetId
	 *            The action set identifier that was added or removed;
	 *            <code>null</code> if an action set did not change.
	 * @param actionSetIdAdded
	 *            Whether the action set identifier became defined (otherwise,
	 *            it became undefined).
	 */
	MenuManagerEvent(final SMenuManager menuManager, final String groupId,
			final boolean groupIdAdded, final String itemId,
			final boolean itemIdAdded, final String menuId,
			final boolean menuIdAdded, final String widgetId,
			final boolean widgetIdAdded, final String actionSetId,
			final boolean actionSetIdAdded) {
		if (menuManager == null) {
			throw new NullPointerException(
					"An event must refer to its menu manager"); //$NON-NLS-1$
		}

		this.menuManager = menuManager;
		this.groupId = groupId;
		this.itemId = itemId;
		this.menuId = menuId;
		this.widgetId = widgetId;
		this.actionSetId = actionSetId;

		if (groupIdAdded) {
			changedValues |= CHANGED_GROUP_DEFINED;
		}
		if (itemIdAdded) {
			changedValues |= CHANGED_ITEM_DEFINED;
		}
		if (menuIdAdded) {
			changedValues |= CHANGED_MENU_DEFINED;
		}
		if (widgetIdAdded) {
			changedValues |= CHANGED_WIDGET_DEFINED;
		}
		if (actionSetIdAdded) {
			changedValues |= CHANGED_ACTION_SET_DEFINED;
		}
	}

	/**
	 * Returns the action set identifier that was added or removed.
	 * 
	 * @return The action set identifier that was added or removed; may be
	 *         <code>null</code> if no action set changed.
	 */
	public final String getActionSetId() {
		return groupId;
	}

	/**
	 * Returns the group identifier that was added or removed.
	 * 
	 * @return The group identifier that was added or removed; may be
	 *         <code>null</code> if no group changed.
	 */
	public final String getGroupId() {
		return groupId;
	}

	/**
	 * Returns the item identifier that was added or removed.
	 * 
	 * @return The item identifier that was added or removed; may be
	 *         <code>null</code> if no item changed.
	 */
	public final String getItemId() {
		return itemId;
	}

	/**
	 * Returns the menu identifier that was added or removed.
	 * 
	 * @return The menu identifier that was added or removed; may be
	 *         <code>null</code> if no menu changed.
	 */
	public final String getMenuId() {
		return menuId;
	}

	/**
	 * Returns the instance of the interface that changed.
	 * 
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public final SMenuManager getMenuManager() {
		return menuManager;
	}

	/**
	 * Returns the widget identifier that was added or removed.
	 * 
	 * @return The widget identifier that was added or removed; may be
	 *         <code>null</code> if no widget changed.
	 */
	public final String getWidgetId() {
		return widgetId;
	}

	/**
	 * Returns whether the list of defined action set identifiers has changed.
	 * 
	 * @return <code>true</code> if the list of action set identifiers has
	 *         changed; <code>false</code> otherwise.
	 */
	public final boolean isActionSetChanged() {
		return (actionSetId != null);
	}

	/**
	 * Returns whether the action set identifier became defined. Otherwise, the
	 * action set identifier became undefined.
	 * 
	 * @return <code>true</code> if the action set identifier became defined;
	 *         <code>false</code> if the action set identifier became
	 *         undefined.
	 */
	public final boolean isActionSetDefined() {
		return (((changedValues & CHANGED_ACTION_SET_DEFINED) != 0) && (actionSetId != null));
	}

	/**
	 * Returns whether the list of defined group identifiers has changed.
	 * 
	 * @return <code>true</code> if the list of group identifiers has changed;
	 *         <code>false</code> otherwise.
	 */
	public final boolean isGroupChanged() {
		return (groupId != null);
	}

	/**
	 * Returns whether the group identifier became defined. Otherwise, the group
	 * identifier became undefined.
	 * 
	 * @return <code>true</code> if the group identifier became defined;
	 *         <code>false</code> if the group identifier became undefined.
	 */
	public final boolean isGroupDefined() {
		return (((changedValues & CHANGED_GROUP_DEFINED) != 0) && (groupId != null));
	}

	/**
	 * Returns whether the list of defined item identifiers has changed.
	 * 
	 * @return <code>true</code> if the list of item identifiers has changed;
	 *         <code>false</code> otherwise.
	 */
	public final boolean isItemChanged() {
		return (itemId != null);
	}

	/**
	 * Returns whether the item identifier became defined. Otherwise, the item
	 * identifier became undefined.
	 * 
	 * @return <code>true</code> if the item identifier became defined;
	 *         <code>false</code> if the item identifier became undefined.
	 */
	public final boolean isItemDefined() {
		return (((changedValues & CHANGED_ITEM_DEFINED) != 0) && (itemId != null));
	}

	/**
	 * Returns whether the list of defined menu identifiers has changed.
	 * 
	 * @return <code>true</code> if the list of menu identifiers has changed;
	 *         <code>false</code> otherwise.
	 */
	public final boolean isMenuChanged() {
		return (menuId != null);
	}

	/**
	 * Returns whether the menu identifier became defined. Otherwise, the menu
	 * identifier became undefined.
	 * 
	 * @return <code>true</code> if the menu identifier became defined;
	 *         <code>false</code> if the menu identifier became undefined.
	 */
	public final boolean isMenuDefined() {
		return (((changedValues & CHANGED_MENU_DEFINED) != 0) && (menuId != null));
	}

	/**
	 * Returns whether the list of defined widget identifiers has changed.
	 * 
	 * @return <code>true</code> if the list of widget identifiers has
	 *         changed; <code>false</code> otherwise.
	 */
	public final boolean isWidgetChanged() {
		return (widgetId != null);
	}

	/**
	 * Returns whether the widget identifier became defined. Otherwise, the
	 * widget identifier became undefined.
	 * 
	 * @return <code>true</code> if the widget identifier became defined;
	 *         <code>false</code> if the widget identifier became undefined.
	 */
	public final boolean isWidgetDefined() {
		return (((changedValues & CHANGED_WIDGET_DEFINED) != 0) && (widgetId != null));
	}
}
