/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.binding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.binding.internal.Binding;
import org.eclipse.jface.binding.internal.CollectionBinding;
import org.eclipse.jface.binding.internal.NestedUpdatableCollection;
import org.eclipse.jface.binding.internal.NestedUpdatableValue;
import org.eclipse.jface.binding.internal.TableBinding;
import org.eclipse.jface.binding.internal.ValueBinding;

/**
 * @since 3.2
 */
public class DatabindingContext {

	private static class Pair {

		private final Object a;

		private Object b;

		Pair(Object a, Object b) {
			this.a = a;
			this.b = b;
		}

		public boolean equals(Object obj) {
			if (obj.getClass() != Pair.class) {
				return false;
			}
			Pair other = (Pair) obj;
			return a.equals(other.a) && b.equals(other.b);
		}

		public int hashCode() {
			return a.hashCode() + b.hashCode();
		}
	}

	private Map converters = new HashMap();

	private Map updatableFactories = new HashMap(50);

	private Map interfacesLookup = new HashMap(50);

	private List createdUpdatables = new ArrayList();

	private boolean defaultIdentityConverter = true;

	private DatabindingContext parent;

	private List partialValidationMessages = new ArrayList();

	private List validationMessages = new ArrayList();

	private SettableValue partialValidationMessage = new SettableValue(
			String.class, ""); //$NON-NLS-1$

	private SettableValue validationMessage = new SettableValue(String.class,
			""); //$NON-NLS-1$

	private SettableValue combinedValidationMessage = new SettableValue(
			String.class, ""); //$NON-NLS-1$

	private List factories2 = new ArrayList();

	/**
	 * 
	 */
	public DatabindingContext() {
		this(null);
	}

	/**
	 * @param parent
	 */
	public DatabindingContext(DatabindingContext parent) {
		this.parent = parent;
		registerValueFactories();
		registerConverters();
	}

	/**
	 * @param clazz
	 * @param factory
	 */
	public void addUpdatableFactory(Class clazz, IUpdatableFactory factory) {
		updatableFactories.put(clazz, factory);
	}

	/**
	 * Convenience method for binding the given target to the given model value,
	 * using converters obtained by calling getConverter().
	 * 
	 * @param target
	 *            the target value
	 * @param model
	 *            the model value
	 * @throws BindingException
	 */
	public void bind(final IUpdatable target, final IUpdatable model)
			throws BindingException {
		IConverter converter = null;
		if (target instanceof IUpdatableValue)
			if (model instanceof IUpdatableValue) {
				IUpdatableValue tgt = (IUpdatableValue) target, mdl = (IUpdatableValue) model;
				converter = getConverter(tgt.getValueType(),
						mdl.getValueType(), isDefaultIdentityConverter());
			} else
				throw new BindingException(
						"Incompatible instances of IUpdatable"); //$NON-NLS-1$
		else if (target instanceof IUpdatableCollection)
			if (model instanceof IUpdatableCollection) {
				IUpdatableCollection tgt = (IUpdatableCollection) target, mdl = (IUpdatableCollection) model;
				converter = getConverter(tgt.getElementType(), mdl
						.getElementType(), isDefaultIdentityConverter());
			} else
				throw new BindingException(
						"Incompatible instances of IUpdatable"); //$NON-NLS-1$

		bind(target, model, converter);
	}

	/**
	 * Convenience method for binding the given target to the given model value,
	 * using the given converters to convert between target values and model
	 * values, and a validator obtained by calling
	 * getValidator(target.getValueType(), model.getValueType(),
	 * targetToModelValidator).
	 * 
	 * @param target
	 *            the target value
	 * @param model
	 *            the model value
	 * @param converter
	 *            the converter for converting from target values to model
	 *            values
	 * @throws BindingException
	 */
	public void bind(final IUpdatable target, final IUpdatable model,
			final IConverter converter) throws BindingException {
		final IValidator targetValidator = getValidator(converter);
		bind(target, model, converter, targetValidator);
	}

