package org.eclipse.ui.internal;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *    IBM Corporation - initial API and implementation 
 *    Cagatay Kavukcuoglu <cagatayk@acm.org>
 *      - Fix for bug 10025 - Resizing views should not use height ratios
**********************************************************************/

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

class LayoutPartSash extends LayoutPart {

	private Sash sash;
	private PartSashContainer rootContainer;
	private int style;

	private LayoutPartSash preLimit;
	private LayoutPartSash postLimit;

	SelectionListener selectionListener;
	private float ratio = 0.5f;
	
	/* Optimize limit checks by calculating minimum 
	 * and maximum ratios once per drag
	 */
	private float minRatio;
	private float maxRatio;

	
	
LayoutPartSash(PartSashContainer rootContainer,int style) {
	super(null);
	this.style = style;
	this.rootContainer = rootContainer;

	selectionListener = new SelectionAdapter () {
		public void widgetSelected(SelectionEvent e) {
			if (e.detail == SWT.DRAG)
				checkDragLimit(e);
			else
				LayoutPartSash.this.widgetSelected(e.x, e.y, e.width, e.height);
		}
	};

	initDragRatios();
}

// checkDragLimit contains changes by cagatayk@acm.org
private void checkDragLimit(SelectionEvent event) {
	LayoutTree root = rootContainer.getLayoutTree();
	LayoutTreeNode node = root.findSash(this);
	Rectangle nodeBounds = node.getBounds();
	
	// optimization: compute ratios only once per drag
	if (minRatio < 0)
		minRatio = node.getMinimumRatioFor(nodeBounds);
	if (maxRatio < 0)
		maxRatio = node.getMaximumRatioFor(nodeBounds);

	if(style == SWT.VERTICAL) {
		// limit drag to current node's bounds
		if (event.x < nodeBounds.x)
			event.x = nodeBounds.x;
		if ((event.x + event.width) > (nodeBounds.x + nodeBounds.width))
			event.x = nodeBounds.x + nodeBounds.width - event.width;
		// limit drag to current node's ratios
		float width = nodeBounds.width;
		if (event.x - nodeBounds.x < width * minRatio)
			event.x = nodeBounds.x + (int)(width * minRatio);
		if (event.x - nodeBounds.x > width * maxRatio)
			event.x = nodeBounds.x + (int)(width * maxRatio);
	} else {
		// limit drag to current node's bounds
		if (event.y < nodeBounds.y)
			event.y = nodeBounds.y;
		if ((event.y + event.height) > (nodeBounds.y + nodeBounds.height))
			event.y = nodeBounds.y + nodeBounds.height - event.height;
		// limit drag to current node's ratios
		float height = nodeBounds.height;
		if (event.y - nodeBounds.y < height * minRatio)
			event.y = nodeBounds.y + (int)(height * minRatio);
		if (event.y - nodeBounds.y > height * maxRatio)
			event.y = nodeBounds.y + (int)(height * maxRatio);
	}
}

/**
 * Creates the control
 */
public void createControl(Composite parent) {
	if (sash == null) {
		sash = new Sash(parent, style);
		sash.addListener(SWT.MouseDown, rootContainer.getMouseDownListener());
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
	if(sash == null)
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
float getRatio() {
	return ratio;
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
	if (newRatio < 0.0 || newRatio > 1.0) return;
	ratio = newRatio;
}
/**
 * @see IPartDropTarget::targetPartFor
 */
public LayoutPart targetPartFor(LayoutPart dragSource) {
	return null;
}
private void widgetSelected(int x, int y, int width, int height) {
	LayoutTree root = rootContainer.getLayoutTree();
	LayoutTreeNode node = root.findSash(this);
	Rectangle nodeBounds = node.getBounds();
	//Recompute ratio
	if(style == SWT.VERTICAL) {
		setRatio((float)(x - nodeBounds.x)/(float)nodeBounds.width);
	} else {
		setRatio((float)(y - nodeBounds.y)/(float)nodeBounds.height);
	}
		
	node.setBounds(nodeBounds);
	initDragRatios();
}


private void initDragRatios() {
	minRatio = maxRatio = -1f;
}


}
