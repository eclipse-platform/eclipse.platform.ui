package org.eclipse.team.internal.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.compare.BufferedContent;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffContainer;
import org.eclipse.compare.structuremergeviewer.DiffElement;
import org.eclipse.compare.structuremergeviewer.DiffTreeViewer;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.UIConstants;
import org.eclipse.team.ui.TeamUIPlugin;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.views.navigator.ResourceNavigator;
import org.eclipse.ui.views.navigator.ShowInNavigatorAction;

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * 
 * This viewer adds a custom filter and some merge actions.
 * Note this is a layer breaker and needs to be refactored. Viewers should
 * not contain references to workbench actions. Actions should be contributed
 * by the view.
 */
public abstract class CatchupReleaseViewer extends DiffTreeViewer implements ISelectionChangedListener {
	
	class ShowInNavigatorAction extends Action implements ISelectionChangedListener {
		IViewSite viewSite;
		public ShowInNavigatorAction(IViewSite viewSite, String title) {
			super(title, null);
			this.viewSite = viewSite;
		}
		public void run() {
			showSelectionInNavigator(viewSite);
		}
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection)event.getSelection();
			if (selection.size() != 1) {
				setEnabled(false);
				return;
			}
			ITeamNode node = (ITeamNode)selection.getFirstElement();
			setEnabled(node.getResource().isAccessible());
		}
	};
	
	/**
	 * This filter hides all empty categories tree nodes.
	 */
	class CategoryFilter extends ViewerFilter {
		static final int SHOW_INCOMING = 1;
		static final int SHOW_OUTGOING = 2;
		static final int SHOW_CONFLICTS = 4;

		private int showMask = 0;
		
		CategoryFilter(int showMask) {
			// Mask for all categories to show
			this.showMask = showMask;
		}
		int getMask() {
			return showMask;
		}
		void setMask(int mask) {
			this.showMask = mask;
		}
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			// If this element has visible children, always show it.
			// This is not great -- O(n^2) filtering
			if (hasFilteredChildren(element)) {
				return true;
			}
			if (element instanceof ITeamNode) {
				int change = ((ITeamNode)element).getKind() & IRemoteSyncElement.CHANGE_MASK;
				int direction = ((ITeamNode)element).getChangeDirection();
				switch (direction) {
					case ITeamNode.INCOMING:
						return (showMask & SHOW_INCOMING) != 0;
					case ITeamNode.OUTGOING:
						return (showMask & SHOW_OUTGOING) != 0;
					case Differencer.CONFLICTING:
						return (showMask & SHOW_CONFLICTS) != 0;
					default:
						return change != 0;
				}
			}
			// No children are visible, and this folder has no changes, so don't show it.
			return false;
		}
		public boolean isFilterProperty(Object element, String property) {
			return property.equals(PROP_KIND);
		}
	}
	class FilterAction extends Action {
		/** 
		 * Must subclass constructor to make it accessible to container class
		 */
		FilterAction(String title, ImageDescriptor image) {
			super(title, image);
		}
		public void run() {
			updateFilters();
		}
	}
	class SyncSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			boolean oneIsFile = e1 instanceof TeamFile;
			boolean twoIsFile = e2 instanceof TeamFile;
			if (oneIsFile != twoIsFile) {
				return oneIsFile ? 1 : -1;
			}
			return super.compare(viewer, e1, e2);
		}
	}
	
	// The current sync mode
	private int syncMode = SyncView.SYNC_NONE;
	
	// Actions
	private FilterAction showIncoming;
	private FilterAction showOutgoing;
	private FilterAction showOnlyConflicts;
	private Action refresh;
	private Action expandAll;
	private Action removeFromTree;
	private ShowInNavigatorAction showInNavigator;
	private Action ignoreWhiteSpace;
	private Action toggleGranularity;
	
	// Property constant for diff mode kind
	static final String PROP_KIND = "team.ui.PropKind"; //$NON-NLS-1$

	private Action copyAllRightToLeft;


	/**
	 * Creates a new catchup/release viewer.
	 */
	protected CatchupReleaseViewer(Composite parent, SyncCompareInput model) {
		super(parent, model.getCompareConfiguration());
		setSorter(new SyncSorter());
		initializeActions(model);
	}
	
	/**
	 * Contributes actions to the provided toolbar
	 */
	void contributeToActionBars(IActionBars actionBars) {
		IToolBarManager toolBar = actionBars.getToolBarManager();
	
		toolBar.add(new Separator());
		toolBar.add(showOnlyConflicts);
	
		// Drop down menu
		IMenuManager menu = actionBars.getMenuManager();
		if (syncMode == SyncView.SYNC_BOTH) {
			menu.add(showIncoming);
			menu.add(showOutgoing);
		}
		menu.add(toggleGranularity);
		menu.add(ignoreWhiteSpace);
		menu.add(refresh);
	}
	
	/**
	 * Contributes actions to the popup menu.
	 */
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(expandAll);
		manager.add(removeFromTree); 
		if (showInNavigator != null) {
			manager.add(showInNavigator);
		}
		if(syncMode == SyncView.SYNC_COMPARE) {
			manager.add(copyAllRightToLeft);
		}
	}
	
	/**
	 * Expands to infinity all items in the selection.
	 */
	protected void expandSelection() {
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection) {
			Iterator elements = ((IStructuredSelection)selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				expandToLevel(next, ALL_LEVELS);
			}
		}
	}
	
	protected int getSyncMode() {
		return syncMode;
	}
	
	/**
	 * Returns true if the given element has filtered children, and false otherwise.
	 */
	protected boolean hasFilteredChildren(Object element) {
		return getFilteredChildren(element).length > 0;
	}
	
	/**
	 * Creates the actions for this viewer.
	 */
	private void initializeActions(final SyncCompareInput diffModel) {
		// Mask actions
		ImageDescriptor image = TeamUIPlugin.getPlugin().getImageDescriptor(UIConstants.IMG_DLG_SYNC_INCOMING);
		showIncoming = new FilterAction(Policy.bind("CatchupReleaseViewer.showIncomingAction"), image); //$NON-NLS-1$
		showIncoming.setToolTipText(Policy.bind("CatchupReleaseViewer.showIncomingAction")); //$NON-NLS-1$
	
		image = TeamUIPlugin.getPlugin().getImageDescriptor(UIConstants.IMG_DLG_SYNC_OUTGOING);
		showOutgoing = new FilterAction(Policy.bind("CatchupReleaseViewer.showOutgoingAction"), image); //$NON-NLS-1$
		showOutgoing.setToolTipText(Policy.bind("CatchupReleaseViewer.showOutgoingAction")); //$NON-NLS-1$
	
		image = TeamUIPlugin.getPlugin().getImageDescriptor(UIConstants.IMG_DLG_SYNC_CONFLICTING);
		
		//show only conflicts is not a HideAction because it doesnt flip bits, it sets an exact mask
		showOnlyConflicts = new FilterAction(Policy.bind("CatchupReleaseViewer.showOnlyConflictsAction"), image); //$NON-NLS-1$
		showOnlyConflicts.setToolTipText(Policy.bind("CatchupReleaseViewer.showOnlyConflictsAction")); //$NON-NLS-1$
	
		//refresh action
		image = TeamUIPlugin.getPlugin().getImageDescriptor(UIConstants.IMG_REFRESH);
		refresh = new Action(Policy.bind("CatchupReleaseViewer.refreshAction"), image) { //$NON-NLS-1$
			public void run() {
				diffModel.refresh();
			}
		};
		refresh.setToolTipText(Policy.bind("CatchupReleaseViewer.refreshAction")); //$NON-NLS-1$
		
		// Expand action
		expandAll = new Action(Policy.bind("CatchupReleaseViewer.expand"), null) { //$NON-NLS-1$
			public void run() {
				expandSelection();
			}
		};
		
		// Toggle granularity
		image = TeamUIPlugin.getPlugin().getImageDescriptor(UIConstants.IMG_CONTENTS);
		toggleGranularity = new Action(Policy.bind("CatchupReleaseViewer.Compare_File_Contents_1"), image) { //$NON-NLS-1$
			public void run() {
				diffModel.setSyncGranularity(isChecked() ? ILocalSyncElement.GRANULARITY_CONTENTS : ILocalSyncElement.GRANULARITY_TIMESTAMP);
			}
		};
		toggleGranularity.setChecked(diffModel.getSyncGranularity() == IRemoteSyncElement.GRANULARITY_CONTENTS);
		
		removeFromTree = new Action(Policy.bind("CatchupReleaseViewer.removeFromView"), null) { //$NON-NLS-1$
			public void run() {
				ISelection s = getSelection();
				if (!(s instanceof IStructuredSelection) || s.isEmpty()) {
					return;
				}
				// mark all selected nodes as in sync
				for (Iterator it = ((IStructuredSelection)s).iterator(); it.hasNext();) {
					Object element = it.next();
					setAllChildrenInSync((IDiffElement)element);
				}
				refresh();				
			}
		};
		
		copyAllRightToLeft = new Action(Policy.bind("CatchupReleaseViewer.copyAllRightToLeft"), null) { //$NON-NLS-1$
			public void run() {
				ISelection s = getSelection();
				if (!(s instanceof IStructuredSelection) || s.isEmpty()) {
					return;
				}
				for (Iterator it = ((IStructuredSelection)s).iterator(); it.hasNext();) {
					final Object element = it.next();
					if(element instanceof DiffElement) {
						try {
							new ProgressMonitorDialog(getTree().getShell()).run(false, false, new IRunnableWithProgress() {
								public void run(IProgressMonitor monitor)
									throws InvocationTargetException, InterruptedException {
										try {
											ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
												public void run(IProgressMonitor monitor) throws CoreException {
													try {
														monitor.beginTask(Policy.bind("CatchupReleaseViewer.Copying_right_contents_into_workspace_2"), 100); //$NON-NLS-1$
														copyAllRightToLeft((DiffElement)element, Policy.subMonitorFor(monitor, 100));
													} finally {
														monitor.done();
													}
												}
											}, monitor);
										} catch(CoreException e) {
											throw new InvocationTargetException(e);
										}
									}
							});
						} catch(InvocationTargetException e) {
							ErrorDialog.openError(WorkbenchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), Policy.bind("CatchupReleaseViewer.errorCopyAllRightToLeft"), null, null); //$NON-NLS-1$
						} catch(InterruptedException e) {
						}														
					}						
				}
				refresh();				
			}
			public boolean isEnabled() {
				ISelection s = getSelection();
				if (!(s instanceof IStructuredSelection) || s.isEmpty()) {
					return false;
				}
				return ((IStructuredSelection)s).size() == 1;
			}
		};
		
		// Show in navigator
		if (diffModel.getViewSite() != null) {
			showInNavigator = new ShowInNavigatorAction(diffModel.getViewSite(), Policy.bind("CatchupReleaseViewer.showInNavigator")); //$NON-NLS-1$
			addSelectionChangedListener(showInNavigator);
		}
		
		// Ignore white space
		image = TeamUIPlugin.getPlugin().getImageDescriptor(UIConstants.IMG_IGNORE_WHITESPACE);
		ignoreWhiteSpace = new Action(Policy.bind("CatchupReleaseViewer.ignoreWhiteSpace"), image) { //$NON-NLS-1$
			public void run() {
				Boolean value = isChecked() ? Boolean.TRUE : Boolean.FALSE;
				diffModel.getCompareConfiguration().setProperty(CompareConfiguration.IGNORE_WHITESPACE, value);
			}
		};
		ignoreWhiteSpace.setId("team.ignoreWhiteSpace"); //$NON-NLS-1$
		ignoreWhiteSpace.setChecked(false);
		
		// Add a selection listener to set the left label
		addSelectionChangedListener(this);
		
		// Add a double-click listener for expanding/contracting
		getTree().addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event e) {
				mouseDoubleClicked(e);
			}
		});
	
		// Add an F5 listener for refresh
		getTree().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.F5) {
					diffModel.refresh();
				}
			}
		});
	
		// Set an initial filter -- show all changes
		showIncoming.setChecked(true);
		showOutgoing.setChecked(true);
		showOnlyConflicts.setChecked(false);
		setFilters(CategoryFilter.SHOW_INCOMING| CategoryFilter.SHOW_CONFLICTS | CategoryFilter.SHOW_OUTGOING);
	}

	/**
	 * Method setAllChildrenInSync.
	 * @param iDiffElement
	 */
	private void setAllChildrenInSync(IDiffElement element) {
		if(element instanceof DiffContainer) {
			DiffContainer container = (DiffContainer)element;
			IDiffElement[] children = container.getChildren();
			for (int i = 0; i < children.length; i++) {
				setAllChildrenInSync(children[i]);
			}			
		}
		((DiffElement)element).setKind(IRemoteSyncElement.IN_SYNC);
	}
	
	protected void copyAllRightToLeft(IDiffElement element, IProgressMonitor monitor) throws CoreException {
		if(element instanceof DiffContainer) {
			DiffContainer container = (DiffContainer)element;
			IDiffElement[] children = container.getChildren();
			for (int i = 0; i < children.length; i++) {
				copyAllRightToLeft(children[i], monitor);
			}
		} else if(element instanceof TeamFile) {
			TeamFile file = (TeamFile)element;
			try {
				monitor = Policy.monitorFor(monitor);
				monitor.beginTask(null, 1);
				file.setProgressMonitor(Policy.subMonitorFor(monitor, 1));
				if(file.getKind() != IRemoteSyncElement.IN_SYNC) {
					if(file.getRight() == null || file.getLeft() == null) {
						file.copy(false /* right to left */);
					} 
					ITypedElement te = file.getLeft();
					ITypedElement rte = file.getRight();
					if(te instanceof IEditableContent) {
						IEditableContent editable = (IEditableContent)te;
						if(editable.isEditable()) {
							if(rte instanceof BufferedContent) {
								editable.setContent(((BufferedContent)rte).getContent());
							}
						}
					}
				}
				file.setProgressMonitor(null);
			} finally {
				monitor.done();
			}
		}
	}
	
	/*
	 * Method declared on ContentViewer.
	 */
	protected void inputChanged(Object input, Object oldInput) {
		super.inputChanged(input, oldInput);
		// Update the refresh action
		if (refresh != null) {
			Tree tree = getTree();
			if (tree != null) {
				refresh.setEnabled(input != null);
			}
		}
	}

	/**
	 * Shows the selected resource(s) in the resource navigator.
	 */
	private void showSelectionInNavigator(IViewSite viewSite) {
		ISelection selection = getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		// Create a selection of IResource objects
		Object[] selected = ((IStructuredSelection)selection).toArray();
		IResource[] resources = new IResource[selected.length];
		for (int i = 0; i < selected.length; i++) {
			resources[i] = ((ITeamNode)selected[i]).getResource();
		}
		ISelection resourceSelection = new StructuredSelection(resources);
		
		// Show the resource selection in the navigator
		try {
			IViewPart part = viewSite.getPage().showView(IPageLayout.ID_RES_NAV);
			if (part instanceof ResourceNavigator) {
				((ResourceNavigator)part).selectReveal(resourceSelection);
			}
		} catch (PartInitException e) {
			TeamUIPlugin.log(e.getStatus());
		}
	}
	
	/**
	 * The mouse has been double-clicked in the tree, perform appropriate
	 * behaviour.
	 */
	private void mouseDoubleClicked(Event e) {
		// Only act on single selection
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection)selection;
			if (structured.size() == 1) {
				Object first = structured.getFirstElement();
				if (first instanceof IDiffContainer) {
					// Try to expand/contract
					setExpandedState(first, !getExpandedState(first));
				}
			}
		}
	}
	
	/**
	 * Notifies that the selection has changed.
	 *
	 * @param event event object describing the change
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection)selection;
			Object selected = structured.getFirstElement();
			if (selected instanceof TeamFile) {
				updateLabels(((TeamFile)selected).getMergeResource());
			}
		}
	}
	
	/**
	 * Subclasses may override to provide different labels for the compare configuration.
	 */
	protected void updateLabels(MergeResource resource) {
		resource.setLabels(getCompareConfiguration());
	}
	
	/**
	 * Set the filter mask to be the exact mask specified.
	 */
	private void setFilters(int maskToHide) {
		ViewerFilter[] filters = getFilters();
		if (filters != null) {
			for (int i = 0; i < filters.length; i++) {
				if (filters[i] instanceof CategoryFilter) {
					CategoryFilter filter = (CategoryFilter)filters[i];
					// Set the exact match to be applied on the filter
					filter.setMask(maskToHide);
					refresh();
					return;
				}
			}
		}
		// No category filter found -- add one
		addFilter(new CategoryFilter(maskToHide));
	}
	
	/**
	 * The sync mode has changed.  Update the filters.
	 */
	public void syncModeChanged(int mode) {
		this.syncMode = mode;
		updateFilters();
	}
	
	/**
	 * Sets the viewer filtering based on the current state
	 * of the filter actions.
	 */
	void updateFilters() {
		//do nothing if viewer is disposed
		Control control = getControl();
		if (control == null || control.isDisposed()) 
			return;
		
		//always show conflicts
		int filters = CategoryFilter.SHOW_CONFLICTS;
		
		//determine what other filters to apply based on current action states
		switch (syncMode) {
			case SyncView.SYNC_INCOMING:
			case SyncView.SYNC_MERGE:
				if (!showOnlyConflicts.isChecked()) {
					filters |= CategoryFilter.SHOW_INCOMING;
				}
				break;
			case SyncView.SYNC_OUTGOING:
				if (!showOnlyConflicts.isChecked()) {
					filters |= CategoryFilter.SHOW_OUTGOING;
				}
				break;
			case SyncView.SYNC_BOTH:
				boolean conflictsOnly = showOnlyConflicts.isChecked();
				//if showing only conflicts, don't allow these actions to happen
				showIncoming.setEnabled(!conflictsOnly);
				showOutgoing.setEnabled(!conflictsOnly);
				if (!conflictsOnly) {
					if (showIncoming.isChecked()) {
						filters |= CategoryFilter.SHOW_INCOMING;
					}
					if (showOutgoing.isChecked()) {
						filters |= CategoryFilter.SHOW_OUTGOING;
					}
				}
				break;
		}
		setFilters(filters);
	}
}
