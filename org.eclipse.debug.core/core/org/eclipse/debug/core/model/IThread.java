package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;

/**
 * A thread is a sequential flow of execution in a debug target. A
 * thread is of element type <code>THREAD</code>. A thread contains
 * stack frames.  Stack frames are only available when the thread is 
 * suspended, and are returned in top-down order.
 * Minimally, a thread supports the following capabilities:
 * <ul>
 * <li>suspend/resume
 * <li>stepping
 * <li>terminate
 * </ul>
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see ISuspendResume
 * @see IStep
 * @see ITerminate
 * @see IStackFrame
 */

public interface IThread extends IDebugElement, ISuspendResume, IStep, ITerminate {
	/**
	 * Returns the stack frames contained in this thread. An
	 * empty collection is returned if this thread contains
	 * no stack frames, or is not currently suspended. Stack frames
	 * are returned in top down order.
	 * 
	 * @return a collection of stack frames
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public IStackFrame[] getStackFrames() throws DebugException;
	
	/**
	 * Returns whether this thread currently contains any stack
	 * frames.
	 * 
	 * @return whether this thread currently contains any stack frames
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * @since 2.0
	 */
	public boolean hasStackFrames() throws DebugException;
	
	/**
	 * Returns the priority of this thread. The meaning of this
	 * number is operating-system dependent.
	 *
	 * @return thread priority
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public int getPriority() throws DebugException;
	/** 
	 * Returns the top stack frame or <code>null</code> if there is
	 * currently no top stack frame.
	 *
	 * @return the top stack frame, or <code>null</code> if none
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public IStackFrame getTopStackFrame() throws DebugException;
	/**
	 * Returns the name of this thread. Name format is debug model
	 * specific, and should be specified by a debug model.
	 *
	 * @return this thread's name
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public String getName() throws DebugException;

	/**
	 * Returns the breakpoint that caused this thread to suspend,
	 * or <code>null</code> if this thread is not suspended or
	 * was not suspended by a breakpoint.
	 *
	 * @return breakpoint that caused suspend, or <code>null</code> if none
	 */
	public IBreakpoint getBreakpoint();	
}
