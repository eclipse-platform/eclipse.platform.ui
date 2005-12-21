/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.List;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.custom.TableTreeEditor;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

/**
 * A concrete viewer based on a SWT <code>TableTree</code> control.
 * <p>
 * This class is not intended to be subclassed outside the viewer framework. 
 * It is designed to be instantiated with a pre-existing SWT table tree control and configured
 * with a domain-specific content provider, label provider, element filter (optional),
 * and element sorter (optional).
 * </p>
 * <p>
 * Content providers for table tree viewers must implement the <code>ITreeContentProvider</code>
 * interface.
 * </p>
 * <p>
 * Label providers for table tree viewers must implement either the <code>ITableLabelProvider</code>
 * or the <code>ILabelProvider</code> interface (see <code>TableTreeViewer.setLabelProvider</code> 
 * for more details).
 * </p>
 */
public class TableTreeViewer extends AbstractTreeViewer {
    /**
     * Internal table viewer implementation.
     */
    private TableEditorImpl tableViewerImpl;

    /**
     * This viewer's table tree control.
     */
    private TableTree tableTree;

    /**
     * This viewer's table tree editor.
     */
    private TableTreeEditor tableTreeEditor;

    /**
     * Private implementation class.
     */
    class TableTreeViewerImpl extends TableEditorImpl {
        public TableTreeViewerImpl(TableTreeViewer viewer) {
            super(viewer);
        }

        Rectangle getBounds(Item item, int columnNumber) {
            return ((TableTreeItem) item).getBounds(columnNumber);
        }

        int getColumnCount() {
            //getColumnCount() should be a API in TableTree.
            return getTableTree().getTable().getColumnCount();
        }

        Item[] getSelection() {
            return getTableTree().getSelection();
        }

        void setEditor(Control w, Item item, int columnNumber) {
            tableTreeEditor.setEditor(w, (TableTreeItem) item, columnNumber);
        }

        void setSelection(StructuredSelection selection, boolean b) {
            TableTreeViewer.this.setSelection(selection, b);
        }

        void showSelection() {
            getTableTree().showSelection();
        }

        void setLayoutData(CellEditor.LayoutData layoutData) {
            tableTreeEditor.horizontalAlignment = layoutData.horizontalAlignment;
            tableTreeEditor.grabHorizontal = layoutData.grabHorizontal;
            tableTreeEditor.minimumWidth = layoutData.minimumWidth;
        }

        void handleDoubleClickEvent() {
            Viewer viewer = getViewer();
            fireDoubleClick(new DoubleClickEvent(viewer, viewer.getSelection()));
            fireOpen(new OpenEvent(viewer, viewer.getSelection()));
        }
    }

    /**
     * Creates a table tree viewer on the given table tree control.
     * The viewer has no input, no content provider, a default label provider, 
     * no sorter, and no filters.
     *
     * @param tree the table tree control
     */
    public TableTreeViewer(TableTree tree) {
        super();
        tableTree = tree;
        hookControl(tree);
        tableTreeEditor = new TableTreeEditor(tableTree);
        tableViewerImpl = new TableTreeViewerImpl(this);
    }

    /**
     * Creates a table tree viewer on a newly-created table tree control under the given parent.
     * The table tree control is created using the SWT style bits <code>MULTI, H_SCROLL, V_SCROLL, and BORDER</code>.
     * The viewer has no input, no content provider, a default label provider, 
     * no sorter, and no filters.
     *
     * @param parent the parent control
     */
    public TableTreeViewer(Composite parent) {
        this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    }

    /**
     * Creates a table tree viewer on a newly-created table tree control under the given parent.
     * The table tree control is created using the given SWT style bits.
     * The viewer has no input, no content provider, a default label provider, 
     * no sorter, and no filters.
     *
     * @param parent the parent control
     * @param style the SWT style bits
     */
    public TableTreeViewer(Composite parent, int style) {
        this(new TableTree(parent, style));
    }

    /* (non-Javadoc)
     * Method declared on AbstractTreeViewer.
     */
    protected void addTreeListener(Control c, TreeListener listener) {
        ((TableTree) c).addTreeListener(listener);
    }

    /**
     * Cancels a currently active cell editor. All changes already done in the cell
     * editor are lost.
     */
    public void cancelEditing() {
        tableViewerImpl.cancelEditing();
    }

