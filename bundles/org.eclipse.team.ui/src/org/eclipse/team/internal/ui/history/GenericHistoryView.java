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

package org.eclipse.team.internal.ui.history;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
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
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.*;
import org.eclipse.team.internal.ui.ITeamUIImages;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.history.actions.OpenRevisionAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;

public class GenericHistoryView extends ViewPart {

	private IFile file;

	// cached for efficiency
	private IFileRevision[] entries;

	protected GenericHistoryTableProvider historyTableProvider;

	private TableViewer tableViewer;
	protected TextViewer textViewer;
	protected TableViewer tagViewer;

	
	protected OpenRevisionAction openAction;
	private IAction toggleTextAction;
	private IAction toggleTextWrapAction;
	private IAction toggleListAction;
	private Action refreshAction;
	private Action linkWithEditorAction;
	
	private SashForm sashForm;
	private SashForm innerSashForm;

	protected IFileRevision currentSelection;

	protected FetchLogEntriesJob fetchLogEntriesJob;

	private boolean shutdown = false;

	private boolean linkingEnabled;
	
	public static final String VIEW_ID = "org.eclipse.team.ui.GenericHistoryView"; //$NON-NLS-1$

	private IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
			if (part instanceof IEditorPart)
				editorActivated((IEditorPart) part);
		}
		public void partBroughtToTop(IWorkbenchPart part) {
			if(part == GenericHistoryView.this)
				editorActivated(getViewSite().getPage().getActiveEditor());
		}
		public void partOpened(IWorkbenchPart part) {
			if(part == GenericHistoryView.this)
				editorActivated(getViewSite().getPage().getActiveEditor());
		}
		public void partClosed(IWorkbenchPart part) {
		}
		public void partDeactivated(IWorkbenchPart part) {
		}
	};
	
	private IPartListener2 partListener2 = new IPartListener2() {
		public void partActivated(IWorkbenchPartReference ref) {
		}
		public void partBroughtToTop(IWorkbenchPartReference ref) {
		}
		public void partClosed(IWorkbenchPartReference ref) {
		}
		public void partDeactivated(IWorkbenchPartReference ref) {
		}
		public void partOpened(IWorkbenchPartReference ref) {
		}
		public void partHidden(IWorkbenchPartReference ref) {
		}
		public void partVisible(IWorkbenchPartReference ref) {
			if(ref.getPart(true) == GenericHistoryView.this)
				editorActivated(getViewSite().getPage().getActiveEditor());
		}
		public void partInputChanged(IWorkbenchPartReference ref) {
		}
	};

	public void createPartControl(Composite parent) {
		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		tableViewer = createTable(sashForm);
		innerSashForm = new SashForm(sashForm, SWT.HORIZONTAL);
		tagViewer = createTagTable(innerSashForm);
		textViewer = createText(innerSashForm);
		sashForm.setWeights(new int[] { 70, 30 });
		innerSashForm.setWeights(new int[] { 50, 50 });

		this.linkingEnabled = TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IFileHistoryConstants.PREF_GENERIC_HISTORYVIEW_EDITOR_LINKING);
		
		contributeActions();

		setViewerVisibility();

		// set F1 help
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(sashForm,
		// IHelpContextIds.RESOURCE_HISTORY_VIEW);
		initDragAndDrop();

		// add listener for editor page activation - this is to support editor
		// linking
		getSite().getPage().addPartListener(partListener);
		getSite().getPage().addPartListener(partListener2);
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
				if (inputElement == null) return new Object[0];
				ITag[] tags = (ITag[])inputElement;
				return tags;
			}
		});
		result.setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				if (element == null) return null;
				ITag tag = (ITag)element;
				//Need an image
				return null;
			}
			public String getText(Object element) {
				return ((ITag)element).getName();
			}
		});
		result.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (!(e1 instanceof ITag) || !(e2 instanceof ITag)) return super.compare(viewer, e1, e2);
				ITag tag1 = (ITag)e1;
				ITag tag2 = (ITag)e2;
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
		TeamUIPlugin plugin = TeamUIPlugin.getPlugin();
		
		// Double click open action
		openAction = new OpenRevisionAction();
		tableViewer.getTable().addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				openAction.selectionChanged(null, tableViewer.getSelection());
				openAction.run(null);
			}
		});
		
		refreshAction = new Action(TeamUIMessages.GenericHistoryView_Refresh,  plugin.getImageDescriptor(ITeamUIImages.IMG_REFRESH)) {
			public void run() {
				refresh();
			}
		};
		
		linkWithEditorAction = new Action(TeamUIMessages.GenericHistoryView_LinkWithEditor, plugin.getImageDescriptor(ITeamUIImages.IMG_LINK_WITH)) { 
			 public void run() {
				 setLinkingEnabled(isChecked());
			 }
		 };
		 linkWithEditorAction.setChecked(isLinkingEnabled());
		 		
		// Toggle text visible action
		final IPreferenceStore store = TeamUIPlugin.getPlugin().getPreferenceStore();
		toggleTextAction = new Action(TeamUIMessages.GenericHistoryView_ShowCommentViewer) { 
			public void run() {
				setViewerVisibility();
				store.setValue(IFileHistoryConstants.PREF_GENERIC_HISTORYVIEW_SHOW_COMMENTS, toggleTextAction.isChecked());
			}
		};
		toggleTextAction.setChecked(store.getBoolean(IFileHistoryConstants.PREF_GENERIC_HISTORYVIEW_SHOW_COMMENTS));
        //PlatformUI.getWorkbench().getHelpSystem().setHelp(toggleTextAction, IHelpContextIds.SHOW_COMMENT_IN_HISTORY_ACTION);	

        // Toggle wrap comments action
        toggleTextWrapAction = new Action(TeamUIMessages.GenericHistoryView_WrapComments) { 
          public void run() {
            setViewerVisibility();
            store.setValue(IFileHistoryConstants.PREF_GENERIC_HISTORYVIEW_WRAP_COMMENTS, toggleTextWrapAction.isChecked());
          }
        };
        toggleTextWrapAction.setChecked(store.getBoolean(IFileHistoryConstants.PREF_GENERIC_HISTORYVIEW_WRAP_COMMENTS));
        //PlatformUI.getWorkbench().getHelpSystem().setHelp(toggleTextWrapAction, IHelpContextIds.SHOW_TAGS_IN_HISTORY_ACTION);   
		
        // Toggle list visible action
		toggleListAction = new Action(TeamUIMessages.GenericHistoryView_ShowTagViewer) { 
			public void run() {
				setViewerVisibility();
				store.setValue(IFileHistoryConstants.PREF_GENERIC_HISTORYVIEW_SHOW_TAGS, toggleListAction.isChecked());
			}
		};
		toggleListAction.setChecked(store.getBoolean(IFileHistoryConstants.PREF_GENERIC_HISTORYVIEW_SHOW_TAGS));
        //PlatformUI.getWorkbench().getHelpSystem().setHelp(toggleListAction, IHelpContextIds.SHOW_TAGS_IN_HISTORY_ACTION);	
		
		
		//Create the local tool bar
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
		tbm.add(refreshAction);
		tbm.add(linkWithEditorAction);
		tbm.update(false);
        
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
		getSite().registerContextMenu(menuMgr, tableViewer);
		
		// Contribute toggle text visible to the toolbar drop-down
		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager actionBarsMenu = actionBars.getMenuManager();
		actionBarsMenu.add(toggleTextWrapAction);
		actionBarsMenu.add(new Separator());
		actionBarsMenu.add(toggleTextAction);
		actionBarsMenu.add(toggleListAction);

	}

	private void fillTableMenu(IMenuManager manager) {
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
							TeamUIPlugin.log(new TeamException(NLS.bind(TeamUIMessages.GenericHistoryView_ErrorFetchingEntries, new String[] { "" }), e));   //$NON-NLS-1$
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
				
				 if (selection == null || !(selection instanceof IStructuredSelection)) { 
					 textViewer.setDocument(new Document("")); //$NON-NLS-1$ 
					 tagViewer.setInput(null);
				  return; } 
				 
				 IStructuredSelection ss =(IStructuredSelection)selection;
				  if (ss.size() != 1) {
				     textViewer.setDocument(new Document("")); //$NON-NLS-1$
				     tagViewer.setInput(null); 
				     return; 
				 } 
				  
				  IFileRevision entry =(IFileRevision)ss.getFirstElement(); 
				  textViewer.setDocument(new Document(entry.getComment()));
				  tagViewer.setInput(entry.getTags());
				 
			}
		});

		return viewer;
	}
	
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
			historyTableProvider.setFile(fileHistory, file);
			tableViewer.setInput(fileHistory);
			setContentDescription(newfile.getName());
		} else {
			this.file = null;
			tableViewer.setInput(null);
			setContentDescription(""); //$NON-NLS-1$
			setTitleToolTip(""); //$NON-NLS-1$
		}
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
        
		boolean wrapText = toggleTextWrapAction.isChecked();
        textViewer.getTextWidget().setWordWrap(wrapText);
	}
	
	/**
	 * Enabled linking to the active editor
	 */
	public void setLinkingEnabled(boolean enabled) {
		this.linkingEnabled = enabled;

		// remember the last setting in the dialog settings		
		TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IFileHistoryConstants.PREF_GENERIC_HISTORYVIEW_EDITOR_LINKING, enabled);
	
		// if turning linking on, update the selection to correspond to the active editor
		if (enabled) {
			editorActivated(getSite().getPage().getActiveEditor());
		}
	}
	
	/**
	 * An editor has been activated.  Fetch the history if the file is shared and the history view
	 * is visible in the current page.
	 * 
	 * @param editor the active editor
	 */
	protected void editorActivated(IEditorPart editor) {
		// Only fetch contents if the view is shown in the current page.
		if (editor == null || !isLinkingEnabled() || !checkIfPageIsVisible()) {
			return;
		}		
		IEditorInput input = editor.getEditorInput();
		
		if (input instanceof FileRevisionEditorInput){
			IFile file;
			try {
				file = ResourceUtil.getFile(((FileRevisionEditorInput) input).getStorage().getFullPath());
				if(file != null) {
	                showHistory(file, false);
	            }
			} catch (CoreException e) {}
		} // Handle regular file editors
		else {
	        IFile file = ResourceUtil.getFile(input);
	        if(file != null) {
	            showHistory(file, false /* don't fetch if already cached */);
	        }
		}
	}
	
	private boolean checkIfPageIsVisible() {
		return getViewSite().getPage().isPartVisible(this);
	}
	
	/**
	 * Returns if linking to the ative editor is enabled or disabled.
	 * @return boolean indicating state of editor linking.
	 */
	private boolean isLinkingEnabled() {
		return linkingEnabled;
	}

	public void dispose() {
		shutdown = true;

		if(fetchLogEntriesJob != null) {
			if(fetchLogEntriesJob.getState() != Job.NONE) {
				fetchLogEntriesJob.cancel();
				try {
					fetchLogEntriesJob.join();
				} catch (InterruptedException e) {
					TeamUIPlugin.log(new TeamException(NLS.bind(TeamUIMessages.GenericHistoryView_ErrorFetchingEntries, new String[] { "" }), e));   //$NON-NLS-1$
				}
			}
		}
		getSite().getPage().removePartListener(partListener);
		getSite().getPage().removePartListener(partListener2);
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
}
