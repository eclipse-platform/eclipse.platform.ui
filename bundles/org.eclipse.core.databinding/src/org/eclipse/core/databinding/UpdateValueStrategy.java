/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding;

import java.util.Date;
import java.util.HashMap;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.internal.databinding.Pair;
import org.eclipse.core.internal.databinding.conversion.StringToDateConverter;
import org.eclipse.core.internal.databinding.validation.ObjectToPrimitiveValidator;
import org.eclipse.core.internal.databinding.validation.StringToByteValidator;
import org.eclipse.core.internal.databinding.validation.StringToDateValidator;
import org.eclipse.core.internal.databinding.validation.StringToDoubleValidator;
import org.eclipse.core.internal.databinding.validation.StringToFloatValidator;
import org.eclipse.core.internal.databinding.validation.StringToIntegerValidator;
import org.eclipse.core.internal.databinding.validation.StringToLongValidator;
import org.eclipse.core.internal.databinding.validation.StringToShortValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 3.3
 * 
 */
public class UpdateValueStrategy extends UpdateStrategy {

	/**
	 * Policy constant denoting that the source observable's state should not be
	 * tracked and that the destination observable's value should never be
	 * updated.
	 */
	public static int POLICY_NEVER = notInlined(1);

	/**
	 * Policy constant denoting that the source observable's state should not be
	 * tracked, but that validation, conversion and updating the destination
	 * observable's value should be performed when explicitly requested.
	 */
	public static int POLICY_ON_REQUEST = notInlined(2);

	/**
	 * Policy constant denoting that the source observable's state should be
	 * tracked, including validating changes except for
	 * {@link #validateBeforeSet(Object)}, but that the destination
	 * observable's value should only be updated on request.
	 */
	public static int POLICY_CONVERT = notInlined(4);

	/**
	 * Policy constant denoting that the source observable's state should be
	 * tracked, and that validation, conversion and updating the destination
	 * observable's value should be performed automaticlly on every change of
	 * the source observable value.
	 */
	public static int POLICY_UPDATE = notInlined(8);

	/**
	 * Helper method allowing API evolution of the above constant values. The
	 * compiler will not inline constant values into client code if values are
	 * "computed" using this helper.
	 * 
	 * @param i
	 *            an integer
	 * @return the same integer
	 */
	private static int notInlined(int i) {
		return i;
	}

	protected IValidator afterGetValidator;
	protected IValidator afterConvertValidator;
	protected IValidator beforeSetValidator;
	protected IConverter converter;

	private int updatePolicy;

	private static ValidatorRegistry validatorRegistry = new ValidatorRegistry();
	private static HashMap validatorsByConverter = new HashMap();

	protected boolean provideDefaults;

	/**
	 * <code>true</code> if we defaulted the converter
	 */
	private boolean defaultedConverter = false;

	/**
	 * Creates a new update value strategy for automatically updating the
	 * destination observable value whenever the source observable value
	 * changes. Default validators and a default converter will be provided. The
	 * defaults can be changed by calling one of the setter methods.
	 */
	public UpdateValueStrategy() {
		this(true, POLICY_UPDATE);
	}

	/**
	 * Creates a new update value strategy with a configurable update policy.
	 * Default validators and a default converter will be provided. The defaults
	 * can be changed by calling one of the setter methods.
	 * 
	 * @param updatePolicy
	 *            one of {@link #POLICY_NEVER}, {@link #POLICY_ON_REQUEST},
	 *            {@link #POLICY_CONVERT}, or {@link #POLICY_UPDATE}
	 */
	public UpdateValueStrategy(int updatePolicy) {
		this(true, updatePolicy);
	}

	/**
	 * Creates a new update value strategy with a configurable update policy.
	 * Default validators and a default converter will be provided if
	 * <code>provideDefaults</code> is <code>true</code>. The defaults can
	 * be changed by calling one of the setter methods.
	 * 
	 * @param provideDefaults
	 *            if <code>true</code>, default validators and a default
	 *            converter will be provided based on the observable value's
	 *            type.
	 * @param updatePolicy
	 *            one of {@link #POLICY_NEVER}, {@link #POLICY_ON_REQUEST},
	 *            {@link #POLICY_CONVERT}, or {@link #POLICY_UPDATE}
	 */
	public UpdateValueStrategy(boolean provideDefaults, int updatePolicy) {
		this.provideDefaults = provideDefaults;
		this.updatePolicy = updatePolicy;
	}

