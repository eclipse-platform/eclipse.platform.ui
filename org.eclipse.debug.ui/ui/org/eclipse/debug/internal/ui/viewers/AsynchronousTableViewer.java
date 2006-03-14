package org.eclipse.debug.internal.ui.viewers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnEditorFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentationFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * @since 3.2
 */
public class AsynchronousTableViewer extends AsynchronousViewer implements Listener {
    /**
     * Memento type for column sizes. Sizes are keyed by colunm presentation id 
     */
    private static final String COLUMN_SIZES = "COLUMN_SIZES"; //$NON-NLS-1$
    /**
     * Memento type for the visible columns for a presentation context.
     * A memento is created for each colunm presentation keyed by column number
     */
    private static final String VISIBLE_COLUMNS = "VISIBLE_COLUMNS";     //$NON-NLS-1$
    /**
     * Memento key for the number of visible columns in a VISIBLE_COLUMNS memento
     * or for the width of a column
     */
    private static final String SIZE = "SIZE";   //$NON-NLS-1$
    /**
     * Memento key prefix a visible column
     */
    private static final String COLUMN = "COLUMN";   //$NON-NLS-1$  
    

    private Table fTable;

    private TableEditor fTableEditor;

    private TableEditorImpl fTableEditorImpl;

    /**
     * Map of column ids to persisted sizes
     */
    private Map fColumnSizes = new HashMap();
    
    /**
     * Whether columns need re-building.
     */
    private boolean fBuildColumns = true;
    
    /**
     * Current column presentation or <code>null</code>
     */
    private IColumnPresentation fColumnPresentation;
    /**
     * Map of columns presentation id to its visible colums ids (String[])
     * When a columns presentation is not in the map, default settings are used.
     */
    private Map fVisibleColumns = new HashMap();    
    public AsynchronousTableViewer(Composite parent) {
        this(parent, SWT.VIRTUAL);
    }

    public AsynchronousTableViewer(Composite parent, int style) {
        this(new Table(parent, style));
    }

    /**
     * Table must be SWT.VIRTUAL. This is intentional. Labels will never be
     * retrieved for non-visible items.
     * 
     * @see SWT.VIRTUAL
     * @param table
     */
    public AsynchronousTableViewer(Table table) {
        Assert.isTrue((table.getStyle() & SWT.VIRTUAL) != 0);
        fTable = table;
        hookControl(fTable);
        fTableEditor = new TableEditor(fTable);
        fTableEditorImpl = createTableEditorImpl();
    }
    
