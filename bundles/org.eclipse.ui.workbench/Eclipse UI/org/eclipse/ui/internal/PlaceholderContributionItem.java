/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;


/**
 * @since 3.0
 */
public final class PlaceholderContributionItem implements IContributionItem {

    private final String id;
    
    /**
     * 
     */
    public PlaceholderContributionItem(String identifier) {
        id = id;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#dispose()
     */
    public void dispose() {
        // Do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.Composite)
     */
    public void fill(Composite parent) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.Menu, int)
     */
    public void fill(Menu parent, int index) {
        throw new UnsupportedOperationException();

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.ToolBar, int)
     */
    public void fill(ToolBar parent, int index) {
        throw new UnsupportedOperationException();

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.CoolBar, int)
     */
    public void fill(CoolBar parent, int index) {
        throw new UnsupportedOperationException();

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#getId()
     */
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#isEnabled()
     */
    public boolean isEnabled() {
        // XXX Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#isDirty()
     */
    public boolean isDirty() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#isDynamic()
     */
    public boolean isDynamic() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#isGroupMarker()
     */
    public boolean isGroupMarker() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#isSeparator()
     */
    public boolean isSeparator() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#isVisible()
     */
    public boolean isVisible() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#saveWidgetState()
     */
    public void saveWidgetState() {
        // Do nothing.

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#setParent(org.eclipse.jface.action.IContributionManager)
     */
    public void setParent(IContributionManager parent) {
        // Do nothing

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        throw new UnsupportedOperationException();

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#update()
     */
    public void update() {
        update(null);

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#update(java.lang.String)
     */
    public void update(String id) {
        // Do nothing

    }

}
