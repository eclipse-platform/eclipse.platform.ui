package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
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
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSCompareRevisionsInput.HistoryLabelProvider;
import org.eclipse.team.internal.ccvs.ui.actions.CVSAction;
import org.eclipse.team.internal.ccvs.ui.actions.OpenLogEntryAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * The history view allows browsing of an array of resource revisions
 */
public class HistoryView extends ViewPart implements ISelectionListener {
	private IFile file;
	// cached for efficiency
	private ILogEntry[] entries;
	private CVSTeamProvider provider;
	
	private TableViewer tableViewer;
	private TextViewer textViewer;
	private TableViewer tagViewer;
	
	private OpenLogEntryAction openAction;
	private IAction toggleTextAction;
	private IAction toggleListAction;
	private TextViewerAction copyAction;
	private TextViewerAction selectAllAction;
	private Action getContentsAction;
	private Action getRevisionAction;
	
	private SashForm sashForm;
	private SashForm innerSashForm;
	
	//column constants
	private static final int COL_REVISION = 0;
	private static final int COL_TAGS = 1;
	private static final int COL_DATE = 2;
	private static final int COL_AUTHOR = 3;
	private static final int COL_COMMENT = 4;

	private Image branchImage;
	private Image versionImage;
	
	private ILogEntry currentSelection;
	private String currentRevision;
	
