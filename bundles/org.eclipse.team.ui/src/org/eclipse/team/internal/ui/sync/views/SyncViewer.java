/*************.******************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.sync.views;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.ITeamResourceChangeListener;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamDelta;
import org.eclipse.team.core.subscribers.TeamProvider;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.team.internal.ui.jobs.RefreshSubscriberInputJob;
import org.eclipse.team.internal.ui.jobs.RefreshSubscriberJob;
import org.eclipse.team.internal.ui.sync.actions.OpenInCompareAction;
import org.eclipse.team.internal.ui.sync.actions.RefreshAction;
import org.eclipse.team.internal.ui.sync.actions.SyncViewerActions;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.sync.ISyncViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;

public class SyncViewer extends ViewPart implements ITeamResourceChangeListener, ISyncSetChangedListener, ISyncViewer {
	
	public static int PROP_VIEWTYPE = 0x10;

	/*
	 * This view's id. The same value as in the plugin.xml.
	 */
	 public static final String VIEW_ID = "org.eclipse.team.sync.views.SyncViewer";  //$NON-NLS-1$
	
	/*
	 * The viewer thst is shown in the view. Currently this can be
	 * either a table or tree viewer.
	 */
	private StructuredViewer viewer;
	
	/*
	 * Parent composite of this view. It is remembered so that we can
	 * dispose of its children when the viewer type is switched.
	 */
	private Composite composite = null;
	private IMemento memento;

	/*
	 * viewer type constants
	 */ 
	private int currentViewType;
	
	/*
	 * Array of SubscriberInput objects. There is one of these for each subscriber
	 * registered with the sync view. 
	 */
	private Map subscriberInputs = new HashMap(1);
	private SubscriberInput input = null;
	private SubscriberInput lastInput = null;
	
	/*
	 * A set of common actions. They are hooked to the active SubscriberInput and
	 * must be reset when the input changes.
	 */
	private SyncViewerActions actions;
	
	/*
	 * View image
	 */
	private Image refreshingImg;
	private Image initialImg; 
	private Image viewImage;

	private IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (getCurrentViewType() == TREE_VIEW &&
				event.getProperty().equals(IPreferenceIds.SYNCVIEW_COMPRESS_FOLDERS)) {
					setTreeViewerContentProvider();
			}
		}
	};
	
	public SyncViewer() {
		IPreferenceStore store = TeamUIPlugin.getPlugin().getPreferenceStore();
		store.addPropertyChangeListener(propertyListener);
		currentViewType = store.getInt(IPreferenceIds.SYNCVIEW_VIEW_TYPE);
		if (currentViewType != TREE_VIEW) {
			currentViewType = TABLE_VIEW;
		}
	}

	public Image getTitleImage() {
		return viewImage;
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		initializeActions();
		createViewer(parent);
		contributeToActionBars();
		this.composite = parent;
		
		TeamProvider.addListener(this);
		TeamSubscriber[] subscribers = TeamProvider.getSubscribers();
		for (int i = 0; i < subscribers.length; i++) {
			TeamSubscriber subscriber = subscribers[i];
			addSubscriber(subscriber);
		}
		
		// initialize images
		initialImg = TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_SYNC_VIEW).createImage();
		refreshingImg = TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_SYNC_MODE_CATCHUP).createImage();
		TeamUIPlugin.disposeOnShutdown(initialImg);
		TeamUIPlugin.disposeOnShutdown(refreshingImg);
		setViewImage(initialImg);
		
		updateTitle();
		
		// add listeners
		Platform.getJobManager().addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if(event.getJob().belongsTo(RefreshSubscriberJob.getFamily())) {
					setViewImage(initialImg);
				}
			}

			public void running(IJobChangeEvent event) {
				if(event.getJob().belongsTo(RefreshSubscriberJob.getFamily())) {
					setViewImage(refreshingImg);
				}
			}
		});		
	}

	private void setViewImage(Image image) {
		viewImage = image;
		fireSafePropertyChange(IWorkbenchPart.PROP_TITLE);
	}

	public void switchViewerType(int viewerType) {
		if(viewerType != currentViewType) {
			ISelection s = viewer.getSelection();
			if (composite == null || composite.isDisposed()) return;
			currentViewType = viewerType;
			TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCVIEW_VIEW_TYPE, currentViewType);
			disposeChildren(composite);
			createViewer(composite);
			composite.layout();
			IStructuredSelection selection = (IStructuredSelection)s;
			if(selection.size() == 0) {
				gotoDifference(INavigableControl.NEXT);
			} else {
				viewer.setSelection(selection, true);
			}
			fireSafePropertyChange(PROP_VIEWTYPE);
		}
	}
	
	private void createViewer(Composite parent) {
		switch(currentViewType) {
			case TREE_VIEW:
				createTreeViewerPartControl(parent); 
				break;
			case TABLE_VIEW:
				createTableViewerPartControl(parent); 
				break;
		}
		hookContextMenu();
		initializeListeners();
		
		if(input != null) {
			viewer.setInput(input.getFilteredSyncSet());
		}
		viewer.getControl().setFocus();
	}

	private void createTreeViewerPartControl(Composite parent) {
		viewer = new SyncTreeViewer(this, parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		setTreeViewerContentProvider();
		viewer.setLabelProvider(SyncViewerLabelProvider.getDecoratingLabelProvider());
		viewer.setSorter(new SyncViewerSorter());
	}
	
	private void setTreeViewerContentProvider() {
		if (TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_COMPRESS_FOLDERS)) {
			viewer.setContentProvider(new CompressedFolderContentProvider());
		} else {
			viewer.setContentProvider(new SyncSetTreeContentProvider());
		}
	}

	private void createTableViewerPartControl(Composite parent) {
		// Create the table
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		
		// Set the table layout
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		
		// Create the viewer
		TableViewer tableViewer = new SyncTableViewer(table);
		
		// Create the table columns
		createColumns(table, layout, tableViewer);
		
		// Set the table contents
		viewer = tableViewer;
		viewer.setContentProvider(new SyncSetTableContentProvider());
		viewer.setLabelProvider(new SyncViewerLabelProvider());
		viewer.setSorter(new SyncViewerTableSorter(SyncViewerTableSorter.COL_NAME));
	}
	
	/**
	 * Creates the columns for the sync viewer table.
	 */
	private void createColumns(Table table, TableLayout layout, TableViewer viewer) {
		SelectionListener headerListener = SyncViewerTableSorter.getColumnListener(viewer);
		// revision
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText("Resource"); //$NON-NLS-1$
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(30, true));
	
		// tags
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText("In Folder"); //$NON-NLS-1$
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(50, true));
	}
	
	private void disposeChildren(Composite parent) {
		Control[] children = parent.getChildren();
		for (int i = 0; i < children.length; i++) {
			Control control = children[i];
			control.dispose();
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				actions.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		actions.fillActionBars(bars);
	}

	/**
	 * Adds the listeners to the viewer.
	 * 
	 * @param viewer the viewer
	 * @since 2.0
	 */
	protected void initializeListeners() {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				handleOpen(event);
			}
		});
