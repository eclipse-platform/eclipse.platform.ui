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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.ant.view.actions.ActivateTargetAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.AddBuildFileAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.AntViewOpenWithMenu;
import org.eclipse.ui.externaltools.internal.ant.view.actions.DeactivateTargetAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.EditLaunchConfigurationAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.RemoveAllAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.RemoveProjectAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.RunActiveTargetsAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.RunTargetAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.SearchForBuildFilesAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.TargetMoveDownAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.TargetMoveUpAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.ToggleAntViewOrientation;
import org.eclipse.ui.externaltools.internal.ant.view.elements.ProjectNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.RootNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.help.WorkbenchHelp;
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
	/**
	 * The selected targets of the target viewer as restored during
	 * initialization
	 */
	private List restoredTargets =null;
	/**
	 * Key used to store the ant view's orientation
	 */
	private static String ANT_VIEW_ORIENTATION= "AntView.orientationSetting"; //$NON-NLS-1$
	
	public static final int VERTICAL_ORIENTATION= SWT.VERTICAL;
	public static final int HORIZONTAL_ORIENTATION= SWT.HORIZONTAL;
	public static final int SINGLE_ORIENTATION= SWT.SINGLE;
	
	private int lastSplitOrientation= VERTICAL_ORIENTATION;

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
	private SashForm sashForm;
	
	private ViewForm projectForm;
	
	/**
	 * These are used to initialize and persist the position of the sash that
	 * separates the tree viewer from the detail pane.
	 */
	private static final int[] DEFAULT_SASH_WEIGHTS = {4, 4};
	private int[] lastSashWeights;
	private boolean toggledDetailOnce;

	/**
	 * The tree viewer that displays the users ant projects
	 */
	private TreeViewer projectViewer;
	private AntProjectContentProvider projectContentProvider;

	/**
	 * The table viewer that displays the users selected targets
	 */
	private TableViewer targetViewer;
	private ToolBar targetToolBar;
	private AntTargetContentProvider targetContentProvider;

	/**
	 * Collection of <code>IUpdate</code> actions that need to update on
	 * selection changed in the project viewer.
	 */
	private List updateProjectActions;
	/**
	 * Collection of <code>IUpdate</code> actions that need to update on
	 * selection changed in the target viewer.
	 */
	private List updateTargetActions;
	// Ant View Actions
	private AddBuildFileAction addBuildFileAction;
	private SearchForBuildFilesAction searchForBuildFilesAction;
	private ToggleAntViewOrientation horizontalOrientationAction;
	private ToggleAntViewOrientation verticalOrientationAction;
	private ToggleAntViewOrientation showTargetViewerAction;
	// ProjectViewer actions
	private RunTargetAction runTargetAction;
	private RemoveProjectAction removeProjectAction;
	private RemoveAllAction removeAllAction;
	private ActivateTargetAction activateTargetAction;
	private AntViewOpenWithMenu openWithMenu;
	private EditLaunchConfigurationAction editConfigAction;
	
	// TargetsViewer actions
	private RunActiveTargetsAction runActiveTargetsAction;
	private DeactivateTargetAction deactivateTargetAction;
	private TargetMoveUpAction moveUpAction;
	private TargetMoveDownAction moveDownAction;

	/**
	 * The given build file has changed. Refresh the view to pick up any
	 * structural changes.
	 */
	private void handleBuildFileChanged(final ProjectNode project) {
		project.parseBuildFile();
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
				projectViewer.refresh(project);
				targetViewer.refresh();
			}
		});
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		initializeActions();
		sashForm = new SashForm(parent, SWT.NONE);
		createProjectViewer();
		createTargetViewer();
		setTargetViewerToolbarActions();
		// Must set view orientation after actions have been initialized
		int orientation;
		try {
			orientation= getDialogSettings().getInt(ANT_VIEW_ORIENTATION);
		} catch (NumberFormatException exception) {
			orientation= SWT.VERTICAL;
		}		
		setViewOrientation(orientation);
		
		IActionBars actionBars= getViewSite().getActionBars();
		IMenuManager menuManager= actionBars.getMenuManager();
		menuManager.add(horizontalOrientationAction);
		menuManager.add(verticalOrientationAction);
		menuManager.add(showTargetViewerAction);
		if (getProjects().length > 0) {
			// If any projects have been added to the view during startup,
			// begin listening for resource changes
			ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		}
		WorkbenchHelp.setHelp(parent, IExternalToolsHelpContextIds.ANT_VIEW);
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
	private void fillContextMenu(Viewer viewer, IMenuManager menu) {
		if (viewer == projectViewer) {
			menu.add(addBuildFileAction);
			menu.add(new Separator());
			menu.add(runTargetAction);
			menu.add(activateTargetAction);
			menu.add(editConfigAction);
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
	 * Adds the actions to the target viewer toolbar
	 */
	private void setTargetViewerToolbarActions() {

		ToolBarManager targetManager = new ToolBarManager(targetToolBar);
		targetManager.add(runActiveTargetsAction);
		targetManager.add(moveDownAction);
		targetManager.add(moveUpAction);
		targetManager.update(true);

		updateProjectActions();
		updateTargetActions();
	}

	/**
	 * Initialize the actions for this view
	 */
	private void initializeActions() {
		updateProjectActions= new ArrayList(5);
		updateTargetActions= new ArrayList(4);
		
		addBuildFileAction = new AddBuildFileAction(this);
		
		removeProjectAction = new RemoveProjectAction(this);
		updateProjectActions.add(removeProjectAction);
		
		removeAllAction = new RemoveAllAction(this);
		updateProjectActions.add(removeAllAction);
		
		runTargetAction = new RunTargetAction(this);
		updateProjectActions.add(runTargetAction);
		
		runActiveTargetsAction = new RunActiveTargetsAction(this);
		updateTargetActions.add(runActiveTargetsAction);
		
		searchForBuildFilesAction = new SearchForBuildFilesAction(this);
		
		activateTargetAction = new ActivateTargetAction(this);
		updateProjectActions.add(activateTargetAction);
		
		deactivateTargetAction = new DeactivateTargetAction(this);
		updateTargetActions.add(deactivateTargetAction);
		
		moveUpAction = new TargetMoveUpAction(this);
		updateTargetActions.add(moveUpAction);
		
		moveDownAction = new TargetMoveDownAction(this);
		updateTargetActions.add(moveDownAction);
		
		openWithMenu= new AntViewOpenWithMenu(this.getViewSite().getPage());
		
		horizontalOrientationAction= new ToggleAntViewOrientation(this, HORIZONTAL_ORIENTATION);
		verticalOrientationAction= new ToggleAntViewOrientation(this, VERTICAL_ORIENTATION);
		showTargetViewerAction= new ToggleAntViewOrientation(this, SINGLE_ORIENTATION);
		
		editConfigAction= new EditLaunchConfigurationAction(this);
		updateProjectActions.add(editConfigAction);
	}
	
	/**
	 * Create the viewer which displays the active targets
	 */
	private void createTargetViewer() {
		ViewForm targetForm = new ViewForm(sashForm, SWT.NONE);
		CLabel title = new CLabel(targetForm, SWT.NONE);
		title.setText(AntViewMessages.getString("AntView.Active_Targets_5")); //$NON-NLS-1$
		title.setImage(ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_TARGET));
		targetForm.setTopLeft(title);
		targetToolBar = new ToolBar(targetForm, SWT.FLAT | SWT.WRAP);
		targetForm.setTopRight(targetToolBar);

		targetViewer = new TableViewer(targetForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		targetForm.setContent(targetViewer.getTable());
		targetContentProvider = new AntTargetContentProvider();
		targetViewer.setContentProvider(targetContentProvider);
		if (restoredTargets != null) {
			Iterator targets = restoredTargets.iterator();
			while (targets.hasNext()) {
				targetContentProvider.addTarget((TargetNode) targets.next());
			}
		}
		targetViewer.setLabelProvider(new AntViewLabelProvider());
		// The content provider doesn't use the input, but it input has to be set to something.
		targetViewer.setInput(ResourcesPlugin.getWorkspace());
		targetViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleViewerSelectionChanged(event, targetViewer);
			}
		});
		
		targetViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleTargetViewerDoubleClick(event);
			}
		});
		
		targetViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				handleTargetViewerKeyPress(event);
			}
		});
		
		createContextMenu(targetViewer);
	}
	
	private void handleTargetViewerKeyPress(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0) {
			if (deactivateTargetAction.isEnabled()) {
				deactivateTargetAction.run();
			}
		}
	}
				
	private void handleTargetViewerDoubleClick(DoubleClickEvent event) {
		ISelection s= event.getSelection();
		if (!(s instanceof IStructuredSelection)) {
			return;
		}
		Object selection= ((IStructuredSelection)s).getFirstElement();
		if (selection instanceof TargetNode) {
			runTargetAction.run((TargetNode)selection);
		}
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
	 * Updates the enabled state of all IUpdate actions associated 
	 * with the target viewer.
	 */
	private void updateTargetActions() {
		Iterator iter = updateTargetActions.iterator();
		while (iter.hasNext()) {
			((IUpdate) iter.next()).update();
		}
	}

	/**
	 * Create the viewer which displays the ant projects
	 */
	private void createProjectViewer() {
		projectForm = new ViewForm(sashForm, SWT.NONE);
		
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
				handleViewerSelectionChanged(event, projectViewer);
			}
		});
		
		projectViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleProjectViewerDoubleClick(event);
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
		}
	}
	
	private void handleProjectViewerDoubleClick(DoubleClickEvent event) {
		ISelection s= event.getSelection();
		if (!(s instanceof IStructuredSelection)) {
			return;
		}
		Object selection= ((IStructuredSelection)s).getFirstElement();
		if (selection instanceof ProjectNode) {
			ProjectNode project = (ProjectNode) selection;
			IEditorRegistry registry= PlatformUI.getWorkbench().getEditorRegistry();
			IFile file= AntUtil.getFile(project.getBuildFileName());
			IEditorDescriptor editor = registry.getDefaultEditor(file);
			if (editor == null) {
				editor= registry.getDefaultEditor();
			}
			try {
				if (editor == null) {
					getViewSite().getPage().openSystemEditor(file);
				} else {
					getViewSite().getPage().openEditor(file, editor.getId());
				}
			} catch (PartInitException e) {
				ExternalToolsPlugin.getDefault().log(e);
			}
		} else if (selection instanceof TargetNode){
			runTargetAction.run();
		}
	}
	
	/**
	 * Updates the actions and status line for selection change in one of the
	 * viewers.
	 */
	private void handleViewerSelectionChanged(SelectionChangedEvent event, Viewer source) {
		if (source == getTargetViewer()) {
			updateTargetActions();
		} else {
			updateProjectActions();
		}
		Iterator selectionIter = ((IStructuredSelection) event.getSelection()).iterator();
		Object selection = null;
		if (selectionIter.hasNext()) {
			selection = selectionIter.next();
		}
		String messageString= null;
		if (!selectionIter.hasNext()) { 
			messageString= getStatusLineText(selection);
		} 
		AntView.this.getViewSite().getActionBars().getStatusLineManager().setMessage(messageString);
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
			String[] depends= target.getDependencies();
			if (depends.length > 0 ) {
				message.append(AntViewMessages.getString("AntView._Dependencies___2")); //$NON-NLS-1$
				message.append(depends[0]); // Unroll the loop to avoid trailing comma
				for (int i = 1; i < depends.length; i++) {
					message.append(", ").append(depends[i]); //$NON-NLS-1$
				}
			}
			message.append(AntViewMessages.getString("AntView._Description___4")); //$NON-NLS-1$
			String description= target.getDescription();
			if (description == null || description.length() == 0) {
				description= AntViewMessages.getString("AntView.(no_description)_9"); //$NON-NLS-1$
			}
			message.append(description);
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
		projectViewer.refresh();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		updateProjectActions();
	}

	/**
	 * Activates the selected targets by adding them to the active targets
	 * viewer.
	 */
	public void activateSelectedTargets() {
		if (sashForm.getMaximizedControl() != null) { //SINGLE ORIENTATION
			setViewOrientation(getLastSplitOrientation());
		}
		TreeItem[] items = projectViewer.getTree().getSelection();
		for (int i = 0; i < items.length; i++) {
			Object data = items[i].getData();
			if (data instanceof TargetNode) {
				targetContentProvider.addTarget((TargetNode) data);
			}
		}
		targetViewer.refresh();
		updateTargetActions();
	}
	
	/**
	 * Show or hide the target viewer pane, based on the value of
	 * <code>on</code>. If showing, reset the sash form to use the relative
	 * weights that were in effect the last time the target viewer was visible,
	 * and populate it with active targets.  If hiding, save the current
	 * relative weights, unless the target viewer hasn't yet been shown.
	 */
	private void toggleTargetViewer(boolean on) {
		if (on) {
			if (sashForm.getMaximizedControl() != null) {
				sashForm.setMaximizedControl(null);
			}
			sashForm.setWeights(getLastSashWeights());
			toggledDetailOnce = true;
		} else {
			if (toggledDetailOnce) {
				setLastSashWeights(sashForm.getWeights());
				setLastSplitOrientation(sashForm.getOrientation());
			}
			sashForm.setMaximizedControl(projectForm);
		}
	}
	
	/**
	 * Sets the current orientation of the sash form, so that the sash form can
	 * be reset to this orientation at a later time.
	 */
	private void setLastSplitOrientation(int orientation) {
		lastSplitOrientation= orientation;
	}
	
	/**
	 * Returns the orientation that was in effect the last time both panes were
	 * visible in the sash form, or the default orientation if both panes have
	 * not yet been made visible.
	 */
	private int getLastSplitOrientation() {
		return lastSplitOrientation;
	}
	
	/**
	 * Set the current relative weights of the controls in the sash form, so that
	 * the sash form can be reset to this layout at a later time.
	 */
	private void setLastSashWeights(int[] weights) {
		lastSashWeights = weights;
	}
	
	/**
	 * Return the relative weights that were in effect the last time both panes were
	 * visible in the sash form, or the default weights if both panes have not yet been
	 * made visible.
	 */
	private int[] getLastSashWeights() {
		if (lastSashWeights == null) {
			lastSashWeights = DEFAULT_SASH_WEIGHTS;
		}
		return lastSashWeights;
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
		//note that actions are updated on selection changed
		targetViewer.getTable().select(startIndex - 1);
	}

	/**
	 * Removes the given project from the view
	 * 
	 * @param project the project to remove
	 */
	private void removeProject(ProjectNode project) {
		removeProjectFromContentProviders(project);
		projectViewer.refresh();
		targetViewer.refresh();
		if (getProjects().length == 0) {		
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
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
		// Refresh the viewers
		projectViewer.refresh();
		targetViewer.refresh();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		updateTargetActions();
		updateProjectActions();
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
		restoredTargets= new ArrayList(targets.length);
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
		// TODO: Remove the call to deselectAll() once Bug 30745 is fixed
		targetViewer.getTable().deselectAll();
		targetViewer.getTable().select(newIndices);
		updateTargetActions();
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
		// TODO: Remove the call to deselectAll() once Bug 30745 is fixed
		targetViewer.getTable().deselectAll();
		targetViewer.getTable().select(newIndices);
		updateTargetActions();
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
			IPath rootPath= ResourcesPlugin.getWorkspace().getRoot().getLocation();
			ProjectNode projects[]= projectContentProvider.getRootNode().getProjects();
			IPath buildFilePath;
			for (int i = 0; i < projects.length; i++) {
				String buildFileName= projects[i].getBuildFileName();
				buildFilePath= new Path(buildFileName);
				// Trim the file system relative path to be workspace relative 
				int matchingSegments= rootPath.matchingFirstSegments(buildFilePath);
				buildFilePath= buildFilePath.removeFirstSegments(matchingSegments);
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
	
	/**
	 * Sets the orientation of the view's sash.
	 * 
	 * @param orientation the orientation to use. Value must be one of either
	 * <code>SWT.HORIZONTAL</code> or <code>SWT.VERTICAL</code>
	 */
	public void setViewOrientation(int orientation) {
		Assert.isTrue(orientation == HORIZONTAL_ORIENTATION || orientation == VERTICAL_ORIENTATION || orientation == SINGLE_ORIENTATION, AntViewMessages.getString("AntView.Invalid_orientation_set_for_Ant_view_10")); //$NON-NLS-1$
		getDialogSettings().put(ANT_VIEW_ORIENTATION, orientation);
		if (orientation == HORIZONTAL_ORIENTATION) {
			toggleTargetViewer(true);
			sashForm.setOrientation(orientation);
			horizontalOrientationAction.setChecked(true);
			verticalOrientationAction.setChecked(false);
			showTargetViewerAction.setChecked(false);
		} else if (orientation == VERTICAL_ORIENTATION) {
			toggleTargetViewer(true);
			sashForm.setOrientation(orientation);
			horizontalOrientationAction.setChecked(false);
			showTargetViewerAction.setChecked(false);
			verticalOrientationAction.setChecked(true);
		} else {
			horizontalOrientationAction.setChecked(false);
			showTargetViewerAction.setChecked(true);
			verticalOrientationAction.setChecked(false);
			toggleTargetViewer(false);
		}
		updateMainToolbar(orientation);
	}
	
	private void updateMainToolbar(int orientation) {
		IActionBars actionBars= getViewSite().getActionBars();
		IToolBarManager tbmanager= actionBars.getToolBarManager();	
			
		if (orientation == SWT.HORIZONTAL) {
			clearMainToolBar(tbmanager);
			ToolBar projectViewerToolBar= new ToolBar(projectForm, SWT.FLAT | SWT.WRAP);
			fillMainToolBar(new ToolBarManager(projectViewerToolBar));
			projectForm.setTopLeft(projectViewerToolBar);
		} else {
			projectForm.setTopLeft(null);
			fillMainToolBar(tbmanager);
		}
	}
	
	private void fillMainToolBar(IToolBarManager toolBarMgr) {
		toolBarMgr.removeAll();
		
		toolBarMgr.add(addBuildFileAction);
		toolBarMgr.add(searchForBuildFilesAction);

		toolBarMgr.add(runTargetAction);
		toolBarMgr.add(removeProjectAction);
		toolBarMgr.add(removeAllAction);
		
		toolBarMgr.update(false);	
	}

	private void clearMainToolBar(IToolBarManager tbmanager) {
		tbmanager.removeAll();
		tbmanager.update(false);		
	}
	
	private IFile getSelectionBuildFile() {
		IStructuredSelection selection= (IStructuredSelection)getProjectViewer().getSelection();
		if (selection.size() == 1) {
			Object element= selection.getFirstElement(); 
			if (element instanceof ProjectNode) {
				return AntUtil.getFile(((ProjectNode)element).getBuildFileName());
				
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
}