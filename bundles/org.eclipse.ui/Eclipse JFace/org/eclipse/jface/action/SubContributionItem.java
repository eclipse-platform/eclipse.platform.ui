package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.*;

/**
 * A <code>SubContributionItem</code> is a wrapper for an <code>IContributionItem</code>.  
 * It is used within a <code>SubContributionManager</code> to control the visibility
 * of items.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 */
public class SubContributionItem implements IContributionItem {
	/**
	 * The visibility of the item.
	 */
	private boolean visible;

	/**
	 * The inner item for this contribution.  
	 */
	private IContributionItem innerItem;
/**
 * Creates a new <code>SubContributionItem</code>.
 */
public SubContributionItem(IContributionItem item) {
	innerItem = item;
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public void fill(Composite parent) {
	if (visible)
		innerItem.fill(parent);
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public void fill(Menu parent, int index) {
	if (visible)
		innerItem.fill(parent, index);
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public void fill(ToolBar parent, int index) {
	if (visible)
		innerItem.fill(parent, index);
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public String getId() {
	return innerItem.getId();
}
/**
 * Returns the inner contribution item.
 *
 * @return the inner contribution item
 */
public IContributionItem getInnerItem() {
	return innerItem;
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public boolean isDynamic() {
	return innerItem.isDynamic();
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public boolean isGroupMarker() {
	return innerItem.isGroupMarker();
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public boolean isSeparator() {
	return innerItem.isSeparator();
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public boolean isVisible() {
	return visible && innerItem.isVisible();
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public void setParent(IContributionManager parent) {
	// do nothing, the parent of our inner item
	// is its SubContributionManager
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public void setVisible(boolean visible) {
	this.visible = visible;
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public void update() {
	innerItem.update();
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public void update(String id) {
	innerItem.update(id);
}
}
