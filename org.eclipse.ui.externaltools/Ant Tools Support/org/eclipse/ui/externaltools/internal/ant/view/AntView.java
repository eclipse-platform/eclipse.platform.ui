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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
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
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.ant.view.actions.ActivateTargetAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.AddBuildFileAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.AntViewOpenWithMenu;
import org.eclipse.ui.externaltools.internal.ant.view.actions.DeactivateTargetAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.RemoveAllAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.RemoveProjectAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.RunActiveTargetsAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.RunTargetAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.SearchForBuildFilesAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.SwitchAntViewOrientation;
import org.eclipse.ui.externaltools.internal.ant.view.actions.TargetMoveDownAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.TargetMoveUpAction;
import org.eclipse.ui.externaltools.internal.ant.view.elements.ProjectNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.RootNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
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
	 * Key used to store the ant view's orientation
	 */
	private static String ANT_VIEW_ORIENTATION= "AntView.orientationSetting"; //$NON-NLS-1$

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
	private SwitchAntViewOrientation horizontalOrientationAction;
	private SwitchAntViewOrientation verticalOrientationAction;
	// ProjectViewer actions
	private RunTargetAction runTargetAction;
	private RemoveProjectAction removeProjectAction;
	private RemoveAllAction removeAllAction;
	private ActivateTargetAction activateTargetAction;
	private AntViewOpenWithMenu openWithMenu;
	// TargetsViewer actions
	private RunActiveTargetsAction runActiveTargetsAction;
	private DeactivateTargetAction deactivateTargetAction;
	private TargetMoveUpAction moveUpAction;
	private TargetMoveDownAction moveDownAction;

	/**
	 * Map of build files (IFile) to project nodes (ProjectNode)
	 */
	private Map buildFilesToProjects = new HashMap();
	private IResourceDeltaVisitor visitor = null;

	/**
	 * Visits a resource delta to determine if a file in the Ant view has
	 * changed
	 */
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
					IProject project = resource.getProject();
					if (!project.isOpen()) {
						handleProjectClosed();
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
			if (target.getProject().equals(project)) {
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
		final ProjectNode project = (ProjectNode) buildFilesToProjects.get(file.getLocation().toString());
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
	 * If any files present in the view no longer exist, remove them from the
	 * view.
	 */
	private void handleProjectClosed() {
		ProjectNode[] projects = projectContentProvider.getRootNode().getProjects();
		final List projectsToRemove = new ArrayList();
		for (int i = 0; i < projects.length; i++) {
			ProjectNode projectNode = projects[i];
			if (!AntUtil.getFile(projectNode.getBuildFileName()).exists()) {
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
		// Must set view orientation after actions have been initialized
		int orientation;
		try {
			orientation= getDialogSettings().getInt(ANT_VIEW_ORIENTATION);
		} catch (NumberFormatException exception) {
			orientation= SWT.VERTICAL;
		}		
		setViewOrientation(orientation);
	}
	
	private IDialogSettings getDialogSettings() {
		return ExternalToolsPlugin.getDefault().getDialogSettings();
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
			addOpenWithMenu(menu);
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

	private void addOpenWithMenu(IMenuManager menu) {
		IStructuredSelection selection= (IStructuredSelection)getProjectViewer().getSelection();
		if (selection.size() == 1) {
			Object element= selection.getFirstElement(); 
			if (element instanceof ProjectNode) {
				IFile buildFile= AntUtil.getFile(((ProjectNode)element).getBuildFileName());
				if (buildFile != null && buildFile.exists()) {
					menu.add(new Separator("group.open")); //$NON-NLS-1$
					IMenuManager submenu= new MenuManager(AntViewMessages.getString("AntView.Open_With_3"));  //$NON-NLS-1$
					openWithMenu.setFile(buildFile);
					submenu.add(openWithMenu);
					menu.appendToGroup("group.open", submenu); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Adds the actions to the toolbar
	 */
	private void createToolbarActions() {
		IActionBars actionBars= getViewSite().getActionBars();
		IMenuManager menuManager= actionBars.getMenuManager();
		menuManager.add(horizontalOrientationAction);
		menuManager.add(verticalOrientationAction);
		
		IToolBarManager toolBarMgr = actionBars.getToolBarManager();
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
		openWithMenu= new AntViewOpenWithMenu(this.getViewSite().getPage());
		horizontalOrientationAction= new SwitchAntViewOrientation(this, SWT.HORIZONTAL);
		verticalOrientationAction= new SwitchAntViewOrientation(this, SWT.VERTICAL);
	}
	
	/**
	 * Create the viewer which displays the active targets
	 */
	private void createTargetViewer() {
		ViewForm targetForm = new ViewForm(sashForm, SWT.NONE);
		CLabel title = new CLabel(targetForm, SWT.NONE);
		title.setText(AntViewMessages.getString("AntView.Active_Targets_5")); //$NON-NLS-1$
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

	/**
	 * Updates the enabled state of all IUpdate actions
	 */
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
		title.setText(AntViewMessages.getString("AntView.Build_Files_6")); //$NON-NLS-1$
		projectForm.setTopLeft(title);
		projectToolBar = new ToolBar(projectForm, SWT.FLAT | SWT.WRAP);
		projectForm.setTopRight(projectToolBar);

		projectViewer = new TreeViewer(projectForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		projectForm.setContent(projectViewer.getTree());
		projectContentProvider = new AntProjectContentProvider();
		projectViewer.setContentProvider(projectContentProvider);
		projectViewer.setLabelProvider(new AntViewLabelProvider());
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
				updateActions();
				Iterator selectionIter = ((IStructuredSelection) event.getSelection()).iterator();
				Object selection = null;
				if (selectionIter.hasNext()) {
					selection = selectionIter.next();
				}
				String messageString= null;
				if (!selectionIter.hasNext()) { 
					if (selection instanceof ProjectNode) {
						ProjectNode project = (ProjectNode) selection;
						StringBuffer message= new StringBuffer(project.getBuildFileName());
						String description= project.getDescription();
						if (description != null) {
							message.append(": "); //$NON-NLS-1$
							message.append(description);
						}
						messageString= message.toString();
					} else if (selection instanceof TargetNode){
						TargetNode target = (TargetNode) selection;
						StringBuffer message= new StringBuffer(target.getName());
						message.append(": "); //$NON-NLS-1$
						String description= target.getDescription();
						if (description == null) {
							description= AntViewMessages.getString("AntView.(no_description)_9"); //$NON-NLS-1$
						}
						message.append(description);
						messageString= message.toString();
					}
				} 
				
				AntView.this.getViewSite().getActionBars().getStatusLineManager().setMessage(messageString);
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
			if (project.equals(target.getProject())) {
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
		// Save the projects
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
		// Save the active targets
		Iterator targets = targetContentProvider.getTargets().iterator();
		IMemento targetMemento;
		TargetNode target;
		while (targets.hasNext()) {
			target = ((TargetNode) targets.next());
			targetMemento = memento.createChild(TAG_TARGET);
			targetMemento.putString(KEY_PATH, target.getProject().getBuildFileName());
			targetMemento.putString(KEY_NAME, target.getName());
		}
	}

	/**
	 * Moves the selected targets up in the list of active targets
	 */
	public void moveUpTargets() {
		int indices[] = targetViewer.getTable().getSelectionIndices();
		if (indices.length == 0) {
			return;
		}
		int newIndices[] = new int[indices.length];
		if (indices[0] == 0) {
			// Only perform the move if the items have somewhere to move to
			return;
		}
		for (int i = 0; i < newIndices.length; i++) {
			int index = indices[i];
			targetContentProvider.moveUpTarget(index);
			newIndices[i] = index - 1;
		}
		targetViewer.refresh();
		targetViewer.getTable().select(newIndices);
		updateActions();
	}

	/**
	 * Moves the selected targets down in the list of active targets
	 */
	public void moveDownTargets() {
		int indices[] = targetViewer.getTable().getSelectionIndices();
		if (indices.length == 0) {
			return;
		}
		int newIndices[] = new int[indices.length];
		if (indices[indices.length - 1] == targetViewer.getTable().getItemCount() - 1) {
			// Only perform the move if the items have somewhere to move to
			return;
		}
		for (int i= indices.length - 1; i >= 0; i--) {
			int index = indices[i];
			targetContentProvider.moveDownTarget(index);
			newIndices[i] = index + 1;
		}
		targetViewer.refresh();
		targetViewer.getTable().select(newIndices);
		updateActions();
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (openWithMenu != null) {
			openWithMenu.dispose();
		}
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta != null) {
			if (visitor == null) {
				visitor = new AntViewVisitor();
			}
			try {
				delta.accept(visitor);
			} catch (CoreException e) {
			}
		}
	}
	
	/**
	 * Sets  the orientation of the view's sash.
	 * 
	 * @param orientation the orientation to use. Value must be one of either
	 * <code>SWT.HORIZONTAL</code> or <code>SWT.VERTICAL</code>
	 */
	public void setViewOrientation(int orientation) {
		Assert.isTrue(orientation == SWT.HORIZONTAL || orientation == SWT.VERTICAL, AntViewMessages.getString("AntView.Invalid_orientation_set_for_Ant_view_10")); //$NON-NLS-1$
		getDialogSettings().put(ANT_VIEW_ORIENTATION, orientation);
		sashForm.setOrientation(orientation);
		if (orientation == SWT.HORIZONTAL) {
			horizontalOrientationAction.setChecked(true);
			verticalOrientationAction.setChecked(false);
		} else {
			horizontalOrientationAction.setChecked(false);
			verticalOrientationAction.setChecked(true);
		}
	}
}