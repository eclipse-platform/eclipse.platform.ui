package org.eclipse.debug.core.model;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
import org.eclipse.debug.core.DebugException;

/**
 * A value represents the value of a variable.
 * A value does not have a parent - instead it has an associated variable.
 * A value representing a complex data structure has
 * children of type <code>VARIABLE</code>.
 * <p>
 * An implementation may choose to re-use or discard
 * values on iterative thread suspensions. Clients
 * cannot assume that values are identical or equal across
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
 * @see IVariable
 */


public interface IValue extends IDebugElement {
	
	/**
	 * Returns a description of the type of data this value contains
	 * or references.
	 * 
	 * @return reference type
	 * @exception DebugException if unable to retrieve this value's reference type
	 *    name from the target
	 */
	String getReferenceTypeName() throws DebugException;
	
	/**
	 * Returns this value as a <code>String</code>.
	 *
	 * @return value
	 * @exception DebugException if unable to retrieve this value's value description
	 *   from the target
	 */
	String getValueString() throws DebugException;

	/**
	 * Returns the name of this element. Implementations of <code>IValue</code>
	 * return <code>null</code>. Values do not have names.
	 *
	 * @return <code>null</code> 
	 */
	String getName();
	
	/**
	 * Returns the variable this value is bound to, or <code>null</code>
	 * if this value is not bound to a variable.
	 *
	 * @return a variable, or <code>null</code> if none
	 */
	IVariable getVariable();
	
	/**
	 * Returns the parent of this element. Implementations of <code>IVariable</code>
	 * return <code>null</code>. Values do not have parents.
	 *
	 * @return <code>null</code>
	 */
	IDebugElement getParent();
	
	/**
	 * Returns whether this value is currently allocated.
	 * <p>
	 * For example, if this value represents
	 * an object that has been garbage collected, <code>false</code> would
	 * be returned.
	 * </p>
	 * @return whether this value is currently allocated
	 * @exception DebugException if unable to determine if this value is currently
	 *   allocated on the target
	 */
	boolean isAllocated() throws DebugException;
}