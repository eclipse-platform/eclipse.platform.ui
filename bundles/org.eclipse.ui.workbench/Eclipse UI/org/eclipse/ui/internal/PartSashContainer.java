/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cagatay Kavukcuoglu <cagatayk@acm.org>
 *     - Fix for bug 10025 - Resizing views should not use height ratios
 *******************************************************************************/
package org.eclipse.ui.internal;

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
public abstract class PartSashContainer extends LayoutPart implements
        ILayoutContainer, IDragOverListener {

    protected Composite parent;

    protected ControlListener resizeListener;

    protected LayoutTree root;

    protected LayoutTree unzoomRoot;

    protected WorkbenchPage page;

    boolean active = false;
    boolean layoutDirty = false;

    /* Array of LayoutPart */
    protected ArrayList children = new ArrayList();

    private SashContainerDropTarget dropTarget;

    protected static class RelationshipInfo {
        protected LayoutPart part;

        protected LayoutPart relative;

        protected int relationship;

        /**
         * Preferred size for the left child (this would be the size, in pixels of the child
         * at the time the sash was last moved)
         */
        protected int left;

        /**
         * Preferred size for the right child (this would be the size, in pixels of the child
         * at the time the sash was last moved)
         */
        protected int right;

        /**
         * Computes the "ratio" for this container. That is, the ratio of the left side over
         * the sum of left + right. This is only used for serializing PartSashContainers in 
         * a form that can be read by old versions of Eclipse. This can be removed if this
         * is no longer required. 
         * 
         * @return the pre-Eclipse 3.0 sash ratio
         */
        public float getRatio() {
            int total = left + right;
            if (total > 0) {
                return (float) left / (float) total;
            } else {
                return 0.5f;
            }
        }
    }

    private class SashContainerDropTarget extends AbstractDropTarget {
        private int side;

        private int cursor;

        private LayoutPart targetPart;

        private LayoutPart sourcePart;

        public SashContainerDropTarget(LayoutPart sourcePart, int side, int cursor, LayoutPart targetPart) {
            this.setTarget(sourcePart, side, cursor, targetPart);
        }
        
        public void setTarget(LayoutPart sourcePart, int side, int cursor, LayoutPart targetPart) {
            this.side = side;
            this.targetPart = targetPart;
            this.sourcePart = sourcePart;
            this.cursor = cursor;
        }

        public void drop() {
            if (side != SWT.NONE) {
                LayoutPart visiblePart = sourcePart;

                if (sourcePart instanceof PartStack) {
                    visiblePart = getVisiblePart((PartStack) sourcePart);
                }

                dropObject(getVisibleParts(sourcePart), visiblePart,
                        targetPart, side);
            }
        }

        public Cursor getCursor() {
            return DragCursors.getCursor(DragCursors
                    .positionToDragCursor(cursor));
        }

        public Rectangle getSnapRectangle() {
            Rectangle targetBounds;

            if (targetPart != null) {
                targetBounds = DragUtil.getDisplayBounds(targetPart
                        .getControl());
            } else {
                targetBounds = DragUtil.getDisplayBounds(getParent());
            }

            if (side == SWT.CENTER || side == SWT.NONE) {
                return targetBounds;
            }

            int distance = Geometry.getDimension(targetBounds, !Geometry
                    .isHorizontal(side));

            return Geometry.getExtrudedEdge(targetBounds,
                    (int) (distance * getDockingRatio(sourcePart, targetPart)),
                    side);
        }
    }

    public PartSashContainer(String id, final WorkbenchPage page) {
        super(id);
        this.page = page;
        resizeListener = new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                resizeSashes(parent.getClientArea());
            }
        };
    }

    /**
     * Given an object associated with a drag (a PartPane or PartStack), this returns
     * the actual PartPanes being dragged.
     * 
     * @param pane
     * @return
     */
    private PartPane[] getVisibleParts(LayoutPart pane) {
        if (pane instanceof PartPane) {
            return new PartPane[] { (PartPane) pane };
        } else if (pane instanceof PartStack) {
            PartStack stack = (PartStack) pane;

            LayoutPart[] children = stack.getChildren();
            ArrayList result = new ArrayList(children.length);
            for (int idx = 0; idx < children.length; idx++) {
                LayoutPart next = children[idx];
                if (next instanceof PartPane) {
                    result.add(next);
                }
            }

            return (PartPane[]) result.toArray(new PartPane[result.size()]);
        }

        return new PartPane[0];
    }

    /**
     * Find the sashs around the specified part.
     */
    public void findSashes(LayoutPart pane, PartPane.Sashes sashes) {
        if (root == null) {
            return;
        }
        LayoutTree part = root.find(pane);
        if (part == null)
            return;
        part.findSashes(sashes);
    }

    /**
     * Add a part.
     */
    public void add(LayoutPart child) {
        if (child == null)
            return;

        addEnhanced(child, SWT.RIGHT, 0.5f, findBottomRight());
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
    void addEnhanced(LayoutPart child, int swtDirectionConstant,
            float ratioForNewPart, LayoutPart relative) {
        int relativePosition = PageLayout
                .swtConstantToLayoutPosition(swtDirectionConstant);

        float ratioForUpperLeftPart;

        if (relativePosition == PageLayout.RIGHT
                || relativePosition == PageLayout.BOTTOM) {
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
     */
    public void add(LayoutPart child, int relationship, float ratio,
            LayoutPart relative) {
        boolean isHorizontal = (relationship == IPageLayout.LEFT || relationship == IPageLayout.RIGHT);

        LayoutTree node = null;
        if (root != null && relative != null) {
            node = root.find(relative);
        }

        Rectangle bounds;
        if (getParent() == null) {
            Control control = getPage().getClientComposite();
            if (control != null && !control.isDisposed()) {
                bounds = control.getBounds();
            } else {
                bounds = new Rectangle(0, 0, 800, 600);
            }
            bounds.x = 0;
            bounds.y = 0;
        } else {
            bounds = getBounds();
        }

        int totalSize = measureTree(bounds, node, isHorizontal);

        int left = (int) (totalSize * ratio);
        int right = totalSize - left;

        add(child, relationship, left, right, relative);
    }

    static int measureTree(Rectangle outerBounds, LayoutTree toMeasure,
            boolean horizontal) {
        if (toMeasure == null) {
            return Geometry.getDimension(outerBounds, horizontal);
        }

        LayoutTreeNode parent = toMeasure.getParent();
        if (parent == null) {
            return Geometry.getDimension(outerBounds, horizontal);
        }

        if (parent.getSash().isHorizontal() == horizontal) {
            return measureTree(outerBounds, parent, horizontal);
        }

        boolean isLeft = parent.isLeftChild(toMeasure);

        LayoutTree otherChild = parent.getChild(!isLeft);
        if (otherChild.isVisible()) {
            int left = parent.getSash().getLeft();
            int right = parent.getSash().getRight();
            int childSize = isLeft ? left : right;

            int bias = parent.getCompressionBias();

            // Normalize bias: 1 = we're fixed, -1 = other child is fixed
            if (isLeft) {
                bias = -bias;
            }

            if (bias == 1) {
                // If we're fixed, return the fixed size
                return childSize;
            } else if (bias == -1) {

                // If the other child is fixed, return the size of the parent minus the fixed size of the
                // other child
                return measureTree(outerBounds, parent, horizontal)
                        - (left + right - childSize);
            }

            // Else return the size of the parent, scaled appropriately
            return measureTree(outerBounds, parent, horizontal) * childSize
                    / (left + right);
        }

        return measureTree(outerBounds, parent, horizontal);
    }

    protected void addChild(RelationshipInfo info) {
        LayoutPart child = info.part;

        children.add(child);

        if (root == null) {
            root = new LayoutTree(child);
        } else {
            //Add the part to the tree.
            int vertical = (info.relationship == IPageLayout.LEFT || info.relationship == IPageLayout.RIGHT) ? SWT.VERTICAL
                    : SWT.HORIZONTAL;
            boolean left = info.relationship == IPageLayout.LEFT
                    || info.relationship == IPageLayout.TOP;
            LayoutPartSash sash = new LayoutPartSash(this, vertical);
            sash.setSizes(info.left, info.right);
            if ((parent != null) && !(child instanceof PartPlaceholder))
                sash.createControl(parent);
            root = root.insert(child, left, sash, info.relative);
        }

        childAdded(child);

        if (active) {
            child.createControl(parent);
            child.setVisible(true);
            child.setContainer(this);
            resizeChild(child);
        }

    }

    /**
     * Adds the child using ratio and position attributes
     * from the specified placeholder without replacing
     * the placeholder
     * 
     * FIXME: I believe there is a bug in computeRelation()
     * when a part is positioned relative to the editorarea.
     * We end up with a null relative and 0.0 for a ratio.
     */
    void addChildForPlaceholder(LayoutPart child, LayoutPart placeholder) {
        RelationshipInfo newRelationshipInfo = new RelationshipInfo();
        newRelationshipInfo.part = child;
        if (root != null) {
            newRelationshipInfo.relationship = IPageLayout.RIGHT;
            newRelationshipInfo.relative = root.findBottomRight();
            newRelationshipInfo.left = 200;
            newRelationshipInfo.right = 200;
        }

        // find the relationship info for the placeholder
        RelationshipInfo[] relationships = computeRelation();
        for (int i = 0; i < relationships.length; i++) {
            RelationshipInfo info = relationships[i];
            if (info.part == placeholder) {
                newRelationshipInfo.left = info.left;
                newRelationshipInfo.right = info.right;
                newRelationshipInfo.relationship = info.relationship;
                newRelationshipInfo.relative = info.relative;
            }
        }

        addChild(newRelationshipInfo);
        flushLayout();
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
        if (isZoomed())
            treeRoot = unzoomRoot;
        ArrayList list = new ArrayList();
        if (treeRoot == null)
            return new RelationshipInfo[0];
        RelationshipInfo r = new RelationshipInfo();
        r.part = treeRoot.computeRelation(list);
        list.add(0, r);
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

        ArrayList children = (ArrayList) this.children.clone();
        for (int i = 0, length = children.size(); i < length; i++) {
            LayoutPart child = (LayoutPart) children.get(i);
            child.setContainer(this);
            child.createControl(parent);
        }

        if (root != null) {
            root.createControl(parent);
        }
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
        if (resizeListener != null && parent != null) {
            parent.removeControlListener(resizeListener);
        }

        resizeSashes(new Rectangle(-200, -200, 0, 0));

        if (children != null) {
            for (int i = 0, length = children.size(); i < length; i++) {
                LayoutPart child = (LayoutPart) children.get(i);
                child.setContainer(null);
                // In PartSashContainer dispose really means deactivate, so we
                // only dispose PartTabFolders.
                if (child instanceof ViewStack)
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
        if (root != null) {
            root.disposeSashes();
        }
    }

    /**
     * Return the most bottom right part or null if none.
     */
    public LayoutPart findBottomRight() {
        if (root == null)
            return null;
        return root.findBottomRight();
    }

    /**
     * @see LayoutPart#getBounds
     */
    public Rectangle getBounds() {
        return this.parent.getBounds();
    }

//    // getMinimumHeight() added by cagatayk@acm.org 
//    /**
//     * @see LayoutPart#getMinimumHeight()
//     */
//    public int computeMinimumSize(boolean width, int knownHeight) {
//        return getLayoutTree().computeMinimumSize(width, knownHeight);
//    }

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

    private boolean isRelationshipCompatible(int relationship,
            boolean isVertical) {
        if (isVertical)
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#forceLayout(org.eclipse.ui.internal.LayoutPart)
     */
    public void resizeChild(LayoutPart childThatChanged) {
    	if (root != null) {
    		LayoutTree tree = root.find(childThatChanged);
    		
    		if (tree != null) {
    			tree.flushCache();
    		}
    	}
    	
        flushLayout();

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
        if (root != null) {
            root = root.remove(child);
        }
        childRemoved(child);

        if (active) {
            child.setVisible(false);
            child.setContainer(null);
            flushLayout();
        }
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.LayoutPart#forceLayout()
	 */
	public void flushLayout() {
		layoutDirty = true;
		super.flushLayout();
		
		if (layoutDirty) {
			resizeSashes(parent.getClientArea());
		}
	}
	
    /**
     * Replace one part with another.
     */
    public void replace(LayoutPart oldChild, LayoutPart newChild) {
        if (isZoomed())
            zoomOut();

        if (!isChild(oldChild)) {
            return;
        }

        LayoutTree leaf = null;
        if (root != null) {
            leaf = root.find(oldChild);
        }

        if (leaf == null) {
            return;
        }

        children.remove(oldChild);
        children.add(newChild);

        childAdded(newChild);

        leaf.setPart(newChild);

        childRemoved(oldChild);
        if (active) {
            oldChild.setVisible(false);
            oldChild.setContainer(null);
            newChild.createControl(parent);
            newChild.setContainer(this);
            newChild.setVisible(true);
            resizeChild(newChild);
        }
    }

    private void resizeSashes(Rectangle parentSize) {
    	layoutDirty = false;
        if (!active)
            return;
        if (root != null) {
            root.setBounds(parentSize);
        }
    }

    /**
     * Returns the maximum size that can be utilized by this part if the given width and
     * height are available. Parts can overload this if they have a quantized set of preferred 
     * sizes.
     * 
     * @param availableWidth available horizontal space (pixels)
     * @param availableHeight available vertical space (pixels)
     * @return returns a new point where point.x is <= availableWidth and point.y is <= availableHeight
     */
    public int computePreferredSize(boolean width, int availableParallel, int availablePerpendicular, int preferredParallel) {
    	if (root != null) {
    		return root.computePreferredSize(width, availableParallel, availablePerpendicular, preferredParallel);
    	}
    	    	
    	return preferredParallel;
    }
	
    public int getSizeFlags(boolean width) {
        if (root != null) {
            return root.getSizeFlags(width);
        }
        
        return 0;
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
        root.setBounds(new Rectangle(0, 0, 0, 0));
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
        root.setBounds(new Rectangle(0, 0, 0, 0));

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

        final LayoutPart sourcePart = (LayoutPart) draggedObject;

        if (!isStackType(sourcePart) && !isPaneType(sourcePart)) {
            return null;
        }

        if (sourcePart.getWorkbenchWindow() != getWorkbenchWindow()) {
            return null;
        }

        Rectangle containerBounds = DragUtil.getDisplayBounds(parent);
        LayoutPart targetPart = null;
        ILayoutContainer sourceContainer = isStackType(sourcePart) ? (ILayoutContainer) sourcePart
                : sourcePart.getContainer();

        if (containerBounds.contains(position)) {

            if (root != null) {
                targetPart = root.findPart(parent.toControl(position));
            }

            if (targetPart != null) {
                final Control targetControl = targetPart.getControl();

                int side = CompatibilityDragTarget.getRelativePosition(
                        targetControl, position);

                final Rectangle targetBounds = DragUtil
                        .getDisplayBounds(targetControl);

                // Disallow stacking if this isn't a container
                if (side == SWT.DEFAULT
                        || (side == SWT.CENTER && !isStackType(targetPart))) {
                    side = Geometry.getClosestSide(targetBounds, position);
                }

                // A "pointless drop" would be one that will put the dragged object back where it started.
                // Note that it should be perfectly valid to drag an object back to where it came from -- however,
                // the drop should be ignored.

                boolean pointlessDrop = isZoomed();

                if (sourcePart == targetPart) {
                    pointlessDrop = true;
                }

                if ((sourceContainer != null)
                        && (sourceContainer == targetPart)
                        && getVisibleChildrenCount(sourceContainer) <= 1) {
                    pointlessDrop = true;
                }

                if (side == SWT.CENTER
                        && sourcePart.getContainer() == targetPart) {
                    pointlessDrop = true;
                }

                int cursor = side;

                if (pointlessDrop) {
                    side = SWT.NONE;
                    cursor = SWT.CENTER;
                }

                return createDropTarget(sourcePart, side, cursor, targetPart);
            }
        } else {

            int side = Geometry.getClosestSide(containerBounds, position);

            boolean pointlessDrop = isZoomed();

            if (isStackType(sourcePart)
                    || (sourcePart.getContainer() != null
                            && isPaneType(sourcePart) && getVisibleChildrenCount(sourcePart
                            .getContainer()) <= 1)) {
                if (root == null || getVisibleChildrenCount(this) <= 1) {
                    pointlessDrop = true;
                }
            }
            ;

            int cursor = Geometry.getOppositeSide(side);

            if (pointlessDrop) {
                side = SWT.NONE;
            }

            return createDropTarget(sourcePart, side, cursor, null);
        }

        return null;
    }

    /**
     * @param sourcePart
     * @param targetPart
     * @param side
     * @param cursor
     * @return
     * @since 3.1
     */
    private SashContainerDropTarget createDropTarget(final LayoutPart sourcePart, int side, int cursor, LayoutPart targetPart) {
        if (dropTarget == null) {
            dropTarget = new SashContainerDropTarget(sourcePart, side, cursor,
                targetPart);
        } else {
            dropTarget.setTarget(sourcePart, side, cursor, targetPart);
        }
        return dropTarget;
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
    protected void dropObject(PartPane[] toDrop, LayoutPart visiblePart,
            LayoutPart targetPart, int side) {
        getControl().setRedraw(false);

        if (side == SWT.CENTER) {
            if (isStackType(targetPart)) {
                PartStack stack = (PartStack) targetPart;
                for (int idx = 0; idx < toDrop.length; idx++) {
                    PartPane next = toDrop[idx];

                    stack(next, stack);
                }
            }
        } else {
            PartStack newPart = createStack();

            for (int idx = 0; idx < toDrop.length; idx++) {
                PartPane next = toDrop[idx];
                stack(next, newPart);
            }

            addEnhanced(newPart, side, getDockingRatio(newPart, targetPart),
                    targetPart);
        }

        setVisiblePart(visiblePart.getContainer(), visiblePart);

        getControl().setRedraw(true);

        visiblePart.setFocus();
    }

    /**
     * @param sourcePart
     * @return
     */
    protected abstract PartStack createStack();

    public void stack(LayoutPart newPart, PartStack container) {

        getControl().setRedraw(false);
        // Remove the part from old container.
        derefPart(newPart);
        // Reparent part and add it to the workbook
        newPart.reparent(getParent());
        container.add(newPart);
        getControl().setRedraw(true);

    }

    /**
     * @param container
     * @param visiblePart
     */
    protected abstract void setVisiblePart(ILayoutContainer container,
            LayoutPart visiblePart);

    /**
     * @param container
     * @return
     */
    protected abstract LayoutPart getVisiblePart(ILayoutContainer container);

    /**
     * @param sourcePart
     */
    protected void derefPart(LayoutPart sourcePart) {
        ILayoutContainer container = sourcePart.getContainer();
        if (container != null) {
            container.remove(sourcePart);
        }

        if (container instanceof LayoutPart) {
            if (isStackType((LayoutPart) container)) {
                PartStack stack = (PartStack) container;
                if (stack.getChildren().length == 0) {
                    remove(stack);
                    stack.dispose();
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

    /**
     * Writes a description of the layout to the given string buffer.
     * This is used for drag-drop test suites to determine if two layouts are the
     * same. Like a hash code, the description should compare as equal iff the
     * layouts are the same. However, it should be user-readable in order to
     * help debug failed tests. Although these are english readable strings,
     * they should not be translated or equality tests will fail.
     * 
     * @param buf
     */
    public void describeLayout(StringBuffer buf) {
        if (root == null) {
            return;
        }

        if (isZoomed()) {
            buf.append("zoomed "); //$NON-NLS-1$
            root.describeLayout(buf);
        } else {
            buf.append("layout "); //$NON-NLS-1$
            root.describeLayout(buf);
        }
    }

    /**
     * Adds a new child to the container relative to some part
     * 
     * @param child
     * @param relationship
     * @param left preferred pixel size of the left/top child
     * @param right preferred pixel size of the right/bottom child
     * @param relative relative part
     */
    void add(LayoutPart child, int relationship, int left, int right,
            LayoutPart relative) {
        if (isZoomed())
            zoomOut();

        if (child == null)
            return;
        if (relative != null && !isChild(relative))
            return;
        if (relationship < IPageLayout.LEFT
                || relationship > IPageLayout.BOTTOM)
            relationship = IPageLayout.LEFT;

        // store info about relative positions
        RelationshipInfo info = new RelationshipInfo();
        info.part = child;
        info.relationship = relationship;
        info.left = left;
        info.right = right;
        info.relative = relative;
        addChild(info);
    }

//    /* (non-Javadoc)
//     * @see org.eclipse.ui.internal.LayoutPart#resizesVertically()
//     */
//    public boolean isMinimized(boolean width) {
//        if (root == null) {
//            return false;
//        }
//        return root.isMinimized(width);
//    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#testInvariants()
     */
    public void testInvariants() {
        super.testInvariants();

        LayoutPart[] children = getChildren();

        for (int idx = 0; idx < children.length; idx++) {
            children[idx].testInvariants();
        }
    }
}