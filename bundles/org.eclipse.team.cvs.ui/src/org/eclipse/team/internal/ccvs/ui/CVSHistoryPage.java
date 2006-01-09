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

package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.ITag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.ui.actions.OpenRevisionAction;
import org.eclipse.team.internal.ui.ITeamUIImages;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.history.IHistoryPage;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.part.Page;

public class CVSHistoryPage extends Page implements IHistoryPage {
	private IFile file;

	// cached for efficiency
	/* private */IFileRevision[] entries;

	protected CVSHistoryTableProvider historyTableProvider;

	/* private */TableViewer tableViewer;
	protected TextViewer textViewer;
	protected TableViewer tagViewer;

	/* private */OpenRevisionAction openAction;
	private IAction toggleTextAction;
	private IAction toggleTextWrapAction;
	private IAction toggleListAction;
	private Action refreshAction;

	private SashForm sashForm;
	private SashForm innerSashForm;

	protected IFileRevision currentSelection;

	protected FetchLogEntriesJob fetchLogEntriesJob;

	private IViewSite parentSite;

	/* private */boolean shutdown = false;

	// preferences
	public final static String PREF_GENERIC_HISTORYVIEW_SHOW_COMMENTS = "pref_generichistory_show_comments"; //$NON-NLS-1$

	public final static String PREF_GENERIC_HISTORYVIEW_WRAP_COMMENTS = "pref_generichistory_wrap_comments"; //$NON-NLS-1$

	public final static String PREF_GENERIC_HISTORYVIEW_SHOW_TAGS = "pref_generichistory_show_tags"; //$NON-NLS-1$

	public void createControl(Composite parent) {
		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		tableViewer = createTable(sashForm);
		innerSashForm = new SashForm(sashForm, SWT.HORIZONTAL);
		tagViewer = createTagTable(innerSashForm);
		textViewer = createText(innerSashForm);
		sashForm.setWeights(new int[] {70, 30});
		innerSashForm.setWeights(new int[] {50, 50});

		contributeActions();

		setViewerVisibility();
	}

