/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILazyTreePathContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IMemento;

/**
 * A tree viewer that displays a model.
 * 
 * @since 3.3
 */
public class InternalTreeModelViewer extends TreeViewer {
	
	private IPresentationContext fContext;
	
    /**
     * Current column presentation or <code>null</code>
     */
    private IColumnPresentation fColumnPresentation = null;	
    
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
     * Item's tree path cache 
     */
    private static final String TREE_PATH_KEY = "TREE_PATH_KEY"; //$NON-NLS-1$
    
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
	 * True while performing an insert... we allow insert with filters
	 */
	private boolean fInserting = false;
	
	/**
	 * Whether to notify the content provider when an element is unmapped
	 */
	private boolean fNotifyUnmap = true;
	
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
	 * Proxy to cell modifier/editor support
	 */
	class CellModifierProxy implements ICellModifier {
		
		private ICellModifier fModifier;

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
		 */
		public boolean canModify(Object element, String property) {
			IElementEditor editor = getElementEditorAdapter(element);
			if (editor != null) {
				fModifier = editor.getCellModifier(getPresentationContext(), element);
				if (fModifier != null) {
					if (fModifier.canModify(element, property)) {
						// install cell editor
						CellEditor cellEditor = editor.getCellEditor(getPresentationContext(), property, element, (Composite)getControl());
		                if (cellEditor != null) {
			                disposeCellEditors();
			                CellEditor[] newEditors = new CellEditor[getVisibleColumns().length];
			                for (int i = 0; i < newEditors.length; i++) {
								newEditors[i] = cellEditor;
							}
			                setCellEditors(newEditors);
			                return true;
		                }
					}
				}
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
		 */
		public Object getValue(Object element, String property) {
			if (fModifier != null) {
				return fModifier.getValue(element, property);
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
		 */
		public void modify(Object element, String property, Object value) {
			if (fModifier != null) {
				if (element instanceof Item) {
					element = ((Item)element).getData();
				}
				fModifier.modify(element, property, value);
			}
		}
		
		/**
		 * Disposes client's column editor and cell editors
		 */
		protected void dispose() {
			fModifier = null;
			disposeCellEditors();
			setCellEditors(null);
		}

		/**
		 * Disposes current cell editors
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
		}
		
	    /**
	     * Returns the element editor for the given element or <code>null</code>.
	     * 
	     * @param input
	     * @return element editor or <code>null</code>
	     */
	    protected IElementEditor getElementEditorAdapter(Object input) {
	    	if (input instanceof IElementEditor) {
				return (IElementEditor) input;
			}
	    	if (input instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) input;
				return (IElementEditor) adaptable.getAdapter(IElementEditor.class);
			}
	    	return null;
	    } 
	}
	
	private CellModifierProxy fCellModifier;
	
	/**
	 * @param parent
	 * @param style
	 */
	public InternalTreeModelViewer(Composite parent, int style, IPresentationContext context) {
		super(parent, style);
		if ((style & SWT.VIRTUAL) == 0) {
			throw new IllegalArgumentException("style must include SWT.VIRTUAL"); //$NON-NLS-1$
		}
		setUseHashlookup(true);
		fCellModifier = new CellModifierProxy();
		fContext = context;
		setContentProvider(createContentProvider());
		setLabelProvider(createLabelProvider());
	}
	
	/**
	 * @return content provider for this tree viewer
	 */
	protected TreeModelContentProvider createContentProvider()
	{
		return new TreeModelContentProvider();
	}
	
	/**
	 * @return label provider for this tree viewer
	 */
	protected TreeModelLabelProvider createLabelProvider()
	{
		return new TreeModelLabelProvider(this);
	}
	
	/* (non-Javadoc)
	 * 
	 * Workaround for bug 159461: when an item is cleared it's label is cleared. To avoid
	 * flashing, restore its label to its previous value.
	 * 
	 * @see org.eclipse.jface.viewers.TreeViewer#hookControl(org.eclipse.swt.widgets.Control)
	 */
	protected void hookControl(Control control) {
		Tree treeControl = (Tree) control;
		treeControl.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				// to avoid flash, reset previous labels/image
				TreeItem item = (TreeItem) event.item;
				Object[] labels = (Object[]) item.getData(LabelUpdate.PREV_LABEL_KEY);
				if (labels != null) {
					for (int i = 0; i < labels.length; i++) {
						if (labels[i] != null) {
							item.setText(i, (String)labels[i]);
						}
					}
				}
				Object[] images = (Object[]) item.getData(LabelUpdate.PREV_IMAGE_KEY);
				if (images != null) {
					for (int i = 0; i < images.length; i++) {
						item.setImage(i, (Image) images[i]);
					}
				}
			}
		});
		super.hookControl(control);
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.StructuredViewer#handleInvalidSelection
     * 
     * Override the default handler for invalid selection to allow model 
     * selection policy to select the new selection.
     */
	protected void handleInvalidSelection(ISelection selection, ISelection newSelection) {
	    IModelSelectionPolicy selectionPolicy = getSelectionPolicy(selection);
	    if (selectionPolicy != null) {
	    	ISelection temp = newSelection;
            newSelection = selectionPolicy.replaceInvalidSelection(selection, newSelection);
            if (temp != newSelection) {
            	if (newSelection == null) {
            		newSelection = new StructuredSelection();
            	}
            	// call super.setSelection(...) to avoid asking the selection policy
            	// if the selection should be overridden
            	super.setSelection(newSelection, false);
            	return;
            }
	    }
	    super.handleInvalidSelection(selection, newSelection);
	}
        

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ContentViewer#handleDispose(org.eclipse.swt.events.DisposeEvent)
	 */
	protected void handleDispose(DisposeEvent event) {
		if (fColumnPresentation != null) {
			fColumnPresentation.dispose();
		}
		fCellModifier.dispose();
		fContext.dispose();
		super.handleDispose(event);
	}
	
