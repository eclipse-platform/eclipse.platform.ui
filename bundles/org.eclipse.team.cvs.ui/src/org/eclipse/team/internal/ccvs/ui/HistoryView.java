/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

 
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
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
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.actions.CVSAction;
import org.eclipse.team.internal.ccvs.ui.actions.MoveRemoteTagAction;
import org.eclipse.team.internal.ccvs.ui.actions.OpenLogEntryAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;
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
	private CVSTeamProvider provider;
	
	private HistoryTableProvider historyTableProvider;
	
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
	private Action refreshAction;
	private Action tagWithExistingAction;
	
	private SashForm sashForm;
	private SashForm innerSashForm;

	private Image branchImage;
	private Image versionImage;
	
	private ILogEntry currentSelection;
	
	public static final String VIEW_ID = "org.eclipse.team.ccvs.ui.HistoryView"; //$NON-NLS-1$
	
	/**
	 * Adds the action contributions for this view.
	 */
	protected void contributeActions() {
		// Refresh (toolbar)
		CVSUIPlugin plugin = CVSUIPlugin.getPlugin();
		refreshAction = new Action(Policy.bind("HistoryView.refreshLabel"), plugin.getImageDescriptor(ICVSUIConstants.IMG_REFRESH_ENABLED)) { //$NON-NLS-1$
			public void run() {
				refresh();
			}
		};
		refreshAction.setToolTipText(Policy.bind("HistoryView.refresh")); //$NON-NLS-1$
		refreshAction.setDisabledImageDescriptor(plugin.getImageDescriptor(ICVSUIConstants.IMG_REFRESH_DISABLED));
		refreshAction.setHoverImageDescriptor(plugin.getImageDescriptor(ICVSUIConstants.IMG_REFRESH));
		
		// Double click open action
		openAction = new OpenLogEntryAction();
		tableViewer.getTable().addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				openAction.selectionChanged(null, tableViewer.getSelection());
				openAction.run(null);
			}
		});

		getContentsAction = getContextMenuAction(Policy.bind("HistoryView.getContentsAction"), true /* needs progress */, new IWorkspaceRunnable() { //$NON-NLS-1$
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
		WorkbenchHelp.setHelp(getContentsAction, IHelpContextIds.GET_FILE_CONTENTS_ACTION);	

		getRevisionAction = getContextMenuAction(Policy.bind("HistoryView.getRevisionAction"), true /* needs progress */, new IWorkspaceRunnable() { //$NON-NLS-1$
			public void run(IProgressMonitor monitor) throws CoreException {
				ICVSRemoteFile remoteFile = currentSelection.getRemoteFile();
				try {
					if(confirmOverwrite()) {
						CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(file.getProject());
						CVSTag revisionTag = new CVSTag(remoteFile.getRevision(), CVSTag.VERSION);
						
						if(CVSAction.checkForMixingTags(getSite().getShell(), new IResource[] {file}, revisionTag)) {							
							provider.update(new IResource[] {file}, new Command.LocalOption[] {Update.IGNORE_LOCAL_CHANGES}, 
													   revisionTag, true /*create backups*/, monitor);
							historyTableProvider.setFile(remoteFile);
							tableViewer.refresh();
						}
					}
				} catch (TeamException e) {
					throw new CoreException(e.getStatus());
				}
			}
		});
		WorkbenchHelp.setHelp(getRevisionAction, IHelpContextIds.GET_FILE_REVISION_ACTION);	

		// Override MoveRemoteTagAction to work for log entries
		final IActionDelegate tagActionDelegate = new MoveRemoteTagAction() {
			protected ICVSResource[] getSelectedCVSResources() {
				ICVSResource[] resources = super.getSelectedCVSResources();
				if (resources == null || resources.length == 0) {
					ArrayList logEntrieFiles = null;
					if (!selection.isEmpty()) {
						logEntrieFiles = new ArrayList();
						Iterator elements = ((IStructuredSelection) selection).iterator();
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
		};
		tagWithExistingAction = getContextMenuAction(Policy.bind("HistoryView.tagWithExistingAction"), false /* no progress */, new IWorkspaceRunnable() { //$NON-NLS-1$
			public void run(IProgressMonitor monitor) throws CoreException {
				tagActionDelegate.selectionChanged(tagWithExistingAction, tableViewer.getSelection());
				tagActionDelegate.run(tagWithExistingAction);
				if( ! ((MoveRemoteTagAction)tagActionDelegate).wasCancelled()) {
					refresh();
				}
			}
		});
		WorkbenchHelp.setHelp(getRevisionAction, IHelpContextIds.TAG_WITH_EXISTING_ACTION);	
				
		// Toggle text visible action
		final IPreferenceStore store = CVSUIPlugin.getPlugin().getPreferenceStore();
		toggleTextAction = new Action(Policy.bind("HistoryView.showComment")) { //$NON-NLS-1$
			public void run() {
				setViewerVisibility();
				store.setValue(ICVSUIConstants.PREF_SHOW_COMMENTS, toggleTextAction.isChecked());
			}
		};
		toggleTextAction.setChecked(store.getBoolean(ICVSUIConstants.PREF_SHOW_COMMENTS));
		WorkbenchHelp.setHelp(toggleTextAction, IHelpContextIds.SHOW_COMMENT_IN_HISTORY_ACTION);	
		// Toggle list visible action
		toggleListAction = new Action(Policy.bind("HistoryView.showTags")) { //$NON-NLS-1$
			public void run() {
				setViewerVisibility();
				store.setValue(ICVSUIConstants.PREF_SHOW_TAGS, toggleListAction.isChecked());
			}
		};
		toggleListAction.setChecked(store.getBoolean(ICVSUIConstants.PREF_SHOW_TAGS));
		WorkbenchHelp.setHelp(toggleListAction, IHelpContextIds.SHOW_TAGS_IN_HISTORY_ACTION);	
		
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
		contributeActions();
		setViewerVisibility();
		// set F1 help
		WorkbenchHelp.setHelp(sashForm, IHelpContextIds.RESOURCE_HISTORY_VIEW);
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
		
		historyTableProvider = new HistoryTableProvider();
		TableViewer viewer = historyTableProvider.createTable(parent);
		
		viewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				// Short-circuit to optimize
				if (entries != null) return entries;
				
				if (!(inputElement instanceof ICVSRemoteFile)) return null;
				final ICVSRemoteFile remoteFile = (ICVSRemoteFile)inputElement;
				final Object[][] result = new Object[1][];
				try {
					new ProgressMonitorDialog(getViewer().getTable().getShell()).run(true, true, new IRunnableWithProgress() {
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
					CVSUIPlugin.openError(getViewSite().getShell(), null, null, e);
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
					// for a file this will return the base
					ICVSRemoteFile remoteFile = (ICVSRemoteFile)CVSWorkspaceRoot.getRemoteResourceFor(file);
					historyTableProvider.setFile(remoteFile);
					tableViewer.setInput(remoteFile);
					setTitle(Policy.bind("HistoryView.titleWithArgument", remoteFile.getName())); //$NON-NLS-1$
				} catch (TeamException e) {
					CVSUIPlugin.openError(getViewSite().getShell(), null, null, e);
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
	public void showHistory(ICVSRemoteFile remoteFile) {
		try {
			if (remoteFile == null) {
				tableViewer.setInput(null);
				setTitle(Policy.bind("HistoryView.title")); //$NON-NLS-1$
				return;
			}
			this.file = null;
			historyTableProvider.setFile(remoteFile);
			tableViewer.setInput(remoteFile);
			setTitle(Policy.bind("HistoryView.titleWithArgument", remoteFile.getName())); //$NON-NLS-1$
		} catch (CVSException e) {
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
						new ProgressMonitorDialog(getViewSite().getShell()).run(false, true, new WorkspaceModifyOperation() {
							protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								try {				
									action.run(monitor);
								} catch (CoreException e) {
									throw new InvocationTargetException(e);
								}
							}
						});
					}	else {
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
}
