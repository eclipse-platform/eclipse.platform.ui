/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

/**
 * Implementation of common function for debug targets.
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 3.1
 */
public abstract class DebugTarget extends DebugElement implements IDebugTarget {
	
	private IProcess fProcess;
	private ILaunch fLaunch;
	private String fName;
	private List fThreads;

	/**
	 * Constructs a new debug target in the given launch associated
	 * with the given process.
	 * 
	 * @param launch launch the target is contained in
	 * @param process associated process or <code>null</code>
	 */
	public DebugTarget(ILaunch launch, IProcess process) {
		super(null);
		fLaunch = launch;
		fProcess = process;
		fThreads = new ArrayList();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getProcess()
	 */
	public IProcess getProcess() {
		return fProcess;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fLaunch;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getThreads()
	 */
	public IThread[] getThreads() throws DebugException {
		synchronized (fThreads) {
			return (IThread[]) fThreads.toArray(new IThread[fThreads.size()]);
		}
	}
	
	/**
	 * Adds the given thread to this target's list of threads.
	 * Has no effect if an equivalent thread is already registered.
	 * 
	 * @param thread thread to add
	 */
	protected void addThread(IThread thread) {
		synchronized (fThreads) {
			if (!fThreads.contains(thread)) {
				fThreads.add(thread);
			}
		}
	}
	
	/**
	 * Removes the given thread from this target's list of threads.
	 * Has no effect if an equivalent thread is not already
	 * registered.
	 * 
	 * @param thread thread to remove
	 */
	protected void removeThread(IThread thread) {
		synchronized (fThreads) {
			fThreads.remove(thread);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#hasThreads()
	 */
	public boolean hasThreads() throws DebugException {
		synchronized (fThreads) {
			return !fThreads.isEmpty();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getName()
	 */
	public String getName() throws DebugException {
		return fName;
	}
	
	/**
	 * Sets the name of this debug target.
	 * 
	 * @param name
	 */
	protected void setName(String name) {
		fName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#supportsBreakpoint(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		return !isTerminated() && !isDisconnected() && breakpoint.getModelIdentifier().equals(getModelIdentifier());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return !isTerminated() && !isDisconnected();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return !isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
	 */
	public boolean supportsStorageRetrieval() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long, long)
	 */
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		notSupported(DebugCoreMessages.getString("DebugTarget.0"), null); //$NON-NLS-1$
		return null;
	}

}
