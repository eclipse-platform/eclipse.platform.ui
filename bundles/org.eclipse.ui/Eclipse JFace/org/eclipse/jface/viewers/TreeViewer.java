package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.util.*;
import java.util.List; // Otherwise ambiguous

/**
 * A concrete viewer based on an SWT <code>Tree</code> control.
 * <p>
 * This class is not intended to be subclassed outside the viewer framework. 
 * It is designed to be instantiated with a pre-existing SWT tree control and configured
 * with a domain-specific content provider, label provider, element filter (optional),
 * and element sorter (optional).
 * </p>
 * <p>
 * Content providers for tree viewers must implement the <code>ITreeContentProvider</code>
 * interface.
 * </p>
 */
public class TreeViewer extends AbstractTreeViewer {
	
	/**
	 * This viewer's control.
	 */
	private Tree tree;
/**
 * Creates a tree viewer on a newly-created tree control under the given parent.
 * The tree control is created using the SWT style bits <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>.
 * The viewer has no input, no content provider, a default label provider, 
 * no sorter, and no filters.
 *
 * @param parent the parent control
 */
public TreeViewer(Composite parent) {
	this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
}
/**
 * Creates a tree viewer on a newly-created tree control under the given parent.
 * The tree control is created using the given SWT style bits.
 * The viewer has no input, no content provider, a default label provider, 
 * no sorter, and no filters.
 * The tree control has the MULTI, H_SCROLL, V_SCROLL and BORDER style bits set.
 *
 * @param parent the parent control
 * @param style the SWT style bits
 */
public TreeViewer(Composite parent, int style) {
	this(new Tree(parent, style));
}
/**
 * Creates a tree viewer on the given tree control.
 * The viewer has no input, no content provider, a default label provider, 
 * no sorter, and no filters.
 *
 * @param tree the tree control
 */
public TreeViewer(Tree tree) {
	super();
	this.tree = tree;
	hookControl(tree);
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected void addSelectionListener(Control c, SelectionListener listener) {
	((Tree)c).addSelectionListener(listener);
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected void addTreeListener(Control c, TreeListener listener) {
	((Tree)c).addTreeListener(listener);
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected void doUpdateItem(Item item, Object element) {
	// update icon and label
	ILabelProvider provider = (ILabelProvider) getLabelProvider();
	CombinedLabel label = getLabel(element, provider);
	item.setText(label.getText());
	Image image = label.getImage();
	if (image != null) {
		item.setImage(image);
	}
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected Item[] getChildren(Widget o) {
	if (o instanceof TreeItem)
		return ((TreeItem) o).getItems();
	if (o instanceof Tree)
		return ((Tree) o).getItems();
	return null;
}
/* (non-Javadoc)
 * Method declared in Viewer.
 */
public Control getControl() {
	return tree;
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected boolean getExpanded(Item item) {
	return ((TreeItem) item).getExpanded();
}
/* (non-Javadoc)
 * Method declared in StructuredViewer.
 */
protected Item getItem(int x, int y) {
	return getTree().getItem(getTree().toControl(new Point(x, y)));
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected int getItemCount(Control widget) {
	return ((Tree) widget).getItemCount();
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected int getItemCount(Item item) {
	return ((TreeItem) item).getItemCount();
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected Item[] getItems(Item item) {
	return ((TreeItem) item).getItems();
}
/**
 * The tree viewer implementation of this <code>Viewer</code> framework
 * method returns the label provider, which in the case of tree
 * viewers will be an instance of <code>ILabelProvider</code>.
 */
public IBaseLabelProvider getLabelProvider() {
	return super.getLabelProvider();
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected Item getParentItem(Item item) {
	return ((TreeItem)item).getParentItem();
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected Item[] getSelection(Control widget) {
	return ((Tree) widget).getSelection();
}
/**
 * Returns this tree viewer's tree control.
 *
 * @return the tree control
 */
public Tree getTree() {
	return tree;
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected Item newItem(Widget parent, int flags, int ix) {
	TreeItem item;
	if (ix >= 0) {
		if (parent instanceof TreeItem)
			item = new TreeItem((TreeItem) parent, flags, ix);
		else
			item = new TreeItem((Tree) parent, flags, ix);
	} else {
		if (parent instanceof TreeItem)
			item = new TreeItem((TreeItem) parent, flags);
		else
			item = new TreeItem((Tree) parent, flags);
	}
	return item;
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected void removeAll(Control widget) {
	((Tree) widget).removeAll();
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected void setExpanded(Item node, boolean expand) {
	((TreeItem) node).setExpanded(expand);
}
/**
 * The tree viewer implementation of this <code>Viewer</code> framework
 * method ensures that the given label provider is an instance
 * of <code>ILabelProvider</code>.
 */
public void setLabelProvider(IBaseLabelProvider labelProvider) {
	Assert.isTrue(labelProvider instanceof ILabelProvider);
	super.setLabelProvider(labelProvider);
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected void setSelection(List items) {
	TreeItem[] newItems = new TreeItem[items.size()];
	items.toArray(newItems);
	getTree().setSelection(newItems);
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected void showItem(Item item) {
	getTree().showItem((TreeItem)item);
}
}
