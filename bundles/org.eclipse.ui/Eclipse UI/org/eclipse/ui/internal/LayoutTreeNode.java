package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import java.util.ArrayList;
import org.eclipse.ui.IPageLayout;

/**
 * Implementation of a tree node. The node represents a
 * sash and it allways has two children.
 */
public class LayoutTreeNode extends LayoutTree {
	/* The node children witch may be another node or a leaf */
	private LayoutTree children[] = new LayoutTree[2];
	/* The sash's width when vertical and hight on horizontal */
	private final static int SASH_WIDTH = 3;
/**
 * Initialize this tree with its sash.
 */
public LayoutTreeNode(LayoutPartSash sash) {
	super(sash);
}
/**
 * Add the relation ship between the children in the list
 * and returns the left children.
 */
public LayoutPart computeRelation(ArrayList relations) {
	PartSashContainer.RelationshipInfo r = new PartSashContainer.RelationshipInfo();
	r.relative = children[0].computeRelation(relations);
	r.part = children[1].computeRelation(relations);
	r.ratio = getSash().getRatio();
	r.relationship = getSash().isVertical()?IPageLayout.RIGHT:IPageLayout.BOTTOM;
	relations.add(0,r);
	return r.relative;
}
/**
 * Dispose all Sashs in this tree
 */
public void disposeSashes() {
	children[0].disposeSashes();
	children[1].disposeSashes();
	getSash().dispose();
}
/**
 * Find a LayoutPart in the tree and return its sub-tree. Returns
 * null if the child is not found.
 */
public LayoutTree find(LayoutPart child) {
	LayoutTree node = children[0].find(child);
	if(node != null) return node;
	node = children[1].find(child);
	return node;
}
/**
 * Find the part that is in the bottom rigth possition.
 */
public LayoutPart findBottomRigth() {
	if(children[1].isVisible())
		return children[1].findBottomRigth();
	return children[0].findBottomRigth();
}
/**
 * Go up in the tree finding a parent that is common of both children.
 * Return the subtree.
 */
public LayoutTreeNode findCommonParent(LayoutPart child1, LayoutPart child2) {
	return findCommonParent(child1,child2,false,false);
}
/**
 * Go up in the tree finding a parent that is common of both children.
 * Return the subtree.
 */
LayoutTreeNode findCommonParent(LayoutPart child1, LayoutPart child2,boolean foundChild1,boolean foundChild2) {
	if(!foundChild1)
		foundChild1 = find(child1) != null;
	if(!foundChild2)
		foundChild2 = find(child2) != null;
	if(foundChild1 && foundChild2)
		return this;
	if(parent == null)
		return null;
	return parent.findCommonParent(child1,child2,foundChild1,foundChild2);
}
/**
 * Find a sash in the tree and return its sub-tree. Returns
 * null if the sash is not found.
 */
public LayoutTreeNode findSash(LayoutPartSash sash) {
	if(this.getSash() == sash)
		return this;
	LayoutTreeNode node = children[0].findSash(sash);
	if(node != null) return node;
	node = children[1].findSash(sash);
	if(node != null) return node;
	return null;
}
/**
 * Sets the elements in the array of sashes with the
 * Left,Rigth,Top and Botton sashes. The elements
 * may be null depending whether there is a shash
 * beside the <code>part</code>
 */
void findSashes(LayoutTree child,PartPane.Sashes sashes) {
	Sash sash = (Sash)getSash().getControl();
	boolean leftOrTop = children[0] == child;
	if(sash != null) {
		int index;
		LayoutPartSash partSash = getSash();
		//If the child is in the left, the sash 
		//is in the rigth and so on.
		if(leftOrTop) {
			if(partSash.isVertical()) {
				if(sashes.right == null)
					sashes.right = sash;
			} else {
				if(sashes.bottom == null)
					sashes.bottom = sash;
			}
		} else {
			if(partSash.isVertical()) {
				if(sashes.left == null)
					sashes.left = sash;
			} else {
				if(sashes.top == null)
					sashes.top = sash;
			}
		}
	}
	if(getParent() != null)
		getParent().findSashes(this,sashes);
}
/**
 * Return the bounds of this tree which is the rectangle that
 * contains all Controls in this tree.
 */
public Rectangle getBounds() {
	if(!children[0].isVisible())
		return children[1].getBounds();

	if(!children[1].isVisible())
		return children[0].getBounds();

	
	Rectangle leftBounds = children[0].getBounds();
	Rectangle rightBounds = children[1].getBounds();
	Rectangle sashBounds = getSash().getBounds();
	Rectangle result = new Rectangle(leftBounds.x,leftBounds.y,leftBounds.width,leftBounds.height);
	if(getSash().isVertical()) {
		result.width = rightBounds.width + leftBounds.width + sashBounds.width;
	} else {
		result.height = rightBounds.height + leftBounds.height + sashBounds.height;
	}
	return result;
}
/**
 * Returns the sash of this node.
 */
public LayoutPartSash getSash() {
	return (LayoutPartSash)part;
}
/**
 * Returns true if this tree has visible parts otherwise returns false.
 */
public boolean isVisible() {
	return children[0].isVisible() || children[1].isVisible();
}
/**
 * Recompute the ratios in this tree.
 */
public void recomputeRatio() {
	children[0].recomputeRatio();
	children[1].recomputeRatio();

	if(children[0].isVisible() && children[1].isVisible()) {
		if(getSash().isVertical()) {
			float left = children[0].getBounds().width;
			float right = children[1].getBounds().width;
			getSash().setRatio(left/(right+left+SASH_WIDTH));
		} else {
			float left = children[0].getBounds().height;
			float right = children[1].getBounds().height;
			getSash().setRatio(left/(right+left+SASH_WIDTH));
		}
	}
		
}
/**
 * Remove the child and this node from the tree
 */
LayoutTree remove(LayoutTree child) {
	getSash().dispose();
	if(parent == null) {
		//This is the root. Return the other child to be the new root.
		if(children[0] == child) {		
			children[1].setParent(null);
			return children[1];
		}
		children[0].setParent(null);
		return children[0];
	}
	
	LayoutTreeNode oldParent = parent;
	if(children[0] == child)
		oldParent.replaceChild(this,children[1]);
	else
		oldParent.replaceChild(this,children[0]);
	return oldParent;
}
/**
 * Replace a child with a new child and sets the new child's parent.
 */
void replaceChild(LayoutTree oldChild,LayoutTree newChild) {
	if(children[0] == oldChild)
		children[0] = newChild;
	else if(children[1] == oldChild)
		children[1] = newChild;
	newChild.setParent(this);
	if(!children[0].isVisible() || ! children[0].isVisible())
		getSash().dispose();
	
}
/**
 * Go up from the subtree and return true if all the sash are 
 * in the direction specified by <code>isVertical</code>
 */
public boolean sameDirection(boolean isVertical,LayoutTreeNode subTree) {
	boolean treeVertical = getSash().isVertical();
	if (treeVertical != isVertical)
		return false;
	while(subTree != null) {
		if(this == subTree)
			return true;
		if(subTree.children[0].isVisible() && subTree.children[1].isVisible())
			if(subTree.getSash().isVertical() != isVertical)
				return false;
		subTree = subTree.getParent();
	}
	return true;
}
/**
 * Resize the parts on this tree to fit in <code>bounds</code>.
 */
public void setBounds(Rectangle bounds) {
	if(!children[0].isVisible()) {
		children[1].setBounds(bounds);
		return;
	}
	if(!children[1].isVisible()) {
		children[0].setBounds(bounds);
		return;
	}
	
	Rectangle leftBounds = new Rectangle(bounds.x,bounds.y,bounds.width,bounds.height);
	Rectangle rightBounds = new Rectangle(bounds.x,bounds.y,bounds.width,bounds.height);
	Rectangle sashBounds = new Rectangle(bounds.x,bounds.y,bounds.width,bounds.height);
	if(getSash().isVertical()) {
		//Work on x and width
		int w = bounds.width - SASH_WIDTH;
		leftBounds.width = (int)(getSash().getRatio() * w);
		sashBounds.x = leftBounds.x + leftBounds.width;
		sashBounds.width = SASH_WIDTH;
		rightBounds.x = sashBounds.x + sashBounds.width;
		rightBounds.width = bounds.width - leftBounds.width - sashBounds.width;
	} else {
		//Work on y and height
		int h = bounds.height - SASH_WIDTH;
		leftBounds.height = (int)(getSash().getRatio() * h);
		sashBounds.y = leftBounds.y + leftBounds.height;
		sashBounds.height = SASH_WIDTH;
		rightBounds.y = sashBounds.y + sashBounds.height;
		rightBounds.height = bounds.height - leftBounds.height - sashBounds.height;
	}
	getSash().setBounds(sashBounds);
	children[0].setBounds(leftBounds);
	children[1].setBounds(rightBounds);
}
/**
 * Sets a child in this node
 */
void setChild(boolean left,LayoutPart part) {
	LayoutTree child = new LayoutTree(part);
	setChild(left,child);
}
/**
 * Sets a child in this node
 */
void setChild(boolean left,LayoutTree child) {
	int index = left?0:1;
	children[index] = child;
	child.setParent(this);
}
/**
 * Returns a string representation of this object.
 */
public String toString() {
	String s = "<null>\n";//$NON-NLS-1$
	if(part.getControl() != null)
		s = "<@" + part.getControl().hashCode() + ">\n";//$NON-NLS-2$//$NON-NLS-1$
	String result = "["; //$NON-NLS-1$
	if(children[0].getParent() != this)
		result = result + "{" + children[0] + "}" + s;//$NON-NLS-2$//$NON-NLS-1$
	else
		result = result + children[0] + s;
	
	if(children[1].getParent() != this)
		result = result + "{" + children[1] + "}]";//$NON-NLS-2$//$NON-NLS-1$
	else
		result = result + children[1] + "]";//$NON-NLS-1$
	return result;
}
/**
 * Create the sashes if the children are visible
 * and dispose it if they are not.
 */
public void updateSashes(Composite parent) {
	if(parent == null) return;
	children[0].updateSashes(parent);
	children[1].updateSashes(parent);
	if(children[0].isVisible() && children[1].isVisible())
		getSash().createControl(parent);
	else
		getSash().dispose();
}
}
