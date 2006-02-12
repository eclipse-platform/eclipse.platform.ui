/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import org.eclipse.jface.internal.databinding.api.IBindSupportFactory;
import org.eclipse.jface.internal.databinding.api.IBinding;
import org.eclipse.jface.internal.databinding.api.IBindingListener;
import org.eclipse.jface.internal.databinding.api.IDataBindingContext;
import org.eclipse.jface.internal.databinding.api.IObservableFactory;
import org.eclipse.jface.internal.databinding.api.conversion.IConverter;
import org.eclipse.jface.internal.databinding.api.observable.IObservable;
import org.eclipse.jface.internal.databinding.api.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.api.observable.value.WritableValue;
import org.eclipse.jface.internal.databinding.api.validation.IDomainValidator;
import org.eclipse.jface.internal.databinding.api.validation.IValidator;
import org.eclipse.jface.internal.databinding.api.validation.ValidationError;
import org.eclipse.jface.internal.databinding.api.validation.ValidatorRegistry;
import org.eclipse.jface.util.Assert;


/**
 * @since 3.2
 */
public class DataBindingContext implements IDataBindingContext {

	private List createdObservables = new ArrayList();

	private DataBindingContext parent;

	private List partialValidationMessages = new ArrayList();

	private List validationMessages = new ArrayList();

	private WritableValue partialValidationMessage = new WritableValue(""); //$NON-NLS-1$

	private WritableValue validationMessage = new WritableValue(""); //$NON-NLS-1$

	private WritableValue combinedValidationMessage = new WritableValue(""); //$NON-NLS-1$

	private List factories = new ArrayList();

	private List bindSupportFactories = new ArrayList();

	private ValidatorRegistry validatorRegistry;
	
	protected int validationTime;

