package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.ccvs.core.ILogEntry;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.actions.OpenRemoteFileAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;

/**
 * The history view allows browsing of an array of resource revisions
 */
public class HistoryView extends ViewPart implements IMenuListener, ISelectionListener {
	private ICVSRemoteFile remoteFile;
	
	private TableViewer viewer;
	private StyledText text;
	
	private OpenRemoteFileAction openAction;
	private IAction toggleTextAction;
	
	private SashForm sashForm;
	
	//column constants
	private static final int COL_REVISION = 0;
	private static final int COL_TAGS = 1;
	private static final int COL_DATE = 2;
	private static final int COL_AUTHOR = 3;
	private static final int COL_COMMENT = 4;

	class HistoryLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			ILogEntry entry = (ILogEntry)element;
			switch (columnIndex) {
				case COL_REVISION:
					return entry.getRevision();
				case COL_TAGS:
					String[] tags = entry.getTags();
					StringBuffer result = new StringBuffer();
					for (int i = 0; i < tags.length; i++) {
						result.append(tags[i]);
						if (i < tags.length - 1) {
							result.append(", ");
						}
					}
					return result.toString();
				case COL_DATE:
					return entry.getDate();
				case COL_AUTHOR:
					return entry.getAuthor();
				case COL_COMMENT:
					String comment = entry.getComment();
					int index = comment.indexOf("\n");
					if (index == -1) return comment;
					return comment.substring(0, index) + "[...]";
			}
			return "";
		}
	}
	
	public static final String VIEW_ID = "org.eclipse.team.ccvs.ui.HistoryView";
	
	/**
	 * Adds the action contributions for this view.
	 */
	protected void contributeActions() {
		// Double click open action
		openAction = new OpenRemoteFileAction();
		viewer.getTable().addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event e) {
				openAction.selectionChanged(null, viewer.getSelection());
				openAction.run(null);
			}
		});

		// Toggle text visible action
		final IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		toggleTextAction = new Action(Policy.bind("HistoryView.showComment")) {
			public void run() {
				if (sashForm.getMaximizedControl() != null) {
					sashForm.setMaximizedControl(null);
				} else {
					sashForm.setMaximizedControl(viewer.getControl());
				}
				store.setValue(ICVSUIConstants.PREF_SHOW_COMMENTS, toggleTextAction.isChecked());
			}
		};
		toggleTextAction.setChecked(store.getBoolean(ICVSUIConstants.PREF_SHOW_COMMENTS));
		
		// Contribute actions to popup menu
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(viewer.getTable());
		menuMgr.addMenuListener(this);
		menuMgr.setRemoveAllWhenShown(true);
		viewer.getTable().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);

		// Contribute toggle text visible to the toolbar drop-down
		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager actionBarsMenu = actionBars.getMenuManager();
		actionBarsMenu.add(toggleTextAction);
	}
	
	/**
	 * Creates the columns for the history table.
	 */
	private void createColumns(Table table, TableLayout layout) {
		SelectionListener headerListener = getColumnListener();
		// revision
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("HistoryView.revision"));
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(20, true));
	
		// tags
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("HistoryView.tags"));
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(20, true));
	
		// creation date
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("HistoryView.date"));
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(20, true));
	
		// author
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("HistoryView.author"));
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(20, true));
	
		//comment
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("HistoryView.comment"));
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(50, true));
	}
	/*
	 * Method declared on IWorkbenchPart
	 */
	public void createPartControl(Composite parent) {
		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer = createTable(sashForm);
		text = createText(sashForm);
		sashForm.setWeights(new int[] { 70, 30 });
		getSite().getPage().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		contributeActions();
		if (!CVSUIPlugin.getPlugin().getPreferenceStore().getBoolean(ICVSUIConstants.PREF_SHOW_COMMENTS)) {
			sashForm.setMaximizedControl(viewer.getControl());
		}
		// set F1 help
		//WorkbenchHelp.setHelp(viewer.getControl(), new ViewContextComputer (this, IVCMHelpContextIds.RESOURCE_HISTORY_VIEW));
		initDragAndDrop();
	}
	/**
	 * Creates the group that displays lists of the available repositories
	 * and team streams.
	 *
	 * @param the parent composite to contain the group
	 * @return the group control
	 */
	protected TableViewer createTable(Composite parent) {
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
	
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		
		createColumns(table, layout);
	
		TableViewer viewer = new TableViewer(table);
		viewer.setContentProvider(new WorkbenchContentProvider());
		viewer.setLabelProvider(new HistoryLabelProvider());
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection == null || !(selection instanceof IStructuredSelection)) {
					text.setText("");
					return;
				}
				IStructuredSelection ss = (IStructuredSelection)selection;
				if (ss.size() != 1) {
					text.setText("");
					return;
				}
				ILogEntry entry = (ILogEntry)ss.getFirstElement();
				text.setText(entry.getComment());
			}
		});
		
		return viewer;
	}
	protected StyledText createText(Composite parent) {
		StyledText text = new StyledText(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_BOTH);
		text.setLayoutData(data);
		return text;
	}
	public void dispose() {
		getSite().getPage().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
	}	
	/**
	 * Adds the listener that sets the sorter.
	 */
	private SelectionListener getColumnListener() {
		/**
	 	 * This class handles selections of the column headers.
		 * Selection of the column header will cause resorting
		 * of the shown tasks using that column's sorter.
		 * Repeated selection of the header will toggle
		 * sorting order (ascending versus descending).
		 */
		return new SelectionAdapter() {
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
				int column = viewer.getTable().indexOf((TableColumn) e.widget);
				HistorySorter oldSorter = (HistorySorter)viewer.getSorter();
				if (oldSorter != null && column == oldSorter.getColumnNumber()) {
					oldSorter.setReversed(!oldSorter.isReversed());
					viewer.refresh();
				} else {
					viewer.setSorter(new HistorySorter(column));
				}
			}
		};
	}
	/**
	 * Returns the table viewer contained in this view.
	 */
	protected TableViewer getViewer() {
		return viewer;
	}
	/**
	 * Adds drag and drop support to the history view.
	 */
	void initDragAndDrop() {
		int ops = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		Transfer[] transfers = new Transfer[] {ResourceTransfer.getInstance()};
		viewer.addDropSupport(ops, transfers, new HistoryDropAdapter(viewer, this));
	}
	/**
	 * @see IMenuListener#menuAboutToShow
	 */
	public void menuAboutToShow(IMenuManager manager) {
		// file actions go first (view file)
		manager.add(new Separator(IWorkbenchActionConstants.GROUP_FILE));
	
		manager.add(new Separator("additions"));
		manager.add(new Separator("additions-end"));
	}
	/**
	 * Makes the history view visible in the active perspective. If there
	 * isn't a history view registered <code>null</code> is returned.
	 * Otherwise the opened view part is returned.
	 */
	public static HistoryView openInActivePerspective() {
		try {
			return (HistoryView)CVSUIPlugin.getActivePage().showView(VIEW_ID);
		} catch (PartInitException pe) {
			return null;
		}
	}
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		/*if (CVSUIPlugin.getDefault().getPreferenceStore().getBoolean(ICVSUIConstants.PREF_HISTORY_TRACKS_SELECTION)) {
			if (selection == null) return;
			if (!(selection instanceof IStructuredSelection)) return;
			IStructuredSelection ss = (IStructuredSelection)selection;
			if (ss.size() != 1) {
				showHistory(null);
				return;
			}
			Object first = ss.getFirstElement();
			try {
				IVersionHistory history = getHistory(first);
				showHistory(history);
			} catch (CoreException e) {
				showHistory(null);
			}
		}*/
	}
	/** (Non-javadoc)
	 * Method declared on IWorkbenchPart
	 */
	public void setFocus() {
		if (viewer != null) {
			Table control = viewer.getTable();
			if (control != null && !control.isDisposed()) {
				control.setFocus();
			}
		}
	}
	/**
	 * Shows the given log entries in the view.
	 */
	public void showHistory(ICVSRemoteFile file) {
		this.remoteFile = file;
		if (remoteFile == null) {
			setTitle(Policy.bind("HistoryView.title"));
			viewer.setInput(new AdaptableList());
			return;
		}
		ILogEntry[] entries = null;
		try {
			entries = file.getLogEntries(new NullProgressMonitor());
		} catch (TeamException e) {
			CVSUIPlugin.log(e.getStatus());
			setTitle(Policy.bind("HistoryView.title"));
			viewer.setInput(new AdaptableList());
			return;
		}
		if (entries.length > 0) {
			setTitle(Policy.bind("HistoryView.titleWithArgument", file.getName()));
		} else {
			setTitle(Policy.bind("HistoryView.title"));
		}
		viewer.setInput(new AdaptableList(entries));
		if (viewer.getSorter() == null) {
			// By default, reverse sort by creation date.
			HistorySorter sorter = new HistorySorter(COL_DATE);
			sorter.setReversed(true);
			viewer.setSorter(sorter);
		}
	}
}