	class HistoryLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			ILogEntry entry = (ILogEntry)element;
			switch (columnIndex) {
				case COL_REVISION:
					String revision = entry.getRevision();
					if (file == null) return revision;
					if (currentRevision != null && currentRevision.equals(revision)) {
						revision = Policy.bind("currentRevision", revision); //$NON-NLS-1$
					}
					return revision;
				case COL_TAGS:
					CVSTag[] tags = entry.getTags();
					StringBuffer result = new StringBuffer();
					for (int i = 0; i < tags.length; i++) {
						result.append(tags[i].getName());
						if (i < tags.length - 1) {
							result.append(", "); //$NON-NLS-1$
						}
					}
					return result.toString();
				case COL_DATE:
					Date date = entry.getDate();
					if (date == null) return Policy.bind("notAvailable"); //$NON-NLS-1$
					return DateFormat.getInstance().format(date);
				case COL_AUTHOR:
					return entry.getAuthor();
				case COL_COMMENT:
					String comment = entry.getComment();
					int index = comment.indexOf("\n"); //$NON-NLS-1$
					switch (index) {
						case -1:
							return comment;
						case 0:
							return Policy.bind("HistoryView.[...]_4"); //$NON-NLS-1$
						default:
							return Policy.bind("CVSCompareRevisionsInput.truncate", comment.substring(0, index)); //$NON-NLS-1$
					}
			}
			return ""; //$NON-NLS-1$
		}
	}
	
	public static final String VIEW_ID = "org.eclipse.team.ccvs.ui.HistoryView"; //$NON-NLS-1$
	
	/**
	 * Adds the action contributions for this view.
	 */
	protected void contributeActions() {
		// Refresh (toolbar)
		final Action refreshAction = new Action(Policy.bind("HistoryView.refresh"), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_REFRESH)) { //$NON-NLS-1$
			public void run() {
				BusyIndicator.showWhile(tableViewer.getTable().getDisplay(), new Runnable() {
					public void run() {
						tableViewer.refresh();
					}
				});
			}
		};
		refreshAction.setToolTipText(Policy.bind("HistoryView.refresh")); //$NON-NLS-1$
		
		// Double click open action
		openAction = new OpenLogEntryAction();
		tableViewer.getTable().addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				openAction.selectionChanged(null, tableViewer.getSelection());
				openAction.run(null);
			}
		});

		getContentsAction = getContextMenuAction(Policy.bind("HistoryView.getContentsAction"), new IWorkspaceRunnable() { //$NON-NLS-1$
			public void run(IProgressMonitor monitor) throws CoreException {
				ICVSRemoteFile remoteFile = currentSelection.getRemoteFile();
				monitor.beginTask(null, 100);
				try {
					if(confirmOverwrite()) {
						InputStream in = remoteFile.getContents(new SubProgressMonitor(monitor, 50));
						file.setContents(in, false, true, new SubProgressMonitor(monitor, 50));				
					}
				} catch (TeamException e) {
					throw new CoreException(e.getStatus());
				} finally {
					monitor.done();
				}
			}
		});
		

		getRevisionAction = getContextMenuAction(Policy.bind("HistoryView.getRevisionAction"), new IWorkspaceRunnable() { //$NON-NLS-1$
			public void run(IProgressMonitor monitor) throws CoreException {
				ICVSRemoteFile remoteFile = currentSelection.getRemoteFile();
				try {
					if(confirmOverwrite()) {
						CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(file.getProject());
						CVSTag revisionTag = new CVSTag(remoteFile.getRevision(), CVSTag.VERSION);
						
						if(CVSAction.checkForMixingTags(getSite().getShell(), new IResource[] {file}, revisionTag)) {							
							provider.update(new IResource[] {file}, new Command.LocalOption[] {Command.UPDATE.IGNORE_LOCAL_CHANGES}, 
													   revisionTag, true /*create backups*/, monitor);
							currentRevision = revisionTag.getName();
							tableViewer.refresh();
						}
					}
				} catch (TeamException e) {
					throw new CoreException(e.getStatus());
				}
			}
		});
		
		// Toggle text visible action
		final IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		toggleTextAction = new Action(Policy.bind("HistoryView.showComment")) { //$NON-NLS-1$
			public void run() {
				setViewerVisibility();
				store.setValue(ICVSUIConstants.PREF_SHOW_COMMENTS, toggleTextAction.isChecked());
			}
		};
		toggleTextAction.setChecked(store.getBoolean(ICVSUIConstants.PREF_SHOW_COMMENTS));
		// Toggle list visible action
		toggleListAction = new Action(Policy.bind("HistoryView.showTags")) { //$NON-NLS-1$
			public void run() {
				setViewerVisibility();
				store.setValue(ICVSUIConstants.PREF_SHOW_TAGS, toggleListAction.isChecked());
			}
		};
		toggleListAction.setChecked(store.getBoolean(ICVSUIConstants.PREF_SHOW_TAGS));
		
		// Contribute actions to popup menu
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(tableViewer.getTable());
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuMgr) {
				fillTableMenu(menuMgr);
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		tableViewer.getTable().setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);

		// Contribute toggle text visible to the toolbar drop-down
		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager actionBarsMenu = actionBars.getMenuManager();
		actionBarsMenu.add(toggleTextAction);
		actionBarsMenu.add(toggleListAction);

		// Create the local tool bar
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
		tbm.add(refreshAction);
		tbm.update(false);
	
		// Create actions for the text editor
		copyAction = new TextViewerAction(textViewer, ITextOperationTarget.COPY);
		copyAction.setText(Policy.bind("HistoryView.copy")); //$NON-NLS-1$
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.COPY, copyAction);
		
		selectAllAction = new TextViewerAction(textViewer, ITextOperationTarget.SELECT_ALL);
		selectAllAction.setText(Policy.bind("HistoryView.selectAll")); //$NON-NLS-1$
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.SELECT_ALL, selectAllAction);

		actionBars.updateActionBars();

		menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuMgr) {
				fillTextMenu(menuMgr);
			}
		});
		StyledText text = textViewer.getTextWidget();
		menu = menuMgr.createContextMenu(text);
		text.setMenu(menu);
	}
	private void setViewerVisibility() {
		boolean showText = toggleTextAction.isChecked();
		boolean showList = toggleListAction.isChecked();
		if (showText && showList) {
			sashForm.setMaximizedControl(null);
			innerSashForm.setMaximizedControl(null);
		} else if (showText) {
			sashForm.setMaximizedControl(null);
			innerSashForm.setMaximizedControl(textViewer.getTextWidget());
		} else if (showList) {
			sashForm.setMaximizedControl(null);
			innerSashForm.setMaximizedControl(tagViewer.getTable());
		} else {
			sashForm.setMaximizedControl(tableViewer.getControl());
		}
	}
	/**
	 * Creates the columns for the history table.
	 */
	private void createColumns(Table table, TableLayout layout) {
		SelectionListener headerListener = getColumnListener();
		// revision
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("HistoryView.revision")); //$NON-NLS-1$
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(20, true));
	
		// tags
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("HistoryView.tags")); //$NON-NLS-1$
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(20, true));
	
		// creation date
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("HistoryView.date")); //$NON-NLS-1$
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(20, true));
	
		// author
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("HistoryView.author")); //$NON-NLS-1$
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(20, true));
	
		//comment
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("HistoryView.comment")); //$NON-NLS-1$
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(50, true));
	}
	/*
	 * Method declared on IWorkbenchPart
	 */
	public void createPartControl(Composite parent) {
		initializeImages();
		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer = createTable(sashForm);
		innerSashForm = new SashForm(sashForm, SWT.HORIZONTAL);
		tagViewer = createTagTable(innerSashForm);
		textViewer = createText(innerSashForm);
		sashForm.setWeights(new int[] { 70, 30 });
		innerSashForm.setWeights(new int[] { 50, 50 });
		getSite().getPage().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		contributeActions();
		setViewerVisibility();
		// set F1 help
		//WorkbenchHelp.setHelp(viewer.getControl(), new ViewContextComputer (this, IVCMHelpContextIds.RESOURCE_HISTORY_VIEW));
		initDragAndDrop();
	}
	private void initializeImages() {
		CVSUIPlugin plugin = CVSUIPlugin.getPlugin();
		versionImage = plugin.getImageDescriptor(ICVSUIConstants.IMG_PROJECT_VERSION).createImage();
		branchImage = plugin.getImageDescriptor(ICVSUIConstants.IMG_TAG).createImage();
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
		viewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				// Short-circuit to optimize
				if (entries != null) return entries;
				
				if (!(inputElement instanceof ICVSRemoteFile)) return null;
				final ICVSRemoteFile remoteFile = (ICVSRemoteFile)inputElement;
				final Object[][] result = new Object[1][];
				try {
					CVSUIPlugin.runWithProgress(getViewer().getTable().getShell(), true /*cancelable*/,
						new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try {
								entries = remoteFile.getLogEntries(monitor);
								result[0] = entries;
							} catch (TeamException e) {
								throw new InvocationTargetException(e);
							}
						}
					});
				} catch (InterruptedException e) { // ignore cancellation
					result[0] = new Object[0];
				} catch (InvocationTargetException e) {
					Throwable t = e.getTargetException();
					if (t instanceof TeamException) {
						ErrorDialog.openError(getViewSite().getShell(), null, null, ((TeamException) t).getStatus());
					}
					result[0] = new Object[0];
				}
				return result[0];				
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				entries = null;
			}
		});
		viewer.setLabelProvider(new HistoryLabelProvider());
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection == null || !(selection instanceof IStructuredSelection)) {
					textViewer.setDocument(new Document("")); //$NON-NLS-1$
					tagViewer.setInput(null);
					return;
				}
				IStructuredSelection ss = (IStructuredSelection)selection;
				if (ss.size() != 1) {
					textViewer.setDocument(new Document("")); //$NON-NLS-1$
					tagViewer.setInput(null);
					return;
				}
				ILogEntry entry = (ILogEntry)ss.getFirstElement();
				textViewer.setDocument(new Document(entry.getComment()));
				tagViewer.setInput(entry.getTags());
			}
		});
		
		// By default, reverse sort by revision.
		HistorySorter sorter = new HistorySorter(COL_REVISION);
		sorter.setReversed(true);
		viewer.setSorter(sorter);
		
		return viewer;
	}
	private TableViewer createTagTable(Composite parent) {
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		TableViewer result = new TableViewer(table);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(100));
		table.setLayout(layout);
		result.setContentProvider(new SimpleContentProvider() {
			public Object[] getElements(Object inputElement) {
				if (inputElement == null) return new Object[0];
				CVSTag[] tags = (CVSTag[])inputElement;
				return tags;
			}
		});
		result.setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				if (element == null) return null;
				CVSTag tag = (CVSTag)element;
				switch (tag.getType()) {
					case CVSTag.BRANCH:
					case CVSTag.HEAD:
						return branchImage;
					case CVSTag.VERSION:
						return versionImage;
				}
				return null;
			}
			public String getText(Object element) {
				return ((CVSTag)element).getName();
			}
		});
		result.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (!(e1 instanceof CVSTag) || !(e2 instanceof CVSTag)) return super.compare(viewer, e1, e2);
				CVSTag tag1 = (CVSTag)e1;
				CVSTag tag2 = (CVSTag)e2;
				int type1 = tag1.getType();
				int type2 = tag2.getType();
				if (type1 != type2) {
					return type2 - type1;
				}
				return super.compare(viewer, tag1, tag2);
			}
		});
		return result;
	}
	protected TextViewer createText(Composite parent) {
		TextViewer result = new TextViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
		result.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				copyAction.update();
			}
		});
		return result;
	}
	public void dispose() {
		getSite().getPage().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		if (branchImage != null) {
			branchImage.dispose();
			branchImage = null;
		}
		if (versionImage != null) {
			versionImage.dispose();
			versionImage = null;
		}
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
				int column = tableViewer.getTable().indexOf((TableColumn) e.widget);
				HistorySorter oldSorter = (HistorySorter)tableViewer.getSorter();
				if (oldSorter != null && column == oldSorter.getColumnNumber()) {
					oldSorter.setReversed(!oldSorter.isReversed());
					tableViewer.refresh();
				} else {
					tableViewer.setSorter(new HistorySorter(column));
				}
			}
		};
	}
	/**
	 * Returns the table viewer contained in this view.
	 */
	protected TableViewer getViewer() {
		return tableViewer;
	}
	/**
	 * Adds drag and drop support to the history view.
	 */
	void initDragAndDrop() {
		int ops = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		Transfer[] transfers = new Transfer[] {ResourceTransfer.getInstance()};
		tableViewer.addDropSupport(ops, transfers, new HistoryDropAdapter(tableViewer, this));
	}
	private void fillTableMenu(IMenuManager manager) {
		if (tableViewer.getInput() == null) return;
		// file actions go first (view file)
		manager.add(new Separator(IWorkbenchActionConstants.GROUP_FILE));
		if (file != null) {
			// Add the "Add to Workspace" action if 1 revision is selected.
			ISelection sel = tableViewer.getSelection();
			if (!sel.isEmpty()) {
				if (sel instanceof IStructuredSelection) {
					if (((IStructuredSelection)sel).size() == 1) {
						manager.add(getContentsAction);
						manager.add(getRevisionAction);
					}
				}
			}
		}
		manager.add(new Separator("additions")); //$NON-NLS-1$
		manager.add(new Separator("additions-end")); //$NON-NLS-1$
	}
	private void fillTextMenu(IMenuManager manager) {
		manager.add(copyAction);
		manager.add(selectAllAction);
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
		if (CVSUIPlugin.getPlugin().getPreferenceStore().getBoolean(ICVSUIConstants.PREF_HISTORY_TRACKS_SELECTION)) {
			if (selection == null) return;
			if (!(selection instanceof IStructuredSelection)) return;
			IStructuredSelection ss = (IStructuredSelection)selection;
			if (ss.size() != 1) {
				showHistory((IResource)null);
				return;
			}
			Object first = ss.getFirstElement();
			if (first instanceof IResource) {
				showHistory((IResource)first);
			} else if (first instanceof ICVSRemoteFile) {
				showHistory((ICVSRemoteFile)first, null /* no current revision */);
			}
		}
	}
	/** (Non-javadoc)
	 * Method declared on IWorkbenchPart
	 */
	public void setFocus() {
		if (tableViewer != null) {
			Table control = tableViewer.getTable();
			if (control != null && !control.isDisposed()) {
				control.setFocus();
			}
		}
	}
	
	/**
	 * Shows the history for the given IResource in the view.
	 * 
	 * Only files are supported for now.
	 */
	public void showHistory(IResource resource) {
		if (resource instanceof IFile) {
			IFile file = (IFile)resource;
			this.file = file;
			RepositoryProvider teamProvider = RepositoryProvider.getProvider(file.getProject(), CVSProviderPlugin.getTypeId());
			if (teamProvider != null) {
				this.provider = (CVSTeamProvider)teamProvider;
				try {
					ICVSRemoteFile remoteFile = (ICVSRemoteFile)CVSWorkspaceRoot.getRemoteResourceFor(file);
					currentRevision = remoteFile.getRevision();
					tableViewer.setInput(remoteFile);
					setTitle(Policy.bind("HistoryView.titleWithArgument", remoteFile.getName())); //$NON-NLS-1$
				} catch (TeamException e) {
					ErrorDialog.openError(getViewSite().getShell(), null, null, e.getStatus());
				}				
			}
			return;
		}
		this.file = null;
		tableViewer.setInput(null);
		setTitle(Policy.bind("HistoryView.title")); //$NON-NLS-1$
	}
	
	/**
	 * Shows the history for the given ICVSRemoteFile in the view.
	 */
	public void showHistory(ICVSRemoteFile file, String currentRevision) {
		if (file == null) {
			tableViewer.setInput(null);
			setTitle(Policy.bind("HistoryView.title")); //$NON-NLS-1$
			return;
		}
		this.currentRevision = currentRevision;
		this.file = null;
		tableViewer.setInput(file);
		setTitle(Policy.bind("HistoryView.titleWithArgument", file.getName())); //$NON-NLS-1$
	}
	
	private Action getContextMenuAction(String title, final IWorkspaceRunnable action) {
			return new Action(title) {
			public void run() {
				try {
					if (file == null) return;
					ISelection selection = tableViewer.getSelection();
					if (!(selection instanceof IStructuredSelection)) return;
					IStructuredSelection ss = (IStructuredSelection)selection;
					Object o = ss.getFirstElement();
					currentSelection = (ILogEntry)o;
					new ProgressMonitorDialog(getViewSite().getShell()).run(false, true, new WorkspaceModifyOperation() {
						protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try {				
								action.run(monitor);
							} catch (CoreException e) {
								throw new InvocationTargetException(e);
							}
						}
					});
				} catch (InvocationTargetException e) {
					Throwable t = e.getTargetException();
					if (t instanceof TeamException) {
						ErrorDialog.openError(getViewSite().getShell(), null, null, ((TeamException)t).getStatus());
					} else if (t instanceof CoreException) {
						IStatus status = ((CoreException)t).getStatus();
						ErrorDialog.openError(getViewSite().getShell(), null, null, status);
						CVSUIPlugin.log(status);
					} else {
						// To do
					}
				} catch (InterruptedException e) {
					// Do nothing
				}
			}
			
			public boolean isEnabled() {
				ISelection selection = tableViewer.getSelection();
				if (!(selection instanceof IStructuredSelection)) return false;
				IStructuredSelection ss = (IStructuredSelection)selection;
				if(ss.size() != 1) return false;
				return true;
			}
		};
	}
	
	private boolean confirmOverwrite() {
		if (file!=null && file.exists()) {
			ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor(file);
			try {
				if(cvsFile.isModified()) {
					String title = Policy.bind("HistoryView.overwriteTitle"); //$NON-NLS-1$
					String msg = Policy.bind("HistoryView.overwriteMsg"); //$NON-NLS-1$
					final MessageDialog dialog = new MessageDialog(getViewSite().getShell(), title, null, msg, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
					final int[] result = new int[1];
					getViewSite().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						result[0] = dialog.open();
					}});
					if (result[0] != 0) {
						// cancel
						return false;
					}
				}
			} catch(CVSException e) {
				CVSUIPlugin.log(e.getStatus());
			}
		}
		return true;
	}
}