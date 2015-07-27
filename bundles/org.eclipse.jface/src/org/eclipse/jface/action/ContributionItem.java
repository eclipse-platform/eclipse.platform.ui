/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

/**
 * An abstract base implementation for contribution items.
 */
public abstract class ContributionItem implements IContributionItem {

    /**
     * The identifier for this contribution item, of <code>null</code> if none.
     */
    private String id = null;

    /**
     * Indicates this item is visible in its manager; <code>true</code>
     * by default.
     */
    private boolean visible = true;

    /**
     * The parent contribution manager for this item
     */
    private IContributionManager parent;

    /**
     * Creates a contribution item with a <code>null</code> id.
     * Calls <code>this(String)</code> with <code>null</code>.
     */
    protected ContributionItem() {
        this(null);
    }

    /**
     * Creates a contribution item with the given (optional) id.
     * The given id is used to find items in a contribution manager,
     * and for positioning items relative to other items.
     *
     * @param id the contribution item identifier, or <code>null</code>
     */
    protected ContributionItem(String id) {
        this.id = id;
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method does nothing. Subclasses may override.
     */
    @Override
	public void dispose() {
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method does nothing. Subclasses may override.
     */
    @Override
	public void fill(Composite parent) {
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method does nothing. Subclasses may override.
     */
    @Override
	public void fill(Menu menu, int index) {
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method does nothing. Subclasses may override.
     */
    @Override
	public void fill(ToolBar parent, int index) {
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method does nothing. Subclasses may override.
     *
     * @since 3.0
     */
    @Override
	public void fill(CoolBar parent, int index) {
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method does nothing. Subclasses may override.
     *
     * @since 3.0
     */
    @Override
	public void saveWidgetState() {
    }

    @Override
	public String getId() {
        return id;
    }

    /**
     * Returns the parent contribution manager, or <code>null</code> if this
     * contribution item is not currently added to a contribution manager.
     *
     * @return the parent contribution manager, or <code>null</code>
     * @since 2.0
     */
    public IContributionManager getParent() {
        return parent;
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method returns <code>false</code>. Subclasses may override.
     */
    @Override
	public boolean isDirty() {
        // @issue should this be false instead of calling isDynamic()?
        return isDynamic();
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method returns <code>true</code>. Subclasses may override.
     */
    @Override
	public boolean isEnabled() {
        return true;
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method returns <code>false</code>. Subclasses may override.
     */
    @Override
	public boolean isDynamic() {
        return false;
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method returns <code>false</code>. Subclasses may override.
     */
    @Override
	public boolean isGroupMarker() {
        return false;
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method returns <code>false</code>. Subclasses may override.
     */
    @Override
	public boolean isSeparator() {
        return false;
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method returns the value recorded in an internal state variable,
     * which is <code>true</code> by default. <code>setVisible</code>
     * should be used to change this setting.
     */
    @Override
	public boolean isVisible() {
        return visible;
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method stores the value in an internal state variable,
     * which is <code>true</code> by default.
     */
    @Override
	public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Returns a string representation of this contribution item
     * suitable only for debugging.
     */
    @Override
	public String toString() {
		return getClass().getSimpleName() + "(id=" + getId() + ", " //$NON-NLS-1$ //$NON-NLS-2$
				+ "visible=" + isVisible() + ")";//$NON-NLS-2$//$NON-NLS-1$
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * method does nothing. Subclasses may override.
     */
    @Override
	public void update() {
    }

    @Override
	public void setParent(IContributionManager parent) {
        this.parent = parent;
    }

    /**
     * The <code>ContributionItem</code> implementation of this
     * method declared on <code>IContributionItem</code> does nothing.
     * Subclasses should override to update their state.
     */
    @Override
	public void update(String id) {
    }

    /**
	 * The ID for this contribution item. It should be set once either in the
	 * constructor or using this method.
	 *
	 * @param itemId
	 * @since 3.4
	 * @see #getId()
	 */
    public void setId(String itemId) {
    	id = itemId;
    }
}
