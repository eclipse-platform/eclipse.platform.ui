/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Randy Hudson <hudsonr@us.ibm.com>
 *     - Fix for bug 19524 - Resizing WorkbenchWindow resizes views
 *     Cagatay Kavukcuoglu <cagatayk@acm.org>
 *     - Fix for bug 10025 - Resizing views should not use height ratios
 *     Matthew Hatem Matthew_Hatem@notesdev.ibm.com Bug 189953
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
import org.eclipse.ui.presentations.AbstractPresentationFactory;

class LayoutPartSash extends LayoutPart {

    private Sash sash;
    private boolean enabled = false;

    private PartSashContainer rootContainer;

    private int style;

    private LayoutPartSash preLimit;

    private LayoutPartSash postLimit;

    SelectionListener selectionListener;

    private int left = 300, right = 300;

    private Rectangle bounds = new Rectangle(0,0,0,0);
    
    private AbstractPresentationFactory presFactory;
    
    /**
     * Stores whether or not the sash is visible. (This is expected to have a meaningful
     * value even if the underlying control doesn't exist).
     */
    private boolean isVisible;
    
    LayoutPartSash(PartSashContainer rootContainer, int style) {
        super(null);
        this.style = style;
        this.rootContainer = rootContainer;

        selectionListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) { 
                checkDragLimit(e);
                
                if (e.detail != SWT.DRAG) {    
                    LayoutPartSash.this.widgetSelected(e.x, e.y, e.width,
                            e.height);
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
    	left = Math.min(left, nodeBounds.width - getSashSize()); 
    	int right = nodeBounds.width - left - getSashSize();
    	
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
     * Creates the control. As an optimization, creation of the control is deferred if
     * the control is invisible.
     */
    public void createControl(Composite parent) {
        // Defer creation of the control until it becomes visible
        if (isVisible) {
            doCreateControl();
        }
    }
    
    /**
     * Creates the underlying SWT control.
     * 
     * @since 3.1
     */
    private void doCreateControl() {         
        if (sash == null) {        	
        	// ask the presentation factory to create the sash
        	AbstractPresentationFactory factory = getPresentationFactory();
        	
        	int sashStyle = AbstractPresentationFactory.SASHTYPE_NORMAL | style;
            sash = factory.createSash(this.rootContainer.getParent(), sashStyle);
            
            sash.addSelectionListener(selectionListener);
            sash.setEnabled(enabled);
            sash.setBounds(bounds);
        }
    }
    
    public void setBounds(Rectangle r) {
        super.setBounds(r);
        
        bounds = r;
    }

    /**
     * Makes the sash visible or invisible. Note: as an optimization, the actual widget is destroyed when the
     * sash is invisible.
     */
    public void setVisible(boolean visible) {
        if (visible == isVisible) {
            return;
        }
        
        if (visible) {
            doCreateControl();
        } else {
            dispose();
        }
        
        super.setVisible(visible);
        
        isVisible = visible;
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    /**
     * See LayoutPart#dispose
     */
    public void dispose() {

        if (sash != null) {
            bounds = sash.getBounds();
            sash.dispose();
        }
        sash = null;
    }

    /**
     * Gets the presentation bounds.
     */
    public Rectangle getBounds() {
        if (sash == null) {
            return bounds;
        }

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
        if (!enabled) {
            return;
        }
        
        LayoutTree root = rootContainer.getLayoutTree();
        LayoutTreeNode node = root.findSash(this);
        Rectangle nodeBounds = node.getBounds();
        //Recompute ratio
        x -= nodeBounds.x;
        y -= nodeBounds.y;
        if (style == SWT.VERTICAL) {
            setSizes(x, nodeBounds.width - x - getSashSize());
        } else {
            setSizes(y, nodeBounds.height - y - getSashSize());
        }

        node.setBounds(nodeBounds);        
    }

    /**
     * @param resizable
     * @since 3.1
     */
    public void setEnabled(boolean resizable) {
        this.enabled = resizable;
        if (sash != null) {
            sash.setEnabled(enabled);
        }
    }
    
    /* package */ int getSashSize() {
    	AbstractPresentationFactory factory = getPresentationFactory();
    	int sashStyle = AbstractPresentationFactory.SASHTYPE_NORMAL | style;
    	int size = factory.getSashSize(sashStyle);
    	return size;
    }
    
    private AbstractPresentationFactory getPresentationFactory() {
    	if (presFactory == null) {
	    	WorkbenchWindow wbw = (WorkbenchWindow)rootContainer.getPage().getWorkbenchWindow();
	    	WorkbenchWindowConfigurer configurer = wbw.getWindowConfigurer();
	        presFactory = configurer.getPresentationFactory();
    	}
        return presFactory;
    }

}
