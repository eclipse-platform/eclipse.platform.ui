package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugException;

/**
 * A variable represents a visible variable in a stack frame,
 * or the child of a value. A variable has a type of <code>VARIABLE</code>.
 * A variable's parent may be of type 
 * <code>STACK_FRAME</code> or <code>VALUE</code>.
 * Each variable has a value which may in turn
 * have children. A variable itself does not have children.
 * A variable may support value modification.
 * <p>
 * An implementation may choose to re-use or discard
 * variables on iterative thread suspensions. Clients
 * cannot assume that variables are identical or equal across
 * iterative thread suspensions and must check for equality on iterative
 * suspensions if they wish to re-use the objects.
 * </p>
 * <p>
 * An implementation that preserves equality
 * across iterative suspensions may display more desirable behavior in
 * some clients. For example, if variables are preserved
 * while stepping, a UI client would be able to update the UI incrementally,
 * rather than collapse and redraw the entire list or tree.
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
 * @see IValue
 * @see IStackFrame
 * @see IValueModification
 */
public interface IVariable extends IDebugElement, IValueModification {
	/**
	 * Returns the value of this variable.
	 * 
	 * @return variable value
	 * @exception DebugException if unable to retrieve this variable's value
	 *   from the target
	 */
	IValue getValue() throws DebugException;
	/**
	 * Returns the name of this variable. Name format is debug model
	 * specific, and should be specified by a debug model.
	 *
	 * @return this variable's name
	 * @exception DebugException if unable to retrieve this element's name from
	 *    the target
	 */
	String getName() throws DebugException;	
	/**
	 * Returns a description of the type of data this variable is
	 * declared to reference. Note that the type of a variable
	 * and the type of its value are not always the same.
	 *
	 * @return declared type of variable
	 * @exception DebugException if unable to retrieve this variables reference type
	 *   name from the target
	 */
	String getReferenceTypeName() throws DebugException;
}