	/**
	 * @param target
	 * @param model
	 * @param converter
	 * @param targetValidator
	 * @throws BindingException
	 */
	public void bind(final IUpdatable target, final IUpdatable model,
			final IConverter converter, final IValidator targetValidator)
			throws BindingException {
		if (target instanceof IUpdatableValue)
			if (model instanceof IUpdatableValue) {
				IUpdatableValue tgt = (IUpdatableValue) target, mdl = (IUpdatableValue) model;
				bind(tgt, mdl, converter, targetValidator);
			} else
				throw new BindingException(
						"Incompatible instances of IUpdatable"); //$NON-NLS-1$
		else if (target instanceof IUpdatableCollection)
			if (model instanceof IUpdatableCollection) {
				IUpdatableCollection tgt = (IUpdatableCollection) target, mdl = (IUpdatableCollection) model;
				bind(tgt, mdl, converter, targetValidator);
			} else
				throw new BindingException(
						"Incompatible instances of IUpdatable"); //$NON-NLS-1$
	}

	/**
	 * 
	 * Binds two {@link org.eclipse.jface.binding.IUpdatableCollection}
	 * 
	 * @param targetCollection
	 * @param modelCollection
	 * @param converter
	 * @param validator
	 * @throws BindingException
	 * 
	 */
	public void bind(IUpdatableCollection targetCollection,
			IUpdatableCollection modelCollection, IConverter converter,
			final IValidator validator) throws BindingException {

		// TODO use a ValueBindings, and deal with validator

		// Verify element conversion types
		Class convertedClass = converter.getTargetType();
		if (!targetCollection.getElementType().isAssignableFrom(convertedClass)) {
			throw new BindingException("no converter from " //$NON-NLS-1$
					+ convertedClass.getName() + " to " //$NON-NLS-1$
					+ targetCollection.getElementType().getName());
		}
		convertedClass = converter.getModelType();
		if (!modelCollection.getElementType().isAssignableFrom(convertedClass)) {
			throw new BindingException("no converter from " //$NON-NLS-1$
					+ convertedClass.getName() + " to " //$NON-NLS-1$
					+ modelCollection.getElementType().getName());
		}

		CollectionBinding binding = new CollectionBinding(this,
				targetCollection, modelCollection, new BindSpec(converter,
						validator));
		targetCollection.addChangeListener(binding);
		modelCollection.addChangeListener(binding);
		binding.updateTargetFromModel();

	}

	/**
	 * Binds the given target to the given model value, using the given
	 * converters to convert between target values and model values, and the
	 * given validator to validate target values. First, the target value will
	 * be set to the current model value, using the modelToTargetConverter.
	 * Subsequently, whenever one of the values changes, the other value will be
	 * updated, using the matching converter. The model value will only be
	 * updated if the given validator successfully validates the current target
	 * value.
	 * 
	 * @param target
	 *            the target value
	 * @param model
	 *            the model value
	 * @param converter
	 *            the converter for converting from target values to model
	 *            values
	 * @param targetValidator
	 *            the validator for validating updated target values
	 * @throws BindingException
	 */
	public void bind(final IUpdatableValue target, final IUpdatableValue model,
			final IConverter converter, final IValidator targetValidator)
			throws BindingException {
		ValueBinding valueBinding = new ValueBinding(this, target, model,
				new BindSpec(converter, targetValidator));
		target.addChangeListener(valueBinding);
		model.addChangeListener(valueBinding);
		valueBinding.updateTargetFromModel();
	}

	/**
	 * Convenience method for binding the given target object's feature to the
	 * given model value. This method uses createUpdatableValue() to obtain the
	 * IUpdatableValue objects used for the binding.
	 * 
	 * @param targetObject
	 *            the target object
	 * @param targetFeature
	 *            the feature identifier for the target object
	 * @param modelValue
	 *            the model value
	 * @throws BindingException
	 */
	public void bind(Object targetObject, Object targetFeature,
			IUpdatable modelValue) throws BindingException {
		bind(createUpdatable(targetObject, targetFeature), modelValue);
	}

	/**
	 * Convenience method for binding the given target object's feature to the
	 * given model object's feature. This method uses createUpdatableValue() to
	 * obtain the IUpdatableValue objects used for the binding.
	 * 
	 * @param targetObject
	 *            the target object
	 * @param targetFeature
	 *            the feature identifier for the target object
	 * @param modelObject
	 *            the model object
	 * @param modelFeature
	 *            the feature identifier for the model object
	 * @throws BindingException
	 */
	public void bind(Object targetObject, Object targetFeature,
			Object modelObject, Object modelFeature) throws BindingException {
		bind(createUpdatable(targetObject, targetFeature), createUpdatable(
				modelObject, modelFeature));
	}

