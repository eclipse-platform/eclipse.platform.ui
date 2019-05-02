/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts;

import java.util.Objects;
import java.util.Set;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.internal.contexts.EclipseContext.Scheduled;

public class ValueComputation extends Computation {

	final static private Object NotAValue = new Object();

	final private IContextFunction function;
	final private EclipseContext originatingContext;
	final private String name;

	private Object cachedValue = NotAValue;
	private boolean computing; // cycle detection
	private boolean valid = true;

	public ValueComputation(String name, IEclipseContext originatingContext, IContextFunction computedValue) {
		this.originatingContext = (EclipseContext) originatingContext;
		this.function = computedValue;
		this.name = name;
		init();
	}

	@Override
	public void handleInvalid(ContextChangeEvent event, Set<Scheduled> scheduled) {
		if (cachedValue == NotAValue) // already invalidated
			return;
		cachedValue = NotAValue;
		originatingContext.invalidate(name, ContextChangeEvent.RECALC, event.getOldValue(), IInjector.NOT_A_VALUE, scheduled);
	}

	public boolean shouldRemove(ContextChangeEvent event) {
		int eventType = event.getEventType();
		boolean containerDisposed = (eventType == ContextChangeEvent.DISPOSE && event.getContext() == originatingContext);
		boolean definitionChanged = (eventType != ContextChangeEvent.RECALC && name.equals(event.getName()));
		return (containerDisposed || definitionChanged);
	}

	public Object get() {
		if (cachedValue != NotAValue)
			return cachedValue;
		if (this.computing)
			throw new RuntimeException("Cycle while computing value " + this.toString()); //$NON-NLS-1$

		originatingContext.pushComputation(this);
		computing = true;
		try {
			cachedValue = function.compute(originatingContext, name);
		} finally {
			computing = false;
			originatingContext.popComputation(this);
		}
		return cachedValue;
	}

	@Override
	public String toString() {
		if (function == null)
			return super.toString();
		return function.toString();
	}

	@Override
	protected int calcHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(function);
		result = prime * result + Objects.hashCode(name);
		result = prime * result + Objects.hashCode(originatingContext);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValueComputation other = (ValueComputation) obj;
		return Objects.equals(this.function, other.function) && Objects.equals(this.name, other.name)
				&& Objects.equals(this.originatingContext, other.originatingContext);
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	public void dipose() {
		valid = false;
	}
}