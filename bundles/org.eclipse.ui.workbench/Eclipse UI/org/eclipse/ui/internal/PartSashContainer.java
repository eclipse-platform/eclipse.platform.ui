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

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.*;
import java.util.*;
import org.eclipse.ui.*;

/**
 * Abstract container that groups various layout
 * parts (possibly other containers) together as
 * a unit. Manages the placement and size of these
 * layout part based on the location of sashes within
 * the container.
 */
public abstract class PartSashContainer extends LayoutPart implements ILayoutContainer {
 
	protected Composite parent;
	protected ControlListener resizeListener;
	protected LayoutTree root;
	protected LayoutTree unzoomRoot;
	protected Listener mouseDownListener;
	boolean active = false;
	
	/* Array of LayoutPart */
	protected ArrayList children = new ArrayList(); 
	
	protected static class RelationshipInfo {
		protected LayoutPart part;
		protected LayoutPart relative;
		protected int relationship;
		protected float ratio;
	}
	
public PartSashContainer(String id,final WorkbenchPage page) {
	super(id);
	resizeListener = new ControlAdapter() {
		public void controlResized(ControlEvent e) {
			resizeSashes(parent.getClientArea());
		}
	};
	// Mouse down listener to hide fast view when
	// user clicks on empty editor area or sashes.
	mouseDownListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.type == SWT.MouseDown)
				page.toggleFastView(null);
		}
	};	
}
/**
 * Find the sashs around the specified part.
 */
public void findSashes(LayoutPart pane,PartPane.Sashes sashes) {
	LayoutTree part = root.find(pane);
	if(part == null)
		return;
	part.findSashes(sashes);
}
/**
 * Add a part.
 */
public void add(LayoutPart child) {
	if (isZoomed())
		zoomOut();
		
	if (child == null)
		return;
	
	RelationshipInfo info = new RelationshipInfo();
	info.part = child;
	if(root != null) {
		findPosition(info);
	}
	addChild(info);
}
/**
 * Add on part relative to another
 */
public void add(LayoutPart child, int relationship, float ratio, LayoutPart relative) {
	if (isZoomed())
		zoomOut();

	if (child == null)
		return;
	if (relative != null && !isChild(relative))
		return;
	if (relationship < IPageLayout.LEFT || relationship > IPageLayout.BOTTOM)
		relationship = IPageLayout.LEFT;

	// store info about relative positions
	RelationshipInfo info = new RelationshipInfo();
	info.part = child;
	info.relationship = relationship;
	info.ratio = ratio;
	info.relative = relative;
	addChild(info);
}
private void addChild(RelationshipInfo info) {
	LayoutPart child = info.part;
	children.add(child);
	
	if(root == null) {
		root = new LayoutTree(child);
	} else {
		//Add the part to the tree.
		int vertical = (info.relationship == IPageLayout.LEFT || info.relationship == IPageLayout.RIGHT)?SWT.VERTICAL:SWT.HORIZONTAL;
		boolean left = info.relationship == IPageLayout.LEFT || info.relationship == IPageLayout.TOP; 
		LayoutPartSash sash = new LayoutPartSash(this,vertical);
		sash.setRatio(info.ratio);
		if((parent != null) && !(child instanceof PartPlaceholder))
			sash.createControl(parent);
		root = root.insert(child,left,sash,info.relative);
	}
	
	childAdded(child);
	
	if (active) {
		child.createControl(parent);
		child.setVisible(true);
		child.setContainer(this);
		resizeSashes(parent.getClientArea());
	}

}
/**
 * See ILayoutContainer#allowBorder
 */
public boolean allowsBorder() {
	return true;
}
/**
 * Notification that a child layout part has been
 * added to the container. Subclasses may override
 * this method to perform any container specific
 * work.
 */
protected abstract void childAdded(LayoutPart child);
/**
 * Notification that a child layout part has been
 * removed from the container. Subclasses may override
 * this method to perform any container specific
 * work.
 */
protected abstract void childRemoved(LayoutPart child);
/**
 * Returns an array with all the relation ship between the
 * parts.
 */
