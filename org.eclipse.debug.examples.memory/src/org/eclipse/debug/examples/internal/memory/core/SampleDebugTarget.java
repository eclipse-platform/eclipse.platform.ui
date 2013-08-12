/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.internal.memory.core;

import java.math.BigInteger;
import java.util.ArrayList;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.examples.internal.memory.MemoryViewSamplePlugin;
import org.eclipse.debug.examples.internal.memory.engine.SampleEngine;

/**
 * Abstract Sample debug target
 */

public class SampleDebugTarget extends DebugElement implements IDebugTarget, IMemoryBlockRetrievalExtension {

	boolean fTerminate = false;
	boolean fSuspend = true;

	protected ILaunch fLaunch;
	protected SampleEngine fEngine = new SampleEngine();
	protected ArrayList<IMemoryBlockExtension> fMemoryBlocks = new ArrayList<IMemoryBlockExtension>();
	protected IThread fThread;
	protected boolean fBusy;

	/**
	 * Creates SampleDebugTarget
	 * 
	 * @param launch the launch this debug target belongs to
	 */
	public SampleDebugTarget(ILaunch launch) {
		super(null);
		fLaunch = launch;
		fireEvent(new DebugEvent(this, DebugEvent.CREATE));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getProcess()
	 */
	@Override
	public IProcess getProcess() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#hasThreads()
	 */
	@Override
	public boolean hasThreads() throws DebugException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.core.model.IDebugTarget#supportsBreakpoint(org.eclipse
	 * .debug.core.model.IBreakpoint)
	 */
	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	@Override
	public IDebugTarget getDebugTarget() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	@Override
	public ILaunch getLaunch() {
		return fLaunch;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	@Override
	public boolean canTerminate() {
		return !fTerminate;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	@Override
	public boolean isTerminated() {
		return fTerminate;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	@Override
	public void terminate() throws DebugException {
		fTerminate = true;
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	@Override
	public boolean canResume() {
		return fSuspend && !fTerminate;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	@Override
	public boolean canSuspend() {
		return !fSuspend && !fTerminate;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	@Override
	public boolean isSuspended() {
		return fSuspend;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	@Override
	public void resume() throws DebugException {
		fSuspend = false;
		fEngine.resume();
		fireEvent(new DebugEvent(this, DebugEvent.RESUME));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	@Override
	public void suspend() throws DebugException {
		fSuspend = true;
		fireEvent(new DebugEvent(getEngine().getThreads(this)[0], DebugEvent.SUSPEND));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse
	 * .debug.core.model.IBreakpoint)
	 */
	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse
	 * .debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse
	 * .debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#canDisconnect()
	 */
	@Override
	public boolean canDisconnect() {

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#disconnect()
	 */
	@Override
	public void disconnect() throws DebugException {

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#isDisconnected()
	 */
	@Override
	public boolean isDisconnected() {

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval
	 * ()
	 */
	@Override
	public boolean supportsStorageRetrieval() {
		return true;
	}

	/**
	 * @return the debug engine
	 */
	public SampleEngine getEngine() {
		return fEngine;
	}

	/**
	 * Remove the memory block from this debug session.
	 * 
	 * @param memBlk
	 */
	public void removeMemoryBlock(IMemoryBlock memBlk) {
		fMemoryBlocks.remove(memBlk);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long,
	 * long)
	 */
	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {

		return null;
	}

	@Override
	public Object getAdapter(Class adapter) {

		if (adapter == ILaunch.class) {
			return getLaunch();
		}

		return super.getAdapter(adapter);
	}

	@Override
	public IThread[] getThreads() throws DebugException {
		if (isTerminated()) {
			return new IThread[0];
		}

		return getEngine().getThreads(this);
	}

	@Override
	public String getName() throws DebugException {
		return Messages.SampleDebugTarget_0;
	}

	@Override
	public String getModelIdentifier() {
		return MemoryViewSamplePlugin.PLUGIN_ID;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension#
	 * getExtendedMemoryBlock(java.lang.String, java.lang.Object)
	 */
	@Override
	public IMemoryBlockExtension getExtendedMemoryBlock(String expression, Object context) throws DebugException {

		// ask debug engine for an address
		BigInteger address = getEngine().evaluateExpression(expression, context);

		// if address can be evaluated to an address, create memory block
		if (address != null) {
			IMemoryBlockExtension memoryBlock = new SampleMemoryBlock(this, expression, address);
			fMemoryBlocks.add(memoryBlock);

			return memoryBlock;
		}
		// otherwise throw debug exception
		IStatus status = new Status(IStatus.ERROR, MemoryViewSamplePlugin.PLUGIN_ID, 0, Messages.SampleDebugTarget_1, null);
		DebugException exception = new DebugException(status);
		throw exception;
	}
}
