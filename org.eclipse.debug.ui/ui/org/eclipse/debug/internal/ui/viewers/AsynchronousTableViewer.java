package org.eclipse.debug.internal.ui.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.viewers.update.DefaultTableUpdatePolicy;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * TODO non virtual table support 
 * TODO sorting by column
 */
public class AsynchronousTableViewer extends AsynchronousViewer {

    private Table fTable;

    private AsynchronousTableViewerContentManager fContentManager;

    private TableEditor fTableEditor;

    private TableEditorImpl fTableEditorImpl;

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
        fContentManager = createContentManager();
        hookControl(fTable);
        fTableEditor = new TableEditor(fTable);
        fTableEditorImpl = createTableEditorImpl();
    }

    protected void hookControl(Control control) {
        super.hookControl(control);
        control.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                fTableEditorImpl.handleMouseDown(e);
            }
        });
    }

    public IUpdatePolicy createUpdatePolicy() {
        return new DefaultTableUpdatePolicy();
    }

    public synchronized void dispose() {
        fContentManager.dispose();
        fTable.dispose();
        fTableEditor.dispose();
        super.dispose();
    }

    private AsynchronousTableViewerContentManager createContentManager() {
        return new AsynchronousTableViewerContentManager(this);
    }

    protected ISelection doAttemptSelectionToWidget(ISelection selection, boolean reveal) {
        if (acceptsSelection(selection)) {
            List list = ((IStructuredSelection) selection).toList();
            if (list == null) {
                fTable.deselectAll();
                return StructuredSelection.EMPTY;
            }

            int[] indices = new int[list.size()];
            Object[] elements = fContentManager.getElements();
            int index = 0;

            // I'm not sure if it would be faster to check TableItems first...
            for (int i = 0; i < elements.length; i++) {
                Object element = elements[i];
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

    protected Widget doFindInputItem(Object element) {
        if (element.equals(getInput())) {
            return fTable;
        }
        TableItem[] items = fTable.getItems();
        for (int i = 0; i < items.length; i++) {
            Object data = items[i].getData();
            if (data != null && equals(data, element)) {
                return items[i];
            }
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

    void setChildren(Widget widget, List children) {
        Object[] elements = filter(children.toArray());
        if (elements.length > 0) {
            ViewerSorter sorter = getSorter();
            if (sorter != null)
                sorter.sort(this, elements);

            fContentManager.setElements(elements);
        }
    }

    protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
        super.doUpdateItem(item, element, fullMap);
    }

    protected void updateLabel(Object element, Widget item) {
        if (item instanceof Item) {
            IAsynchronousLabelAdapter adapter = getLabelAdapter(element);
            if (adapter != null) {
                ILabelRequestMonitor labelUpdate = new LabelRequestMonitor(item, this);
                schedule(labelUpdate);
                adapter.retrieveLabel(element, getPresentationContext(), labelUpdate);
            }
        }
    }

    protected void inputChanged(Object input, Object oldInput) {
        super.inputChanged(input, oldInput);
        map(input, fTable);
        refresh();
    }

    protected void internalRefresh(Object element, Widget widget) {
        super.internalRefresh(element, widget);
        if (element.equals(getRoot())) {
            IAsynchronousTreeContentAdapter adapter = getTableContentAdapter(element);
            if (adapter != null) {
                IChildrenRequestMonitor update = new ChildrenRequestMonitor(widget, this);
                schedule(update);
                adapter.retrieveChildren(element, getPresentationContext(), update);
            }
        }
    }

    private IAsynchronousTreeContentAdapter getTableContentAdapter(Object element) {
        IAsynchronousTreeContentAdapter adapter = null;
        if (element instanceof IAsynchronousTreeContentAdapter) {
            adapter = (IAsynchronousTreeContentAdapter) element;
        } else if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            adapter = (IAsynchronousTreeContentAdapter) adaptable.getAdapter(IAsynchronousTreeContentAdapter.class);
        }
        return adapter;
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
     * FIXME: currently requires UI Thread
     * 
     * @param index
     */
    void clear(int index) {
        TableItem item = fTable.getItem(index);
        Object data = item.getData();
        if (item.getData() != null) {
            unmap(data, item);
        }
        fTable.clear(index);
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

        ViewerSorter sorter = getSorter();
        if (sorter != null) {
            insert(elements, 0);
            return;
        }

        final Object[] filteredElements = filter(elements);
        if (filteredElements.length == 0)
            return;

        WorkbenchJob job = new WorkbenchJob("AsychronousTableViewer.add") { //$NON-NLS-1$
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (!monitor.isCanceled())
                    fContentManager.add(filteredElements);
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.setPriority(Job.INTERACTIVE);
        job.schedule();
    }

    public void remove(Object element) {
        if (element != null)
            remove(new Object[] { element });
    }

    public void remove(final Object[] elements) {
        if (elements == null || elements.length == 0)
            return; // done

        WorkbenchJob job = new WorkbenchJob("AsychronousTableViewer.remove") { //$NON-NLS-1$
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (!monitor.isCanceled())
                    fContentManager.remove(elements);
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.setPriority(Job.INTERACTIVE);
        job.schedule();
    }

    public void insert(Object element, int position) {
        if (element != null)
            insert(new Object[] { element }, position);
    }

    public void insert(Object[] elements, final int position) {
        if (elements == null || elements.length == 0)
            return;

        final Object[] filteredElements = filter(elements);
        WorkbenchJob job = new WorkbenchJob("AsychronousTableViewer.insert") { //$NON-NLS-1$
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (monitor.isCanceled())
                    return Status.OK_STATUS;

                ViewerSorter sorter = getSorter();
                if (sorter == null) {
                    fContentManager.insert(filteredElements, position);
                } else {
                    for (int i = 0; i < filteredElements.length; i++) {
                        Object element = filteredElements[i];
                        int index = indexForElement(element);
                        fContentManager.insert(new Object[] { element }, index);
                    }
                }
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.setPriority(Job.INTERACTIVE);
        job.schedule();
    }

    public void replace(final Object element, final Object replacement) {
        if (element == null || replacement == null)
            throw new IllegalArgumentException("unexpected null parameter"); //$NON-NLS-1$

        Object[] filtered = filter(new Object[] { replacement });
        if (filtered.length == 0) {
            remove(element);
            return;
        }

        WorkbenchJob job = new WorkbenchJob("AsychronousTableViewer.replace") { //$NON-NLS-1$
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (monitor.isCanceled())
                    return Status.OK_STATUS;

                ViewerSorter sorter = getSorter();
                if (sorter == null) {
                    fContentManager.replace(element, replacement);
                } else {
                    fContentManager.remove(new Object[] { element });
                    int position = indexForElement(replacement);
                    fContentManager.insert(new Object[] { replacement }, position);
                }
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.setPriority(Job.INTERACTIVE);
        job.schedule();
    }
}
