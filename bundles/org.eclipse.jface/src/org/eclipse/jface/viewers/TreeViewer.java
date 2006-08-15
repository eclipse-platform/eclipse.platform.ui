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

package org.eclipse.jface.viewers;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * A concrete viewer based on an SWT <code>Tree</code> control.
 * <p>
 * This class is not intended to be subclassed outside the viewer framework. It
 * is designed to be instantiated with a pre-existing SWT tree control and
 * configured with a domain-specific content provider, label provider, element
 * filter (optional), and element sorter (optional).
 * </p>
 * <p>
 * Content providers for tree viewers must implement either the
 * {@link ITreeContentProvider} interface or (as of 3.2) the
 * {@link ILazyTreeContentProvider} interface. If the content provider is an
 * <code>ILazyTreeContentProvider</code>, the underlying Tree must be created
 * using the {@link SWT#VIRTUAL} style bit, and the tree viewer will not support
 * sorting or filtering.
 * </p>
 */
public class TreeViewer extends AbstractTreeViewer {

	/**
	 * Internal tree viewer implementation.
	 */
	private TreeEditorImpl treeViewerImpl;

	/**
	 * This viewer's control.
	 */
	private Tree tree;

	/**
	 * This viewer's tree editor.
	 */
	private TreeEditor treeEditor;

	/**
	 * Flag for whether the tree has been disposed of.
	 */
	private boolean treeIsDisposed = false;

	/**
	 * Creates a tree viewer on a newly-created tree control under the given
	 * parent. The tree control is created using the SWT style bits
	 * <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>. The
	 * viewer has no input, no content provider, a default label provider, no
	 * sorter, and no filters.
	 * 
	 * @param parent
	 *            the parent control
	 */
	public TreeViewer(Composite parent) {
		this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
	}

	/**
	 * Creates a tree viewer on a newly-created tree control under the given
	 * parent. The tree control is created using the given SWT style bits. The
	 * viewer has no input, no content provider, a default label provider, no
	 * sorter, and no filters.
	 * 
	 * @param parent
	 *            the parent control
	 * @param style
	 *            the SWT style bits used to create the tree.
	 */
	public TreeViewer(Composite parent, int style) {
		this(new Tree(parent, style));
	}

	/**
	 * Creates a tree viewer on the given tree control. The viewer has no input,
	 * no content provider, a default label provider, no sorter, and no filters.
	 * 
	 * @param tree
	 *            the tree control
	 */
	public TreeViewer(Tree tree) {
		super();
		this.tree = tree;
		hookControl(tree);
		treeEditor = new TreeEditor(tree);
		initTreeViewerImpl();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected void addTreeListener(Control c, TreeListener listener) {
		((Tree) c).addTreeListener(listener);
	}

	/**
	 * Cancels a currently active cell editor. All changes already done in the
	 * cell editor are lost.
	 * 
	 * @since 3.1
	 */
	public void cancelEditing() {
		treeViewerImpl.cancelEditing();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected void doUpdateItem(final Item item, Object element) {
 		if (!(item instanceof TreeItem)) {
			return;
		}
		TreeItem treeItem = (TreeItem) item;
		if (treeItem.isDisposed()) {
			unmapElement(element, treeItem);
			return;
		}

		int columnCount = getTree().getColumnCount();
		if(columnCount == 0)//If no columns are created then fake one
			columnCount = 1;

		for (int column = 0; column < columnCount; column++) {
			ColumnViewerPart columnViewer = getColumnViewer(column);
			columnViewer.refresh(getRowPartFromItem(treeItem),column);

			// As it is possible for user code to run the event
			// loop check here.
			if (item.isDisposed()) {
				unmapElement(element, item);
				return;
			}

		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnViewer#getColumnViewerOwner(int)
	 */
	protected Widget getColumnViewerOwner(int columnIndex) {
		if( columnIndex < 0 || columnIndex > getTree().getColumnCount() ) {
			return null;
		}
		
		if (getTree().getColumnCount() == 0)// Hang it off the table if it
			return getTree();
		
		return getTree().getColumn(columnIndex);
	}

	/**
	 * Override to handle tree paths.
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#buildLabel(org.eclipse.jface.viewers.ViewerLabel,
	 *      java.lang.Object)
	 */
	protected void buildLabel(ViewerLabel updateLabel, Object elementOrPath) {
		Object element;
		if (elementOrPath instanceof TreePath) {
			TreePath path = (TreePath) elementOrPath;
			IBaseLabelProvider provider = getLabelProvider();
			if (provider instanceof ITreePathLabelProvider) {
				ITreePathLabelProvider pprov = (ITreePathLabelProvider) provider;
				buildLabel(updateLabel, path, pprov);
				return;
			}
			element = path.getLastSegment();
		} else {
			element = elementOrPath;
		}
		super.buildLabel(updateLabel, element);
	}

	/**
	 * Starts editing the given element.
	 * 
	 * @param element
	 *            the element
	 * @param column
	 *            the column number
	 * @since 3.1
	 */
	public void editElement(Object element, int column) {
		treeViewerImpl.editElement(element, column);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnViewer#getCellEditors()
	 */
	public CellEditor[] getCellEditors() {
		return treeViewerImpl.getCellEditors();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnViewer#getCellModifier()
	 */
	public ICellModifier getCellModifier() {
		return treeViewerImpl.getCellModifier();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected Item[] getChildren(Widget o) {
		if (o instanceof TreeItem) {
			return ((TreeItem) o).getItems();
		}
		if (o instanceof Tree) {
			return ((Tree) o).getItems();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnViewer#getColumnProperties()
	 */
	public Object[] getColumnProperties() {
		return treeViewerImpl.getColumnProperties();
	}

	/*
	 * (non-Javadoc) Method declared in Viewer.
	 */
	public Control getControl() {
		return tree;
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected boolean getExpanded(Item item) {
		return ((TreeItem) item).getExpanded();
	}

	/**
	 * Returns the item at the given display-relative coordinates, or
	 * <code>null</code> if there is no item at that location.
	 * <p>
	 * The default implementation of this method returns <code>null</code>.
	 * </p>
	 * 
	 * @param x
	 *            horizontal coordinate
	 * @param y
	 *            vertical coordinate
	 * @return the item, or <code>null</code> if there is no item at the given
	 *         coordinates
	 */
	protected Item getItem(int x, int y) {
		return getTree().getItem(getTree().toControl(new Point(x, y)));
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected int getItemCount(Control widget) {
		return ((Tree) widget).getItemCount();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected int getItemCount(Item item) {
		return ((TreeItem) item).getItemCount();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected Item[] getItems(Item item) {
		return ((TreeItem) item).getItems();
	}

	/**
	 * The tree viewer implementation of this <code>Viewer</code> framework
	 * method ensures that the given label provider is an instance of either
	 * <code>ITableLabelProvider</code> or <code>ILabelProvider</code>. If
	 * it is an <code>ITableLabelProvider</code>, then it provides a separate
	 * label text and image for each column. If it is an
	 * <code>ILabelProvider</code>, then it provides only the label text and
	 * image for the first column, and any remaining columns are blank.
	 */
	public IBaseLabelProvider getLabelProvider() {
		return super.getLabelProvider();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected Item getParentItem(Item item) {
		return ((TreeItem) item).getParentItem();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#hookControl(org.eclipse.swt.widgets.Control)
	 */
	protected void hookControl(Control control) {
		super.hookControl(control);
		Tree treeControl = (Tree) control;
		treeControl.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				treeViewerImpl.handleMouseDown(e);
			}
		});
		if ((treeControl.getStyle() & SWT.VIRTUAL) != 0) {
			treeControl.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					treeIsDisposed = true;
					unmapAllElements();
				}
			});
			treeControl.addListener(SWT.SetData, new Listener() {

				public void handleEvent(Event event) {
					if (getContentProvider() instanceof ILazyTreeContentProvider) {
						ILazyTreeContentProvider lazyContentProvider = (ILazyTreeContentProvider) getContentProvider();
						TreeItem item = (TreeItem) event.item;
						TreeItem parentItem = item.getParentItem();
						Object parent;
						int index;
						if (parentItem != null) {
							parent = parentItem.getData();
							index = parentItem.indexOf(item);
						} else {
							parent = getInput();
							index = getTree().indexOf(item);
						}
						lazyContentProvider.updateElement(parent, index);
					}
				}
			});
		}
	}

	/**
	 * Initializes the tree viewer implementation.
	 */
	private void initTreeViewerImpl() {
		treeViewerImpl = new TreeEditorImpl(this) {
			Rectangle getBounds(Item item, int columnNumber) {
				return ((TreeItem) item).getBounds(columnNumber);
			}

			int getColumnCount() {
				return getTree().getColumnCount();
			}

			Item[] getSelection() {
				return getTree().getSelection();
			}

			void setEditor(Control w, Item item, int columnNumber) {
				treeEditor.setEditor(w, (TreeItem) item, columnNumber);
			}

			void setSelection(IStructuredSelection selection, boolean b) {
				TreeViewer.this.setSelection(selection, b);
			}

			void showSelection() {
				getTree().showSelection();
			}

			void setLayoutData(CellEditor.LayoutData layoutData) {
				treeEditor.grabHorizontal = layoutData.grabHorizontal;
				treeEditor.horizontalAlignment = layoutData.horizontalAlignment;
				treeEditor.minimumWidth = layoutData.minimumWidth;
			}

			void handleDoubleClickEvent() {
				Viewer viewer = getViewer();
				fireDoubleClick(new DoubleClickEvent(viewer, viewer
						.getSelection()));
				fireOpen(new OpenEvent(viewer, viewer.getSelection()));
			}
		};
	}

	/**
	 * Returns whether there is an active cell editor.
	 * 
	 * @return <code>true</code> if there is an active cell editor, and
	 *         <code>false</code> otherwise
	 * @since 3.1
	 */
	public boolean isCellEditorActive() {
		return treeViewerImpl.isCellEditorActive();
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected Item newItem(Widget parent, int flags, int ix) {
		TreeItem item;
		
		if (parent instanceof TreeItem) {
			item = (TreeItem)createNewRowPart(getRowPartFromItem(parent),flags, ix).getItem();
		} else {
			item = (TreeItem)createNewRowPart(null,flags, ix).getItem();
		}
		
		return item;
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected void removeAll(Control widget) {
		((Tree) widget).removeAll();
	}

	/**
	 * Sets the cell editors of this tree viewer.
	 * 
	 * @param editors
	 *            the list of cell editors
	 * @since 3.1
	 */
	public void setCellEditors(CellEditor[] editors) {
		treeViewerImpl.setCellEditors(editors);
	}

	/**
	 * Sets the cell modifier of this tree viewer.
	 * 
	 * @param modifier
	 *            the cell modifier
	 * @since 3.1
	 */
	public void setCellModifier(ICellModifier modifier) {
		treeViewerImpl.setCellModifier(modifier);
	}

	/**
	 * Sets the column properties of this tree viewer. The properties must
	 * correspond with the columns of the tree control. They are used to
	 * identify the column in a cell modifier.
	 * 
	 * @param columnProperties
	 *            the list of column properties
	 * @since 3.1
	 */
	public void setColumnProperties(String[] columnProperties) {
		treeViewerImpl.setColumnProperties(columnProperties);
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected void setExpanded(Item node, boolean expand) {
		((TreeItem) node).setExpanded(expand);
		if (getContentProvider() instanceof ILazyTreeContentProvider) {
			// force repaints to happen
			getControl().update();
		}
	}

	/**
	 * The tree viewer implementation of this <code>Viewer</code> framework
	 * method ensures that the given label provider is an instance of either
	 * <code>ITableLabelProvider</code> or <code>ILabelProvider</code>.
	 * <p>
	 * If the label provider is an {@link ITableLabelProvider}, then it
	 * provides a separate label text and image for each column. Implementers of
	 * <code>ITableLabelProvider</code> may also implement
	 * {@link ITableColorProvider} and/or {@link ITableFontProvider} to provide
	 * colors and/or fonts. Note that the underlying {@link Tree} must be
	 * configured with {@link TreeColumn} objects in this case.
	 * </p>
	 * <p>
	 * If the label provider is an <code>ILabelProvider</code>, then it
	 * provides only the label text and image for the first column, and any
	 * remaining columns are blank. Implementers of <code>ILabelProvider</code>
	 * may also implement {@link IColorProvider} and/or {@link IFontProvider} to
	 * provide colors and/or fonts.
	 * </p>
	 */
	public void setLabelProvider(IBaseLabelProvider labelProvider) {
		Assert.isTrue(labelProvider instanceof ITableLabelProvider
				|| labelProvider instanceof ILabelProvider);
		clearColumnParts();//Clear before refresh
		super.setLabelProvider(labelProvider);
		
	}

	/**
	 * Clear the viewer parts for the columns
	 */
	private void clearColumnParts() {
		TreeColumn[] columns = getTree().getColumns();
		if(columns.length == 0)
			getTree().setData(ColumnViewerPart.COLUMN_VIEWER_KEY,null);
		else{
			for (int i = 0; i < columns.length; i++) {
				columns[i].setData(ColumnViewerPart.COLUMN_VIEWER_KEY,null);
				
			}
		}
		
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected void setSelection(List items) {

		Item[] current = getSelection(getTree());

		// Don't bother resetting the same selection
		if (isSameSelection(items, current)) {
			return;
		}

		TreeItem[] newItems = new TreeItem[items.size()];
		items.toArray(newItems);
		getTree().setSelection(newItems);
	}

	/**
	 * Returns <code>true</code> if the given list and array of items refer to
	 * the same model elements. Order is unimportant.
	 * 
	 * @param items
	 *            the list of items
	 * @param current
	 *            the array of items
	 * @return <code>true</code> if the refer to the same elements,
	 *         <code>false</code> otherwise
	 * 
	 * @since 3.1
	 */
	protected boolean isSameSelection(List items, Item[] current) {
		// If they are not the same size then they are not equivalent
		int n = items.size();
		if (n != current.length) {
			return false;
		}

		CustomHashtable itemSet = newHashtable(n * 2 + 1);
		for (Iterator i = items.iterator(); i.hasNext();) {
			Item item = (Item) i.next();
			Object element = item.getData();
			itemSet.put(element, element);
		}

		// Go through the items of the current collection
		// If there is a mismatch return false
		for (int i = 0; i < current.length; i++) {
			if (current[i].getData() == null
					|| !itemSet.containsKey(current[i].getData())) {
				return false;
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc) Method declared in AbstractTreeViewer.
	 */
	protected void showItem(Item item) {
		getTree().showItem((TreeItem) item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#getChild(org.eclipse.swt.widgets.Widget,
	 *      int)
	 */
	protected Item getChild(Widget widget, int index) {
		if (widget instanceof TreeItem) {
			return ((TreeItem) widget).getItem(index);
		}
		if (widget instanceof Tree) {
			return ((Tree) widget).getItem(index);
		}
		return null;
	}

	protected void assertContentProviderType(IContentProvider provider) {
		if (provider instanceof ILazyTreeContentProvider) {
			return;
		}
		super.assertContentProviderType(provider);
	}

	protected Object[] getRawChildren(Object parent) {
		if (getContentProvider() instanceof ILazyTreeContentProvider) {
			return new Object[0];
		}
		return super.getRawChildren(parent);
	}

	/**
	 * For a TreeViewer with a tree with the VIRTUAL style bit set, set the
	 * number of children of the given element. To set the number of children of
	 * the invisible root of the tree, the input object is passed as the
	 * element.
	 * 
	 * @param element
	 * @param count
	 * 
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is no guarantee that this API will
	 * remain unchanged during the 3.2 release cycle. Please do not use this API
	 * without consulting with the Platform/UI team.
	 * </p>
	 * 
	 * @since 3.2
	 */
	public void setChildCount(Object element, int count) {
		Tree tree = (Tree) doFindInputItem(element);
		if (tree != null) {
			tree.setItemCount(count);
			return;
		}
		Widget[] items = findItems(element);
		for (int i = 0; i < items.length; i++) {
			TreeItem treeItem = (TreeItem) items[i];
			treeItem.setItemCount(count);
		}
	}

	/**
	 * For a TreeViewer with a tree with the VIRTUAL style bit set, replace the
	 * given parent's child at index with the given element. If the given parent
	 * is this viewer's input, this will replace the root element at the given
	 * index.
	 * <p>
	 * This method should be called by implementers of ILazyTreeContentProvider
	 * to populate this viewer.
	 * </p>
	 * 
	 * @param parent
	 *            the parent of the element that should be updated
	 * @param index
	 *            the index in the parent's children
	 * @param element
	 *            the new element
	 * 
	 * @see #setChildCount(Object, int)
	 * @see ILazyTreeContentProvider
	 * 
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is no guarantee that this API will
	 * remain unchanged during the 3.2 release cycle. Please do not use this API
	 * without consulting with the Platform/UI team.
	 * </p>
	 * 
	 * @since 3.2
	 */
	public void replace(Object parent, int index, Object element) {
		if (parent.equals(getInput())) {
			if (index < tree.getItemCount()) {
				updateItem(tree.getItem(index), element);
			}
		} else {
			Widget[] parentItems = findItems(parent);
			for (int i = 0; i < parentItems.length; i++) {
				TreeItem parentItem = (TreeItem) parentItems[i];
				if (index < parentItem.getItemCount()) {
					updateItem(parentItem.getItem(index), element);
				}
			}
		}
	}

	public boolean isExpandable(Object element) {
		if (getContentProvider() instanceof ILazyTreeContentProvider) {
			TreeItem treeItem = (TreeItem) internalExpand(element, false);
			if (treeItem == null) {
				return false;
			}
			virtualMaterializeItem(treeItem);
			return treeItem.getItemCount() > 0;
		}
		return super.isExpandable(element);
	}

	protected Object getParentElement(Object element) {
		if (!(element instanceof TreePath)
				&& (getContentProvider() instanceof ILazyTreeContentProvider)) {
			ILazyTreeContentProvider lazyTreeContentProvider = (ILazyTreeContentProvider) getContentProvider();
			return lazyTreeContentProvider.getParent(element);
		}
		return super.getParentElement(element);
	}

	protected void createChildren(Widget widget) {
		if (getContentProvider() instanceof ILazyTreeContentProvider) {
			final Item[] tis = getChildren(widget);
			if (tis != null && tis.length > 0) {
				// children already there, touch them
				for (int i = 0; i < tis.length; i++) {
					tis[i].getText();
				}
				return;
			}
			ILazyTreeContentProvider lazyTreeContentProvider = (ILazyTreeContentProvider) getContentProvider();
			Object element = widget.getData();
			if (element == null && widget instanceof TreeItem) {
				// parent has not been materialized
				virtualMaterializeItem((TreeItem) widget);
				// try getting the element now that updateElement was called
				element = widget.getData();
			}
			TreeItem[] children;
			if (widget instanceof Tree) {
				children = ((Tree) widget).getItems();
			} else {
				children = ((TreeItem) widget).getItems();
			}
			if (element != null && children.length > 0) {
				for (int i = 0; i < children.length; i++) {
					lazyTreeContentProvider.updateElement(element, i);
				}
			}
			return;
		}
		super.createChildren(widget);
	}

	protected void internalAdd(Widget widget, Object parentElement,
			Object[] childElements) {
		if (getContentProvider() instanceof ILazyTreeContentProvider) {
			if (widget instanceof TreeItem) {
				TreeItem ti = (TreeItem) widget;
				int count = ti.getItemCount() + childElements.length;
				ti.setItemCount(count);
				ti.clearAll(false);
			} else {
				Tree t = (Tree) widget;
				t.setItemCount(t.getItemCount() + childElements.length);
				t.clearAll(false);
			}
			return;
		}
		super.internalAdd(widget, parentElement, childElements);
	}

	private void virtualMaterializeItem(TreeItem treeItem) {
		if (treeItem.getData() != null) {
			// already materialized
			return;
		}
		if (!(getContentProvider() instanceof ILazyTreeContentProvider)) {
			return;
		}
		ILazyTreeContentProvider lazyTreeContentProvider = (ILazyTreeContentProvider) getContentProvider();
		int index;
		Widget parent = treeItem.getParentItem();
		if (parent == null) {
			parent = treeItem.getParent();
		}
		Object parentElement = parent.getData();
		if (parentElement != null) {
			if (parent instanceof Tree) {
				index = ((Tree) parent).indexOf(treeItem);
			} else {
				index = ((TreeItem) parent).indexOf(treeItem);
			}
			lazyTreeContentProvider.updateElement(parentElement, index);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#internalRefreshStruct(org.eclipse.swt.widgets.Widget,
	 *      java.lang.Object, boolean)
	 */
	protected void internalRefreshStruct(Widget widget, Object element,
			boolean updateLabels) {
		if (getContentProvider() instanceof ILazyTreeContentProvider) {
			// first phase: update child counts
			virtualRefreshChildCounts(widget, element);
			// second phase: update labels
			if (updateLabels) {
				if (widget instanceof Tree) {
					((Tree) widget).clearAll(true);
				} else if (widget instanceof TreeItem) {
					((TreeItem) widget).clearAll(true);
				}
			}
			return;
		}
		super.internalRefreshStruct(widget, element, updateLabels);
	}

	/**
	 * Traverses the visible (expanded) part of the tree and updates child
	 * counts.
	 * 
	 * @param widget
	 * @param element
	 * @param updateLabels
	 */
	private void virtualRefreshChildCounts(Widget widget, Object element) {
		ILazyTreeContentProvider lazyTreeContentProvider = (ILazyTreeContentProvider) getContentProvider();
		if (widget instanceof Tree || ((TreeItem) widget).getExpanded()) {
			// widget shows children - it is safe to call getChildren
			if (element != null) {
				lazyTreeContentProvider.updateChildCount(element,
						getChildren(widget).length);
			} else {
				if (widget instanceof Tree) {
					((Tree) widget).setItemCount(0);
				} else {
					((TreeItem) widget).setItemCount(0);
				}
			}
			// need to get children again because they might have been updated
			// through a callback to setChildCount.
			Item[] items = getChildren(widget);
			for (int i = 0; i < items.length; i++) {
				Item item = items[i];
				Object data = item.getData();
				if (data != null) {
					virtualRefreshChildCounts(item, data);
				}
			}
		}
	}

	/*
	 * To unmap elements correctly, we need to register a dispose listener with
	 * the item if the tree is virtual.
	 */
	protected void mapElement(Object element, final Widget item) {
		super.mapElement(element, item);
		// make sure to unmap elements if the tree is virtual
		if ((getTree().getStyle() & SWT.VIRTUAL) != 0) {
			item.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (!treeIsDisposed) {
						Object data = item.getData();
						if (usingElementMap() && data != null) {
							unmapElement(data, item);
						}
					}
				}
			});
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnViewer#getRowPartFromItem(org.eclipse.swt.widgets.Widget)
	 */
	protected RowPart getRowPartFromItem(Widget item) {
		RowPart part = (RowPart)item.getData(RowPart.ROWPART_KEY);
		
		if( part == null ) {
			part = new TreeRowPart(((TreeItem)item));
		}
		
		return part;
	}
		
	/**
	 * Create a new RowPart at rowIndex
	 * @param parent
	 * @param style
	 * @param rowIndex
	 * @return RowPart
	 */
	private RowPart createNewRowPart(RowPart parent, int style, int rowIndex) {
		if( parent == null ) {
			if( rowIndex >= 0 ) {
				return getRowPartFromItem(new TreeItem(tree,style,rowIndex));
			}
			return getRowPartFromItem(new TreeItem(tree,style));
		}
		
		if( rowIndex >= 0 ) {
			return getRowPartFromItem(new TreeItem((TreeItem)parent.getItem(),SWT.NONE,rowIndex));
		}
		
		return getRowPartFromItem(new TreeItem((TreeItem)parent.getItem(),SWT.NONE));
	}
	
	protected ColumnViewerPart createColumnViewer(Widget columnOwner, ViewerLabelProvider labelProvider) {
		if( columnOwner instanceof TreeColumn ) {
			return new TreeColumnViewerPart((TreeColumn)columnOwner,labelProvider);
		}
		
		return super.createColumnViewer(columnOwner, labelProvider);
	}
}
