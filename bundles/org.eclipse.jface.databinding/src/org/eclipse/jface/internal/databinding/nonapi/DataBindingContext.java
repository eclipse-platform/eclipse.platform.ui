/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.nonapi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.internal.databinding.api.BindSpec;
import org.eclipse.jface.internal.databinding.api.BindingEvent;
import org.eclipse.jface.internal.databinding.api.BindingException;
import org.eclipse.jface.internal.databinding.api.IBindSpec;
import org.eclipse.jface.internal.databinding.api.IBinding;
import org.eclipse.jface.internal.databinding.api.IBindingListener;
import org.eclipse.jface.internal.databinding.api.IDataBindingContext;
import org.eclipse.jface.internal.databinding.api.conversion.IConverter;
import org.eclipse.jface.internal.databinding.api.factories.IBindSupportFactory;
import org.eclipse.jface.internal.databinding.api.factories.IObservableFactory;
import org.eclipse.jface.internal.databinding.api.observable.IObservable;
import org.eclipse.jface.internal.databinding.api.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.api.observable.list.ObservableList;
import org.eclipse.jface.internal.databinding.api.observable.list.WritableList;
import org.eclipse.jface.internal.databinding.api.observable.mapping.IObservableMultiMappingWithDomain;
import org.eclipse.jface.internal.databinding.api.observable.set.IObservableSetWithLabels;
import org.eclipse.jface.internal.databinding.api.observable.value.ComputedValue;
import org.eclipse.jface.internal.databinding.api.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.api.validation.IDomainValidator;
import org.eclipse.jface.internal.databinding.api.validation.IValidator;
import org.eclipse.jface.internal.databinding.api.validation.ValidationError;
import org.eclipse.jface.util.Assert;

/**
 * @since 1.0
 */
public class DataBindingContext implements IDataBindingContext {

	private List createdObservables = new ArrayList();

	private DataBindingContext parent;

	private List factories = new ArrayList();

	private List bindSupportFactories = new ArrayList();

	protected int validationTime;

	protected int updateTime;

	private WritableList bindings = new WritableList();

	private ObservableList validationErrors = new ValidationErrorList(bindings,
			false);

	private ObservableList partialValidationErrors = new ValidationErrorList(
			bindings, true);

	private ComputedValue validationError = new ComputedValue() {
		protected Object calculate() {
			int size = validationErrors.size();
			return size == 0 ? null : validationErrors.get(size - 1);
		}
	};