    /* (non-Javadoc)
     * Method declared on AbstractTreeViewer.
     */
    protected void doUpdateItem(Item item, Object element) {
        // update icon and label
        // Similar code in TableTreeViewer.doUpdateItem()
        IBaseLabelProvider prov = getLabelProvider();
        ITableLabelProvider tprov = null;
        
        
        if (prov instanceof ITableLabelProvider)
            tprov = (ITableLabelProvider) prov;
        
        int columnCount = tableTree.getTable().getColumnCount();
        TableTreeItem ti = (TableTreeItem) item;
        // Also enter loop if no columns added.  See 1G9WWGZ: JFUIF:WINNT - TableViewer with 0 columns does not work
        for (int column = 0; column < columnCount || column == 0; column++) {
            String text = "";//$NON-NLS-1$
            Image image = null;
            if (tprov != null) {
                text = tprov.getColumnText(element, column);
                image = tprov.getColumnImage(element, column);
            } else {
                if (column == 0) {
					ViewerLabel updateLabel = new ViewerLabel(item
							.getText(), item.getImage());
					buildLabel(updateLabel,element);
					
					//As it is possible for user code to run the event 
		            //loop check here.
					if (item.isDisposed()) {
		                unmapElement(element, item);
		                return;
		            }   
					
					text = updateLabel.getText();
					image = updateLabel.getImage();
				}
            }
            
            //Avoid setting text to null
            if(text == null)
            	text = ""; //$NON-NLS-1$
            
            ti.setText(column, text);
            // Apparently a problem to setImage to null if already null
            if (ti.getImage(column) != image)
                ti.setImage(column, image);
            
            getColorAndFontCollector().setFontsAndColors(element);
            getColorAndFontCollector().applyFontsAndColors(ti);
        }
   
    }

    /**
     * Starts editing the given element.
     *
     * @param element the element
     * @param column the column number
     */
    public void editElement(Object element, int column) {
        tableViewerImpl.editElement(element, column);
    }

    /**
     * Returns the cell editors of this viewer.
     *
     * @return the list of cell editors
     */
    public CellEditor[] getCellEditors() {
        return tableViewerImpl.getCellEditors();
    }

    /**
     * Returns the cell modifier of this viewer.
     *
     * @return the cell modifier
     */
    public ICellModifier getCellModifier() {
        return tableViewerImpl.getCellModifier();
    }

    /* (non-Javadoc)
     * Method declared on AbstractTreeViewer.
     */
    protected Item[] getChildren(Widget o) {
        if (o instanceof TableTreeItem)
            return ((TableTreeItem) o).getItems();
        if (o instanceof TableTree)
            return ((TableTree) o).getItems();
        return null;
    }
   
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTreeViewer#getChild(org.eclipse.swt.widgets.Widget, int)
     */
    protected Item getChild (Widget widget, int index) {
      if (widget instanceof TableTreeItem)
        return ((TableTreeItem) widget).getItem (index);
      if (widget instanceof TableTree)
        return ((TableTree) widget).getItem (index);
      return null;
    }

    /**
     * Returns the column properties of this viewer.
     * The properties must correspond with the columns of the table control.
     * They are used to identify the column in a cell modifier.
     *
     * @return the list of column properties
     */
    public Object[] getColumnProperties() {
        return tableViewerImpl.getColumnProperties();
    }

    /* (non-Javadoc)
     * Method declared on Viewer.
     */
    public Control getControl() {
        return tableTree;
    }

    /**
     * Returns the element with the given index from this viewer.
     * Returns <code>null</code> if the index is out of range.
     * <p>
     * This method is internal to the framework.
     * </p>
     *
     * @param index the zero-based index
     * @return the element at the given index, or <code>null</code> if the
     *   index is out of range
     */
    public Object getElementAt(int index) {
        // XXX: Workaround for 1GBCSB1: SWT:WIN2000 - TableTree should have getItem(int index)
        TableTreeItem i = tableTree.getItems()[index];
        if (i != null)
            return i.getData();
        return null;
    }

    /* (non-Javadoc)
     * Method declared on AbstractTreeViewer.
     */
    protected boolean getExpanded(Item item) {
        return ((TableTreeItem) item).getExpanded();
    }

    /* (non-Javadoc)
     * Method declared on StructuredViewer.
     */
    protected Item getItem(int x, int y) {
        // XXX: Workaround for 1GBCSHG: SWT:WIN2000 - TableTree should have getItem(Point point)
        return getTableTree().getTable().getItem(
                getTableTree().toControl(new Point(x, y)));
    }

    /* (non-Javadoc)
     * Method declared on AbstractTreeViewer.
     */
    protected int getItemCount(Control widget) {
        return ((TableTree) widget).getItemCount();
    }

    /* (non-Javadoc)
     * Method declared on AbstractTreeViewer.
     */
    protected int getItemCount(Item item) {
        return ((TableTreeItem) item).getItemCount();
    }

    /* (non-Javadoc)
     * Method declared on AbstractTreeViewer.
     */
    protected org.eclipse.swt.widgets.Item[] getItems(
            org.eclipse.swt.widgets.Item item) {
        return ((TableTreeItem) item).getItems();
    }

