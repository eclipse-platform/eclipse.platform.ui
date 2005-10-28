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
package org.eclipse.team.internal.ccvs.ui;

 
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.actions.*;
import org.eclipse.team.internal.ccvs.ui.operations.*;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.SyncInfoCompareInput;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * The history view allows browsing of an array of resource revisions
 */
public class HistoryView extends ViewPart {
	private IFile file;
	// cached for efficiency
	private ILogEntry[] entries;
	
	private HistoryTableProvider historyTableProvider;
	
	private TableViewer tableViewer;
	private TextViewer textViewer;
	private TableViewer tagViewer;
	
	private OpenLogEntryAction openAction;
	private IAction toggleTextAction;
	private IAction toggleTextWrapAction;
	private IAction toggleListAction;
	private TextViewerAction copyAction;
	private TextViewerAction selectAllAction;
	private Action getContentsAction;
	private Action getRevisionAction;
	private Action refreshAction;
	private Action tagWithExistingAction;
	private Action linkWithEditorAction;
	
	private SashForm sashForm;
	private SashForm innerSashForm;

	private Image branchImage;
	private Image versionImage;
	
	private ILogEntry currentSelection;
	private boolean linkingEnabled;
	
	private IPreferenceStore settings;
	
	private FetchLogEntriesJob fetchLogEntriesJob;
	
	private boolean shutdown = false;
	
	public static final String VIEW_ID = "org.eclipse.team.ccvs.ui.HistoryView"; //$NON-NLS-1$
	