public RelationshipInfo[] computeRelation() {
	LayoutTree treeRoot = root;
	if(isZoomed())
		treeRoot = unzoomRoot;
	ArrayList list = new ArrayList();
	if(treeRoot == null)
		return new RelationshipInfo[0];
	RelationshipInfo r = new RelationshipInfo();
	r.part = treeRoot.computeRelation(list);
	list.add(0,r);
	RelationshipInfo[] result = new RelationshipInfo[list.size()];
	list.toArray(result);
	return result;
}
/**
 * @see LayoutPart#getControl
 */
public void createControl(Composite parentWidget) {
	if (active)
		return;

	parent = createParent(parentWidget);
	parent.addControlListener(resizeListener);
	
	for (int i = 0, length = children.size(); i < length; i++) {
		LayoutPart child = (LayoutPart)children.get(i);
		child.setContainer(this);
		child.createControl(parent);
	}

	root.updateSashes(parent);
	active = true;
	resizeSashes(parent.getClientArea());
}
/**
 * Subclasses override this method to specify
 * the composite to use to parent all children
 * layout parts it contains.
 */
protected abstract Composite createParent(Composite parentWidget);
/**
 * @see LayoutPart#dispose
 */
public void dispose() {
	if (!active)
		return;
	
	// remove all Listeners
	if (resizeListener != null && parent != null){
		parent.removeControlListener(resizeListener);
	}
	
	resizeSashes(new Rectangle(-200, -200, 0, 0));

	if (children != null) {
		for (int i = 0, length = children.size(); i < length; i++){
			LayoutPart child = (LayoutPart)children.get(i);
			child.setContainer(null);
			// In PartSashContainer dispose really means deactivate, so we
			// only dispose PartTabFolders.
			if (child instanceof PartTabFolder)
				child.dispose();
		}
	}
	
	disposeParent();
	this.parent = null;
	
	active = false;
}
/**
 * Subclasses override this method to dispose
 * of any swt resources created during createParent.
 */
protected abstract void disposeParent();
/**
 * Dispose all sashs used in this perspective.
 */
public void disposeSashes() {
	root.disposeSashes();
}
/**
 * Return the most bottom right part or null if none.
 */
public LayoutPart findBottomRight() {
	if(root == null)
		return null;
	return root.findBottomRight();
}
/**
 * Find a initial position for a new part.
 */
private void findPosition(RelationshipInfo info) {

	info.ratio = (float)0.5;
	info.relationship = IPageLayout.RIGHT;
	info.relative = root.findBottomRight();

	// If no parent go with default.
	if (parent == null)
		return;
		
	// If the relative part is too small, place the part on the left of everything.
	if (((float)this.getBounds().width / (float)info.relative.getBounds().width > 2) ||
		 ((float)this.getBounds().height / (float)info.relative.getBounds().height > 4)) {
		info.relative = null;
		info.ratio = 0.75f;
	}
}
/**
 * @see LayoutPart#getBounds
 */
public Rectangle getBounds() {
	return this.parent.getBounds();
}


// getMinimumHeight() added by cagatayk@acm.org 
/**
 * @see LayoutPart#getMinimumHeight()
 */
public int getMinimumHeight() {
	return getLayoutTree().getMinimumHeight();
}

// getMinimumHeight() added by cagatayk@acm.org 
/**
 * @see LayoutPart#getMinimumWidth()
 */
public int getMinimumWidth() {
	return getLayoutTree().getMinimumWidth();
}


/**
 * @see ILayoutContainer#getChildren
 */
public LayoutPart[] getChildren() {
	LayoutPart[] result = new LayoutPart[children.size()];
	children.toArray(result);
	return result;
}

/**
 * @see LayoutPart#getControl
 */
public Control getControl() {
	return this.parent;
}

public LayoutTree getLayoutTree() {
	return root;
}
/**
 * Return the interested listener of mouse down events.
 */
protected Listener getMouseDownListener() {
	return mouseDownListener;
}
/**
 * Returns the composite used to parent all the
 * layout parts contained within.
 */
public Composite getParent() {
	return parent;
}
private boolean isChild(LayoutPart part) {
	return children.indexOf(part) >= 0;
}
private boolean isRelationshipCompatible(int relationship,boolean isVertical) {
	if(isVertical)
		return (relationship == IPageLayout.RIGHT || relationship == IPageLayout.LEFT);
	else 
		return (relationship == IPageLayout.TOP || relationship == IPageLayout.BOTTOM);
}
/**
 * Returns whether this container is zoomed.
 */
