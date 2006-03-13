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

package org.eclipse.jface.internal.databinding.provisional.viewers;

import org.eclipse.jface.internal.databinding.internal.viewers.MultiMappingAndListBinding;
import org.eclipse.jface.internal.databinding.internal.viewers.MultiMappingAndSetBinding;
import org.eclipse.jface.internal.databinding.provisional.BindSpec;
import org.eclipse.jface.internal.databinding.provisional.Binding;
import org.eclipse.jface.internal.databinding.provisional.BindingException;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.factories.IBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.IObservableMultiMappingWithDomain;
import org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet;

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
		// List-based
		if (targetObservable instanceof IObservableList
				&& targetObservable instanceof IObservableCollectionWithLabels) {
			if (modelObservable instanceof IObservableMultiMappingWithDomain) {
				IObservableMultiMappingWithDomain model = (IObservableMultiMappingWithDomain) modelObservable;
				if (model.getDomain() instanceof IObservableList) {
					IObservableList modelList = (IObservableList) model
							.getDomain();
					IObservableCollectionWithLabels target = (IObservableCollectionWithLabels) targetObservable;
					IObservableList targetList = (IObservableList) targetObservable;
					dataBindingContext.fillBindSpecDefaults(dataBindingContext,
							bindSpec, targetList.getElementType(), modelList
									.getElementType());
					MultiMappingAndListBinding binding = new MultiMappingAndListBinding(
							dataBindingContext, targetList, target, modelList,
							model, bindSpec);
					return binding;
				}
			}
			throw new BindingException(
					"incompatible updatables: target is observable list with labels, model is " + modelObservable.getClass().getName()); //$NON-NLS-1$
		} else if (targetObservable instanceof IObservableSet
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
					MultiMappingAndSetBinding binding = new MultiMappingAndSetBinding(
							dataBindingContext, targetSet, target, modelSet,
							model, bindSpec);
					return binding;
				}
			}
			throw new BindingException(
					"incompatible updatables: target is observable set with labels, model is " + modelObservable.getClass().getName()); //$NON-NLS-1$
		}
		return null;
	}

}
