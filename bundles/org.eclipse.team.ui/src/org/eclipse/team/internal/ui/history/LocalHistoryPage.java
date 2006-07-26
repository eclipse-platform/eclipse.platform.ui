/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.history;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IOpenEventListener;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.*;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.core.history.LocalFileHistory;
import org.eclipse.team.internal.core.history.LocalFileRevision;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.actions.CompareRevisionAction;
import org.eclipse.team.internal.ui.actions.OpenRevisionAction;
import org.eclipse.team.ui.history.HistoryPage;
import org.eclipse.team.ui.history.IHistoryPageSite;
import org.eclipse.ui.*;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.progress.IProgressConstants;

import com.ibm.icu.util.Calendar;

public class LocalHistoryPage extends HistoryPage {
	
	/* private */ IFile file;
	/* private */ IFileRevision currentFileRevision;
	
	// cached for efficiency
	/* private */ LocalFileHistory localFileHistory;
	/* private */IFileRevision[] entries;
	
	/* private */ TreeViewer treeViewer;
	
	/* private */boolean shutdown = false;
	
	//grouping on
	private boolean groupingOn;

	//toggle constants for default click action
	private boolean compareMode = false;
	
	protected LocalHistoryTableProvider historyTableProvider;
	private RefreshFileHistory refreshFileHistoryJob;
	private Composite localComposite;
	private Action groupByDateMode;
	private Action collapseAll;
	private Action compareModeAction;
	private Action getContentsAction;
	private CompareRevisionAction compareAction;
	private OpenRevisionAction openAction;
	
	private HistoryResourceListener resourceListener;
	
	private IFileRevision currentSelection;
	
	public boolean inputSet() {
		currentFileRevision = null;
		IFile tempFile = getFile();
		this.file = tempFile;
		if (tempFile == null)
			return false;
		
		//blank current input only after we're sure that we have a file
		//to fetch history for
		this.treeViewer.setInput(null);
		
		localFileHistory = new LocalFileHistory(file);
		
		if (refreshFileHistoryJob == null)
			refreshFileHistoryJob = new RefreshFileHistory();
		
		//always refresh the history if the input gets set
		refreshHistory(true);
		return true;
	}

	private IFile getFile() {
		Object obj = getInput();
		if (obj instanceof IFile)
			return (IFile) obj;
		
		return null;
	}

	private void refreshHistory(boolean refetch) {
		if (refreshFileHistoryJob.getState() != Job.NONE){
			refreshFileHistoryJob.cancel();
		}
		refreshFileHistoryJob.setFileHistory(localFileHistory);
		refreshFileHistoryJob.setGrouping(groupingOn);
		IHistoryPageSite parentSite = getHistoryPageSite();
		Utils.schedule(refreshFileHistoryJob, getWorkbenchSite(parentSite));
	}

	private IWorkbenchPartSite getWorkbenchSite(IHistoryPageSite parentSite) {
		IWorkbenchPart part = parentSite.getPart();
		if (part != null)
			return part.getSite();
		return null;
	}
	
