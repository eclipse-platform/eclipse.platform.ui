/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousLabelAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditor;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditorFactoryAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A tree viewer that retrieves children and labels asynchronously via adapters
 * and supports duplicate elements in the tree with different parents.
 * Retrieving children and labels asynchronously allows for arbitrary latency
 * without blocking the UI thread.
 * <p>
 * Clients may instantiate and subclass this class.
 * </p>
 * 
 * @since 3.2
 */
public class AsynchronousTreeViewer extends AsynchronousViewer {

	/**
     * The tree
     */
    private Tree fTree;
    
    /**
     * Cell editing
     */
    private TreeEditorImpl fTreeEditorImpl;
    private TreeEditor fTreeEditor;

    /**
     * Collection of tree paths to be expanded. A path is removed from the
     * collection when it is expanded. The entire list is cleared when the input
     * to the viewer changes.
     */
    private List fPendingExpansion = new ArrayList();
    
    /**
     * Current column presentation or <code>null</code>
     */
    private IColumnPresentation fColumnPresentation = null;
    
    /**
     * Current column editor or <code>null</code>
     */
    private IColumnEditor fColumnEditor = null;
    
    /**
     * Map of columns presentation id to its visible columns id's (String[])
     * When a columns presentation is not in the map, default settings are used.
     */
    private Map fVisibleColumns = new HashMap();
    
    /**
     * Map of column id's to persisted sizes
     */
    private Map fColumnSizes = new HashMap();
    
    /**
     * Map of column presentation id's to an array of integers representing the column order
     * for that presentation, or <code>null</code> if default.
     */
    private Map fColumnOrder = new HashMap();
    
    /**
     * Map of column presentation id to whether columns should be displayed
     * for that presentation (the user can toggle columns on/off when a 
     * presentation is optional.
     */
    private Map fShowColumns = new HashMap();
        
    /**
	 * Memento type for column sizes. Sizes are keyed by column presentation id 
	 */
	private static final String COLUMN_SIZES = "COLUMN_SIZES"; //$NON-NLS-1$
	/**
	 * Memento type for the column order for a presentation context.
	 * A memento is created for each column presentation
	 */
	private static final String COLUMN_ORDER = "COLUMN_ORDER";     //$NON-NLS-1$	
	/**
	 * Memento type for the visible columns for a presentation context.
	 * A memento is created for each column presentation keyed by column number
	 */
	private static final String VISIBLE_COLUMNS = "VISIBLE_COLUMNS";     //$NON-NLS-1$
	/**
	 * Memento type for whether columns are visible for a presentation context.
	 * Booleans are keyed by column presentation id
	 */
	private static final String SHOW_COLUMNS = "SHOW_COLUMNS";     //$NON-NLS-1$	
	/**
	 * Memento key for the number of visible columns in a VISIBLE_COLUMNS memento
	 * or for the width of a column
	 */
	private static final String SIZE = "SIZE";	 //$NON-NLS-1$
	/**
	 * Memento key prefix a visible column
	 */
	private static final String COLUMN = "COLUMN";	 //$NON-NLS-1$	
	
	/**
	 * Persist column sizes when they change.
	 * 
	 * @since 3.2
	 */
	class ColumnListener implements ControlListener {
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
		 */
		public void controlMoved(ControlEvent e) {
			persistColumnOrder();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
		 */
		public void controlResized(ControlEvent e) {
			persistColumnSizes();
		}
	}
	
	private ColumnListener fListener = new ColumnListener();
    
    /**
     * Creates an asynchronous tree viewer on a newly-created tree control under
     * the given parent. The tree control is created using the SWT style bits
     * <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>. The
     * viewer has no input, no content provider, a default label provider, no
     * sorter, and no filters.
     * 
     * @param parent
     *            the parent control
     */
    public AsynchronousTreeViewer(Composite parent) {
        this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL);
    }

    /**
     * Creates an asynchronous tree viewer on a newly-created tree control under
     * the given parent. The tree control is created using the given SWT style
     * bits. The viewer has no input.
     * 
     * @param parent
     *            the parent control
     * @param style
     *            the SWT style bits used to create the tree.
     */
    public AsynchronousTreeViewer(Composite parent, int style) {
        this(new Tree(parent, style));
    }

