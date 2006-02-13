package org.eclipse.jface.examples.databinding.contentprovider.test;

import org.eclipse.jface.internal.databinding.api.observable.mapping.AbstractMappingDiff;
import org.eclipse.jface.internal.databinding.api.observable.mapping.ObservableMapping;
import org.eclipse.jface.internal.databinding.api.observable.set.IObservableSet;

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
public class SomeMathFunction extends ObservableMapping {

	/**
	 * 
	 */
	public static final int OP_IDENTITY = 0;

	/**
	 * 
	 */
	public static final int OP_MULTIPLY = 1;

	/**
	 * 
	 */
	public static final int OP_ROUND = 2;

	private int op = OP_ROUND;

	/**
	 * @param domain
	 */
	public SomeMathFunction(IObservableSet domain) {
		super();
		initDomain(domain);
	}

	/**
	 * @param operation
	 */
	public void setOperation(final int operation) {
		final int oldOp = this.op;
		this.op = operation;

		// Fire a change event. Changing the operation is going to affect every
		// answer returned by
		// this function, so include every element in the function domain in the
		// event.
		// If this was a change that would only affect a subset of elements, we
		// would include
		// the subset of affected elements rather than using
		// domain.toCollection()
		fireMappingValueChange(new AbstractMappingDiff(getDomain()) {

			public Object getOldMappingValue(Object element) {
				return doComputeResult(element, oldOp);
			}

			public Object getNewMappingValue(Object element) {
				return doComputeResult(element, operation);
			}
		});
	}

	protected Object doGetMappingValue(Object element) {
		return doComputeResult(element, this.op);
	}

	private Object doComputeResult(Object element, int op) {
		switch (op) {
		case OP_IDENTITY:
			return element;
		case OP_MULTIPLY:
			return new Double((((Double) element).doubleValue() * 2.0));
		case OP_ROUND:
			return new Double(Math.floor((((Double) element).doubleValue())));
		}
		return element;
	}

	protected void addListenerTo(Object domainElement) {
		// ignore, no need to listen to immutable Double objects
	}

	protected void removeListenerFrom(Object domainElement) {
		// ignore, no need to listen to immutable Double objects
	}

}
