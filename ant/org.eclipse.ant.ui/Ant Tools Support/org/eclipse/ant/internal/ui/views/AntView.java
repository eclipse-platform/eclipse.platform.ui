/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Roscoe Rush - Concept and prototype implementation
 *     IBM Corporation - current implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.views;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.internal.core.IAntCoreConstants;
import org.eclipse.ant.internal.ui.IAntUIHelpContextIds;
import org.eclipse.ant.internal.ui.model.AntElementNode;
import org.eclipse.ant.internal.ui.model.AntModelLabelProvider;
import org.eclipse.ant.internal.ui.model.AntModelProblem;
import org.eclipse.ant.internal.ui.model.AntProjectNode;
import org.eclipse.ant.internal.ui.model.AntProjectNodeProxy;
import org.eclipse.ant.internal.ui.model.AntTargetNode;
import org.eclipse.ant.internal.ui.model.InternalTargetFilter;
import org.eclipse.ant.internal.ui.views.actions.AddBuildFilesAction;
import org.eclipse.ant.internal.ui.views.actions.AntOpenWithMenu;
import org.eclipse.ant.internal.ui.views.actions.FilterInternalTargetsAction;
import org.eclipse.ant.internal.ui.views.actions.RefreshBuildFilesAction;
import org.eclipse.ant.internal.ui.views.actions.RemoveAllAction;
import org.eclipse.ant.internal.ui.views.actions.RemoveProjectAction;
import org.eclipse.ant.internal.ui.views.actions.RunTargetAction;
import org.eclipse.ant.internal.ui.views.actions.SearchForBuildFilesAction;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * A view which displays a hierarchical view of ant build files and allows the
 * user to run selected targets from those files.
 */
public class AntView extends ViewPart implements IResourceChangeListener, IShowInSource {
	
	private Object[] restoredViewerInput= null;
	private boolean filterInternalTargets= false;
	private InternalTargetFilter fInternalTargetFilter= null;
	/**
	 * This memento allows the Ant view to save and restore state
	 * when it is closed and opened within a session. A different
	 * memento is supplied by the platform for persistence at
	 * workbench shutdown.
	 */
	private static IMemento fgTempMemento = null;

	/**
	 * XML tag used to identify an ant project in storage
	 */
	private static final String TAG_PROJECT = "project"; //$NON-NLS-1$
	/**
	 * XML key used to store whether or not an Ant project is an error node.
	 * Persisting this data saved a huge amount of processing at startup.
	 */
	private static final String KEY_ERROR = "error"; //$NON-NLS-1$
	/**
	 * XML key used to store whether or not an Ant project is an error node.
	 * Persisting this data saved a huge amount of processing at startup.
	 */
	private static final String KEY_WARNING = "warning"; //$NON-NLS-1$
	/**
	 * XML key used to store an ant project's path
	 */
	private static final String KEY_PATH = "path"; //$NON-NLS-1$
	/**
	 * XML key used to store an ant node's name
	 */
	private static final String KEY_NAME = "name"; //$NON-NLS-1$
	/**
	 * XML key used to store an ant project's default target name
	 */
	private static final String KEY_DEFAULT = "default"; //$NON-NLS-1$
	/**
	 * XML tag used to identify the "filter internal targets" preference.
	 */
	private static final String TAG_FILTER_INTERNAL_TARGETS = "filterInternalTargets"; //$NON-NLS-1$
	/**
	 * XML key used to store the value of the "filter internal targets" preference. 
	 */
	private static final String KEY_VALUE = "value"; //$NON-NLS-1$

	/**
	 * The tree viewer that displays the users ant projects
	 */
	private TreeViewer projectViewer;
	private AntViewContentProvider contentProvider;

	/**
	 * Collection of <code>IUpdate</code> actions that need to update on
	 * selection changed in the project viewer.
	 */
	private List updateProjectActions;
	// Ant View Actions
	private AddBuildFilesAction addBuildFileAction;
	private SearchForBuildFilesAction searchForBuildFilesAction;
	private RefreshBuildFilesAction refreshBuildFilesAction;
	private RemoveProjectAction removeProjectAction;
	private RemoveAllAction removeAllAction;
	private FilterInternalTargetsAction filterInternalTargetsAction;
	private RunTargetAction runTargetAction;
	// Context-menu-only actions
	private AntOpenWithMenu openWithMenu;

