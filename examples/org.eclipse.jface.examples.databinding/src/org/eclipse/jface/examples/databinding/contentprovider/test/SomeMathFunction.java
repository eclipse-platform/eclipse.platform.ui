/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.contentprovider.test;

import java.util.Set;

import org.eclipse.jface.databinding.observable.set.IObservableSet;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.MappingDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.ObservableMappingWithDomain;

/**
 * Simple function that performs one of three operations on Doubles:
 * <ul>
 * <li>Multiply by two</li>
 * <li>Round to nearest integer</li>
 * <li>Do nothing</li>
 * </ul>
 * 
 * @since 1.0
 */
public class SomeMathFunction extends ObservableMappingWithDomain {

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
		final int[] indices = new int[] { 0 };
		fireMappingValueChange(new MappingDiff() {
			public Set getElements() {
				return getDomain();
			}

			public int[] getAffectedIndices() {
				return indices;
			}

			public Object[] getOldMappingValues(Object element, int[] indices) {
				return new Object[] { doComputeResult(element, oldOp) };
			}

			public Object[] getNewMappingValues(Object element, int[] indices) {
				return new Object[] { doComputeResult(element, operation) };
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

	public Object getValueType() {
		return Double.class;
	}

}
