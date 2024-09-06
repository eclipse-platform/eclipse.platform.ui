/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.core;

import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.text.quicksearch.internal.core.priority.DefaultPriorityFunction;
import org.eclipse.text.quicksearch.internal.core.priority.PriorityFunction;
import org.eclipse.text.quicksearch.internal.ui.Messages;
import org.eclipse.text.quicksearch.internal.ui.QuickSearchActivator;

/**
 * A Helper class that allows traversing all the resources in the workspace, assigning priorities
 * to the resources to decide the ordering and completely ignore some resources.
 * <p>
 * The walker can also be paused and resumed.
 *
 * @author Kris De Volder
 */
public abstract class ResourceWalker extends Job {

	private static record QItem(double priority, IResource resource) implements Comparable<QItem> {
		@Override
		public int compareTo(QItem other) {
			return Double.compare(other.priority, this.priority);
		}
	}

	public ResourceWalker() {
		super(Messages.QuickSearchDialog_title);
		init();
	}

	protected void init() {
		filesToSearch.clear();
		addRecursive(filesToSearch, new QItem(0, ResourcesPlugin.getWorkspace().getRoot()));
	}

	private void addRecursive(Collection<QItem> queue, QItem item) {
		IResource r = item.resource;
		if (r instanceof IFile) {
			queue.add(item);
		} else if (r instanceof IContainer f) {
			if (f.isAccessible()) {
				try {
					for (IResource child : f.members()) {
						double p = priority(child);
						if (p != PriorityFunction.PRIORITY_IGNORE) {
							addRecursive(queue, new QItem(p, child));
						}
					}
				} catch (CoreException e) {
					QuickSearchActivator.log(e);
				}
			}
		}
	}

	/**
	 * Work to do. When all work is done this is empty. So it
	 * can also be used to determine 'done' status.
	 */
	private final Set<QItem> filesToSearch = ConcurrentHashMap.newKeySet();

	/**
	 * Setting this to true will cause the ResourceWalker to stop walking. If the walker is running
	 * as a scheduled job, then this Job will terminate. However it is possible to 'resume' the
	 * later since pending list of workitems will be retained.
	 */
	private volatile boolean suspend = false;

	private PriorityFunction prioritFun = new DefaultPriorityFunction();

	public boolean isDone() {
		return filesToSearch.isEmpty();
	}

	/**
	 * Request that the walker stops walking at the next reasonable opportunity.
	 */
	public void suspend() {
		this.suspend = true;
	}

	/**
	 * Request that the walker stops walking at the next reasonable opportunity and drop
	 * all pending workitems. The walker cannot be resumed and must be reinitialized.
	 */
	public void stop() {
		this.filesToSearch.clear();
		this.suspend = false;
	}

	/**
	 * Request the walker to be restarted... i.e. begin walking the resource tree from
	 * the initial state.
	 */

	/**
	 * Request that the walker be resumed. This clears the 'suspend' state if it is set
	 * and ensures that the Job is scheduled.
	 */
	public void resume() {
		if (isDone()) {
			//Well... there's no work so don't bother with doing anything.
			return;
		}
		this.suspend = false;
		this.schedule();
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		int workers = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
		ExecutorService executorService = Executors.newFixedThreadPool(workers);
		// copy the filesToSearch, to only remove a file after search completed
		PriorityQueue<QItem> queue = new PriorityQueue<>();
		queue.addAll(filesToSearch);
		for (int worker = 0; worker < workers; worker++) {
			executorService.submit(() -> {
				QItem item;
				while ((item = queue.poll()) != null) {
					if (monitor.isCanceled() || suspend) {
						break;
					} else {
						IResource r = item.resource;
						IFile f = (IFile) r;
						boolean searched = searchIn(f, () -> monitor.isCanceled() || suspend);
						if (searched) {
							filesToSearch.remove(item);
						}
					}
				}
			});
		}
		try {
			while (!executorService.awaitTermination(1, TimeUnit.MILLISECONDS)) {
				executorService.shutdown();
				if (monitor.isCanceled() || suspend) {
					queue.clear();
					executorService.shutdownNow();
				}
			}
		} catch (InterruptedException e) {
			// ignore
		}
		// on suspend keep unsearched files for later
		if (!suspend) {
			filesToSearch.clear();
		}
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		} else {
			return Status.OK_STATUS;
		}
	}

	protected abstract boolean searchIn(IFile f, BooleanSupplier canceled);

	/**
	 * Assigns a priority to a given resource. This priority will affect the order in which
	 * resources get visited. Resources to be visited are tracked in a priority queue and
	 * at any time the resource with highest priority number is visited first.
	 * <p>
	 * Note that if a resource is a folder then lowering its priority implicitly reduces
	 * the priority of anything nested inside that folder because to visit the children
	 * one has to first visit the parent to reach them.
	 * <p>
	 * If the priority returned is PRIORITY_IGNORE then the resource will be ignored
	 * completely and not visited at all.
	 */
	final double priority(IResource r) {
		return prioritFun.priority(r);
	}

	/**
	 * Set the priority function to use to determine walking order. For the function to
	 * take effect, it should be set before walking has started as the function is
	 * used when elements are added to the work queue during the walk.
	 * <p>
	 * Elements already in the work queue are not re-prioritized if a function is set
	 * in 'mid-run'.
	 */
	public void setPriorityFun(PriorityFunction f) {
		Assert.isNotNull(f, "PriorityFunction should never be null"); //$NON-NLS-1$
		this.prioritFun = f;
	}
}