	/**
	 * The given build file has changed. Refresh the view to pick up any
	 * structural changes.
	 */
	private void handleBuildFileChanged(AntProjectNode project) {
		((AntProjectNodeProxy)project).parseBuildFile(true);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				//must do a full refresh to re-sort
				projectViewer.refresh();
				//update the status line
				handleSelectionChanged((IStructuredSelection) projectViewer.getSelection());
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		initializeActions();
		createProjectViewer(parent);
		initializeDragAndDrop();
		fillMainToolBar();
		if (getProjects().length > 0) {
			// If any projects have been added to the view during startup,
			// begin listening for resource changes
			ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		}
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IAntUIHelpContextIds.ANT_VIEW);
		updateProjectActions();
	}
	
	private void initializeDragAndDrop() {
		int ops = DND.DROP_COPY | DND.DROP_DEFAULT;
		Transfer[] transfers = new Transfer[] { FileTransfer.getInstance() };
		TreeViewer viewer = getViewer();
		AntViewDropAdapter adapter = new AntViewDropAdapter(this);
		viewer.addDropSupport(ops, transfers, adapter);
	}

	/**
	 * Creates a pop-up menu on the given control
	 *
	 * @param menuControl the control with which the pop-up
	 *  menu will be associated
	 */
	private void createContextMenu(Viewer viewer) {
		Control menuControl = viewer.getControl();
		MenuManager menuMgr = new MenuManager("#PopUp"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});
		Menu menu = menuMgr.createContextMenu(menuControl);
		menuControl.setMenu(menu);

		// register the context menu such that other plugins may contribute to it
		getSite().registerContextMenu(menuMgr, viewer);
	}

	/**
	 * Adds actions to the context menu
	 *
	 * @param viewer the viewer who's menu we're configuring
	 * @param menu The menu to contribute to
	 */
	private void fillContextMenu(IMenuManager menu) {
		addOpenWithMenu(menu);
		menu.add(new Separator());
		menu.add(addBuildFileAction);
		menu.add(removeProjectAction);
		menu.add(removeAllAction);
		menu.add(refreshBuildFilesAction);
		
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void addOpenWithMenu(IMenuManager menu) {
		AntElementNode node= getSelectionNode();
		if (node != null) {
			IFile buildFile= node.getIFile();
			if (buildFile != null) {
				menu.add(new Separator("group.open")); //$NON-NLS-1$
				IMenuManager submenu= new MenuManager(AntViewMessages.AntView_1);
				openWithMenu.setNode(node);
				submenu.add(openWithMenu);
				menu.appendToGroup("group.open", submenu); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Initialize the actions for this view
	 */
	private void initializeActions() {
		updateProjectActions= new ArrayList(5);
		
		addBuildFileAction = new AddBuildFilesAction(this);
		
		removeProjectAction = new RemoveProjectAction(this);
		updateProjectActions.add(removeProjectAction);
		
		removeAllAction = new RemoveAllAction(this);
		updateProjectActions.add(removeAllAction);
		
		runTargetAction = new RunTargetAction(this);
		updateProjectActions.add(runTargetAction);
		
		searchForBuildFilesAction = new SearchForBuildFilesAction(this);
		
		refreshBuildFilesAction = new RefreshBuildFilesAction(this);
		updateProjectActions.add(refreshBuildFilesAction); 
		
		openWithMenu= new AntOpenWithMenu(this.getViewSite().getPage());
		
		filterInternalTargetsAction= new FilterInternalTargetsAction(this);
	}

	/**
	 * Updates the enabled state of all <code>IUpdate</code> actions associated 
	 * with the project viewer.
	 */
	private void updateProjectActions() {
		Iterator iter = updateProjectActions.iterator();
		while (iter.hasNext()) {
			((IUpdate) iter.next()).update();
		}
	}

	/**
	 * Create the viewer which displays the Ant projects
	 */
	private void createProjectViewer(Composite parent) {
		projectViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		contentProvider = new AntViewContentProvider();
		projectViewer.setContentProvider(contentProvider);
		
		filterInternalTargetsAction.setChecked(filterInternalTargets);
		setFilterInternalTargets(filterInternalTargets);
		
		projectViewer.setLabelProvider(new AntModelLabelProvider());
		if (fgTempMemento != null) {
			restoreViewerInput(fgTempMemento);
			fgTempMemento= null;
		}
		projectViewer.setInput(new Object[0]);
		if (restoredViewerInput.length > 0) {
		    contentProvider.addAll(restoredViewerInput);
		}
		projectViewer.setComparator(new ViewerComparator() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof AntProjectNode && e2 instanceof AntProjectNode
					|| e1 instanceof AntTargetNode && e2 instanceof AntTargetNode) {
					return e1.toString().compareToIgnoreCase(e2.toString());
				} 
				return 0;
			}
		});
		
		projectViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged((IStructuredSelection)event.getSelection());
			}
		});
		
		projectViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (!event.getSelection().isEmpty()) {
					handleProjectViewerDoubleClick();
				}
			}
		});
		
		projectViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				handleProjectViewerKeyPress(event);
			}
		});
		
		createContextMenu(projectViewer);
		getSite().setSelectionProvider(projectViewer);
	}
	
	private void handleProjectViewerKeyPress(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0) {
			if (removeProjectAction.isEnabled()) {
				removeProjectAction.run();
			}
		} else if (event.keyCode == SWT.F5 && event.stateMask == 0) {
			if (refreshBuildFilesAction.isEnabled()) {
				refreshBuildFilesAction.run();
			}
		}
	}
	
	private void handleProjectViewerDoubleClick() {
		AntElementNode node= getSelectionNode();
		if (node != null) {
			runTargetAction.run(node);
		} 
	}

	/**
	 * Updates the actions and status line for selection change in one of the
	 * viewers.
	 */
	private void handleSelectionChanged(IStructuredSelection selection) {
		updateProjectActions();
		Iterator selectionIter = selection.iterator();
		AntElementNode selected = null;
		if (selectionIter.hasNext()) {
			selected = (AntElementNode) selectionIter.next();
		}
		String messageString= null;
		if (!selectionIter.hasNext()) { 
		    if (selected != null) {
		        String errorString= selected.getProblemMessage();
		        if (errorString != null) {
		            getViewSite().getActionBars().getStatusLineManager().setErrorMessage(errorString);
		            return;
		        }
		    }
		    getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
			messageString= getStatusLineText(selected);
		} 
		getViewSite().getActionBars().getStatusLineManager().setMessage(messageString);
	}
	
	/**
	 * Returns text appropriate for display in the workbench status line for the
	 * given node.
	 */
	private String getStatusLineText(AntElementNode node) {
		if (node instanceof AntProjectNode) {
			AntProjectNode project = (AntProjectNode) node;
			StringBuffer message= new StringBuffer(project.getBuildFileName());
			String description= project.getDescription();
			if (description != null && description.length() > 0) {
				message.append(": "); //$NON-NLS-1$
				message.append(description);
			}
			return message.toString();
		} else if (node instanceof AntTargetNode) {
			AntTargetNode target = (AntTargetNode) node;
			StringBuffer message= new StringBuffer();
			Enumeration depends= target.getTarget().getDependencies();
			if (depends.hasMoreElements()) {
				message.append(AntViewMessages.AntView_3);
				message.append((String)depends.nextElement()); // Unroll the loop to avoid trailing comma
				while (depends.hasMoreElements()) {
					String dependancy = (String) depends.nextElement();
					message.append(',').append(dependancy);
				}
				message.append('\"');
			}
			String description= target.getTarget().getDescription();
			if (description != null && description.length() != 0) {
				message.append(AntViewMessages.AntView_4);
				message.append(description);
				message.append('\"');
			}
			return message.toString();
		}
		return null;
	}

	/**
	 * Returns the tree viewer that displays the projects in this view
	 * 
	 * @return TreeViewer this view's project viewer
	 */
	public TreeViewer getViewer() {
		return projectViewer;
	}

	/**
	 * Returns the <code>AntProjectNode</code>s currently displayed in this view.
	 * 
	 * @return AntProjectNode[] the <code>ProjectNode</code>s currently displayed
	 * in this view
	 */
	public AntProjectNode[] getProjects() {
		Object[] objects= contentProvider.getElements(projectViewer.getInput());
		AntProjectNode[] projects= new AntProjectNode[objects.length];
		for (int i = 0; i < projects.length; i++) {
			projects[i] = (AntProjectNode)objects[i];
		}
		return projects;
	}

	/**
	 * Adds the given project to the view
	 * 
	 * @param project the project to add
	 */
	public void addProject(AntProjectNode project) {
		contentProvider.add(project);
		projectViewer.refresh();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		handleSelectionChanged(new StructuredSelection(project));
	}

	/**
	 * Removes the given project from the view
	 * 
	 * @param project the project to remove
	 */
	private void removeProject(AntProjectNode project) {
		removeProjectFromContentProvider(project);
		projectViewer.refresh();
		setProjectViewerSelectionAfterDeletion();
	}

	private void setProjectViewerSelectionAfterDeletion() {
		Object[] children= getProjects();
		if (children.length > 0) {
			ViewerComparator comparator= projectViewer.getComparator();
			comparator.sort(projectViewer, children);
			IStructuredSelection selection= new StructuredSelection(children[0]);
			projectViewer.setSelection(selection);
			handleSelectionChanged(selection);
		}
	}

	/**
	 * Removes the given list of <code>AntProjectNode</code> objects from the view.
	 * This method should be called whenever multiple projects are to be removed
	 * because this method optimizes the viewer refresh associated with removing
	 * multiple items.
	 * 
	 * @param projectNodes the list of <code>ProjectNode</code> objects to
	 * remove
	 */
	public void removeProjects(List projectNodes) {
		Iterator iter = projectNodes.iterator();
		while (iter.hasNext()) {
			AntProjectNode project = (AntProjectNode) iter.next();
			removeProjectFromContentProvider(project);
		}
		projectViewer.refresh();
		setProjectViewerSelectionAfterDeletion();
	}

	/**
	 * Removes the given project node from the project content provider.
	 * 
	 * @param project the project to remove
	 */
	private void removeProjectFromContentProvider(AntProjectNode project) {
	    project.dispose();
		contentProvider.remove(project);
		if (getProjects().length == 0) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		}
	}

	/**
	 * Removes all projects from the view
	 */
	public void removeAllProjects() {
	    AntProjectNode[] projects= getProjects();
	    for (int i = 0; i < projects.length; i++) {
            AntProjectNode node = projects[i];
            node.dispose();
        }
		// Remove all projects
		contentProvider.removeAll();
		
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		updateProjectActions();
		projectViewer.refresh();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
		restoreViewerInput(memento);
		if (memento != null) {
			IMemento child= memento.getChild(TAG_FILTER_INTERNAL_TARGETS);
			if (child != null) {
				filterInternalTargets= Boolean.valueOf(child.getString(KEY_VALUE)).booleanValue();
			}
		}
	}

	/**
	 * Restore the viewer content that was persisted
	 * 
	 * @param  memento the memento containing the persisted viewer content
	 */
	private void restoreViewerInput(IMemento memento) {
		if (memento == null) {
			restoredViewerInput= new Object[0];
			return;
		}
		IMemento[] projects = memento.getChildren(TAG_PROJECT);
		if (projects.length < 1) {
			restoredViewerInput= new Object[0];
			return;
		}
		List projectNodes = new ArrayList(projects.length);
		for (int i = 0; i < projects.length; i++) {
			IMemento projectMemento = projects[i];
			String pathString = projectMemento.getString(KEY_PATH);
			if (!ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(pathString)).exists()) {
				// If the file no longer exists, don't add it.
				continue;
			}
			String nameString = projectMemento.getString(KEY_NAME);
			String defaultTarget= projectMemento.getString(KEY_DEFAULT);
			String errorString = projectMemento.getString(KEY_ERROR);
			String warningString = projectMemento.getString(KEY_WARNING);

			AntProjectNodeProxy project = null;
			if (nameString == null) {
				nameString = IAntCoreConstants.EMPTY_STRING;
			}
			project = new AntProjectNodeProxy(nameString, pathString);
			if (errorString != null && Boolean.valueOf(errorString).booleanValue()) {
				project.setProblemSeverity(AntModelProblem.SEVERITY_ERROR);
			} else if (warningString != null && Boolean.valueOf(warningString).booleanValue()) {
			    project.setProblemSeverity(AntModelProblem.SEVERITY_WARNING);
			}
			if (defaultTarget != null) {
				project.setDefaultTargetName(defaultTarget);
			}
			projectNodes.add(project);
		}
		restoredViewerInput= projectNodes.toArray(new AntProjectNode[projectNodes.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		// Save the projects
		AntProjectNode[] projects = getProjects();
		AntProjectNode project;
		IMemento projectMemento;
		for (int i = 0; i < projects.length; i++) {
			project= projects[i];
			projectMemento = memento.createChild(TAG_PROJECT);
			projectMemento.putString(KEY_PATH, project.getBuildFileName());
			projectMemento.putString(KEY_NAME, project.getLabel());
			String defaultTarget= project.getDefaultTargetName();
			if (project.isErrorNode()) {
				projectMemento.putString(KEY_ERROR, String.valueOf(true));
			} else {
			    if (project.isWarningNode()) {
			        projectMemento.putString(KEY_WARNING, String.valueOf(true));
			    }
				if (defaultTarget != null) {
					projectMemento.putString(KEY_DEFAULT, defaultTarget);
				}
				projectMemento.putString(KEY_ERROR, String.valueOf(false));
			}
		}
		IMemento filterTargets= memento.createChild(TAG_FILTER_INTERNAL_TARGETS);
		filterTargets.putString(KEY_VALUE, isFilterInternalTargets() ? String.valueOf(true) : String.valueOf(false));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		fgTempMemento= XMLMemento.createWriteRoot("AntViewMemento"); //$NON-NLS-1$
		saveState(fgTempMemento);
		super.dispose();
		if (openWithMenu != null) {
			openWithMenu.dispose();
		}
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta != null) {
			AntProjectNode projects[]= getProjects();
			IPath buildFilePath;
			for (int i = 0; i < projects.length; i++) {
				buildFilePath= new Path(projects[i].getBuildFileName());
				IResourceDelta change= delta.findMember(buildFilePath);
				if (change != null) {
					handleChangeDelta(change, projects[i]);
				}
			}
		}
	}
	
	/**
	 * Update the view for the given resource delta. The delta is a resource
	 * delta for the given build file in the view
	 * 
	 * @param delta a delta for a build file in the view
	 * @param project the project node that has changed
	 */
	private void handleChangeDelta(IResourceDelta delta, final AntProjectNode project) {
		IResource resource= delta.getResource();
		if (resource.getType() != IResource.FILE) {
			return;
		}
		if (delta.getKind() == IResourceDelta.REMOVED) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					removeProject(project);
				}
			});
		} else if (delta.getKind() == IResourceDelta.CHANGED && (delta.getFlags() & IResourceDelta.CONTENT) != 0) {
			handleBuildFileChanged(project);
		}
	}
	
	private void fillMainToolBar() {
		IToolBarManager toolBarMgr= getViewSite().getActionBars().getToolBarManager();
		toolBarMgr.removeAll();
		
		toolBarMgr.add(addBuildFileAction);
		toolBarMgr.add(searchForBuildFilesAction);
		toolBarMgr.add(filterInternalTargetsAction);

		toolBarMgr.add(runTargetAction);
		toolBarMgr.add(removeProjectAction);
		toolBarMgr.add(removeAllAction);
		
		toolBarMgr.update(false);	
	}
	
	private AntElementNode getSelectionNode() {
		IStructuredSelection selection= (IStructuredSelection)getViewer().getSelection();
		if (selection.size() == 1) {
			Object element= selection.getFirstElement(); 
			if (element instanceof AntElementNode) {
				AntElementNode node= (AntElementNode) element;
				return node;
			}
		}
		return null;		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IShowInSource#getShowInContext()
	 */
	public ShowInContext getShowInContext() {
		AntElementNode node= getSelectionNode();
		if (node != null) {
			IFile buildFile= node.getIFile();
			if (buildFile != null) {
				ISelection selection= new StructuredSelection(buildFile);
				return new ShowInContext(null, selection);
			}
		}
		return null;
	}

	/**
	 * Returns whether internal targets are currently being filtered out of
	 * the view.
	 * 
	 * @return whether or not internal targets are being filtered out
	 */
	public boolean isFilterInternalTargets() {
		return filterInternalTargets;
	}

	/**
	 * Sets whether internal targets should be filtered out of the view.
	 * 
	 * @param filter whether or not internal targets should be filtered out
	 */
	public void setFilterInternalTargets(boolean filter) {
		filterInternalTargets= filter;
		if (filter) {
			projectViewer.addFilter(getInternalTargetsFilter());
		} else {
			projectViewer.removeFilter(getInternalTargetsFilter());
		}
	}
	
	private ViewerFilter getInternalTargetsFilter() {
		if (fInternalTargetFilter == null) {
			fInternalTargetFilter= new InternalTargetFilter();
		}
		return fInternalTargetFilter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (getViewer() != null) {
			getViewer().getControl().setFocus();
		}
	}
}
