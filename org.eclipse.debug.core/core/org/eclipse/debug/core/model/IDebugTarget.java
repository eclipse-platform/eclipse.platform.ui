package org.eclipse.debug.core.model;


/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.List;

/**
 * A debug target is a debuggable execution context. For example, a debug target
 * may represent a debuggable process or a virtual machine. A debug target is the root
 * of the debug element hierarchy. A debug target has element type <code>DEBUG_TARGET</code>,
 * children of type <code>THREAD</code>, and no parent. Minimally, a debug target supports
 * the following capabilities:
 * <ul>
 * <li>terminate
 * <li>suspend/resume
 * <li>breakpoints
 * <li>diconnect
 * </ul>
 * <p>
 * Generally, launching a debug session results in the creation of a
 * debug target. Launching is a client responsibility, as is debug target
 * creation.
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see ITerminate
 * @see ISuspendResume
 * @see IBreakpointSupport
 * @see IDisconnect
 * @see org.eclipse.debug.core.ILaunch
 */
public interface IDebugTarget extends IDebugElement, ITerminate, ISuspendResume, IBreakpointSupport, IDisconnect {
	/**
	 * Returns the system process associated with this debug target
	 * or <code>null</code> if no system process is associated with
	 * this debug target.
	 */
	IProcess getProcess();
}


