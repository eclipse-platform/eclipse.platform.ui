/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dina Sayed, dsayed@eg.ibm.com, IBM -  bug 269844
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.misc.DisjointSet;

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
 * @since 3.3
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
	 * Builds the connected component set for the input projects.
	 * The result is a DisjointSet where all related projects belong
	 * to the same set.
	 */
	private static DisjointSet buildConnectedComponents(IProject[] projects) {
		//initially each vertex is in a set by itself
		DisjointSet set = new DisjointSet();
		for (int i = 0; i < projects.length; i++) {
			set.makeSet(projects[i]);
		}
		for (int i = 0; i < projects.length; i++) {
			try {
				IProject[] references = projects[i].getReferencedProjects();
				//each reference represents an edge in the project reference
				//digraph from projects[i] -> references[j]
				for (int j = 0; j < references.length; j++) {
					Object setOne = set.findSet(projects[i]);
					//note that referenced projects may not exist in the workspace
					Object setTwo = set.findSet(references[j]);
					//these two projects are related, so join their sets
					if (setOne != null && setTwo != null && setOne != setTwo)
						set.union(setOne, setTwo);
				}
			} catch (CoreException e) {
				//assume inaccessible projects have no references
			}
		}
		return set;
	}

	/**
	 * Creates this action.
	 * 
	 * @param shell
	 *            The shell to use for parenting any dialogs created by this
	 *            action.
	 *            
	 * @deprecated {@link #CloseUnrelatedProjectsAction(IShellProvider)}
	 */
	public CloseUnrelatedProjectsAction(Shell shell) {
		super(shell, IDEWorkbenchMessages.CloseUnrelatedProjectsAction_text);
		initAction();
	}
	
	/**
	 * Creates this action.
	 * 
	 * @param provider
	 *            The shell to use for parenting any dialogs created by this
	 *            action.
	 * @since 3.4
	 */
	public CloseUnrelatedProjectsAction(IShellProvider provider){
		super(provider, IDEWorkbenchMessages.CloseUnrelatedProjectsAction_text);
		initAction();
	}
	
	/*
	 * (non-Javadoc)overrides method on CloseResourceAction.
	 */
	public void run() {
		if(promptForConfirmation())
				super.run();
	}
   
   /**
	 * Returns whether to close unrelated projects.
	 * Consults the preference and prompts the user if necessary.
	 * 
	 * @return <code>true</code> if unrelated projects should be closed, and
	 *         <code>false</code> otherwise.
	 */
	private boolean promptForConfirmation() {
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		if (store.getBoolean(IDEInternalPreferences.CLOSE_UNRELATED_PROJECTS))
			return true;

		// get first project name
		List selection = super.getSelectedResources();
		int selectionSize = selection.size();
		if (selectionSize == 0)
			return true;

		String message = null;
		if (selectionSize == 1) { // if one project is selected then print its name
			Object firstSelected = selection.get(0);
			String projectName = null;
			if (firstSelected instanceof IProject)
				projectName = ((IProject) firstSelected).getName();
			message = NLS.bind(IDEWorkbenchMessages.CloseUnrelatedProjectsAction_confirmMsg1, projectName);
		} else // if more then one project is selected then print there number
			message = NLS.bind(IDEWorkbenchMessages.CloseUnrelatedProjectsAction_confirmMsgN,
					new Integer(selectionSize));

		MessageDialogWithToggle dialog = MessageDialogWithToggle.openOkCancelConfirm(
						getShell(), IDEWorkbenchMessages.CloseUnrelatedProjectsAction_toolTip,
						message, IDEWorkbenchMessages.CloseUnrelatedProjectsAction_AlwaysClose,
						false, null, null);
		if (dialog.getReturnCode() != IDialogConstants.OK_ID)
			return false;
		store.setValue(IDEInternalPreferences.CLOSE_UNRELATED_PROJECTS, dialog.getToggleState());
		return true;
	}

	/**
     * Initializes for the constructor.
     */
	private void initAction(){
		setId(ID);
		setToolTipText(IDEWorkbenchMessages.CloseUnrelatedProjectsAction_toolTip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.CLOSE_UNRELATED_PROJECTS_ACTION);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.SelectionListenerAction#clearCache()
	 */
	protected void clearCache() {
		super.clearCache();
		oldSelection = Collections.EMPTY_LIST;
		selectionDirty = true;
	}

	/**
	 * Computes the related projects of the selection.
	 */
	private void computeRelated(List selection) {
		//build the connected component set for all projects in the workspace
		DisjointSet set = buildConnectedComponents(ResourcesPlugin.getWorkspace().getRoot().getProjects());
		//remove the connected components that the selected projects are in
		for (Iterator it = selection.iterator(); it.hasNext();)
			set.removeSet(it.next());
		//the remainder of the projects in the disjoint set are unrelated to the selection
		projectsToClose.clear();
		set.toList(projectsToClose);
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

	/**
	 * Handles a resource changed event by updating the enablement
	 * when projects change.
	 * <p>
	 * This method overrides the super-type implementation to update
	 * the selection when the open state or description of any project changes.
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		// don't bother looking at delta if selection not applicable
		if (selectionIsOfType(IResource.PROJECT)) {
			IResourceDelta delta = event.getDelta();
			if (delta != null) {
				IResourceDelta[] projDeltas = delta.getAffectedChildren(IResourceDelta.CHANGED);
				for (int i = 0; i < projDeltas.length; ++i) {
					IResourceDelta projDelta = projDeltas[i];
					//changing either the description or the open state can affect enablement
					if ((projDelta.getFlags() & (IResourceDelta.OPEN | IResourceDelta.DESCRIPTION)) != 0) {
						selectionChanged(getStructuredSelection());
						return;
					}
				}
			}
		}
	}
}