	/**
	 * Convenience method for binding the given target object's feature to the
	 * given model object's feature. This method uses createUpdatableValue() to
	 * obtain the IUpdatableValue objects used for the binding.
	 * 
	 * @param targetObject
	 *            the target object
	 * @param targetFeature
	 *            the feature identifier for the target object
	 * @param modelObject
	 *            the model object
	 * @param modelFeature
	 *            the feature identifier for the model object
	 * @param converter
	 *            the converter for converting from target values to model
	 *            values
	 * @throws BindingException
	 */
	public void bind(Object targetObject, Object targetFeature,
			Object modelObject, Object modelFeature, final IConverter converter)
			throws BindingException {
		bind(createUpdatable(targetObject, targetFeature), createUpdatable(
				modelObject, modelFeature), converter);
	}

	/**
	 * Convenience method for binding the given target object's feature to the
	 * given model object's feature. This method uses createUpdatableValue() to
	 * obtain the IUpdatableValue objects used for the binding.
	 * 
	 * @param targetObject
	 *            the target object
	 * @param targetFeature
	 *            the feature identifier for the target object
	 * @param modelObject
	 *            the model object
	 * @param modelFeature
	 *            the feature identifier for the model object
	 * @param converter
	 *            the converter for converting from target values to model
	 *            values
	 * @param validator
	 * @throws BindingException
	 */
	public void bind(Object targetObject, Object targetFeature,
			Object modelObject, Object modelFeature, IConverter converter,
			IValidator validator) throws BindingException {
		bind(createUpdatable(targetObject, targetFeature), createUpdatable(
				modelObject, modelFeature), converter, validator);
	}

	/**
	 * Creates an updatable value from the given object and feature ID. This
	 * method looks up a factory registered for the given object's type. If the
	 * given object is itself an IUpdatableValue, this method creates a derived
	 * updatable value. The returned instance will be disposed when this data
	 * binding context is disposed.
	 * 
	 * @param object
	 *            is the instance we need an updatable for
	 * @param featureID
	 *            is the property designated the updatable object.
	 * @return updatable for the given object
	 * @throws BindingException
	 */

	public IUpdatable createUpdatable(Object object, Object featureID)
			throws BindingException {
		if (object instanceof IUpdatableValue)
			return new NestedUpdatableValue(this, ((IUpdatableValue) object),
					featureID, null);
		else if (object instanceof IUpdatableCollection)
			throw new BindingException("TODO: need to implement this"); //$NON-NLS-1$ // TODO:

		IUpdatable result = doCreateUpdatable(object, featureID);

		if (result != null) {
			createdUpdatables.add(result);
		}

		return result;
	}

	/**
	 * 
	 */
	public void dispose() {
		for (Iterator it = createdUpdatables.iterator(); it.hasNext();) {
			IUpdatable updatable = (IUpdatable) it.next();
			updatable.dispose();
		}
	}

	/**
	 * @return the validation message updatable value
	 */
	public IUpdatableValue getCombinedValidationMessage() {
		return combinedValidationMessage;
	}

	/**
	 * Get a registered converter between teh fromType and the toType
	 * 
	 * @param fromType
	 * @param toType
	 * @param createIdentity
	 *            if set to true, and no registered converter is found, create
	 *            an Identity one
	 * @return registered converter, Identity (if create Identity is true)
	 * @throws BindingException
	 *             if no converter is found
	 * 
	 */
	public IConverter getConverter(Class fromType, Class toType,
			boolean createIdentity) throws BindingException {
		if (fromType == toType) {
			return new IdentityConverter(fromType, toType);
		}
		IConverter converter = (IConverter) converters.get(new Pair(fromType,
				toType));
		if (converter == null && createIdentity)
			converter = new IdentityConverter(fromType, toType);
		else
			throw new BindingException("no converter from " //$NON-NLS-1$
					+ fromType.getName() + " to " //$NON-NLS-1$
					+ toType.getName());

		return converter;
	}

	/**
	 * @return the validation updatable
	 */
	public IUpdatableValue getPartialValidationMessage() {
		return partialValidationMessage;
	}

	/**
	 * @return the converter
	 */
	public IConverter getStringToDoubleConverter() {
		IConverter doubleConverter = new IConverter() {

			public Object convertTargetToModel(Object object) {
				return new Double((String) object);
			}

			public Object convertModelToTarget(Object aDouble) {
				return aDouble.toString();
			}

			public Class getModelType() {
				return String.class;
			}

			public Class getTargetType() {
				return double.class;
			}
		};
		return doubleConverter;
	}

