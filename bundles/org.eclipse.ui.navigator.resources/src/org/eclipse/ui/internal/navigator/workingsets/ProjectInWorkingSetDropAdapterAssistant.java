/*******************************************************************************
 * Copyright (c) 2014-2015 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.workingsets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;

/**
 * Allow dragging projects between working sets in the Project Explorer.
 *
 * @since 3.4.800
 */
public class ProjectInWorkingSetDropAdapterAssistant extends CommonDropAdapterAssistant {

	@Override
	public IStatus validateDrop(Object target, int operation, TransferData transferType) {
		// We don't currently support COPY or LINK
		if (operation != DND.DROP_MOVE) {
			return Status.CANCEL_STATUS;
		}

		IWorkingSet targetWorkingSet = Adapters.adapt(target, IWorkingSet.class);
		if (targetWorkingSet == null) {
			return Status.CANCEL_STATUS;
		}

		if (!LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
			return Status.CANCEL_STATUS;
		}
		// Verify that we have at least one project not already in the target
		ISelection sel = LocalSelectionTransfer.getTransfer().getSelection();
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			for (Object item : ((IStructuredSelection) sel).toArray()) {
				IProject project = Adapters.adapt(item, IProject.class);
				if (project != null && !workingSetContains(targetWorkingSet, project)) {
					return Status.OK_STATUS;
				}
			}
		}
		return Status.CANCEL_STATUS;
	}

	@Override
	public IStatus handleDrop(CommonDropAdapter dropAdapter, DropTargetEvent dropTargetEvent, Object target) {
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet targetWorkingSet = Adapters.adapt(target, IWorkingSet.class);
		ISelection sel = LocalSelectionTransfer.getTransfer().getSelection();
		if (sel instanceof ITreeSelection) {
			for (TreePath path : ((ITreeSelection) sel).getPaths()) {
				IProject project = Adapters.adapt(path.getLastSegment(), IProject.class);
				if (project != null) {
					if (!workingSetContains(targetWorkingSet, project)) {
						workingSetManager.addToWorkingSets(project, new IWorkingSet[] { targetWorkingSet });
					}
					// Check if our top-level element is a working set so that
					// we can perform a move
					IWorkingSet sourceWorkingSet = Adapters.adapt(path.getFirstSegment(), IWorkingSet.class);
					if (sourceWorkingSet != null) {
						removeFromWorkingSet(project, sourceWorkingSet);
					}
				}
			}
		} else if (sel instanceof IStructuredSelection) {
			for (Object item : ((IStructuredSelection) sel).toArray()) {
				IProject project = Adapters.adapt(item, IProject.class);
				if (project != null && !workingSetContains(targetWorkingSet, project)) {
					workingSetManager.addToWorkingSets(project, new IWorkingSet[] { targetWorkingSet });
				}
			}
		}

		return Status.OK_STATUS;
	}

	/**
	 * Remove the given project from the provided working set
	 *
	 * @param project
	 *            the project to remove
	 * @param workingSet
	 *            the working set
	 */
	private void removeFromWorkingSet(IProject project, IWorkingSet workingSet) {
		IAdaptable[] srcElements = workingSet.getElements();
		List<IAdaptable> newSrcElements = new ArrayList<IAdaptable>();
		for (IAdaptable srcElement : srcElements) {
			if (!project.equals(Adapters.adapt(srcElement, IProject.class))) {
				newSrcElements.add(srcElement);
			}
		}
		IAdaptable[] adaptedNewSrcElements = workingSet
				.adaptElements(newSrcElements.toArray(new IAdaptable[newSrcElements.size()]));
		workingSet.setElements(adaptedNewSrcElements);
	}

	/**
	 * Verify if the working set contains the given project
	 *
	 * @param workingSet
	 *            the working set to check
	 * @param project
	 *            the project to check
	 * @return true if the provided project is contained in the working set,
	 *         false otherwise
	 */
	private boolean workingSetContains(IWorkingSet workingSet, IProject project) {
		for (IAdaptable element : workingSet.getElements()) {
			if (project.equals(Adapters.adapt(element, IProject.class))) {
				return true;
			}
		}
		return false;
	}

}
