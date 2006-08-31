/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional;

import org.eclipse.jface.databinding.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;

/**
 * The interface that represents a binding between a model and a target.
 * 
 * This interface is not intended to be implemented by clients.
 * 
 * @since 1.0
 * @deprecated use {@link org.eclipse.jface.databinding.Binding} instead
 */
public abstract class Binding extends org.eclipse.jface.databinding.Binding {

	/**
	 * @since 3.2
	 *
	 */
	private static class WrappingBindingListener implements
			org.eclipse.jface.databinding.IBindingListener {
		/**
		 * 
		 */
		private final IBindingListener listener;

		/**
		 * @param listener
		 */
		private WrappingBindingListener(IBindingListener listener) {
			this.listener = listener;
		}

		public ValidationError bindingEvent(org.eclipse.jface.databinding.BindingEvent e) {
			return listener.bindingEvent(new BindingEvent(e.model,e.target,e.diff,e.copyType,e.pipelinePosition));
		}

		public int hashCode() {
			return listener.hashCode();
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final WrappingBindingListener other = (WrappingBindingListener) obj;
			if (listener == null) {
				if (other.listener != null)
					return false;
			} else if (!listener.equals(other.listener))
				return false;
			return true;
		}
		
	}

	/**
	 * @param context
	 */
	public Binding(DataBindingContext context) {
		super(context);
	}

	/**
	 * Add a listener to the set of listeners that will be notified when an
	 * event occurs in the data flow pipeline that is managed by this Binding.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public void addBindingEventListener(final IBindingListener listener) {
		super.addBindingEventListener(new WrappingBindingListener(listener));
	}

	/**
	 * Removes a listener from the set of listeners that will be notified when
	 * an event occurs in the data flow pipeline that is managed by this
	 * Binding.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	public void removeBindingEventListener(IBindingListener listener) {
		super.removeBindingEventListener(new WrappingBindingListener(listener));
	}
	
}
