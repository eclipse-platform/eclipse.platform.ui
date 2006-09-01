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
import org.eclipse.jface.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.internal.LazyListBinding;
import org.eclipse.jface.internal.databinding.internal.ListBinding;
import org.eclipse.jface.internal.databinding.internal.ValueBinding;
import org.eclipse.jface.internal.databinding.provisional.BindSpec;
import org.eclipse.jface.internal.databinding.provisional.Binding;
import org.eclipse.jface.internal.databinding.provisional.BindingException;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider;

/**
 * @since 3.2
 * @deprecated no longer part of the API
 * 
 */
public class DefaultBindingFactory implements IBindingFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.internal.databinding.provisional.IBindingFactory#createBinding(org.eclipse.jface.internal.databinding.provisional.observable.IObservable,
	 *      org.eclipse.jface.internal.databinding.provisional.observable.IObservable,
	 *      org.eclipse.jface.internal.databinding.provisional.BindSpec)
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
				
				return new ListBinding(dataBindingContext, target, model,
						bindSpec);
			}
			throw new BindingException(
					"incompatible observable: target is list, model is " + modelObservable.getClass().getName()); //$NON-NLS-1$
		}
		if (targetObservable instanceof ILazyDataRequestor
				&& modelObservable instanceof ILazyListElementProvider) {
			if (bindSpec == null) {
				bindSpec = new BindSpec();
			}
			return new LazyListBinding(dataBindingContext,
					(ILazyDataRequestor) targetObservable,
					(ILazyListElementProvider) modelObservable, bindSpec);
		}
		return null;
	}

}