    /**
     * The table tree viewer implementation of this <code>Viewer</code> framework
     * method returns the label provider, which in the case of table tree
     * viewers will be an instance of either <code>ITableLabelProvider</code>
     * or <code>ILabelProvider</code>.
     * If it is an <code>ITableLabelProvider</code>, then it provides a
     * separate label text and image for each column. If it is an 
     * <code>ILabelProvider</code>, then it provides only the label text 
     * and image for the first column, and any remaining columns are blank.
     */
    public IBaseLabelProvider getLabelProvider() {
        return super.getLabelProvider();
    }

    /* (non-Javadoc)
     * Method declared on AbstractTreeViewer.
     */
    protected Item getParentItem(Item item) {
        return ((TableTreeItem) item).getParentItem();
    }

    /* (non-Javadoc)
     * Method declared on AbstractTreeViewer.
     */
    protected Item[] getSelection(Control widget) {
        return ((TableTree) widget).getSelection();
    }

    /**
     * Returns this table tree viewer's table tree control.
     *
     * @return the table tree control
     */
    public TableTree getTableTree() {
        return tableTree;
    }

    /* (non-Javadoc)
     * Method declared on AbstractTreeViewer.
     */
    protected void hookControl(Control control) {
        super.hookControl(control);
        tableTree.getTable().addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                /* If user clicked on the [+] or [-], do not activate CellEditor. */
                //XXX: This code should not be here. SWT should either have support to see
                //if the user clicked on the [+]/[-] or manage the table editor 
                //activation
                org.eclipse.swt.widgets.TableItem[] items = tableTree
                        .getTable().getItems();
                for (int i = 0; i < items.length; i++) {
                    Rectangle rect = items[i].getImageBounds(0);
                    if (rect.contains(e.x, e.y))
                        return;
                }

                tableViewerImpl.handleMouseDown(e);
            }
        });
    }

    /**
     * Returns whether there is an active cell editor.
     *
     * @return <code>true</code> if there is an active cell editor, and 
     *   <code>false</code> otherwise
     */
    public boolean isCellEditorActive() {
        return tableViewerImpl.isCellEditorActive();
    }

    /* (non-Javadoc)
     * Method declared in AbstractTreeViewer.
     */
    protected Item newItem(Widget parent, int flags, int ix) {
        TableTreeItem item;
        if (ix >= 0) {
            if (parent instanceof TableTreeItem)
                item = new TableTreeItem((TableTreeItem) parent, flags, ix);
            else
                item = new TableTreeItem((TableTree) parent, flags, ix);
        } else {
            if (parent instanceof TableTreeItem)
                item = new TableTreeItem((TableTreeItem) parent, flags);
            else
                item = new TableTreeItem((TableTree) parent, flags);
        }
        return item;
    }

    /* (non-Javadoc)
     * Method declared in AbstractTreeViewer.
     */
    protected void removeAll(Control widget) {
        ((TableTree) widget).removeAll();
    }

    /**
     * Sets the cell editors of this table viewer.
     *
     * @param editors the list of cell editors
     */
    public void setCellEditors(CellEditor[] editors) {
        tableViewerImpl.setCellEditors(editors);
    }

    /**
     * Sets the cell modifier of this table viewer.
     *
     * @param modifier the cell modifier
     */
    public void setCellModifier(ICellModifier modifier) {
        tableViewerImpl.setCellModifier(modifier);
    }

    /**
     * Sets the column properties of this table viewer.
     * The properties must correspond with the columns of the table control.
     * They are used to identify the column in a cell modifier.
     *
     * @param columnProperties the list of column properties
     */
    public void setColumnProperties(String[] columnProperties) {
        tableViewerImpl.setColumnProperties(columnProperties);
    }

    /* (non-Javadoc)
     * Method declared in AbstractTreeViewer.
     */
    protected void setExpanded(Item node, boolean expand) {
        ((TableTreeItem) node).setExpanded(expand);
    }

    /**
     * The table tree viewer implementation of this <code>Viewer</code> framework
     * method ensures that the given label provider is an instance
     * of either <code>ITableLabelProvider</code> or <code>ILabelProvider</code>.
     * If it is an <code>ITableLabelProvider</code>, then it provides a
     * separate label text and image for each column. If it is an 
     * <code>ILabelProvider</code>, then it provides only the label text 
     * and image for the first column, and any remaining columns are blank.
     */
    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        Assert.isTrue(labelProvider instanceof ITableLabelProvider
                || labelProvider instanceof ILabelProvider);
        super.setLabelProvider(labelProvider);
    }

    /* (non-Javadoc)
     * Method declared in AbstractTreeViewer.
     */
    protected void setSelection(List items) {
        TableTreeItem[] newItems = new TableTreeItem[items.size()];
        items.toArray(newItems);
        getTableTree().setSelection(newItems);
    }

    /* (non-Javadoc)
     * Method declared in AbstractTreeViewer.
     */
    protected void showItem(Item item) {
        getTableTree().showItem((TableTreeItem) item);
    }
}
