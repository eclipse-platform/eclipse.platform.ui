package org.eclipse.jface.examples.databinding.contentprovider.test;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IReadableSet;
import org.eclipse.jface.databinding.updatables.UpdatableFunction;

/**
 * Simple function that performs one of three operations on Doubles:
 * <ul>
 * <li>Multiply by two</li>
 * <li>Round to nearest integer</li>
 * <li>Do nothing</li>
 * </ul>
 * 
 * @since 3.2
 */
public class SomeMathFunction extends UpdatableFunction {

	public static final int OP_IDENTITY = 0;
	public static final int OP_MULTIPLY = 1;
	public static final int OP_ROUND = 2;
	
	private IReadableSet domain;
	private int op = OP_ROUND;
	
	public SomeMathFunction(IReadableSet domain) {
		this.domain = domain;
	}
	
	public void setOperation(int operation) {
		this.op = operation;
		
		// Fire a change event. Changing the operation is going to affect every answer returned by
		// this function, so include every element in the function domain in the event.
		// If this was a change that would only affect a subset of elements, we would include
		// the subset of affected elements rather than using domain.toCollection()
		fireChangeEvent(new ChangeEvent(this, ChangeEvent.FUNCTION_CHANGED, null, domain.toCollection()));
	}
	
	protected Object doComputeResult(Object input) {
		switch (op) {
		case OP_IDENTITY: 
			return input;
		case OP_MULTIPLY:
			return new Double((((Double)input).doubleValue() * 2.0));
		case OP_ROUND:
			return new Double(Math.floor((((Double)input).doubleValue())));
		}
		return input;
	}

}