	/**
	 * @param value
	 * @return the converted value
	 */
	public Object convert(Object value) {
		return converter == null ? value : converter.convert(value);
	}

	/**
	 * Tries to create a validator that can validate values of type fromType.
	 * Returns <code>null</code> if no validator could be created. Either
	 * toType or modelDescription can be <code>null</code>, but not both.
	 * 
	 * @param fromType
	 * @param toType
	 * @return an IValidator, or <code>null</code> if unsuccessful
	 */
	protected IValidator createValidator(Object fromType, Object toType) {
		if (fromType == null || toType == null) {
			return new IValidator() {

				public IStatus validate(Object value) {
					return Status.OK_STATUS;
				}
			};
		}

		return findValidator(fromType, toType);
	}

	/**
	 * Fills out default values based upon the provided <code>source</code>
	 * and <code>destination</code>. If the strategy is to default values it
	 * will attempt to default a converter. If the converter can be defaulted an
	 * attempt is made to default the
	 * {@link #validateAfterGet(Object) after get validator}. If a validator
	 * cannot be defaulted it will be <code>null</code>.
	 * 
	 * @param source
	 * @param destination
	 */
	protected void fillDefaults(IObservableValue source,
			IObservableValue destination) {
		Object sourceType = source.getValueType();
		Object destinationType = destination.getValueType();
		if (provideDefaults && sourceType != null && destinationType != null) {
			if (converter == null) {
				IConverter converter = createConverter(sourceType,
						destinationType);
				defaultedConverter = (converter != null);
				setConverter(converter);
			}

			if (afterGetValidator == null) {
				afterGetValidator = createValidator(sourceType, destinationType);
			}
		}
		if (converter != null) {
			if (sourceType != null) {
				checkAssignable(converter.getFromType(), sourceType,
						"converter does not convert from type " + sourceType); //$NON-NLS-1$
			}
			if (destinationType != null) {
				checkAssignable(converter.getToType(), destinationType,
						"converter does not convert to type " + destinationType); //$NON-NLS-1$
			}
		}
	}

	private IValidator findValidator(Object fromType, Object toType) {
		IValidator result = null;

		// We only default the validator if we defaulted the converter since the
		// two are tightly coupled.
		if (defaultedConverter) {
			if (String.class.equals(fromType)) {
				result = (IValidator) validatorsByConverter.get(converter);

				if (result == null) {
					// TODO sring based lookup
					if (Integer.class.equals(toType)
							|| Integer.TYPE.equals(toType)) {
						result = new StringToIntegerValidator(converter);
					} else if (Long.class.equals(toType)
							|| Long.TYPE.equals(toType)) {
						result = new StringToLongValidator(converter);
					} else if (Float.class.equals(toType)
							|| Float.TYPE.equals(toType)) {
						result = new StringToFloatValidator(converter);
					} else if (Double.class.equals(toType)
							|| Double.TYPE.equals(toType)) {
						result = new StringToDoubleValidator(converter);
					} else if (Byte.class.equals(toType)
							|| Byte.TYPE.equals(toType)) {
						result = new StringToByteValidator(converter);
					} else if (Short.class.equals(toType)
							|| Short.TYPE.equals(toType)) {
						result = new StringToShortValidator(converter);
					} else if (Date.class.equals(toType)
							&& converter instanceof StringToDateConverter) {
						result = new StringToDateValidator(
								(StringToDateConverter) converter);
					}

					if (result != null) {
						validatorsByConverter.put(converter, result);
					}
				}
			}

			if (result == null) {
				// TODO string based lookup
				result = validatorRegistry.get(fromType, toType);
			}
		}

		return result;
	}

	/**
	 * @return the update policy
	 */
	public int getUpdatePolicy() {
		return updatePolicy;
	}

	/**
	 * @param validator
	 * @return the receiver, to enable method call chaining
	 */
	public UpdateValueStrategy setAfterConvertValidator(IValidator validator) {
		this.afterConvertValidator = validator;
		return this;
	}

	/**
	 * @param validator
	 * @return the receiver, to enable method call chaining
	 */
	public UpdateValueStrategy setAfterGetValidator(IValidator validator) {
		this.afterGetValidator = validator;
		return this;
	}

	/**
	 * @param validator
	 * @return the receiver, to enable method call chaining
	 */
	public UpdateStrategy setBeforeSetValidator(IValidator validator) {
		this.beforeSetValidator = validator;
		return this;
	}

