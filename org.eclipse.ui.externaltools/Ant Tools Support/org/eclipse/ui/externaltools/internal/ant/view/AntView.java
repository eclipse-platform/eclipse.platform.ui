package org.eclipse.ui.externaltools.internal.ant.view;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	Roscoe Rush - Concept and prototype implementation
	IBM Corporation - Final implementation
*********************************************************************/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.externaltools.internal.ant.view.actions.ActivateTargetAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.AddBuildFileAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.DeactivateTargetAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.GoToBuildFileAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.RemoveAllAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.RemoveProjectAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.RunActiveTargetsAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.RunTargetAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.SearchForBuildFilesAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.TargetMoveDownAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.TargetMoveUpAction;
import org.eclipse.ui.externaltools.internal.ant.view.elements.ProjectNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.RootNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * A view which displays a hierarchical view of ant build files and allows the
 * user to run selected targets from those files.
 */
public class AntView extends ViewPart implements IResourceChangeListener {
	/**
	 * The root node of the project viewer as restored during initialization
	 */
	private RootNode restoredRoot = null;
	/**
	 * The selected targets of the target viewer as restored during
	 * initialization
	 */
	private List restoredTargets = new ArrayList();
	/**
	 * XML tag used to identify an ant project in storage
	 */
	private static final String TAG_PROJECT = "project"; //$NON-NLS-1$
	/**
	 * XML key used to store whether or not an ant project is an error node.
	 * Persisting this data saved a huge amount of processing at startup.
	 */
	private String KEY_ERROR = "error"; //$NON-NLS-1$
	/**
	 * XML key used to store an ant project's path
	 */
	private static final String KEY_PATH = "path"; //$NON-NLS-1$
	/**
	 * XML tag used to identify an ant target in storage
	 */
	private String TAG_TARGET = "target"; //$NON-NLS-1$
	/**
	 * XML key used to store an ant node's name
	 */
	private String KEY_NAME = "name"; //$NON-NLS-1$
	/**
	 * XML value for a boolean attribute whose value is <code>true</code>
	 */
	private String VALUE_TRUE = "true"; //$NON-NLS-1$
	/**
	 * XML value for a boolean attribute whose value is <code>false</code>
	 */
	private String VALUE_FALSE = "false"; //$NON-NLS-1$

	/**
	 * The sash form containing the project viewer and target viewer
	 */
	SashForm sashForm;

	/**
	 * The tree viewer that displays the users ant projects
	 */
	TreeViewer projectViewer;
	ToolBar projectToolBar;
	AntProjectContentProvider projectContentProvider;

	/**
	 * The table viewer that displays the users selected targets
	 */
	TableViewer targetViewer;
	ToolBar targetToolBar;
	AntTargetContentProvider targetContentProvider;

	/**
	 * Collection of <code>IUpdate</code> actions that need to update on
	 * selection changed.
	 */
	private List updateActions = new ArrayList();
	// Ant View Actions
	private AddBuildFileAction addBuildFileAction;
	private SearchForBuildFilesAction searchForBuildFilesAction;
	// ProjectViewer actions
	private RunTargetAction runTargetAction;
	private RemoveProjectAction removeProjectAction;
	private RemoveAllAction removeAllAction;
	private ActivateTargetAction activateTargetAction;
	private GoToBuildFileAction goToBuildFileAction;
	// TargetsViewer actions
	private RunActiveTargetsAction runActiveTargetsAction;
	private DeactivateTargetAction deactivateTargetAction;
	private TargetMoveUpAction moveUpAction;
	private TargetMoveDownAction moveDownAction;

	/**
	 * Map of build files (IFile) to project nodes (ProjectNode)
	 */
	private Map buildFilesToProjects = new HashMap();
	private IResourceDeltaVisitor visitor= null;
	
