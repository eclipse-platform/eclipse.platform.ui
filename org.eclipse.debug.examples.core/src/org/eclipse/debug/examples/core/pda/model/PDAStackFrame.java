/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     Pawel Piech (Wind River) - ported PDA Virtual Machine to Java (Bug 261400)
 *******************************************************************************/
package org.eclipse.debug.examples.core.pda.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.examples.core.protocol.PDAFrameData;

/**
 * PDA stack frame.
 */
public class PDAStackFrame extends PDADebugElement implements IStackFrame {
	
	private PDAThread fThread;
	private String fName;
	private int fPC;
	private String fFileName;
	private int fId;
	
	/**
	 * Constructs a stack frame in the given thread with the given
	 * frame data.
	 * 
	 * @param thread
	 * @param data frame data
	 * @param id stack frame id (0 is the bottom of the stack)
	 */
	public PDAStackFrame(PDAThread thread, PDAFrameData data, int id) {
		super(thread.getPDADebugTarget());
		fId = id;
		fThread = thread;
		init(data);
	}
	
	/**
	 * Initializes this frame based on its data
	 * 
	 * @param data
	 */
	private void init(PDAFrameData data) {
		fFileName = data.fFilePath.lastSegment();
		fPC = data.fPC + 1;
		fName = data.fFunction;
		IVariable[] vars = new IVariable[data.fVariables.length];
		for (int i = 0; i < data.fVariables.length; i++) {
			vars[i] = new PDAVariable(this, data.fVariables[i]);
		}
		fThread.setVariables(this, vars);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getThread()
	 */
	public IThread getThread() {
		return fThread;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		return fThread.getVariables(this);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return getVariables().length > 0;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getLineNumber()
	 */
	public int getLineNumber() throws DebugException {
		return fPC;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharStart()
	 */
	public int getCharStart() throws DebugException {
		return -1;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharEnd()
	 */
	public int getCharEnd() throws DebugException {
		return -1;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getName()
	 */
	public String getName() throws DebugException {
		return fName;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getRegisterGroups()
	 */
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasRegisterGroups()
	 */
	public boolean hasRegisterGroups() throws DebugException {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto() {
		return getThread().canStepInto();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */
	public boolean canStepOver() {
		return getThread().canStepOver();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn() {
		return getThread().canStepReturn();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public boolean isStepping() {
		return getThread().isStepping();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
	public void stepInto() throws DebugException {
		getThread().stepInto();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		getThread().stepOver();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
		getThread().stepReturn();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return getThread().canResume();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return getThread().canSuspend();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return getThread().isSuspended();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		getThread().resume();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		getThread().suspend();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return getThread().canTerminate();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return getThread().isTerminated();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		getThread().terminate();
	}
	
	/**
	 * Returns the name of the source file this stack frame is associated
	 * with.
	 * 
	 * @return the name of the source file this stack frame is associated
	 * with
	 */
	public String getSourceName() {
		return fFileName;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PDAStackFrame) {
			PDAStackFrame sf = (PDAStackFrame)obj;
			return sf.getThread().equals(getThread()) && 
				sf.getSourceName().equals(getSourceName()) &&
				sf.fId == fId;
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getSourceName().hashCode() + fId;
	}
	
	/**
	 * Returns this stack frame's unique identifier within its thread
	 * 
	 * @return this stack frame's unique identifier within its thread
	 */
	protected int getIdentifier() {
		return fId;
	}
	
    /**
     * Returns the stack frame's thread's unique identifier
     * 
     * @return this stack frame's thread's unique identifier
     * 
     * @since 3.5
     */
	protected int getThreadIdentifier() {
	    return fThread.getIdentifier();
	}
	
}
