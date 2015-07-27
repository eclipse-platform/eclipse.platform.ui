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
 * A <code>SubContributionItem</code> is a wrapper for an <code>IContributionItem</code>.
 * It is used within a <code>SubContributionManager</code> to control the visibility
 * of items.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SubContributionItem implements IContributionItem {
    /**
     * The visibility of the item.
     */
    private boolean visible;

    /**
     * The inner item for this contribution.
     */
    private IContributionItem innerItem;

    /**
     * Creates a new <code>SubContributionItem</code>.
     * @param item the contribution item to be wrapped
     */
    public SubContributionItem(IContributionItem item) {
        innerItem = item;
    }

    /**
     * The default implementation of this <code>IContributionItem</code>
     * delegates to the inner item. Subclasses may override.
     */
    @Override
	public void dispose() {
        innerItem.dispose();
    }

    @Override
	public void fill(Composite parent) {
        if (visible) {
			innerItem.fill(parent);
		}
    }

    @Override
	public void fill(Menu parent, int index) {
        if (visible) {
			innerItem.fill(parent, index);
		}
    }

    @Override
	public void fill(ToolBar parent, int index) {
        if (visible) {
			innerItem.fill(parent, index);
		}
    }

    @Override
	public String getId() {
        return innerItem.getId();
    }

    /**
     * Returns the inner contribution item.
     *
     * @return the inner contribution item
     */
    public IContributionItem getInnerItem() {
        return innerItem;
    }

    @Override
	public boolean isEnabled() {
        return innerItem.isEnabled();
    }

    @Override
	public boolean isDirty() {
        return innerItem.isDirty();
    }

    @Override
	public boolean isDynamic() {
        return innerItem.isDynamic();
    }

    @Override
	public boolean isGroupMarker() {
        return innerItem.isGroupMarker();
    }

    @Override
	public boolean isSeparator() {
        return innerItem.isSeparator();
    }

    @Override
	public boolean isVisible() {
        return visible && innerItem.isVisible();
    }

    @Override
	public void setParent(IContributionManager parent) {
        // do nothing, the parent of our inner item
        // is its SubContributionManager
    }

    @Override
	public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
	public void update() {
        innerItem.update();
    }

    @Override
	public void update(String id) {
        innerItem.update(id);
    }

    @Override
	public void fill(CoolBar parent, int index) {
        if (visible) {
			innerItem.fill(parent, index);
		}
    }

    @Override
	public void saveWidgetState() {
    }

}
