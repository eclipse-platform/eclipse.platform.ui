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

package org.eclipse.e4.core.services.internal.context;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IComputedValue;


public class ValueComputation extends Computation {
	Object cachedValue;
	IEclipseContext context;
	String name;
	boolean valid;
	IComputedValue computedValue;
	EclipseContext originatingContext; // XXX IEclipseContext
	
	public ValueComputation(IEclipseContext context, IEclipseContext originatingContext, String name,
			IComputedValue computedValue) {
		this.context = context;
		this.originatingContext = (EclipseContext) originatingContext;
		this.name = name;
		this.computedValue = computedValue;
	}

	final protected void doClear() {
		valid = false;
		cachedValue = null;
	}

	final protected void doHandleInvalid(IEclipseContext context) {
		this.originatingContext.invalidate(this.name);
	}

	final Object get() {
		if (valid) {
			return cachedValue;
		}
		Computation oldComputation = (Computation) EclipseContext.currentComputation
				.get();  // XXX IEclipseContext
		EclipseContext.currentComputation.set(this);  // XXX IEclipseContext
		try {
			cachedValue = computedValue.compute(originatingContext);
			valid = true;
		} finally {
			EclipseContext.currentComputation.set(oldComputation);  // XXX IEclipseContext
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