	/**
	 * @param fromClass
	 * @return the converter
	 */
	public IConverter getToStringConverter(final Class fromClass) {
		IConverter toStringConverter = new IConverter() {

			public Object convertTargetToModel(Object object) {
				return object.toString();
			}

			public Object convertModelToTarget(Object aString) {
				return aString;
			}

			public Class getModelType() {
				return fromClass;
			}

			public Class getTargetType() {
				return String.class;
			}
		};
		return toStringConverter;
	}

	/**
	 * @return the validation updatable
	 */
	public IUpdatableValue getValidationMessage() {
		return validationMessage;
	}

	/**
	 * @param converter
	 * @return the validator
	 */
	public IValidator getValidator(IConverter converter) {
		return new IValidator() {

			public String isPartiallyValid(Object value) {
				return null;
			}

			public String isValid(Object value) {
				return null;
			}
		};
	}

	/**
	 * @return true if ...
	 */
	public boolean isDefaultIdentityConverter() {
		// TODO BB asks: What is this used for? Nobody calls
		// setDefaultIdentityConverter...
		return defaultIdentityConverter;
	}

	private void getInterfacesOrder(Class[] interfaces, List order, Set seen) {
		List newBees = new ArrayList(interfaces.length);
		for (int i = 0; i < interfaces.length; i++)
			if (seen.add(interfaces[i])) {
				order.add(interfaces[i]);
				newBees.add(interfaces[i]);
			}

		for (Iterator it = newBees.iterator(); it.hasNext();)
			getInterfacesOrder(((Class) it.next()).getInterfaces(), order, seen);

	}

	private List getInterfacesOrder(Class clazz) {
		List interfaces = (List) interfacesLookup.get(clazz);
		if (interfaces != null)
			return interfaces;

		Class[] ifcs = clazz.getInterfaces();
		interfaces = new ArrayList(ifcs.length);
		getInterfacesOrder(ifcs, interfaces, new HashSet(4));
		interfacesLookup.put(clazz, interfaces);
		return interfaces;
	}

	/**
	 * 
	 * This returns an adaptable for the given object. Does not remember the
	 * created updatable for later disposal (this is done in createUpdatable()).
	 * 
	 * @param object
	 *            is the instance we need an updatable for
	 * @param featureID
	 *            is the property designated the updatable object.
	 * @return updatable for the given object
	 * @throws BindingException
	 * 
	 */
	protected IUpdatable doCreateUpdatable(Object object, Object featureID)
			throws BindingException {

		Class clazz = object.getClass();
		IUpdatableFactory factory = null;
		IUpdatable result = null;
		while (result == null && clazz != null) {
			factory = (IUpdatableFactory) updatableFactories.get(clazz);
			if (factory != null) {
				result = factory.createUpdatable(object, featureID);
			}
			if (result == null) {
				// Traverse the interfaces.
				for (Iterator iter = getInterfacesOrder(clazz).iterator(); iter
						.hasNext();) {
					factory = (IUpdatableFactory) updatableFactories.get(iter
							.next());
					if (factory != null) {
						result = factory.createUpdatable(object, featureID);
						if (result != null)
							break;
					}
				}
			}
			clazz = clazz.getSuperclass();
		}

		if (result != null) {
			return result;
		} else if (parent != null) {
			return parent.doCreateUpdatable(object, featureID);
		}
		throw new BindingException(
				"Couldn't create an updatable value for object=" + object //$NON-NLS-1$
						+ ", feature=" + featureID); //$NON-NLS-1$
	}

	protected void registerConverters() {
		IConverter doubleConverter = getStringToDoubleConverter();
		converters.put(new Pair(String.class, Double.class), doubleConverter);
		converters.put(new Pair(String.class, double.class), doubleConverter);
		IConverter integerConverter = new IConverter() {

			public Object convertTargetToModel(Object aString) {
				return new Integer((String) aString);
			}

			public Object convertModelToTarget(Object anInteger) {
				return anInteger.toString();
			}

			public Class getModelType() {
				return String.class;
			}

			public Class getTargetType() {
				return int.class;
			}
		};
		converters.put(new Pair(String.class, Integer.class), integerConverter);
		converters.put(new Pair(String.class, int.class), integerConverter);
		converters.put(new Pair(Double.class, String.class),
				getToStringConverter(Double.class));
		converters.put(new Pair(double.class, String.class),
				getToStringConverter(double.class));
		converters.put(new Pair(Object.class, String.class),
				getToStringConverter(Object.class));
		converters.put(new Pair(Integer.class, String.class),
				getToStringConverter(Object.class));
		converters.put(new Pair(Integer.class, int.class),
				new IdentityConverter(Integer.class, int.class));
		converters.put(new Pair(int.class, Integer.class),
				new IdentityConverter(int.class, Integer.class));
	}

