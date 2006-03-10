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
import org.eclipse.jface.internal.databinding.api.observable.mapping.IObservableMultiMappingWithDomain;
import org.eclipse.jface.internal.databinding.api.observable.set.IObservableSetWithLabels;
import org.eclipse.jface.internal.databinding.api.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.nonapi.TableBinding;
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
				fillBindSpecDefaults(dataBindingContext, bindSpec, target
						.getValueType(), model.getValueType());
				binding = new ValueBinding(dataBindingContext, target, model,
						bindSpec);
				return binding;
			}
			throw new BindingException(
					"incompatible updatables: target is value, model is " + modelObservable.getClass().getName()); //$NON-NLS-1$
		} else if (targetObservable instanceof IObservableSetWithLabels) {
			if (modelObservable instanceof IObservableMultiMappingWithDomain) {
				IObservableSetWithLabels target = (IObservableSetWithLabels) targetObservable;
				IObservableMultiMappingWithDomain model = (IObservableMultiMappingWithDomain) modelObservable;
				fillBindSpecDefaults(dataBindingContext, bindSpec, target
						.getElementType(), model.getDomain().getElementType());
				binding = new TableBinding(dataBindingContext, target, model,
						bindSpec);
				return binding;
			}
			throw new BindingException(
					"incompatible updatables: target is observable set with labels, model is " + modelObservable.getClass().getName()); //$NON-NLS-1$
		}
		return null;
	}

	private void fillBindSpecDefaults(DataBindingContext dataBindingContext,
			BindSpec bindSpec, Object fromType, Object toType) {
		if (bindSpec.getTypeConversionValidator() == null) {
			bindSpec.setValidator(dataBindingContext.createValidator(fromType,
					toType));
		}
		if (bindSpec.getDomainValidator() == null) {
			bindSpec.setDomainValidator(dataBindingContext
					.createDomainValidator(toType));
		}
		if (bindSpec.getModelToTargetConverter() == null) {
			bindSpec.setModelToTargetConverter(dataBindingContext
					.createConverter(fromType, toType));
		}
		if (bindSpec.getTargetToModelConverter() == null) {
			bindSpec.setTargetToModelConverter(dataBindingContext
					.createConverter(toType, fromType));
		}
	}

}
