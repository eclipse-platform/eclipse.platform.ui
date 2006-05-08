/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.TrimUtil;

/**
 * The ProgressCanvasViewer is the viewer used by progress windows. It displays text
 * on the canvas.
 */
public class ProgressCanvasViewer extends AbstractProgressViewer {
    Canvas canvas;

    Object[] displayedItems = new Object[0];

    private final static List EMPTY_LIST = new ArrayList();

    /**
     * Font metrics to use for determining pixel sizes.
     */
    private FontMetrics fontMetrics;

    private int numShowItems = 1;

    private int maxCharacterWidth;

	private int orientation = SWT.HORIZONTAL;

    /**
     * Create a new instance of the receiver with the supplied
     * parent and style bits.
     * @param parent The composite the Canvas is created in
     * @param style style bits for the canvas
     * @param itemsToShow the number of items this will show
     * @param numChars The number of characters for the width hint.
     * @param side the side to display text, this helps determine horizontal vs vertical
     */
    ProgressCanvasViewer(Composite parent, int style, int itemsToShow, int numChars, int orientation) {
        super();
        this.orientation = orientation;
        numShowItems = itemsToShow;
        maxCharacterWidth = numChars;
        canvas = new Canvas(parent, style);
        hookControl(canvas);
        // Compute and store a font metric
        GC gc = new GC(canvas);
        gc.setFont(JFaceResources.getDefaultFont());
        fontMetrics = gc.getFontMetrics();
        gc.dispose();
        initializeListeners();
    }

    /**
     * NE: Copied from ContentViewer.  We don't want the OpenStrategy hooked
     * in StructuredViewer.hookControl otherwise the canvas will take focus
     * since it has a key listener.  We don't want this included in the window's
     * tab traversal order.  Defeating it here is more self-contained then 
     * setting the tab list on the shell or other parent composite.
     */
    protected void hookControl(Control control) {
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent event) {
                handleDispose(event);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.StructuredViewer#doFindInputItem(java.lang.Object)
     */
    protected Widget doFindInputItem(Object element) {
        return null; // No widgets associated with items
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.StructuredViewer#doFindItem(java.lang.Object)
     */
    protected Widget doFindItem(Object element) {
        return null; // No widgets associated with items
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.StructuredViewer#doUpdateItem(org.eclipse.swt.widgets.Widget,
     *      java.lang.Object, boolean)
     */
    protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
        canvas.redraw();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.StructuredViewer#getSelectionFromWidget()
     */
    protected List getSelectionFromWidget() {
        //No selection on a Canvas
        return EMPTY_LIST;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.StructuredViewer#internalRefresh(java.lang.Object)
     */
    protected void internalRefresh(Object element) {
        displayedItems = getSortedChildren(getRoot());
        canvas.redraw();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.StructuredViewer#reveal(java.lang.Object)
     */
    public void reveal(Object element) {
        //Nothing to do here as we do not scroll
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.StructuredViewer#setSelectionToWidget(java.util.List,
     *      boolean)
     */
    protected void setSelectionToWidget(List l, boolean reveal) {
        //Do nothing as there is no selection
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.Viewer#getControl()
     */
    public Control getControl() {
        return canvas;
    }

    private void initializeListeners() {
        canvas.addPaintListener(new PaintListener() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
             */
            public void paintControl(PaintEvent event) {

                GC gc = event.gc;
                Transform transform = null;
                if (orientation == SWT.VERTICAL) {
	                transform = new Transform(event.display);
	            	transform.translate(TrimUtil.TRIM_DEFAULT_HEIGHT, 0);
	            	transform.rotate(90);
                }
                ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();

                int itemCount = Math.min(displayedItems.length, numShowItems);

                int yOffset = 0;
                int xOffset = 0;
                if (numShowItems == 1) {//If there is a single item try to center it
                    Rectangle clientArea = canvas.getParent().getClientArea();
                    if (orientation == SWT.HORIZONTAL) {
                    	int size = clientArea.height;
	                    yOffset = size - (fontMetrics.getHeight());
	                    yOffset = yOffset / 2;
                    } else {
                    	int size = clientArea.width;
                    	xOffset = size - (fontMetrics.getHeight());
                    	xOffset = xOffset / 2;
                    }
                }

                for (int i = 0; i < itemCount; i++) {
                    String string = labelProvider.getText(displayedItems[i]);
                    if(string == null) {
						string = "";//$NON-NLS-1$
					}
                    if (orientation == SWT.HORIZONTAL) {
                    	gc.drawString(string, 2, yOffset + (i * fontMetrics.getHeight()), true);
                    } else {
		            	gc.setTransform(transform);
                    	gc.drawString(string, xOffset + (i * fontMetrics.getHeight()), 2, true);
                    }
                }
                if (transform != null)
                	transform.dispose();
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ContentViewer#setLabelProvider(org.eclipse.jface.viewers.IBaseLabelProvider)
     */
    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        Assert.isTrue(labelProvider instanceof ILabelProvider);
        super.setLabelProvider(labelProvider);
    }

    /**
     * Get the size hints for the receiver. These are used for 
     * layout data.
     * @return Point - the preferred x and y coordinates
     */
    public Point getSizeHints() {

        Display display = canvas.getDisplay();

        GC gc = new GC(canvas);
        FontMetrics fm = gc.getFontMetrics();
        int charWidth = fm.getAverageCharWidth();
        int charHeight = fm.getHeight();
        int maxWidth = display.getBounds().width / 2;
        int maxHeight = display.getBounds().height / 6;
        int fontWidth = charWidth * maxCharacterWidth;
        int fontHeight = charHeight * numShowItems;
        if (maxWidth < fontWidth) {
			fontWidth = maxWidth;
		}
        if (maxHeight < fontHeight) {
			fontHeight = maxHeight;
		}
        gc.dispose();
        return new Point(fontWidth, fontHeight);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.AbstractProgressViewer#add(java.lang.Object[])
	 */
	public void add(Object[] elements) {
		refresh(true);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.AbstractProgressViewer#remove(java.lang.Object[])
	 */
	public void remove(Object[] elements) {
		refresh(true);
		
	}


}
