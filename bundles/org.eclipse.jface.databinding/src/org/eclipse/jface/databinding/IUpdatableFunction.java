/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2005-2006. All Rights Reserved. 
 * 
 * Note to U.S. Government Users Restricted Rights:  Use, 
 * duplication or disclosure restricted by GSA ADP Schedule 
 * Contract with IBM Corp.
 *******************************************************************************/
package org.eclipse.jface.databinding;

/**
 * Represents a function whose value may change over time. The function
 * will fire a CHANGE event whenever the value of the function changes
 * for some domain object. The getNewValue() in the ChangeEvent will be
 * a Collection of domain objects for which the function value has changed.
 * 
 * <p>
 * A function is only required to report CHANGE events for values in its 
 * domain that it has previously been asked to evaluate. That is, an
 * updatable function will only fire a CHANGE event on an object that 
 * object was previously used as the argument to computeResult. 
 * This is permits functions to optimize their listeners. If the function
 * is attaching a listener to each domain object, it only needs to attach
 * the listener the first time it is asked about that object and it can
 * remove the listener whenever it fires a CHANGE event for the object.
 * </p>
 * 
 * Fires event types: CHANGE_MANY, STALE  
 * 
 * <p>
 * Not intended to be implemented by clients. Clients should subclass 
 * UpdatableFunction.
 * </p>
 * 
 * @see IUpdatableFunctionFactory
 * @since 3.2
 */
public interface IUpdatableFunction extends IReadable {
	/**
	 * Evaluates the function on the given domain element
	 * 
	 * @TrackedGetter This method will notify UpdateTracker that the reciever has been read from
	 * 
	 * @param input domain element to evaluate the function on
	 * @return result of the function 
	 */
    public abstract Object calculate(Object input);
}
