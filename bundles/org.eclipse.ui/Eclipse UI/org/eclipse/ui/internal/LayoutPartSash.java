package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
private void checkDragLimit(SelectionEvent event) {
	LayoutTree root = rootContainer.getLayoutTree();
	LayoutTreeNode node = root.findSash(this);
	Rectangle bounds = node.getBounds();
	float newRatio = 0.0f;
	if(style == SWT.VERTICAL) {
		if (event.x < bounds.x)
			event.x = bounds.x;
		if ((event.x + event.width) > (bounds.x + bounds.width))
			event.x = bounds.x + bounds.width - event.width;
		if (event.x - bounds.x < ((float)bounds.width * IPageLayout.RATIO_MIN))
			event.x = bounds.x + (int)((float)bounds.width * IPageLayout.RATIO_MIN);
		if (event.x - bounds.x > ((float)bounds.width * IPageLayout.RATIO_MAX))
			event.x = bounds.x + (int)((float)bounds.width * IPageLayout.RATIO_MAX);		
	} else {
		if (event.y < bounds.y)
			event.y = bounds.y;
		if ((event.y + event.height) > (bounds.y + bounds.height))
			event.y = bounds.y + bounds.height - event.height;
		if (event.y - bounds.y < ((float)bounds.height * IPageLayout.RATIO_MIN))
			event.y = bounds.y + (int)((float)bounds.height * IPageLayout.RATIO_MIN);
		if (event.y - bounds.y > ((float)bounds.height * IPageLayout.RATIO_MAX))
			event.y = bounds.y + (int)((float)bounds.height * IPageLayout.RATIO_MAX);		
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