    protected void persistColumnSizes() {
        Table table = getTable();
        TableColumn[] columns = table.getColumns();
        for (int i = 0; i < columns.length; i++) {
            TableColumn tableColumn = columns[i];
            Object id = tableColumn.getData();
            fColumnSizes.put(id, new Integer(tableColumn.getWidth()));
        }
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
     * Configures the columns based on the current settings.
     * 
     * @param input
     */
    protected void configureColumns() {
        if (fColumnPresentation != null) {
            if (fBuildColumns) {
                buildColumns(fColumnPresentation);                    
            }
        } else {
            // get rid of columns
            buildColumns(null);
        }
    }
    
    /**
     * Returns whether columns can be toggled on/off for the current input.
     * 
     * @return whether columns can be toggled on/off for the current input
     */
    public boolean canToggleColumns() {
        return fColumnPresentation != null && fColumnPresentation.isOptional();
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
    
    protected void buildColumns(IColumnPresentation presentation) {
        persistColumnSizes();
        // dispose current columns, persisting their weigts
        Table table = getTable();
        TableColumn[] columns = table.getColumns();
        for (int i = 0; i < columns.length; i++) {
            columns[i].dispose();
        }
        // TODO: persist/restore columns per presentation
        if (presentation != null) {
            String[] columnIds = getVisibleColumns();
            for (int i = 0; i < columnIds.length; i++) {
                String id = columnIds[i];
                String header = presentation.getHeader(id);
                // TODO: allow client to specify style
                TableColumn column = new TableColumn(table, SWT.LEFT, i);
                column.setMoveable(true);
                column.setText(header);
                ImageDescriptor image = presentation.getImageDescriptor(id);
                if (image != null) {
                    column.setImage(getImage(image));
                }
                column.setData(id);
            }
            table.setHeaderVisible(true);
            table.setLinesVisible(true);
        } else {
            table.setHeaderVisible(false);
            table.setLinesVisible(false);
        }
        columns = table.getColumns();
        for (int i = 0; i < columns.length; i++) {
            Integer width = (Integer) fColumnSizes.get(columns[i].getData());
            if (width == null) {
                columns[i].pack();
            } else {
                columns[i].setWidth(width.intValue());
            }
        }
        
    }

    /**
     * Initializes veiwer state from the memento
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
        
    }
    
    protected void hookControl(Control control) {
        super.hookControl(control);
        control.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                fTableEditorImpl.handleMouseDown(e);
            }
        });      
    }

    public synchronized void dispose() {
        if (fColumnPresentation != null) {
            // persist column sizes, if any
            buildColumns(null);
            fColumnPresentation.dispose();
        }
        if (fTableEditor != null)
            fTableEditor.dispose();
        fTable.dispose();
        
        super.dispose();
    }  

    protected ISelection doAttemptSelectionToWidget(ISelection selection, boolean reveal) {
        if (acceptsSelection(selection)) {
            List list = ((IStructuredSelection) selection).toList();
            if (list == null) {
                fTable.deselectAll();
                return StructuredSelection.EMPTY;
            }

            int[] indices = new int[list.size()];
            ModelNode[] nodes = getModel().getRootNode().getChildrenNodes();
            if (nodes != null) {
	            int index = 0;
	
	            // I'm not sure if it would be faster to check TableItems first...
	            for (int i = 0; i < nodes.length; i++) {
	                Object element = nodes[i].getElement();
	                if (list.contains(element)) {
	                    indices[index] = i;
	                    index++;
	                }
	            }
	
	            fTable.setSelection(indices);
	            if (reveal && indices.length > 0) {
	                TableItem item = fTable.getItem(indices[0]);
	                fTable.showItem(item);
	            }
            }
        }
        return StructuredSelection.EMPTY;
    }

    protected boolean acceptsSelection(ISelection selection) {
        return selection instanceof IStructuredSelection;
    }

    protected ISelection getEmptySelection() {
        return StructuredSelection.EMPTY;
    }

    protected Widget getParent(Widget widget) {
        if (widget instanceof TableItem) {
            return fTable;
        }
        return null;
    }

    protected List getSelectionFromWidget() {
        TableItem[] selection = fTable.getSelection();
        List datas = new ArrayList(selection.length);
        for (int i = 0; i < selection.length; i++) {
            datas.add(selection[i].getData());
        }
        return datas;
    }

    public Control getControl() {
        return fTable;
    }

    public Table getTable() {
        return (Table) getControl();
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousViewer#internalRefresh(org.eclipse.debug.internal.ui.model.viewers.ModelNode)
     */
    protected void internalRefresh(ModelNode node) {
        super.internalRefresh(node);
        if (node.getElement().equals(getInput())) {
            updateChildren(node);
        }
    } 

    public void setLabels(Widget widget, String[] labels, ImageDescriptor[] imageDescriptors) {
        TableItem item = (TableItem) widget;
        item.setText(labels);
        Image[] images = new Image[labels.length];
        if (imageDescriptors != null) {
            for (int i = 0; i < images.length; i++) {
                if (i < imageDescriptors.length)
                    images[i] = getImage(imageDescriptors[i]);
            }
        }
        item.setImage(images);
    }

    public void setColors(Widget widget, RGB[] foregrounds, RGB[] backgrounds) {
        TableItem item = (TableItem) widget;
        if (foregrounds == null) {
            foregrounds = new RGB[fTable.getColumnCount()];
        }
        if (backgrounds == null) {
            backgrounds = new RGB[fTable.getColumnCount()];
        }

        for (int i = 0; i < foregrounds.length; i++) {
            Color fg = getColor(foregrounds[i]);
            item.setForeground(i, fg);
        }
        for (int i = 0; i < backgrounds.length; i++) {
            Color bg = getColor(backgrounds[i]);
            item.setBackground(i, bg);
        }
    }

    public void setFonts(Widget widget, FontData[] fontDatas) {
        TableItem item = (TableItem) widget;
        if (fontDatas != null) {
            for (int i = 0; i < fontDatas.length; i++) {
                Font font = getFont(fontDatas[i]);
                item.setFont(i, font);
            }
        }
    }

    public void setColumnHeaders(final String[] headers) {
        fTableEditorImpl.setColumnProperties(headers);
    }

    public Object[] getColumnProperties() {
        return fTableEditorImpl.getColumnProperties();
    }

    public void showColumnHeader(final boolean showHeaders) {
        WorkbenchJob job = new WorkbenchJob("Set Header Visibility") { //$NON-NLS-1$
            public IStatus runInUIThread(IProgressMonitor monitor) {
                fTable.setHeaderVisible(showHeaders);
                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.INTERACTIVE);
        job.setSystem(true);
        job.schedule();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.StructuredViewer#reveal(java.lang.Object)
     */
    public void reveal(Object element) {
        Assert.isNotNull(element);
        Widget w = findItem(element);
        if (w instanceof TableItem)
            getTable().showItem((TableItem) w);
    }

    /**
     * Sets the cell editors of this table viewer.
     * 
     * @param editors
     *            the list of cell editors
     */
    public void setCellEditors(CellEditor[] editors) {
        fTableEditorImpl.setCellEditors(editors);
    }

    /**
     * Sets the cell modifier of this table viewer.
     * 
     * @param modifier
     *            the cell modifier
     */
    public void setCellModifier(ICellModifier modifier) {
        fTableEditorImpl.setCellModifier(modifier);
    }

    protected TableEditorImpl createTableEditorImpl() {
        return new TableEditorImpl(this) {
            Rectangle getBounds(Item item, int columnNumber) {
                return ((TableItem) item).getBounds(columnNumber);
            }

            int getColumnCount() {
                return getTable().getColumnCount();
            }

            Item[] getSelection() {
                return getTable().getSelection();
            }

            void setEditor(Control w, Item item, int columnNumber) {
                fTableEditor.setEditor(w, (TableItem) item, columnNumber);
            }

            void setSelection(StructuredSelection selection, boolean b) {
                AsynchronousTableViewer.this.setSelection(selection, b);
            }

            void showSelection() {
                getTable().showSelection();
            }

            void setLayoutData(CellEditor.LayoutData layoutData) {
                fTableEditor.grabHorizontal = layoutData.grabHorizontal;
                fTableEditor.horizontalAlignment = layoutData.horizontalAlignment;
                fTableEditor.minimumWidth = layoutData.minimumWidth;
            }

            void handleDoubleClickEvent() {
                Viewer viewer = getViewer();
                fireDoubleClick(new DoubleClickEvent(viewer, viewer.getSelection()));
                fireOpen(new OpenEvent(viewer, viewer.getSelection()));
            }
        };
    }

    protected ISelection newSelectionFromWidget() {
        Control control = getControl();
        if (control == null || control.isDisposed()) {
            return StructuredSelection.EMPTY;
        }
        List list = getSelectionFromWidget();
        return new StructuredSelection(list);
    }

    public CellEditor[] getCellEditors() {
        return fTableEditorImpl.getCellEditors();
    }

    public ICellModifier getCellModifier() {
        return fTableEditorImpl.getCellModifier();
    }

    public boolean isCellEditorActive() {
        return fTableEditorImpl.isCellEditorActive();
    }

    /**
     * This is not asynchronous. This method must be called in the UI Thread.
     */
    public void cancelEditing() {
        fTableEditorImpl.cancelEditing();
    }

    /**
     * This is not asynchronous. This method must be called in the UI Thread.
     * 
     * @param element
     *            The element to edit. Each element maps to a row in the Table.
     * @param column
     *            The column to edit
     */
    public void editElement(Object element, int column) {
        fTableEditorImpl.editElement(element, column);
    }

    protected int indexForElement(Object element) {
        ViewerSorter sorter = getSorter();
        if (sorter == null)
            return fTable.getItemCount();
        int count = fTable.getItemCount();
        int min = 0, max = count - 1;
        while (min <= max) {
            int mid = (min + max) / 2;
            Object data = fTable.getItem(mid).getData();
            int compare = sorter.compare(this, data, element);
            if (compare == 0) {
                // find first item > element
                while (compare == 0) {
                    ++mid;
                    if (mid >= count) {
                        break;
                    }
                    data = fTable.getItem(mid).getData();
                    compare = sorter.compare(this, data, element);
                }
                return mid;
            }
            if (compare < 0)
                min = mid + 1;
            else
                max = mid - 1;
        }
        return min;
    }

    public void add(Object element) {
        if (element != null)
            add(new Object[] { element });
    }

    public void add(Object[] elements) {
        if (elements == null || elements.length == 0)
            return; // done
        ((AsynchronousTableModel)getModel()).add(elements);
    }

    public void remove(Object element) {
        if (element != null)
            remove(new Object[] { element });
    }

    public void remove(final Object[] elements) {
        if (elements == null || elements.length == 0)
            return; // done
        ((AsynchronousTableModel)getModel()).remove(elements);
    }

    public void insert(Object element, int position) {
        if (element != null)
            insert(new Object[] { element }, position);
    }

    public void insert(Object[] elements, int position) {
        if (elements == null || elements.length == 0)
            return;
        ((AsynchronousTableModel)getModel()).insert(elements, position);
    }

    public void replace(Object element, Object replacement) {
        if (element == null || replacement == null)
            throw new IllegalArgumentException("unexpected null parameter"); //$NON-NLS-1$
        ((AsynchronousTableModel)getModel()).replace(element, replacement);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousViewer#createModel()
	 */
	protected AsynchronousModel createModel() {
		return new AsynchronousTableModel(this);
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousViewer#setItemCount(org.eclipse.swt.widgets.Widget, int)
     */
    protected void setItemCount(Widget parent, int itemCount) {
		fTable.setItemCount(itemCount);
	}  

	protected int getVisibleItemCount(int top) {
        int itemCount = fTable.getItemCount();
        return Math.min((fTable.getBounds().height / fTable.getItemHeight()) + 2, itemCount - top);
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousViewer#createUpdatePolicy()
	 */
	public AbstractUpdatePolicy createUpdatePolicy() {
		return new TableUpdatePolicy();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousViewer#getParentWidget(org.eclipse.swt.widgets.Widget)
	 */
	protected Widget getParentWidget(Widget widget) {
		if (widget instanceof TableItem) {
			return ((TableItem)widget).getParent();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousViewer#getChildWidget(org.eclipse.swt.widgets.Widget, int)
	 */
	protected Widget getChildWidget(Widget parent, int index) {
		if (index < fTable.getItemCount()) {
			return fTable.getItem(index);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousViewer#clear(org.eclipse.swt.widgets.Widget)
	 */
	protected void clear(Widget item) {
		if (item instanceof TableItem) {
			int i = fTable.indexOf((TableItem)item);
			if (i >= 0) {
				fTable.clear(i);
			}
		}
	}
}