	/**
	 * @param converter
	 * @return the receiver, to enable method call chaining
	 */
	public UpdateValueStrategy setConverter(IConverter converter) {
		this.converter = converter;
		return this;
	}

	/**
	 * @param value
	 * @return an ok status
	 */
	public IStatus validateAfterConvert(Object value) {
		return afterConvertValidator == null ? Status.OK_STATUS
				: afterConvertValidator.validate(value);
	}

	/**
	 * @param value
	 * @return an ok status
	 */
	public IStatus validateAfterGet(Object value) {
		return afterGetValidator == null ? Status.OK_STATUS : afterGetValidator
				.validate(value);
	}

	/**
	 * @param value
	 * @return an ok status
	 */
	public IStatus validateBeforeSet(Object value) {
		return beforeSetValidator == null ? Status.OK_STATUS
				: beforeSetValidator.validate(value);
	}
	
	/**
	 * Sets the current value of the given observable to the given value.
	 * Clients may extend but must call the super implementation.
	 * 
	 * @param observableValue
	 * @param value
	 * @return TODO
	 */
	protected IStatus doSet(IObservableValue observableValue, Object value) {
		try {
			observableValue.setValue(value);
		} catch (Exception ex) {
			return ValidationStatus.error(BindingMessages
					.getString("ValueBinding_ErrorWhileSettingValue"), //$NON-NLS-1$
					ex);
		}
		return Status.OK_STATUS;
	}

	private static class ValidatorRegistry {

		private HashMap validators = new HashMap();

		/**
		 * Adds the system-provided validators to the current validator
		 * registry. This is done automatically for the validator registry
		 * singleton.
		 */
		private ValidatorRegistry() {
			// Standalone validators here...
			associate(Integer.class, Integer.TYPE,
					new ObjectToPrimitiveValidator(Integer.TYPE));
			associate(Byte.class, Byte.TYPE, new ObjectToPrimitiveValidator(
					Byte.TYPE));
			associate(Short.class, Short.TYPE, new ObjectToPrimitiveValidator(
					Short.TYPE));
			associate(Long.class, Long.TYPE, new ObjectToPrimitiveValidator(
					Long.TYPE));
			associate(Float.class, Float.TYPE, new ObjectToPrimitiveValidator(
					Float.TYPE));
			associate(Double.class, Double.TYPE,
					new ObjectToPrimitiveValidator(Double.TYPE));
			associate(Boolean.class, Boolean.TYPE,
					new ObjectToPrimitiveValidator(Boolean.TYPE));

			associate(Object.class, Integer.TYPE,
					new ObjectToPrimitiveValidator(Integer.TYPE));
			associate(Object.class, Byte.TYPE, new ObjectToPrimitiveValidator(
					Byte.TYPE));
			associate(Object.class, Short.TYPE, new ObjectToPrimitiveValidator(
					Short.TYPE));
			associate(Object.class, Long.TYPE, new ObjectToPrimitiveValidator(
					Long.TYPE));
			associate(Object.class, Float.TYPE, new ObjectToPrimitiveValidator(
					Float.TYPE));
			associate(Object.class, Double.TYPE,
					new ObjectToPrimitiveValidator(Double.TYPE));
			associate(Object.class, Boolean.TYPE,
					new ObjectToPrimitiveValidator(Boolean.TYPE));
		}

		/**
		 * Associate a particular validator that can validate the conversion
		 * (fromClass, toClass)
		 * 
		 * @param fromClass
		 *            The Class to convert from
		 * @param toClass
		 *            The Class to convert to
		 * @param validator
		 *            The IValidator
		 */
		private void associate(Object fromClass, Object toClass,
				IValidator validator) {
			validators.put(new Pair(fromClass, toClass), validator);
		}

		/**
		 * Return an IValidator for a specific fromClass and toClass.
		 * 
		 * @param fromClass
		 *            The Class to convert from
		 * @param toClass
		 *            The Class to convert to
		 * @return An appropriate IValidator
		 */
		private IValidator get(Object fromClass, Object toClass) {
			IValidator result = (IValidator) validators.get(new Pair(fromClass,
					toClass));
			if (result != null)
				return result;
			if (fromClass != null && toClass != null && fromClass == toClass) {
				return new IValidator() {
					public IStatus validate(Object value) {
						return Status.OK_STATUS;
					}
				};
			}
			return new IValidator() {
				public IStatus validate(Object value) {
					return Status.OK_STATUS;
				}
			};
		}
	}

}
