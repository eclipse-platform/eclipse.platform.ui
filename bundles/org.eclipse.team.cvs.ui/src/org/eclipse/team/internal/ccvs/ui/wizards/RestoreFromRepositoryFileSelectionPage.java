/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.wizards;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareViewerPane;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.Splitter;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.ui.AdaptableHierarchicalResourceList;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.HistoryTableProvider;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * Select the files to restore
 */
public class RestoreFromRepositoryFileSelectionPage extends CVSWizardPage {
	private TreeViewer fileTree;
	private CompareViewerPane fileSelectionPane;
	private CompareViewerPane revisionSelectionPane;
	private CheckboxTableViewer revisionsTable;
	private CompareViewerSwitchingPane fileContentPane;
	
	private HistoryTableProvider historyTableProvider;
	private AdaptableHierarchicalResourceList treeInput = new AdaptableHierarchicalResourceList(WorkbenchPlugin.getPluginWorkspace().getRoot(), new IResource[0]);
	
	private IContainer folder;
	private IFile selectedFile;
	private ILogEntry selectedRevision;
	private Map entriesCache = new HashMap();
	private Map filesToRestore = new HashMap();

	private static final int WIZARD_WIDTH = 550;
	
	class HistoryInput implements ITypedElement, IStreamContentAccessor, IModificationDate {
		IFile file;
		ILogEntry logEntry;
		
		HistoryInput(IFile file, ILogEntry logEntry) {
			this.file= file;
			this.logEntry = logEntry;
		}
		public InputStream getContents() throws CoreException {
			return getContentsFromLogEntry(logEntry);
		}
		public String getName() {
			return file.getName();
		}
		public String getType() {
			return file.getFileExtension();
		}
		public Image getImage() {
			return CompareUI.getImage(file);
		}
		public long getModificationDate() {
			return logEntry.getDate().getTime();
		}
	}
	
	/**
	 * Constructor for RestoreFromRepositoryFileSelectionPage.
	 * @param pageName
	 * @param title
	 * @param titleImage
	 * @param description
	 */
	public RestoreFromRepositoryFileSelectionPage(
		String pageName,
		String title,
		ImageDescriptor titleImage,
		String description) {
		super(pageName, title, titleImage, description);
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite= createComposite(parent, 1);
		setControl(composite);
		
		WorkbenchHelp.setHelp(composite, IHelpContextIds.RESTORE_FROM_REPOSITORY_FILE_SELECTION_PAGE);
		
		// Top and bottom panes: top is the two selection panes, bottom is the file content viewer
		Splitter vsplitter= new Splitter(composite,  SWT.VERTICAL);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL
					| GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL);
		// Set the width to be extra wide to accomodate the two selection lists
		data.widthHint = WIZARD_WIDTH;
		vsplitter.setLayoutData(data);
		
		// Top left and top right panes: the left for the files, the right for the log entries
		Splitter hsplitter= new Splitter(vsplitter,  SWT.HORIZONTAL);

		// Top left: file selection pane
		fileSelectionPane = new CompareViewerPane(hsplitter, SWT.BORDER | SWT.FLAT);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		fileSelectionPane.setLayoutData(data);
		fileTree = createFileSelectionTree(fileSelectionPane);
		