	/**
	 * Returns this viewer's presentation context.
	 * 
	 * @return presentation context
	 */
	public IPresentationContext getPresentationContext() {
		return fContext;
	}
	
	protected void unmapElement(Object element, Widget widget) {
		if (fNotifyUnmap) {
			// TODO: should we update the filter with the "new non-identical element"?
			IContentProvider provider = getContentProvider();
			if (provider instanceof ModelContentProvider) {
				((ModelContentProvider) provider).unmapPath((TreePath) widget.getData(TREE_PATH_KEY));
			}
		}
		super.unmapElement(element, widget);
	}
	
	protected void associate(Object element, Item item) {
		// see AbstractTreeViewer.associate(...)
		Object data = item.getData();
		if (data != null && data != element && equals(data, element)) {
			// elements are equal but not identical
			// -> being removed from map, but should not change filters
			try {
				fNotifyUnmap = false;
				super.associate(element, item);
			} finally {
				fNotifyUnmap = true;
			}
		} else {
			super.associate(element, item);
		}
	}

	/* (non-Javadoc)
	 * 
	 * We need tree paths when disposed/unmapped in any order so cache the tree path.
	 * 
	 * @see org.eclipse.jface.viewers.TreeViewer#mapElement(java.lang.Object, org.eclipse.swt.widgets.Widget)
	 */
	protected void mapElement(Object element, Widget widget) {
		super.mapElement(element, widget);
		if (widget instanceof Item) {
			widget.setData(TREE_PATH_KEY, getTreePathFromItem((Item)widget));
		} else {
			widget.setData(TREE_PATH_KEY, ModelContentProvider.EMPTY_TREE_PATH);
		}
	}
	
	/* (non-Javadoc)
	 * 
	 * Override because we allow inserting with filters present.
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#insert(java.lang.Object, java.lang.Object, int)
	 */
	public void insert(Object parentElementOrTreePath, Object element, int position) {
		try {
			fInserting = true;
			super.insert(parentElementOrTreePath, element, position);
		} finally {
			fInserting = false;
		}
	}
	
