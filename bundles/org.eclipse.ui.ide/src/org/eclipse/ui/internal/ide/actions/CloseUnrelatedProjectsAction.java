/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * This action closes all projects that are unrelated to the selected projects. A
 * project is unrelated if it is not directly or transitively referenced by one 
 * of the selected projects, and does not directly or transitively reference
 * one of the selected projects.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @see IDEActionFactory#CLOSE_UNRELATED_PROJECTS
 * @since 3.2
 */
public class CloseUnrelatedProjectsAction extends CloseResourceAction {
	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".CloseUnrelatedProjectsAction"; //$NON-NLS-1$

	private final List projectsToClose = new ArrayList();

	private boolean selectionDirty = true;

	private List oldSelection = Collections.EMPTY_LIST;

	/**
	 * Creates this action.
	 * 
	 * @param shell
	 *            The shell to use for parenting any dialogs created by this
	 *            action.
	 */
	public CloseUnrelatedProjectsAction(Shell shell) {
		super(shell, IDEWorkbenchMessages.CloseUnrelatedProjectsAction_text);
		setId(ID);
		setToolTipText(IDEWorkbenchMessages.CloseUnrelatedProjectsAction_toolTip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.CLOSE_UNRELATED_PROJECTS_ACTION);
	}

	/**
	 * Adds the given project, and all related projects, to the given set.
	 * 
	 * @param relatedProjects
	 * @param project
	 */
	private void addRelatedProjects(HashSet relatedProjects, IProject project) {
		if (project == null || relatedProjects.contains(project))
			return;
		relatedProjects.add(project);
		try {
			IProject[] related = project.getReferencedProjects();
			for (int i = 0; i < related.length; i++)
				addRelatedProjects(relatedProjects, related[i]);
			related = project.getReferencingProjects();
			for (int i = 0; i < related.length; i++)
				addRelatedProjects(relatedProjects, related[i]);
		} catch (CoreException e) {
			// ignore project for which we can't compute related projects
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.SelectionListenerAction#clearCache()
	 */
	protected void clearCache() {
		super.clearCache();
		selectionDirty = true;
	}

	/**
	 * Computes the related projects of the selection.
	 */
	private void computeRelated(List selection) {
		HashSet relatedProjects = new HashSet();
		for (Iterator it = selection.iterator(); it.hasNext();) {
			Object next = it.next();
			if (next instanceof IProject)
				addRelatedProjects(relatedProjects, (IProject) next);
		}
		HashSet unrelated = new HashSet();
		unrelated.addAll(Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects()));
		unrelated.removeAll(relatedProjects);
		projectsToClose.clear();
		projectsToClose.addAll(unrelated);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.SelectionListenerAction#getSelectedResources()
	 */
	protected List getSelectedResources() {
		if (selectionDirty) {
			List newSelection = super.getSelectedResources();
			if (!oldSelection.equals(newSelection)) {
				oldSelection = newSelection;
				computeRelated(newSelection);
			}
			selectionDirty = false;
		}
		return projectsToClose;
	}
}