	public void createControl(Composite parent) {

		localComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		localComposite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = true;
		localComposite.setLayoutData(data);
		
		treeViewer = createTree(localComposite);
		
		contributeActions();
		
		IHistoryPageSite parentSite = getHistoryPageSite();
		if (parentSite != null && parentSite instanceof DialogHistoryPageSite && treeViewer != null)
			parentSite.setSelectionProvider(treeViewer);
		
		resourceListener = new HistoryResourceListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE);
	}

	private void contributeActions() {
		final IPreferenceStore store = TeamUIPlugin.getPlugin().getPreferenceStore();
		//Group by Date
		groupByDateMode = new Action(TeamUIMessages.LocalHistoryPage_GroupRevisionsByDateAction, TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_DATES_CATEGORY)){
			public void run() {
				groupingOn = !groupingOn;
				store.setValue(IPreferenceIds.PREF_GROUPBYDATE_MODE, groupingOn);
				refreshHistory(false);
			}
		};
		groupingOn = store.getBoolean(IPreferenceIds.PREF_GROUPBYDATE_MODE);
		groupByDateMode.setChecked(groupingOn);
		groupByDateMode.setToolTipText(TeamUIMessages.LocalHistoryPage_GroupRevisionsByDateTip);
		groupByDateMode.setDisabledImageDescriptor(TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_DATES_CATEGORY));
		groupByDateMode.setHoverImageDescriptor(TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_DATES_CATEGORY));
		
		//Collapse All
		collapseAll =  new Action(TeamUIMessages.LocalHistoryPage_CollapseAllAction, TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_COLLAPSE_ALL)) {
			public void run() {
				treeViewer.collapseAll();
			}
		};
		collapseAll.setToolTipText(TeamUIMessages.LocalHistoryPage_CollapseAllTip); 
		collapseAll.setDisabledImageDescriptor(TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_COLLAPSE_ALL));
		collapseAll.setHoverImageDescriptor(TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_COLLAPSE_ALL));
		
		//Compare Mode Action
		compareModeAction = new Action(TeamUIMessages.LocalHistoryPage_CompareModeAction,TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_COMPARE_VIEW)) {
			public void run() {
				compareMode = !compareMode;
				compareModeAction.setChecked(compareMode);
			}
		};
		compareModeAction.setToolTipText(TeamUIMessages.LocalHistoryPage_CompareModeTip); 
		compareModeAction.setDisabledImageDescriptor(TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_COMPARE_VIEW));
		compareModeAction.setHoverImageDescriptor(TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_COMPARE_VIEW));
		compareModeAction.setChecked(false);
		
		getContentsAction = getContextMenuAction(TeamUIMessages.LocalHistoryPage_GetContents, true /* needs progress */, new IWorkspaceRunnable() { 
			public void run(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask(null, 100);
				try {
					if(confirmOverwrite()) {
						IStorage currentStorage = currentSelection.getStorage(new SubProgressMonitor(monitor, 50));
						InputStream in = currentStorage.getContents();
						(file).setContents(in, false, true, new SubProgressMonitor(monitor, 50));				
					}
				} catch (TeamException e) {
					throw new CoreException(e.getStatus());
				} finally {
					monitor.done();
				}
			}
		});
		//TODO: Doc help
        //PlatformUI.getWorkbench().getHelpSystem().setHelp(getContentsAction, );	

		// Click Compare action
		compareAction = new CompareRevisionAction(TeamUIMessages.LocalHistoryPage_CompareAction);
		treeViewer.getTree().addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				compareAction.setCurrentFileRevision(getCurrentFileRevision());
				compareAction.selectionChanged((IStructuredSelection) treeViewer.getSelection());
			}
		});
		compareAction.setPage(this);
		
		openAction = new OpenRevisionAction(TeamUIMessages.LocalHistoryPage_OpenAction);
		treeViewer.getTree().addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				openAction.selectionChanged((IStructuredSelection) treeViewer.getSelection());
			}
		});
		openAction.setPage(this);
		
		OpenStrategy handler = new OpenStrategy(treeViewer.getTree());
		handler.addOpenListener(new IOpenEventListener() {
		public void handleOpen(SelectionEvent e) {
				StructuredSelection tableStructuredSelection = (StructuredSelection) treeViewer.getSelection();
				if (compareMode){
					StructuredSelection sel = new StructuredSelection(new Object[] {getCurrentFileRevision(), tableStructuredSelection.getFirstElement()});
					compareAction.selectionChanged(sel);
					compareAction.run();
				} else {
					//Pass in the entire structured selection to allow for multiple editor openings
					StructuredSelection sel = tableStructuredSelection;
					openAction.selectionChanged(sel);
					openAction.run();
				}
			}
		});
		
		//Contribute actions to popup menu
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(treeViewer.getTree());
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuMgr) {
				fillTableMenu(menuMgr);
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		treeViewer.getTree().setMenu(menu);
		
		//Don't add the object contribution menu items if this page is hosted in a dialog
		IHistoryPageSite parentSite = getHistoryPageSite();
		if (!parentSite.isModal()) {
			IWorkbenchPart part = parentSite.getPart();
			if (part != null) {
				IWorkbenchPartSite workbenchPartSite = part.getSite();
				workbenchPartSite.registerContextMenu(menuMgr, treeViewer);
			}
			IPageSite pageSite = parentSite.getWorkbenchPageSite();
			if (pageSite != null) {
				IActionBars actionBars = pageSite.getActionBars();
				// Contribute toggle text visible to the toolbar drop-down
				IMenuManager actionBarsMenu = actionBars.getMenuManager();
				if (actionBarsMenu != null){
					actionBarsMenu.removeAll();
				}
				actionBars.updateActionBars();
			}
		}

		//Create the local tool bar
		IToolBarManager tbm = parentSite.getToolBarManager();
		if (tbm != null) {
			String fileNameQualifier = getFileNameQualifier();
			//Add groups
			tbm.add(new Separator(fileNameQualifier+"grouping"));	//$NON-NLS-1$
			tbm.appendToGroup(fileNameQualifier+"grouping", groupByDateMode); //$NON-NLS-1$
			tbm.add(new Separator(fileNameQualifier+"collapse")); //$NON-NLS-1$
			tbm.appendToGroup(fileNameQualifier+"collapse", collapseAll); //$NON-NLS-1$
			tbm.appendToGroup(fileNameQualifier+"collapse", compareModeAction);  //$NON-NLS-1$
			tbm.update(false);
		}
		
	}

	private String getFileNameQualifier() {
		if (file != null)
			return file.getFullPath().toString();
		
		return ""; //$NON-NLS-1$
	}

	protected void fillTableMenu(IMenuManager manager) {
		// file actions go first (view file)
		IHistoryPageSite parentSite = getHistoryPageSite();
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		
		if (file != null && !parentSite.isModal()){
			manager.add(openAction);
			manager.add(compareAction);
			ISelection sel = treeViewer.getSelection();
			if (!sel.isEmpty()) {
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection tempSelection = (IStructuredSelection) sel;
					if (tempSelection.size() == 1) {
						manager.add(new Separator("getContents")); //$NON-NLS-1$
						manager.add(getContentsAction);
					}
				}
			}
		}
	}

	/**
	 * Creates the tree that displays the local file revisions
	 * 
	 * @param parent the parent composite to contain the group
	 * @return the group control
	 */
	protected TreeViewer createTree(Composite parent) {

		historyTableProvider = new LocalHistoryTableProvider();
		TreeViewer viewer = historyTableProvider.createTree(parent);

		viewer.setContentProvider(new ITreeContentProvider() {
			public Object[] getElements(Object inputElement) {

				// The entries of already been fetch so return them
				if (entries != null)
					return entries;
				
				if (!(inputElement instanceof IFileHistory) &&
					!(inputElement instanceof AbstractHistoryCategory[]))
					return new Object[0];

				if (inputElement instanceof AbstractHistoryCategory[]){
					return (AbstractHistoryCategory[]) inputElement;
				}
				
				final IFileHistory fileHistory = (IFileHistory) inputElement;
				entries = fileHistory.getFileRevisions();
			
				return entries;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				entries = null;
			}

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof AbstractHistoryCategory){
					return ((AbstractHistoryCategory) parentElement).getRevisions();
				}
				
				return null;
			}

			public Object getParent(Object element) {
				return null;
			}

			public boolean hasChildren(Object element) {
				if (element instanceof AbstractHistoryCategory){
					return ((AbstractHistoryCategory) element).hasRevisions();
				}
				return false;
			}
		});

		return viewer;
	}

	public Control getControl() {
		return localComposite;
	}

	public void setFocus() {
		localComposite.setFocus();
	}

	public String getDescription() {
		if (file != null)
			return file.getFullPath().toString();
		
		return null;
	}

	public String getName() {
		if (file != null)
			return file.getName();
		
		return ""; //$NON-NLS-1$
	}

	public boolean isValidInput(Object object) {
		//true if object is an unshared file
		if (object instanceof IFile) {
			if (!RepositoryProvider.isShared(((IFile) object).getProject()))
				return true;
		}
		
		return false;
	}

	public void refresh() {
		refreshHistory(true);
	}

	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

    public void dispose() {
    	shutdown = true;

    	if (resourceListener != null){
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
			resourceListener = null;
		}
    	
		//Cancel any incoming 
		if (refreshFileHistoryJob != null) {
			if (refreshFileHistoryJob.getState() != Job.NONE) {
				refreshFileHistoryJob.cancel();
			}
		}
		
		
    }
    
	public IFileRevision getCurrentFileRevision() {
		if (currentFileRevision != null)
			return currentFileRevision;

		if (file != null) 
			currentFileRevision = new LocalFileRevision(file);
				
		return currentFileRevision;
	}
	
	private class RefreshFileHistory extends Job {
		private final static int NUMBER_OF_CATEGORIES = 4;
		
		private LocalFileHistory fileHistory;
		private AbstractHistoryCategory[] categories;
		private boolean grouping;
		private Object[] elementsToExpand;
	
		public RefreshFileHistory() {
			super(TeamUIMessages.LocalHistoryPage_FetchLocalHistoryMessage);
		}
		
		public void setFileHistory(LocalFileHistory fileHistory) {
			this.fileHistory = fileHistory;
		}
	
		public void setGrouping (boolean value){
			this.grouping = value;
		}

		public IStatus run(IProgressMonitor monitor)  {
		
			IStatus status = Status.OK_STATUS;
			
			if (fileHistory != null && !shutdown) {
				//If fileHistory termintates in a bad way, try to fetch the local
				//revisions only
				try {
					fileHistory.refresh(monitor);
				} catch (CoreException ex) {
					status = new TeamStatus(ex.getStatus().getSeverity(), TeamUIPlugin.ID, ex.getStatus().getCode(), ex.getMessage(), ex, file);
				}

				if (grouping)
					sortRevisions();

				Utils.asyncExec(new Runnable() {
					public void run() {
						historyTableProvider.setFile(file);
						if (grouping) {
							mapExpandedElements(treeViewer.getExpandedElements());
							treeViewer.getTree().setRedraw(false);
							treeViewer.setInput(categories);
							//if user is switching modes and already has expanded elements
							//selected try to expand those, else expand all
							if (elementsToExpand.length > 0)
								treeViewer.setExpandedElements(elementsToExpand);
							else {
								treeViewer.expandAll();
								Object[] el = treeViewer.getExpandedElements();
								if (el != null && el.length > 0) {
									treeViewer.setSelection(new StructuredSelection(el[0]));
									treeViewer.getTree().deselectAll();
								}
							}
							treeViewer.getTree().setRedraw(true);
						} else {
							if (fileHistory.getFileRevisions().length > 0) {
								treeViewer.setInput(fileHistory);
							} else {
								categories = new AbstractHistoryCategory[] {getErrorMessage()};
								treeViewer.setInput(categories);
							}
						}
					}
				}, treeViewer);
			}

			if (status != Status.OK_STATUS ) {
				this.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
				this.setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE);
			}
			
			return status;
		}

		private void mapExpandedElements(Object[] expandedElements) {
			//store the names of the currently expanded categories in a map
			HashMap elementMap = new HashMap();
			for (int i=0; i<expandedElements.length; i++){
				elementMap.put(((DateHistoryCategory)expandedElements[i]).getName(), null);
			}
			
			//Go through the new categories and keep track of the previously expanded ones
			ArrayList expandable = new ArrayList();
			for (int i = 0; i<categories.length; i++){
				//check to see if this category is currently expanded
				if (elementMap.containsKey(categories[i].getName())){
					expandable.add(categories[i]);
				}
			}
			
			elementsToExpand = new Object[expandable.size()];
			elementsToExpand = (Object[]) expandable.toArray(new Object[expandable.size()]);
		}

		private boolean sortRevisions() {
			IFileRevision[] fileRevision = fileHistory.getFileRevisions();
			
			//Create the 4 categories
			DateHistoryCategory[] tempCategories = new DateHistoryCategory[NUMBER_OF_CATEGORIES];
			//Get a calendar instance initialized to the current time
			Calendar currentCal = Calendar.getInstance();
			tempCategories[0] = new DateHistoryCategory(TeamUIMessages.HistoryPage_Today, currentCal, null);
			//Get yesterday 
			Calendar yesterdayCal = Calendar.getInstance();
			yesterdayCal.roll(Calendar.DAY_OF_YEAR, -1);
			tempCategories[1] = new DateHistoryCategory(TeamUIMessages.HistoryPage_Yesterday, yesterdayCal, null);
			//Get this month
			Calendar monthCal = Calendar.getInstance();
			monthCal.set(Calendar.DAY_OF_MONTH, 1);
			tempCategories[2] = new DateHistoryCategory(TeamUIMessages.HistoryPage_ThisMonth, monthCal, yesterdayCal);
			//Everything before after week is previous
			tempCategories[3] = new DateHistoryCategory(TeamUIMessages.HistoryPage_Previous, null, monthCal);
		
			ArrayList finalCategories = new ArrayList();
			for (int i = 0; i<NUMBER_OF_CATEGORIES; i++){
				tempCategories[i].collectFileRevisions(fileRevision, false);
				if (tempCategories[i].hasRevisions())
					finalCategories.add(tempCategories[i]);
			}
			
			//Assume that some revisions have been found
			boolean revisionsFound = true;
			
			if (finalCategories.size() == 0){
				//no revisions found for the current mode, so add a message category
				finalCategories.add(getErrorMessage());
				revisionsFound = false;
			}
			
			categories = (AbstractHistoryCategory[])finalCategories.toArray(new AbstractHistoryCategory[finalCategories.size()]);
			return revisionsFound;
		}
		
		private MessageHistoryCategory getErrorMessage(){
			MessageHistoryCategory messageCategory = new MessageHistoryCategory(TeamUIMessages.LocalHistoryPage_NoRevisionsFound);
			return messageCategory;
		}
	}
	
	private class HistoryResourceListener implements IResourceChangeListener {
		/**
		 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta root = event.getDelta();
		
			if (file == null)
				 return;
			
			IResourceDelta resourceDelta = root.findMember(file.getFullPath());
			if (resourceDelta != null){
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						refresh();
					}
				});
			}
		}
	}
	
	private Action getContextMenuAction(String title, final boolean needsProgressDialog, final IWorkspaceRunnable action) {
		return new Action(title) {
			public void run() {
				try {
					if (file == null) return;
					ISelection selection = treeViewer.getSelection();
					if (!(selection instanceof IStructuredSelection)) return;
					IStructuredSelection ss = (IStructuredSelection)selection;
					Object o = ss.getFirstElement();
					
					if (o instanceof AbstractHistoryCategory)
						return;
					
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
					IHistoryPageSite parentSite = getHistoryPageSite();
					Utils.handleError(parentSite.getShell(), e, null, null);
				} catch (InterruptedException e) {
					// Do nothing
				}
			}
			
			public boolean isEnabled() {
				ISelection selection = treeViewer.getSelection();
				if (!(selection instanceof IStructuredSelection)) return false;
				IStructuredSelection ss = (IStructuredSelection)selection;
				if(ss.size() != 1) return false;
				return true;
			}
		};
	}

	private boolean confirmOverwrite() {
		if (file != null && file.exists()) {
			String title = TeamUIMessages.LocalHistoryPage_OverwriteTitle;
			String msg = TeamUIMessages.LocalHistoryPage_OverwriteMessage;
			IHistoryPageSite parentSite = getHistoryPageSite();
			final MessageDialog dialog = new MessageDialog(parentSite.getShell(), title, null, msg, MessageDialog.QUESTION, new String[] {IDialogConstants.YES_LABEL, IDialogConstants.CANCEL_LABEL}, 0);
			final int[] result = new int[1];
			parentSite.getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					result[0] = dialog.open();
				}
			});
			if (result[0] != 0) {
				// cancel
				return false;
			}
		}
		return true;
	}

	
	
}