	/* (non-Javadoc)
	 * 
	 * Override because we allow inserting with filters present.
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#hasFilters()
	 */
	protected boolean hasFilters() {
		if (fInserting) {
			return false;
		}
		return super.hasFilters();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#inputChanged(java.lang.Object, java.lang.Object)
	 */
	protected void inputChanged(Object input, Object oldInput) {
		super.inputChanged(input, oldInput);
		resetColumns(input);
	}

	/**
     * Configures the columns for the given viewer input.
     * 
     * @param input
     */
    protected void resetColumns(Object input) {
    	if (input != null) {
    		// only change columns if the input is non-null (persist when empty)
	    	IColumnPresentationFactory factory = getColumnPresenetationFactoryAdapter(input);
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
     * Returns the column presentation factory for the given element or <code>null</code>.
     * 
     * @param input
     * @return column presentation factory of <code>null</code>
     */
    protected IColumnPresentationFactory getColumnPresenetationFactoryAdapter(Object input) {
    	if (input instanceof IColumnPresentationFactory) {
			return (IColumnPresentationFactory) input;
		}
    	if (input instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) input;
			return (IColumnPresentationFactory) adaptable.getAdapter(IColumnPresentationFactory.class);
		}
    	return null;
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
	 * Resets any persisted column size for the given columns
	 */
	public void resetColumnSizes(String[] columnIds) {
		for (int i = 0; i < columnIds.length; i++) {
			fColumnSizes.remove(columnIds[i]);
		}
	}
	
	/**
	 * Sets the id's of visible columns, or <code>null</code> to set default columns.
	 * Only effects the current column presentation.
	 * 
	 * @param ids visible columns
	 */
	public void setVisibleColumns(String[] ids) {
		if (ids != null && ids.length == 0) {
			ids = null;
		}
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
     * Refreshes the columns in the view, based on the viewer input.
     */
    protected void refreshColumns() {
    	configureColumns();
    	refresh();
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
     * Creates new columns for the given presentation.
     * 
     * TODO: does this need to be asynchronous?
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
					column.setImage(((TreeModelLabelProvider)getLabelProvider()).getImage(image));
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
	    	setColumnProperties(getVisibleColumns());
	    	setCellModifier(fCellModifier);
    	} else {
    		tree.setHeaderVisible(false);
    		tree.setLinesVisible(false);
    		presentationContext.setColumns(null);
    		setCellModifier(null);
    		setColumnProperties(null);
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
		if (isShowColumns()) {
			IColumnPresentation presentation = getColumnPresentation();
			if (presentation != null) {
				String[] columns = (String[]) fVisibleColumns.get(presentation.getId());
				if (columns == null) {
					return presentation.getInitialColumns();
				}
				return columns;
			}
		}
		return null;
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
	 * Returns whether the candidate selection should override the current
	 * selection.
	 * 
	 * @param current
	 * @param curr
	 * @return
	 */
	protected boolean overrideSelection(ISelection current, ISelection candidate) {
		IModelSelectionPolicy selectionPolicy = getSelectionPolicy(current);
		if (selectionPolicy == null) {
			return true;
		}
		if (selectionPolicy.contains(candidate, getPresentationContext())) {
			return selectionPolicy.overrides(current, candidate, getPresentationContext());
		}
		return !selectionPolicy.isSticky(current, getPresentationContext());
	}
	
	/**
	 * Returns the selection policy associated with the given selection
	 * or <code>null</code> if none.
	 * 
	 * @param selection or <code>null</code>
	 * @return selection policy or <code>null</code>
	 */
	protected IModelSelectionPolicy getSelectionPolicy(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (element instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) element;
				IModelSelectionPolicyFactory factory =  (IModelSelectionPolicyFactory) adaptable.getAdapter(IModelSelectionPolicyFactory.class);
				if (factory != null) {
					return factory.createModelSelectionPolicyAdapter(adaptable, getPresentationContext());
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * 
	 * Consider selection policy
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#setSelection(org.eclipse.jface.viewers.ISelection, boolean)
	 */
	public void setSelection(ISelection selection, boolean reveal) {
		if (!overrideSelection(getSelection(), selection)) {
			return;
		}
		super.setSelection(selection, reveal);
	}
	
	/**
	 * Sets the selection in the viewer to the specified selection.
	 * 
	 * @param selection the selection
	 * @param reveal whether to reveal the selection
	 * @param force whether to force the selection (i.e. <code>true</code> to
	 *  override the model selection policy)
	 */
	public void setSelection(ISelection selection, boolean reveal, boolean force) {
		if (force) {
			super.setSelection(selection, reveal);
		} else {
			setSelection(selection, reveal);
		}
	}
	
	/**
	 * Registers the specified listener for view update notifications.
	 * 
	 * @param listener listener
	 */
	public void addViewerUpdateListener(IViewerUpdateListener listener) {
		((ModelContentProvider)getContentProvider()).addViewerUpdateListener(listener);
	}
	
	/**
	 * Removes the specified listener from update notifications.
	 * 
	 * @param listener listener
	 */
	public void removeViewerUpdateListener(IViewerUpdateListener listener) {
		ModelContentProvider cp = (ModelContentProvider)getContentProvider();
		if (cp !=  null) {
			cp.removeViewerUpdateListener(listener);
		}
	}
	
	/**
	 * Registers the given listener for model delta notification.
	 * 
	 * @param listener model delta listener
	 */
	public void addModelChangedListener(IModelChangedListener listener) {
		((ModelContentProvider)getContentProvider()).addModelChangedListener(listener); 
	}
	
	/**
	 * Unregisters the given listener from model delta notification.
	 * 
	 * @param listener model delta listener
	 */
	public void removeModelChangedListener(IModelChangedListener listener) {
		ModelContentProvider cp = (ModelContentProvider)getContentProvider();
		if (cp !=  null) {
			cp.removeModelChangedListener(listener);
		}
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
		
		((TreeModelLabelProvider)getLabelProvider()).update(getTreePathFromItem(item), getViewerRowFromItem(treeItem));

		// As it is possible for user code to run the event
		// loop check here.
		if (item.isDisposed()) {
			unmapElement(element, item);
		}
	}
	
	/**
	 * Forces unmapped virtual items to populate 
	 */
	boolean populateVitrualItems() {
		Tree tree = getTree();
		return populateVitrualItems(TreePath.EMPTY, tree.getItems());
	}

	/**
	 * @param items
	 */
	private boolean populateVitrualItems(TreePath parentPath, TreeItem[] items) {
		boolean queued = false;
		for (int i = 0; i < items.length; i++) {
			TreeItem treeItem = items[i];
			if (treeItem.getData() == null) {
				queued = true;
				((ILazyTreePathContentProvider)getContentProvider()).updateElement(parentPath, i);
			}
			if (treeItem.getExpanded()) {
				queued = populateVitrualItems(parentPath.createChildPath(treeItem.getData()), treeItem.getItems()) | queued;
			}
		}
		return queued;
	}
	
	void addLabelUpdateListener(ILabelUpdateListener listener) {
		((TreeModelLabelProvider)getLabelProvider()).addLabelUpdateListener(listener);
	}
	
	void removeLabelUpdateListener(ILabelUpdateListener listener) {
		((TreeModelLabelProvider)getLabelProvider()).removeLabelUpdateListener(listener);
	}

}
