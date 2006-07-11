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
package org.eclipse.jface.internal.databinding.provisional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.internal.databinding.internal.ValidationErrorList;
import org.eclipse.jface.internal.databinding.provisional.conversion.IConverter;
import org.eclipse.jface.internal.databinding.provisional.factories.BindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.IBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.IObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.provisional.observable.list.ObservableList;
import org.eclipse.jface.internal.databinding.provisional.observable.list.WritableList;
import org.eclipse.jface.internal.databinding.provisional.observable.value.ComputedValue;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.provisional.validation.IDomainValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.IValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;
import org.eclipse.jface.util.Assert;

/**
 * A context for binding observable objects with a shared lifecycle. The
 * factories registered with a data binding context determine how observable
 * objects are created from description objects, and which converters and
 * validators are used when no specific converter or validator is given.
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 1.0
 * 
 */
public final class DataBindingContext {

	/**
	 * Policy constant specifying that update or validation should occur
	 * automatically whenever a bound observable object generates a change
	 * event.
	 */
	public static final int POLICY_AUTOMATIC = 1;

	/**
	 * Policy constant specifying that update or validation should only occur
	 * when explicitly requested by calling {@link #updateModels() } or
	 * {@link #updateTargets() }.
	 */
	public static final int POLICY_EXPLICIT = 2;

	/**
	 * Constant specifiying that validation or update events from UI observables
	 * should be triggered early, typically on each keystroke.
	 */
	public static final int TIME_EARLY = 0;

	/**
	 * Constant specifiying that validation or update events from UI observables
	 * should be triggered late, typically on focus lost.
	 */
	public static final int TIME_LATE = 1;

	/**
	 * Returns a new data binding context with the given parent.
	 * 
	 * @param parent
	 * @return a data binding context
	 */
	public static DataBindingContext createContext(DataBindingContext parent) {
		DataBindingContext result = new DataBindingContext(parent);
		return result;
	}

	/**
	 * Returns a new data binding context on which the given factories have been
	 * registered using
	 * {@link DataBindingContext#addObservableFactory(IObservableFactory)}. The
	 * factories will be added in the order given.
	 * 
	 * @param observableFactories
	 * @return a data binding context
	 */
	public static DataBindingContext createContext(
			IObservableFactory[] observableFactories) {
		return createContext(observableFactories,
				new BindSupportFactory[] { new DefaultBindSupportFactory() },
				new IBindingFactory[] { new DefaultBindingFactory() });
	}

	/**
	 * Returns a new data binding context on which the given factories have been
	 * registered using
	 * {@link DataBindingContext#addObservableFactory(IObservableFactory)}. The
	 * factories will be added in the order given.
	 * 
	 * @param observableFactories
	 * @param bindSupportFactories
	 * @param bindingFactories
	 * @return a data binding context
	 */
	public static DataBindingContext createContext(
			IObservableFactory[] observableFactories,
			BindSupportFactory[] bindSupportFactories,
			IBindingFactory[] bindingFactories) {
		DataBindingContext result = new DataBindingContext();
		if (observableFactories != null)
			for (int i = 0; i < observableFactories.length; i++) {
				result.addObservableFactory(observableFactories[i]);
			}
		if (bindSupportFactories != null)
			for (int i = 0; i < bindSupportFactories.length; i++) {
				result.addBindSupportFactory(bindSupportFactories[i]);
			}
		if (bindingFactories != null)
			for (int i = 0; i < bindingFactories.length; i++) {
				result.addBindingFactory(bindingFactories[i]);
			}
		return result;
	}

	private List bindingEventListeners = new ArrayList();

	private WritableList bindings = new WritableList();

	private List bindSupportFactories = new ArrayList();

	private List bindingFactories = new ArrayList();

	private List createdObservables = new ArrayList();

	private List factories = new ArrayList();

	private DataBindingContext parent;

	private ComputedValue partialValidationError = new ComputedValue() {
		protected Object calculate() {
			int size = partialValidationErrors.size();
			return size == 0 ? null : partialValidationErrors.get(size - 1);
		}
	};

	private ObservableList partialValidationErrors = new ValidationErrorList(
			bindings, true);