	private ComputedValue partialValidationError = new ComputedValue() {
		protected Object calculate() {
			int size = partialValidationErrors.size();
			return size == 0 ? null : partialValidationErrors.get(size - 1);
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.databinding.IDataBindingContext#addBindSupportFactory(org.eclipse.jface.databinding.IBindSupportFactory)
	 */
	public void addBindSupportFactory(IBindSupportFactory factory) {
		bindSupportFactories.add(factory);
	}

	/**
	 * 
	 */
	public DataBindingContext() {
	}

	/**
	 * @param parent
	 * 
	 */
	public DataBindingContext(DataBindingContext parent) {
		this.parent = parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.databinding.IDataBindingContext#dispose()
	 */
	public void dispose() {
		for (Iterator it = createdObservables.iterator(); it.hasNext();) {
			IObservable updatable = (IObservable) it.next();
			updatable.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.databinding.IDataBindingContext#bind(org.eclipse.jface.databinding.IObservable,
	 *      org.eclipse.jface.databinding.IObservable,
	 *      org.eclipse.jface.databinding.IBindSpec)
	 */
	public IBinding bind(IObservable targetObservable,
			IObservable modelObservable, IBindSpec bindSpec) {
		Binding binding;
		if (bindSpec == null) {
			bindSpec = new BindSpec(null, null, null, null);
		}
		if (targetObservable instanceof IObservableValue) {
			if (modelObservable instanceof IObservableValue) {
				IObservableValue target = (IObservableValue) targetObservable;
				IObservableValue model = (IObservableValue) modelObservable;
				fillBindSpecDefaults(bindSpec, target.getValueType(), model
						.getValueType());
				binding = new ValueBinding(this, target, model, bindSpec);
			} else {
				throw new BindingException(
						"incompatible updatables: target is value, model is " + modelObservable.getClass().getName()); //$NON-NLS-1$
			}
		} else if (targetObservable instanceof IObservableSetWithLabels) {
			if (modelObservable instanceof IObservableMultiMappingWithDomain) {
				IObservableSetWithLabels target = (IObservableSetWithLabels) targetObservable;
				IObservableMultiMappingWithDomain model = (IObservableMultiMappingWithDomain) modelObservable;
				fillBindSpecDefaults(bindSpec, target.getElementType(), model
						.getDomain().getElementType());
				binding = new TableBinding(this, target, model, bindSpec);
			} else {
				throw new BindingException(
						"incompatible updatables: target is observable set with labels, model is " + modelObservable.getClass().getName()); //$NON-NLS-1$
			}
		} else {
			throw new BindingException(
					"not yet implemented - target: " + targetObservable.getClass().getName() + ", model: " + modelObservable.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// DJO: Each binder is now responsible for adding its own change
		// listeners.
		// targetObservable.addChangeListener(binding);
		// modelObservable.addChangeListener(binding);
		binding.updateTargetFromModel();
		bindings.add(binding);
		return binding;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.databinding.IDataBindingContext#bind(java.lang.Object,
	 *      org.eclipse.jface.databinding.IObservable,
	 *      org.eclipse.jface.databinding.IBindSpec)
	 */
	public IBinding bind(Object targetDescription, IObservable modelObservable,
			IBindSpec bindSpec) {
		return bind(createObservable(targetDescription), modelObservable,
				bindSpec);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.databinding.IDataBindingContext#bind(org.eclipse.jface.databinding.IObservable,
	 *      java.lang.Object, org.eclipse.jface.databinding.IBindSpec)
	 */
	public IBinding bind(IObservable targetObservable, Object modelDescription,
			IBindSpec bindSpec) {
		if (bindSpec == null) {
			bindSpec = new BindSpec(null, null, null, null);
		}
		Object fromType = null;
		if (targetObservable instanceof IObservableValue) {
			fromType = ((IObservableValue) targetObservable).getValueType();
			// } else if (targetObservable instanceof IObservableCollection) {
			// fromType = ((IObservableCollection) targetObservable)
			// .getElementType();
		}
		fillBindSpecDefaults(bindSpec, fromType, null);
		return bind(targetObservable, createObservable(modelDescription),
				bindSpec);
	}

	protected void fillBindSpecDefaults(IBindSpec bindSpec, Object fromType,
			Object toType) {
		if (bindSpec.getTypeConversionValidator() == null) {
			((BindSpec) bindSpec)
					.setValidator(createValidator(fromType, toType));
		}
		if (bindSpec.getDomainValidator() == null) {
			((BindSpec) bindSpec)
					.setDomainValidator(createDomainValidator(toType));
		}
		if (bindSpec.getModelToTargetConverter() == null) {
			((BindSpec) bindSpec).setModelToTargetConverter(createConverter(
					fromType, toType));
		}
		if (bindSpec.getTargetToModelConverter() == null) {
			((BindSpec) bindSpec).setTargetToModelConverter(createConverter(
					toType, fromType));
		}
	}

	public IValidator createValidator(Object fromType, Object toType) {
		for (int i = bindSupportFactories.size() - 1; i >= 0; i--) {
			IBindSupportFactory bindSupportFactory = (IBindSupportFactory) bindSupportFactories
					.get(i);
			IValidator validator = bindSupportFactory.createValidator(fromType,
					toType);
			if (validator != null) {
				return validator;
			}
		}
		if (parent != null) {
			return parent.createValidator(fromType, toType);
		}
		return null;
	}

	public IDomainValidator createDomainValidator(Object modelType) {
		for (int i = bindSupportFactories.size() - 1; i >= 0; i--) {
			IBindSupportFactory bindSupportFactory = (IBindSupportFactory) bindSupportFactories
					.get(i);
			IDomainValidator validator = bindSupportFactory
					.createDomainValidator(modelType);
			if (validator != null) {
				return validator;
			}
		}
		if (parent != null) {
			return parent.createDomainValidator(modelType);
		}
		return null;
	}

	public IConverter createConverter(Object fromType, Object toType) {
		for (int i = bindSupportFactories.size() - 1; i >= 0; i--) {
			IBindSupportFactory bindSupportFactory = (IBindSupportFactory) bindSupportFactories
					.get(i);
			IConverter converter = bindSupportFactory.createConverter(fromType,
					toType);
			if (converter != null) {
				return converter;
			}
		}
		if (parent != null) {
			return parent.createConverter(fromType, toType);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.databinding.IDataBindingContext#bind(java.lang.Object,
	 *      java.lang.Object, org.eclipse.jface.databinding.IBindSpec)
	 */
	public IBinding bind(Object targetDescription, Object modelDescription,
			IBindSpec bindSpec) {
		return bind(createObservable(targetDescription), modelDescription,
				bindSpec);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.databinding.IDataBindingContext#createObservable(java.lang.Object)
	 */
	public IObservable createObservable(Object description) {
		IObservable updatable = doCreateObservable(description, this);
		if (updatable != null) {
			createdObservables.add(updatable);
		}
		return updatable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.databinding.IDataBindingContext#registerForDispose(org.eclipse.jface.databinding.IObservable)
	 */
	public void registerForDispose(IObservable updatable) {
		createdObservables.add(updatable);
	}

	protected IObservable doCreateObservable(Object description,
			DataBindingContext thisDatabindingContext) {
		for (int i = factories.size() - 1; i >= 0; i--) {
			IObservableFactory factory = (IObservableFactory) factories.get(i);
			IObservable result = factory.createObservable(
					thisDatabindingContext, description);
			if (result != null) {
				return result;
			}
		}
		if (parent != null) {
			return parent.doCreateObservable(description,
					thisDatabindingContext);
		}
		throw new BindingException("could not find updatable for " //$NON-NLS-1$
				+ description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.databinding.IDataBindingContext#addObservableFactory(org.eclipse.jface.databinding.IObservableFactory)
	 */
	public void addObservableFactory(IObservableFactory observableFactory) {
		// TODO: consider the fact that adding new factories for a given
		// description
		// may hide default ones (e.g., a new PropertyDescriptor may overide the
		// ond for EMF)
		factories.add(observableFactory);
	}

	public void updateTargets() {
		Assert.isTrue(false, "updateTargets is not yet implemented"); //$NON-NLS-1$
	}

	public void updateModels() {
		Assert.isTrue(false, "updateModels is not yet implemented"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.databinding.IDataBindingContext#addBindingEventListener(org.eclipse.jface.databinding.IBindingListener)
	 */
	public void addBindingEventListener(IBindingListener listener) {
		bindingEventListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.databinding.IDataBindingContext#removeBindingEventListener(org.eclipse.jface.databinding.IBindingListener)
	 */
	public void removeBindingEventListener(IBindingListener listener) {
		bindingEventListeners.remove(listener);
	}

	private List bindingEventListeners = new ArrayList();

	protected ValidationError fireBindingEvent(BindingEvent event) {
		ValidationError result = null;
		for (Iterator bindingEventIter = bindingEventListeners.iterator(); bindingEventIter
				.hasNext();) {
			IBindingListener listener = (IBindingListener) bindingEventIter
					.next();
			result = listener.bindingEvent(event);
			if (result != null)
				break;
		}
		return result;
	}

	/**
	 * @param fromType
	 * @param toType
	 * @return whether fromType is assignable to toType
	 */
	public boolean isAssignableFromTo(Object fromType, Object toType) {
		for (int i = bindSupportFactories.size() - 1; i >= 0; i--) {
			IBindSupportFactory bindSupportFactory = (IBindSupportFactory) bindSupportFactories
					.get(i);
			Boolean result = bindSupportFactory.isAssignableFromTo(fromType,
					toType);
			if (result != null) {
				return result.booleanValue();
			}
		}
		if (parent != null) {
			return parent.isAssignableFromTo(fromType, toType);
		}
		// TODO does this default make sense?
		return true;
	}

	public IObservableList getBindings() {
		return bindings;
	}

	public IObservableValue getPartialValidationError() {
		return partialValidationError;
	}

	public IObservableList getValidationErrors() {
		return validationErrors;
	}

	public IObservableValue getValidationError() {
		return validationError;
	}

}
