/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Roscoe Rush - Concept and prototype implementation
 *     IBM Corporation - current implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.AntUtil;
import org.eclipse.ant.internal.ui.IAntUIHelpContextIds;
import org.eclipse.ant.internal.ui.views.actions.AddBuildFilesAction;
import org.eclipse.ant.internal.ui.views.actions.AntOpenWithMenu;
import org.eclipse.ant.internal.ui.views.actions.FilterInternalTargetsAction;
import org.eclipse.ant.internal.ui.views.actions.RefreshBuildFilesAction;
import org.eclipse.ant.internal.ui.views.actions.RemoveAllAction;
import org.eclipse.ant.internal.ui.views.actions.RemoveProjectAction;
import org.eclipse.ant.internal.ui.views.actions.RunTargetAction;
import org.eclipse.ant.internal.ui.views.actions.SearchForBuildFilesAction;
import org.eclipse.ant.internal.ui.views.elements.ProjectNode;
import org.eclipse.ant.internal.ui.views.elements.RootNode;
import org.eclipse.ant.internal.ui.views.elements.TargetNode;
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
import org.eclipse.jface.viewers.ViewerSorter;
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
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * A view which displays a hierarchical view of ant build files and allows the
 * user to run selected targets from those files.
 */
public class AntView extends ViewPart implements IResourceChangeListener, IShowInSource {
	/**
	 * The root node of the project viewer as restored during initialization
	 */
	private RootNode restoredRoot = null;
	private boolean restoredFilterInternalTargets= false;
	/**
	 * This memento allows the Ant view to save and restore state
	 * when it is closed and opened within a session. A different
	 * memento is supplied by the platform for persistance at
	 * workbench shutdown.
	 */
	private static IMemento tempMemento = null;

	/**
	 * XML tag used to identify an ant project in storage
	 */
	private static final String TAG_PROJECT = "project"; //$NON-NLS-1$
	/**
	 * XML key used to store whether or not an ant project is an error node.
	 * Persisting this data saved a huge amount of processing at startup.
	 */
	private static final String KEY_ERROR = "error"; //$NON-NLS-1$
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
	private AntProjectContentProvider projectContentProvider;

	/**
	 * Collection of <code>IUpdate</code> actions that need to update on
	 * selection changed in the project viewer.
	 */
	private List updateProjectActions;
	// Ant View Actions
	private AddBuildFilesAction addBuildFileAction;
	private SearchForBuildFilesAction searchForBuildFilesAction;
	private RefreshBuildFilesAction refreshBuildFilesAction;
	private RunTargetAction runTargetAction;
	private RemoveProjectAction removeProjectAction;
	private RemoveAllAction removeAllAction;
	private FilterInternalTargetsAction filterInternalTargetsAction;
	// Context-menu-only actions
	private AntOpenWithMenu openWithMenu;
	private RunTargetAction editConfigAction;

