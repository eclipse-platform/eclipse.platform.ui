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
package org.eclipse.ui.actions;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Standard actions for full and incremental builds of the selected project(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class BuildAction extends WorkspaceAction {
	
	/**
	 * The id of an incremental build action.
	 */
	public static final String ID_BUILD = PlatformUI.PLUGIN_ID + ".BuildAction";//$NON-NLS-1$
	
	/**
	 * The id of a rebuild all action.
	 */
	public static final String ID_REBUILD_ALL = PlatformUI.PLUGIN_ID + ".RebuildAllAction";//$NON-NLS-1$

	private int	buildType;
	
	/**
	 * The list of IProjects to build (computed lazily).
	 */
	private List	projectsToBuild = null;
	
/**
 * Creates a new action of the appropriate type. The action id is 
 * <code>ID_BUILD</code> for incremental builds and <code>ID_REBUILD_ALL</code>
 * for full builds.
 *
 * @param shell the shell for any dialogs
 * @param type the type of build; one of
 *  <code>IncrementalProjectBuilder.INCREMENTAL_BUILD</code> or 
 *  <code>IncrementalProjectBuilder.FULL_BUILD</code>
 */
public BuildAction(Shell shell, int type) {
	super(shell, "");//$NON-NLS-1$

	if (type == IncrementalProjectBuilder.INCREMENTAL_BUILD) {
		setText(WorkbenchMessages.getString("BuildAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("BuildAction.toolTip")); //$NON-NLS-1$
		setId(ID_BUILD);
		WorkbenchHelp.setHelp(this, IHelpContextIds.INCREMENTAL_BUILD_ACTION);
	}
	else {
		setText(WorkbenchMessages.getString("RebuildAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("RebuildAction.tooltip")); //$NON-NLS-1$
		setId(ID_REBUILD_ALL);
		WorkbenchHelp.setHelp(this, IHelpContextIds.FULL_BUILD_ACTION);
	}
		
	this.buildType = type;
}

/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
protected List getActionResources() {
	return getProjectsToBuild();
}

/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getOperationMessage() {
	return WorkbenchMessages.getString("BuildAction.operationMessage"); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsMessage() {
	return WorkbenchMessages.getString("BuildAction.problemMessage"); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsTitle() {
	return WorkbenchMessages.getString("BuildAction.problemTitle"); //$NON-NLS-1$
}

/**
 * Returns the projects to build.
 * This contains the set of projects which have builders, across all selected resources.
 */
List getProjectsToBuild() {
	if (projectsToBuild == null) {
		projectsToBuild = new ArrayList(3);
		for (Iterator i = getSelectedResources().iterator(); i.hasNext();) {
			IResource resource = (IResource) i.next();
			IProject project = resource.getProject();
			if (project != null) {
				if (!projectsToBuild.contains(project)) {
					if (hasBuilder(project)) {
						projectsToBuild.add(project);
					}
				}
			}
		}
	}
	return projectsToBuild;
}

/**
 * Returns whether there are builders configured on the given project.
 *
 * @return <code>true</code> if it has builders,
 *   <code>false</code> if not, or if this couldn't be determined
 */
boolean hasBuilder(IProject project) {
	try {
		ICommand[] commands = project.getDescription().getBuildSpec();
		if (commands.length > 0) {
			return true;
		}
	}
	catch (CoreException e) {
		// this method is called when selection changes, so
		// just fall through if it fails.
		// this shouldn't happen anyway, since the list of selected resources
		// has already been checked for accessibility before this is called
	}
	return false;
}

/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
void invokeOperation(IResource resource, IProgressMonitor monitor) throws CoreException {
	((IProject)resource).build(buildType,monitor);
}
/**
 * Returns whether the user's preference is set to automatically save modified
 * resources before a manual build is done.
 *
 * @return <code>true</code> if Save All Before Build is enabled
 */
public static boolean isSaveAllSet() {
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	return store.getBoolean(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD);
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 *
 * Change the order of the resources so that
 * it matches the build order. Closed and
 * non existant projects are eliminated. Also,
 * any projects in cycles are eliminated.
 */
List pruneResources(List resourceCollection) {
	// Optimize...
	if (resourceCollection.size() < 2)
		return resourceCollection;

	// Try the workspace's description build order if specified
	String[] orderedNames = ResourcesPlugin.getWorkspace().getDescription().getBuildOrder();
	if (orderedNames != null) {
		List orderedProjects = new ArrayList(resourceCollection.size());
		//Projects may not be in the build order but should be built if selected
		List unorderedProjects = new ArrayList(resourceCollection.size());
		unorderedProjects.addAll(resourceCollection);
	
		for (int i = 0; i < orderedNames.length; i++) {
			String projectName = orderedNames[i];
			for (int j = 0; j < resourceCollection.size(); j++) {
				IProject project = (IProject) resourceCollection.get(j);
				if (project.getName().equals(projectName)) {
					orderedProjects.add(project);
					unorderedProjects.remove(project);
					break;
				}
			}
		}
		//Add anything not specified before we return
		orderedProjects.addAll(unorderedProjects);
		return orderedProjects;
	}

	// Try the project prerequisite order then
	IProject[] projects = new IProject[resourceCollection.size()];
	projects = (IProject[]) resourceCollection.toArray(projects);
	IWorkspace.ProjectOrder po = ResourcesPlugin.getWorkspace().computeProjectOrder(projects);
	ArrayList orderedProjects = new ArrayList();
	orderedProjects.addAll(Arrays.asList(po.projects));
	return orderedProjects;
}
/* (non-Javadoc)
 * Method declared on IAction; overrides method on WorkspaceAction.
 * This override allows the user to save the contents of selected
 * open editors so that the updated contents will be used for building.
 */
public void run() {

	// Save all resources prior to doing build
	saveAllResources();

	super.run();
}
/**
 * Causes all editors to save any modified resources depending on the user's
 * preference.
 */
void saveAllResources() {
	List projects = getSelectedResources();
	if (projects == null || projects.isEmpty())
		return;
		
	if (!isSaveAllSet())
		return;
		
	IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
	for (int i = 0; i < windows.length; i++) {
		IWorkbenchPage [] pages = windows[i].getPages();
		for (int j = 0; j < pages.length; j++) {
			IWorkbenchPage page = pages[j];
			IEditorPart[] editors = page.getDirtyEditors();
			for (int k = 0; k < editors.length; k++) {
				IEditorPart editor = editors[k];
				IEditorInput input = editor.getEditorInput();
				if (input instanceof IFileEditorInput) {
					IFile inputFile = ((IFileEditorInput)input).getFile();
					if (projects.contains(inputFile.getProject())) {
						page.saveEditor(editor, false);
					}
				}
			}
		}
	}
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
boolean shouldPerformResourcePruning() {
	return true;
}
/**
 * The <code>BuildAction</code> implementation of this
 * <code>SelectionListenerAction</code> method ensures that this action is
 * enabled only if all of the selected resources have buildable projects.
 */
protected boolean updateSelection(IStructuredSelection s) {
	projectsToBuild = null;
	return super.updateSelection(s) && getProjectsToBuild().size() > 0;
}
}
