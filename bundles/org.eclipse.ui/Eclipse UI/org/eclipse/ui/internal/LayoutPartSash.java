package org.eclipse.ui.internal;

/**********************************************************************
Copyright (c) 2000, 2001, 2002, International Business Machines Corp and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
  Cagatay Kavukcuoglu <cagatayk@acm.org> 
    - Fix for bug 10025 - Resizing views should not use height ratios
**********************************************************************/

import org.eclipse.ui.IPageLayout;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.util.*;

class LayoutPartSash extends LayoutPart {

	private Sash sash;
	private PartSashContainer rootContainer;
	private int style;

	private LayoutPartSash preLimit;
	private LayoutPartSash postLimit;

	SelectionListener selectionListener;
	private float ratio = (float)0.5;
	
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
}

// checkDragLimit contains changes by cagatayk@acm.org
private void checkDragLimit(SelectionEvent event) {
	LayoutTree root = rootContainer.getLayoutTree();
	LayoutTreeNode node = root.findSash(this);
	Rectangle bounds = node.getBounds();
	
	float minRatio = node.getMinimumRatioFor(bounds);
	float maxRatio = node.getMaximumRatioFor(bounds);

	if(style == SWT.VERTICAL) {
		if (event.x < bounds.x)
			event.x = bounds.x;
		if ((event.x + event.width) > (bounds.x + bounds.width))
			event.x = bounds.x + bounds.width - event.width;
		if (event.x - bounds.x < ((float)bounds.width * minRatio))
			event.x = bounds.x + (int)((float)bounds.width * minRatio);
		if (event.x - bounds.x > ((float)bounds.width * maxRatio))
			event.x = bounds.x + (int)((float)bounds.width * maxRatio);		
	} else {
		if (event.y < bounds.y)
			event.y = bounds.y;
		if ((event.y + event.height) > (bounds.y + bounds.height))
			event.y = bounds.y + bounds.height - event.height;
		if (event.y - bounds.y < ((float)bounds.height * minRatio))
			event.y = bounds.y + (int)((float)bounds.height * minRatio);
		if (event.y - bounds.y > ((float)bounds.height * maxRatio))
			event.y = bounds.y + (int)((float)bounds.height * maxRatio);		
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
		
	node.setBounds(node.getBounds());
}
}
