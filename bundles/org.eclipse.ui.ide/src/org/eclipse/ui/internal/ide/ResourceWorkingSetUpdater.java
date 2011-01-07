/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetUpdater;

/**
 * A working set updater that updates resource working sets on resource deltas
 * 
 * @since 3.2
 */
public class ResourceWorkingSetUpdater implements IWorkingSetUpdater,
		IResourceChangeListener {

	/**
	 * Utility class used to help process incoming resource deltas.
	 */
	private static class WorkingSetDelta {
		private IWorkingSet fWorkingSet;

		private List fElements;

		private boolean fChanged;

		/**
		 * Create a new instance of this class.
		 * 
		 * @param workingSet
		 *            the working set to track.
		 */
		public WorkingSetDelta(IWorkingSet workingSet) {
			fWorkingSet = workingSet;
			fElements = new ArrayList(Arrays.asList(workingSet.getElements()));
		}

		/**
		 * Returns the index of this element in the list of known elements.
		 * 
		 * @param element
		 *            the element to search for
		 * @return the index, or -1 if unknown.
		 */
		public int indexOf(Object element) {
			return fElements.indexOf(element);
		}

		/**
		 * Add a new element to the list of known elements.
		 * 
		 * @param index
		 *            the index at which to place the element
		 * @param element
		 *            the element to set
		 */
		public void set(int index, Object element) {
			fElements.set(index, element);
			fChanged = true;
		}

		/**
		 * Remove an element from the list of known elements.
		 * 
		 * @param index
		 *            the index of the element to remove
		 */
		public void remove(int index) {
			if (fElements.remove(index) != null) {
				fChanged = true;
			}
		}

		/**
		 * Process the changes to this delta and update the working set if
		 * necessary.
		 */
		public void process() {
			if (fChanged) {
				fWorkingSet.setElements((IAdaptable[]) fElements
						.toArray(new IAdaptable[fElements.size()]));
			}
		}
	}

	private List fWorkingSets;

	/**
	 * Create a new instance of this updater.
	 */
	public ResourceWorkingSetUpdater() {
		fWorkingSets = new ArrayList();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this,
				IResourceChangeEvent.POST_CHANGE);
	}

	public void add(IWorkingSet workingSet) {
		checkElementExistence(workingSet);
		synchronized (fWorkingSets) {
			fWorkingSets.add(workingSet);
		}
	}

	public boolean remove(IWorkingSet workingSet) {
		boolean result;
		synchronized (fWorkingSets) {
			result = fWorkingSets.remove(workingSet);
		}

		return result;
	}

	public boolean contains(IWorkingSet workingSet) {
		synchronized (fWorkingSets) {
			return fWorkingSets.contains(workingSet);
		}
	}

	public void dispose() {
		synchronized (fWorkingSets) {
			fWorkingSets.clear();
		}
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta == null) {
			return;
		}
		IWorkingSet[] workingSets;
		synchronized (fWorkingSets) {
			workingSets = (IWorkingSet[]) fWorkingSets
					.toArray(new IWorkingSet[fWorkingSets.size()]);
		}
		for (int w = 0; w < workingSets.length; w++) {
			WorkingSetDelta workingSetDelta = new WorkingSetDelta(
					workingSets[w]);
			processResourceDelta(workingSetDelta, delta);
			workingSetDelta.process();
		}
	}

	private void processResourceDelta(WorkingSetDelta result,
			IResourceDelta delta) {
		IResource resource = delta.getResource();
		int type = resource.getType();
		int index = result.indexOf(resource);
		int kind = delta.getKind();
		int flags = delta.getFlags();
		if (kind == IResourceDelta.CHANGED && type == IResource.PROJECT
				&& index != -1) {
			if ((flags & IResourceDelta.OPEN) != 0) {
				result.set(index, resource);
			}
		}
		if (index != -1 && kind == IResourceDelta.REMOVED) {
			if ((flags & IResourceDelta.MOVED_TO) != 0) {
				result.set(index, ResourcesPlugin.getWorkspace().getRoot()
						.findMember(delta.getMovedToPath()));
			} else {
				result.remove(index);
			}
		}

		// Don't dive into closed or opened projects
		if (projectGotClosedOrOpened(resource, kind, flags)) {
			return;
		}

		IResourceDelta[] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; i++) {
			processResourceDelta(result, children[i]);
		}
	}

	private boolean projectGotClosedOrOpened(IResource resource, int kind,
			int flags) {
		return resource.getType() == IResource.PROJECT
				&& kind == IResourceDelta.CHANGED
				&& (flags & IResourceDelta.OPEN) != 0;
	}

	private void checkElementExistence(IWorkingSet workingSet) {
		List elements = new ArrayList(Arrays.asList(workingSet.getElements()));
		boolean changed = false;
		for (Iterator iter = elements.iterator(); iter.hasNext();) {
			IAdaptable element = (IAdaptable) iter.next();
			boolean remove = false;
			if (element instanceof IProject) {
				IProject project = (IProject) element;
				remove = !project.exists();
			} else if (element instanceof IResource) {
				IResource resource = (IResource) element;
				IProject project = resource.getProject();
				remove = (project != null ? project.isOpen() : true)
						&& !resource.exists();
			}
			if (remove) {
				iter.remove();
				changed = true;
			}
		}
		if (changed) {
			workingSet.setElements((IAdaptable[]) elements
					.toArray(new IAdaptable[elements.size()]));
		}
	}
}
