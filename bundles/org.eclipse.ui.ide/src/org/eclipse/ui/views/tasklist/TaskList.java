/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cagatay Kavukcuoglu <cagatayk@acm.org> - Filter for markers in same project
 *     Sebastian Davids <sdavids@gmx.de> - Reordered menu items
 *******************************************************************************/

package org.eclipse.ui.views.tasklist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.views.tasklist.TaskListMessages;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.markers.MarkerSupportView;

/**
 * Main class for the Task List view for displaying tasks and problem annotations
 * on resources, and for opening an editor on the resource when the user commands.
 * 
 * @deprecated This view is no longer in use as of Eclipse 3.4.
 * The view referenced by {@link IPageLayout#ID_TASK_LIST} is an {@link MarkerSupportView}.
 * </p>
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TaskList extends ViewPart {

    private Table table;

    private TaskSorter comparator;

    private CellEditor descriptionEditor;

    private TableViewer viewer;

    private TasksFilter filter = new TasksFilter();

    private IMemento memento;

    private boolean markerLimitExceeded;

    private Composite parent;

    private StackLayout stackLayout = new StackLayout();

    private Composite compositeMarkerLimitExceeded;

    private CellEditorActionHandler editorActionHandler;

    private TaskAction newTaskAction;

    private TaskAction copyTaskAction;

    private TaskAction pasteTaskAction;

    private TaskAction removeTaskAction;

    private TaskAction purgeCompletedAction;

    private TaskAction gotoTaskAction;

    private TaskAction selectAllAction;

    private ResolveMarkerAction resolveMarkerAction;

    private TaskAction filtersAction;

    private MarkCompletedAction markCompletedAction;

    private TaskAction propertiesAction;

    //sort by action
    private Action sortByCategoryAction;

    private Action sortByCompletedAction;

    private Action sortByPriorityAction;

    private Action sortByDescriptionAction;

    private Action sortByResourceAction;

    private Action sortByContainerAction;

    private Action sortByLocationAction;

    private Action sortByCreationTimeAction;

    private Action sortAscendingAction;

    private Action sortDescendingAction;

    private Clipboard clipboard;

    private static String[] tableColumnProperties = {
            IBasicPropertyConstants.P_IMAGE, IMarker.DONE, IMarker.PRIORITY,
            IMarker.MESSAGE, IMarkerConstants.P_RESOURCE_NAME,
            IMarkerConstants.P_CONTAINER_NAME,
            IMarkerConstants.P_LINE_AND_LOCATION };

    // Persistance tags.
    private static final String TAG_COLUMN = "column"; //$NON-NLS-1$

    private static final String TAG_NUMBER = "number"; //$NON-NLS-1$

    private static final String TAG_WIDTH = "width"; //$NON-NLS-1$

    private static final String TAG_FILTER = "filter"; //$NON-NLS-1$

    private static final String TAG_SELECTION = "selection"; //$NON-NLS-1$

    private static final String TAG_ID = "id"; //$NON-NLS-1$

    private static final String TAG_MARKER = "marker"; //$NON-NLS-1$

    private static final String TAG_RESOURCE = "resource"; //$NON-NLS-1$

    private static final String TAG_TOP_INDEX = "topIndex"; //$NON-NLS-1$

    private static final String TAG_SORT_SECTION = "TaskListSortState"; //$NON-NLS-1$

    static class TaskListLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        private static String[] keys = { IBasicPropertyConstants.P_IMAGE,
                IMarkerConstants.P_COMPLETE_IMAGE,
                IMarkerConstants.P_PRIORITY_IMAGE, IMarker.MESSAGE,
                IMarkerConstants.P_RESOURCE_NAME,
                IMarkerConstants.P_CONTAINER_NAME,
                IMarkerConstants.P_LINE_AND_LOCATION };

        public String getColumnText(Object element, int columnIndex) {
            if (columnIndex >= 3 && columnIndex <= 6) {
				return (String) MarkerUtil.getProperty(element,
                        keys[columnIndex]);
			}
            return ""; //$NON-NLS-1$
        }

        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex >= 0 && columnIndex <= 2) {
                return (Image) MarkerUtil.getProperty(element,
                        keys[columnIndex]);
            }
            return null;
        }
    }

    class SortByAction extends Action {

        private int column;

        /**
         * @param column
         */
        public SortByAction(int column) {
            this.column = column;
        }

        public void run() {
            comparator.setTopPriority(column);
            updateSortingState();
            viewer.refresh();
            IDialogSettings workbenchSettings = getPlugin().getDialogSettings();
            IDialogSettings settings = workbenchSettings
                    .getSection(TAG_SORT_SECTION);
            if (settings == null) {
				settings = workbenchSettings.addNewSection(TAG_SORT_SECTION);
			}
            comparator.saveState(settings);
        }
    }

    class SortDirectionAction extends Action {

        private int direction;

        /**
         * @param direction
         */
        public SortDirectionAction(int direction) {
            this.direction = direction;
        }

        public void run() {
            comparator.setTopPriorityDirection(direction);
            updateSortingState();
            viewer.refresh();
            IDialogSettings workbenchSettings = getPlugin().getDialogSettings();
            IDialogSettings settings = workbenchSettings
                    .getSection(TAG_SORT_SECTION);
            if (settings == null) {
				settings = workbenchSettings.addNewSection(TAG_SORT_SECTION);
			}
            comparator.saveState(settings);
        }
    }

    private String columnHeaders[] = {
            TaskListMessages.TaskList_headerIcon,
            TaskListMessages.TaskList_headerCompleted, 
            TaskListMessages.TaskList_headerPriority, 
            TaskListMessages.TaskList_headerDescription, 
            TaskListMessages.TaskList_headerResource,
            TaskListMessages.TaskList_headerFolder, 
            TaskListMessages.TaskList_headerLocation
    };

    private ColumnLayoutData columnLayouts[] = {
            new ColumnPixelData(16, false, true), new ColumnPixelData(16, false, true),
            new ColumnPixelData(16, false, true), new ColumnWeightData(200),
            new ColumnWeightData(75), new ColumnWeightData(150),
            new ColumnWeightData(60) };

    private IPartListener partListener = new IPartListener() {
        public void partActivated(IWorkbenchPart part) {
            TaskList.this.partActivated(part);
        }

        public void partBroughtToTop(IWorkbenchPart part) {
        }

        public void partClosed(IWorkbenchPart part) {
            TaskList.this.partClosed(part);
        }

        public void partDeactivated(IWorkbenchPart part) {
        }

        public void partOpened(IWorkbenchPart part) {
        }
    };

    private ISelectionChangedListener focusSelectionChangedListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
            TaskList.this.focusSelectionChanged(event);
        }
    };

    private IResource[] focusResources;

    private IWorkbenchPart focusPart;

    private ISelectionProvider focusSelectionProvider;

    private ICellModifier cellModifier = new ICellModifier() {
        public Object getValue(Object element, String property) {
            return MarkerUtil.getProperty(element, property);
        }

        public boolean canModify(Object element, String property) {
            return MarkerUtil.isEditable((IMarker) element);
        }

        /**
         * Modifies a marker as a result of a successfully completed direct editing.
         */
        public void modify(Object element, String property, Object value) {
            Item item = (Item) element;
            IMarker marker = (IMarker) item.getData();
            setProperty(marker, property, value);
        }
    };

    /**
     * Creates a new task list view.
     */
    public TaskList() {
        super();
    }

    /**
     * @param control
     */
    void addDragSupport(Control control) {

        int operations = DND.DROP_COPY;
        Transfer[] transferTypes = new Transfer[] {
                MarkerTransfer.getInstance(), TextTransfer.getInstance() };
        DragSourceListener listener = new DragSourceAdapter() {
            public void dragSetData(DragSourceEvent event) {
                performDragSetData(event);
            }

            public void dragFinished(DragSourceEvent event) {
            }
        };
        viewer.addDragSupport(operations, transferTypes, listener);
    }

    void cancelEditing() {
        getTableViewer().cancelEditing();
    }

    void createColumns() {
        /**
         * This class handles selections of the column headers.
         * Selection of the column header will cause resorting
         * of the shown tasks using that column's sorter.
         * Repeated selection of the header will toggle
         * sorting order (ascending versus descending).
         */
        SelectionListener headerListener = new SelectionAdapter() {
            /**
             * Handles the case of user selecting the
             * header area.
             * <p>If the column has not been selected previously,
             * it will set the sorter of that column to be
             * the current tasklist sorter. Repeated
             * presses on the same column header will
             * toggle sorting order (ascending/descending).
             */
            public void widgetSelected(SelectionEvent e) {
                // column selected - need to sort
                int column = table.indexOf((TableColumn) e.widget);
                if (column == comparator.getTopPriority()) {
					comparator.reverseTopPriority();
				} else {
                    comparator.setTopPriority(column);
                }
                updateSortingState();
                viewer.refresh();
                IDialogSettings workbenchSettings = getPlugin()
                        .getDialogSettings();
                IDialogSettings settings = workbenchSettings
                        .getSection(TAG_SORT_SECTION);
                if (settings == null) {
					settings = workbenchSettings
                            .addNewSection(TAG_SORT_SECTION);
				}
                comparator.saveState(settings);
            }
        };

        if (memento != null) {
            //restore columns width
            IMemento children[] = memento.getChildren(TAG_COLUMN);
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    Integer val = children[i].getInteger(TAG_NUMBER);
                    if (val != null) {
                        int index = val.intValue();
                        val = children[i].getInteger(TAG_WIDTH);
                        if (val != null) {
                            columnLayouts[index] = new ColumnPixelData(val
                                    .intValue(), true);
                        }
                    }
                }
            }
        }

        boolean text = Util.isMac();
        TableLayout layout = new TableLayout();
        table.setLayout(layout);
        table.setHeaderVisible(true);

        for (int i = 0; i < columnHeaders.length; i++) {
            TableColumn tc = new TableColumn(table, SWT.NONE, i);

            if (!text && i == 1) {
				tc.setImage(MarkerUtil.getImage("header_complete")); //$NON-NLS-1$
			} else if (!text && i == 2) {
				tc.setImage(MarkerUtil.getImage("header_priority")); //$NON-NLS-1$
			} else {
				tc.setText(columnHeaders[i]);
			}

            if (text && (i == 1 || i == 2)) {
                tc.pack();
                columnLayouts[i] = new ColumnPixelData(Math.max(16, tc
                        .getWidth()), false, true);
            }

            tc.setResizable(columnLayouts[i].resizable);
            layout.addColumnData(columnLayouts[i]);
            tc.addSelectionListener(headerListener);
        }
    }

    /**
     * Returns a string that summarizes the contents of the
     * given markers.
     */
    static String createMarkerReport(IMarker[] markers) {
        StringBuffer buf = new StringBuffer();
        // Create the header
        buf.append(TaskListMessages.TaskList_reportKind);
        buf.append("\t"); //$NON-NLS-1$
        buf.append(TaskListMessages.TaskList_reportStatus); 
        buf.append("\t"); //$NON-NLS-1$
        buf.append(TaskListMessages.TaskList_reportPriority); 
        buf.append("\t"); //$NON-NLS-1$
        buf.append(TaskListMessages.TaskList_headerDescription);
        buf.append("\t"); //$NON-NLS-1$
        buf.append(TaskListMessages.TaskList_headerResource); 
        buf.append("\t"); //$NON-NLS-1$
        buf.append(TaskListMessages.TaskList_headerFolder); 
        buf.append("\t"); //$NON-NLS-1$
        buf.append(TaskListMessages.TaskList_headerLocation); 
        buf.append(System.getProperty("line.separator")); //$NON-NLS-1$

        // Create the report for the markers
        for (int i = 0; i < markers.length; i++) {
            writeMarker(buf, markers[i]);
        }
        return buf.toString();
    }

    /**
     * Writes a string representation of the given marker to the buffer.
     */
    static void writeMarker(StringBuffer buf, IMarker marker) {
        buf.append(MarkerUtil.getKindText(marker));
        buf.append("\t"); //$NON-NLS-1$
        buf.append(MarkerUtil.getCompleteText(marker));
        buf.append("\t"); //$NON-NLS-1$
        buf.append(MarkerUtil.getPriorityText(marker));
        buf.append("\t"); //$NON-NLS-1$
        buf.append(MarkerUtil.getMessage(marker));
        buf.append("\t"); //$NON-NLS-1$
        buf.append(MarkerUtil.getResourceName(marker));
        buf.append("\t"); //$NON-NLS-1$
        buf.append(MarkerUtil.getContainerName(marker));
        buf.append("\t"); //$NON-NLS-1$
        buf.append(MarkerUtil.getLineAndLocation(marker));
        buf.append(System.getProperty("line.separator")); //$NON-NLS-1$
    }

    /* package */
    boolean isMarkerLimitExceeded() {
        return markerLimitExceeded;
    }

    /* package */
    void setMarkerLimitExceeded(boolean markerLimitExceeded) {
        this.markerLimitExceeded = markerLimitExceeded;

        if (markerLimitExceeded) {
            stackLayout.topControl = compositeMarkerLimitExceeded;
        } else {
            stackLayout.topControl = table;
        }

        parent.layout();
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchPart.
     */
    public void createPartControl(Composite parent) {
        //	long t = System.currentTimeMillis();
        createPartControl0(parent);
        //	t = System.currentTimeMillis() - t;
        //	System.out.println("TaskList.createPartControl: " + t + "ms");
    }

    private void createPartControl0(Composite parent) {
        this.parent = parent;
        clipboard = new Clipboard(parent.getDisplay());
        createTable(parent);
        viewer = new TableViewer(table);
        viewer.setUseHashlookup(true);
        createColumns();
        makeActions();
        fillActionBars();
        addDragSupport(table);

        compositeMarkerLimitExceeded = new Composite(parent, SWT.NONE);
        compositeMarkerLimitExceeded.setLayout(new GridLayout());
        Label labelMarkerLimitExceeded = new Label(
                compositeMarkerLimitExceeded, SWT.WRAP);
        labelMarkerLimitExceeded.setText(TaskListMessages.TaskList_markerLimitExceeded);
        parent.setLayout(stackLayout);
        setMarkerLimitExceeded(false);

        viewer.setContentProvider(new TaskListContentProvider(this));
        viewer.setLabelProvider(new TaskListLabelProvider());
        if (memento != null) {
            //restore filter
            IMemento filterMem = memento.getChild(TAG_FILTER);
            if (filterMem != null) {
				getFilter().restoreState(filterMem);
			}
        }

        comparator = new TaskSorter();
        IDialogSettings workbenchSettings = getPlugin().getDialogSettings();
        IDialogSettings settings = workbenchSettings
                .getSection(TAG_SORT_SECTION);
        comparator.restoreState(settings);
        viewer.setComparator(comparator);

        //update the menu to indicate how task are currently sorted
        updateSortingState();
        viewer.setInput(getWorkspace().getRoot());
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                TaskList.this.selectionChanged(event);
            }
        });
        viewer.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                gotoTaskAction.run();
            }
        });
        viewer.getControl().addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e);
            }
        });

        //Add in some accessibility support to supplement the description that we already 
        //get from the SWT table.
        viewer.getControl().getAccessible().addAccessibleControlListener(
                new AccessibleControlAdapter() {

                    /* (non-Javadoc)
                     * @see org.eclipse.swt.accessibility.AccessibleControlListener#getValue(org.eclipse.swt.accessibility.AccessibleControlEvent)
                     */
                    public void getValue(AccessibleControlEvent e) {

                        int childIndex = e.childID;

                        if (childIndex == ACC.CHILDID_SELF) {
                            super.getValue(e);
                            return;
                        }
                        Object item = viewer.getElementAt(childIndex);
                        if (item instanceof IMarker) {
                            IMarker marker = (IMarker) item;

                            //If it is editable all we need is completeness
                            // the rest is found by the table accessibility
                            if (MarkerUtil.isEditable(marker)) {
								e.result = MarkerUtil.getCompleteText(marker);
							} else {
								//Otherwise all it needs is severity
                                e.result = MarkerUtil.getKindText(marker);
							}

                        } else {
                            super.getValue(e);
                            return;
                        }

                    }

                });

        CellEditor editors[] = new CellEditor[columnHeaders.length];
        editors[1] = new CheckboxCellEditor(table);
        String[] priorities = new String[] {
                TaskListMessages.TaskList_high, 
                TaskListMessages.TaskList_normal, 
                TaskListMessages.TaskList_low
        };
        editors[2] = new ComboBoxCellEditor(table, priorities, SWT.READ_ONLY);
        editors[3] = descriptionEditor = new TextCellEditor(table);
        viewer.setCellEditors(editors);
        viewer.setCellModifier(cellModifier);
        viewer.setColumnProperties(tableColumnProperties);

        // Configure the context menu to be lazily populated on each pop-up.
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                TaskList.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(table);
        table.setMenu(menu);
        // Be sure to register it so that other plug-ins can add actions.
        getSite().registerContextMenu(menuMgr, viewer);

        // Track selection in the page.
        getSite().getPage().addPartListener(partListener);

        // Add global action handlers.
        editorActionHandler = new CellEditorActionHandler(getViewSite()
                .getActionBars());
        editorActionHandler.addCellEditor(descriptionEditor);
        editorActionHandler.setCopyAction(copyTaskAction);
        editorActionHandler.setPasteAction(pasteTaskAction);
        editorActionHandler.setDeleteAction(removeTaskAction);
        editorActionHandler.setSelectAllAction(selectAllAction);

        getViewSite().getActionBars().setGlobalActionHandler(
                ActionFactory.PROPERTIES.getId(), propertiesAction);

        getSite().setSelectionProvider(viewer);

        if (memento != null) {
			restoreState(memento);
		}
        memento = null;

        // Set help on the view itself
        viewer.getControl().addHelpListener(new HelpListener() {
            /*
             * @see HelpListener#helpRequested(HelpEvent)
             */
            public void helpRequested(HelpEvent e) {
                String contextId = null;
                // See if there is a context registered for the current selection
                IMarker marker = (IMarker) ((IStructuredSelection) getSelection())
                        .getFirstElement();
                if (marker != null) {
                    contextId = IDE.getMarkerHelpRegistry().getHelp(marker);
                }

                if (contextId == null) {
					contextId = ITaskListHelpContextIds.TASK_LIST_VIEW;
				}

                getSite().getWorkbenchWindow().getWorkbench().getHelpSystem()
						.displayHelp(contextId);
            }
        });

        // Prime the status line and title.
        updateStatusMessage();
        updateTitle();
    }

    /**
     * Creates the table control.
     */
    void createTable(Composite parent) {
        table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
                | SWT.FULL_SELECTION);
        table.setLinesVisible(true);
        //table.setLayout(new TableLayout());

        new TableEditor(table);
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchPart.
     */
    public void dispose() {
        super.dispose();
        getSite().getPage().removePartListener(partListener);
        if (focusSelectionProvider != null) {
            focusSelectionProvider
                    .removeSelectionChangedListener(focusSelectionChangedListener);
            focusSelectionProvider = null;
        }
        focusPart = null;
        if (editorActionHandler != null) {
            editorActionHandler.dispose();
            editorActionHandler = null;
        }
        if (clipboard != null) {
			clipboard.dispose();
		}
    }

    /**
     * Activates the editor on the given marker.
     * 
     * @param marker the marker to edit
     */
    public void edit(IMarker marker) {
        viewer.editElement(marker, 3);
    }

    /**
     * Fills the local tool bar and menu manager with actions.
     */
    void fillActionBars() {
        IActionBars actionBars = getViewSite().getActionBars();
        IMenuManager menu = actionBars.getMenuManager();
        IMenuManager submenu = new MenuManager(TaskListMessages.SortByMenu_text);

        menu.add(submenu);
        submenu.add(sortByCategoryAction);
        submenu.add(sortByCompletedAction);
        submenu.add(sortByPriorityAction);
        submenu.add(sortByDescriptionAction);
        submenu.add(sortByResourceAction);
        submenu.add(sortByContainerAction);
        submenu.add(sortByLocationAction);
        submenu.add(sortByCreationTimeAction);
        submenu.add(new Separator());
        submenu.add(sortAscendingAction);
        submenu.add(sortDescendingAction);

        menu.add(filtersAction);

        IToolBarManager toolBar = actionBars.getToolBarManager();
        toolBar.add(newTaskAction);
        toolBar.add(removeTaskAction);
        toolBar.add(filtersAction);
    }

    /**
     * Contributes actions to the pop-up menu.
     */
    void fillContextMenu(IMenuManager menu) {
        // update enabled state for actions that aren't updated in selectionChanged
        IStructuredSelection selection = (IStructuredSelection) getSelection();
        markCompletedAction.setEnabled(markCompletedAction
                .shouldEnable(selection));
        resolveMarkerAction.setEnabled(resolveMarkerAction
                .shouldEnable(selection));

        // add the actions to the menu
        menu.add(newTaskAction);
        menu.add(gotoTaskAction);
        menu.add(new Separator());
        menu.add(copyTaskAction);
        menu.add(pasteTaskAction);
        menu.add(removeTaskAction);
        menu.add(new Separator());
        menu.add(markCompletedAction);
        menu.add(purgeCompletedAction);
        menu.add(new Separator());
        menu.add(resolveMarkerAction);
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu
                .add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
                        + "-end")); //$NON-NLS-1$
        menu.add(propertiesAction);
    }

    /**
     * The filter settings have changed.
     * Refreshes the viewer and title bar.
     */
    void filterChanged() {

        BusyIndicator.showWhile(viewer.getControl().getShell().getDisplay(),
                new Runnable() {
                    public void run() {
                        // Filter has already been updated by dialog; just refresh.
                        // Don't need to update labels for existing elements 
                        // since changes to filter settings don't affect them.
                        viewer.getControl().setRedraw(false);
                        viewer.refresh(false);
                        viewer.getControl().setRedraw(true);
                        // update after refresh since the content provider caches summary info
                        updateStatusMessage();
                        updateTitle();
                    }
                });

    }

    void focusSelectionChanged(SelectionChangedEvent event) {
        updateFocusResource(event.getSelection());
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IShowInSource.class) {
            return new IShowInSource() {
                public ShowInContext getShowInContext() {
                    return new ShowInContext(null, getSelection());
                }
            };
        }
        if (adapter == IShowInTargetList.class) {
            return new IShowInTargetList() {
                public String[] getShowInTargetIds() {
                    return new String[] { IPageLayout.ID_RES_NAV };
                }

            };
        }
        return super.getAdapter(adapter);
    }

    /**
     * Returns a clipboard for cut/copy/paste actions.
     * <p>
     * May only be called after this part's viewer has been created.
     * The clipboard is disposed when this part is disposed.
     * </p>
     * @return a clipboard
     * @since 2.0
     */
    /*package*/
    Clipboard getClipboard() {
        return clipboard;
    }

    /**
     * Returns the filter for the viewer.
     */
    TasksFilter getFilter() {
        return filter;
    }

    /**
     * Returns the UI plugin for the task list.
     */
    static AbstractUIPlugin getPlugin() {
        return (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
    }

    /**
     * Returns the resource for which the task list is showing tasks.
     *
     * @return the resource, possibly the workspace root
     */
    public IResource getResource() {
        if (showSelections()) {
            if (focusResources != null && focusResources.length >= 1
                    && focusResources[0] != null) {
                return focusResources[0];
            }
        }

        return getWorkspace().getRoot();
    }

    /**
     * Get the resources.
     * 
     * @return the resources
     */
    public IResource[] getResources() {
        if (showSelections()) {
            if (focusResources != null) {
                return focusResources;
            }
        }

        return new IResource[] { getWorkspace().getRoot() };
    }

    /**
     * Returns the resource depth which the task list is using to show tasks.
     *
     * @return an <code>IResource.DEPTH_*</code> constant
     */
    int getResourceDepth() {
        if (showSelections() && !showChildrenHierarchy()) {
			return IResource.DEPTH_ZERO;
		}

        return IResource.DEPTH_INFINITE;
    }

    /**
     * API method which returns the current selection.
     *
     * @return the current selection (element type: <code>IMarker</code>)
     */
    public ISelection getSelection() {
        return viewer.getSelection();
    }

    /**
     * Returns the message to display in the status line.
     */
    String getStatusMessage(IStructuredSelection selection) {
        if (selection != null && selection.size() == 1) {
            IMarker marker = (IMarker) selection.getFirstElement();
            return MarkerUtil.getMessage(marker);
        }

        TaskListContentProvider provider = (TaskListContentProvider) viewer
                .getContentProvider();

        if (selection != null && selection.size() > 1) {
            return provider.getStatusSummarySelected(selection);
        }
        return provider.getStatusSummaryVisible();
    }

    /**
     * When created, new task instance is cached in
     * order to keep it at the top of the list until
     * first edited. This method returns it, or
     * null if there is no task instance pending
     * for first editing.
     */
    TableViewer getTableViewer() {
        return viewer;
    }

    /**
     * Returns the workspace.
     */
    IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    /**
     * Handles key events in viewer.
     */
    void handleKeyPressed(KeyEvent event) {
        if (event.character == SWT.DEL && event.stateMask == 0
                && removeTaskAction.isEnabled()) {
			removeTaskAction.run();
		}
    }

    /* (non-Javadoc)
     * Method declared on IViewPart.
     */
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        this.memento = memento;
    }

    /**
     * Returns whether we are interested in markers on the given resource.
     */
    boolean checkResource(IResource resource) {
        if (!showSelections()) {
            return true;
        }

        IResource[] resources = getResources();
        IResource resource2;

        if (showOwnerProject()) {
            IProject project;

            for (int i = 0, l = resources.length; i < l; i++) {
                resource2 = resources[i];

                if (resource2 == null) {
                    return true;
                }
                project = resource2.getProject();

                if (project == null
                        || project.equals(resource.getProject())) {
					return true;
				}
            }
        }

        if (showChildrenHierarchy()) {
            for (int i = 0, l = resources.length; i < l; i++) {
                resource2 = resources[i];

                if (resource2 != null
                        && resource2.getFullPath().isPrefixOf(
                                resource.getFullPath())) {
					return true;
				}
            }
        } else {
			for (int i = 0, l = resources.length; i < l; i++) {
                resource2 = resources[i];

                if (resource.equals(resource2)) {
					return true;
				}
            }
		}

        return false;
    }

    /**
     * Returns whether the given marker should be shown,
     * given the current filter settings.
     */
    boolean shouldShow(IMarker marker) {
        return checkResource(marker.getResource())
                && getFilter().select(marker);
    }

    /**
     * Makes actions used in the local tool bar and
     * popup menu.
     */
    void makeActions() {
        ISharedImages sharedImages = PlatformUI.getWorkbench()
                .getSharedImages();

        // goto
        gotoTaskAction = new GotoTaskAction(this, "gotoFile"); //$NON-NLS-1$
        gotoTaskAction.setText(TaskListMessages.GotoTask_text); 
        gotoTaskAction.setToolTipText(TaskListMessages.GotoTask_tooltip); 
        gotoTaskAction.setImageDescriptor(MarkerUtil
                .getImageDescriptor("gotoobj")); //$NON-NLS-1$
        gotoTaskAction.setEnabled(false);

        // new task
        newTaskAction = new NewTaskAction(this, "newTask"); //$NON-NLS-1$
        newTaskAction.setText(TaskListMessages.NewTask_text); 
        newTaskAction.setToolTipText(TaskListMessages.NewTask_tooltip);
        newTaskAction.setImageDescriptor(MarkerUtil
                .getImageDescriptor("addtsk")); //$NON-NLS-1$
        newTaskAction.setDisabledImageDescriptor(MarkerUtil
                .getImageDescriptor("addtsk_disabled")); //$NON-NLS-1$

        // copy task
        copyTaskAction = new CopyTaskAction(this, "copy"); //$NON-NLS-1$
        copyTaskAction.setText(TaskListMessages.CopyTask_text); 
        copyTaskAction.setToolTipText(TaskListMessages.CopyTask_tooltip);
        copyTaskAction.setEnabled(false);

        // paste task
        pasteTaskAction = new PasteTaskAction(this, "paste"); //$NON-NLS-1$
        pasteTaskAction.setText(TaskListMessages.PasteTask_text); 
        pasteTaskAction.setToolTipText(TaskListMessages.PasteTask_tooltip);
        pasteTaskAction.setEnabled(false);

        // remove task
        removeTaskAction = new RemoveTaskAction(this, "delete"); //$NON-NLS-1$
        removeTaskAction.setText(TaskListMessages.RemoveTask_text);
        removeTaskAction.setToolTipText(TaskListMessages.RemoveTask_tooltip); 
        removeTaskAction.setImageDescriptor(sharedImages
                .getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
        removeTaskAction.setDisabledImageDescriptor(sharedImages
                .getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
        removeTaskAction.setEnabled(false);

        //mark completed task
        markCompletedAction = new MarkCompletedAction(this, "markCompleted"); //$NON-NLS-1$
        markCompletedAction.setText(TaskListMessages.MarkCompleted_text);
        markCompletedAction.setToolTipText(TaskListMessages.MarkCompleted_tooltip);
        markCompletedAction.setEnabled(false);

        //delete completed task
        purgeCompletedAction = new PurgeCompletedAction(this, "deleteCompleted"); //$NON-NLS-1$
        purgeCompletedAction.setText(TaskListMessages.PurgeCompleted_text);
        purgeCompletedAction.setToolTipText(TaskListMessages.PurgeCompleted_tooltip); 
        purgeCompletedAction.setImageDescriptor(sharedImages
                .getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
        purgeCompletedAction.setEnabled(true);

        // select all
        selectAllAction = new SelectAllTasksAction(this, "selectAll"); //$NON-NLS-1$
        selectAllAction.setText(TaskListMessages.SelectAll_text); 
        selectAllAction.setToolTipText(TaskListMessages.SelectAll_tooltip);

        // resolutions
        resolveMarkerAction = new ResolveMarkerAction(this, "resolve"); //$NON-NLS-1$
        resolveMarkerAction.setText(TaskListMessages.Resolve_text); 
        resolveMarkerAction.setToolTipText(TaskListMessages.Resolve_tooltip);
        resolveMarkerAction.setEnabled(false);

        // Sort by ->	
        sortByCategoryAction = new SortByAction(TaskSorter.TYPE);
        sortByCategoryAction.setText(TaskListMessages.SortByCategory_text);
        sortByCategoryAction.setToolTipText(TaskListMessages.SortByCategory_tooltip);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(sortByCategoryAction,
				ITaskListHelpContextIds.TASK_SORT_TYPE_ACTION);

        sortByCompletedAction = new SortByAction(TaskSorter.COMPLETION);
        sortByCompletedAction.setText(TaskListMessages.SortByCompleted_text);
        sortByCompletedAction.setToolTipText(TaskListMessages.SortByCompleted_tooltip); 
        PlatformUI.getWorkbench().getHelpSystem().setHelp(
				sortByCompletedAction,
				ITaskListHelpContextIds.TASK_SORT_COMPLETED_ACTION);

        sortByPriorityAction = new SortByAction(TaskSorter.PRIORITY);
        sortByPriorityAction.setText(TaskListMessages.SortByPriority_text); 
        sortByPriorityAction.setToolTipText(TaskListMessages.SortByPriority_tooltip); 
        PlatformUI.getWorkbench().getHelpSystem().setHelp(sortByPriorityAction,
				ITaskListHelpContextIds.TASK_SORT_PRIORITY_ACTION);

        sortByDescriptionAction = new SortByAction(TaskSorter.DESCRIPTION);
        sortByDescriptionAction.setText(TaskListMessages.SortByDescription_text); 
        sortByDescriptionAction.setToolTipText(TaskListMessages.SortByDescription_tooltip);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(
				sortByDescriptionAction,
				ITaskListHelpContextIds.TASK_SORT_DESCRIPTION_ACTION);

        sortByResourceAction = new SortByAction(TaskSorter.RESOURCE);
        sortByResourceAction.setText(TaskListMessages.SortByResource_text);
        sortByResourceAction.setToolTipText(TaskListMessages.SortByResource_tooltip); 
        PlatformUI.getWorkbench().getHelpSystem().setHelp(sortByResourceAction,
				ITaskListHelpContextIds.TASK_SORT_RESOURCE_ACTION);

        sortByContainerAction = new SortByAction(TaskSorter.FOLDER);
        sortByContainerAction.setText(TaskListMessages.SortByContainer_text); 
        sortByContainerAction.setToolTipText(TaskListMessages.SortByContainer_tooltip);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(
				sortByContainerAction,
				ITaskListHelpContextIds.TASK_SORT_FOLDER_ACTION);

        sortByLocationAction = new SortByAction(TaskSorter.LOCATION);
        sortByLocationAction.setText(TaskListMessages.SortByLocation_text); 
        sortByLocationAction.setToolTipText(TaskListMessages.SortByLocation_tooltip); 
        PlatformUI.getWorkbench().getHelpSystem().setHelp(sortByLocationAction,
				ITaskListHelpContextIds.TASK_SORT_LOCATION_ACTION);

        sortByCreationTimeAction = new SortByAction(TaskSorter.CREATION_TIME);
        sortByCreationTimeAction.setText(TaskListMessages.SortByCreationTime_text);
        sortByCreationTimeAction.setToolTipText(TaskListMessages.SortByCreationTime_tooltip); 
        PlatformUI.getWorkbench().getHelpSystem().setHelp(
				sortByCreationTimeAction,
				ITaskListHelpContextIds.TASK_SORT_CREATION_TIME_ACTION);

        sortAscendingAction = new SortDirectionAction(TaskSorter.ASCENDING);
        sortAscendingAction.setText(TaskListMessages.SortAscending_text);
        sortAscendingAction.setToolTipText(TaskListMessages.SortAscending_tooltip);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(sortAscendingAction,
				ITaskListHelpContextIds.TASK_SORT_ASCENDING_ACTION);

        sortDescendingAction = new SortDirectionAction(TaskSorter.DESCENDING);
        sortDescendingAction.setText(TaskListMessages.SortDescending_text);
        sortDescendingAction.setToolTipText(TaskListMessages.SortDescending_tooltip);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(sortDescendingAction,
				ITaskListHelpContextIds.TASK_SORT_DESCENDING_ACTION);

        // filters...
        filtersAction = new FiltersAction(this, "filter"); //$NON-NLS-1$
        filtersAction.setText(TaskListMessages.Filters_text); 
        filtersAction.setToolTipText(TaskListMessages.Filters_tooltip);
        filtersAction.setImageDescriptor(MarkerUtil
                .getImageDescriptor("filter")); //$NON-NLS-1$

        // properties
        propertiesAction = new TaskPropertiesAction(this, "properties"); //$NON-NLS-1$
        propertiesAction.setText(TaskListMessages.Properties_text); 
        propertiesAction.setToolTipText(TaskListMessages.Properties_tooltip);
        propertiesAction.setEnabled(false);
    }

    /**
     * The markers have changed.  Update the status line and title bar.
     */
    void markersChanged() {
        updateStatusMessage();
        updateTitle();
    }

    void partActivated(IWorkbenchPart part) {
        if (part == focusPart) {
			return;
		}

        if (focusSelectionProvider != null) {
            focusSelectionProvider
                    .removeSelectionChangedListener(focusSelectionChangedListener);
            focusSelectionProvider = null;
        }

        focusPart = part;
        if (focusPart != null) {
            focusSelectionProvider = focusPart.getSite().getSelectionProvider();
            if (focusSelectionProvider != null) {
                focusSelectionProvider
                        .addSelectionChangedListener(focusSelectionChangedListener);
                updateFocusResource(focusSelectionProvider.getSelection());
            } else {
                updateFocusResource(null);
            }
        }

    }

    void partClosed(IWorkbenchPart part) {
        if (part != focusPart) {
			return;
		}
        if (focusSelectionProvider != null) {
            focusSelectionProvider
                    .removeSelectionChangedListener(focusSelectionChangedListener);
            focusSelectionProvider = null;
        }
        focusPart = null;
    }

    /**
     * The user is attempting to drag marker data.  Add the appropriate
     * data to the event depending on the transfer type.
     */
    void performDragSetData(DragSourceEvent event) {
        if (MarkerTransfer.getInstance().isSupportedType(event.dataType)) {
            event.data = ((IStructuredSelection) viewer.getSelection())
                    .toArray();
            return;
        }
        if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
            Object[] data = ((IStructuredSelection) viewer.getSelection())
                    .toArray();
            if (data != null) {
                IMarker[] markers = new IMarker[data.length];
                for (int i = 0; i < markers.length; i++) {
                    markers[i] = (IMarker) data[i];
                }
                event.data = createMarkerReport(markers);
            }
            return;
        }
    }

    void restoreState(IMemento memento) {
        //restore selection
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IMemento selectionMem = memento.getChild(TAG_SELECTION);
        if (selectionMem != null) {
            ArrayList selectionList = new ArrayList();
            IMemento markerMems[] = selectionMem.getChildren(TAG_MARKER);
            for (int i = 0; i < markerMems.length; i++) {
                try {
                    long id = Long.parseLong(markerMems[i].getString(TAG_ID));
                    IResource resource = root.findMember(markerMems[i]
                            .getString(TAG_RESOURCE));
                    if (resource != null) {
                        IMarker marker = resource.findMarker(id);
                        if (marker != null) {
                            selectionList.add(marker);
                        }
                    }
                } catch (NumberFormatException e) {
                } catch (CoreException e) {
                }

            }
            viewer.setSelection(new StructuredSelection(selectionList));
        }

        Table table = viewer.getTable();
        //restore vertical position
        try {
            String topIndexStr = memento.getString(TAG_TOP_INDEX);
            table.setTopIndex(Integer.parseInt(topIndexStr));
        } catch (NumberFormatException e) {
        }
    }

    /* (non-Javadoc)
     * Method declared on IViewPart.
     */
    public void saveState(IMemento memento) {
        if (viewer == null) {
            if (this.memento != null) {
				memento.putMemento(this.memento);
			}
            return;
        }

        //save filter
        getFilter().saveState(memento.createChild(TAG_FILTER));

        //save columns width
        Table table = viewer.getTable();
        TableColumn columns[] = table.getColumns();
        //check whether it has ever been layed out
        //workaround for 1GDTU19: ITPUI:WIN2000 - Task list columns "collapsed" left
        boolean shouldSave = false;
        for (int i = 0; i < columns.length; i++) {
            if (columnLayouts[i].resizable && columns[i].getWidth() != 0) {
                shouldSave = true;
                break;
            }
        }
        if (shouldSave) {
            for (int i = 0; i < columns.length; i++) {
                if (columnLayouts[i].resizable) {
                    IMemento child = memento.createChild(TAG_COLUMN);
                    child.putInteger(TAG_NUMBER, i);
                    child.putInteger(TAG_WIDTH, columns[i].getWidth());
                }
            }
        }

        //save selection
        Object markers[] = ((IStructuredSelection) viewer.getSelection())
                .toArray();
        if (markers.length > 0) {
            IMemento selectionMem = memento.createChild(TAG_SELECTION);
            for (int i = 0; i < markers.length; i++) {
                IMemento elementMem = selectionMem.createChild(TAG_MARKER);
                IMarker marker = (IMarker) markers[i];
                elementMem.putString(TAG_RESOURCE, marker.getResource()
                        .getFullPath().toString());
                elementMem.putString(TAG_ID, String.valueOf(marker.getId()));
            }
        }

        //save vertical position
        int topIndex = table.getTopIndex();
        memento.putString(TAG_TOP_INDEX, String.valueOf(topIndex));
    }

    /**
     * Handles marker selection change in the task list by updating availability of
     * the actions in the local tool bar.
     */
    void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event
                .getSelection();
        updateStatusMessage(selection);
        updateTitle();

        updatePasteEnablement();

        // If selection is empty, then disable copy, remove and goto.	
        if (selection.isEmpty()) {
            copyTaskAction.setEnabled(false);
            removeTaskAction.setEnabled(false);
            gotoTaskAction.setEnabled(false);
            propertiesAction.setEnabled(false);
            return;
        }
        

        // Can only open properties for a single task at a time
        propertiesAction.setEnabled(selection.size() == 1);

        // Can always copy
        copyTaskAction.setEnabled(true);

        // Determine if goto should be enabled
        IMarker selectedMarker = (IMarker) selection.getFirstElement();
        boolean canJump = selection.size() == 1
                && selectedMarker.getResource().getType() == IResource.FILE;
        gotoTaskAction.setEnabled(canJump);

        // Determine if remove should be enabled
        boolean canRemove = true;
        for (Iterator markers = selection.iterator(); markers.hasNext();) {
            IMarker m = (IMarker) markers.next();
            if (!MarkerUtil.isEditable(m)) {
                canRemove = false;
                break;
            }
        }
        removeTaskAction.setEnabled(canRemove);

        // if there is an active editor on the selection's input, tell
        // the editor to goto the marker
        if (canJump) {
            IEditorPart editor = getSite().getPage().getActiveEditor();
            if (editor != null) {
                IFile file = ResourceUtil.getFile(editor.getEditorInput());
                if (file != null) {
                    if (selectedMarker.getResource().equals(file)) {
                        IDE.gotoMarker(editor, selectedMarker);
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchPart.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    /**
     * Sets the property on a marker to the given value.
     */
    void setProperty(IMarker marker, String property, Object value) {
        if (MarkerUtil.getProperty(marker, property).equals(value)) {
            return;
        }
        try {
            if (property == tableColumnProperties[1]) { // Completed
                marker.setAttribute(IMarker.DONE, value);
            } else if (property == tableColumnProperties[2]) { // Priority
                // this property is used only by cell editor, where order is High, Normal, Low
                marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH
                        - ((Integer) value).intValue());
            } else if (property == tableColumnProperties[3]) { // Description
                marker.setAttribute(IMarker.MESSAGE, value);
                // Let's not refilter too lightly - see if it is needed
                //			TaskSorter sorter = (TaskSorter) viewer.getSorter();
                //			if (sorter != null && sorter.getColumnNumber() == 3) {
                //				viewer.refresh();
                //			}
            }
        } catch (CoreException e) {
            String msg = TaskListMessages.TaskList_errorModifyingTask; 
            ErrorDialog.openError(getSite().getShell(), msg, null, e
                    .getStatus());
        }
    }

    /**
     * API method which sets the current selection of this viewer.
     *
     * @param selection a structured selection of <code>IMarker</code> objects
     * @param reveal <code>true</code> to reveal the selection, <false> otherwise
     */
    public void setSelection(ISelection selection, boolean reveal) {
        Assert.isTrue(selection instanceof IStructuredSelection);
        IStructuredSelection ssel = (IStructuredSelection) selection;

        for (Iterator i = ssel.iterator(); i.hasNext();) {
			Assert.isTrue(i.next() instanceof IMarker);
		}

        if (viewer != null) {
			viewer.setSelection(selection, reveal);
		}
    }

    boolean showChildrenHierarchy() {
        switch (getFilter().onResource) {
        case TasksFilter.ON_ANY_RESOURCE:
        case TasksFilter.ON_SELECTED_RESOURCE_AND_CHILDREN:
        case TasksFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT:
        // added by cagatayk@acm.org
        case TasksFilter.ON_WORKING_SET:
        default:
            return true;
        case TasksFilter.ON_SELECTED_RESOURCE_ONLY:
            return false;
        }
    }

    boolean showSelections() {
        switch (getFilter().onResource) {
        case TasksFilter.ON_SELECTED_RESOURCE_ONLY:
        case TasksFilter.ON_SELECTED_RESOURCE_AND_CHILDREN:
        case TasksFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT:
            // added by cagatayk@acm.org
            return true;
        case TasksFilter.ON_ANY_RESOURCE:
        case TasksFilter.ON_WORKING_SET:
        default:
            return false;
        }
    }

    // showOwnerProject() added by cagatayk@acm.org 
    boolean showOwnerProject() {
        return getFilter().onResource == TasksFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT;
    }

    /**
     * Processes state change of the 'showSelections' switch.
     * If true, it will resync with the saved input element.
     * Otherwise, it will reconfigure to show all the
     * problems/tasks in the workbench.
     * 
     * @param value the value
     */
    void toggleInputSelection(boolean value) {
        /*
         if (value) {
         handleInput(inputSelection, false);
         } else {
         // detach from input and link to the workbench object
         handleInput(WorkbenchPlugin.getPluginWorkbench(), true);
         }
         updateTitle();
         */
    }

    /**
     * If true, current input will be
     * remembered and further selections will be
     * ignored.
     * 
     * @param value the value
     */
    void toggleLockInput(boolean value) {
        /*
         if (!value) {
         handleInput(inputSelection, false);
         lockedInput = null;
         } else {
         lockedInput = (IElement) getInput();
         }
         String lockedInputPath = "";
         if (lockedInput != null && lockedInput instanceof IResource) {
         IResource resource = (IResource) lockedInput;
         lockedInputPath = resource.getFullPath().toString();
         }
         IDialogStore store = WorkbenchPlugin.getDefault().getDialogStore();
         store.put(STORE_LOCKED_INPUT, lockedInputPath);
         updateTitle();
         */
    }

    /**
     * Updates the focus resource, and refreshes if we're showing only tasks for the focus resource.
     */
    void updateFocusResource(ISelection selection) {
        ArrayList list = new ArrayList();

        if (selection instanceof IStructuredSelection) {
            Iterator iterator = ((IStructuredSelection) selection).iterator();
            while (iterator.hasNext()) {
                Object object = iterator.next();

                if (object instanceof IAdaptable) {
                    ITaskListResourceAdapter taskListResourceAdapter;
                    Object adapter = ((IAdaptable) object)
                            .getAdapter(ITaskListResourceAdapter.class);
                    if (adapter != null
                            && adapter instanceof ITaskListResourceAdapter) {
                        taskListResourceAdapter = (ITaskListResourceAdapter) adapter;
                    } else {
                        taskListResourceAdapter = DefaultTaskListResourceAdapter
                                .getDefault();
                    }

                    IResource resource = taskListResourceAdapter
                            .getAffectedResource((IAdaptable) object);
                    if (resource != null) {
                        list.add(resource);
                    }
                }
            }
        }

        if (list.size() == 0 && focusPart instanceof IEditorPart) {
            IEditorInput input = ((IEditorPart) focusPart).getEditorInput();
            if (input != null) {
                IResource resource = ResourceUtil.getResource(input);
                if (resource != null) {
                    list.add(resource);
                }
            }
        }

        int l = list.size();
        if (l < 1) {
            return; // required to achieve lazy update behavior.
        }

        IResource[] resources = (IResource[]) list.toArray(new IResource[l]);
        for (int i = 0; i < l; i++) {
            Assert.isNotNull(resources[i]);
        }

        if (!Arrays.equals(resources, focusResources)) {
            boolean updateNeeded = false;

            if (showOwnerProject()) {
                int m = focusResources == null ? 0 : focusResources.length;
                if (l != m) {
                    updateNeeded = true;
                } else {
                    for (int i = 0; i < l; i++) {
                        IProject oldProject = m < 1 ? null : focusResources[0]
                                .getProject();
                        IProject newProject = resources[0].getProject();
                        boolean projectsEqual = (oldProject == null ? newProject == null
                                : oldProject.equals(newProject));
                        if (!projectsEqual) {
                            updateNeeded = true;
                            break;
                        }
                    }
                }
            } else if (showSelections()) {
                updateNeeded = true;
            }

            // remember the focus resources even if update is not needed,
            // so that we know them if the filter settings change
            focusResources = resources;

            if (updateNeeded) {
                viewer.getControl().setRedraw(false);
                viewer.refresh();
                viewer.getControl().setRedraw(true);
                updateStatusMessage();
                updateTitle();
            }
        }
    }

    /**
     * Updates the enablement of the paste action
     */
    void updatePasteEnablement() {
        // Paste if clipboard contains tasks
        MarkerTransfer transfer = MarkerTransfer.getInstance();
        IMarker[] markerData = (IMarker[]) getClipboard().getContents(transfer);
        boolean canPaste = false;
        if (markerData != null) {
            for (int i = 0; i < markerData.length; i++) {
                if (MarkerUtil.isMarkerType(markerData[i], IMarker.TASK)) {
                    canPaste = true;
                    break;
                }
            }
        }
        pasteTaskAction.setEnabled(canPaste);
    }

    /**
     * Updates that message displayed in the status line.
     */
    void updateStatusMessage() {
        ISelection selection = viewer.getSelection();

        if (selection instanceof IStructuredSelection) {
			updateStatusMessage((IStructuredSelection) selection);
		} else {
			updateStatusMessage(null);
		}
    }

    /**
     * Updates that message displayed in the status line.
     */
    void updateStatusMessage(IStructuredSelection selection) {
        String message = getStatusMessage(selection);
        getViewSite().getActionBars().getStatusLineManager()
                .setMessage(message);
    }

    /**
     * Updates the title of the view.  Should be called when filters change.
     */
    void updateTitle() {
        TaskListContentProvider provider = (TaskListContentProvider) getTableViewer()
                .getContentProvider();
        String summary = provider.getTitleSummary();
        setContentDescription(summary);
    }

    /**
     * Method updateSortingState.
     */
    void updateSortingState() {
        int curColumn = comparator.getTopPriority();
        sortByCategoryAction.setChecked(curColumn == TaskSorter.TYPE);
        sortByCompletedAction.setChecked(curColumn == TaskSorter.COMPLETION);
        sortByPriorityAction.setChecked(curColumn == TaskSorter.PRIORITY);
        sortByDescriptionAction.setChecked(curColumn == TaskSorter.DESCRIPTION);
        sortByResourceAction.setChecked(curColumn == TaskSorter.RESOURCE);
        sortByContainerAction.setChecked(curColumn == TaskSorter.FOLDER);
        sortByLocationAction.setChecked(curColumn == TaskSorter.LOCATION);
        sortByCreationTimeAction
                .setChecked(curColumn == TaskSorter.CREATION_TIME);

        int curDirection = comparator.getTopPriorityDirection();
        sortAscendingAction.setChecked(curDirection == TaskSorter.ASCENDING);
        sortDescendingAction.setChecked(curDirection == TaskSorter.DESCENDING);
    }

}
