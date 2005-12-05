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

package org.eclipse.team.internal.ui.filehistory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.filehistory.IFileHistory;
import org.eclipse.team.core.filehistory.IFileRevision;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;

public class GenericHistoryView extends ViewPart {

	private IFile file;

	// cached for efficiency
	private IFileRevision[] entries;

	private GenericHistoryTableProvider historyTableProvider;

	private TableViewer tableViewer;

	/*
	 * private TextViewer textViewer; private TableViewer tagViewer;
	 * 
	 * private OpenLogEntryAction openAction; private IAction toggleTextAction;
	 * private IAction toggleTextWrapAction; private IAction toggleListAction;
	 * private TextViewerAction copyAction; private TextViewerAction
	 * selectAllAction; private Action getContentsAction; private Action
	 * getRevisionAction; private Action refreshAction; private Action
	 * tagWithExistingAction; private Action linkWithEditorAction;
	 */

	private SashForm sashForm;

	private SashForm innerSashForm;

	private Image branchImage;

	private Image versionImage;

	protected IFileRevision currentSelection;

	private boolean linkingEnabled;

	private IPreferenceStore settings;

	private FetchLogEntriesJob fetchLogEntriesJob;

	private boolean shutdown = false;

	private Action refreshAction;
	private Action getPredecessor;
	private Action getDirectDescendents;

	public static final String VIEW_ID = "org.eclipse.team.ccvs.ui.HistoryView"; //$NON-NLS-1$

	public void createPartControl(Composite parent) {

		// initializeImages();

		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		tableViewer = createTable(sashForm);
		// innerSashForm = new SashForm(sashForm, SWT.HORIZONTAL);
		// tagViewer = createTagTable(innerSashForm);
		// textViewer = createText(innerSashForm);
		// sashForm.setWeights(new int[] { 70, 30 });
		// innerSashForm.setWeights(new int[] { 50, 50 });//

		contributeActions();

		// setViewerVisibility();

		// set F1 help
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(sashForm,
		// IHelpContextIds.RESOURCE_HISTORY_VIEW);
		initDragAndDrop();

		// add listener for editor page activation - this is to support editor
		// linking
		// getSite().getPage().addPartListener(partListener);
		// getSite().getPage().addPartListener(partListener2);
	}

	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/**
	 * Adds drag and drop support to the history view.
	 */
	void initDragAndDrop() {
		int ops = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		Transfer[] transfers = new Transfer[] { ResourceTransfer.getInstance(),
				ResourceTransfer.getInstance() };
		tableViewer.addDropSupport(ops, transfers,
				new GenericHistoryDropAdapter(tableViewer, this));
	}

	protected void contributeActions() {
		refreshAction = new Action("Refresh", null) {
			public void run() {
				refresh();
			}
		};
		
		getDirectDescendents = new Action("Get Direct Descendents", null) {
			public void run() {
				IFileHistory currentHistory = historyTableProvider.getIFileHistory();
				try {
					entries=currentHistory.getDirectDescendents(currentSelection);
				} catch (TeamException e) {}
				
				tableViewer.refresh();
			}
		};
		

		getPredecessor = new Action("Get Predecessor", null) {
			public void run() {
				IFileHistory currentHistory = historyTableProvider.getIFileHistory();
				try {
					entries=new IFileRevision[] {currentHistory.getPredecessor(currentSelection)};
				} catch (TeamException e) {}
				
				tableViewer.refresh();
			}
		};
		
//		 Contribute actions to popup menu
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

	}

	private void fillTableMenu(IMenuManager manager) {
		// file actions go first (view file)
		/*manager.add(new Separator(IWorkbenchActionConstants.GROUP_FILE));
		if (file != null) {
			// Add the "Add to Workspace" action if 1 revision is selected.
			ISelection sel = tableViewer.getSelection();
			if (!sel.isEmpty()) {
				if (sel instanceof IStructuredSelection) {
					if (((IStructuredSelection)sel).size() == 1) {
						manager.add(refreshAction);
						manager.add(getRevisionAction);
						manager.add(new Separator());
						manager.add(tagWithExistingAction);
					}
				}
			}
		}*/
		manager.add(getPredecessor);
		manager.add(getDirectDescendents);
		manager.add(new Separator("additions")); //$NON-NLS-1$
		manager.add(refreshAction);
		manager.add(new Separator("additions-end")); //$NON-NLS-1$
	}
	
	/**
	 * Creates the group that displays lists of the available repositories and
	 * team streams.
	 * 
	 * @param the
	 *            parent composite to contain the group
	 * @return the group control
	 */
	protected TableViewer createTable(Composite parent) {

		historyTableProvider = new GenericHistoryTableProvider();
		TableViewer viewer = historyTableProvider.createTable(parent);

		viewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {

				// The entries of already been fetch so return them
				if (entries != null)
					return entries;

				// The entries need to be fetch (or are being fetched)
				if (!(inputElement instanceof IFileHistory))
					return null;

				final IFileHistory remoteFile = (IFileHistory) inputElement;
				if (fetchLogEntriesJob == null) {
					fetchLogEntriesJob = new FetchLogEntriesJob();
				}

				IFileHistory file = fetchLogEntriesJob.getRemoteFile();

				if (file == null || !file.equals(remoteFile)) { // The resource
																// has changed
																// so stop the
																// currently
																// running job
					if (fetchLogEntriesJob.getState() != Job.NONE) {
						fetchLogEntriesJob.cancel();
						try {
							fetchLogEntriesJob.join();
						} catch (InterruptedException e) {
						}
					}
					fetchLogEntriesJob.setRemoteFile(remoteFile);
				} // Schedule the job even if it is already running
				Utils.schedule(fetchLogEntriesJob, getViewSite());

				return new Object[0];
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				entries = null;
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				
				if (selection != null &&
					selection instanceof IStructuredSelection){
						IStructuredSelection ss = (IStructuredSelection) selection;
						currentSelection = (IFileRevision) ss.getFirstElement();
				}
				/*
				 * if (selection == null || !(selection instanceof
				 * IStructuredSelection)) { textViewer.setDocument(new
				 * Document("")); //$NON-NLS-1$ tagViewer.setInput(null);
				 * return; } IStructuredSelection ss =
				 * (IStructuredSelection)selection; if (ss.size() != 1) {
				 * textViewer.setDocument(new Document("")); //$NON-NLS-1$
				 * tagViewer.setInput(null); return; } ILogEntry entry =
				 * (ILogEntry)ss.getFirstElement(); textViewer.setDocument(new
				 * Document(entry.getComment()));
				 * tagViewer.setInput(entry.getTags());
				 */
			}
		});