	protected int updateTime;

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
		registerDefaultBindSupportFactory();
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
	 * @see org.eclipse.jface.databinding.IDataBindingContext#getCombinedValidationMessage()
	 */
	public IObservableValue getCombinedValidationMessage() {
		return combinedValidationMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.databinding.IDataBindingContext#getPartialValidationMessage()
	 */
	public IObservableValue getPartialValidationMessage() {
		return partialValidationMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.databinding.IDataBindingContext#getValidationMessage()
	 */
	public IObservableValue getValidationMessage() {
		return validationMessage;
	}

	protected void registerDefaultBindSupportFactory() {
		// Add the default bind support factory
		addBindSupportFactory(new IBindSupportFactory() {

			public IValidator createValidator(Object fromType, Object toType,
					Object modelDescription) {
				if (fromType == null || toType == null) {
					// System.err.println("FIXME: Boris, is this a bug? In
					// registerDefaultBindSupportFactory.addBindSupportFactory.createValidator:
					// fromType is null or toType is null!!!!!"); //$NON-NLS-1$
					// try {
					// throw new BindingException("Cannot create proper
					// IValidator."); //$NON-NLS-1$
					// } catch (BindingException e) {
					// e.printStackTrace();
					// System.err.println();
					// }
					return new IValidator() {

						public ValidationError isPartiallyValid(Object value) {
							return null;
						}

						public ValidationError isValid(Object value) {
							return null;
						}
					};
				}

				IValidator dataTypeValidator = findValidator(fromType, toType);
				if (dataTypeValidator == null) {
					throw new BindingException(
							"No IValidator is registered for conversions from " + fromType + " to " + toType); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return dataTypeValidator;
			}

			public IConverter createConverter(Object fromType, Object toType,
					Object modelDescription) {
				if (toType == null) {
					return null;
				}
				if (fromType == toType) {
					return new IdentityConverter(fromType, toType);
				}
				if (ConversionFunctionRegistry.canConvertPair(fromType, toType)) {
					return new FunctionalConverter(fromType, toType);
				}
				// FIXME: djo -- This doesn't always work in the case of object
				// types?
				if (isAssignableFromTo(fromType, toType) //toType.isAssignableFrom(fromType)
						|| isAssignableFromTo(toType, fromType)) {//fromType.isAssignableFrom(toType)) {
					return new IdentityConverter(fromType, toType);
				}
				return null;
			}

			public IDomainValidator createDomainValidator(Object modelType, Object modelDescription) {
				return new IDomainValidator() {
					public ValidationError isValid(Object value) {
						return null;
					}
				};
			}
		});
	}

	private void removeValidationListenerAndMessage(List listOfPairs,
			Object first) {
		for (int i = listOfPairs.size() - 1; i >= 0; i--) {
			Pair pair = (Pair) listOfPairs.get(i);
			if (pair.a.equals(first)) {
				listOfPairs.remove(i);
				return;
			}
		}
		return;
	}

	/**
	 * @param binding
	 * @param partialValidationErrorOrNull
	 */
	public void updatePartialValidationError(IBinding binding,
			ValidationError partialValidationErrorOrNull) {
		removeValidationListenerAndMessage(partialValidationMessages, binding);
		if (partialValidationErrorOrNull != null) {
			partialValidationMessages.add(new Pair(binding,
					partialValidationErrorOrNull));
		}
		updateValidationMessage(
				combinedValidationMessage,
				partialValidationMessages.size() > 0 ? partialValidationMessages
						: validationMessages);
		updateValidationMessage(partialValidationMessage,
				partialValidationMessages);
	}

	/**
	 * @param binding
	 * @param validationErrorOrNull
	 */
	public void updateValidationError(IBinding binding,
			ValidationError validationErrorOrNull) {
		removeValidationListenerAndMessage(validationMessages, binding);
		if (validationErrorOrNull != null) {
			validationMessages.add(new Pair(binding, validationErrorOrNull));
		}
		updateValidationMessage(
				combinedValidationMessage,
				partialValidationMessages.size() > 0 ? partialValidationMessages
						: validationMessages);
		updateValidationMessage(validationMessage, validationMessages);
	}

	private void updateValidationMessage(
			WritableValue validationSettableMessage, List listOfPairs) {
		if (listOfPairs.size() == 0) {
			validationSettableMessage.setValue(""); //$NON-NLS-1$
		} else {
			validationSettableMessage.setValue(((Pair) listOfPairs
					.get(listOfPairs.size() - 1)).b);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.databinding.IDataBindingContext#bind(org.eclipse.jface.databinding.IObservable,
	 *      org.eclipse.jface.databinding.IObservable,
	 *      org.eclipse.jface.databinding.IBindSpec)
	 */
	public IBinding bind(IObservable targetObservable, IObservable modelObservable,
			IBindSpec bindSpec) {
		Binding binding;
		if (bindSpec == null) {
			bindSpec = new BindSpec(null, null,null,null);
		}
		if (targetObservable instanceof IObservableValue) {
			if (modelObservable instanceof IObservableValue) {
				IObservableValue target = (IObservableValue) targetObservable;
				IObservableValue model = (IObservableValue) modelObservable;
				fillBindSpecDefaults(bindSpec, target.getValueType(), model
						.getValueType(), null);
				binding = new ValueBinding(this, target, model, bindSpec);
			} else {
				throw new BindingException(
						"incompatible updatables: target is value, model is " + modelObservable.getClass().getName()); //$NON-NLS-1$
			}
		} else {
			throw new BindingException("not yet implemented"); //$NON-NLS-1$
		}
		// DJO: Each binder is now responsible for adding its own change
		// listeners.
		// targetObservable.addChangeListener(binding);
		// modelObservable.addChangeListener(binding);
		binding.updateTargetFromModel();
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
		return bind(createObservable(targetDescription), modelObservable, bindSpec);
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
			bindSpec = new BindSpec(null, null,null,null);
		}
		Object fromType = null;
		if (targetObservable instanceof IObservableValue) {
			fromType = ((IObservableValue) targetObservable).getValueType();
//		} else if (targetObservable instanceof IObservableCollection) {
//			fromType = ((IObservableCollection) targetObservable)
//					.getElementType();
		}
		fillBindSpecDefaults(bindSpec, fromType, null, modelDescription);
		return bind(targetObservable, createObservable(modelDescription), bindSpec);
	}

	protected void fillBindSpecDefaults(IBindSpec bindSpec, Object fromType,
			Object toType, Object modelDescriptionOrNull) {
		if (bindSpec.getTypeConversionValidator() == null) {
			((BindSpec) bindSpec).setValidator(createValidator(fromType,
					toType, modelDescriptionOrNull));
		}
		if (bindSpec.getDomainValidator() == null) {
			((BindSpec) bindSpec).setDomainValidator(createDomainValidator(fromType, modelDescriptionOrNull)); // FIXME: Not sure which is the model type
		}
		if (bindSpec.getModelToTargetConverter() == null) {
			((BindSpec) bindSpec).setModelToTargetConverter(createConverter(fromType,
					toType, modelDescriptionOrNull));
		}
	}

	public IValidator createValidator(Object fromType, Object toType,
			Object modelDescription) {
		for (int i = bindSupportFactories.size() - 1; i >= 0; i--) {
			IBindSupportFactory bindSupportFactory = (IBindSupportFactory) bindSupportFactories
					.get(i);
			IValidator validator = bindSupportFactory.createValidator(fromType,
					toType, modelDescription);
			if (validator != null) {
				return validator;
			}
		}
		if (parent != null) {
			return parent.createValidator(fromType, toType, modelDescription);
		}
		return null;
	}

	public IDomainValidator createDomainValidator(Object modelType,	Object modelDescription) {
		for (int i = bindSupportFactories.size() - 1; i >= 0; i--) {
			IBindSupportFactory bindSupportFactory = (IBindSupportFactory) bindSupportFactories
					.get(i);
			IDomainValidator validator = bindSupportFactory.createDomainValidator(modelType,
					modelDescription);
			if (validator != null) {
				return validator;
			}
		}
		if (parent != null) {
			return parent.createDomainValidator(modelType, modelDescription);
		}
		return null;
	}

	public IConverter createConverter(Object fromType, Object toType,
			Object modelDescription) {
		for (int i = bindSupportFactories.size() - 1; i >= 0; i--) {
			IBindSupportFactory bindSupportFactory = (IBindSupportFactory) bindSupportFactories
					.get(i);
			IConverter converter = bindSupportFactory.createConverter(fromType,
					toType, modelDescription);
			if (converter != null) {
				return converter;
			}
		}
		if (parent != null) {
			return parent.createConverter(fromType, toType, modelDescription);
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
		return bind(createObservable(targetDescription), modelDescription, bindSpec);
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IDataBindingContext#registerForDispose(org.eclipse.jface.databinding.IObservable)
	 */
	public void registerForDispose(IObservable updatable) {
		createdObservables.add(updatable);
	}
	
	protected IObservable doCreateObservable(Object description,
			DataBindingContext thisDatabindingContext) {
		for (int i = factories.size() - 1; i >= 0; i--) {
			IObservableFactory factory = (IObservableFactory) factories.get(i);
			IObservable result = factory.createObservable(thisDatabindingContext, description);
			if (result != null) {
				return result;
			}
		}
		if (parent != null) {
			return parent
					.doCreateObservable(description, thisDatabindingContext);
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IDataBindingContext#addBindingEventListener(org.eclipse.jface.databinding.IBindingListener)
	 */
	public void addBindingEventListener(IBindingListener listener) {
		bindingEventListeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IDataBindingContext#removeBindingEventListener(org.eclipse.jface.databinding.IBindingListener)
	 */
	public void removeBindingEventListener(IBindingListener listener) {
		bindingEventListeners.remove(listener);
	}
	
	private List bindingEventListeners = new ArrayList();
	
	protected ValidationError fireBindingEvent(BindingEvent event) {
		ValidationError result = null;
		for (Iterator bindingEventIter = bindingEventListeners.iterator(); bindingEventIter.hasNext();) {
			IBindingListener listener = (IBindingListener) bindingEventIter.next();
			result = listener.bindingEvent(event);
			if (result != null)
				break;
		}
		return result;
	}

	public boolean isAssignableFromTo(Object fromType, Object toType) {
		return true;
	}
	
	private IValidator findValidator(Object fromType, Object toType) {
		ValidatorRegistry registry = getValidatorRegistry();
		IValidator result = null;
		if (registry != null) {
			result = registry.get(fromType, toType);
		}
		if (result == null) {
			return parent.findValidator(fromType, toType);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.api.IDataBindingContext#getValidatorRegistry()
	 */
	public ValidatorRegistry getValidatorRegistry() {
		return validatorRegistry;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.api.IDataBindingContext#setValidatorRegistry(org.eclipse.jface.internal.databinding.api.validation.ValidatorRegistry)
	 */
	public void setValidatorRegistry(ValidatorRegistry validatorRegistry) {
		this.validatorRegistry = validatorRegistry;
	}
}