	class AntViewVisitor implements IResourceDeltaVisitor {
		/**
		 * Returns whether children should be visited
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			if (delta == null || (0 == (delta.getKind() & IResourceDelta.CHANGED) && 0 == (delta.getKind() & IResourceDelta.REMOVED))) {
				return false;
			}
			IResource resource = delta.getResource();
			if (resource.getType() == IResource.FILE) {
				if ("xml".equalsIgnoreCase(((IFile) resource).getFileExtension())) { //$NON-NLS-1$
					if ((delta.getKind() & IResourceDelta.CHANGED) != 0 && delta.getFlags() == IResourceDelta.CONTENT) {
						handleXMLFileChanged((IFile) resource);
					} else if ((delta.getKind() & IResourceDelta.REMOVED) != 0) {
						handleXMLFileRemoved((IFile) resource);
					}
				}
				return false;
			} else if (resource.getType() == IResource.PROJECT) {
				if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
					IProject project= resource.getProject();
					if (!project.isOpen()) {
						handleProjectClosed(project);
						return false;
					}
				}
			}
			return true;
		}
	}

	/**
	 * The given XML file has changed. If it is present in the view, refresh the
	 * view to pick up any structural changes.
	 */
	private void handleXMLFileChanged(IFile file) {
		final ProjectNode project = (ProjectNode) buildFilesToProjects.get(file.getLocation().toString());
		if (project == null) {
			return;
		}
		project.parseBuildFile();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				projectViewer.refresh(project);
			}
		});
		// Update targets pane for removed targets
		List activeTargets = targetContentProvider.getTargets();
		ListIterator iter = activeTargets.listIterator();
		while (iter.hasNext()) {
			TargetNode target = (TargetNode) iter.next();
			if (target.getParent().equals(project)) {
				TargetNode[] newTargets = project.getTargets();
				boolean oldTargetFound = false;
				for (int i = 0; i < newTargets.length; i++) {
					TargetNode newTarget = newTargets[i];
					if (newTarget.getName().equals(target.getName())) {
						// Replace the old target with the new
						oldTargetFound = true;
						iter.set(newTarget);
					}
				}
				if (!oldTargetFound) {
					// If no replacement was found for the old target, it was removed
					iter.remove();
				}
			}
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				targetViewer.refresh();
			}
		});
	}
	
	/**
	 * The given XML file has been removed from the workspace. If the file is
	 * present in the view, remove it.
	 */
	private void handleXMLFileRemoved(IFile file) {
		final ProjectNode project= (ProjectNode) buildFilesToProjects.get(file.getLocation().toString());
		if (project == null) {
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				removeProject(project);
			}
		});
	}
	
	/**
	 * The given project has been closed. If any files present in the view are
	 * contained in that project, remove them from the view.
	 */
	private void handleProjectClosed(IProject project) {
		ProjectNode[] projects= projectContentProvider.getRootNode().getProjects();
		final List projectsToRemove= new ArrayList();
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		for (int i = 0; i < projects.length; i++) {
			ProjectNode projectNode = projects[i];
			Path buildFilePath= new Path(projectNode.getBuildFileName());
			int matchingSegments= root.getLocation().matchingFirstSegments(buildFilePath);
			IFile file= root.getFile(buildFilePath.removeFirstSegments(matchingSegments));
			//if (project.equals(file.getProject())) {
			if (!file.exists()) {
				projectsToRemove.add(projectNode);
			}
		}
		if (!projectsToRemove.isEmpty()) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					removeProjects(projectsToRemove);
				}
			});
		}
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		initializeActions();
		sashForm = new SashForm(parent, SWT.NONE);
		createProjectViewer();
		createTargetViewer();
		createToolbarActions();
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
	protected void fillContextMenu(Viewer viewer, IMenuManager menu) {
		if (viewer == projectViewer) {
			menu.add(addBuildFileAction);
			menu.add(new Separator());
			menu.add(runTargetAction);
			menu.add(activateTargetAction);
			menu.add(new Separator());
			menu.add(goToBuildFileAction);
			menu.add(new Separator());
			menu.add(removeProjectAction);
			menu.add(removeAllAction);
		} else if (viewer == targetViewer) {
			menu.add(runActiveTargetsAction);
			menu.add(deactivateTargetAction);
			menu.add(new Separator());
			menu.add(moveUpAction);
			menu.add(moveDownAction);
		}
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Adds the actions to the toolbar
	 */
	private void createToolbarActions() {
		IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();
		toolBarMgr.add(addBuildFileAction);
		toolBarMgr.add(searchForBuildFilesAction);

		ToolBarManager projectManager = new ToolBarManager(projectToolBar);
		projectManager.add(runTargetAction);
		projectManager.add(removeProjectAction);
		projectManager.add(removeAllAction);
		projectManager.update(true);

		ToolBarManager targetManager = new ToolBarManager(targetToolBar);
		targetManager.add(runActiveTargetsAction);
		targetManager.add(moveDownAction);
		targetManager.add(moveUpAction);
		targetManager.update(true);

		updateActions();
	}

	/**
	 * Initialize the actions for this view
	 */
	private void initializeActions() {
		addBuildFileAction = new AddBuildFileAction(this);
		removeProjectAction = new RemoveProjectAction(this);
		updateActions.add(removeProjectAction);
		removeAllAction = new RemoveAllAction(this);
		runTargetAction = new RunTargetAction(this);
		updateActions.add(runTargetAction);
		runActiveTargetsAction = new RunActiveTargetsAction(this);
		updateActions.add(runActiveTargetsAction);
		searchForBuildFilesAction = new SearchForBuildFilesAction(this);
		activateTargetAction = new ActivateTargetAction(this);
		updateActions.add(activateTargetAction);
		deactivateTargetAction = new DeactivateTargetAction(this);
		updateActions.add(deactivateTargetAction);
		moveUpAction = new TargetMoveUpAction(this);
		updateActions.add(moveUpAction);
		moveDownAction = new TargetMoveDownAction(this);
		updateActions.add(moveDownAction);
		goToBuildFileAction= new GoToBuildFileAction(this);
		updateActions.add(goToBuildFileAction);
	}
	/**
	 * Create the viewer which displays the active targets
	 */
	private void createTargetViewer() {
		ViewForm targetForm = new ViewForm(sashForm, SWT.NONE);
		CLabel title = new CLabel(targetForm, SWT.NONE);
		title.setText("Active Targets");
		targetForm.setTopLeft(title);
		targetToolBar = new ToolBar(targetForm, SWT.FLAT | SWT.WRAP);
		targetForm.setTopRight(targetToolBar);

		targetViewer = new TableViewer(targetForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		targetForm.setContent(targetViewer.getTable());
		targetContentProvider = new AntTargetContentProvider();
		targetViewer.setContentProvider(targetContentProvider);
		Iterator targets = restoredTargets.iterator();
		while (targets.hasNext()) {
			targetContentProvider.addTarget((TargetNode) targets.next());
		}
		targetViewer.setLabelProvider(new AntTargetsLabelProvider());
		// The content provider doesn't use the input, but it input has to be set to something.
		targetViewer.setInput(ResourcesPlugin.getWorkspace());
		targetViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateActions();
			}
		});
		createContextMenu(targetViewer);
	}

	private void updateActions() {
		Iterator iter = updateActions.iterator();
		while (iter.hasNext()) {
			((IUpdate) iter.next()).update();
		}
	}

	/**
	 * Create the viewer which displays the ant projects
	 */
	private void createProjectViewer() {
		ViewForm projectForm = new ViewForm(sashForm, SWT.NONE);
		CLabel title = new CLabel(projectForm, SWT.NONE);
		title.setText("Build Files");
		projectForm.setTopLeft(title);
		projectToolBar = new ToolBar(projectForm, SWT.FLAT | SWT.WRAP);
		projectForm.setTopRight(projectToolBar);

		projectViewer = new TreeViewer(projectForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		projectForm.setContent(projectViewer.getTree());
		projectContentProvider = new AntProjectContentProvider();
		projectViewer.setContentProvider(projectContentProvider);
		projectViewer.setLabelProvider(new AntViewLabelProvider());
		projectViewer.setInput(restoredRoot);
		projectViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Iterator iter = updateActions.iterator();
				while (iter.hasNext()) {
					((IUpdate) iter.next()).update();
				}
			}
		});
		createContextMenu(projectViewer);
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
	 * Returns the list viewer that displays the active targets in this view
	 * 
	 * @return TableViewer this view's active target viewer
	 */
	public TableViewer getTargetViewer() {
		return targetViewer;
	}

	/**
	 * Returns a list of <code>TargetNode</code> objects that have been
	 * activated by the user
	 * 
	 * @return List a list of <code>TargetNode</code> objects that have been
	 * activated by the user.
	 */
	public List getActiveTargets() {
		return targetContentProvider.getTargets();
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
		buildFilesToProjects.put(project.getBuildFileName(), project);
		projectViewer.refresh();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * Activates the selected targets by adding them to the active targets
	 * viewer.
	 */
	public void activateSelectedTargets() {
		TreeItem[] items = projectViewer.getTree().getSelection();
		for (int i = 0; i < items.length; i++) {
			Object data = items[i].getData();
			if (data instanceof TargetNode) {
				targetContentProvider.addTarget((TargetNode) data);
			}
		}
		targetViewer.refresh();
	}

	/**
	 * Deactivates the selected targets by removing them from the active targets
	 * viewer.
	 */
	public void deactivateSelectedTargets() {
		int startIndex = targetViewer.getTable().getSelectionIndex();
		int indices[] = targetViewer.getTable().getSelectionIndices();
		for (int i = indices.length - 1; i >= 0; i--) {
			targetContentProvider.removeTarget(indices[i]);
		}
		targetViewer.refresh();
		targetViewer.getTable().select(startIndex - 1);
	}

	/**
	 * Removes the given project from the view
	 * 
	 * @param project the project to remove
	 */
	public void removeProject(ProjectNode project) {
		removeProjectFromContentProviders(project);
		projectViewer.refresh();
		targetViewer.refresh();
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
		targetViewer.refresh();
	}

	/**
	 * Removes the given project node from the project content provider. Also
	 * removes any targets from the given project from the target content
	 * provider.
	 * 
	 * @param project the project to remove
	 */
	private void removeProjectFromContentProviders(ProjectNode project) {
		ListIterator targets = targetContentProvider.getTargets().listIterator();
		while (targets.hasNext()) {
			TargetNode target = (TargetNode) targets.next();
			if (project.equals(target.getParent())) {
				targets.remove();
			}
		}
		projectContentProvider.getRootNode().removeProject(project);
		// Clear the file to project mapping for this project
		buildFilesToProjects.remove(project.getBuildFileName());
		if (!projectContentProvider.getRootNode().hasProjects()) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		}
	}

	/**
	 * Removes all projects from the view
	 */
	public void removeAllProjects() {
		// First, clear the active targets list
		targetContentProvider.getTargets().clear();
		// Remove all projects
		projectContentProvider.getRootNode().removeAllProjects();
		// Clear the file to project map
		buildFilesToProjects.clear();
		// Refresh the viewers
		projectViewer.refresh();
		targetViewer.refresh();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}

	/**
	 * Restore the projects and selected targets
	 * 
	 * @see org.eclipse.ui.IViewPart#init(IViewSite, IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
		restoreRoot(memento);
		restoreTargets(memento);
	}

	/**
	 * Initialize the target selection by restoring the persisted targets. This
	 * method should be called only after restoreRoot(IMemento) has been called.
	 * 
	 * @param memento the memento containing the persisted targets
	 */
	private void restoreTargets(IMemento memento) {
		if (memento == null || restoredRoot == null) {
			return;
		}
		IMemento[] targets = memento.getChildren(TAG_TARGET);
		for (int i = 0; i < targets.length; i++) {
			IMemento target = targets[i];
			String buildFileName = target.getString(KEY_PATH);
			String targetName = target.getString(KEY_NAME);
			ProjectNode[] projects = restoredRoot.getProjects();
			for (int j = 0; j < projects.length; j++) {
				ProjectNode project = projects[j];
				if (project.getBuildFileName().equals(buildFileName)) {
					TargetNode[] projectTargets = project.getTargets();
					for (int k = 0; k < projectTargets.length; k++) {
						if (projectTargets[k].getName().equals(targetName)) {
							restoredTargets.add(projectTargets[k]);
						}
					}
				}
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
			String nameString = projectMemento.getString(KEY_NAME);
			String errorString = projectMemento.getString(KEY_ERROR);

			ProjectNode project = null;
			if (nameString == null) {
				nameString = ""; //$NON-NLS-1$
			}
			project = new ProjectNode(nameString, pathString);
			if (errorString != null && errorString.equals(VALUE_TRUE)) {
				project.setIsErrorNode(true);
			}
			projectNodes.add(project);
			buildFilesToProjects.put(project.getBuildFileName(), project);
		}
		restoredRoot = new RootNode((ProjectNode[]) projectNodes.toArray(new ProjectNode[projectNodes.size()]));
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * Save the contents of the project viewer and the target viewer
	 * 
	 * @see org.eclipse.ui.IViewPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
		ProjectNode[] projects = projectContentProvider.getRootNode().getProjects();
		ProjectNode project;
		IMemento projectMemento;
		for (int i = 0; i < projects.length; i++) {
			project = projects[i];
			projectMemento = memento.createChild(TAG_PROJECT);
			projectMemento.putString(KEY_PATH, project.getBuildFileName());
			projectMemento.putString(KEY_NAME, project.getName());
			if (project.isErrorNode()) {
				projectMemento.putString(KEY_ERROR, VALUE_TRUE);
			} else {
				projectMemento.putString(KEY_ERROR, VALUE_FALSE);
			}
		}
		Iterator targets = targetContentProvider.getTargets().iterator();
		IMemento targetMemento;
		TargetNode target;
		while (targets.hasNext()) {
			target = ((TargetNode) targets.next());
			targetMemento = memento.createChild(TAG_TARGET);
			targetMemento.putString(KEY_PATH, ((ProjectNode) target.getParent()).getBuildFileName());
			targetMemento.putString(KEY_NAME, target.getName());
		}
	}

	/**
	 * Moves the given target up in the list of active targets
	 */
	public void moveUpTarget(TargetNode target) {
		int index = targetViewer.getTable().getSelectionIndex();
		if (index < 0) {
			return;
		}
		targetContentProvider.moveUpTarget(index);
		targetViewer.refresh();
		targetViewer.getTable().select(index - 1);
	}

	/**
	 * Moves the given target down in the list of active targets
	 */
	public void moveDownTarget(TargetNode target) {
		int index = targetViewer.getTable().getSelectionIndex();
		if (index < 0) {
			return;
		}
		targetContentProvider.moveDownTarget(index);
		targetViewer.refresh();
		targetViewer.getTable().select(index + 1);
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		if (delta != null) {
			if (visitor == null) {
				visitor= new AntViewVisitor();
			}			
			try {
				delta.accept(visitor);
			} catch (CoreException e) {
			}
		}
	}
}