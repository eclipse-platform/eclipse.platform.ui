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
package org.eclipse.ui.internal.presentations.defaultpresentation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.presentations.util.AbstractTabFolder;
import org.eclipse.ui.internal.presentations.util.AbstractTabItem;
import org.eclipse.ui.internal.presentations.util.EnhancedFillLayout;
import org.eclipse.ui.internal.presentations.util.PartInfo;

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
    private Color borderColor;
    
    public EmptyTabFolder(Composite parent, boolean showborder) {
        control = new Composite(parent, SWT.NONE);
        EnhancedFillLayout layout = new EnhancedFillLayout();
        control.setLayout(layout);
        borderColor = parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
        if (showborder) {
            layout.xmargin = 1;
            layout.ymargin = 1;
	        control.addPaintListener(new PaintListener() {
	        	public void paintControl(PaintEvent e) {
	        		e.gc.setForeground(borderColor);
	        		Rectangle rect = control.getClientArea();
	        		rect.width--;
	        		rect.height--;
	        		e.gc.drawRectangle(rect);
	        	}
	        });
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#computeSize(int, int)
     */
    public Point computeSize(int widthHint, int heightHint) {
        if (childControl != null) {
        	// Fix for bug 85899 [Perspectives] [RCP]Standalone view does not divide space in 
        	//   proper ratio with reference when added to IPageLayout with showTitle parameter false
        	// If the childControl is a content proxy (a Composite with no children -- see PresentablePartFolder constructor),
        	// then computeSize returns 64x64, which is inappropriate.
        	// Note that this happens even if the Composite has a Layout that returns 0@0.
        	// Instead, use the sizes of the margins.  This is equivalent to calling control.computeSize(...)
        	// if the childControl returned 0@0 for its preferred size.
        	if (childControl instanceof Composite) {
        		Composite composite = (Composite) childControl;
        		if (composite.getChildren().length == 0) {
        			EnhancedFillLayout layout = (EnhancedFillLayout) control.getLayout();
        			int w = widthHint == SWT.DEFAULT ? layout.xmargin * 2 : widthHint;
        			int h = heightHint == SWT.DEFAULT ? layout.ymargin * 2 : heightHint;
        			return new Point(w, h);
        		}
        	}
            return childControl.computeSize(widthHint, heightHint);
        }
        return new Point(0,0);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#add(int, int)
     */
    public AbstractTabItem add(int index, int flags) {
        return new EmptyTabItem();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getContentParent()
     */
    public Composite getContentParent() {
        return control;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#setContent(org.eclipse.swt.widgets.Control)
     */
    public void setContent(Control newContent) {
        childControl = newContent;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getItems()
     */
    public AbstractTabItem[] getItems() {
        return new AbstractTabItem[0];
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getSelection()
     */
    public AbstractTabItem getSelection() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#setSelection(org.eclipse.ui.internal.presentations.util.AbstractTabItem)
     */
    public void setSelection(AbstractTabItem toSelect) {

    }

    public void setToolbar(Control toolbar) {
        if (toolbar != null) {
            toolbar.setVisible(false);
        }
    }
    
    public void layout(boolean flushCache) {
        super.layout(flushCache);
        
        control.layout(flushCache);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#setSelectedInfo(org.eclipse.ui.internal.presentations.util.PartInfo)
     */
    public void setSelectedInfo(PartInfo info) {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#enablePaneMenu(boolean)
     */
    public void enablePaneMenu(boolean enabled) {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getToolbarParent()
     */
    public Composite getToolbarParent() {
        return control;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getControl()
     */
    public Control getControl() {
        return control;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.AbstractTabFolder#getTabArea()
     */
    public Rectangle getTabArea() {
        return new Rectangle(0,0,0,0);
    }
}
