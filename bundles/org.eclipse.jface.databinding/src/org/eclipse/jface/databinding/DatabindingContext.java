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

package org.eclipse.jface.databinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.databinding.internal.Binding;
import org.eclipse.jface.databinding.internal.CollectionBinding;
import org.eclipse.jface.databinding.internal.NestedUpdatableCollection;
import org.eclipse.jface.databinding.internal.NestedUpdatableValue;
import org.eclipse.jface.databinding.internal.ValueBinding;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class DatabindingContext implements IValidationContext {

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

	private List createdUpdatables = new ArrayList();

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
		registerFactories();
		registerConverters();
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
	 * @return registered converter, Identity (if create Identity is true)
	 * @throws BindingException
	 *             if no converter is found
	 * 
	 */
	public IConverter getConverter(Class fromType, Class toType)
			throws BindingException {
		if (fromType == toType) {
			return new IdentityConverter(fromType, toType);
		}
		IConverter converter = (IConverter) converters.get(new Pair(fromType,
				toType));
		if (converter == null)
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
	private IConverter getStringToDoubleConverter() {
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
	private IConverter getToStringConverter(final Class fromClass) {
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

	protected void registerFactories() {
		addUpdatableFactory2(new IUpdatableFactory2() {
			public IUpdatable createUpdatable(Map properties,
					Object description, IValidationContext validationContext)
					throws BindingException {
				if (description instanceof NestedPropertyDescription) {
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
		if (targetUpdatable instanceof IUpdatableValue) {
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
			IUpdatable result = factory.createUpdatable(properties,
					description, this);
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
		// TODO: consider the fact that adding new factories for a given
		// description
		// may hide default ones (e.g., a new PropertyDescriptor may overide the
		// ond for EMF)
		factories2.add(updatableFactory);
	}

}
