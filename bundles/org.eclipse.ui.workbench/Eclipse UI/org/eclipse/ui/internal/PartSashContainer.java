package org.eclipse.ui.internal;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *   IBM Corporation - initial API and implementation 
 *   Cagatay Kavukcuoglu <cagatayk@acm.org>
 *      - Fix for bug 10025 - Resizing views should not use height ratios
**********************************************************************/

import java.util.ArrayList;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.internal.dnd.AbstractDropTarget;
import org.eclipse.ui.internal.dnd.CompatibilityDragTarget;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDropTarget;

/**
 * Abstract container that groups various layout
 * parts (possibly other containers) together as
 * a unit. Manages the placement and size of these
 * layout part based on the location of sashes within
 * the container.
 */
public abstract class PartSashContainer extends LayoutPart implements ILayoutContainer, IDragOverListener {
 
	protected Composite parent;
	protected ControlListener resizeListener;
	protected LayoutTree root;
	protected LayoutTree unzoomRoot;
	protected WorkbenchPage page;
	boolean active = false;
	
	/* Array of LayoutPart */
	protected ArrayList children = new ArrayList(); 
	
	protected static class RelationshipInfo {
		protected LayoutPart part;
		protected LayoutPart relative;
		protected int relationship;
		protected float ratio;
	}

	private class SashContainerDropTarget extends AbstractDropTarget {
		private int side;
		private int cursor;
		private LayoutPart targetPart;
		private LayoutPart sourcePart;
		
		public SashContainerDropTarget(LayoutPart sourcePart, int side, int cursor, LayoutPart targetPart) {
			this.side = side;
			this.targetPart = targetPart;
			this.sourcePart = sourcePart;
			this.cursor = cursor;
		}

		public void drop() {
			if (side != SWT.NONE) {
				dropObject(sourcePart, targetPart, side);
			}
		}

		public Cursor getCursor() {			
			return DragCursors.getCursor(DragCursors.positionToDragCursor(cursor));
		}

