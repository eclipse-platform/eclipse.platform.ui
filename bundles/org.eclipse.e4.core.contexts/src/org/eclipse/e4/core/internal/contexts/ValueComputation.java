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

import java.util.List;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.EclipseContext.Scheduled;

public class ValueComputation extends Computation {
	Object cachedValue;
	IEclipseContext context;
	String name;
	boolean valid;
	IContextFunction function;
	EclipseContext originatingContext;
	private boolean computing; // cycle detection

	public ValueComputation(IEclipseContext context, IEclipseContext originatingContext, String name, IContextFunction computedValue) {
		this.context = context;
		this.originatingContext = (EclipseContext) originatingContext;
		this.name = name;
		this.function = computedValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime * result + ((function == null) ? 0 : function.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((originatingContext == null) ? 0 : originatingContext.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValueComputation other = (ValueComputation) obj;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		if (function == null) {
			if (other.function != null)
				return false;
		} else if (!function.equals(other.function))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (originatingContext == null) {
			if (other.originatingContext != null)
				return false;
		} else if (!originatingContext.equals(other.originatingContext))
			return false;
		return true;
	}

	static class CycleException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final String cycleMessage;

		CycleException(String cycleMessage) {
			super("Cycle while computing value"); //$NON-NLS-1$
			this.cycleMessage = cycleMessage;
		}

		String getCycleMessage() {
			return cycleMessage;
		}

		public String toString() {
			return "\n" + cycleMessage + '\n'; //$NON-NLS-1$
		}
	}

	final protected void doClear() {
		valid = false;
		cachedValue = null;
	}

	final protected void doHandleInvalid(ContextChangeEvent event, List<Scheduled> scheduled) {
		int eventType = event.getEventType();
		// if the originating context is being disposed, remove this value computation completely
		if (eventType == ContextChangeEvent.DISPOSE) {
			IEclipseContext eventsContext = event.getContext();
			if (originatingContext.equals(eventsContext)) {
				removeAll(originatingContext);
				return;
			}
			return;
		}
		// this.originatingContext.handleInvalid(this.name,
		originatingContext.invalidate(name, eventType == ContextChangeEvent.DISPOSE ? ContextChangeEvent.REMOVED : eventType, event.getOldValue(), scheduled);
	}

	final Object get(Object[] arguments) {
		if (valid) {
			return cachedValue;
		}
		if (this.computing) {
			throw new CycleException(this.toString());
		}
		Computation oldComputation = EclipseContext.currentComputation.get();
		EclipseContext.currentComputation.set(this);
		computing = true;
		try {
			cachedValue = function.compute(originatingContext, arguments);
			valid = true;
		} catch (CycleException ex) {
			throw new CycleException(ex.getCycleMessage() + '\n' + this.toString());
		} finally {
			computing = false;
			EclipseContext.currentComputation.set(oldComputation);
		}
		startListening(originatingContext);
		return cachedValue;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("ValueComputation("); //$NON-NLS-1$
		result.append(context);
		result.append('/');
		result.append(originatingContext);
		result.append(',');
		result.append(name);
		result.append(')');
		return result.toString();
	}
}