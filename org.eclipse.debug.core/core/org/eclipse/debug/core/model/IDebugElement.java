package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;

/**
 * A debug element represents an artifact in a program being
 * debugged.
 * <p>
 * Some methods on debug elements require communication
 * with the target program. Such methods may throw a <code>DebugException</code>
 * with a status code of <code>TARGET_REQUEST_FAILED</code>
 * when unable to complete a request due to a failure on the target.
 * Methods that require communication with the target program or require
 * the target to be in a specific state (for example, suspended), are declared
 * as such.
 * </p>
 * <p>
 * Debug elements are language independent. However, language specific
 * features can be made available via the adpater mechanism provided by
 * <code>IAdaptable</code>, or by extending the debug element interfaces.
 * A debug model is responsible for declaring any special adapters 
 * its debug elements implement.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public interface IDebugElement extends IAdaptable {
	
	/**
	 * Debug target type.
	 *
	 * @see IDebugTarget
	 */
	public static final int DEBUG_TARGET= 0x0002;

	/**
	 * Thread type.
	 *
	 * @see IThread
	 */
	public static final int THREAD= 0x0004;

	/**
	 * Stack frame type.
	 *
	 * @see IStackFrame
	 */
	public static final int STACK_FRAME= 0x0008;

	/**
	 * Variable type.
	 *
	 * @see IVariable
	 */
	public static final int VARIABLE= 0x0010;
	
	/**
	 * Value type.
	 *
	 * @see IValue
	 */
	public static final int VALUE= 0x0012;
	
	/**
	 * Returns the children of this debug element, or an empty collection
	 * if this element has no children.
	 *
	 * @return array of debug elements
	 * @exception DebugException if unable to retrieve this element's children
	 *   from the target
	 */
	IDebugElement[] getChildren() throws DebugException;
	/**
	 * Returns the debug target this element originated from.
	 *
	 * @return a debug target
	 */
	IDebugTarget getDebugTarget();
	/**
	 * Returns the type of this element, encoded as an integer - one
	 * of the constants defined in this interface.
	 *
	 * @return debug element type constant
	 */
	int getElementType();
	/**
	 * Returns the launch this element originated from, or
	 * <code>null</code> if this element is not registered with
	 * a launch. This is a convenience method for
	 * <code>ILaunchManager.findLaunch(getDebugTarget())</code>.
	 *
	 * @return this element's launch, or <code>null</code> if not registered
	 */
	ILaunch getLaunch();
	/**
	 * Returns the name of this element. Name format is debug model
	 * specific, and should be specified by a debug model.
	 *
	 * @return this element's name
	 * @exception DebugException if unable to retrieve this element's name from
	 *    the target
	 */
	String getName() throws DebugException;
	/**
	 * Returns the parent of this debug element, or <code>null</code>
	 * if this element has no parent.
	 *
	 * @return this element's parent, or <code>null</code> if none
	 */
	IDebugElement getParent();
	/**
	 * Returns the process associated with this debug element,
	 * or <code>null</code> if no process is associated with this element.
	 * This is a convenience method - it returns the process associated with
	 * this element's debug target, if any.
	 *
	 * @return this element's process, or <code>null</code> if none
	 */
	IProcess getProcess();
	/**
	 * Returns the source locator that can be used to locate source elements
	 * associated with this element or <code>null</code> if source lookup
	 * is not supported. This is a convenience method - it
	 * returns the source locator associated with this element's launch. 
	 *
	 * @return this element's source locator, or <code>null</code> if none
	 */
	ISourceLocator getSourceLocator();
	/**
	 * Returns the stack frame containing this element,
	 * or <code>null</code> if this element is not contained in a stack frame.
	 *
	 * @return this element's stack frame, or <code>null</code> if none
	 */
	IStackFrame getStackFrame();
	/**
	 * Returns the thread containing this element,
	 * or <code>null</code> if this element is not contained in a thread.
	 *
	 * @return this element's thread, or <code>null</code> if none
	 */
	IThread getThread();
	/**
	 * Returns whether this element has children.
	 *
	 * @return whether this element has children
	 * @exception DebugException if unable to determine if this element has children
	 */
	boolean hasChildren() throws DebugException;
	/**
	 * Returns the unique identifier of the plug-in
	 * this debug element originated from.
	 *
	 * @return plug-in identifier
	 */
	String getModelIdentifier();
}


