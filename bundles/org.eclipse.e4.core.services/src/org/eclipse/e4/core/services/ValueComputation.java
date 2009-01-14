/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.core.services;


public class ValueComputation extends Computation {
	Object cachedValue;
	Context context;
	String name;
	boolean valid;
	ComputedValue computedValue;
	Context originatingContext;
	
	public ValueComputation(Context context, Context originatingContext, String name,
			ComputedValue computedValue) {
		this.context = context;
		this.originatingContext = originatingContext;
		this.name = name;
		this.computedValue = computedValue;
	}

	final protected void doClear() {
		valid = false;
		cachedValue = null;
	}

	final protected void doHandleInvalid(Context context) {
		this.originatingContext.invalidate(this.name);
	}

	final Object get() {
		if (valid) {
			return cachedValue;
		}
		Computation oldComputation = (Computation) Context.currentComputation
				.get();
		Context.currentComputation.set(this);
		try {
			cachedValue = computedValue.compute(originatingContext);
			valid = true;
		} finally {
			Context.currentComputation.set(oldComputation);
		}
		startListening();
		return cachedValue;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("VC(");
		result.append(context);
		result.append(',');
		result.append(name);
		result.append(')');
		return result.toString();
	}
}