    /**
     * Creates an asynchronous tree viewer on the given tree control. The viewer
     * has no input, no content provider, a default label provider, no sorter,
     * and no filters.
     * 
     * @param tree
     *            the tree control
     */
    public AsynchronousTreeViewer(Tree tree) {
        super();
        Assert.isTrue((tree.getStyle() & SWT.VIRTUAL) != 0);
        fTree = tree;
        hookControl(fTree);
        fTreeEditor = new TreeEditor(tree);
		initTreeViewerImpl();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.StructuredViewer#hookControl(org.eclipse.swt.widgets.Control)
     */
    protected void hookControl(Control control) {
		super.hookControl(control);
		Tree tree = (Tree)control;
        tree.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
            	if (isShowColumns()) {
	            	Item[] items = fTreeEditorImpl.getSelection();
	            	if (items.length > 0) {
	            		TreeItem treeItem = (TreeItem) items[0];	            		
		            	if (treeItem != null) { 
		            		Object element = treeItem.getData();
		            		updateColumnEditor(element);
		            		if (fColumnEditor != null) {
				            	int columnToEdit = -1;
				                int columns = fTree.getColumnCount();
				                if (columns == 0) {
				                    // If no TreeColumn, Tree acts as if it has a single column
				                    // which takes the whole width.
				                    columnToEdit = 0;
				                } else {
				                    columnToEdit = -1;
				                    for (int i = 0; i < columns; i++) {
				                        Rectangle bounds = fTreeEditorImpl.getBounds(treeItem, i);
				                        if (bounds.contains(e.x, e.y)) {
				                            columnToEdit = i;
				                            break;
				                        }
				                    }
				                    if (columnToEdit == -1) {
				                        return;
				                    }
				                }
				                CellEditor cellEditor = fColumnEditor.getCellEditor(getVisibleColumns()[columnToEdit], element, fTree);
				                if (cellEditor == null) {
				                	return;
				                }
				                disposeCellEditors();
				                CellEditor[] newEditors = new CellEditor[columns];
				                newEditors[columnToEdit] = cellEditor;
				                setCellEditors(newEditors);
				                setCellModifier(fColumnEditor.getCellModifier());
				                setColumnProperties(getVisibleColumns());
		            		}
		            	}
	            	}
            	}
                
                fTreeEditorImpl.handleMouseDown(e);
            }
        }); 		
        tree.addTreeListener(new TreeListener() {
            public void treeExpanded(TreeEvent e) {
                ((TreeItem) e.item).setExpanded(true);
                ModelNode node = findNode(e.item);
                if (node != null) {
					internalRefresh(node);
                }
            }

            public void treeCollapsed(TreeEvent e) {
            }
        });

        tree.addMouseListener(new MouseListener() {
            public void mouseUp(MouseEvent e) {
            }

            public void mouseDown(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
                TreeItem item = ((Tree) e.widget).getItem(new Point(e.x, e.y));
                if (item != null) {
                    if (item.getExpanded()) {
                        item.setExpanded(false);
                    } else {
                        item.setExpanded(true);
                        ModelNode node = findNode(item);
                        if (node != null) {
                        	internalRefresh(node);
                        }
                    }
                }
            }
        });		
	}

    /**
     * Sets the column editor for the given element
     * 
     * @param element
     */
    protected void updateColumnEditor(Object element) {
    	IColumnEditorFactoryAdapter factoryAdapter = getColumnEditorFactoryAdapter(element);
    	if (factoryAdapter != null) {    		
    		if (fColumnEditor != null) {
    			if (fColumnEditor.getId().equals(factoryAdapter.getColumnEditorId(getPresentationContext(), element))) {
    				// no change
    				return;
    			} else {
    				// dispose current
    				fColumnEditor.dispose();
    			}
    		}
   			// create new one
			fColumnEditor = factoryAdapter.createColumnEditor(getPresentationContext(), element);
			if (fColumnEditor != null) {
				fColumnEditor.init(getPresentationContext());
			}
    	} else {
    		// no editor - dispose current
	    	if (fColumnEditor != null) {
	    		fColumnEditor.dispose();
	    		fColumnEditor = null;
	    	}
    	}
    }
    
	/**
     * Returns the tree control for this viewer.
     * 
     * @return the tree control for this viewer
     */
    public Tree getTree() {
        return fTree;
    }

    /**
     * Updates whether the given node has children.
     * 
     * @param node node to update
     */
    protected void updateHasChildren(ModelNode node) {
        ((AsynchronousTreeModel)getModel()).updateHasChildren(node);
    }

    /**
     * Expands all elements in the given tree selection.
     * 
     * @param selection
     */
    public synchronized void expand(ISelection selection) {
        if (selection instanceof TreeSelection) {
            TreePath[] paths = ((TreeSelection) selection).getPaths();
            for (int i = 0; i < paths.length; i++) {
                fPendingExpansion.add(paths[i]);
            }
            if (getControl().getDisplay().getThread() == Thread.currentThread()) {
                attemptExpansion();
            } else {
                WorkbenchJob job = new WorkbenchJob("attemptExpansion") { //$NON-NLS-1$
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        attemptExpansion();
                        return Status.OK_STATUS;
                    }

                };
                job.setSystem(true);
                job.schedule();
            }
        }
    }

    /**
     * Attempts to expand all pending expansions.
     */
    synchronized void attemptExpansion() {
        if (fPendingExpansion != null) {
            for (Iterator i = fPendingExpansion.iterator(); i.hasNext();) {
                TreePath path = (TreePath) i.next();
                if (attemptExpansion(path)) {
                    i.remove();
                }
            }
        }
    }

    /**
     * Attempts to expand the given tree path and returns whether the expansion
     * was completed.
     * 
     * @param path path to expand
     * @return whether the expansion was completed
     */
    synchronized boolean attemptExpansion(TreePath path) {
        int segmentCount = path.getSegmentCount();
        for (int j = segmentCount - 1; j >= 0; j--) {
            Object element = path.getSegment(j);
            ModelNode[] nodes = getModel().getNodes(element);
            if (nodes != null) {
                for (int k = 0; k < nodes.length; k++) {
                    ModelNode node = nodes[k];
                    TreePath treePath = node.getTreePath();
                    if (path.startsWith(treePath, null)) {
                        if (!node.isDisposed()) {
                            Widget widget = findItem(node);
                            if (widget == null) {
                                // force the widgets to be mapped
                                ModelNode parent = node.getParentNode();
                                ModelNode child = node;
                                widget = findItem(parent);
                                while (widget == null && parent != null) {
                                    child = parent;
                                    parent = parent.getParentNode();
                                    if (parent != null) {
                                        widget = findItem(parent);
                                        treePath = parent.getTreePath();
                                    }
                                }
                                int childIndex = parent.getChildIndex(child);
                                if (childIndex < 0) {
                                    return false;
                                }
                                TreeItem[] items = getItems(widget);
                                if (childIndex < items.length) {
                                    widget = items[childIndex];
                                    mapElement(child, widget);
                                    widget.setData(child.getElement());
                                    treePath = child.getTreePath();
                                    node = child;
                                } else {
                                    return false;
                                }
                            }
                            if (widget instanceof TreeItem && !widget.isDisposed()) {
                                TreeItem treeItem = (TreeItem) widget;
                                if (treeItem.getExpanded()) {
                                    return path.getSegmentCount() == treePath.getSegmentCount();
                                }
                                if (treeItem.getItemCount() > 0) {
                                    updateChildren(node);
                                    expand(treeItem);
                                    if (path.getSegmentCount() == treePath.getSegmentCount()) {
                                        return true;
                                    }
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.Viewer#getControl()
     */
    public Control getControl() {
        return fTree;
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.AsynchronousViewer#dispose()
     */
    public synchronized void dispose() {
		if (fColumnPresentation != null) {
			fColumnPresentation.dispose();
		}
		disposeCellEditors();
		if (fColumnEditor != null) {
			fColumnEditor.dispose();
		}
		super.dispose();
	}

    /**
     * Disposes cell editors
     */
	protected void disposeCellEditors() {
		CellEditor[] cellEditors = getCellEditors();
		if (cellEditors != null) {
			for (int i = 0; i < cellEditors.length; i++) {
				CellEditor editor = cellEditors[i];
				if (editor != null) {
					editor.dispose();
				}
			}
		}
		setCellEditors(null);
	}

	/*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object,
     *      java.lang.Object)
     */
    synchronized protected void inputChanged(Object input, Object oldInput) {
        fPendingExpansion.clear();
        super.inputChanged(input, oldInput);
        resetColumns(input);
    }
    
    /**
     * Refreshes the columns in the view, based on the viewer input.
     */
    public void refreshColumns() {
    	configureColumns();
    	refresh();
    }
    
    /**
     * Configures the columns for the given viewer input.
     * 
     * @param input
     */
    protected void resetColumns(Object input) {
    	if (input != null) {
    		// only change columns if the input is non-null (persist when empty)
	    	IColumnPresentationFactoryAdapter factory = getColumnPresenetationFactoryAdapter(input);
	    	PresentationContext context = (PresentationContext) getPresentationContext();
	    	String type = null;
	    	if (factory != null) {
	    		type = factory.getColumnPresentationId(context, input);
	    	}
			if (type != null) {
				if (fColumnPresentation != null) {
					if (!fColumnPresentation.getId().equals(type)) {
						// dispose old, create new
						fColumnPresentation.dispose();
						fColumnPresentation = null;
					}
				}
				if (fColumnPresentation == null) {
					fColumnPresentation = factory.createColumnPresentation(context, input);
					if (fColumnPresentation != null) {
						fColumnPresentation.init(context);
						configureColumns();
					}
				}
			} else {
				if (fColumnPresentation != null) {
					fColumnPresentation.dispose();
					fColumnPresentation = null;
					configureColumns();
				}
			}
    	}
    }    
    
    /**
     * Configures the columns based on the current settings.
     * 
     * @param input
     */
    protected void configureColumns() {
    	if (fColumnPresentation != null) {
			IColumnPresentation build = null;
			if (isShowColumns(fColumnPresentation.getId())) {
				build = fColumnPresentation;
			}
			buildColumns(build);					
		} else {
			// get rid of columns
			buildColumns(null);
		}
    }
    
    /**
     * Creates new columns for the given presentation.
     * 
     * TODO: does this need to be async?
     * 
     * @param presentation
     */
    protected void buildColumns(IColumnPresentation presentation) {
    	// dispose current columns, persisting their weights
    	Tree tree = getTree();
		final TreeColumn[] columns = tree.getColumns();
		String[] visibleColumnIds = getVisibleColumns();
    	for (int i = 0; i < columns.length; i++) {
    		TreeColumn treeColumn = columns[i];
    		treeColumn.removeControlListener(fListener);
			treeColumn.dispose();
		}
    	PresentationContext presentationContext = (PresentationContext) getPresentationContext();
    	if (presentation != null) {	    	
	    	for (int i = 0; i < visibleColumnIds.length; i++) {
				String id = visibleColumnIds[i];
				String header = presentation.getHeader(id);
				// TODO: allow client to specify style
				TreeColumn column = new TreeColumn(tree, SWT.LEFT, i);
				column.setMoveable(true);
				column.setText(header);
				column.setWidth(1);
				ImageDescriptor image = presentation.getImageDescriptor(id);
				if (image != null) {
					column.setImage(getImage(image));
				}
				column.setData(id);
			}
	    	int[] order = (int[]) fColumnOrder.get(presentation.getId());
	    	if (order != null) {
	    		tree.setColumnOrder(order);
	    	}
	    	tree.setHeaderVisible(true);
	    	tree.setLinesVisible(true);
	    	presentationContext.setColumns(getVisibleColumns());
    	} else {
    		tree.setHeaderVisible(false);
    		tree.setLinesVisible(false);
    		presentationContext.setColumns(null);
    	}

    	int avg = tree.getSize().x;
    	if (visibleColumnIds != null)
    		avg /= visibleColumnIds.length;
    	
        if (avg == 0) {
            tree.addPaintListener(new PaintListener() {
                public void paintControl(PaintEvent e) {
                    Tree tree2 = getTree();
                    String[] visibleColumns = getVisibleColumns();
                    if (visibleColumns != null) {
						int avg1 = tree2.getSize().x / visibleColumns.length;
	                    initColumns(avg1);
                    }
                    tree2.removePaintListener(this);
                }
            });
        } else {
            initColumns(avg);
        }
    }

    private void initColumns(int widthHint) {
        TreeColumn[] columns = getTree().getColumns();
        for (int i = 0; i < columns.length; i++) {
            TreeColumn treeColumn = columns[i];
            Integer width = (Integer) fColumnSizes.get(treeColumn.getData());
            if (width == null) {
                treeColumn.setWidth(widthHint);
            } else {
                treeColumn.setWidth(width.intValue());
            }
            treeColumn.addControlListener(fListener);
        }
    }
    /**
     * Persists column sizes in cache
     */
    protected void persistColumnSizes() { 
		Tree tree = getTree();
		TreeColumn[] columns = tree.getColumns();
		for (int i = 0; i < columns.length; i++) {
			TreeColumn treeColumn = columns[i];
			Object id = treeColumn.getData();
			fColumnSizes.put(id, new Integer(treeColumn.getWidth()));
		}
    }
    
    /**
     * Persists column ordering
     */
    protected void persistColumnOrder() {
    	IColumnPresentation presentation = getColumnPresentation();
    	if (presentation != null) {
	    	Tree tree = getTree();
	    	int[] order = tree.getColumnOrder();
	    	if (order.length > 0) {
	    		for (int i = 0; i < order.length; i++) {
					if (i != order[i]) {
						// non default order
						fColumnOrder.put(presentation.getId(), order);
						return;
					}
				}
	    	}
	    	// default order
	    	fColumnOrder.remove(presentation.getId());
    	}
    }
    
    /**
     * Returns the column presentation factory for the given element or <code>null</code>.
     * 
     * @param input
     * @return column presentation factory of <code>null</code>
     */
    protected IColumnPresentationFactoryAdapter getColumnPresenetationFactoryAdapter(Object input) {
    	if (input instanceof IColumnPresentationFactoryAdapter) {
			return (IColumnPresentationFactoryAdapter) input;
		}
    	if (input instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) input;
			return (IColumnPresentationFactoryAdapter) adaptable.getAdapter(IColumnPresentationFactoryAdapter.class);
		}
    	return null;
    }
    
    /**
     * Returns the column editor factory for the given element or <code>null</code>.
     * 
     * @param input
     * @return column editor factory of <code>null</code>
     */
    protected IColumnEditorFactoryAdapter getColumnEditorFactoryAdapter(Object input) {
    	if (input instanceof IColumnEditorFactoryAdapter) {
			return (IColumnEditorFactoryAdapter) input;
		}
    	if (input instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) input;
			return (IColumnEditorFactoryAdapter) adaptable.getAdapter(IColumnEditorFactoryAdapter.class);
		}
    	return null;
    }    

    /**
     * Constructs and returns a tree path for the given item
     * or <code>null</code>. Must be called
     * from the UI thread.
     * 
     * @param item
     *            item to construct a path for
     * @return tree path for the item or <code>null</code> if none
     */
    protected synchronized TreePath getTreePath(TreeItem item) {
        TreeItem parent = item;
        List path = new ArrayList();
        while (parent != null && !parent.isDisposed()) {
            Object parentElement = parent.getData();
            if (parentElement == null) {
            	// this is a fix for bug 139859:
            	// on Mac, an item gets a 'selection' event before 'set data' when 
            	// scrolling with arrow keys. so this forces the item to get a
            	// 'set data' callback
                parent.getItemCount(); 
                parentElement = parent.getData();
                if (parentElement == null)
                    return null;
            }
			path.add(0, parentElement);
            parent = parent.getParentItem();
        }
        Object data = fTree.getData();
        if (data == null) {
        	return null;
        }
		path.add(0, data);
        return new TreePath(path.toArray());
    }
    
    /**
     * Returns the tree paths to the given element in this viewer, possibly
     * empty.
     * 
     * @param element model element
     * @return the paths to the element in this viewer
     */
    public TreePath[] getTreePaths(Object element) {
        ModelNode[] nodes = getModel().getNodes(element);
        if (nodes == null) {
            return new TreePath[]{};
        }
        TreePath[] paths = new TreePath[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            paths[i] = nodes[i].getTreePath();
        }
        return paths;
    }
    
    protected int getItemCount(Widget widget) {
    	if (widget instanceof TreeItem) {
			return ((TreeItem) widget).getItemCount();
		}
    	return ((Tree) widget).getItemCount();
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.AsynchronousViewer#setItemCount(org.eclipse.swt.widgets.Widget, int)
     */
    protected void setItemCount(Widget widget, int itemCount) {
        if (widget == fTree) {
            fTree.setItemCount(itemCount);
        } else {
            ((TreeItem) widget).setItemCount(itemCount);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.AsynchronousViewer#getChildWidget(org.eclipse.swt.widgets.Widget, int)
     */
    protected Widget getChildWidget(Widget parent, int index) {
		if (parent instanceof Tree) {
			Tree tree = (Tree) parent;
			if (index < tree.getItemCount()) {
				return tree.getItem(index);
			}
		} else if (parent instanceof TreeItem){
			TreeItem item = (TreeItem) parent;
			if (index < item.getItemCount()) {
				return item.getItem(index);
			}
		}
		return null;
	}

	private TreeItem[] getItems(Widget widget) {
        if (widget instanceof TreeItem) {
            return ((TreeItem) widget).getItems();
        } else {
            return fTree.getItems();
        }
    }

    /**
     * Returns the parent widget for the given widget or <code>null</code>
     * 
     * @param widget
     * @return parent widget or <code>null</code>
     */
    protected Widget getParentWidget(Widget widget) {
    	if (widget instanceof TreeItem) {
    		TreeItem parentItem = ((TreeItem)widget).getParentItem();
    		if (parentItem == null) {
    			return getControl();
    		}
    		return parentItem;
    	}
    	return null;
    }
    
    
    /**
     * Expands the given tree item and all of its parents. Does *not* update
     * elements or retrieve children.
     * 
     * @param child
     *            item to expand
     */
    private void expand(TreeItem child) {
        if (!child.getExpanded()) {
            child.setExpanded(true);

            TreeItem parent = child.getParentItem();
            if (parent != null) {
                expand(parent);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.AsynchronousViewer#clear(org.eclipse.swt.widgets.Widget)
     */
    protected void clear(Widget widget) {
        if (DEBUG_VIEWER) {
            DebugUIPlugin.debug("CLEAR [" + widget + "]");  //$NON-NLS-1$//$NON-NLS-2$
        }

    	if (widget instanceof TreeItem && !widget.isDisposed()) {
    		TreeItem item = (TreeItem) widget;
    		TreeItem parentItem = item.getParentItem();
    		if (parentItem == null) {
    			int index = fTree.indexOf(item);
                if (index >= 0)
                    fTree.clear(index, true);
    		} else {
    			int index = parentItem.indexOf(item);
                if (index >= 0)
                    parentItem.clear(index, true);
    		}
    		item.clearAll(true);
    	} else {
    		fTree.clearAll(true);
    	}
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.AsynchronousViewer#clearChildren(org.eclipse.swt.widgets.Widget)
     */
    protected void clearChildren(Widget widget) {
        if (DEBUG_VIEWER) {
            DebugUIPlugin.debug("CLEAR_CHILDREN [" + widget + "]");  //$NON-NLS-1$//$NON-NLS-2$
        }

    	if (widget instanceof TreeItem && !widget.isDisposed()) {
			TreeItem item = (TreeItem) widget;
			int itemCount = item.getItemCount();
			for (int i = 0; i < itemCount; i++) {
				item.clear(i, false);
			}
		} else {
			int itemCount = fTree.getItemCount();
			for (int i = 0; i < itemCount; i++) {
				fTree.clear(i, false);
			}
		}
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.AsynchronousViewer#clearChild(org.eclipse.swt.widgets.Widget, int)
     */
    protected void clearChild(Widget parent, int childIndex) {
        if (DEBUG_VIEWER) {
            DebugUIPlugin.debug("CLEAR_CHILD [" + parent + "]: " + childIndex);  //$NON-NLS-1$//$NON-NLS-2$
        }
  
       	if (parent instanceof TreeItem && !parent.isDisposed()) {
    		TreeItem item = (TreeItem) parent;
    		item.clear(childIndex, false);
    	} else {
    		fTree.clear(childIndex, false);
    	}
	}

	/*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.viewers.AsynchronousModelViewer#newSelectionFromWidget()
     */
    protected ISelection newSelectionFromWidget() {
        Control control = getControl();
        if (control == null || control.isDisposed()) {
            return StructuredSelection.EMPTY;
        }
        List list = getSelectionFromWidget();
        return new TreeSelection((TreePath[]) list.toArray(new TreePath[list.size()]));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.StructuredViewer#getSelectionFromWidget()
     */
    protected synchronized List getSelectionFromWidget() {
        TreeItem[] selection = fTree.getSelection();
        List paths = new ArrayList(selection.length);
        for (int i = 0; i < selection.length; i++) {
            TreePath treePath = getTreePath(selection[i]);
            if (treePath != null) {
            	paths.add(treePath);
            }
        }
        return paths;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.AsynchronousViewer#internalRefresh(org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelNode)
     */
    protected void internalRefresh(ModelNode node) {
        super.internalRefresh(node);
        updateHasChildren(node);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.StructuredViewer#reveal(java.lang.Object)
     */
    public void reveal(Object element) {
    	// TODO: in virtual case, we should attempt expansion
        ModelNode[] nodes = getModel().getNodes(element);
        if (nodes != null) {
        	for (int i = 0; i < nodes.length; i++) {
				ModelNode node = nodes[i];
				Widget widget = findItem(node);
				if (widget instanceof TreeItem) {
		            // TODO: only reveals the first occurrence - should we reveal all?
		            TreeItem item = (TreeItem) widget;
		            Tree tree = (Tree) getControl();
		            tree.showItem(item);			
		            return;
				}
			}
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.viewers.AsynchronousViewer#doAttemptSelectionToWidget(org.eclipse.jface.viewers.ISelection,
     *      boolean)
     */
    protected synchronized ISelection doAttemptSelectionToWidget(ISelection selection, boolean reveal) {
        List remaining = new ArrayList();
        if (!selection.isEmpty()) {
            List toSelect = new ArrayList();
            List theNodes = new ArrayList();
            List theElements = new ArrayList();
            TreeSelection treeSelection = (TreeSelection) selection;
            TreePath[] paths = treeSelection.getPaths();
            for (int i = 0; i < paths.length; i++) {
                TreePath path = paths[i];
                if (path == null) {
                    continue;
                }
                ModelNode[] nodes = getModel().getNodes(path.getLastSegment());
                boolean selected = false;
                if (nodes != null) {
                    for (int j = 0; j < nodes.length; j++) {
                        ModelNode node = nodes[j];
                        if (node.correspondsTo(path)) {
                            Widget widget = findItem(node);
                            if (widget != null && !widget.isDisposed()) {
                                toSelect.add(widget);
                                theNodes.add(node);
                                theElements.add(path.getLastSegment());
                                selected = true;
                                break;
                            }
                        }
                        // attempt to map widget
                        ModelNode parent = node.getParentNode();
                        ModelNode child = node;
                        if (parent != null) {
                        	Widget widget = findItem(parent);
		                    if (widget != null && !widget.isDisposed()) {
		                        int childIndex = parent.getChildIndex(child);
		                        if (childIndex < 0) {
		                            break;
		                        }
		                        TreeItem[] items = getItems(widget);
		                        if (childIndex < items.length) {
		                            widget = items[childIndex];
		                            mapElement(child, widget);
		                            widget.setData(child.getElement());
		                            toSelect.add(widget);
		                            theNodes.add(child);
		                            theElements.add(child.getElement());
		                            selected = true;
		                        } else {
		                            break;
		                        }
		                    }
                        }
                    }
                }
                if (!selected) {
                    remaining.add(path);
                }
            }
            if (!toSelect.isEmpty()) {
                final TreeItem[] items = (TreeItem[]) toSelect.toArray(new TreeItem[toSelect.size()]);
                // TODO: hack to ensure selection contains 'selected' element
                // instead of 'equivalent' element. Handles synch problems
                // between set selection & refresh
                for (int i = 0; i < items.length; i++) {
                    TreeItem item = items[i];
                    Object element = theElements.get(i); 
                    if (!item.isDisposed() && item.getData() != element) {
                        ModelNode theNode = (ModelNode) theNodes.get(i);
						Widget mapped = findItem(theNode);
                        if (mapped == null) {
                        	// the node has been unmapped from the widget (pushed down perhaps)
                        	return selection;
                        }
                        theNode.remap(element);
                        item.setData(element);
                    }
                }
                fTree.setSelection(items);
                if (reveal) {
                    fTree.showItem(items[0]);
                }
            }
        } else {
            fTree.setSelection(new TreeItem[0]);
        }
        return new TreeSelection((TreePath[]) remaining.toArray(new TreePath[remaining.size()]));
    }

    /**
     * Collapses all items in the tree.
     */
    public void collapseAll() {
        TreeItem[] items = fTree.getItems();
        for (int i = 0; i < items.length; i++) {
            TreeItem item = items[i];
            if (item.getExpanded())
                collapse(item);
        }
    }

    /**
     * Collapses the given item and all of its children items.
     * 
     * @param item
     *            item to collapse recursively
     */
    protected void collapse(TreeItem item) {
        TreeItem[] items = item.getItems();
        for (int i = 0; i < items.length; i++) {
            TreeItem child = items[i];
            if (child.getExpanded()) {
                collapse(child);
            }
        }
        item.setExpanded(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.viewers.AsynchronousViewer#setColor(org.eclipse.swt.widgets.Widget,
     *      org.eclipse.swt.graphics.RGB, org.eclipse.swt.graphics.RGB)
     */
    protected void setColors(Widget widget, RGB[] foregrounds, RGB[] backgrounds) {
        if (widget instanceof TreeItem) {
            TreeItem item = (TreeItem) widget;
            Color[] fgs = getColors(foregrounds);
            for (int i = 0; i < fgs.length; i++) {
				item.setForeground(i, fgs[i]);
			}
            Color[] bgs = getColors(backgrounds);
            for (int i = 0; i < bgs.length; i++) {
                item.setBackground(i, bgs[i]);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.viewers.AsynchronousViewer#setFont(org.eclipse.swt.widgets.Widget,
     *      org.eclipse.swt.graphics.FontData)
     */
    protected void setFonts(Widget widget, FontData[] fontData) {
        if (widget instanceof TreeItem) {
            TreeItem item = (TreeItem) widget;
            Font[] fonts = getFonts(fontData);
            for (int i = 0; i < fonts.length; i++) {
                item.setFont(i, fonts[i]);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.viewers.AsynchronousViewer#acceptsSelection(org.eclipse.jface.viewers.ISelection)
     */
    protected boolean acceptsSelection(ISelection selection) {
        return selection instanceof TreeSelection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.viewers.AsynchronousViewer#getEmptySelection()
     */
    protected ISelection getEmptySelection() {
        return new TreeSelection(new TreePath[0]);
    }

    /**
     * Adds the item specified by the given tree path to this tree. Can be
     * called in a non-UI thread.
     * 
     * @param treePath
     */
    public void add(TreePath treePath) {
        ((AsynchronousTreeModel)getModel()).add(treePath);
    }

    /**
     * Removes the item specified in the given tree path from this tree. Can be
     * called in a non-UI thread.
     * 
     * @param treePath
     */
    public void remove(TreePath treePath) {
        synchronized (this) {
            for (Iterator i = fPendingExpansion.iterator(); i.hasNext();) {
                TreePath expansionPath = (TreePath) i.next();
                if (expansionPath.startsWith(treePath, null)) {
                    i.remove();
                }
            }
        }
        ((AsynchronousTreeModel)getModel()).remove(treePath);
    }

    
    protected void restoreLabels(Item item) {
    	TreeItem treeItem = (TreeItem) item;
    	String[] values = (String[]) treeItem.getData(OLD_LABEL);
    	Image[] images = (Image[])treeItem.getData(OLD_IMAGE);
		if (values != null) {
			treeItem.setText(values);
			treeItem.setImage(images);
		}
	}

	protected void setLabels(Widget widget, String[] text, ImageDescriptor[] image) {
        if (widget instanceof TreeItem) {
            TreeItem item = (TreeItem) widget;
            if (!item.isDisposed()) {
            	if (text != null) {
	                item.setText(text);
	                item.setData(OLD_LABEL, text);
            	}
                Image[] images = getImages(image);
				item.setImage(images);
                item.setData(OLD_IMAGE, images);
            }
        }
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousModelViewer#createModel()
	 */
	protected AsynchronousModel createModel() {
		return new AsynchronousTreeModel(this);
	}
    
    /**
	 * Attempt pending updates. Subclasses may override but should call super.
	 */
    protected void attemptPendingUpdates() {
    	attemptExpansion();
    	super.attemptPendingUpdates();
    }    

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.AsynchronousViewer#createUpdatePolicy()
	 */
	public AbstractUpdatePolicy createUpdatePolicy() {
		return new TreeUpdatePolicy();
	}

	protected synchronized void unmapAllElements() {
		super.unmapAllElements();
		Tree tree = getTree();
		if (!tree.isDisposed()) {
			TreeItem[] items = tree.getItems();
			for (int i = 0; i < items.length; i++) {
				items[i].dispose();
			}
			clear(tree);
		}
	}
	
	/**
	 * Returns the current column presentation for this viewer, or <code>null</code>
	 * if none.
	 * 
	 * @return column presentation or <code>null</code>
	 */
	public IColumnPresentation getColumnPresentation() {
		return fColumnPresentation;
	}
	
	/**
	 * Returns identifiers of the visible columns in this viewer, or <code>null</code>
	 * if there is currently no column presentation.
	 *  
	 * @return visible columns or <code>null</code>
	 */
	public String[] getVisibleColumns() {
		IColumnPresentation presentation = getColumnPresentation();
		if (presentation != null) {
			String[] columns = (String[]) fVisibleColumns.get(presentation.getId());
			if (columns == null) {
				return presentation.getInitialColumns();
			}
			return columns;
		}
		return null;
	}
	
	/**
	 * Sets the id's of visible columns, or <code>null</code> to set default columns.
	 * Only effects the current column presentation.
	 * 
	 * @param ids visible columns
	 */
	public void setVisibleColumns(String[] ids) {
		IColumnPresentation presentation = getColumnPresentation();
		if (presentation != null) {
			fColumnOrder.remove(presentation.getId());
			fVisibleColumns.remove(presentation.getId());
			if (ids != null) {
				// put back in table if not default
				String[] columns = presentation.getInitialColumns();
				if (columns.length == ids.length) {
					for (int i = 0; i < columns.length; i++) {
						if (!ids[i].equals(columns[i])) {
							fVisibleColumns.put(presentation.getId(), ids);
							break;
						}
					}
				} else {
					fVisibleColumns.put(presentation.getId(), ids);
				}
			}
			PresentationContext presentationContext = (PresentationContext) getPresentationContext();
			presentationContext.setColumns(getVisibleColumns());
			refreshColumns();
		}
	}
	
	/**
	 * Save viewer state into the given memento.
	 * 
	 * @param memento
	 */
	public void saveState(IMemento memento) {
		if (!fColumnSizes.isEmpty()) {
			Iterator iterator = fColumnSizes.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Entry) iterator.next();
				IMemento sizes = memento.createChild(COLUMN_SIZES, (String)entry.getKey());
				sizes.putInteger(SIZE, ((Integer)entry.getValue()).intValue());
			}
		}
		if (!fShowColumns.isEmpty()) {
			Iterator iterator = fShowColumns.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Entry) iterator.next();
				IMemento sizes = memento.createChild(SHOW_COLUMNS, (String)entry.getKey());
				sizes.putString(SHOW_COLUMNS, ((Boolean)entry.getValue()).toString());
			}			
		}
		if (!fVisibleColumns.isEmpty()) {
			Iterator iterator = fVisibleColumns.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Entry) iterator.next();
				String id = (String) entry.getKey();
				IMemento visible = memento.createChild(VISIBLE_COLUMNS, id);
				String[] columns = (String[]) entry.getValue();
				visible.putInteger(SIZE, columns.length);
				for (int i = 0; i < columns.length; i++) {
					visible.putString(COLUMN+Integer.toString(i), columns[i]);
				}
			}
		}
		if (!fColumnOrder.isEmpty()) {
			Iterator iterator = fColumnOrder.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Entry) iterator.next();
				String id = (String) entry.getKey();
				IMemento orderMemento = memento.createChild(COLUMN_ORDER, id);
				int[] order = (int[]) entry.getValue();
				orderMemento.putInteger(SIZE, order.length);
				for (int i = 0; i < order.length; i++) {
					orderMemento.putInteger(COLUMN+Integer.toString(i), order[i]);
				}
			}
		}
	}
	
	/**
	 * Resets any persisted column size for the given columns
	 */
	public void resetColumnSizes(String[] columnIds) {
		for (int i = 0; i < columnIds.length; i++) {
			fColumnSizes.remove(columnIds[i]);
		}
	}
	
	/**
	 * Initializes viewer state from the memento
	 * 
	 * @param memento
	 */
	public void initState(IMemento memento) {
		IMemento[] mementos = memento.getChildren(COLUMN_SIZES);
		for (int i = 0; i < mementos.length; i++) {
			IMemento child = mementos[i];
			String id = child.getID();
			Integer size = child.getInteger(SIZE);
			if (size != null) {
				fColumnSizes.put(id, size);
			}
		}
		mementos = memento.getChildren(SHOW_COLUMNS);
		for (int i = 0; i < mementos.length; i++) {
			IMemento child = mementos[i];
			String id = child.getID();
			Boolean bool = Boolean.valueOf(child.getString(SHOW_COLUMNS));
			if (!bool.booleanValue()) {
				fShowColumns.put(id, bool);
			}
		}
		mementos = memento.getChildren(VISIBLE_COLUMNS);
		for (int i = 0; i < mementos.length; i++) {
			IMemento child = mementos[i];
			String id = child.getID();
			Integer integer = child.getInteger(SIZE);
			if (integer != null) {
				int length = integer.intValue();
				String[] columns = new String[length];
				for (int j = 0; j < length; j++) {
					columns[j] = child.getString(COLUMN+Integer.toString(j));
				}
				fVisibleColumns.put(id, columns);
			}
		}
		mementos = memento.getChildren(COLUMN_ORDER);
		for (int i = 0; i < mementos.length; i++) {
			IMemento child = mementos[i];
			String id = child.getID();
			Integer integer = child.getInteger(SIZE);
			if (integer != null) {
				int length = integer.intValue();
				int[] order = new int[length];
				for (int j = 0; j < length; j++) {
					order[j] = child.getInteger(COLUMN+Integer.toString(j)).intValue();
				}
				fColumnOrder.put(id, order);
			}
		}
	}

	/**
	 * Initializes the tree viewer implementation.
	 */
	private void initTreeViewerImpl() {
		fTreeEditorImpl = new TreeEditorImpl(this) {
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
				fTreeEditor.setEditor(w, (TreeItem) item, columnNumber);
			}

			void setSelection(IStructuredSelection selection, boolean b) {
				AsynchronousTreeViewer.this.setSelection(selection, b);
			}

			void showSelection() {
				getTree().showSelection();
			}

			void setLayoutData(CellEditor.LayoutData layoutData) {
				fTreeEditor.grabHorizontal = layoutData.grabHorizontal;
				fTreeEditor.horizontalAlignment = layoutData.horizontalAlignment;
				fTreeEditor.minimumWidth = layoutData.minimumWidth;
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
	 * Starts editing the given element.
	 * 
	 * @param element
	 *            the element
	 * @param column
	 *            the column number
	 */
	public void editElement(Object element, int column) {
		fTreeEditorImpl.editElement(element, column);
	}

	/**
	 * Returns the cell editors of this tree viewer.
	 * 
	 * @return the list of cell editors
	 */
	public CellEditor[] getCellEditors() {
		return fTreeEditorImpl.getCellEditors();
	}

	/**
	 * Returns the cell modifier of this tree viewer.
	 * 
	 * @return the cell modifier
	 */
	public ICellModifier getCellModifier() {
		return fTreeEditorImpl.getCellModifier();
	}	
	
	/**
	 * Cancels a currently active cell editor. All changes already done in the
	 * cell editor are lost.
	 * 
	 */
	public void cancelEditing() {
		fTreeEditorImpl.cancelEditing();
	}	
	
	/**
	 * Returns whether there is an active cell editor.
	 * 
	 * @return <code>true</code> if there is an active cell editor, and
	 *         <code>false</code> otherwise
	 */
	public boolean isCellEditorActive() {
		return fTreeEditorImpl.isCellEditorActive();
	}	
	
	/**
	 * Sets the cell editors of this tree viewer.
	 * 
	 * @param editors
	 *            the list of cell editors
	 */
	protected void setCellEditors(CellEditor[] editors) {
		fTreeEditorImpl.setCellEditors(editors);
	}

	/**
	 * Sets the cell modifier of this tree viewer.
	 * 
	 * @param modifier
	 *            the cell modifier
	 */
	protected void setCellModifier(ICellModifier modifier) {
		fTreeEditorImpl.setCellModifier(modifier);
	}
	
	/**
	 * Sets the column properties of this tree viewer. The properties must
	 * correspond with the columns of the tree control. They are used to
	 * identify the column in a cell modifier.
	 * 
	 * @param columnProperties
	 *            the list of column properties
	 */
	protected void setColumnProperties(String[] columnProperties) {
		fTreeEditorImpl.setColumnProperties(columnProperties);
	}	
	
	/**
	 * Toggles columns on/off for the current column presentation, if any.
	 * 
	 * @param show whether to show columns if the current input supports
	 * 	columns
	 */
	public void setShowColumns(boolean show) {
		if (show) {
			if (!isShowColumns()) {
				fShowColumns.remove(fColumnPresentation.getId());
			}
		} else {
			if (isShowColumns()){
				fShowColumns.put(fColumnPresentation.getId(), Boolean.FALSE);
			}
		}
		refreshColumns();
	}
	
	/**
	 * Returns whether columns are being displayed currently.
	 * 
	 * @return
	 */
	public boolean isShowColumns() {
		if (fColumnPresentation != null) {
			return isShowColumns(fColumnPresentation.getId());
		}
		return false;
	}
	
	/**
	 * Returns whether columns can be toggled on/off for the current input.
	 * 
	 * @return whether columns can be toggled on/off for the current input
	 */
	public boolean canToggleColumns() {
		return fColumnPresentation != null && fColumnPresentation.isOptional();
	}
	
	protected boolean isShowColumns(String columnPresentationId) {
		Boolean bool = (Boolean) fShowColumns.get(columnPresentationId);
		if (bool == null) {
			return true;
		}
		return bool.booleanValue();
	}
	
	/**
	 * Notification the container status of a node has changed/been computed.
	 * 
	 * @param node
	 */
	protected void nodeContainerChanged(ModelNode node) {
		Widget widget = findItem(node);
		if (widget != null && !widget.isDisposed()) {
			int childCount = node.getChildCount();
			setItemCount(widget, childCount);
			if (node.isContainer()) {
				if (widget instanceof TreeItem) {
					if (((TreeItem)widget).getExpanded()) {
						updateChildren(node);
					}
				} else {
					updateChildren(node);
				}
				attemptPendingUpdates();
			}
		}			
	}
	
	/**
	 * Notification a node's children have changed.
	 * Updates the child count for the parent's widget
	 * and clears children to be updated.
	 * 
	 * @param parentNode
	 */
	protected void nodeChildrenChanged(ModelNode parentNode) {
		Widget widget = findItem(parentNode);
		if (widget != null && !widget.isDisposed()) {
			int childCount = parentNode.getChildCount();
			setItemCount(widget, childCount);
			TreeItem[] items = null;
			if (widget instanceof TreeItem) {
				TreeItem treeItem = (TreeItem) widget;
				if (treeItem.getExpanded()) {
					items = treeItem.getItems();
				}
			} else {
				items = ((Tree)widget).getItems();
			}
			if (items != null) {
				for (int i = 0; i < items.length; i++) {
					if (items[i].getExpanded()) {
						update(items[i], i);
					} else {
						clearChild(widget, i);
					}
				}
			}
			attemptPendingUpdates();
		}		
	}	
	
	/**
	 * Collects label results.
	 * 
	 * @param monitor progress monitor
	 * @param element element to start collecting at, including all children
	 * @param taskName label for progress monitor main task
	 * 
	 * @return results or <code>null</code> if cancelled
	 */
	public List buildLabels(IProgressMonitor monitor, Object element, String taskName) {
		ModelNode[] theNodes = getModel().getNodes(element);
		List results = new ArrayList();
		if (theNodes != null && theNodes.length > 0) {
			ModelNode root = theNodes[0];
			List nodes = new ArrayList(); 
			collectNodes(nodes, root);
			monitor.beginTask(taskName, nodes.size());
			Iterator iterator = nodes.iterator();
			while (!monitor.isCanceled() && iterator.hasNext()) {
				ModelNode node = (ModelNode) iterator.next();
				IAsynchronousLabelAdapter labelAdapter = getModel().getLabelAdapter(node.getElement());
				if (labelAdapter != null) {
					LabelResult result = new LabelResult(node, getModel());
					labelAdapter.retrieveLabel(node.getElement(), getPresentationContext(), result);
					synchronized (result) { 
						if (!result.isDone()) {
							try {
								result.wait();
							} catch (InterruptedException e) {
								monitor.setCanceled(true);
								return null;
							}
						}
					}
					IStatus status = result.getStatus();
					if (status == null || status.isOK()) {
						results.add(result);
					}
				}
				monitor.worked(1);
			}
		}
		monitor.done();
		return results;
	}
	
	private void collectNodes(List nodes, ModelNode node) {
		if (node.getParentNode() != null) {
			nodes.add(node);
		}
		ModelNode[] childrenNodes = node.getChildrenNodes();
		if (childrenNodes != null) {
			for (int i = 0; i < childrenNodes.length; i++) {
				collectNodes(nodes, childrenNodes[i]);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousViewer#indexOf(org.eclipse.swt.widgets.Widget, org.eclipse.swt.widgets.Widget)
	 */
	protected int indexOf(Widget parent, Widget child) {
		if (parent instanceof Tree) {
			return ((Tree)parent).indexOf((TreeItem)child);
		} else {
			return ((TreeItem)parent).indexOf((TreeItem)child);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousViewer#selectionExists(org.eclipse.jface.viewers.ISelection)
	 */
	protected boolean selectionExists(ISelection selection) {
		if (!selection.isEmpty() && selection instanceof TreeSelection) {
			TreeSelection ts = (TreeSelection) selection;
			TreePath[] paths = ts.getPaths();
			int matchingPaths = 0;
			for (int i = 0; i < paths.length; i++) {
				TreePath path = paths[i];
				Object element = path.getLastSegment();
				ModelNode[] nodes = getModel().getNodes(element);
				if (nodes != null) {
					for (int j = 0; j < nodes.length; j++) {
						ModelNode node = nodes[j];
						if (node.getTreePath().equals(path)) {
							matchingPaths++;
							break;
						}
					}
				}
			}
			return matchingPaths == paths.length;
		}
		return super.selectionExists(selection);
	}	
	
	
	

}