		return viewer;
	}

	/*
	 * protected TextViewer createText(Composite parent) { TextViewer result =
	 * new TextViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI |
	 * SWT.BORDER | SWT.READ_ONLY); result.addSelectionChangedListener(new
	 * ISelectionChangedListener() { public void
	 * selectionChanged(SelectionChangedEvent event) { copyAction.update(); }
	 * }); return result; }
	 */

	/*private Action getContextMenuAction(String title,
			final boolean needsProgressDialog, final IWorkspaceRunnable action) {
		return new Action(title) {
			public void run() {
				try {
					if (file == null)
						return;
					ISelection selection = tableViewer.getSelection();
					if (!(selection instanceof IStructuredSelection))
						return;
					IStructuredSelection ss = (IStructuredSelection) selection;
					Object o = ss.getFirstElement();
					currentSelection = (IFileHistory) o;
					if (needsProgressDialog) {
						PlatformUI.getWorkbench().getProgressService().run(
								true, true, new IRunnableWithProgress() {
									public void run(IProgressMonitor monitor)
											throws InvocationTargetException,
											InterruptedException {
										try {
											action.run(monitor);
										} catch (CoreException e) {
											throw new InvocationTargetException(
													e);
										}
									}
								});
					} else {
						try {
							action.run(null);
						} catch (CoreException e) {
							throw new InvocationTargetException(e);
						}
					}
				} catch (InvocationTargetException e) {
					// CVSUIPlugin.openError(getViewSite().getShell(), null,
					// null, e, CVSUIPlugin.LOG_NONTEAM_EXCEPTIONS);
				} catch (InterruptedException e) {
					// Do nothing
				}
			}

			public boolean isEnabled() {
				ISelection selection = tableViewer.getSelection();
				if (!(selection instanceof IStructuredSelection))
					return false;
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (ss.size() != 1)
					return false;
				return true;
			}
		};
	}*/

	/*
	 * Refresh the view by refetching the log entries for the remote file
	 */
	private void refresh() {
		entries = null;
		BusyIndicator.showWhile(tableViewer.getTable().getDisplay(),
				new Runnable() {
					public void run() {
						// if a local file was fed to the history view then we
						// will have to refetch the handle
						// to properly display the current revision marker.
						/*
						 * if(file != null) { ICVSRemoteFile remoteFile; try {
						 * remoteFile = (ICVSRemoteFile)
						 * CVSWorkspaceRoot.getRemoteResourceFor(file);
						 * historyTableProvider.setFile(remoteFile); } catch
						 * (CVSException e) { // use previously fetched remote
						 * file, but log error CVSUIPlugin.log(e); } }
						 */
						tableViewer.refresh();
					}
				});
	}

	/**
	 * Shows the history for the given IResource in the view.
	 * 
	 * Only files are supported for now.
	 */
	public void showHistory(IResource resource, boolean refetch) {
		if (resource instanceof IFile) {
			IFile newfile = (IFile) resource;
			if (!refetch && this.file != null && newfile.equals(this.file)) {
				return;
			}
			this.file = newfile;
			RepositoryProvider teamProvider = RepositoryProvider
					.getProvider(file.getProject());
			IFileHistory fileHistory = teamProvider.getFileHistoryProvider()
					.getFileHistoryFor(resource, new NullProgressMonitor());

			try {
				IFileRevision[] revisions = fileHistory
						.getFileRevisions();

			} catch (TeamException e) {

			}
			historyTableProvider.setFile(fileHistory);
			tableViewer.setInput(fileHistory);
			setContentDescription(newfile.getName());

			/*
			 * RepositoryProvider teamProvider =
			 * RepositoryProvider.getProvider(file.getProject(),
			 * CVSProviderPlugin.getTypeId()); if (teamProvider != null) { try { //
			 * for a file this will return the base //ICVSRemoteFile remoteFile =
			 * (ICVSRemoteFile)CVSWorkspaceRoot.getRemoteResourceFor(file);
			 * if(remoteFile != null) {
			 * historyTableProvider.setFile(remoteFile); // input is set
			 * asynchronously so we can't assume that the view // has been
			 * populated until the job that queries for the history // has
			 * completed. tableViewer.setInput(remoteFile);
			 * setContentDescription(remoteFile.getName());
			 * setTitleToolTip(resource.getFullPath().toString()); } } catch
			 * (TeamException e) {
			 * //CVSUIPlugin.openError(getViewSite().getShell(), null, null, e); } }
			 */
		} else {
			this.file = null;
			tableViewer.setInput(null);
			setContentDescription(""); //$NON-NLS-1$
			setTitleToolTip(""); //$NON-NLS-1$
		}
	}

	private class FetchLogEntriesJob extends Job {
		public IFileHistory fileHistory;

		public FetchLogEntriesJob() {
			super("Generic File History Fetcher");
		}

		public void setRemoteFile(IFileHistory file) {
			this.fileHistory = file;
		}

		public IStatus run(IProgressMonitor monitor) {
			try {
				if (fileHistory != null && !shutdown) {
					entries = fileHistory.getFileRevisions();
					// final String revisionId = fileHistory.;
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (entries != null && tableViewer != null
								&& !tableViewer.getTable().isDisposed()) {
								tableViewer.refresh();
								// selectRevision(revisionId);
							}
						}
					});
				}
				return Status.OK_STATUS;
			} catch (TeamException e) {
				return e.getStatus();
			}
		}

		public IFileHistory getRemoteFile() {
			return this.fileHistory;
		}
	};
}
