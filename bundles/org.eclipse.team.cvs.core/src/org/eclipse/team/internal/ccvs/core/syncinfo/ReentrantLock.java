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
package org.eclipse.team.internal.ccvs.core.syncinfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * Provides a per-thread nested locking mechanism. A thread can acquire a
 * lock on a specific resource by calling acquire(). Subsequently, acquire() can be called
 * multiple times on the resource or any of its children from within the same thread
 * without blocking. Other threads that try
 * and acquire the lock on those same resources will be blocked until the first 
 * thread releases all it's nested locks.
 * <p>
 * The locking is managed by the platform via scheduling rules. This class simply 
 * provides the nesting mechnism in order to allow the client to determine when
 * the lock for the thread has been released. Therefore, this lock will block if
 * another thread already locks the same resource.</p>
 */
public class ReentrantLock {

	private final static boolean DEBUG = Policy.DEBUG_THREADING;
	
	public class ThreadInfo {
		private int nestingCount = 0;
		private Set changedResources = new HashSet();
		private Set changedFolders = new HashSet();
		private IFlushOperation operation;
		private ISchedulingRule schedulingRule;
		public ThreadInfo(ISchedulingRule schedulingRule, IFlushOperation operation) {
			this.schedulingRule = schedulingRule;
			this.operation = operation;
		}
		public void increment() {
			nestingCount++;
		}
		public int decrement() {
			nestingCount--;
			return nestingCount;
		}
		public int getNestingCount() {
			return nestingCount;
		}
		public void addChangedResource(IResource resource) {
			changedResources.add(resource);
		}
		public void addChangedFolder(IContainer container) {
			changedFolders.add(container);
		}
		public boolean isEmpty() {
			return changedFolders.isEmpty() && changedResources.isEmpty();
		}
		public IResource[] getChangedResources() {
			return (IResource[]) changedResources.toArray(new IResource[changedResources.size()]);
		}
		public IContainer[] getChangedFolders() {
			return (IContainer[]) changedFolders.toArray(new IContainer[changedFolders.size()]);
		}
		public void flush(IProgressMonitor monitor) throws CVSException {
			try {
				operation.flush(this, monitor);
			} catch (OutOfMemoryError e) {
				throw e;
			} catch (Error e) {
				handleAbortedFlush(e);
				throw e;
			} catch (RuntimeException e) {
				handleAbortedFlush(e);
				throw e;
			}
			changedResources.clear();
			changedFolders.clear();
		}
		private void handleAbortedFlush(Throwable t) {
			CVSProviderPlugin.log(new CVSStatus(IStatus.ERROR, Policy.bind("ReentrantLock.9"), t)); //$NON-NLS-1$
		}
		public ISchedulingRule getSchedulingRule() {
			return schedulingRule;
		}
	}
	
	public interface IFlushOperation {
		public void flush(ThreadInfo info, IProgressMonitor monitor) throws CVSException;
	}
	
	private Map infos = new HashMap();
	
	
	public ReentrantLock() {
	}
	
	private ThreadInfo getThreadInfo() {
		Thread thisThread = Thread.currentThread();
		ThreadInfo info = (ThreadInfo)infos.get(thisThread);
		return info;
	}
	
	public synchronized void acquire(IResource resource, IFlushOperation operation) {
		ISchedulingRule ruleUsed = lock(resource);	
		incrementNestingCount(resource, ruleUsed, operation);
	}
	
	private void incrementNestingCount(IResource resource, ISchedulingRule rule, IFlushOperation operation) {
		ThreadInfo info = getThreadInfo();
		if (info == null) {
			info = new ThreadInfo(rule, operation);
			Thread thisThread = Thread.currentThread();
			infos.put(thisThread, info);
			if(DEBUG) System.out.println("[" + thisThread.getName() + "] acquired CVS lock on " + resource.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		info.increment();
	}
	
	private ISchedulingRule lock(IResource resource) {
		// The scheduling rule is either the project or the resource's parent
		ISchedulingRule rule;
		if (resource.getType() == IResource.ROOT) {
			// Never lock the whole workspace
			rule = null;
		} else  if (resource.getType() == IResource.PROJECT) {
			rule = resource;
		} else {
			rule = resource.getParent();
		}
		if (rule != null) {
			Platform.getJobManager().beginRule(rule);
		}
		return rule;
	}

	private void unlock(ISchedulingRule rule) {
		if (rule != null) {
			Platform.getJobManager().endRule(rule);
		}
	}
	
	/**
	 * Release the lock held on any resources by this thread. Execute the 
	 * provided runnable if the lock is no longer held (i.e. nesting count is 0).
	 * On exit, the scheduling rule is held by the lock until after the runnable
	 * is run.
	 */
	public synchronized void release(IProgressMonitor monitor) throws CVSException {
		ThreadInfo info = getThreadInfo();
		Assert.isNotNull(info, "Unmatched acquire/release."); //$NON-NLS-1$
		Assert.isTrue(info.getNestingCount() > 0, "Unmatched acquire/release."); //$NON-NLS-1$
		if (info.decrement() == 0) {
			Thread thisThread = Thread.currentThread();
			if(DEBUG) System.out.println("[" + thisThread.getName() + "] released CVS lock"); //$NON-NLS-1$ //$NON-NLS-2$
			infos.remove(thisThread);
			info.flush(monitor);
			unlock(info.getSchedulingRule());
		}
	}

	public void folderChanged(IContainer folder) {
		ThreadInfo info = getThreadInfo();
		Assert.isNotNull(info, "Folder changed outside of resource lock"); //$NON-NLS-1$
		info.addChangedFolder(folder);
	}

	public void resourceChanged(IResource resource) {
		ThreadInfo info = getThreadInfo();
		Assert.isNotNull(info, "Folder changed outside of resource lock"); //$NON-NLS-1$
		info.addChangedResource(resource);
	}

	/**
	 * Flush any changes accumulated by the lock so far.
	 */
	public void flush(IProgressMonitor monitor) throws CVSException {
		ThreadInfo info = getThreadInfo();
		Assert.isNotNull(info, "Flush requested outside of resource lock"); //$NON-NLS-1$
		info.flush(monitor);
	}
}
