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
package org.eclipse.e4.core.internal.contexts;

import java.util.Set;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.EclipseContext.Scheduled;

public class ValueComputation extends Computation {

	final static private Object NotAValue = new Object();

	private Object cachedValue = NotAValue;
	private IContextFunction function;
	private EclipseContext originatingContext;
	private boolean computing; // cycle detection
	private String name;

	public ValueComputation(String name, IEclipseContext originatingContext, IContextFunction computedValue) {
		this.originatingContext = (EclipseContext) originatingContext;
		this.function = computedValue;
		this.name = name;
	}

	public void handleInvalid(ContextChangeEvent event, Set<Scheduled> scheduled) {
		cachedValue = NotAValue;

		if (name.equals(event.getName()))
			invalidateComputation();

		int eventType = event.getEventType();
		originatingContext.invalidate(name, eventType == ContextChangeEvent.DISPOSE ? ContextChangeEvent.REMOVED : eventType, event.getOldValue(), scheduled);
	}

	public Object get() {
		if (!isValid())
			throw new IllegalArgumentException("Reusing invalidated computation"); //$NON-NLS-1$
		if (cachedValue != NotAValue)
			return cachedValue;
		if (this.computing)
			throw new RuntimeException("Cycle while computing value" + this.toString()); //$NON-NLS-1$

		originatingContext.pushComputation(this);
		computing = true;
		try {
			cachedValue = function.compute(originatingContext);
			validComputation = true;
		} finally {
			computing = false;
			originatingContext.popComputation(this);
		}
		return cachedValue;
	}

	public String toString() {
		if (function == null)
			return super.toString();
		return function.toString();
	}
}