	protected void registerValueFactories() {
		addUpdatableFactory2(new IUpdatableFactory2() {
			public IUpdatable createUpdatable(Map properties, Object description)
					throws BindingException {
				if (description instanceof PropertyDescription) {
					PropertyDescription propertyDescription = (PropertyDescription) description;
					// TODO this should be removed if/when we switch to bind2
					// completely
					return DatabindingContext.this.createUpdatable(
							propertyDescription.getObject(),
							propertyDescription.getPropertyID());
				} else if (description instanceof ListDescription) {
					// ListDescription was not handled already, turn it into a
					// TableDescription and try again
					ListDescription listDescription = (ListDescription) description;
					TableDescription tableDescription = new TableDescription(
							listDescription.getObject(),
							listDescription.getPropertyID(),
							new Object[] { listDescription.getLabelPropertyID() });
					return DatabindingContext.this
							.createUpdatable2(tableDescription);
				} else if (description instanceof NestedPropertyDescription) {
					NestedPropertyDescription propertyDescription = (NestedPropertyDescription) description;
					return new NestedUpdatableValue(DatabindingContext.this,
							propertyDescription.getUpdatableValue(),
							propertyDescription.getPropertyID(),
							propertyDescription.getPropertyType());
				} else if (description instanceof NestedCollectionDescription) {
					NestedCollectionDescription nestedCollectionDescription = (NestedCollectionDescription) description;
					return new NestedUpdatableCollection(
							DatabindingContext.this,
							nestedCollectionDescription.getUpdatableValue(),
							nestedCollectionDescription.getPropertyID(),
							nestedCollectionDescription
									.getPropertyElementType());
				}
				return null;
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
	 * @param defaultIdentityConverter
	 */
	public void setDefaultIdentityConverter(boolean defaultIdentityConverter) {
		this.defaultIdentityConverter = defaultIdentityConverter;
	}

	/**
	 * @param listener
	 * @param partialValidationErrorOrNull
	 */
	public void updatePartialValidationError(IChangeListener listener,
			String partialValidationErrorOrNull) {
		removeValidationListenerAndMessage(partialValidationMessages, listener);
		if (partialValidationErrorOrNull != null) {
			partialValidationMessages.add(new Pair(listener,
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
	 * @param listener
	 * @param validationErrorOrNull
	 */
	public void updateValidationError(IChangeListener listener,
			String validationErrorOrNull) {
		removeValidationListenerAndMessage(validationMessages, listener);
		if (validationErrorOrNull != null) {
			validationMessages.add(new Pair(listener, validationErrorOrNull));
		}
		updateValidationMessage(
				combinedValidationMessage,
				partialValidationMessages.size() > 0 ? partialValidationMessages
						: validationMessages);
		updateValidationMessage(validationMessage, validationMessages);
	}

	private void updateValidationMessage(
			SettableValue validationSettableMessage, List listOfPairs) {
		if (listOfPairs.size() == 0) {
			validationSettableMessage.setValue(""); //$NON-NLS-1$
		} else {
			validationSettableMessage.setValue(((Pair) listOfPairs
					.get(listOfPairs.size() - 1)).b);
		}
	}

	/**
	 * Binds targetUpdatable and modelUpdatable using converter and validator as
	 * specified in bindSpec. If bindSpec is null, a default converter and
	 * validator is used.
	 * 
	 * @param targetUpdatable
	 * @param modelUpdatable
	 * @param bindSpec
	 *            the bind spec, or null
	 * @throws BindingException
	 */
	public void bind2(IUpdatable targetUpdatable, IUpdatable modelUpdatable,
			IBindSpec bindSpec) throws BindingException {
		Binding binding;
		if (targetUpdatable instanceof IUpdatableTable) {
			if (modelUpdatable instanceof IUpdatableTable) {
				binding = new TableBinding(this,
						(IUpdatableTable) targetUpdatable,
						(IUpdatableTable) modelUpdatable,
						(ITableBindSpec) bindSpec);
			} else {
				throw new BindingException(
						"incompatible updatables (target is table, model is not)"); //$NON-NLS-1$
			}
		} else if (targetUpdatable instanceof IUpdatableValue) {
			if (modelUpdatable instanceof IUpdatableValue) {
				IUpdatableValue target = (IUpdatableValue) targetUpdatable;
				IUpdatableValue model = (IUpdatableValue) modelUpdatable;
				binding = new ValueBinding(this, target, model, bindSpec);
			} else {
				throw new BindingException(
						"incompatible updatables (target is value, model is not)"); //$NON-NLS-1$
			}
		} else if (targetUpdatable instanceof IUpdatableCollection) {
			if (modelUpdatable instanceof IUpdatableCollection) {
				IUpdatableCollection target = (IUpdatableCollection) targetUpdatable;
				IUpdatableCollection model = (IUpdatableCollection) modelUpdatable;
				binding = new CollectionBinding(this, target, model, bindSpec);
			} else {
				throw new BindingException(
						"incompatible updatables (target is value, model is not)"); //$NON-NLS-1$
			}
		} else {
			throw new BindingException("not yet implemented"); //$NON-NLS-1$
		}
		targetUpdatable.addChangeListener(binding);
		modelUpdatable.addChangeListener(binding);
		binding.updateTargetFromModel();
	}

	/**
	 * Convenience method to bind createUpdatable2(targetDescription) and
	 * modelUpdatable.
	 * 
	 * @param targetDescription
	 * @param modelUpdatable
	 * @param bindSpec
	 *            the bind spec, or null
	 * @throws BindingException
	 */
	public void bind2(Object targetDescription, IUpdatable modelUpdatable,
			IBindSpec bindSpec) throws BindingException {
		bind2(createUpdatable2(targetDescription), modelUpdatable, bindSpec);
	}

	/**
	 * Convenience method to bind targetUpdatable and
	 * createUpdatable2(modelDescription).
	 * 
	 * @param targetUpdatable
	 * @param modelDescription
	 * @param bindSpec
	 *            the bind spec, or null
	 * @throws BindingException
	 */
	public void bind2(IUpdatable targetUpdatable, Object modelDescription,
			IBindSpec bindSpec) throws BindingException {
		bind2(targetUpdatable, createUpdatable2(modelDescription), bindSpec);
	}

	/**
	 * Convenience method to bind createUpdatable2(targetDescription) and
	 * createUpdatable2(modelDescription).
	 * 
	 * @param targetDescription
	 * @param modelDescription
	 * @param bindSpec
	 *            the bind spec, or null
	 * @throws BindingException
	 */
	public void bind2(Object targetDescription, Object modelDescription,
			IBindSpec bindSpec) throws BindingException {
		bind2(createUpdatable2(targetDescription),
				createUpdatable2(modelDescription), bindSpec);
	}

	/**
	 * Convenience method to bind createUpdatable2(new
	 * PropertyDescription(targetObject, targetPropertyID)) and
	 * createUpdatable2(new PropertyDescription(modelObject, modelPropertyID))
	 * 
	 * @param targetObject
	 * @param targetPropertyID
	 * @param modelObject
	 * @param modelPropertyID
	 * @param bindSpec
	 *            the bind spec, or null
	 * @throws BindingException
	 */
	public void bind2(Object targetObject, Object targetPropertyID,
			Object modelObject, Object modelPropertyID, IBindSpec bindSpec)
			throws BindingException {
		bind2(createUpdatable2(new PropertyDescription(targetObject,
				targetPropertyID)), createUpdatable2(new PropertyDescription(
				modelObject, modelPropertyID)), bindSpec);
	}

	/**
	 * @param description
	 * @return IUpdatable for the given description
	 * @throws BindingException
	 */
	public IUpdatable createUpdatable2(Object description)
			throws BindingException {
		Map properties = new HashMap();
		collectProperties(properties);
		for (int i = factories2.size() - 1; i >= 0; i--) {
			IUpdatableFactory2 factory = (IUpdatableFactory2) factories2.get(i);
			IUpdatable result = factory
					.createUpdatable(properties, description);
			if (result != null) {
				return result;
			}
		}
		if (parent != null) {
			return parent.createUpdatable2(description);
		}
		throw new BindingException("could not find updatable for " //$NON-NLS-1$
				+ description);
	}

	protected void collectProperties(Map properties) {
		if (parent != null) {
			parent.collectProperties(properties);
		}
	}

	/**
	 * @param updatableFactory
	 */
	public void addUpdatableFactory2(IUpdatableFactory2 updatableFactory) {
		//TODO: consider the fact that adding new factories for a given description
		//      may hide default ones (e.g., a new PropertyDescriptor may overide the ond for EMF)
		factories2.add(updatableFactory);
	}

}
