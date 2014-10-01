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

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.databinding.observable.map.ComputedObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.set.IObservableSet;

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
public class SomeMathFunction extends ComputedObservableMap {

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
		super(domain);
		init();
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
		fireMapChange(new MapDiff() {

			@Override
			public Set getAddedKeys() {
				return Collections.EMPTY_SET;
			}

			@Override
			public Set getChangedKeys() {
				return keySet();
			}

			@Override
			public Object getNewValue(Object key) {
				return doComputeResult(key, operation);
			}

			@Override
			public Object getOldValue(Object key) {
				return doComputeResult(key, oldOp);
			}

			@Override
			public Set getRemovedKeys() {
				return Collections.EMPTY_SET;
			}
		});
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

	@Override
	protected Object doGet(Object key) {
		return doComputeResult(key, this.op);
	}

	@Override
	protected Object doPut(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void hookListener(Object addedKey) {
		// ignore, no need to listen to immutable Double objects
	}

	@Override
	protected void unhookListener(Object removedKey) {
		// ignore, no need to listen to immutable Double objects
	}

}