public boolean isZoomed() {
	return (unzoomRoot != null);
}
/**
 * Move a part to a new position and keep the bounds when possible, ie,
 * when the new relative part has the same higth or width as the part
 * being move.
 */
public void move(LayoutPart child, int relationship, LayoutPart relative) {
	LayoutTree childTree = root.find(child);
	LayoutTree relativeTree = root.find(relative);

	LayoutTreeNode commonParent = relativeTree.getParent().findCommonParent(child,relative);
	boolean isVertical = commonParent.getSash().isVertical();
	boolean recomputeRatio = false;
	recomputeRatio =
		isRelationshipCompatible(relationship,isVertical) &&
			commonParent.sameDirection(isVertical,relativeTree.getParent()) && 
				commonParent.sameDirection(isVertical,childTree.getParent());

	root = root.remove(child);
	int vertical = (relationship == IPageLayout.LEFT || relationship == IPageLayout.RIGHT)?SWT.VERTICAL:SWT.HORIZONTAL;
	boolean left = relationship == IPageLayout.LEFT || relationship == IPageLayout.TOP; 
	LayoutPartSash sash = new LayoutPartSash(this,vertical);
	sash.setRatio(0.5f);
	if((parent != null) && !(child instanceof PartPlaceholder))
		sash.createControl(parent);
	root = root.insert(child,left,sash,relative);
	root.updateSashes(parent);
	if(recomputeRatio)
		root.recomputeRatio();
		
	resizeSashes(parent.getClientArea());
}
/**
 * Remove a part.
 */ 
public void remove(LayoutPart child) {
	if (isZoomed())
		zoomOut();
		
	if (!isChild(child))
		return;

	children.remove(child); 
	root = root.remove(child);
	if(root != null)
		root.updateSashes(parent);
	childRemoved(child);
	
	if (active){
		child.setVisible(false);
		child.setContainer(null);
		resizeSashes(parent.getClientArea());
	}
}
/**
 * Replace one part with another.
 */ 
public void replace(LayoutPart oldChild, LayoutPart newChild) {
	if (isZoomed())
		zoomOut();

	if (!isChild(oldChild))return;
		
	children.remove(oldChild);
	children.add(newChild);

	childAdded(newChild);
	LayoutTree leaf = root.find(oldChild);
	leaf.setPart(newChild);
	root.updateSashes(parent);

	childRemoved(oldChild);
	if (active){
		oldChild.setVisible(false);
		oldChild.setContainer(null);
		newChild.createControl(parent);
		newChild.setContainer(this);
		newChild.setVisible(true);		
		resizeSashes(parent.getClientArea());
	}
}
private void resizeSashes(Rectangle parentSize) {
	if (!active) return;
	root.setBounds(parentSize);
}
/**
 * @see LayoutPart#setBounds
 */
public void setBounds(Rectangle r) {
	this.parent.setBounds(r);
}
/**
 * Zoom in on a particular layout part.
 *
 * The implementation of zoom is quite simple.  When zoom occurs we create
 * a zoom root which only contains the zoom part.  We store the old
 * root in unzoomRoot and then active the zoom root.  When unzoom occurs
 * we restore the unzoomRoot and dispose the zoom root.
 *
 * Note: Method assumes we are active.
 */
public void zoomIn(LayoutPart part) {
	// Sanity check.
	if (unzoomRoot != null)
		return;

	// Hide main root.
	Rectangle oldBounds = root.getBounds();
	root.setBounds(new Rectangle(0,0,0,0));
	unzoomRoot = root;

	// Show zoom root.
	root = new LayoutTree(part);
	root.setBounds(oldBounds);
}
/**
 * Zoom out.
 *
 * See zoomIn for implementation details.
 * 
 * Note: Method assumes we are active.
 */
public void zoomOut() {
	// Sanity check.
	if (unzoomRoot == null)
		return;

	// Dispose zoom root.
	Rectangle oldBounds = root.getBounds();
	root.setBounds(new Rectangle(0,0,0,0));

	// Show main root.
	root = unzoomRoot;
	root.setBounds(oldBounds);
	unzoomRoot = null;
}
}