	private IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
			if (part instanceof IEditorPart)
				editorActivated((IEditorPart) part);
		}
		public void partBroughtToTop(IWorkbenchPart part) {
			if(part == HistoryView.this)
				editorActivated(getViewSite().getPage().getActiveEditor());
		}
		public void partOpened(IWorkbenchPart part) {
			if(part == HistoryView.this)
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
			if(ref.getPart(true) == HistoryView.this)
				editorActivated(getViewSite().getPage().getActiveEditor());
		}
		public void partInputChanged(IWorkbenchPartReference ref) {
		}
	};


	private class FetchLogEntriesJob extends Job {
		public ICVSRemoteFile remoteFile;
		public FetchLogEntriesJob() {
			super(CVSUIMessages.HistoryView_fetchHistoryJob);  //;
		}
		public void setRemoteFile(ICVSRemoteFile file) {
			this.remoteFile = file;
		}
		public IStatus run(IProgressMonitor monitor) {
			try {
				if(remoteFile != null && !shutdown) {
					entries = remoteFile.getLogEntries(monitor);
					final String revisionId = remoteFile.getRevision();
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if(entries != null && tableViewer != null && ! tableViewer.getTable().isDisposed()) {
								tableViewer.refresh();
								selectRevision(revisionId);
							}
						}
					});
				}
				return Status.OK_STATUS;
			} catch (TeamException e) {
				return e.getStatus();
			}
		}
		public ICVSRemoteFile getRemoteFile() {
			return this.remoteFile;
		}
	};
	
	/**
	 * Adds the action contributions for this view.
	 */
	protected void contributeActions() {
		// Refresh (toolbar)
		CVSUIPlugin plugin = CVSUIPlugin.getPlugin();
		refreshAction = new Action(CVSUIMessages.HistoryView_refreshLabel, plugin.getImageDescriptor(ICVSUIConstants.IMG_REFRESH_ENABLED)) { 
			public void run() {
				refresh();
			}
		};
		refreshAction.setToolTipText(CVSUIMessages.HistoryView_refresh); 
		refreshAction.setDisabledImageDescriptor(plugin.getImageDescriptor(ICVSUIConstants.IMG_REFRESH_DISABLED));
		refreshAction.setHoverImageDescriptor(plugin.getImageDescriptor(ICVSUIConstants.IMG_REFRESH));
		
		//	Link with Editor (toolbar)
		 linkWithEditorAction = new Action(CVSUIMessages.HistoryView_linkWithLabel, plugin.getImageDescriptor(ICVSUIConstants.IMG_LINK_WITH_EDITOR_ENABLED)) { 
			 public void run() {
				 setLinkingEnabled(isChecked());
			 }
		 };
		linkWithEditorAction.setToolTipText(CVSUIMessages.HistoryView_linkWithLabel); 
		linkWithEditorAction.setHoverImageDescriptor(plugin.getImageDescriptor(ICVSUIConstants.IMG_LINK_WITH_EDITOR));
		linkWithEditorAction.setChecked(isLinkingEnabled());
		
		// Double click open action
		openAction = new OpenLogEntryAction();
		tableViewer.getTable().addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				openAction.selectionChanged(null, tableViewer.getSelection());
				openAction.run(null);
			}
		});

		getContentsAction = getContextMenuAction(CVSUIMessages.HistoryView_getContentsAction, true /* needs progress */, new IWorkspaceRunnable() { 
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
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getContentsAction, IHelpContextIds.GET_FILE_CONTENTS_ACTION);	

		getRevisionAction = getContextMenuAction(CVSUIMessages.HistoryView_getRevisionAction, true /* needs progress */, new IWorkspaceRunnable() { 
			public void run(IProgressMonitor monitor) throws CoreException {
				ICVSRemoteFile remoteFile = currentSelection.getRemoteFile();
				try {
					if(confirmOverwrite()) {
						CVSTag revisionTag = new CVSTag(remoteFile.getRevision(), CVSTag.VERSION);
						
						if(CVSAction.checkForMixingTags(getSite().getShell(), new IResource[] {file}, revisionTag)) {
							new UpdateOperation(
									null, 
									new IResource[] {file},
									new Command.LocalOption[] {Update.IGNORE_LOCAL_CHANGES}, 
									revisionTag)
										.run(monitor);
							historyTableProvider.setFile(remoteFile);
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									tableViewer.refresh();
								}
							});
						}
					}
				} catch (InvocationTargetException e) {
					CVSException.wrapException(e);
				} catch (InterruptedException e) {
					// Cancelled by user
				}
			}
		});
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getRevisionAction, IHelpContextIds.GET_FILE_REVISION_ACTION);	

		// Override MoveRemoteTagAction to work for log entries
		final IActionDelegate tagActionDelegate = new MoveRemoteTagAction() {
			protected ICVSResource[] getSelectedCVSResources() {
				ICVSResource[] resources = super.getSelectedCVSResources();
				if (resources == null || resources.length == 0) {
					ArrayList logEntrieFiles = null;
					IStructuredSelection selection = getSelection();
					if (!selection.isEmpty()) {
						logEntrieFiles = new ArrayList();
						Iterator elements = selection.iterator();
						while (elements.hasNext()) {
							Object next = elements.next();
							if (next instanceof ILogEntry) {
								logEntrieFiles.add(((ILogEntry)next).getRemoteFile());
								continue;
							}
							if (next instanceof IAdaptable) {
								IAdaptable a = (IAdaptable) next;
								Object adapter = a.getAdapter(ICVSResource.class);
								if (adapter instanceof ICVSResource) {
									logEntrieFiles.add(((ILogEntry)adapter).getRemoteFile());
									continue;
								}
							}
						}
					}
					if (logEntrieFiles != null && !logEntrieFiles.isEmpty()) {
						return (ICVSResource[])logEntrieFiles.toArray(new ICVSResource[logEntrieFiles.size()]);
					}
				}
				return resources;
			}
            /*
             * Override the creation of the tag operation in order to support
             * the refresh of the view after the tag operation completes
             */
            protected ITagOperation createTagOperation() {
                return new TagInRepositoryOperation(getTargetPart(), getSelectedRemoteResources()) {
                    public void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
                        super.execute(monitor);
                        Display.getDefault().asyncExec(new Runnable() {
                            public void run() {
                                if( ! wasCancelled()) {
                                    refresh();
                                }
                            }
                        });
                    };
                };
            }
		};
		tagWithExistingAction = getContextMenuAction(CVSUIMessages.HistoryView_tagWithExistingAction, false /* no progress */, new IWorkspaceRunnable() { 
			public void run(IProgressMonitor monitor) throws CoreException {
				tagActionDelegate.selectionChanged(tagWithExistingAction, tableViewer.getSelection());
				tagActionDelegate.run(tagWithExistingAction);
			}
		});
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getRevisionAction, IHelpContextIds.TAG_WITH_EXISTING_ACTION);	
				
		// Toggle text visible action
		final IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		toggleTextAction = new Action(CVSUIMessages.HistoryView_showComment) { 
			public void run() {
				setViewerVisibility();
				store.setValue(ICVSUIConstants.PREF_SHOW_COMMENTS, toggleTextAction.isChecked());
			}
		};
		toggleTextAction.setChecked(store.getBoolean(ICVSUIConstants.PREF_SHOW_COMMENTS));
        PlatformUI.getWorkbench().getHelpSystem().setHelp(toggleTextAction, IHelpContextIds.SHOW_COMMENT_IN_HISTORY_ACTION);	

        // Toggle wrap comments action
        toggleTextWrapAction = new Action(CVSUIMessages.HistoryView_wrapComment) { 
          public void run() {
            setViewerVisibility();
            store.setValue(ICVSUIConstants.PREF_WRAP_COMMENTS, toggleTextWrapAction.isChecked());
          }
        };
        toggleTextWrapAction.setChecked(store.getBoolean(ICVSUIConstants.PREF_WRAP_COMMENTS));
        //PlatformUI.getWorkbench().getHelpSystem().setHelp(toggleTextWrapAction, IHelpContextIds.SHOW_TAGS_IN_HISTORY_ACTION);   
		
        // Toggle list visible action
		toggleListAction = new Action(CVSUIMessages.HistoryView_showTags) { 
			public void run() {
				setViewerVisibility();
				store.setValue(ICVSUIConstants.PREF_SHOW_TAGS, toggleListAction.isChecked());
			}
		};
		toggleListAction.setChecked(store.getBoolean(ICVSUIConstants.PREF_SHOW_TAGS));
        PlatformUI.getWorkbench().getHelpSystem().setHelp(toggleListAction, IHelpContextIds.SHOW_TAGS_IN_HISTORY_ACTION);	
		
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
		actionBarsMenu.add(toggleTextWrapAction);
		actionBarsMenu.add(new Separator());
		actionBarsMenu.add(toggleTextAction);
		actionBarsMenu.add(toggleListAction);

		// Create the local tool bar
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
		tbm.add(refreshAction);
		tbm.add(linkWithEditorAction);
		tbm.update(false);
	
		// Create actions for the text editor
		copyAction = new TextViewerAction(textViewer, ITextOperationTarget.COPY);
		copyAction.setText(CVSUIMessages.HistoryView_copy); 
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.COPY, copyAction);
		
		selectAllAction = new TextViewerAction(textViewer, ITextOperationTarget.SELECT_ALL);
		selectAllAction.setText(CVSUIMessages.HistoryView_selectAll); 
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
        
		boolean wrapText = toggleTextWrapAction.isChecked();
        textViewer.getTextWidget().setWordWrap(wrapText);
	}
	/*
	 * Method declared on IWorkbenchPart
	 */
	public void createPartControl(Composite parent) {
		settings = CVSUIPlugin.getPlugin().getPreferenceStore();
		this.linkingEnabled = settings.getBoolean(ICVSUIConstants.PREF_HISTORY_VIEW_EDITOR_LINKING);

		initializeImages();
		
		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		tableViewer = createTable(sashForm);
		innerSashForm = new SashForm(sashForm, SWT.HORIZONTAL);
		tagViewer = createTagTable(innerSashForm);
		textViewer = createText(innerSashForm);
		sashForm.setWeights(new int[] { 70, 30 });
		innerSashForm.setWeights(new int[] { 50, 50 });
		
		contributeActions();
		
		setViewerVisibility();
		
		// set F1 help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(sashForm, IHelpContextIds.RESOURCE_HISTORY_VIEW);
		initDragAndDrop();
		 
		// add listener for editor page activation - this is to support editor linking
		getSite().getPage().addPartListener(partListener);	
		getSite().getPage().addPartListener(partListener2);	
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
		
		historyTableProvider = new HistoryTableProvider();
		TableViewer viewer = historyTableProvider.createTable(parent);
		
		viewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				
				// The entries of already been fetch so return them
				if (entries != null) return entries;
				
				// The entries need to be fetch (or are being fetched)
				if (!(inputElement instanceof ICVSRemoteFile)) return null;
				final ICVSRemoteFile remoteFile = (ICVSRemoteFile)inputElement;
				if(fetchLogEntriesJob == null) {
					fetchLogEntriesJob = new FetchLogEntriesJob();
				} 
				ICVSRemoteFile file = fetchLogEntriesJob.getRemoteFile();
				if (file == null || !file.equals(remoteFile)) {
					// The resource has changed so stop the currently running job
					if(fetchLogEntriesJob.getState() != Job.NONE) {
						fetchLogEntriesJob.cancel();
						try {
							fetchLogEntriesJob.join();
						} catch (InterruptedException e) {
							CVSUIPlugin.log(new CVSException(NLS.bind(CVSUIMessages.HistoryView_errorFetchingEntries, new String[] { remoteFile.getName() }), e)); 
						}
					}
					fetchLogEntriesJob.setRemoteFile(remoteFile);
				}
				// Schedule the job even if it is already running
				Utils.schedule(fetchLogEntriesJob, getViewSite());
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
		shutdown = true;
		if (branchImage != null) {
			branchImage.dispose();
			branchImage = null;
		}
		if (versionImage != null) {
			versionImage.dispose();
			versionImage = null;
		}
		
		if(fetchLogEntriesJob != null) {
			if(fetchLogEntriesJob.getState() != Job.NONE) {
				fetchLogEntriesJob.cancel();
				try {
					fetchLogEntriesJob.join();
				} catch (InterruptedException e) {
					CVSUIPlugin.log(new CVSException(NLS.bind(CVSUIMessages.HistoryView_errorFetchingEntries, new String[] { "" }), e)); //$NON-NLS-1$ 
				}
			}
		}
		getSite().getPage().removePartListener(partListener);
		getSite().getPage().removePartListener(partListener2);
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
		Transfer[] transfers = new Transfer[] {ResourceTransfer.getInstance(), CVSResourceTransfer.getInstance()};
		tableViewer.addDropSupport(ops, transfers, new HistoryDropAdapter(tableViewer, this));
	}
	private void fillTableMenu(IMenuManager manager) {
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
						manager.add(new Separator());
						manager.add(tagWithExistingAction);
					}
				}
			}
		}
		manager.add(new Separator("additions")); //$NON-NLS-1$
		manager.add(refreshAction);
		manager.add(new Separator("additions-end")); //$NON-NLS-1$
	}
	private void fillTextMenu(IMenuManager manager) {
		manager.add(copyAction);
		manager.add(selectAllAction);
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
	public void showHistory(IResource resource, boolean refetch) {
		if (resource instanceof IFile) {
			IFile newfile = (IFile)resource;
			if(!refetch && this.file != null && newfile.equals(this.file)) {
				return;
			} 
			this.file = newfile;
			RepositoryProvider teamProvider = RepositoryProvider.getProvider(file.getProject(), CVSProviderPlugin.getTypeId());
			if (teamProvider != null) {
				try {
					// for a file this will return the base
					ICVSRemoteFile remoteFile = (ICVSRemoteFile)CVSWorkspaceRoot.getRemoteResourceFor(file);
					if(remoteFile != null) {
						historyTableProvider.setFile(remoteFile);
						// input is set asynchronously so we can't assume that the view
						// has been populated until the job that queries for the history
						// has completed.
						tableViewer.setInput(remoteFile);
						setContentDescription(remoteFile.getName()); 
						setTitleToolTip(resource.getFullPath().toString());
					}
				} catch (TeamException e) {
					CVSUIPlugin.openError(getViewSite().getShell(), null, null, e);
				}				
			}
		} else {
			this.file = null;
			tableViewer.setInput(null);
			setContentDescription(""); //$NON-NLS-1$
			setTitleToolTip(""); //$NON-NLS-1$
		}
	}
	
	/**
	 * An editor has been activated.  Fetch the history if it is shared with CVS and the history view
	 * is visible in the current page.
	 * 
	 * @param editor the active editor
	 * @since 3.0
	 */
	protected void editorActivated(IEditorPart editor) {
		// Only fetch contents if the view is shown in the current page.
		if (editor == null || !isLinkingEnabled() || !checkIfPageIsVisible()) {
			return;
		}		
		IEditorInput input = editor.getEditorInput();
		// Handle compare editors opened from the Synchronize View
		if (input instanceof SyncInfoCompareInput) {
			SyncInfoCompareInput syncInput = (SyncInfoCompareInput) input;
			SyncInfo info = syncInput.getSyncInfo();
			if(info instanceof CVSSyncInfo && info.getLocal().getType() == IResource.FILE) {
                // Highlight the loaded versions unless there isn't one
                ICVSRemoteFile loaded;
                try {
                    loaded = (ICVSRemoteFile)CVSWorkspaceRoot.getRemoteResourceFor(info.getLocal());
                } catch (CVSException e) {
                    CVSUIPlugin.log(e);
                    loaded = null;
                }
                if (loaded != null) {
                    showHistory(loaded, false);
                } else {
    				ICVSRemoteFile remote = (ICVSRemoteFile)info.getRemote();
    				ICVSRemoteFile base = (ICVSRemoteFile)info.getBase();
                    if(remote != null) {
                        showHistory(remote, false);
                    } else if(base != null) {
                        showHistory(base, false);
                    }
                }
			}
		// Handle editors opened on remote files
		} else if(input instanceof RemoteFileEditorInput) {
			ICVSRemoteFile remote = ((RemoteFileEditorInput)input).getCVSRemoteFile();
			if(remote != null) {
				showHistory(remote, false);
			}
		// Handle regular file editors
		} else {
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
	 * Shows the history for the given ICVSRemoteFile in the view.
	 */
	public void showHistory(ICVSRemoteFile remoteFile, boolean refetch) {
		try {
			if (remoteFile == null) {
				tableViewer.setInput(null);
				setContentDescription(""); //$NON-NLS-1$
				setTitleToolTip(""); //$NON-NLS-1$
				return;
			}
			ICVSFile existingFile = historyTableProvider.getICVSFile(); 
			if(!refetch && existingFile != null && existingFile.equals(remoteFile)) return;
			this.file = null;
			historyTableProvider.setFile(remoteFile);
			tableViewer.setInput(remoteFile);
			setContentDescription(remoteFile.getName());
			setTitleToolTip(remoteFile.getRepositoryRelativePath());
		} catch (TeamException e) {
			CVSUIPlugin.openError(getViewSite().getShell(), null, null, e);
		} 
	}
	
	private Action getContextMenuAction(String title, final boolean needsProgressDialog, final IWorkspaceRunnable action) {
			return new Action(title) {
			public void run() {
				try {
					if (file == null) return;
					ISelection selection = tableViewer.getSelection();
					if (!(selection instanceof IStructuredSelection)) return;
					IStructuredSelection ss = (IStructuredSelection)selection;
					Object o = ss.getFirstElement();
					currentSelection = (ILogEntry)o;
					if(needsProgressDialog) {
						PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								try {				
									action.run(monitor);
								} catch (CoreException e) {
									throw new InvocationTargetException(e);
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
					CVSUIPlugin.openError(getViewSite().getShell(), null, null, e, CVSUIPlugin.LOG_NONTEAM_EXCEPTIONS);
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
				if(cvsFile.isModified(null)) {
					String title = CVSUIMessages.HistoryView_overwriteTitle; 
					String msg = CVSUIMessages.HistoryView_overwriteMsg; 
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
				CVSUIPlugin.log(e);
			}
		}
		return true;
	}
	
	/*
	 * Refresh the view by refetching the log entries for the remote file
	 */
	private void refresh() {
		entries = null;
		BusyIndicator.showWhile(tableViewer.getTable().getDisplay(), new Runnable() {
			public void run() {
				// if a local file was fed to the history view then we will have to refetch the handle
				// to properly display the current revision marker. 
				if(file != null) {
					 ICVSRemoteFile remoteFile;
					try {
						remoteFile = (ICVSRemoteFile) CVSWorkspaceRoot.getRemoteResourceFor(file);
						historyTableProvider.setFile(remoteFile);
					} catch (CVSException e) {
						// use previously fetched remote file, but log error
						CVSUIPlugin.log(e);
					}
				}
				tableViewer.refresh();
			}
		});
	}
	
	/**
	 * Select the revision in the receiver.
	 */
	public void selectRevision(String revision) {
		if (entries == null) {
			return;
		}
	
		ILogEntry entry = null;
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getRevision().equals(revision)) {
				entry = entries[i];
				break;
			}
		}
	
		if (entry != null) {
			IStructuredSelection selection = new StructuredSelection(entry);
			tableViewer.setSelection(selection, true);
		}
	}
	
	/**
	 * Enabled linking to the active editor
	 * @since 3.0
	 */
	public void setLinkingEnabled(boolean enabled) {
		this.linkingEnabled = enabled;

		// remember the last setting in the dialog settings		
		settings.setValue(ICVSUIConstants.PREF_HISTORY_VIEW_EDITOR_LINKING, enabled);
	
		// if turning linking on, update the selection to correspond to the active editor
		if (enabled) {
			editorActivated(getSite().getPage().getActiveEditor());
		}
	}
	
	/**
	 * Returns if linking to the ative editor is enabled or disabled.
	 * @return boolean indicating state of editor linking.
	 */
	private boolean isLinkingEnabled() {
		return linkingEnabled;
	}
	
	/*
	 * Flatten the text in the multiline comment
	 */
	public static String flattenText(String string) {
		StringBuffer buffer = new StringBuffer(string.length() + 20);
		boolean skipAdjacentLineSeparator = true;
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (c == '\r' || c == '\n') {
				if (!skipAdjacentLineSeparator)
					buffer.append(CVSUIMessages.separator); 
				skipAdjacentLineSeparator = true;
			} else {
				buffer.append(c);
				skipAdjacentLineSeparator = false;
			}
		}
		return buffer.toString();
	}
}