	private TextViewer createText(SashForm parent) {
		TextViewer result = new TextViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
		result.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				//copyAction.update();
			}
		});
		return result;
	}

	private TableViewer createTagTable(SashForm parent) {
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		TableViewer result = new TableViewer(table);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(100));
		table.setLayout(layout);
		result.setContentProvider(new SimpleContentProvider() {
			public Object[] getElements(Object inputElement) {
				if (inputElement == null)
					return new Object[0];
				ITag[] tags = (ITag[]) inputElement;
				return tags;
			}
		});
		result.setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				if (element == null)
					return null;
				ITag tag = (ITag) element;
				//Need an image
				return null;
			}

			public String getText(Object element) {
				return ((ITag) element).getName();
			}
		});
		result.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (!(e1 instanceof ITag) || !(e2 instanceof ITag))
					return super.compare(viewer, e1, e2);
				ITag tag1 = (ITag) e1;
				ITag tag2 = (ITag) e2;
				/*int type1 = tag1.getType();
				 int type2 = tag2.getType();
				 if (type1 != type2) {
				 return type2 - type1;
				 }*/
				return super.compare(viewer, tag1, tag2);
			}
		});
		return result;
	}

	public void setFocus() {
		sashForm.setFocus();
	}

	protected void contributeActions() {
		TeamUIPlugin plugin = TeamUIPlugin.getPlugin();

		refreshAction = new Action(TeamUIMessages.GenericHistoryView_Refresh, plugin.getImageDescriptor(ITeamUIImages.IMG_REFRESH)) {
			public void run() {
				refresh();
			}
		};

		// Double click open action
		openAction = new OpenRevisionAction();
		tableViewer.getTable().addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				openAction.selectionChanged(null, tableViewer.getSelection());
				openAction.run(null);
			}
		});

		// Toggle text visible action
		final IPreferenceStore store = TeamUIPlugin.getPlugin().getPreferenceStore();
		toggleTextAction = new Action(TeamUIMessages.GenericHistoryView_ShowCommentViewer) {
			public void run() {
				setViewerVisibility();
				store.setValue(CVSHistoryPage.PREF_GENERIC_HISTORYVIEW_SHOW_COMMENTS, toggleTextAction.isChecked());
			}
		};
		toggleTextAction.setChecked(store.getBoolean(CVSHistoryPage.PREF_GENERIC_HISTORYVIEW_SHOW_COMMENTS));
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(toggleTextAction, IHelpContextIds.SHOW_COMMENT_IN_HISTORY_ACTION);	

		// Toggle wrap comments action
		toggleTextWrapAction = new Action(TeamUIMessages.GenericHistoryView_WrapComments) {
			public void run() {
				setViewerVisibility();
				store.setValue(CVSHistoryPage.PREF_GENERIC_HISTORYVIEW_WRAP_COMMENTS, toggleTextWrapAction.isChecked());
			}
		};
		toggleTextWrapAction.setChecked(store.getBoolean(CVSHistoryPage.PREF_GENERIC_HISTORYVIEW_WRAP_COMMENTS));
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(toggleTextWrapAction, IHelpContextIds.SHOW_TAGS_IN_HISTORY_ACTION);   

		// Toggle list visible action
		toggleListAction = new Action(TeamUIMessages.GenericHistoryView_ShowTagViewer) {
			public void run() {
				setViewerVisibility();
				store.setValue(CVSHistoryPage.PREF_GENERIC_HISTORYVIEW_SHOW_TAGS, toggleListAction.isChecked());
			}
		};
		toggleListAction.setChecked(store.getBoolean(CVSHistoryPage.PREF_GENERIC_HISTORYVIEW_SHOW_TAGS));
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(toggleListAction, IHelpContextIds.SHOW_TAGS_IN_HISTORY_ACTION);	

		//Contribute actions to popup menu
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(tableViewer.getTable());
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuMgr) {
				fillTableMenu(menuMgr);
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		tableViewer.getTable().setMenu(menu);
		parentSite.registerContextMenu(menuMgr, tableViewer);

		// Contribute toggle text visible to the toolbar drop-down
		IActionBars actionBars = getSite().getActionBars();
		IMenuManager actionBarsMenu = actionBars.getMenuManager();
		actionBarsMenu.add(toggleTextWrapAction);
		actionBarsMenu.add(new Separator());
		actionBarsMenu.add(toggleTextAction);
		actionBarsMenu.add(toggleListAction);

	}

	/* private */ void fillTableMenu(IMenuManager manager) {
		// file actions go first (view file)
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

		historyTableProvider = new CVSHistoryTableProvider();
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
							TeamUIPlugin.log(new TeamException(NLS.bind(TeamUIMessages.GenericHistoryView_ErrorFetchingEntries, new String[] {""}), e)); //$NON-NLS-1$
						}
					}
					fetchLogEntriesJob.setRemoteFile(remoteFile);
				} // Schedule the job even if it is already running
				Utils.schedule(fetchLogEntriesJob, /*getSite()*/parentSite);

				return new Object[0];
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				entries = null;
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();

				if (selection != null && selection instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection) selection;
					currentSelection = (IFileRevision) ss.getFirstElement();
				}

				if (selection == null || !(selection instanceof IStructuredSelection)) {
					textViewer.setDocument(new Document("")); //$NON-NLS-1$ 
					tagViewer.setInput(null);
					return;
				}

				IStructuredSelection ss = (IStructuredSelection) selection;
				if (ss.size() != 1) {
					textViewer.setDocument(new Document("")); //$NON-NLS-1$
					tagViewer.setInput(null);
					return;
				}

				IFileRevision entry = (IFileRevision) ss.getFirstElement();
				textViewer.setDocument(new Document(entry.getComment()));
				tagViewer.setInput(entry.getTags());

			}
		});

		return viewer;
	}

	/*
	 * Refresh the view by refetching the log entries for the remote file
	 */
	public void refresh() {
		entries = null;
		BusyIndicator.showWhile(tableViewer.getTable().getDisplay(), new Runnable() {
			public void run() {
				// if a local file was fed to the history view then we
				// will have to refetch the handle
				// to properly display the current revision marker.
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
			RepositoryProvider teamProvider = RepositoryProvider.getProvider(file.getProject());
			IFileHistory fileHistory = teamProvider.getFileHistoryProvider().getFileHistoryFor(resource, new NullProgressMonitor());

			historyTableProvider.setFile(fileHistory, file);
			tableViewer.setInput(fileHistory);
		} else {
			this.file = null;
			tableViewer.setInput(null);
		}

		return;
	}

	/* private */void setViewerVisibility() {
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

		boolean wrapText = toggleTextWrapAction.isChecked();
		textViewer.getTextWidget().setWordWrap(wrapText);
	}

	public void dispose() {
		shutdown = true;

		if (fetchLogEntriesJob != null) {
			if (fetchLogEntriesJob.getState() != Job.NONE) {
				fetchLogEntriesJob.cancel();
				try {
					fetchLogEntriesJob.join();
				} catch (InterruptedException e) {
					TeamUIPlugin.log(new TeamException(NLS.bind(TeamUIMessages.GenericHistoryView_ErrorFetchingEntries, new String[] {""}), e)); //$NON-NLS-1$
				}
			}
		}
	}

	private class FetchLogEntriesJob extends Job {
		public IFileHistory fileHistory;

		public FetchLogEntriesJob() {
			super(TeamUIMessages.GenericHistoryView_GenericFileHistoryFetcher);
		}

		public void setRemoteFile(IFileHistory file) {
			this.fileHistory = file;
		}

		public IStatus run(IProgressMonitor monitor) {
			try {
				if (fileHistory != null && !shutdown) {
					entries = fileHistory.getFileRevisions();
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (entries != null && tableViewer != null && !tableViewer.getTable().isDisposed()) {
								tableViewer.refresh();
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
	}

	/**
	 * A default content provider to prevent subclasses from
	 * having to implement methods they don't need.
	 */
	private class SimpleContentProvider implements IStructuredContentProvider {

		/**
		 * SimpleContentProvider constructor.
		 */
		public SimpleContentProvider() {
			super();
		}

		/*
		 * @see SimpleContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * @see SimpleContentProvider#getElements()
		 */
		public Object[] getElements(Object element) {
			return new Object[0];
		}

		/*
		 * @see SimpleContentProvider#inputChanged()
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	public Control getControl() {
		return sashForm;
	}

	public boolean canShowHistoryFor(IResource resource) {

		RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject());
		if (provider instanceof CVSTeamProvider)
			return true;

		return false;
	}

	public String getName() {
		return file.getName();
	}

	public void setSite(IViewSite viewSite) {
		this.parentSite = viewSite;
	}
}
