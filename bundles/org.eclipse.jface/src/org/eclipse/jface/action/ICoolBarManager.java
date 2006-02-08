/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.action;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;

/**
 * The <code>ICoolBarManager</code> interface provides protocol for managing
 * contributions to a cool bar. A cool bar manager delegates responsibility for
 * creating child controls to its contribution items by calling
 * {@link IContributionItem#fill(CoolBar, int)}.
 * <p>
 * This interface is internal to the framework; it should not be implemented
 * outside the framework. This package provides a concrete cool bar manager
 * implementation, {@link CoolBarManager}, which
 * clients may instantiate or subclass.
 * </p>
 * 
 * @see ToolBarContributionItem
 * @since 3.0
 */
public interface ICoolBarManager extends IContributionManager {

    /**
     * Property name of a cool item's size (value <code>"size"</code>).
     * <p>
     * The cool bar manager uses this property to tell its cool items to update
     * their size.
     * </p>
     * 
     * @see IContributionItem#update(String) @issue consider declaring this
     *      constant elsewhere
     */
    public static final String SIZE = "size"; //$NON-NLS-1$

    /**
     * A convenience method to add a tool bar as a contribution item to this
     * cool bar manager. Equivalent to <code>add(new ToolBarContributionManager(toolBarManager))</code>.
     * 
     * @param toolBarManager
     *            the tool bar manager to be added
     * @see ToolBarContributionItem
     */
    public void add(IToolBarManager toolBarManager);
    
    /**
     * Creates and returns this manager's cool bar control. Does not create a
     * new control if one already exists.
     * 
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is a guarantee neither that this API will
	 * work nor that it will remain the same. Please do not use this API without
	 * consulting with the Platform/UI team.
	 * </p>
     * 
     * @param parent
     *            the parent control
     * @return the cool bar control
	 * @since 3.2
     */
    public CoolBar createControl(Composite parent);

    /**
     * Creates and returns this manager's control. Does not create a
     * new control if one already exists.
     * 
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is a guarantee neither that this API will
	 * work nor that it will remain the same. Please do not use this API without
	 * consulting with the Platform/UI team.
	 * </p>
     * 
     * @param parent
     *            the parent control
     * @return the control
	 * @since 3.2
     */
    public Control createControl2(Composite parent);

    /**
     * Returns the context menu manager used by this cool bar manager. This
     * context menu manager is used by the cool bar manager except for cool
     * items that provide their own.
     * 
     * @return the context menu manager, or <code>null</code> if none
     * @see #setContextMenuManager
     */
    public IMenuManager getContextMenuManager();

    /**
     * Returns the cool bar control for this manager.
     * 
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is a guarantee neither that this API will
	 * work nor that it will remain the same. Please do not use this API without
	 * consulting with the Platform/UI team.
	 * </p>
     * 
     * @return the cool bar control, or <code>null</code> if none
	 * @since 3.2
     */
    public CoolBar getControl();
    
    /**
     * Returns the bar control for this manager.
     * 
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is a guarantee neither that this API will
	 * work nor that it will remain the same. Please do not use this API without
	 * consulting with the Platform/UI team.
	 * </p>
     *  
     * @return the bar control, or <code>null</code> if none
	 * @since 3.2
     */
    public Control getControl2();

    /**
     * Returns whether the layout of the underlying cool bar widget is locked.
     * 
     * @return <code>true</code> if cool bar layout is locked, <code>false</code>
     *         otherwise
     */
    public boolean getLockLayout();

    /**
     * Returns the style of the underlying cool bar widget.
     * 
     * @return the style of the cool bar
     */
    public int getStyle();

    /**
     * Sets the context menu of this cool bar manager to the given menu
     * manager.
     * 
     * @param menuManager
     *            the context menu manager, or <code>null</code> if none
     * @see #getContextMenuManager
     */
    public void setContextMenuManager(IMenuManager menuManager);

    /**
     * Synchronizes the visual order of the cool items in the control with this
     * manager's internal data structures. This method should be called before
     * requesting the order of the contribution items to ensure that the order
     * is accurate.
     * <p>
     * Note that <code>update()</code> and <code>refresh()</code> are
     * converses: <code>update()</code> changes the visual order to match the
     * internal structures, and <code>refresh</code> changes the internal
     * structures to match the visual order.
     * </p>
     * 
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is a guarantee neither that this API will
	 * work nor that it will remain the same. Please do not use this API without
	 * consulting with the Platform/UI team.
	 * </p>
     * 
	 * @since 3.2
     */
    public void refresh();
    
    /**
	 * Disposes the resources for this manager.
     * 
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is a guarantee neither that this API will
	 * work nor that it will remain the same. Please do not use this API without
	 * consulting with the Platform/UI team.
	 * </p>
     * 
     * @since 3.2 
     */
    public void dispose();

    /**
     * Restores the canonical order of this cool bar manager. The canonical
     * order is the order in which the contribution items where added.
     * 
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is a guarantee neither that this API will
	 * work nor that it will remain the same. Please do not use this API without
	 * consulting with the Platform/UI team.
	 * </p>
     * 
	 * @since 3.2     
	 */
    public void resetItemOrder();

    /**
     * Replaces the current items with the given items.
     * Forces an update.
     * 
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is a guarantee neither that this API will
	 * work nor that it will remain the same. Please do not use this API without
	 * consulting with the Platform/UI team.
	 * </p>
     * 
     * @param newItems the items with which to replace the current items
     * @since 3.2
	 */
    public void setItems(IContributionItem[] newItems);
	
    /**
     * Locks or unlocks the layout of the underlying cool bar widget. Once the
     * cool bar is locked, cool items cannot be repositioned by the user.
     * <p>
     * Note that items can be added or removed programmatically even while the
     * cool bar is locked.
     * </p>
     * 
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is a guarantee neither that this API will
	 * work nor that it will remain the same. Please do not use this API without
	 * consulting with the Platform/UI team.
	 * </p>
     *  
     * @param value
     *            <code>true</code> to lock the cool bar, <code>false</code>
     *            to unlock
     */
    public void setLockLayout(boolean value);

}