		public Rectangle getSnapRectangle() {
			Rectangle targetBounds;
			
			if (targetPart != null) {
				targetBounds = DragUtil.getDisplayBounds(targetPart.getControl());
			} else {
				targetBounds = DragUtil.getDisplayBounds(getParent());
			}
			
			if (side == SWT.CENTER || side == SWT.NONE) {
				return targetBounds;
			}
			
			int distance = Geometry.getDimension(targetBounds, !Geometry.isHorizontal(side));
			
			return Geometry.getExtrudedEdge(targetBounds, (int) (distance
					* getDockingRatio(sourcePart, targetPart)), side);
		}
	}
	
public PartSashContainer(String id,final WorkbenchPage page) {
	super(id);
	this.page = page;
	resizeListener = new ControlAdapter() {
		public void controlResized(ControlEvent e) {
			resizeSashes(parent.getClientArea());
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
 * Add a new part relative to another. This should be used in place of <code>add</code>. 
 * It differs as follows:
 * <ul>
 * <li>relationships are specified using SWT direction constants</li>
 * <li>the ratio applies to the newly added child -- not the upper-left child</li>
 * </ul>
 * 
 * @param child new part to add to the layout
 * @param swtDirectionConstant one of SWT.TOP, SWT.BOTTOM, SWT.LEFT, or SWT.RIGHT
 * @param ratioForNewPart a value between 0.0 and 1.0 specifying how much space will be allocated for the newly added part
 * @param relative existing part indicating where the new child should be attached
 * @since 3.0
 */
void addEnhanced(LayoutPart child, int swtDirectionConstant, float ratioForNewPart, LayoutPart relative) {
	int relativePosition = PageLayout.swtConstantToLayoutPosition(swtDirectionConstant);
	
	float ratioForUpperLeftPart;
	
	if (relativePosition == PageLayout.RIGHT || relativePosition == PageLayout.BOTTOM) {
		ratioForUpperLeftPart = 1.0f - ratioForNewPart;
	} else {
		ratioForUpperLeftPart = ratioForNewPart;
	}
	
	add(child, relativePosition, ratioForUpperLeftPart, relative);
}

/**
 * Add a part relative to another. For compatibility only. New code should use
 * addEnhanced, above.
 * 
 * @param child the new part to add
 * @param relationship one of PageLayout.TOP, PageLayout.BOTTOM, PageLayout.LEFT, or PageLayout.RIGHT
 * @param ratio a value between 0.0 and 1.0, indicating how much space will be allocated to the UPPER-LEFT pane
 * @param relative part where the new part will be attached
 * 
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
protected void addChild(RelationshipInfo info) {
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
	
	DragUtil.addDragTarget(parent, this);
	DragUtil.addDragTarget(parent.getShell(), this);
	
	ArrayList children = (ArrayList)this.children.clone();
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

	DragUtil.removeDragTarget(parent, this);
	DragUtil.removeDragTarget(parent.getShell(), this);
	
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
 * For themes.
 * 
 * @return the current WorkbenchPage.
 */
public WorkbenchPage getPage() {
    return page;
}
/**
 * Returns the composite used to parent all the
 * layout parts contained within.
 */
public Composite getParent() {
	return parent;
}
protected boolean isChild(LayoutPart part) {
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

/* (non-Javadoc)
 * @see org.eclipse.ui.internal.dnd.IDragOverListener#drag(org.eclipse.swt.widgets.Control, java.lang.Object, org.eclipse.swt.graphics.Point, org.eclipse.swt.graphics.Rectangle)
 */
public IDropTarget drag(Control currentControl, Object draggedObject,
		Point position, Rectangle dragRectangle) {
	
	if (!(draggedObject instanceof LayoutPart)) {
		return null;
	}
	
	final LayoutPart sourcePart = (LayoutPart)draggedObject;
	
	if (!isStackType(sourcePart) && !isPaneType(sourcePart)) {
		return null;
	}
	
	if (isStackType(sourcePart) && !(sourcePart.getContainer() == this)) {
		return null;
	}
	
	if (sourcePart.getWorkbenchWindow() != getWorkbenchWindow()) {
		return null;
	}
	
	Rectangle containerBounds = DragUtil.getDisplayBounds(parent);
	LayoutPart targetPart = null;
	ILayoutContainer sourceContainer = isStackType(sourcePart) ? (ILayoutContainer)sourcePart : sourcePart.getContainer();
	
	if (containerBounds.contains(position)) {
		targetPart = root.findPart(parent.toControl(position));
		
		if (targetPart != null) {
			final Control targetControl = targetPart.getControl();
			
			int side = CompatibilityDragTarget.getRelativePosition(targetControl, position);
			
			final Rectangle targetBounds = DragUtil.getDisplayBounds(targetControl);
			
			// Disallow stacking if this isn't a container
			if (side == SWT.DEFAULT || (side == SWT.CENTER && !isStackType(targetPart))) {
				side = Geometry.getClosestSide(targetBounds, position);
			}
			
			// A "pointless drop" would be one that will put the dragged object back where it started.
			// Note that it should be perfectly valid to drag an object back to where it came from -- however,
			// the drop should be ignored.
			
			boolean pointlessDrop = isZoomed();
	
			if (sourcePart == targetPart) {
				pointlessDrop = true;
			}
			
			if ((sourceContainer != null) && (sourceContainer == targetPart) && getVisibleChildrenCount(sourceContainer) <= 1) {
				pointlessDrop = true;
			}
			
			if (side == SWT.CENTER && sourcePart.getContainer() == targetPart) {
				pointlessDrop = true;
			}
			
			int cursor = side;
			
			if (pointlessDrop) {
				side = SWT.NONE;
				cursor = SWT.CENTER;
			}
			
			return new SashContainerDropTarget(sourcePart, side, cursor, targetPart);
		}
	} else {
		
		int side = Geometry.getClosestSide(containerBounds, position);
		
		boolean pointlessDrop = isZoomed();
		
		if (isStackType(sourcePart) 
				|| (sourcePart.getContainer() != null && isPaneType(sourcePart) && getVisibleChildrenCount(sourcePart.getContainer()) <= 1)) {			
			if (root == null || getVisibleChildrenCount(this) <= 1) {
				pointlessDrop = true;
			}
		};
		
		int cursor = Geometry.getOppositeSide(side);
		
		if (pointlessDrop) {
			side = SWT.NONE;
			//cursor = SWT.CENTER;			
		}
		
		return new SashContainerDropTarget(sourcePart, side, cursor, null);
	}
		
	return null;
}

/**
 * Returns true iff this PartSashContainer allows its parts to be stacked onto the given
 * container.
 * 
 * @param container
 * @return
 */
public abstract boolean isStackType(LayoutPart toTest);

public abstract boolean isPaneType(LayoutPart toTest);

/* (non-Javadoc)
 * @see org.eclipse.ui.internal.PartSashContainer#dropObject(org.eclipse.ui.internal.LayoutPart, org.eclipse.ui.internal.LayoutPart, int)
 */
protected void dropObject(LayoutPart sourcePart, LayoutPart targetPart, int side) {
	
	if (side == SWT.CENTER) {
		stack(sourcePart, targetPart);
	} else {

		if (isStackType(sourcePart)) {
			// Remove the part from old container.
			derefPart(sourcePart);
			addEnhanced(sourcePart, side, getDockingRatio(sourcePart, targetPart), targetPart);
		} else {
			
			derefPart(sourcePart);
			LayoutPart newPart = createStack(sourcePart);
			
			addEnhanced(newPart, side, getDockingRatio(sourcePart, targetPart), targetPart);
		}
		
		sourcePart.setFocus();
	}
}

/**
 * @param sourcePart
 * @return
 */
protected abstract LayoutPart createStack(LayoutPart sourcePart);

public void stack(LayoutPart newPart, LayoutPart relPos) {
	
	ILayoutContainer container = (ILayoutContainer)relPos;
	
	getControl().setRedraw(false);
	if (isStackType(newPart)) {
		ILayoutContainer sourceContainer = (ILayoutContainer)newPart;
		LayoutPart visiblePart = getVisiblePart(sourceContainer);
		LayoutPart[] children = sourceContainer.getChildren();
		for (int i = 0; i < children.length; i++)
			stackPane(children[i], container);
		if (visiblePart != null) {
			setVisiblePart(container, visiblePart);
			visiblePart.setFocus();
		}
	}
	else if (isPaneType(newPart)) {
		stackPane(newPart, container);
		setVisiblePart(container, newPart);
		newPart.setFocus();
	}
	
	getControl().setRedraw(true);
}

/**
 * @param container
 * @param visiblePart
 */
protected abstract void setVisiblePart(ILayoutContainer container, LayoutPart visiblePart);

/**
 * @param container
 * @return
 */
protected abstract LayoutPart getVisiblePart(ILayoutContainer container);

private void stackPane(LayoutPart newPart, ILayoutContainer refPart) {
	// Remove the part from old container.
	derefPart(newPart);
	// Reparent part and add it to the workbook
	newPart.reparent(getParent());
	refPart.add(newPart);
}

/**
 * @param sourcePart
 */
protected void derefPart(LayoutPart sourcePart) {
	ILayoutContainer container = sourcePart.getContainer();
	if (container != null) {
		container.remove(sourcePart);
	}
	
	if (container instanceof LayoutPart) {
		if (isStackType((LayoutPart)container)) {
			if (container.getChildren().length == 0) {
				remove((LayoutPart)container);
			}
		}
	}
}

protected int getVisibleChildrenCount(ILayoutContainer container) {
	// Treat null as an empty container
	if (container == null) {
		return 0;
	}
	
	LayoutPart[] children = container.getChildren();
	
	int count = 0;
	for (int idx = 0; idx < children.length; idx++) {
		if (!(children[idx] instanceof PartPlaceholder)) {
			count++;
		}
	}
	
	return count;
}

protected float getDockingRatio(LayoutPart dragged, LayoutPart target) {
	return 0.5f;
}

}
