/*******************************************************************************
 * Copyright (c) 2003, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Mickael Istria (Red Hat Inc.) Bug 264404 - Problem decorators
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.workbench;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.navigator.resources.nested.PathComparator;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * @since 3.2
 */
public class ResourceExtensionContentProvider extends WorkbenchContentProvider {

	private static final Object[] NO_CHILDREN = new Object[0];
	private Viewer viewer;

	public ResourceExtensionContentProvider() {
		super();
	}

	@Override
	public Object[] getElements(Object element) {
		return super.getChildren(element);
	}

	@Override
	public Object[] getChildren(Object element) {
		if(element instanceof IResource)
			return super.getChildren(element);
		return NO_CHILDREN;
	}

	@Override
	public boolean hasChildren(Object element) {
		try {
			if (element instanceof IContainer) {
				IContainer c = (IContainer) element;
				if (!c.isAccessible())
					return false;
				return c.members().length > 0;
			}
		} catch (CoreException ex) {
			WorkbenchNavigatorPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.PLUGIN_ID, 0, ex.getMessage(), ex));
			return false;
		}

		return super.hasChildren(element);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		super.inputChanged(viewer, oldInput, newInput);
		this.viewer = viewer;
	}


	/**
	 * Process the resource delta.
	 */
	@Override
	protected void processDelta(IResourceDelta delta) {

		Control ctrl = viewer.getControl();
		if (ctrl == null || ctrl.isDisposed()) {
			return;
		}

		final Collection<Runnable> runnables = new ArrayList<>();
		final SortedSet<IResource> resourcesToRefresh = new TreeSet<>(new Comparator<IResource>() {
			private PathComparator pathComparator = new PathComparator();
			@Override
			public int compare(IResource arg0, IResource arg1) {
				return pathComparator.compare(arg0.getFullPath(), arg1.getFullPath());
			}
		});
		processDelta(delta, runnables, resourcesToRefresh);

		IResource currentTopLevelResource = null;
		for (IResource resource : resourcesToRefresh) {
			if (resource == null) {
				// paranoia, see bug 509821
				continue;
			}
			if (currentTopLevelResource == null
					|| !currentTopLevelResource.getFullPath().isPrefixOf(resource.getFullPath())) {
				currentTopLevelResource = resource;
				runnables.add(getRefreshRunnable(resource));
			}
		}

		if (runnables.isEmpty()) {
			return;
		}

		//Are we in the UIThread? If so spin it until we are done
		if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
			runUpdates(runnables);
		} else {
			ctrl.getDisplay().asyncExec(() -> {
				// Abort if this happens after disposes
				Control ctrl1 = viewer.getControl();
				if (ctrl1 == null || ctrl1.isDisposed()) {
					return;
				}

				runUpdates(runnables);
			});
		}

	}

	/**
	 * Process a resource delta. Add runnables for addAndRemove and
	 * resourceToUpdate.
	 */
	private void processDelta(IResourceDelta delta, Collection<Runnable> addAndRemoveRunnables,
			Set<IResource> toRefresh) {
		//he widget may have been destroyed
		// by the time this is run. Check for this and do nothing if so.
		Control ctrl = viewer.getControl();
		if (ctrl == null || ctrl.isDisposed()) {
			return;
		}

		// Get the affected resource
		final IResource resource = delta.getResource();

		// If any children have changed type, just do a full refresh of this
		// parent,
		// since a simple update on such children won't work,
		// and trying to map the change to a remove and add is too dicey.
		// The case is: folder A renamed to existing file B, answering yes to
		// overwrite B.
		IResourceDelta[] affectedChildren = delta
				.getAffectedChildren(IResourceDelta.CHANGED);
		for (IResourceDelta affectedChild : affectedChildren) {
			if ((affectedChild.getFlags() & IResourceDelta.TYPE) != 0) {
				toRefresh.add(resource);
				return;
			}
		}

		// Check the flags for changes the Navigator cares about.
		// See ResourceLabelProvider for the aspects it cares about.
		// Notice we don't care about F_CONTENT or F_MARKERS currently.
		int changeFlags = delta.getFlags();
		if ((changeFlags & (IResourceDelta.OPEN | IResourceDelta.SYNC
				| IResourceDelta.TYPE | IResourceDelta.DESCRIPTION)) != 0) {
			/* support the Closed Projects filter;
			 * when a project is closed, it may need to be removed from the view.
			 */
			IContainer parent = resource.getParent();
			if (parent != null) {
				toRefresh.add(parent);
			}
		}
		// Replacing a resource may affect its label and its children
		if ((changeFlags & IResourceDelta.REPLACED) != 0) {
			toRefresh.add(resource);
			return;
		}


		// Handle changed children .
		for (IResourceDelta affectedChild : affectedChildren) {
			processDelta(affectedChild, addAndRemoveRunnables, toRefresh);
		}

		// @issue several problems here:
		//  - should process removals before additions, to avoid multiple equal
		// elements in viewer
		//   - Kim: processing removals before additions was the indirect cause of
		// 44081 and its varients
		//   - Nick: no delta should have an add and a remove on the same element,
		// so processing adds first is probably OK
		//  - using setRedraw will cause extra flashiness
		//  - setRedraw is used even for simple changes
		//  - to avoid seeing a rename in two stages, should turn redraw on/off
		// around combined removal and addition
		//   - Kim: done, and only in the case of a rename (both remove and add
		// changes in one delta).

		IResourceDelta[] addedChildren = delta
				.getAffectedChildren(IResourceDelta.ADDED);
		IResourceDelta[] removedChildren = delta
				.getAffectedChildren(IResourceDelta.REMOVED);

		if (addedChildren.length == 0 && removedChildren.length == 0) {
			return;
		}

		final Object[] addedObjects;
		final Object[] removedObjects;

		// Process additions before removals as to not cause selection
		// preservation prior to new objects being added
		// Handle added children. Issue one update for all insertions.
		int numMovedFrom = 0;
		int numMovedTo = 0;
		if (addedChildren.length > 0) {
			addedObjects = new Object[addedChildren.length];
			for (int i = 0; i < addedChildren.length; i++) {
				addedObjects[i] = addedChildren[i].getResource();
				if ((addedChildren[i].getFlags() & IResourceDelta.MOVED_FROM) != 0) {
					++numMovedFrom;
				}
			}
		} else {
			addedObjects = new Object[0];
		}

		// Handle removed children. Issue one update for all removals.
		if (removedChildren.length > 0) {
			removedObjects = new Object[removedChildren.length];
			for (int i = 0; i < removedChildren.length; i++) {
				removedObjects[i] = removedChildren[i].getResource();
				if ((removedChildren[i].getFlags() & IResourceDelta.MOVED_TO) != 0) {
					++numMovedTo;
				}
			}
		} else {
			removedObjects = new Object[0];
		}
		// heuristic test for items moving within same folder (i.e. renames)
		final boolean hasRename = numMovedFrom > 0 && numMovedTo > 0;

		Runnable addAndRemove = () -> {
			if (viewer instanceof AbstractTreeViewer) {
				AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
				// Disable redraw until the operation is finished so we don't
				// get a flash of both the new and old item (in the case of
				// rename)
				// Only do this if we're both adding and removing files (the
				// rename case)
				if (hasRename) {
					treeViewer.getControl().setRedraw(false);
				}
				try {
					if (addedObjects.length > 0) {
						treeViewer.add(resource, addedObjects);
					}
					if (removedObjects.length > 0) {
						treeViewer.remove(removedObjects);
					}
				} finally {
					if (hasRename) {
						treeViewer.getControl().setRedraw(true);
					}
				}
			} else {
				((StructuredViewer) viewer).refresh(resource);
			}
		};
		addAndRemoveRunnables.add(addAndRemove);
	}

	/**
	 * Return a runnable for refreshing a resource.
	 * @return Runnable
	 */
	private Runnable getRefreshRunnable(final IResource resource) {
		return () -> ((StructuredViewer) viewer).refresh(resource);
	}

	/**
	 * Run all of the runnables that are the widget updates
	 */
	private void runUpdates(Collection<Runnable> runnables) {
		for (Runnable runnable : runnables) {
			runnable.run();
		}

	}

}
