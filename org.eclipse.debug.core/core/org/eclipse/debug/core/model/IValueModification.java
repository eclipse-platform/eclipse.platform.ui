package org.eclipse.debug.core.model;

import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to modify the value of a variable in
 * the target.
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
public interface IValueModification {

	/**
	 * Attempts to set the value of this variable to the
	 * value of the given expression.
	 *
	 * @param expression an expression to generate a new value
	 * @exception DebugException on failure. Reasons include:<ul>
	 * <li>TARGET_REQUEST_FAILED - The request failed in the target
	 * <li>NOT_SUPPORTED - The capability is not supported by the target
	 * </ul>
	 */
	public void setValue(String expression) throws DebugException;
	
	/**
	 * Returns whether this varaible supports value modification.
	 *
	 * @return whether this varaible supports value modification
	 */
	public boolean supportsValueModification();
	
	/**
	 * Returns whether the given expression is valid to be used in
	 * setting a new value for this variable.
	 *
	 * @param expression an expression to generate a new value
	 * @return whether the expression is acceptable
	 * @exception DebugException on failure. Reasons include:<ul>
	 * <li>TARGET_REQUEST_FAILED - The request failed in the target
	 * <li>NOT_SUPPORTED - The capability is not supported by the target
	 * </ul>
	 */
	public boolean verifyValue(String expression) throws DebugException;
	
}


