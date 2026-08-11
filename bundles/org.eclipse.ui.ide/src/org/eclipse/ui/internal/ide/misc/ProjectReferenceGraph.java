/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Workspace-wide cache of the connected components of the project reference
 * graph. Two projects are in the same component if one references the other,
 * directly or transitively, in either direction.
 * <p>
 * Building it calls {@link IProject#getReferencedProjects()} for every project,
 * which runs the dynamic reference providers and can take minutes. It therefore
 * runs in a background job, never on the calling thread.
 * </p>
 */
public class ProjectReferenceGraph {

	/** Job family of the background rebuild. */
	public static final Object FAMILY_REBUILD = new Object();

	private static final int MAX_REFRESH_ATTEMPTS = 3;

	/** Delay of the rebuild that follows one invalidated while it was running. */
	private static final long RETRY_DELAY = 200;

	private static ProjectReferenceGraph instance;

	private final Object lock = new Object();

	private List<List<IProject>> components = Collections.emptyList();

	/** Incremented whenever a resource change may have altered the graph. */
	private int requestedGeneration = 1;

	/** Generation {@link #components} was built from. Equal means up to date. */
	private int builtGeneration;

	private final Set<Runnable> rebuildCallbacks = new LinkedHashSet<>();

	private final Job rebuildJob = new Job(IDEWorkbenchMessages.ProjectReferenceGraph_jobName) {
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			rebuild();
			return Status.OK_STATUS;
		}

		@Override
		public boolean belongsTo(Object family) {
			return FAMILY_REBUILD == family;
		}
	};

	public static synchronized ProjectReferenceGraph getInstance() {
		if (instance == null) {
			instance = new ProjectReferenceGraph();
		}
		return instance;
	}

	private ProjectReferenceGraph() {
		rebuildJob.setSystem(true);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this::invalidateOnResourceChange,
				IResourceChangeEvent.POST_CHANGE);
	}

	/**
	 * Returns the components known so far, which may be stale or empty. If they
	 * are stale, a background rebuild is started and <code>whenRebuilt</code> is
	 * run on the job's thread once it completed.
	 *
	 * @param whenRebuilt may be <code>null</code>
	 */
	public List<List<IProject>> getComponents(Runnable whenRebuilt) {
		List<List<IProject>> known;
		boolean stale;
		synchronized (lock) {
			known = components;
			stale = requestedGeneration != builtGeneration;
			if (stale && whenRebuilt != null) {
				rebuildCallbacks.add(whenRebuilt);
			}
		}
		if (stale) {
			rebuildJob.schedule();
		}
		return known;
	}

	/**
	 * Waits for a rebuild instead of computing the components on the calling
	 * thread, and returns whether they ended up current. Retries are bounded: a
	 * running build keeps invalidating them, and blocking a user gesture until the
	 * workspace goes quiet would be worse than answering from components that are
	 * one rebuild old.
	 */
	public boolean refresh(IProgressMonitor monitor) throws InterruptedException {
		for (int attempt = 0; attempt < MAX_REFRESH_ATTEMPTS; attempt++) {
			synchronized (lock) {
				if (requestedGeneration == builtGeneration) {
					return true;
				}
			}
			rebuildJob.schedule();
			Job.getJobManager().join(FAMILY_REBUILD, monitor);
		}
		synchronized (lock) {
			return requestedGeneration == builtGeneration;
		}
	}

	private void rebuild() {
		int generation;
		synchronized (lock) {
			generation = requestedGeneration;
		}
		// the generation is read before building, so a change arriving during the
		// build leaves the result marked stale instead of being lost
		List<List<IProject>> built = buildComponents(ResourcesPlugin.getWorkspace().getRoot().getProjects());
		List<Runnable> callbacks;
		boolean retry;
		synchronized (lock) {
			components = built;
			builtGeneration = generation;
			if (requestedGeneration != generation) {
				// stale again: notifying now would report components that are already
				// outdated, so leave the callbacks registered for the rebuild that
				// converges
				retry = !rebuildCallbacks.isEmpty();
				callbacks = List.of();
			} else {
				retry = false;
				callbacks = new ArrayList<>(rebuildCallbacks);
				rebuildCallbacks.clear();
			}
		}
		if (retry) {
			// nothing else would start that rebuild, because invalidating only marks
			// the components stale; the delay keeps the job from running back to back
			// while the workspace keeps changing
			rebuildJob.schedule(RETRY_DELAY);
			return;
		}
		for (Runnable callback : callbacks) {
			SafeRunner.run(callback::run);
		}
	}

	/**
	 * Invalidates the components on any workspace change. Filtering by delta kind
	 * would miss that a dynamic reference provider can derive its references from
	 * any file of a project, such as a plug-in manifest or a class path.
	 */
	private void invalidateOnResourceChange(IResourceChangeEvent event) {
		if (event.getDelta() == null) {
			return;
		}
		synchronized (lock) {
			requestedGeneration++;
		}
	}

	private static List<List<IProject>> buildComponents(IProject[] projects) {
		DisjointSet<IProject> set = buildDisjointSet(projects);
		Map<IProject, List<IProject>> componentsByRepresentative = new LinkedHashMap<>();
		for (IProject project : projects) {
			IProject representative = set.findSet(project);
			if (representative != null) {
				componentsByRepresentative.computeIfAbsent(representative, key -> new ArrayList<>()).add(project);
			}
		}
		List<List<IProject>> result = new ArrayList<>(componentsByRepresentative.size());
		for (List<IProject> component : componentsByRepresentative.values()) {
			result.add(List.copyOf(component));
		}
		return List.copyOf(result);
	}

	/**
	 * Builds the connected component set for the input projects. The result is a
	 * DisjointSet where all related projects belong to the same set.
	 */
	private static DisjointSet<IProject> buildDisjointSet(IProject[] projects) {
		// initially each vertex is in a set by itself
		DisjointSet<IProject> set = new DisjointSet<>();
		for (IProject project : projects) {
			set.makeSet(project);
		}
		for (IProject project : projects) {
			try {
				IProject[] references = project.getReferencedProjects();
				// each reference represents an edge in the project reference
				// digraph from projects[i] -> references[j]
				for (IProject reference : references) {
					IProject setOne = set.findSet(project);
					// note that referenced projects may not exist in the workspace
					IProject setTwo = set.findSet(reference);
					// these two projects are related, so join their sets
					if (setOne != null && setTwo != null && setOne != setTwo) {
						set.union(setOne, setTwo);
					}
				}
			} catch (CoreException e) {
				// assume inaccessible projects have no references
			}
		}
		return set;
	}
}
