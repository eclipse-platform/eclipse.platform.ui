/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.defaultpresentation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder;
import org.eclipse.ui.internal.presentations.newapi.AbstractTabItem;
import org.eclipse.ui.internal.presentations.newapi.EnhancedFillLayout;
import org.eclipse.ui.internal.presentations.newapi.PartInfo;

/**
 * Implements the AbstractTabFolder interface, however this object only displays
 * the content of the currently selected part. There are no tabs, no title, no toolbar,
 * etc. There is no means to select a different part, unless it is done programmatically.
 * 
 * @since 3.1
 */
public class EmptyTabFolder extends AbstractTabFolder {

    private Composite control;
    private Control childControl;
    
    public EmptyTabFolder(Composite parent) {
        control = new Composite(parent, SWT.NONE);
        control.setLayout(new EnhancedFillLayout());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#computeSize(int, int)
     */
    public Point computeSize(int widthHint, int heightHint) {
        if (childControl != null) {
            return childControl.computeSize(widthHint, heightHint);
        }
        return new Point(0,0);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#add(int, int)
     */
    public AbstractTabItem add(int index, int flags) {
        return new EmptyTabItem();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#getContentParent()
     */
    public Composite getContentParent() {
        return control;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#setContent(org.eclipse.swt.widgets.Control)
     */
    public void setContent(Control newContent) {
        childControl = newContent;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#getItems()
     */
    public AbstractTabItem[] getItems() {
        return new AbstractTabItem[0];
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#getSelection()
     */
    public AbstractTabItem getSelection() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#setSelection(org.eclipse.ui.internal.presentations.newapi.AbstractTabItem)
     */
    public void setSelection(AbstractTabItem toSelect) {

    }

    public void setToolbar(Control toolbar) {
        if (toolbar != null) {
            toolbar.setVisible(false);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#setSelectedInfo(org.eclipse.ui.internal.presentations.newapi.PartInfo)
     */
    public void setSelectedInfo(PartInfo info) {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#enablePaneMenu(boolean)
     */
    public void enablePaneMenu(boolean enabled) {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#getToolbarParent()
     */
    public Composite getToolbarParent() {
        return control;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#getControl()
     */
    public Control getControl() {
        return control;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.AbstractTabFolder#getTabArea()
     */
    public Rectangle getTabArea() {
        return new Rectangle(0,0,0,0);
    }
}