	private ComputedValue validationError = new ComputedValue() {
		protected Object calculate() {
			int size = validationErrors.size();
			return size == 0 ? null : validationErrors.get(size - 1);
		}
	};

	private ObservableList validationErrors = new ValidationErrorList(bindings,
			false);

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

	/**
	 * Add a listener to the set of listeners that will be notified when an
	 * event occurs in the data flow pipeline that is managed by any binding
	 * created by this data binding context.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public void addBindingEventListener(IBindingListener listener) {
		bindingEventListeners.add(listener);
	}

	/**
	 * Adds a factory that can create converters and validators. The list of
	 * bind support factories is used for creating converters and validators
	 * when binding without specifying a converter or validator.
	 * 
	 * @param factory
	 *            the factory to add.
	 */
	public void addBindSupportFactory(BindSupportFactory factory) {
		bindSupportFactories.add(factory);
	}

	/**
	 * Adds a factory for creating observable objects from description objects
	 * to this context. The list of observable factories is used for creating
	 * observable objects when binding based on description objects.
	 * 
	 * @param observableFactory
	 */
	public void addObservableFactory(IObservableFactory observableFactory) {
		// TODO: consider the fact that adding new factories for a given
		// description
		// may hide default ones (e.g., a new PropertyDescriptor may overide the
		// ond for EMF)
		factories.add(observableFactory);
	}

	/**
	 * Adds the given factory to the list of binding factories.
	 * 
	 * @param factory
	 */
	public void addBindingFactory(IBindingFactory factory) {
		bindingFactories.add(factory);
	}

