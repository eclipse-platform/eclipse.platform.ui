/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Randy Hudson <hudsonr@us.ibm.com>
 *     - Fix for bug 19524 - Resizing WorkbenchWindow resizes views
 *     Cagatay Kavukcuoglu <cagatayk@acm.org>
 *     - Fix for bug 10025 - Resizing views should not use height ratios
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Sash;

class LayoutPartSash extends LayoutPart {

    private Sash sash;

    private PartSashContainer rootContainer;

    private int style;

    private LayoutPartSash preLimit;

    private LayoutPartSash postLimit;

    SelectionListener selectionListener;

    private int left = 300, right = 300;
    
    private int scheduledUpdates = 0;
    
    private Runnable updateCounterJob = new Runnable() {

        public void run() {
            scheduledUpdates--;
        }
        
    };
    
    LayoutPartSash(PartSashContainer rootContainer, int style) {
        super(null);
        this.style = style;
        this.rootContainer = rootContainer;

        selectionListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) { 
                checkDragLimit(e);
                
                // Ensure that we don't wear out the poor CPU -- if
                // we have more than 5 updates scheduled at once then
                // stop updating the views with the sash.
                // This will look ugly (the views will stay still until the user
                // moves the mouse) but it ensures that we never
                // make the UI unusable by causing too many redraws.
                if (e.detail != SWT.DRAG || (scheduledUpdates <= 5)) {
                    scheduledUpdates++;
                    
                    LayoutPartSash.this.widgetSelected(e.x, e.y, e.width,
                            e.height);
                    
                    sash.getDisplay().asyncExec(updateCounterJob);
                }
            }
        };
    }

    // checkDragLimit contains changes by cagatayk@acm.org
    private void checkDragLimit(SelectionEvent event) {
        LayoutTree root = rootContainer.getLayoutTree();
        LayoutTreeNode node = root.findSash(this);
        Rectangle nodeBounds = node.getBounds();
    	Rectangle eventRect = new Rectangle(event.x, event.y, event.width, event.height);
		
        boolean vertical = (style == SWT.VERTICAL);
        
        // If a horizontal sash, flip the coordinate system so that we
        // can handle horizontal and vertical sashes without special cases
        if (!vertical) {
        	Geometry.flipXY(nodeBounds);
        	Geometry.flipXY(eventRect);
        }

    	int eventX = eventRect.x;
    	int left = Math.max(0, eventX - nodeBounds.x);
    	left = Math.min(left, nodeBounds.width); 
    	int right = nodeBounds.width - left - LayoutTreeNode.SASH_WIDTH;
    	
    	LayoutTreeNode.ChildSizes sizes = node.computeChildSizes(nodeBounds.width, nodeBounds.height, left, right, nodeBounds.width);

        eventRect.x = nodeBounds.x + sizes.left;
        
        // If it's a horizontal sash, restore eventRect to its original coordinate system
        if (!vertical) {
        	Geometry.flipXY(eventRect);
        }
        
        event.x = eventRect.x;
        event.y = eventRect.y;        
    }

    /**
     * Creates the control
     */
    public void createControl(Composite parent) {
        if (sash == null) {
            sash = new Sash(parent, style);
            sash.addSelectionListener(selectionListener);
        }
    }

    /**
     * See LayoutPart#dispose
     */
    public void dispose() {

        if (sash != null)
            sash.dispose();
        sash = null;
    }

    /**
     * Gets the presentation bounds.
     */
    public Rectangle getBounds() {
        if (sash == null)
            return super.getBounds();
        return sash.getBounds();
    }

    /**
     * Returns the part control.
     */
    public Control getControl() {
        return sash;
    }

    /**
     *  
     */
    public String getID() {
        return null;
    }

    LayoutPartSash getPostLimit() {
        return postLimit;
    }

    LayoutPartSash getPreLimit() {
        return preLimit;
    }

    int getLeft() {
        return left;
    }

    int getRight() {
        return right;
    }

    boolean isHorizontal() {
        return ((style & SWT.HORIZONTAL) == SWT.HORIZONTAL);
    }

    boolean isVertical() {
        return ((style & SWT.VERTICAL) == SWT.VERTICAL);
    }

    void setPostLimit(LayoutPartSash newPostLimit) {
        postLimit = newPostLimit;
    }

    void setPreLimit(LayoutPartSash newPreLimit) {
        preLimit = newPreLimit;
    }

    void setRatio(float newRatio) {
        int total = left + right;
        int newLeft = (int) (total * newRatio);
        setSizes(newLeft, total - newLeft);
    }

    void setSizes(int left, int right) {
        if (left < 0 || right < 0) {
            return;
        }
        
        if (left == this.left && right == this.right) {
            return;
        }
        
        this.left = left;
        this.right = right;
        
        flushCache();
    }
    
    private void flushCache() {
        LayoutTree root = rootContainer.getLayoutTree();

        if (root != null) {
	        LayoutTreeNode node = root.findSash(this);
	        if (node != null) {
	            node.flushCache();
	        }
        }
    }

    private void widgetSelected(int x, int y, int width, int height) {
        LayoutTree root = rootContainer.getLayoutTree();
        LayoutTreeNode node = root.findSash(this);
        Rectangle nodeBounds = node.getBounds();
        //Recompute ratio
        x -= nodeBounds.x;
        y -= nodeBounds.y;
        if (style == SWT.VERTICAL) {
            setSizes(x, nodeBounds.width - x - LayoutTreeNode.SASH_WIDTH);
        } else {
            setSizes(y, nodeBounds.height - y - LayoutTreeNode.SASH_WIDTH);
        }

        node.setBounds(nodeBounds);        
    }

}