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

package org.eclipse.jface.internal.databinding.provisional.factories;

import org.eclipse.jface.databinding.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.BindSpec;
import org.eclipse.jface.internal.databinding.provisional.Binding;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;

/**
 * @since 1.0
 * @deprecated no longer part of the API
 * 
 */
public interface IBindingFactory {

	/**
	 * Creates a new binding between the given observable objects, using
	 * additional information given in the bind spec. Returns null if this
	 * factory cannot create bindings between the given observables.
	 * @param dataBindingContext TODO
	 * @param target
	 * @param model
	 * @param bindSpec the bind spec, or null
	 * 
	 * @return a new binding, or null
	 */
	public Binding createBinding(DataBindingContext dataBindingContext, IObservable target,
			IObservable model, BindSpec bindSpec);

}
