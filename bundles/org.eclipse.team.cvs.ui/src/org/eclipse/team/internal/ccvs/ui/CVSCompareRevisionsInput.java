package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffContainer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ILogEntry;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class CVSCompareRevisionsInput extends CompareEditorInput {
	IFile resource;
	ICVSRemoteFile currentEdition;
	ILogEntry[] editions;
	TableViewer viewer;
	Action loadAction;
	Shell shell;
	
	/**
	 * This class is an edition node which knows the log entry it came from.
	 */
	class ResourceRevisionNode extends ResourceEditionNode {	
		ILogEntry entry;
		public ResourceRevisionNode(ILogEntry entry) {
			super(entry.getRemoteFile());
			this.entry = entry;
		}
		public ILogEntry getLogEntry() {
			return entry;
		}
		public String getName() {
			ICVSRemoteResource edition = getRemoteResource();
			String revisionName = entry.getRevision();
			if (revisionName != null) {
				IResource resource = CVSCompareRevisionsInput.this.resource;
				try {
					ICVSRemoteFile currentEdition = (ICVSRemoteFile) CVSWorkspaceRoot.getRemoteResourceFor(resource);
					if (currentEdition != null && currentEdition.getRevision().equals(revisionName)) {
						return "*" + revisionName;
					} else {
						return revisionName;
					}
				} catch (TeamException e) {
					handle(e);
				}
			}
			return super.getName();
		}
	};
	/**
	 * A compare node that gets its label from the right element
	 */
	class VersionCompareDiffNode extends DiffNode {
		public VersionCompareDiffNode(ITypedElement left, ITypedElement right) {
			super(left, right);
		}
		public String getName() {
			return getRight().getName();
		}
	};
	/**
	 * A content provider which knows how to get the children of the diff container
	 */
	class VersionCompareContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof DiffContainer) {
				return ((DiffContainer)inputElement).getChildren();
			}
			return null;
		}
	};
	/**
	 * A sorter which gets the remote resources from the diff nodes
	 */
	class VersionSorter extends HistorySorter {
		public VersionSorter(int columnNumber) {
			super(columnNumber);
		}
		public int compare(Viewer viewer, Object o1, Object o2) {
			VersionCompareDiffNode d1 = (VersionCompareDiffNode)o1;
			VersionCompareDiffNode d2 = (VersionCompareDiffNode)o2;
			return super.compare(viewer, ((ResourceRevisionNode)d1.getRight()).getRemoteResource(), ((ResourceRevisionNode)d2.getRight()).getRemoteResource());
		}
		
	};
	
	//column constants
	private static final int COL_REVISION = 0;
	private static final int COL_TAGS = 1;
	private static final int COL_DATE = 2;
	private static final int COL_AUTHOR = 3;
	private static final int COL_COMMENT = 4;

	/**
	 * A history label provider, largely copied from HistoryView.
	 */
	class HistoryLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			if (!(element instanceof DiffNode)) return "";
			ITypedElement right = ((DiffNode)element).getRight();
			if (!(right instanceof ResourceRevisionNode)) return "";
			ILogEntry entry = ((ResourceRevisionNode)right).getLogEntry();
			switch (columnIndex) {
				case COL_REVISION:
					try {
						StringBuffer revisionName = new StringBuffer();
						if (currentEdition != null && currentEdition.getRevision().equals(entry.getRevision())) {
							revisionName.append("*");
						}
						revisionName.append(entry.getRevision());
						return revisionName.toString();
					} catch (TeamException e) {
						handle(e);
					}
					return entry.getRevision();
				case COL_TAGS:
					CVSTag[] tags = entry.getTags();
					StringBuffer result = new StringBuffer();
					for (int i = 0; i < tags.length; i++) {
						result.append(tags[i].getName());
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
	
	public CVSCompareRevisionsInput(IFile resource, ILogEntry[] editions) {
		super(new CompareConfiguration());
		this.resource = resource;
		this.editions = editions;
		updateCurrentEdition();
		initializeActions();
	}
	/**
	 * Creates the columns for the history table.
	 * Copied from HistoryView.
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
	public Viewer createDiffViewer(Composite parent) {
		this.shell = parent.getShell();
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		table.setData(CompareUI.COMPARE_VIEWER_TITLE, Policy.bind("CVSCompareRevisionsInput.structureCompare"));
	
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		
		createColumns(table, layout);
	
		viewer = new TableViewer(table);
		viewer.setContentProvider(new VersionCompareContentProvider());
		viewer.setLabelProvider(new HistoryLabelProvider());

		MenuManager mm = new MenuManager();
		mm.setRemoveAllWhenShown(true);
		mm.addMenuListener(
			new IMenuListener() {
				public void menuAboutToShow(IMenuManager mm) {
					mm.add(loadAction);
				}
			}
		);
		table.setMenu(mm.createContextMenu(table));
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (!(selection instanceof IStructuredSelection)) {
					loadAction.setEnabled(false);
					return;
				}
				IStructuredSelection ss = (IStructuredSelection)selection;
				loadAction.setEnabled(ss.size() == 1);
			}	
		});
		return viewer;
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
				VersionSorter oldSorter = (VersionSorter)viewer.getSorter();
				if (oldSorter != null && column == oldSorter.getColumnNumber()) {
					oldSorter.setReversed(!oldSorter.isReversed());
					viewer.refresh();
				} else {
					viewer.setSorter(new VersionSorter(column));
				}
			}
		};
	}
	private void initLabels() {
		CompareConfiguration cc = (CompareConfiguration)getCompareConfiguration();
		String resourceName = resource.getName();	
//		if (editions[0].isTeamStreamResource()) {
//			setTitle(Policy.bind("CompareResourceEditorInput.compareResourceAndStream", new Object[] {resourceName, editions[0].getTeamStream().getName()}));
//		} else {
//			setTitle(Policy.bind("CompareResourceEditorInput.compareResourceAndVersions", new Object[] {resourceName}));
//		}
		setTitle(Policy.bind("CVSCompareRevisionsInput.compareResourceAndVersions", new Object[] {resourceName}));
		cc.setLeftEditable(true);
		cc.setRightEditable(false);
		
		String leftLabel = Policy.bind("CVSCompareRevisionsInput.workspace", new Object[] {resourceName});
		cc.setLeftLabel(leftLabel);
		String rightLabel = Policy.bind("CVSCompareRevisionsInput.repository", new Object[] {resourceName});
		cc.setRightLabel(rightLabel);
	}
	private void initializeActions() {
		loadAction = new Action(Policy.bind("CVSCompareRevisionsInput.addToWorkspace"), null) {
			public void run() {
				try {
					new ProgressMonitorDialog(shell).run(false, true, new WorkspaceModifyOperation() {
						protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
							if (selection.size() != 1) return;
							VersionCompareDiffNode node = (VersionCompareDiffNode)selection.getFirstElement();
							ResourceEditionNode right = (ResourceEditionNode)node.getRight();
							ICVSRemoteResource edition = right.getRemoteResource();
							// Do the load. This just consists of setting the local contents. We don't
							// actually want to change the base.
							try {
								monitor.beginTask(null, 100);
								InputStream in = edition.getContents(new SubProgressMonitor(monitor, 50));
								resource.setContents(in, false, true, new SubProgressMonitor(monitor, 50));
							} catch (TeamException e) {
								throw new InvocationTargetException(e);
							} catch (CoreException e) {
								throw new InvocationTargetException(e);
							} finally {
								monitor.done();
							}
						}
					});
				} catch (InterruptedException e) {
					// Do nothing
					return;
				} catch (InvocationTargetException e) {
					handle(e);
				}
				// recompute the labels on the viewer
				updateCurrentEdition();
				viewer.refresh();
			}
		};
		// set F1 help
//		WorkbenchHelp.setHelp(loadAction, new Object[] {IVCMHelpContextIds.CATCHUPRELEASE_CATCHUP_ACTION});
	}
	public boolean isSaveNeeded() {
		return false;
	}
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		initLabels();
		DiffNode diffRoot = new DiffNode(Differencer.NO_CHANGE);
		for (int i = 0; i < editions.length; i++) {		
			ITypedElement left = new ResourceNode(resource);
			ITypedElement right = new ResourceRevisionNode(editions[i]);
			diffRoot.add(new VersionCompareDiffNode(left, right));
		}
		return diffRoot;		
	}
	private void updateCurrentEdition() {
		try {
			this.currentEdition = ((ICVSRemoteFile) CVSWorkspaceRoot.getRemoteResourceFor(resource));
		} catch (TeamException e) {
			handle(e);
		}
	}
	private void handle(Exception e) {
		// create a status
		Throwable t = e;
		// unwrap the invocation target exception
		if (t instanceof InvocationTargetException) {
			t = ((InvocationTargetException)t).getTargetException();
		}
		IStatus error;
		if (t instanceof CoreException) {
			error = ((CoreException)t).getStatus();
		} else if (t instanceof TeamException) {
			error = ((TeamException)t).getStatus();
		} else {
			error = new Status(IStatus.ERROR, CVSUIPlugin.ID, 1, Policy.bind("internal"), t);
		}
		setMessage(error.getMessage());
		ErrorDialog.openError(shell, null, null, error);
		if (!(t instanceof TeamException)) {
			CVSUIPlugin.log(error);
		}
	}
}