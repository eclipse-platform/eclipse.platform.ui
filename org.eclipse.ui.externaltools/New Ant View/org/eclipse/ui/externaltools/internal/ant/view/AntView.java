package org.eclipse.ui.externaltools.internal.ant.view;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.ant.core.TargetInfo;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.externaltools.internal.ant.view.actions.AddProjectAction;
import org.eclipse.ui.externaltools.internal.ant.view.actions.RemoveProjectAction;
import org.eclipse.ui.externaltools.internal.ant.view.elements.ProjectErrorNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.ProjectNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.RootNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;
import org.eclipse.ui.part.ViewPart;

/**
 * A view which displays a hierarchical view of ant build files and allows the
 * user to run selected targets from those files.
 */
public class AntView extends ViewPart {
	/**
	 * The root node of the project viewer as restored during initialization
	 */
	private RootNode restoredRoot= null;
	/**
	 * The selected targets of the target viewer as restored during
	 * initialization
	 */
	private List restoredTargets= new ArrayList();
	/**
	 * XML tag used to identify an ant project in storage
	 */
	private static final String TAG_PROJECT= "project";
	/**
	 * XML key used to store an ant project's path
	 */
	private static final String TAG_PATH= "path";
	/**
	 * XML tag used to identify an ant target in storage
	 */
	private String TAG_TARGET= "target";
	/**
	 * XML key used to store an ant node's name
	 */
	private String TAG_NAME= "name";

	/**
	 * The sash form containing the project viewer and target viewer
	 */
	SashForm sashForm;

	/**
	 * The tree viewer that displays the users ant projects
	 */
	TreeViewer projectViewer;
	ProjectTreeContentProvider projectContentProvider;

	/**
	 * The list viewer that displays the users selected targets
	 */
	ListViewer targetViewer;
	TargetListContentProvider targetContentProvider;

	/**
	 * The label provider used by viewers in this view
	 */
	AntViewLabelProvider labelProvider = new AntViewLabelProvider();
	
