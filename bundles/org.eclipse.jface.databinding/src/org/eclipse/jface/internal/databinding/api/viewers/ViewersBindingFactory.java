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

package org.eclipse.jface.internal.databinding.api.viewers;

import org.eclipse.jface.internal.databinding.api.BindSpec;
import org.eclipse.jface.internal.databinding.api.Binding;
import org.eclipse.jface.internal.databinding.api.BindingException;
import org.eclipse.jface.internal.databinding.api.DataBindingContext;
import org.eclipse.jface.internal.databinding.api.factories.IBindingFactory;
import org.eclipse.jface.internal.databinding.api.observable.IObservable;
import org.eclipse.jface.internal.databinding.api.observable.mapping.IObservableMultiMappingWithDomain;
import org.eclipse.jface.internal.databinding.api.observable.set.IObservableSet;
import org.eclipse.jface.internal.databinding.nonapi.viewers.MultiMappingAndSetBinding;

/**
 * @since 3.2
 * 
 */
public class ViewersBindingFactory implements IBindingFactory {

	public Binding createBinding(DataBindingContext dataBindingContext,
			IObservable targetObservable, IObservable modelObservable,
			BindSpec bindSpec) {
		if (bindSpec == null)
			bindSpec = new BindSpec(null, null, null, null);
		// Set-based
		if (targetObservable instanceof IObservableSet
				&& targetObservable instanceof IObservableCollectionWithLabels) {
			if (modelObservable instanceof IObservableMultiMappingWithDomain) {
				IObservableMultiMappingWithDomain model = (IObservableMultiMappingWithDomain) modelObservable;
				if (model.getDomain() instanceof IObservableSet) {
					IObservableSet modelSet = (IObservableSet) model
							.getDomain();
					IObservableCollectionWithLabels target = (IObservableCollectionWithLabels) targetObservable;
					IObservableSet targetSet = (IObservableSet) targetObservable;
					dataBindingContext.fillBindSpecDefaults(dataBindingContext,
							bindSpec, targetSet.getElementType(), modelSet
									.getElementType());
					MultiMappingAndSetBinding binding = new MultiMappingAndSetBinding(dataBindingContext,
							targetSet, target, modelSet, model, bindSpec);
					return binding;
				}
			}
			throw new BindingException(
					"incompatible updatables: target is observable set with labels, model is " + modelObservable.getClass().getName()); //$NON-NLS-1$
		}
		return null;
	}

}
