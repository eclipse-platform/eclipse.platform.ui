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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
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
	
	// This is a placeholder rule used to indicate that no scheduling rule is needed
	/* internal use only */ static final ISchedulingRule NULL_SCHEDULING_RULE= new ISchedulingRule() {
		public boolean contains(ISchedulingRule rule) {
			return false;
		}
		public boolean isConflicting(ISchedulingRule rule) {
			return false;
		}
	};
	
	public class ThreadInfo {
		private Set changedResources = new HashSet();
		private Set changedFolders = new HashSet();
		private Set changedIgnoreFiles = new HashSet();
		private IFlushOperation operation;
		private List rules = new ArrayList();
		public ThreadInfo(IFlushOperation operation) {
			this.operation = operation;
		}
		/**
		 * Push a scheduling rule onto the stack for this thread and
		 * acquire the rule if it is not the workspace root.
		 * @param resource
		 */
		public void pushRule(IResource resource) {
			// The scheduling rule is either the project or the resource's parent
			ISchedulingRule rule = getRuleForResoure(resource);
			if (rule != NULL_SCHEDULING_RULE) {
				Platform.getJobManager().beginRule(rule);
			}
			addRule(rule);
		}
		/**
		 * Pop the scheduling rule from the stack and release it if it
		 * is not the workspace root. Flush any changed sync info to 
		 * disk if necessary. A flush is necessary if the stack is empty
		 * or if the top-most non-null scheduling rule was popped as a result
		 * of this operation.
		 * @param monitor
		 * @throws CVSException
		 */
		public void popRule(IResource resource, IProgressMonitor monitor) throws CVSException {
			ISchedulingRule rule = removeRule();
			ISchedulingRule compareRule = getRuleForResoure(resource);
			Assert.isTrue(rule.equals(compareRule), "end for resource '" + resource + "' does not match stacked rule '" + rule + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			try {
				if (isFlushRequired()) {
					flush(monitor);
				}
			} finally {
				if (rule != NULL_SCHEDULING_RULE) {
					Platform.getJobManager().endRule(rule);
				}
			}
		}
		private ISchedulingRule getRuleForResoure(IResource resource) {
			ISchedulingRule rule;
			if (resource.getType() == IResource.ROOT) {
				// Never lock the whole workspace
				rule = NULL_SCHEDULING_RULE;
			} else  if (resource.getType() == IResource.PROJECT) {
				rule = resource;
			} else {
				rule = resource.getParent();
			}
			return rule;
		}
		/**
		 * Return <code>true</code> if we are still nested in
		 * an acquire for this thread.
		 * 
		 * @return
		 */
		public boolean isNested() {
			return !rules.isEmpty();
		}
		public void addChangedResource(IResource resource) {
			changedResources.add(resource);
		}
		public void addChangedFolder(IContainer container) {
			changedFolders.add(container);
		}
		public void addChangedIgnoreFile(IFile resource) {
			changedIgnoreFiles.add(resource);
		}
		public boolean isEmpty() {
			return changedFolders.isEmpty() && changedResources.isEmpty() && changedIgnoreFiles.isEmpty();
		}
		public IResource[] getChangedResources() {
			return (IResource[]) changedResources.toArray(new IResource[changedResources.size()]);
		}
		public IContainer[] getChangedFolders() {
			return (IContainer[]) changedFolders.toArray(new IContainer[changedFolders.size()]);
		}
		public IFile[] getChangedIgnoreFiles() {
			return (IFile[]) changedIgnoreFiles.toArray(new IFile[changedIgnoreFiles.size()]);
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
		private boolean isFlushRequired() {
			return !isNested() || !isNoneNullRules();
		}
		private boolean isNoneNullRules() {
			for (Iterator iter = rules.iterator(); iter.hasNext();) {
				ISchedulingRule rule = (ISchedulingRule) iter.next();
				if (rule != NULL_SCHEDULING_RULE) {
					return true;
				}
			}
			return false;
		}
		private void handleAbortedFlush(Throwable t) {
			CVSProviderPlugin.log(new CVSStatus(IStatus.ERROR, Policy.bind("ReentrantLock.9"), t)); //$NON-NLS-1$
		}
		private void addRule(ISchedulingRule rule) {
			rules.add(rule);
		}
		private ISchedulingRule removeRule() {
			return (ISchedulingRule)rules.remove(rules.size() - 1);
		}
		public boolean ruleContains(IResource resource) {
			for (Iterator iter = rules.iterator(); iter.hasNext();) {
				ISchedulingRule rule = (ISchedulingRule) iter.next();
				if (rule != NULL_SCHEDULING_RULE) {
					return rule.contains(resource);
				}
			}
			return false;
		}
	}
	
	public interface IFlushOperation {
		public void flush(ThreadInfo info, IProgressMonitor monitor) throws CVSException;
	}
	
	private Map infos = new HashMap();
	
	private ThreadInfo getThreadInfo() {
		Thread thisThread = Thread.currentThread();
		ThreadInfo info = (ThreadInfo)infos.get(thisThread);
		return info;
	}
	
	private ThreadInfo getThreadInfo(IResource resource) {
		for (Iterator iter = infos.values().iterator(); iter.hasNext();) {
			ThreadInfo info = (ThreadInfo) iter.next();
			if (info.ruleContains(resource)) {
				return info;
			}
		}
		return null;
	}
	
	public synchronized void acquire(IResource resource, IFlushOperation operation) {
		ThreadInfo info = getThreadInfo();
		if (info == null) {
			info = new ThreadInfo(operation);
			Thread thisThread = Thread.currentThread();
			infos.put(thisThread, info);
			if(DEBUG) System.out.println("[" + thisThread.getName() + "] acquired CVS lock on " + resource.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		info.pushRule(resource);
	}
	
	/**
	 * Release the lock held on any resources by this thread. Execute the 
	 * provided runnable if the lock is no longer held (i.e. nesting count is 0).
	 * On exit, the scheduling rule is held by the lock until after the runnable
	 * is run.
	 */
	public synchronized void release(IResource resource, IProgressMonitor monitor) throws CVSException {
		ThreadInfo info = getThreadInfo();
		Assert.isNotNull(info, "Unmatched acquire/release."); //$NON-NLS-1$
		Assert.isTrue(info.isNested(), "Unmatched acquire/release."); //$NON-NLS-1$
		info.popRule(resource, monitor);
		if (!info.isNested()) {
			Thread thisThread = Thread.currentThread();
			if(DEBUG) System.out.println("[" + thisThread.getName() + "] released CVS lock"); //$NON-NLS-1$ //$NON-NLS-2$
			infos.remove(thisThread);
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

	/**
	 * Return <code>true</code> if the current thread is part of a CVS operation
	 * and the given resource is contained the scheduling rule held by that operation.
	 * @param resource
	 * @return
	 */
	public synchronized boolean isWithinActiveThread(IResource resource) {
		return getThreadInfo(resource) != null;
	}

	/**
	 * Record the ignore file change as part of the current operation.
	 * @param resource
	 */
	public synchronized void recordIgnoreFileChange(IFile resource) {
		ThreadInfo info = getThreadInfo(resource);
		Assert.isNotNull(info);
		info.addChangedIgnoreFile(resource);
	}
}
