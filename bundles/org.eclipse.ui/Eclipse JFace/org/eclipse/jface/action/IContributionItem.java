package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.*;

/**
 * A contribution item represents a contribution to a shared UI resource such as a
 * menu or tool bar. More generally, contribution items are managed by a contribution
 * manager.
 * For instance, in a tool bar a contribution item is a tool bar button or a separator.
 * In a menu bar a contribution item is a menu, and in a menu a contribution item 
 * is a menu item or separator.
 * <p>
 * A contribution item can realize itself in different SWT widgets, using the different 
 * <code>fill</code> methods.  The same type of contribution item can be used with a 
 * <code>MenuBarManager</code>, <code>ToolBarManager</code>, or a <code>StatusLineManager</code>.
 * </p>
 * <p>
 * This interface is internal to the framework; it should not be implemented outside
 * the framework.
 * </p>
 *
 * @see IContributionManager
 */
public interface IContributionItem {

/**
 * Fills the given composite control with controls representing this 
 * contribution item.  Used by <code>StatusLineManager</code>.
 *
 * @param parent the parent control
 */
public void fill(Composite parent);
/**
 * Fills the given menu with controls representing this contribution item.
 * Used by <code>MenuManager</code>.
 *
 * @param parent the parent menu
 * @param index the index where the controls are inserted,
 *   or <code>-1</code> to insert at the end
 */
public void fill(Menu parent, int index);
/**
 * Fills the given tool bar with controls representing this contribution item.
 * Used by <code>ToolBarManager</code>.
 *
 * @param parent the parent tool bar
 * @param index the index where the controls are inserted,
 *   or <code>-1</code> to insert at the end
 */
public void fill(ToolBar parent, int index);
/**
 * Returns the identifier of this contribution item.
 * The id is used for retrieving an item from its manager.
 *
 * @return the contribution item identifier, or <code>null</code>
 *   if none
 */
public String getId();
/**
 * Returns whether this contribution item is dynamic. A dynamic contribution
 * item contributes items conditionally, dependent on some internal state.
 *
 * @return <code>true</code> if this item is dynamic, and
 *  <code>false</code> for normal items
 */
public boolean isDynamic();
/**
 * Returns whether this contribution item is a group marker.
 * This information is used when adding items to a group.
 *
 * @return <code>true</code> if this item is a group marker, and
 *  <code>false</code> for normal items
 *
 * @see GroupMarker
 * @see IContributionManager#appendToGroup
 * @see IContributionManager#prependToGroup
 */
public boolean isGroupMarker();
/**
 * Returns whether this contribution item is a separator.
 * This information is used to enable hiding of unnecessary separators.
 *
 * @return <code>true</code> if this item is a separator, and
 *  <code>false</code> for normal items
 * @see Separator
 */
public boolean isSeparator();
/**
 * Returns whether this contribution item is allowed to be enabled 
 * within its manager.
 * 
 * @return <code>true</code> if this item is enabled, and
 *  <code>false</code> otherwise
 * @since 2.0
 */
public boolean isEnabledAllowed();
/**
 * Sets whether this contribution item is allowed to be enabled within
 * its manager.
 *
 * @param enabledAllowed <code>true</code> if this item can be enabled, and
 *  <code>false</code> otherwise
 * @since 2.0
 */
public void setEnabledAllowed(boolean enabledAllowed);
/**
 * Returns whether this contribution item is visibile within its manager.
 *
 * @return <code>true</code> if this item is visible, and
 *  <code>false</code> otherwise
 */
public boolean isVisible();
/**
 * Sets whether this contribution item is visibile within its manager.
 *
 * @param visible <code>true</code> if this item should be visible, and
 *  <code>false</code> otherwise
 */
public void setVisible(boolean visible);
/**
 * Updates any SWT controls cached by this contribution item with any
 * changes which have been made to this contribution item since the last update.
 * Called by contribution manager update methods.
 */
public void update();
}