		// Top right: Revision selection pane
		revisionSelectionPane = new CompareViewerPane(hsplitter, SWT.BORDER | SWT.FLAT);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);;
		revisionSelectionPane.setLayoutData(data);
		historyTableProvider = new HistoryTableProvider();
		revisionsTable = createRevisionSelectionTable(revisionSelectionPane, historyTableProvider);
		revisionSelectionPane.setText(Policy.bind("RestoreFromRepositoryFileSelectionPage.emptyRevisionPane")); //$NON-NLS-1$
		
		// Bottom: File content viewer
		fileContentPane = new CompareViewerSwitchingPane(vsplitter, SWT.BORDER | SWT.FLAT) {
			protected Viewer getViewer(Viewer oldViewer, Object input) {
				return CompareUIPlugin.findContentViewer(oldViewer, input, this, null);	
			}
		};
						
		initializeValues();
		updateWidgetEnablements();
	}

	protected CheckboxTableViewer createRevisionSelectionTable(CompareViewerPane composite, HistoryTableProvider tableProvider) {
		CheckboxTableViewer table = tableProvider.createCheckBoxTable(composite);
		table.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				ILogEntry[] entries = getSelectedEntries();
				if (entries != null) return entries;
				return new Object[0];		
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		table.setInput(this);
		table.getTable().addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// Handle check selection in the check state listener
					if (e.detail == SWT.CHECK) return;
					handleRevisionSelection(e.item);
				}
			}
		);
		table.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				handleRevisionChecked(event);
			}
		});
		composite.setContent(table.getControl());
		return table;
	}
	
	protected TreeViewer createFileSelectionTree(CompareViewerPane composite) {
		TreeViewer tree = new TreeViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		tree.setUseHashlookup(true);
		tree.setContentProvider(treeInput.getTreeContentProvider());
		tree.setLabelProvider(
			new DecoratingLabelProvider(
				new WorkbenchLabelProvider() {
					protected String decorateText(String input, Object element) {
						String text;
						if (element instanceof IFolder && element.equals(folder)) {
							text = super.decorateText(folder.getProjectRelativePath().toString(), element);
						} else {
							ILogEntry entry = (ILogEntry)filesToRestore.get(element);
							text = super.decorateText(input, element);
							if (entry != null) {
								text = Policy.bind("RestoreFromRepositoryFileSelectionPage.fileToRestore", text, entry.getRevision()); //$NON-NLS-1$
							}
						}
						return text;
					}
				}, 
				WorkbenchPlugin.getDefault().getWorkbench().getDecoratorManager().getLabelDecorator()));
		tree.setSorter(new ResourceSorter(ResourceSorter.NAME));
		tree.setInput(treeInput);
		
		GridData data = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		tree.getTree().setLayoutData(data);
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleFileSelection(event);
			}
		});
		composite.setContent(tree.getControl());
		return tree;
	}
	
	/**
	 * Method updateWidgetEnablements.
	 */
	private void updateWidgetEnablements() {
		setErrorMessage(null);
		if (filesToRestore.isEmpty()) {
			setPageComplete(false);
			return;
		}
		for (Iterator iter = filesToRestore.keySet().iterator(); iter.hasNext();) {
			IFile file = (IFile) iter.next();
			if (file.exists()) {
				setPageComplete(false);
				setErrorMessage(Policy.bind("RestoreFromRepositoryFileSelectionPage.fileExists", file.getName())); //$NON-NLS-1$
				return;
			}
		}
		setPageComplete(true);
	}
	
	/**
	 * Method initializeValues.
	 */
	private void initializeValues() {
		refresh();
	}

	/**
	 * Sets the folder.
	 * @param folder The folder to set
	 */
	public void setInput(IContainer folder, ICVSFile[] files) {
		if (folder.equals(this.folder)) return;
		this.folder = folder;
		setTreeInput(folder, files);
		initializeValues();
		updateWidgetEnablements();
	}
	
	/*
	 * Set the resource tree input to the files that were deleted	 */
	private void setTreeInput(IContainer folder, ICVSFile[] cvsFiles) {
		reset();
		IResource[] files = new IResource[cvsFiles.length];
		for (int i = 0; i < cvsFiles.length; i++) {
			try {
				files[i] = cvsFiles[i].getIResource();
			} catch (CVSException e) {
				// In practive, this error shold not occur.
				// It may if there is an existing folder with a name that matches the file
				// but this is bad in general when using CVS
				CVSUIPlugin.log(e);
			}
		}
		treeInput.setResources(files);
		// kludge to avoid auto-selection of first element
		// set the root to the folder's parent so the folder appears in the tree
		treeInput.setRoot(folder.getParent());
		refresh();
	}

	private void reset() {
		this.selectedFile = null;
		this.selectedRevision = null;
		treeInput.setResources(null);
		filesToRestore = new HashMap();
		if (fileContentPane != null && !fileContentPane.isDisposed()) {
			fileContentPane.setInput(null);
		}
		updateWidgetEnablements();
	}
	
	/**
	 * Method refresh.
	 */
	private void refresh() {
		if (folder == null) return;
		
		if (fileSelectionPane != null && !fileSelectionPane.isDisposed()) {
			fileSelectionPane.setText(Policy.bind("RestoreFromRepositoryFileSelectionPage.fileSelectionPaneTitle", folder.getProject().getName())); //$NON-NLS-1$
			fileSelectionPane.setImage(CompareUI.getImage(folder.getProject()));
		}
		
		if (revisionSelectionPane != null && !revisionSelectionPane.isDisposed()) {
			if (selectedFile == null) {
				revisionSelectionPane.setText(Policy.bind("RestoreFromRepositoryFileSelectionPage.emptyRevisionPane")); //$NON-NLS-1$
				revisionSelectionPane.setImage(null);
			}
		}
		
		// Empty the file content viewer
		if (fileContentPane != null && !fileContentPane.isDisposed()) {
			fileContentPane.setInput(null);
		}
		setErrorMessage(null);
		
		// refresh the tree
		if (fileTree != null) {
			// If the parent folder is in the tree, make sure it is expanded
			fileTree.setExpandedState(folder, true);
			fileTree.refresh();
		}
		if (revisionsTable != null)
			revisionsTable.refresh();
	}
	
	/*
	 * Set the log entry table input to the fetched entries  in response to a file selection
	 */
	private void setLogEntryTableInput(ILogEntry[] entries) {
		this.selectedRevision = null;
		// Refresh the table so it picks up the selected entries through its content provider
		revisionsTable.refresh();
		// Check the previously checked entry if one exists
		ILogEntry selectedEntry = (ILogEntry)filesToRestore.get(selectedFile);
		if (selectedEntry != null) {
			revisionsTable.setChecked(selectedEntry, true);
		}
		// Disable entries that represent deletions since they can't be loaded
		for (int i = 0; i < entries.length; i++) {
			ILogEntry entry = entries[i];
			if (entry.isDeletion()) {
				revisionsTable.setGrayed(entry, true);
			}
		}
		// Set the titlebar text for the revisions table
		revisionSelectionPane.setText(Policy.bind("RestoreFromRepositoryFileSelectionPage.revisionSelectionPaneTitle", selectedFile.getName())); //$NON-NLS-1$
		revisionSelectionPane.setImage(CompareUI.getImage(selectedFile));
		// Clear the file content pane
		fileContentPane.setInput(null);
		// Clear any previous error messages
		setErrorMessage(null);
	}
	
	private void handleFileSelection(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection == null || selection.isEmpty()) {
			clearSelection();
		} else {
			if (selection instanceof StructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				IResource resource = (IResource)structuredSelection.getFirstElement();
				if (resource instanceof IFile) {
					handleFileSelection((IFile) resource);
				} else {
					clearSelection();
				}
			}
		}
	}
	
	/**
	 * Method handleFileSelection.
	 * @param file
	 */
	private void handleFileSelection(IFile file) {
		if (this.selectedFile == file) return;
		this.selectedFile = file;
		if (entriesCache.get(file) == null) {
			try {
				
				// First, we need to create a remote file handle so we can get the log entries
				ICVSFolder parent = CVSWorkspaceRoot.getCVSFolderFor(file.getParent());
				FolderSyncInfo info = parent.getFolderSyncInfo();
				ICVSRepositoryLocation location = CVSProviderPlugin.getPlugin().getRepository(info.getRoot());
				final ICVSRemoteFile remoteFile = location.getRemoteFile(new Path(info.getRepository()).append(file.getName()).toString(), CVSTag.DEFAULT);
				
				// Then we need to fetch the log entries
				getContainer().run(true, true, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							// fetch the entries
							ILogEntry[] entries = remoteFile.getLogEntries(monitor);
							// cache the entries with the selected file
							entriesCache.put(selectedFile, entries);
						} catch (TeamException e) {
							throw new InvocationTargetException(e);
						}
					}
				});

			} catch (CVSException e) {
				setErrorMessage(
					CVSUIPlugin.openError(getShell(), null, null, e, CVSUIPlugin.PERFORM_SYNC_EXEC)
						.getMessage());
				return;
			} catch (InvocationTargetException e) {
				setErrorMessage(
					CVSUIPlugin.openError(getShell(), null, null, e, CVSUIPlugin.PERFORM_SYNC_EXEC)
						.getMessage());
				return;
			} catch (InterruptedException e) {
				return;
			}
		}
		
		// Set the log table to display the entries for the selected file
		setLogEntryTableInput(getSelectedEntries());
	}

	private ILogEntry[] getSelectedEntries() {
		return (ILogEntry[])entriesCache.get(selectedFile);
	}
	
	/**
	 * Method getContents.
	 * @param logEntry
	 * @return InputStream
	 */
	private InputStream getContentsFromLogEntry(final ILogEntry logEntry) {
		final InputStream[] is = new InputStream[] { null };
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						ICVSRemoteFile remoteFile = logEntry.getRemoteFile();
						is[0] = remoteFile.getContents(monitor);
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InvocationTargetException e) {
			setErrorMessage(
				CVSUIPlugin.openError(getShell(), null, null, e, CVSUIPlugin.PERFORM_SYNC_EXEC)
					.getMessage());
			return null;
		} catch (InterruptedException e) {
			return null;
		}
		setErrorMessage(null);
		return new BufferedInputStream(is[0]);
	}

	private void handleRevisionChecked(CheckStateChangedEvent event) {
		// Only allow one element to be checked
		if (event.getChecked()) {
			if (((ILogEntry)event.getElement()).isDeletion()) {
				revisionsTable.setChecked(event.getElement(), false);
			} else {
				revisionsTable.setCheckedElements(new Object[] {event.getElement()});
				filesToRestore.put(selectedFile, event.getElement());
			}
		}
		if (revisionsTable.getCheckedElements().length == 0) {
			filesToRestore.remove(selectedFile);
		}
		fileTree.refresh();
		updateWidgetEnablements();
	}
				
	/*
	 * A revision in the revision table has been selected.
	 * Populate the file contents pane with the selected log entry.
	 */
	private void handleRevisionSelection(Widget w) {
		if (fileContentPane != null && !fileContentPane.isDisposed()) {
			Object o= w.getData();
			if (o instanceof ILogEntry) {
				ILogEntry selected = (ILogEntry) o;
				if (this.selectedRevision == selected) return;
				this.selectedRevision = selected;
				if (selected.isDeletion()) {
					fileContentPane.setInput(null);
				} else {
					fileContentPane.setInput(new HistoryInput(selectedFile, selected));
					fileContentPane.setText(getEditionLabel(selectedFile, selected));
					fileContentPane.setImage(CompareUI.getImage(selectedFile));
				}
			} else {
				fileContentPane.setInput(null);
			}
		}
	}
	/**
	 * Method getEditionLabel.
	 * @param selectedFile
	 * @param selected
	 * @return String
	 */
	private String getEditionLabel(IFile selectedFile, ILogEntry selected) {
		return Policy.bind("RestoreFromRepositoryFileSelectionPage.fileContentPaneTitle", //$NON-NLS-1$
			new Object[] { selectedFile.getName(), selected.getRevision(), selectedFile.getFullPath().makeRelative().removeLastSegments(1).toString() });
	}
	
	public boolean restoreSelectedFiles() {
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask(null, 100 * filesToRestore.size());
						for (Iterator iter = filesToRestore.keySet().iterator();iter.hasNext();) {
							IFile file = (IFile) iter.next();
							ILogEntry entry = (ILogEntry)filesToRestore.get(file);
							ensureParentExists(file);
							file.create(entry.getRemoteFile().getContents(Policy.subMonitorFor(monitor, 50)), false, Policy.subMonitorFor(monitor, 50));
						}
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InvocationTargetException e) {
			setErrorMessage(
				CVSUIPlugin.openError(getShell(), null, null, e, CVSUIPlugin.PERFORM_SYNC_EXEC)
					.getMessage());
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	/**
	 * Method ensureParentExists.
	 * @param file
	 */
	private void ensureParentExists(IResource resource) throws CoreException {
		IContainer parent = resource.getParent();
		if (!parent.exists() && parent.getType() == IResource.FOLDER) {
			ensureParentExists(parent);
			((IFolder)parent).create(false, true, null);
		}
	}

	private void clearSelection() {
		this.selectedFile = null;
		this.selectedRevision = null;
		refresh();
	}
}
