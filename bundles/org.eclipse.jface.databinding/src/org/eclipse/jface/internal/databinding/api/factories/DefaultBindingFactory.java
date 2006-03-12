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

package org.eclipse.jface.internal.databinding.api.factories;

import org.eclipse.jface.internal.databinding.api.BindSpec;
import org.eclipse.jface.internal.databinding.api.Binding;
import org.eclipse.jface.internal.databinding.api.BindingException;
import org.eclipse.jface.internal.databinding.api.DataBindingContext;
import org.eclipse.jface.internal.databinding.api.observable.IObservable;
import org.eclipse.jface.internal.databinding.api.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.api.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.nonapi.ListBinding;
import org.eclipse.jface.internal.databinding.nonapi.ValueBinding;

/**
 * @since 3.2
 * 
 */
public class DefaultBindingFactory implements IBindingFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.internal.databinding.api.IBindingFactory#createBinding(org.eclipse.jface.internal.databinding.api.observable.IObservable,
	 *      org.eclipse.jface.internal.databinding.api.observable.IObservable,
	 *      org.eclipse.jface.internal.databinding.api.BindSpec)
	 */
	public Binding createBinding(DataBindingContext dataBindingContext,
			IObservable targetObservable, IObservable modelObservable,
			BindSpec bindSpec) {
		Binding binding;
		if (bindSpec == null) {
			bindSpec = new BindSpec(null, null, null, null);
		}
		if (targetObservable instanceof IObservableValue) {
			if (modelObservable instanceof IObservableValue) {
				IObservableValue target = (IObservableValue) targetObservable;
				IObservableValue model = (IObservableValue) modelObservable;
				dataBindingContext.fillBindSpecDefaults(dataBindingContext,
						bindSpec, target.getValueType(), model.getValueType());
				binding = new ValueBinding(dataBindingContext, target, model,
						bindSpec);
				return binding;
			}
			throw new BindingException(
					"incompatible observables: target is value, model is " + modelObservable.getClass().getName()); //$NON-NLS-1$
		} else if (targetObservable instanceof IObservableList) {
			if (modelObservable instanceof IObservableList) {
				IObservableList target = (IObservableList) targetObservable;
				IObservableList model = (IObservableList) modelObservable;
				dataBindingContext.fillBindSpecDefaults(dataBindingContext,
						bindSpec, target.getElementType(), model
								.getElementType());
				binding = new ListBinding(dataBindingContext, target, model,
						bindSpec);
				return binding;
			}
			throw new BindingException(
					"incompatible observable: target is list, model is " + modelObservable.getClass().getName()); //$NON-NLS-1$
		}
		return null;
	}

}