	/**
	 * Binds targetObservable and modelObservable using converter and validator
	 * as specified in bindSpec. If bindSpec is null, a default converter and
	 * validator is used.
	 * 
	 * @param targetObservable
	 * @param modelObservable
	 * @param bindSpec
	 *            the bind spec, or null.  Any bindSpec object must not be reused
	 *            or changed after it is passed to #bind.
	 * @return The Binding that manages this data flow
	 */
	public Binding bind(IObservable targetObservable,
			IObservable modelObservable, BindSpec bindSpec) {
		Binding result = doCreateBinding(targetObservable, modelObservable,
				bindSpec, this);
		if (result != null)
			return result;
		throw new BindingException(
				"No binding found for target: " + targetObservable.getClass().getName() + ", model: " + modelObservable.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Binding doCreateBinding(IObservable targetObservable,
			IObservable modelObservable, BindSpec bindSpec,
			DataBindingContext originatingContext) {
		for (int i = bindingFactories.size() - 1; i >= 0; i--) {
			IBindingFactory factory = (IBindingFactory) bindingFactories.get(i);
			Binding binding = factory.createBinding(originatingContext, targetObservable,
					modelObservable, bindSpec);
			if (binding != null) {
				bindings.add(binding);
				return binding;
			}
		}
		if (parent != null) {
			return parent.doCreateBinding(targetObservable, modelObservable,
					bindSpec, originatingContext);
		}
		return null;
	}

	/**
	 * Convenience method to bind targetObservable and
	 * createObservable(modelDescription).
	 * 
	 * @param targetObservable
	 * @param modelDescription
	 * @param bindSpec
	 *            the bind spec, or null.  Any bindSpec object must not be reused
	 *            or changed after it is passed to #bind.
	 * @return The Binding that manages this data flow
	 */
	public Binding bind(IObservable targetObservable, Object modelDescription,
			BindSpec bindSpec) {
		return bind(targetObservable, createObservable(modelDescription),
				bindSpec);
	}

	/**
	 * Convenience method to bind createObservable(targetDescription) and
	 * modelObservable.
	 * 
	 * @param targetDescription
	 * @param modelObservable
	 * @param bindSpec
	 *            the bind spec, or null.  Any bindSpec object must not be reused
	 *            or changed after it is passed to #bind.
	 * @return The Binding that manages this data flow
	 */
	public Binding bind(Object targetDescription, IObservable modelObservable,
			BindSpec bindSpec) {
		return bind(createObservable(targetDescription), modelObservable,
				bindSpec);
	}

	/**
	 * Convenience method to bind createObservable(targetDescription) and
	 * createObservable(modelDescription).
	 * 
	 * @param targetDescription
	 * @param modelDescription
	 * @param bindSpec
	 *            the bind spec, or null.  Any bindSpec object must not be reused
	 *            or changed after it is passed to #bind.
	 * @return The Binding that manages this data flow
	 */
	public Binding bind(Object targetDescription, Object modelDescription,
			BindSpec bindSpec) {
		return bind(createObservable(targetDescription), modelDescription,
				bindSpec);
	}

	/**
	 * Tries to create a converter that can convert from values of type
	 * fromType. Returns <code>null</code> if no converter could be created.
	 * Either toType or modelDescription can be <code>null</code>, but not
	 * both. The implementation of this method will iterate over the registered
	 * bind support factories in reverse order, passing the given arguments to
	 * {@link BindSupportFactory#createConverter(Object, Object)}. The first
	 * non-null converter will be returned.
	 * 
	 * @param fromType
	 * @param toType
	 * @return an IConverter, or <code>null</code> if unsuccessful
	 */
	public IConverter createConverter(Object fromType, Object toType) {
		for (int i = bindSupportFactories.size() - 1; i >= 0; i--) {
			BindSupportFactory bindSupportFactory = (BindSupportFactory) bindSupportFactories
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

	/**
	 * @param modelType
	 * @return an IValidator, or null if unsuccessful
	 */
	public IDomainValidator createDomainValidator(Object modelType) {
		for (int i = bindSupportFactories.size() - 1; i >= 0; i--) {
			BindSupportFactory bindSupportFactory = (BindSupportFactory) bindSupportFactories
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

	/**
	 * Creates an observable object from a description. Description objects are
	 * interpreted by implementors of IObservableFactory, the data binding
	 * framework does not impose any semantics on them.
	 * 
	 * @param description
	 * @return IObservable for the given description
	 */
	public IObservable createObservable(Object description) {
		IObservable observable = doCreateObservable(description, this);
		if (observable != null) {
			createdObservables.add(observable);
		}
		return observable;
	}

	/**
	 * Tries to create a validator that can validate values of type fromType.
	 * Returns <code>null</code> if no validator could be created. Either
	 * toType or modelDescription can be <code>null</code>, but not both. The
	 * implementation of this method will iterate over the registered bind
	 * support factories in reverse order, passing the given arguments to
	 * {@link BindSupportFactory#createValidator(Class, Class, Object)}. The
	 * first non-null validator will be returned.
	 * 
	 * @param fromType
	 * @param toType
	 * @param modelDescription
	 * @return an IValidator, or <code>null</code> if unsuccessful
	 */
	public IValidator createValidator(Object fromType, Object toType) {
		for (int i = bindSupportFactories.size() - 1; i >= 0; i--) {
			BindSupportFactory bindSupportFactory = (BindSupportFactory) bindSupportFactories
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

	/**
	 * Disposes of this data binding context and all observable objects created
	 * in this context.
	 */
	public void dispose() {
		for (Iterator it = createdObservables.iterator(); it.hasNext();) {
			IObservable observable = (IObservable) it.next();
			observable.dispose();
		}
	}

	private IObservable doCreateObservable(Object description,
			DataBindingContext thisDatabindingContext) {
		for (int i = factories.size() - 1; i >= 0; i--) {
			IObservableFactory factory = (IObservableFactory) factories.get(i);
			IObservable result = factory.createObservable(description);
			if (result != null) {
				return result;
			}
		}
		if (parent != null) {
			return parent.doCreateObservable(description,
					thisDatabindingContext);
		}
		throw new BindingException("could not find observable for " //$NON-NLS-1$
				+ description);
	}

	/**
	 * @param dataBindingContext
	 * @param bindSpec
	 * @param targetType
	 * @param modelType
	 */
	public void fillBindSpecDefaults(DataBindingContext dataBindingContext,
			BindSpec bindSpec, Object targetType, Object modelType) {
		if (bindSpec.getTypeConversionValidator() == null) {
			bindSpec.setValidator(
					dataBindingContext.createValidator(targetType, modelType));
		}
		if (bindSpec.getDomainValidator() == null) {
			bindSpec.setDomainValidator(
					dataBindingContext.createDomainValidator(modelType));
		}
		IConverter[] modelToTargetConverters = bindSpec.getModelToTargetConverters();
		if (modelToTargetConverters.length > 1) {
			for (int i = 0; i < modelToTargetConverters.length; i++) {
				if (modelToTargetConverters[i] == null) {
					modelToTargetConverters[i] = dataBindingContext.createConverter(modelType, targetType);
				}
			}
		} else {
			// There's code in setModelToTargetConverter() that converts the 0
			// element array that represents null to a 1 element array, so we'll
			// just call setMTTC() instead of manipulating the array directly
			if (bindSpec.getModelToTargetConverter() == null) {
				bindSpec.setModelToTargetConverter(
						dataBindingContext.createConverter(modelType, targetType));
			}
		}
		IConverter[] targetToModelConverters = bindSpec.getTargetToModelConverters();
		if (targetToModelConverters.length > 1) {
			for (int i = 0; i < targetToModelConverters.length; i++) {
				if (targetToModelConverters[i] == null) {
					targetToModelConverters[i] = dataBindingContext.createConverter(targetType, modelType);
				}
			}
		} else {
			// There's code in setTargetToModelConverter() that converts the 0
			// element array that represents null to a 1 element array, so we'll
			// just call setTTMC() instead of manipulating the array directly
			if (bindSpec.getTargetToModelConverter() == null) {
				bindSpec.setTargetToModelConverter(
						dataBindingContext.createConverter(targetType, modelType));
			}
		}
	}

	/* package */ValidationError fireBindingEvent(BindingEvent event) {
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
	 * Returns an observable list with elements of type Binding, ordered by
	 * creation time
	 * 
	 * @return the observable list containing all bindings
	 */
	public IObservableList getBindings() {
		return bindings;
	}

	/**
	 * Returns an observable value of type ValidationError, containing the most
	 * recent partial validation error
	 * 
	 * @return the validation error observable
	 */
	public IObservableValue getPartialValidationError() {
		return partialValidationError;
	}

	/**
	 * Returns an observable value of type ValidationError, containing the most
	 * recent full validation error, i.e. the last element of the list returned
	 * by getValidationErrors().
	 * 
	 * @return the validation observable
	 */
	public IObservableValue getValidationError() {
		return validationError;
	}

	/**
	 * Returns an observable list with elements of type ValidationError, ordered
	 * by the time of detection
	 * 
	 * @return the observable list containing all validation errors
	 */
	public IObservableList getValidationErrors() {
		return validationErrors;
	}

	/**
	 * @param fromType
	 * @param toType
	 * @return whether fromType is assignable to toType
	 */
	public boolean isAssignableFromTo(Object fromType, Object toType) {
		for (int i = bindSupportFactories.size() - 1; i >= 0; i--) {
			BindSupportFactory bindSupportFactory = (BindSupportFactory) bindSupportFactories
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

	/**
	 * Registers an IObservable with the data binding context so that it will be
	 * disposed when all other IObservables are disposed. This is only necessary
	 * for observables like SettableValue that are instantiated directly, rather
	 * being created by a data binding context to begin with.
	 * 
	 * @param observable
	 *            The IObservable to register.
	 */
	public void registerForDispose(IObservable observable) {
		createdObservables.add(observable);
	}

	/**
	 * Removes a listener from the set of listeners that will be notified when
	 * an event occurs in the data flow pipeline that is managed by any binding
	 * created by this data binding context.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	public void removeBindingEventListener(IBindingListener listener) {
		bindingEventListeners.remove(listener);
	}

	/**
	 * Updates all model observable objects to reflect the current state of the
	 * target observable objects.
	 * 
	 */
	public void updateModels() {
		Assert.isTrue(false, "updateModels is not yet implemented"); //$NON-NLS-1$
	}

	/**
	 * Updates all target observable objects to reflect the current state of the
	 * model observable objects.
	 * 
	 */
	public void updateTargets() {
		Assert.isTrue(false, "updateTargets is not yet implemented"); //$NON-NLS-1$
	}

}