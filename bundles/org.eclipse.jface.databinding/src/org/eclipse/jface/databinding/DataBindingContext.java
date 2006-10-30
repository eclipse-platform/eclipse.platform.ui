/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 159539
 *     Brad Reynolds - bug 140644
 *     Brad Reynolds - bug 159940
 *******************************************************************************/
package org.eclipse.jface.databinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.databinding.observable.Observables;
import org.eclipse.jface.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.observable.list.ObservableList;
import org.eclipse.jface.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.observable.value.ComputedValue;
import org.eclipse.jface.databinding.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.internal.ListBinding;
import org.eclipse.jface.internal.databinding.internal.ValidationErrorList;
import org.eclipse.jface.internal.databinding.internal.ValueBinding;
import org.eclipse.jface.internal.databinding.provisional.conversion.IConverter;
import org.eclipse.jface.internal.databinding.provisional.factories.BindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.validation.IDomainValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.IValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;

/**
 * A context for binding observable objects. This class is not intended to be
 * subclassed by clients.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 1.1
 * 
 */
public class DataBindingContext {

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

	private List bindingEventListeners = new ArrayList();

	private WritableList bindings = new WritableList();
    
    /**
     * Unmodifiable version of {@link #bindings} for exposure publicly.
     */
    private IObservableList unmodifiableBindings = Observables.unmodifiableObservableList(bindings);

	private List bindSupportFactories;

	protected DataBindingContext parent;

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

	private List childContexts = new ArrayList();

	/**
	 * Creates a data binding context set up with a
	 * {@link DefaultBindSupportFactory}, supplying converters and validators
	 * for common data types.
	 */
	public DataBindingContext() {
		this(null, new BindSupportFactory[]{new DefaultBindSupportFactory()});
	}

	/**
	 * @param parent 
	 *            may be null
	 * @param factories
	 *            an array of bind support factories that will be consulted in
	 *            the given order when creating converters and validators.
	 */
	public DataBindingContext(DataBindingContext parent,
			BindSupportFactory[] factories) {
		this.parent = parent;
		if (parent != null) {
			parent.addChild(this);
		}
		bindSupportFactories = new ArrayList(Arrays.asList(factories));
	}

	protected void addChild(DataBindingContext context) {
		childContexts.add(context);
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
	protected void addBindSupportFactory(BindSupportFactory factory) {
		bindSupportFactories.add(factory);
	}

	/**
	 * Binds two observable values using converter and validator as specified in
	 * bindSpec. If bindSpec is null, a default converter and validator is used.
	 * 
	 * @param targetObservableValue
	 * @param modelObservableValue
	 * @param bindSpec
	 *            the bind spec, or null. Any bindSpec object must not be reused
	 *            or changed after it is passed to #bind.
	 * @return The Binding that manages this data flow
	 */
	public Binding bindValue(IObservableValue targetObservableValue,
			IObservableValue modelObservableValue, BindSpec bindSpec) {
		if (bindSpec == null) {
			bindSpec = new BindSpec();
		}
		fillBindSpecDefaults(this, bindSpec, targetObservableValue
				.getValueType(), modelObservableValue.getValueType());
		Binding result = new ValueBinding(this, targetObservableValue,
				modelObservableValue, bindSpec);
        bindings.add(result);
		return result;
	}

	/**
	 * Binds two observable lists using converter and validator as specified in
	 * bindSpec. If bindSpec is null, a default converter and validator is used.
	 * 
	 * @param targetObservableList
	 * @param modelObservableList
	 * @param bindSpec
	 *            the bind spec, or null. Any bindSpec object must not be reused
	 *            or changed after it is passed to #bind.
	 * @return The Binding that manages this data flow
	 */
	public Binding bindList(IObservableList targetObservableList,
			IObservableList modelObservableList, BindSpec bindSpec) {
		if (bindSpec == null) {
			bindSpec = new BindSpec();
		}
		fillBindSpecDefaults(this, bindSpec, targetObservableList.getElementType(), modelObservableList.getElementType());
		Binding result = new ListBinding(this, targetObservableList,
				modelObservableList, bindSpec);
        bindings.add(result);
		return result;
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
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			binding.dispose();
		}
		for (Iterator it = childContexts.iterator(); it.hasNext();) {
			DataBindingContext context = (DataBindingContext) it.next();
			context.dispose();
		}
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
			bindSpec.setValidator(dataBindingContext.createValidator(
					targetType, modelType));
		}
		if (bindSpec.getDomainValidator() == null) {
			bindSpec.setDomainValidator(dataBindingContext
					.createDomainValidator(modelType));
		}
		IConverter[] modelToTargetConverters = bindSpec
				.getModelToTargetConverters();
		if (modelToTargetConverters.length > 1) {
			for (int i = 0; i < modelToTargetConverters.length; i++) {
				if (modelToTargetConverters[i] == null) {
					modelToTargetConverters[i] = dataBindingContext
							.createConverter(modelType, targetType);
				}
			}
		} else {
			// There's code in setModelToTargetConverter() that converts the 0
			// element array that represents null to a 1 element array, so we'll
			// just call setMTTC() instead of manipulating the array directly
			if (bindSpec.getModelToTargetConverter() == null) {
				bindSpec.setModelToTargetConverter(dataBindingContext
						.createConverter(modelType, targetType));
			}
		}
		IConverter[] targetToModelConverters = bindSpec
				.getTargetToModelConverters();
		if (targetToModelConverters.length > 1) {
			for (int i = 0; i < targetToModelConverters.length; i++) {
				if (targetToModelConverters[i] == null) {
					targetToModelConverters[i] = dataBindingContext
							.createConverter(targetType, modelType);
				}
			}
		} else {
			// There's code in setTargetToModelConverter() that converts the 0
			// element array that represents null to a 1 element array, so we'll
			// just call setTTMC() instead of manipulating the array directly
			if (bindSpec.getTargetToModelConverter() == null) {
				bindSpec.setTargetToModelConverter(dataBindingContext
						.createConverter(targetType, modelType));
			}
		}
	}

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
	 * Returns an unmodifiable observable list with elements of type Binding, ordered by
	 * creation time
	 * 
	 * @return the observable list containing all bindings
	 */
	public IObservableList getBindings() {
		return unmodifiableBindings;
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
	 * Adds the given binding to this data binding context.
	 * 
	 * @param binding
	 *            The binding to add.
	 */
	public void addBinding(Binding binding) {
		bindings.add(binding);
		binding.setDataBindingContext(this);
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
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			binding.updateModelFromTarget();
		}
	}

	/**
	 * Updates all target observable objects to reflect the current state of the
	 * model observable objects.
	 * 
	 */
	public void updateTargets() {
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			binding.updateTargetFromModel();
		}
	}
    
    /**
     * Removes the binding.
     * 
     * @param binding
     * @return <code>true</code> if was associated with the context,
     *         <code>false</code> if not
     */
    public boolean removeBinding(Binding binding) {
        if (bindings.contains(binding)) {
            binding.setDataBindingContext(null);
        }

        return bindings.remove(binding);
    }
}