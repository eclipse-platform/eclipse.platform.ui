package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;

/**
 * A thread represents a thread of execution in a debug target. A
 * thread has element type <code>THREAD</code>, and a parent
 * of type <code>DEBUG_TARGET</code>. The children of a thread are of
 * type <code>STACK_FRAME</code>. A thread only has children when
 * suspended, and children are returned in top-down order.
 * Minimally, a thread supports the following capabilities:
 * <ul>
 * <li>suspend/resume
 * <li>stepping
 * <li>termiante
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
	 * Returns the priority of this thread. The meaning of this
	 * number is operating-system dependent.
	 *
	 * @return thread priority
	 * @exception DebugException if unable to retrieve this thread's priority from
	 *   the target
	 */
	int getPriority() throws DebugException;
	/** 
	 * Returns the top stack frame or <code>null</code> if there is
	 * currently no top stack frame.
	 *
	 * @return the top stack frame, or <code>null</code> if none
	 * @exception DebugException if unable to retrieve this thread's top stack frame
	 *   from the target
	 */
	IStackFrame getTopStackFrame() throws DebugException;
}
