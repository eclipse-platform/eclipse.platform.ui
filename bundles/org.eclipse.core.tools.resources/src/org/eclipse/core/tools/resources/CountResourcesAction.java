/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.resources;

import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.tools.ErrorUtil;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.*;

/**
 * This action pops-up a <code>MessageDialog</code> showing the number of
 * resources (and their child resources) currently selected. 
 */
public class CountResourcesAction implements IWorkbenchWindowActionDelegate {

	/** A reference to the window where this action will work in.*/
	private IWorkbenchWindow window;

	/**
	 * Executes this action, popping up a <code>MessageDialog</code> showing the
	 * number of resources (and their child resources) currently selected.
	 * 
	 * @param action the action proxy that handles the presentation portion of the
	 *   action
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run(IAction action) {
		List resources = getSelectedResources();
		try {
			int count = countResources(resources);
			showResourcesCount(resources, count);
		} catch (CoreException ce) {
			ErrorUtil.logException(ce, "Error counting resources."); //$NON-NLS-1$
			ErrorUtil.showErrorMessage(ce.toString(), "Error counting resources"); //$NON-NLS-1$
		}
	}

	/**
	 * Returns a list of resources currently selected. If no resource is currently
	 * selected, returns a list containing the workspace root.
	 * 
	 * @return a list of resources
	 */
	private List getSelectedResources() {

		List resources = new LinkedList();

		ISelectionService selectionService = window.getSelectionService();
		ISelection selection = selectionService.getSelection();

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Iterator elements = structuredSelection.iterator(); elements.hasNext();) {
				IResource resource = convertToResource(elements.next());
				if (resource != null)
					resources.add(resource);
			}
			eliminateRedundancies(resources);
		}

		// if no resources were selected, add the workspace root to the list
		if (resources.size() == 0)
			resources.add(ResourcesPlugin.getWorkspace().getRoot());

		return resources;
	}

	/**
	 * Pops-up a <code>MessageDialog</code> showing the number of resources (and
	 * their child resources) currently selected.
	 * 
	 * @param resources a list containing all resources used as a starting point in
	 * the search
	 * @param count the number of resources found
	 */

	private void showResourcesCount(List resources, int count) {
		StringBuffer message = new StringBuffer();
		message.append("Number of resources visited: "); //$NON-NLS-1$
		message.append(count);
		message.append("\nStarting point(s): \n"); //$NON-NLS-1$
		for (Iterator resourcesIter = resources.iterator(); resourcesIter.hasNext();) {
			message.append('\t');
			message.append(((IResource) resourcesIter.next()).getFullPath());
			message.append('\n');
		}
		MessageDialog.openInformation(window.getShell(), "Resource counting", message.toString()); //$NON-NLS-1$
	}

	/**
	 * Helper method that converts an object to the <code>IResource</code>
	 * interface. The conversion is a bare cast operation (if the object is instance
	 * of <code>IResource</code>, or an adaptation (if the object is instance of
	 * <code>IAdaptable</code>).
	 * 
	 * @param object the object to be cast to <code>IResource</code>
	 * @return a reference to an IResource corresponding to the object provided, or
	 * null if it is not possible to convert the provided object to
	 * <code>IResource</code>.
	 */
	private IResource convertToResource(Object object) {

		if (object instanceof IResource)
			return (IResource) object;

		if (object instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) object;
			return (IResource) adaptable.getAdapter(IResource.class);
		}

		return null;
	}

	/**
	 * Counts the number of resources (and its sub-resources) in the list of
	 * resources provided.
	 * 
	 * @param resources a <code>List</code> object containing resource objects 
	 * @return the number of resources found
	 * @throws CoreException if a visited resource does not exist
	 * @see IResource#accept(org.eclipse.core.resources.IResourceVisitor)
	 */
	private int countResources(List resources) throws CoreException {

		ResourceCounterVisitor counter = new ResourceCounterVisitor();

		for (Iterator resourcesIter = resources.iterator(); resourcesIter.hasNext();)
			((IResource) resourcesIter.next()).accept(counter);

		return counter.count;
	}

	/**
	 * A helper class that implements <code>IResourceVisitor</code>. Visits each
	 * resource in a resource hierarchy, counting them.
	 */
	class ResourceCounterVisitor implements IResourceVisitor {
		protected int count;

		/**
		 * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
		 */
		public boolean visit(IResource resource) {
			count++;
			return true;
		}
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		// do nothing
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing
	}

	/**
	 * Initializes this action delegate with the workbench window it will work in.
	 *
	 * @param window the window that provides the context for this delegate
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * Eliminates from the resource list provided any redundant resources.
	 * Redundant resources are resources that have any other resources in the list
	 * as ancestors.
	 * 
	 * @param resourcesList a <code>List</code> object containing resource objects.
	 */
	private void eliminateRedundancies(List resourcesList) {
		if (resourcesList.size() <= 1)
			return;

		//	we sort the resources list by path so it is easier to check for redundancies     
		Collections.sort(resourcesList, new Comparator() {
			public int compare(Object arg1, Object arg2) {
				IResource resource1 = (IResource) arg1;
				IResource resource2 = (IResource) arg2;
				return resource1.getFullPath().toString().compareTo(resource2.getFullPath().toString());
			}
		});

		// We iterate through the list removing any resource which is descendant 
		// from any resource previously visited 
		Iterator resourcesIter = resourcesList.iterator();
		IResource last = (IResource) resourcesIter.next();
		while (resourcesIter.hasNext()) {
			IResource current = (IResource) resourcesIter.next();
			if (last.getFullPath().isPrefixOf(current.getFullPath()))
				resourcesIter.remove();
			else
				last = current;
		}
	}
}