	// Actions
	private AddProjectAction addProjectAction;
	private RemoveProjectAction removeProjectAction;

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		initializeActions();
		createToolbarActions();
		sashForm = new SashForm(parent, SWT.NONE);
		createProjectViewer();
		createTargetViewer();
	}

	private void createToolbarActions() {
		IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();
		toolBarMgr.add(addProjectAction);
		toolBarMgr.add(removeProjectAction);
	}

	/**
	 * Initialize the collection of actions for this view
	 */
	private void initializeActions() {
		addProjectAction = new AddProjectAction(this);
		removeProjectAction = new RemoveProjectAction(this);
	}
	/**
	 * Create the viewer which displays the active targets
	 */
	private void createTargetViewer() {
		targetViewer = new ListViewer(sashForm, SWT.H_SCROLL | SWT.V_SCROLL);
		targetContentProvider = new TargetListContentProvider();
		targetViewer.setContentProvider(targetContentProvider);
		Iterator targets= restoredTargets.iterator();
		while (targets.hasNext()) {
			targetContentProvider.addTarget((TargetNode) targets.next());
		}
		targetViewer.setLabelProvider(labelProvider);
		// The content provider doesn't use the input, but it input has to be set to something.
		targetViewer.setInput(ResourcesPlugin.getWorkspace());
	}

	/**
	 * Create the viewer which displays the ant projects
	 */
	private void createProjectViewer() {
		projectViewer = new TreeViewer(sashForm, SWT.H_SCROLL | SWT.V_SCROLL);
		projectContentProvider = new ProjectTreeContentProvider();
		projectViewer.setContentProvider(projectContentProvider);
		projectViewer.setLabelProvider(labelProvider);
		projectViewer.setInput(restoredRoot);
		projectViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				removeProjectAction.update();
			}
		});
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
	 * Adds a project to the view based on the given build file
	 * 
	 * @param buildFile the build file to add
	 */
	public void addBuildFile(String buildFile) {
		projectContentProvider.addProject(parseBuildFile(buildFile));
		projectViewer.refresh();
	}

	/**
	 * Removes the given project from the view
	 * 
	 * @param project the project to remove
	 */
	public void removeProject(ProjectNode project) {
		projectContentProvider.getRootNode().removeProject(project);
		projectViewer.refresh();
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
		IMemento[] targets= memento.getChildren(TAG_TARGET);
		for (int i = 0; i < targets.length; i++) {
			IMemento target= targets[i];
			TargetNode targetNode= null;
			String buildFileName= target.getString(TAG_PATH);
			String targetName= target.getString(TAG_NAME);
			ProjectNode[] projects= restoredRoot.getProjects();
			for (int j = 0; j < projects.length; j++) {
				ProjectNode project = projects[j];
				if (project.getBuildFileName().equals(buildFileName)) {
					TargetNode[] projectTargets= project.getTargets();
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
			restoredRoot= new RootNode();
			return;
		}
		IMemento[] projects = memento.getChildren(TAG_PROJECT);
		if (projects.length < 1) {
			restoredRoot= new RootNode();
			return;
		}
		List projectNodes = new ArrayList(projects.length);
		for (int i = 0; i < projects.length; i++) {
			IMemento project = projects[i];
			String pathString = project.getString(TAG_PATH);
			ProjectNode node = parseBuildFile(pathString);
			if (!(node instanceof ProjectErrorNode)) {
				projectNodes.add(node);
			}
		}
		restoredRoot= new RootNode((ProjectNode[]) projectNodes.toArray(new ProjectNode[projectNodes.size()]));
	}

	/**
	 * Save the contents of the project viewer and the target viewer
	 * 
	 * @see org.eclipse.ui.IViewPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
		ProjectNode[] projects = projectContentProvider.getRootNode().getProjects();
		IMemento projectMemento;
		for (int i = 0; i < projects.length; i++) {
			projectMemento = memento.createChild(TAG_PROJECT);
			projectMemento.putString(TAG_PATH, projects[i].getBuildFileName());
		}
		Iterator targets= targetContentProvider.getTargets().iterator();
		IMemento targetMemento;
		TargetNode target;
		while (targets.hasNext()) {
			target= ((TargetNode)targets.next());
			targetMemento= memento.createChild(TAG_TARGET);
			targetMemento.putString(TAG_PATH, ((ProjectNode)target.getParent()).getBuildFileName());
			targetMemento.putString(TAG_NAME, target.getName());
		}
	}
	
	/**
	 * Parses the given build file and returns a project node representing the
	 * build file. If an error occurs while parsing the file, an error node will
	 * be returned
	 * 
	 * @param filename the name (full path) of the build file to parse
	 * @return ProjectNode the  project node that represents the given build
	 * file or a <code>ProjectErrorNode</code> if an error occurs while parsing
	 * the given file.
	 */
	public static ProjectNode parseBuildFile(String filename) {
		AntRunner runner = new AntRunner();
		runner.setBuildFileLocation(filename);
		TargetInfo[] infos = null;
		try {
			infos = runner.getAvailableTargets();
		} catch (CoreException e) {
			return new ProjectErrorNode(filename, "An exception occurred retrieving targets: " + e.getMessage());
		}
		if (infos.length < 1) {
			return new ProjectErrorNode(filename, "No targets found");
		}
		Project project = new Project();
		if (infos[0].getProject() != null) {
			project.setName(infos[0].getProject());
		}
		for (int i = 0; i < infos.length; i++) {
			TargetInfo info = infos[i];
			if (info.isDefault()) {
				project.setDefault(info.getName());
			}
			Target target = new Target();
			target.setName(info.getName());
			String[] dependencies = info.getDependencies();
			StringBuffer depends = new StringBuffer();
			int numDependencies = dependencies.length;
			if (numDependencies > 0) {
				// Onroll the loop to avoid trailing comma
				depends.append(dependencies[0]);
			}
			for (int j = 1; j < numDependencies; j++) {
				depends.append(',').append(dependencies[j]);
			}
			target.setDepends(depends.toString());
			target.setDescription(info.getDescription());
			project.addTarget(target);
		}
		if (project.getDefaultTarget() == null) {
			return new ProjectErrorNode(filename, "No project element found");
		}

		String projectName = project.getName();
		if (projectName == null) {
			projectName = "(unnamed)";
		}
		ProjectNode projectNode = new ProjectNode(projectName, filename);
		Enumeration projTargets = project.getTargets().elements();
		while (projTargets.hasMoreElements()) {
			Target target = (Target) projTargets.nextElement();
			// Target Node -----------------
			Enumeration targetDependencies= target.getDependencies();
			TargetNode targetNode = new TargetNode(target.getName(), target.getDescription());
			while (targetDependencies.hasMoreElements()) {
				targetNode.addDependency((String) targetDependencies.nextElement());
			}
			projectNode.addTarget(targetNode);
			// Execution Path -------
			Vector topoSort = project.topoSort(target.getName(), project.getTargets());
			int n = topoSort.indexOf(target) + 1;
			while (topoSort.size() > n)
				topoSort.remove(topoSort.size() - 1);
			topoSort.trimToSize();
			ListIterator topoElements = topoSort.listIterator();
			while (topoElements.hasNext()) {
				int i = topoElements.nextIndex();
				Target topoTask = (Target) topoElements.next();
				targetNode.addToExecutionPath((i + 1) + ":" + topoTask.getName());
			}
		}
		return projectNode;
	}

}