	/**
	 * The given build file has changed. Refresh the view to pick up any
	 * structural changes.
	 */
	private void handleBuildFileChanged(ProjectNode project) {
		project.parseBuildFile();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				//must do a full refresh to re-sort
				projectViewer.refresh();
			}
		});
	}

	/**
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
		WorkbenchHelp.setHelp(parent, IAntUIHelpContextIds.ANT_VIEW);
		updateProjectActions();
	}
	
	private void initializeDragAndDrop() {
		int ops = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT;
		Transfer[] transfers = new Transfer[] { FileTransfer.getInstance() };
		TreeViewer viewer = getProjectViewer();
		AntViewDropAdapter adapter = new AntViewDropAdapter(this);
		viewer.addDropSupport(ops, transfers, adapter);
	}

	/**
	 * Creates a pop-up menu on the given control
	 *
	 * @param menuControl the control with which the pop-up
	 *  menu will be associated
	 */
	private void createContextMenu(final Viewer viewer) {
		Control menuControl = viewer.getControl();
		MenuManager menuMgr = new MenuManager("#PopUp"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(viewer, mgr);
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
	private void fillContextMenu(Viewer viewer, IMenuManager menu) {
		if (viewer == projectViewer) {
			menu.add(runTargetAction);
			menu.add(editConfigAction);
			menu.add(new Separator());
			addOpenWithMenu(menu);
			menu.add(new Separator());
			menu.add(addBuildFileAction);
			menu.add(removeProjectAction);
			menu.add(removeAllAction);
			menu.add(refreshBuildFilesAction);
		}
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void addOpenWithMenu(IMenuManager menu) {
		IFile buildFile= getSelectionBuildFile();
		if (buildFile != null && buildFile.exists()) {
			menu.add(new Separator("group.open")); //$NON-NLS-1$
			IMenuManager submenu= new MenuManager(AntViewMessages.getString("AntView.Open_With_3"));  //$NON-NLS-1$
			openWithMenu.setFile(buildFile);
			submenu.add(openWithMenu);
			menu.appendToGroup("group.open", submenu); //$NON-NLS-1$
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
		
		runTargetAction = new RunTargetAction(this, false);
		updateProjectActions.add(runTargetAction);
		
		searchForBuildFilesAction = new SearchForBuildFilesAction(this);
		
		refreshBuildFilesAction = new RefreshBuildFilesAction(this);
		updateProjectActions.add(refreshBuildFilesAction); 
		
		openWithMenu= new AntOpenWithMenu(this.getViewSite().getPage());
		
		editConfigAction= new RunTargetAction(this, true);
		updateProjectActions.add(editConfigAction);
		
		filterInternalTargetsAction= new FilterInternalTargetsAction(this);
	}

	/**
	 * Updates the enabled state of all IUpdate actions associated 
	 * with the project viewer.
	 */
	private void updateProjectActions() {
		Iterator iter = updateProjectActions.iterator();
		while (iter.hasNext()) {
			((IUpdate) iter.next()).update();
		}
	}

	/**
	 * Create the viewer which displays the ant projects
	 */
	private void createProjectViewer(Composite parent) {
		projectViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		projectContentProvider = new AntProjectContentProvider();
		projectViewer.setContentProvider(projectContentProvider);
		projectContentProvider.setFilterInternalTargets(restoredFilterInternalTargets);
		filterInternalTargetsAction.setChecked(restoredFilterInternalTargets);
		projectViewer.setLabelProvider(new AntViewLabelProvider());
		if (tempMemento != null) {
			restoreRoot(tempMemento);
		}
		projectViewer.setInput(restoredRoot);
		projectViewer.setSorter(new ViewerSorter() {
			/**
			 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			public int compare(Viewer viewer, Object e1, Object e2) {
				return e1.toString().compareToIgnoreCase(e2.toString());
			}
		});
		
		projectViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
		
		projectViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (!event.getSelection().isEmpty()) {
					handleProjectViewerDoubleClick(event);
				}
			}
		});
		
		projectViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				handleProjectViewerKeyPress(event);
			}
		});
		
		createContextMenu(projectViewer);
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
	
	private void handleProjectViewerDoubleClick(DoubleClickEvent event) {
		ISelection s= event.getSelection();
		if (s.isEmpty() || !(s instanceof IStructuredSelection)) {
			return;
		}
		Object selection= ((IStructuredSelection)s).getFirstElement();
		if (selection instanceof ProjectNode) {
			ProjectNode project = (ProjectNode) selection;
			IEditorRegistry registry= PlatformUI.getWorkbench().getEditorRegistry();
			IFile file= AntUtil.getFile(project.getBuildFileName());
			IEditorDescriptor editor = IDE.getDefaultEditor(file);
			if (editor == null) {
				editor= registry.findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
			}
			try {
				if (editor == null) {
					getViewSite().getPage().openEditor(new FileEditorInput(file), IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
				} else {
					getViewSite().getPage().openEditor(new FileEditorInput(file), editor.getId());
				}
			} catch (PartInitException e) {
				AntUIPlugin.log(e);
			}
		} else if (selection instanceof TargetNode){
			runTargetAction.run();
		}
	}
	
	/**
	 * Updates the actions and status line for selection change in one of the
	 * viewers.
	 */
	private void handleSelectionChanged(SelectionChangedEvent event) {
		updateProjectActions();
		Iterator selectionIter = ((IStructuredSelection) event.getSelection()).iterator();
		Object selection = null;
		if (selectionIter.hasNext()) {
			selection = selectionIter.next();
		}
		String messageString= null;
		if (!selectionIter.hasNext()) { 
			messageString= getStatusLineText(selection);
		} 
		getViewSite().getActionBars().getStatusLineManager().setMessage(messageString);
	}
	
	/**
	 * Returns text appropriate for display in the workbench status line for the
	 * given node.
	 */
	private static String getStatusLineText(Object node) {
		if (node instanceof ProjectNode) {
			ProjectNode project = (ProjectNode) node;
			StringBuffer message= new StringBuffer(project.getBuildFileName());
			String description= project.getDescription();
			if (description != null) {
				message.append(": "); //$NON-NLS-1$
				message.append(description);
			}
			return message.toString();
		} else if (node instanceof TargetNode) {
			TargetNode target = (TargetNode) node;
			StringBuffer message= new StringBuffer(AntViewMessages.getString("AntView.Name___1")); //$NON-NLS-1$
			message.append(target.getName());
			message.append('\"');
			String[] depends= target.getDependencies();
			if (depends.length > 0 ) {
				message.append(AntViewMessages.getString("AntView._Depends___2")); //$NON-NLS-1$
				message.append(depends[0]); // Unroll the loop to avoid trailing comma
				for (int i = 1; i < depends.length; i++) {
					message.append(',').append(depends[i]);
				}
				message.append('\"');
			}
			String description= target.getDescription();
			if (description != null && description.length() != 0) {
				message.append(AntViewMessages.getString("AntView._Description___4")); //$NON-NLS-1$
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
	public TreeViewer getProjectViewer() {
		return projectViewer;
	}

	/**
	 * Returns the <code>ProjectNode</code>s currently displayed in this view.
	 * 
	 * @return ProjectNode[] the <code>ProjectNode</code>s currently displayed
	 * in this view
	 */
	public ProjectNode[] getProjects() {
		return projectContentProvider.getRootNode().getProjects();
	}

	/**
	 * Adds the given project project to the view
	 * 
	 * @param project the project to add
	 */
	public void addProject(ProjectNode project) {
		projectContentProvider.addProject(project);
		projectViewer.refresh();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		updateProjectActions();
	}

	/**
	 * Removes the given project from the view
	 * 
	 * @param project the project to remove
	 */
	private void removeProject(ProjectNode project) {
		removeProjectFromContentProviders(project);
		projectViewer.refresh();
		if (getProjects().length == 0) {		
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		}
		setProjectViewerSelectionAfterDeletion();
	}

	private void setProjectViewerSelectionAfterDeletion() {
		Object[] children= projectContentProvider.getChildren(projectContentProvider.getRootNode());
		if (children.length > 0) {
			ViewerSorter sorter= projectViewer.getSorter();
			sorter.sort(projectViewer, children);
			projectViewer.setSelection(new StructuredSelection(children[0]));
		}
	}

	/**
	 * Removes the given list of <code>ProjectNode</code> objects from the view.
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
			ProjectNode project = (ProjectNode) iter.next();
			removeProjectFromContentProviders(project);
		}
		projectViewer.refresh();
		setProjectViewerSelectionAfterDeletion();
	}

	/**
	 * Removes the given project node from the project content provider. Also
	 * removes any targets from the given project from the target content
	 * provider.
	 * 
	 * @param project the project to remove
	 */
	private void removeProjectFromContentProviders(ProjectNode project) {
		projectContentProvider.getRootNode().removeProject(project);
		if (!projectContentProvider.getRootNode().hasProjects()) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		}
	}

	/**
	 * Removes all projects from the view
	 */
	public void removeAllProjects() {
		// Remove all projects
		projectContentProvider.getRootNode().removeAllProjects();
		// Refresh the viewer
		projectViewer.refresh();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		updateProjectActions();
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		if (getProjectViewer() != null) {
			getProjectViewer().getControl().setFocus();
		}
	}

	/**
	 * Restore the projects and selected targets
	 * 
	 * @see org.eclipse.ui.IViewPart#init(IViewSite, IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
		restoreRoot(memento);
		if (memento != null) {
			IMemento child= memento.getChild(TAG_FILTER_INTERNAL_TARGETS);
			if (child != null) {
				restoredFilterInternalTargets= Boolean.valueOf(child.getString(KEY_VALUE)).booleanValue();
			}
		}
	}

	/**
	 * Initialize the root node by restoring the persisted projects
	 * 
	 * @param  memento the memento containing the persisted projects
	 */
	private void restoreRoot(IMemento memento) {
		if (memento == null) {
			restoredRoot = new RootNode();
			return;
		}
		IMemento[] projects = memento.getChildren(TAG_PROJECT);
		if (projects.length < 1) {
			restoredRoot = new RootNode();
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

			ProjectNode project = null;
			if (nameString == null) {
				nameString = ""; //$NON-NLS-1$
			}
			project = new ProjectNode(nameString, pathString);
			if (errorString != null && Boolean.valueOf(errorString).booleanValue()) {
				project.setIsErrorNode(true);
			}
			if (defaultTarget != null) {
				project.setDefaultTargetName(defaultTarget);
			}
			projectNodes.add(project);
		}
		restoredRoot = new RootNode((ProjectNode[]) projectNodes.toArray(new ProjectNode[projectNodes.size()]));
	}

	/**
	 * Save the contents of the project viewer and the target viewer
	 * 
	 * @see org.eclipse.ui.IViewPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
		// Save the projects
		ProjectNode[] projects = projectContentProvider.getRootNode().getProjects();
		ProjectNode project;
		IMemento projectMemento;
		for (int i = 0; i < projects.length; i++) {
			project = projects[i];
			projectMemento = memento.createChild(TAG_PROJECT);
			projectMemento.putString(KEY_PATH, project.getBuildFileName());
			projectMemento.putString(KEY_NAME, project.getName());
			String defaultTarget= project.getDefaultTargetName();
			if (project.isErrorNode()) {
				projectMemento.putString(KEY_ERROR, String.valueOf(true));
			} else {
				if (defaultTarget != null) {
					projectMemento.putString(KEY_DEFAULT, defaultTarget);
				}
				projectMemento.putString(KEY_ERROR, String.valueOf(false));
			}
		}
		IMemento filterTargets= memento.createChild(TAG_FILTER_INTERNAL_TARGETS);
		filterTargets.putString(KEY_VALUE, isFilterInternalTargets() ? String.valueOf(true) : String.valueOf(false));
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		tempMemento= XMLMemento.createWriteRoot("AntViewMemento"); //$NON-NLS-1$
		saveState(tempMemento);
		super.dispose();
		if (openWithMenu != null) {
			openWithMenu.dispose();
		}
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta != null) {
			ProjectNode projects[]= projectContentProvider.getRootNode().getProjects();
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
	private void handleChangeDelta(IResourceDelta delta, final ProjectNode project) {
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
	
	private IFile getSelectionBuildFile() {
		IStructuredSelection selection= (IStructuredSelection)getProjectViewer().getSelection();
		if (selection.size() == 1) {
			Object element= selection.getFirstElement(); 
			ProjectNode projectNode= null;
			if (element instanceof ProjectNode) {
				projectNode= (ProjectNode)element;
			} else if (element instanceof TargetNode) {
				projectNode= ((TargetNode)element).getProject();
			}
			if (projectNode != null) {
				return AntUtil.getFile(projectNode.getBuildFileName());
			}
		}
		return null;		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IShowInSource#getShowInContext()
	 */
	public ShowInContext getShowInContext() {
		IFile buildFile= getSelectionBuildFile();
		if (buildFile != null && buildFile.exists()) {
			ISelection selection= new StructuredSelection(buildFile);
			return new ShowInContext(null, selection);
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
		if (projectContentProvider != null) {
			return projectContentProvider.isFilterInternalTargets();
		}
		return false;
	}

	/**
	 * Sets whether internal targets should be filtered out of the view.
	 * 
	 * @param filter whether or not internal targets should be filtered out
	 */
	public void setFilterInternalTargets(boolean filter) {
		if (projectContentProvider != null) {
			projectContentProvider.setFilterInternalTargets(filter);
			projectViewer.refresh();
		}
	}
}