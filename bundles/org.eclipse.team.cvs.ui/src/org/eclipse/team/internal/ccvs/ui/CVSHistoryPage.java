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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.*;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.filehistory.CVSFileHistory;
import org.eclipse.team.internal.ccvs.core.filehistory.CVSFileRevision;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.actions.*;
import org.eclipse.team.internal.ccvs.ui.operations.*;
import org.eclipse.team.internal.core.LocalFileRevision;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.history.HistoryPage;
import org.eclipse.team.ui.history.IHistoryPageSite;
import org.eclipse.ui.*;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

public class CVSHistoryPage extends HistoryPage {
	
	/* private */ ICVSFile file;

	/* private */ IFileRevision currentFileRevision;
	
	// cached for efficiency
	/* private */IFileRevision[] entries;

	protected CVSHistoryTableProvider historyTableProvider;

	/* private */TableViewer tableViewer;
	protected TextViewer textViewer;
	protected TableViewer tagViewer;

	/* private */OpenRevisionAction openAction;
	private CVSHistoryFilterAction  cvsHistoryFilter;
	private IAction toggleTextAction;
	private IAction toggleTextWrapAction;
	private IAction toggleListAction;
	private TextViewerAction copyAction;
	private TextViewerAction selectAllAction;
	private Action getContentsAction;
	private Action getRevisionAction;
	private Action refreshAction;
	private Action tagWithExistingAction;
	
	private SashForm sashForm;
	private SashForm innerSashForm;

	private Image branchImage;
	private Image versionImage;
	
	protected IFileRevision currentSelection;

	protected RefreshCVSFileHistory  refreshCVSFileHistoryJob;

	private IHistoryPageSite parentSite;

	/* private */boolean shutdown = false;

	private HistoryResourceListener resourceListener;

	// preferences
	public final static String PREF_GENERIC_HISTORYVIEW_SHOW_COMMENTS = "pref_generichistory_show_comments"; //$NON-NLS-1$

	public final static String PREF_GENERIC_HISTORYVIEW_WRAP_COMMENTS = "pref_generichistory_wrap_comments"; //$NON-NLS-1$

	public final static String PREF_GENERIC_HISTORYVIEW_SHOW_TAGS = "pref_generichistory_show_tags"; //$NON-NLS-1$

	public CVSHistoryPage(Object object) {
		this.file = getCVSFile(object);
	}