//		viewer.getControl().addKeyListener(new KeyListener() {
//			public void keyPressed(KeyEvent event) {
//				handleKeyPressed(event);
//			}
//			public void keyReleased(KeyEvent event) {
//				handleKeyReleased(event);
//			}
//		});
	}
	
	/**
	 * Handles a selection changed event from the viewer.
	 * Updates the status line and the action bars, and links to editor (if option enabled).
	 * 
	 * @param event the selection event
	 * @since 2.0
	 */
	protected void handleSelectionChanged(SelectionChangedEvent event) {
		final IStructuredSelection sel = (IStructuredSelection) event.getSelection();
		updateStatusLine(sel);
		updateActionBars(sel);
// TODO: Need to decide if link to editor should be supported
//		dragDetected = false;
//		if (isLinkingEnabled()) {
//			getShell().getDisplay().asyncExec(new Runnable() {
//				public void run() {
//					if (dragDetected == false) {
//						// only synchronize with editor when the selection is not the result 
//						// of a drag. Fixes bug 22274.
//						linkToEditor(sel);
//					}
//				}
//			});
//		}
	}

	protected void handleOpen(OpenEvent event) {
		actions.open();
	}
	/**
	 * Handles a double-click event from the viewer.
	 * Expands or collapses a folder when double-clicked.
	 * 
	 * @param event the double-click event
	 * @since 2.0
	 */
	protected void handleDoubleClick(DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object element = selection.getFirstElement();

		// Double-clicking should expand/collapse containers
		if (viewer instanceof TreeViewer) {
			TreeViewer tree = (TreeViewer)viewer;
			if (tree.isExpandable(element)) {
				tree.setExpandedState(element, !tree.getExpandedState(element));
			}
		}

	}
	
	private void initializeActions() {
		actions = new SyncViewerActions(this);
		actions.restore(memento);
	}
	

	public void activateSubscriber(TeamSubscriber subscriber) {
		SubscriberInput input = (SubscriberInput)subscriberInputs.get(subscriber.getId());
		if (input == null) {
			addSubscriber(subscriber);
		};
		initializeSubscriberInput(input);
	}
	
	/*
	 * This method is synchronized to ensure that all internal state is not corrupted
	 */
	public synchronized void initializeSubscriberInput(final SubscriberInput input) {
		Assert.isNotNull(input);
		
		this.lastInput = this.input;
		this.input = input;
		
		if(lastInput != null) {
			lastInput.getFilteredSyncSet().removeSyncSetChangedListener(this);
			lastInput.getSubscriberSyncSet().removeSyncSetChangedListener(this);
		}
		
		input.getFilteredSyncSet().addSyncSetChangedListener(this);
		input.getSubscriberSyncSet().addSyncSetChangedListener(this);

		final IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					ActionContext context = new ActionContext(null);
					context.setInput(input);					
					input.prepareInput(monitor);
					// important to set the context after the input has been initialized. There
					// are some actions that depend on the sync set to be initialized.
					actions.setContext(context);
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		};

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!hasRunnableContext()) return;				
				SyncViewer.this.run(runnable);
				viewer.setInput(input.getFilteredSyncSet());
				RefreshSubscriberInputJob refreshJob = TeamUIPlugin.getPlugin().getRefreshJob();
				refreshJob.setSubscriberInput(input);
				IStructuredSelection s = (IStructuredSelection)viewer.getSelection();
				if(s.size() == 0) {
					gotoDifference(INavigableControl.NEXT);
				}
			}
		});
		updateTitle();
	}
	
	/*
	 * Live Synchronize - {showing N of M changes} {Subscriber name}
	 */
	public void updateTitle() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				SubscriberInput input = getInput();
				if(input != null) {
					TeamSubscriber subscriber = input.getSubscriber();
					String changesText = Policy.bind("LiveSyncView.titleChangeNumbers",  //$NON-NLS-1$
														new Integer(input.getFilteredSyncSet().size()).toString(), 
														new Integer(input.getSubscriberSyncSet().size()).toString());
				 	setTitle(
				 		Policy.bind("LiveSyncView.titleWithSubscriber", new String[] { //$NON-NLS-1$
				 				Policy.bind("LiveSyncView.title"),  //$NON-NLS-1$
				 				changesText,
				 				subscriber.getName()}));
				 	IWorkingSet ws = input.getWorkingSet();
				 	if(ws != null) {
				 		setTitleToolTip(Policy.bind("LiveSyncView.titleTooltip", subscriber.getDescription(), ws.getName())); //$NON-NLS-1$
				 	} else {
					 	setTitleToolTip(subscriber.getDescription());
				 	}
				} else {
					setTitle(Policy.bind("LiveSyncView.title")); //$NON-NLS-1$
					setTitleToolTip(""); //$NON-NLS-1$
				}
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (viewer == null) return;
		viewer.getControl().setFocus();
	}

	public StructuredViewer getViewer() {
		return viewer;
	}
	
	private static void handle(Shell shell, Exception exception, String title, String message) {
		Utils.handleError(shell, exception, title, message);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		
		// cancel and wait 
		RefreshSubscriberInputJob job = TeamUIPlugin.getPlugin().getRefreshJob();
		
		job.cancel();
//		try {
//			job.join();
//		} catch (InterruptedException e) {
//			// continue with shutdown
//		}
		job.setSubscriberInput(null);
		
		TeamProvider.removeListener(this);
		for (Iterator it = subscriberInputs.values().iterator(); it.hasNext();) {
			SubscriberInput input = (SubscriberInput) it.next();
			input.dispose();
		}
		
		TeamUIPlugin.getPlugin().getPreferenceStore().removePropertyChangeListener(propertyListener);
	}

	public void run(IRunnableWithProgress runnable) {
		try {
			getRunnableContext().run(true, true, runnable);
		} catch (InvocationTargetException e) {
			handle(getSite().getShell(), e, null, null);
		} catch (InterruptedException e) {
			// Nothing to be done
		}
	}
	
	/**
	 * Returns the runnableContext.
	 * @return IRunnableContext
	 */
	private IRunnableContext getRunnableContext() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();									
	}
	
	private boolean hasRunnableContext() {
		return getRunnableContext() != null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.memento = memento;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		super.saveState(memento);
		actions.save(memento);
	}
	
	public int getViewerType() {
		return currentViewType;
	}

	/*
	 * Return the current input for the view.
	 */
	public SubscriberInput getInput() {
		return input;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ITeamResourceChangeListener#teamResourceChanged(org.eclipse.team.core.sync.TeamDelta[])
	 */
	public void teamResourceChanged(TeamDelta[] deltas) {
		for (int i = 0; i < deltas.length; i++) {
			TeamDelta delta = deltas[i];
			if(delta.getFlags() == TeamDelta.SUBSCRIBER_CREATED) {
				TeamSubscriber s = delta.getSubscriber();
				addSubscriber(s);
			} else if(delta.getFlags() == TeamDelta.SUBSCRIBER_DELETED) {
				TeamSubscriber s = delta.getSubscriber();
				removeSubscriber(s);
			}
		}
	}

	/*
	 * Add the subscriber to the view. This method does not activate
	 * the subscriber.
	 */
	synchronized private void addSubscriber(final TeamSubscriber s) {
		SubscriberInput si = new SubscriberInput(s);
		subscriberInputs.put(s.getId(), si);
		ActionContext context = new ActionContext(null);
		context.setInput(si);
		actions.addContext(context);
	}
	
	synchronized private void removeSubscriber(TeamSubscriber s) {
		// notify that context is changing
		SubscriberInput si = (SubscriberInput)subscriberInputs.get(s.getId());
		ActionContext context = new ActionContext(null);
		context.setInput(si);
		actions.removeContext(context);
		
		// dispose of the input
		si.dispose();
		
		// forget about this input
		subscriberInputs.remove(s.getId());
		
		if (si == input && lastInput != null) {
			// show last input
			initializeSubscriberInput(lastInput);
		}
	}
	
	public void collapseAll() {
		if (viewer == null || !(viewer instanceof AbstractTreeViewer)) return;
		viewer.getControl().setRedraw(false);		
		((AbstractTreeViewer)viewer).collapseToLevel(viewer.getInput(), TreeViewer.ALL_LEVELS);
		viewer.getControl().setRedraw(true);
	}
	
	public ISelection getSelection() {
		ISelection selection = getViewer().getSelection();
		if (! selection.isEmpty() && viewer instanceof AbstractTreeViewer) {
			// For a tree, selection should be deep and only include out-of-sync resources
			Object[] selected = ((IStructuredSelection)selection).toArray();
			Set result = new HashSet();
			for (int i = 0; i < selected.length; i++) {
				Object object = selected[i];
				if (object instanceof SyncResource) {
					SyncResource syncResource = (SyncResource) object;
					SyncInfo[] infos = syncResource.getOutOfSyncDescendants();
					result.addAll(Arrays.asList(infos));
				}
			}
			selection = new StructuredSelection((Object[]) result.toArray(new Object[result.size()]));
		}
		return selection;
	}
	
	/**
	 * This method enables "Show In" support for this view
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class key) {
		if (key == IShowInSource.class) {
			return new IShowInSource() {
				public ShowInContext getShowInContext() {
					StructuredViewer v = getViewer();
					if (v == null) return null;
					return new ShowInContext(null, v.getSelection());
				}
			};
		}
		return super.getAdapter(key);
	}
	
	/**
	 * Updates the action bar actions.
	 * 
	 * @param selection the current selection
	 * @since 2.0
	 */
	protected void updateActionBars(IStructuredSelection selection) {
		if (actions != null) {
			ActionContext actionContext = actions.getContext();
			if(actionContext != null) {
				actionContext.setSelection(selection);
				actions.updateActionBars();
			}
		}
	}
	
	/**
	 * Updates the message shown in the status line.
	 *
	 * @param selection the current selection
	 */
	protected void updateStatusLine(IStructuredSelection selection) {
		String msg = getStatusLineMessage(selection);
		getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
	}

	/**
	 * Returns the message to show in the status line.
	 *
	 * @param selection the current selection
	 * @return the status line message
	 * @since 2.0
	 */
	protected String getStatusLineMessage(IStructuredSelection selection) {
		if (selection.size() == 1) {
			IResource resource = getResource(selection.getFirstElement());
			if (resource == null) {
				return "One item selected";
			} else {
				return resource.getFullPath().makeRelative().toString();
			}
		}
		if (selection.size() > 1) {
			return selection.size() + " items selected";
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * @param object
	 * @return
	 */
	private IResource getResource(Object object) {
		return (IResource)TeamAction.getAdapter(object, IResource.class);
	}
	
	/**
	 * Makes this view visible in the active page.
	 */
	public static SyncViewer showInActivePage(IWorkbenchPage activePage) {
		try {
			if (activePage == null) {
				activePage = TeamUIPlugin.getActivePage();
				if (activePage == null) return null;
			}
			return (SyncViewer)activePage.showView(VIEW_ID);
		} catch (PartInitException pe) {
			TeamUIPlugin.log(new TeamException("error showing view", pe));
			return null;
		}
	}

	/**
	 * Update the title when either the subscriber or filter sync set changes.
	 */
	public void syncSetChanged(SyncSetChangedEvent event) {
		updateTitle();
		// remove opened compare editors if file was removed from sync view and update if changed
		IResource[] resources =event.getRemovedResources();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			OpenInCompareAction.closeCompareEditorFor(this, resource);
		}
	}

	private void fireSafePropertyChange(final int property) {
		if(! viewer.getControl().isDisposed()) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {		
					firePropertyChange(property);
				}
			});
		}
	}
	
	public void setSelection(TeamSubscriber subscriber, IResource[] resources, int viewType) {
		switchViewerType(viewType);
		List syncResource = new ArrayList(resources.length);
		for (int i = 0; i < resources.length; i++) {
			syncResource.add(new SyncResource((SyncSet)viewer.getInput(), resources[i]));			
		}
		if(! syncResource.isEmpty()) {
			viewer.setSelection(new StructuredSelection(syncResource), true /* reveal */);
			if(viewer instanceof AbstractTreeViewer) {
				((AbstractTreeViewer)viewer).expandToLevel(2);
			}
		}
		if(subscriber != null) {
			activateSubscriber(subscriber);
		}
	}
	
	/**
	 * Refreshes the resources from the specified subscriber. The working set or filters applied
	 * to the sync view do not affect the sync.
	 */
	public void refreshWithRemote(TeamSubscriber subscriber, IResource[] resources) {
		QualifiedName id = subscriber.getId();
		if(subscriberInputs.containsKey(id)) {
			if(! input.getSubscriber().getId().equals(id)) {
				initializeSubscriberInput((SubscriberInput)subscriberInputs.get(id));
			}
			RefreshAction.run(this, resources, subscriber);
		}		
	}

	/**
	 * Refreshes the resources in the current input for the given subscriber.
	 */	
	public void refreshWithRemote(TeamSubscriber subscriber) {
		QualifiedName id = subscriber.getId();
		if(subscriberInputs.containsKey(id)) {
			if(! input.getSubscriber().getId().equals(id)) {
				initializeSubscriberInput((SubscriberInput)subscriberInputs.get(id));
			}
			RefreshAction.run(this, input.roots(), subscriber);
		}		
	}
	
	/**
	 * Refreshes the resources in the current input for the given subscriber.
	 */	
	public void refreshWithRemote() {
		RefreshAction.run(this, input.roots(), input.getSubscriber());
	}
	
	public int getCurrentViewType() {
		return currentViewType;
	}

	public void selectAll() {
		if (getViewerType() == TABLE_VIEW) {
			TableViewer table = (TableViewer)getViewer();
			table.getTable().selectAll();
		} else {
			// Select All in a tree doesn't really work well
//			TreeViewer tree = (TreeViewer)getViewer();
//			tree.getTree().selectAll();
		}
	}
	
	public boolean gotoDifference(int direction) {
		if(viewer instanceof INavigableControl) {
			return ((INavigableControl)viewer).gotoDifference(direction);
		}
		return false;
	}
	
	public SyncSetContentProvider getContentProvider() {
		return (SyncSetContentProvider)getViewer().getContentProvider();
	}
}