	public void createControl(Composite parent) {
		initializeImages();
		
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
		
		resourceListener = new HistoryResourceListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE);
	}

	private TextViewer createText(SashForm parent) {
		TextViewer result = new TextViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
		result.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				copyAction.update();
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
				if (!(tag instanceof CVSTag))
					return null;
				
				switch (((CVSTag)tag).getType()) {
					case CVSTag.BRANCH:
					case CVSTag.HEAD:
						return branchImage;
					case CVSTag.VERSION:
						return versionImage;
				}
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
				CVSTag tag1 = (CVSTag) e1;
				CVSTag tag2 = (CVSTag) e2;
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

	public void setFocus() {
		sashForm.setFocus();
	}

	protected void contributeActions() {
		CVSUIPlugin plugin = CVSUIPlugin.getPlugin();

		refreshAction = new Action(CVSUIMessages.HistoryView_refreshLabel, plugin.getImageDescriptor(ICVSUIConstants.IMG_REFRESH_ENABLED)) {
			public void run() {
				refresh();
			}
		};
		refreshAction.setToolTipText(CVSUIMessages.HistoryView_refresh); 
		refreshAction.setDisabledImageDescriptor(plugin.getImageDescriptor(ICVSUIConstants.IMG_REFRESH_DISABLED));
		refreshAction.setHoverImageDescriptor(plugin.getImageDescriptor(ICVSUIConstants.IMG_REFRESH));

		// Double click open action
		openAction = new OpenRevisionAction();
		tableViewer.getTable().addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				openAction.selectionChanged(null, tableViewer.getSelection());
				openAction.run(null);
			}
		});

		getContentsAction = getContextMenuAction(CVSUIMessages.HistoryView_getContentsAction, true /* needs progress */, new IWorkspaceRunnable() { 
			public void run(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask(null, 100);
				try {
					if(confirmOverwrite()) {
						IStorage currentStorage = currentSelection.getStorage(new SubProgressMonitor(monitor, 50));
						InputStream in = currentStorage.getContents();
						((IFile)file.getIResource()).setContents(in, false, true, new SubProgressMonitor(monitor, 50));				
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
				ICVSRemoteFile remoteFile = (ICVSRemoteFile) CVSWorkspaceRoot.getRemoteResourceFor(((CVSFileRevision) currentSelection).getCVSRemoteFile());
				try {
					if(confirmOverwrite()) {
						CVSTag revisionTag = new CVSTag(remoteFile.getRevision(), CVSTag.VERSION);
						
						if(CVSAction.checkForMixingTags(getSite().getShell(), new IResource[] {file.getIResource()}, revisionTag)) {
							new UpdateOperation(
									null, 
									new IResource[] {file.getIResource()},
									new Command.LocalOption[] {Update.IGNORE_LOCAL_CHANGES}, 
									revisionTag)
										.run(monitor);
							
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									refresh();
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
							if (next instanceof CVSFileRevision) {
								logEntrieFiles.add(((CVSFileRevision)next).getCVSRemoteFile());
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
		parentSite.getWorkbenchPartSite().registerContextMenu(menuMgr, tableViewer);

		// Contribute toggle text visible to the toolbar drop-down
		IActionBars actionBars = getSite().getActionBars();
		IMenuManager actionBarsMenu = actionBars.getMenuManager();
		actionBarsMenu.add(toggleTextWrapAction);
		actionBarsMenu.add(new Separator());
		actionBarsMenu.add(toggleTextAction);
		actionBarsMenu.add(toggleListAction);
		
		cvsHistoryFilter = new CVSHistoryFilterAction(this);
		cvsHistoryFilter.setImageDescriptor(CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_FILTER_HISTORY));
		cvsHistoryFilter.init(tableViewer);
		//Create the local tool bar
		IToolBarManager tbm = actionBars.getToolBarManager();
		//Take out history support for now
		tbm.add(cvsHistoryFilter);
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

	/* private */ void fillTableMenu(IMenuManager manager) {
		// file actions go first (view file)
		manager.add(new Separator(IWorkbenchActionConstants.GROUP_FILE));
		if (file != null) {
			// Add the "Add to Workspace" action if 1 revision is selected.
			ISelection sel = tableViewer.getSelection();
			if (!sel.isEmpty()) {
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection tempSelection = (IStructuredSelection) sel;
					if (tempSelection.size() == 1) {
						manager.add(getContentsAction);
						if (!(tempSelection.getFirstElement() instanceof LocalFileRevision)) {
							manager.add(getRevisionAction);
							manager.add(new Separator());
							manager.add(tagWithExistingAction);
						}
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
				
				if (!(inputElement instanceof IFileHistory))
					return new Object[0];

				final IFileHistory fileHistory = (IFileHistory) inputElement;
				entries = fileHistory.getFileRevisions();
				
				return entries;
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
				IFileRevision entry = (IFileRevision)ss.getFirstElement();
				textViewer.setDocument(new Document(entry.getComment()));
				tagViewer.setInput(entry.getTags());
			}
		});

		return viewer;
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
					currentSelection = (IFileRevision)o;
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
					CVSUIPlugin.openError(parentSite.getShell(), null, null, e, CVSUIPlugin.LOG_NONTEAM_EXCEPTIONS);
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
		if (file!=null && file.getIResource().exists()) {
			try {
				if(file.isModified(null)) {
					String title = CVSUIMessages.HistoryView_overwriteTitle; 
					String msg = CVSUIMessages.HistoryView_overwriteMsg; 
					final MessageDialog dialog = new MessageDialog(parentSite.getShell(), title, null, msg, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
					final int[] result = new int[1];
					parentSite.getShell().getDisplay().syncExec(new Runnable() {
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
	public void refresh() {
		entries = null;
		BusyIndicator.showWhile(tableViewer.getTable().getDisplay(), new Runnable() {
			public void run() {
				// if a local file was fed to the history view then we will have to refetch the handle
				// to properly display the current revision marker. 
				if(file != null) {
					//Could use Repository Provider to get a FileHistoryProvider which will return an IFileHistory, but
					//we can just create a file history directly
					CVSFileHistory fileHistory = new CVSFileHistory(file);
					historyTableProvider.setFile(fileHistory, file);
					tableViewer.setInput(fileHistory);
				}
				tableViewer.refresh();
			}
		});
	}

	/**
	 * Shows the history for the given IResource in the view.
	 * 
	 * Only files are supported for now.
	 */
	public boolean showHistory(Object object, boolean refetch) {
		ICVSFile cvsFile = getCVSFile(object);
		this.file = cvsFile;
		if (cvsFile == null)
			return false;
		
		CVSFileHistory fileHistory = new CVSFileHistory(cvsFile);
		fileHistory.includeLocalRevisions(true);
		if (refreshCVSFileHistoryJob == null)
			refreshCVSFileHistoryJob = new RefreshCVSFileHistory();
		
		if (refreshCVSFileHistoryJob.getState() != Job.NONE){
			refreshCVSFileHistoryJob.cancel();
			try {
				refreshCVSFileHistoryJob.join();
			} catch (InterruptedException e) {
				TeamUIPlugin.log(new TeamException(NLS.bind(TeamUIMessages.GenericHistoryView_ErrorFetchingEntries, new String[] {""}), e)); //$NON-NLS-1$
			}
		}
		refreshCVSFileHistoryJob.setFileHistory(fileHistory);
		Utils.schedule(refreshCVSFileHistoryJob, /*getSite()*/parentSite.getWorkbenchPartSite());
		return true;
	}

	private ICVSFile getCVSFile(Object object) {
		if (object instanceof IFile){
			return CVSWorkspaceRoot.getCVSFileFor((IFile) object);
		} else if (object instanceof ICVSRemoteFile){
			return (ICVSRemoteFile) object;
		}
		
		return null;
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

	private void initializeImages() {
		CVSUIPlugin plugin = CVSUIPlugin.getPlugin();
		versionImage = plugin.getImageDescriptor(ICVSUIConstants.IMG_PROJECT_VERSION).createImage();
		branchImage = plugin.getImageDescriptor(ICVSUIConstants.IMG_TAG).createImage();
	}
	
	public void dispose() {
		shutdown = true;

		if (resourceListener != null){
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
			resourceListener = null;
		}
		
		if (branchImage != null) {
			branchImage.dispose();
			branchImage = null;
		}
		if (versionImage != null) {
			versionImage.dispose();
			versionImage = null;
		}
		
		//Cancel any incoming 
		if (refreshCVSFileHistoryJob != null) {
			if (refreshCVSFileHistoryJob.getState() != Job.NONE) {
				refreshCVSFileHistoryJob.cancel();
			}
		}
	}

	private class RefreshCVSFileHistory extends Job {
		private CVSFileHistory fileHistory;
	
		public RefreshCVSFileHistory() {
			super(CVSUIMessages.HistoryView_fetchHistoryJob);
		}
		
		public void setFileHistory(CVSFileHistory fileHistory) {
			this.fileHistory = fileHistory;
		}
	

		public IStatus run(IProgressMonitor monitor)  {
			try {
				if (fileHistory != null && !shutdown) {
					fileHistory.refresh(monitor);
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							historyTableProvider.setLocalRevisionsDisplayed(fileHistory.getIncludesExists());
							historyTableProvider.setFile(fileHistory, file);
							tableViewer.setInput(fileHistory);
						}
					});
					
				}
				return Status.OK_STATUS;
			} catch (TeamException e) {
				return e.getStatus();
			} 
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

	private class HistoryResourceListener implements IResourceChangeListener {
		/**
		 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta root = event.getDelta();
			//Safety check for non-managed files that are added with the CVSHistoryPage
			//in view
			if (file == null ||	file.getIResource() == null)
				 return;
			
			IResourceDelta resourceDelta = root.findMember(((IFile)file.getIResource()).getFullPath());
			if (resourceDelta != null){
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						refresh();
					}
				});
			}
		}
	}
	public Control getControl() {
		return sashForm;
	}

	public boolean canShowHistoryFor(Object object) {
		if (object instanceof IResource){
		RepositoryProvider provider = RepositoryProvider.getProvider(((IResource)object).getProject());
		if (provider instanceof CVSTeamProvider)
			return true;
		} else if (object instanceof ICVSRemoteResource){
			return true;
		}
		
		return false;
	}

	public String getName() {
		return file.getName();
	}

	public void setSite(IHistoryPageSite historySite) {
		this.parentSite = historySite;
	}

	/*
	 * Used to reset sorting in CVSHistoryTableProvider for
	 * changes to local revisions displays. Local revisions don't
	 * have a revision id so we need to sort by date when they are
	 * displayed - else we can just sort by revision id.
	 */
	public void setSorter(boolean localDisplayed) {
		historyTableProvider.setLocalRevisionsDisplayed(localDisplayed);
	}
}
