/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 159768
 ******************************************************************************/

package org.eclipse.core.databinding;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Data binding has three concerns, the target, the model, and the data flow
 * between the target and model. BindSpec contains values and settings that
 * influence how bindings manage this data flow between the target and the
 * model.
 * 
 * @since 1.0
 */
public class BindSpec {
	/**
	 * Policy constant specifying that update or validation should occur
	 * automatically whenever a bound observable object generates a change
	 * event.
	 */
	public static final Integer POLICY_AUTOMATIC = new Integer(1);

	/**
	 * Policy constant specifying that update or validation should only occur
	 * when explicitly requested by calling
	 * {@link Binding#updateModelFromTarget()} or
	 * {@link Binding#updateTargetFromModel()}.
	 */
	public static final Integer POLICY_EXPLICIT = new Integer(2);

	/**
	 * Empty validator array to represent no validators.
	 */
	private static final IValidator[] NO_VALIDATORS = new IValidator[0];

	/*
	 * Default validator implementation that always returns an OK status.
	 */
	static class DefaultValidator implements IValidator {
		public IStatus validate(Object value) {
			return Status.OK_STATUS;
		}
	}

	/*
	 * Default converter implementation, does not perform any conversion.
	 */
	static class DefaultConverter implements IConverter {

		private final Object toType;

		private final Object fromType;

		/**
		 * @param fromType
		 * @param toType
		 */
		DefaultConverter(Object fromType, Object toType) {
			this.toType = toType;
			this.fromType = fromType;
		}

		public Object convert(Object fromObject) {
			return fromObject;
		}

		public Object getFromType() {
			return fromType;
		}

		public Object getToType() {
			return toType;
		}
	}

	private IConverter modelToTargetConverter;

	private Integer modelUpdatePolicy;

	private IConverter targetToModelConverter;

	private Integer targetUpdatePolicy;

	private boolean updateModel = true;

	private boolean updateTarget = true;

	private Map targetValidatorsContainers;

	private Map modelValidatorsContainers;

	private Integer targetValidatePosition;

	/**
	 * Default constructor.  Does not set any values.
	 */
	public BindSpec() {
	}

	/**
	 * Returns the converter to be used, or <code>null</code> if a default
	 * converter should be used.
	 * 
	 * @return the converter, or <code>null</code>
	 */
	public IConverter getModelToTargetConverter() {
		return modelToTargetConverter;
	}

	/**
	 * Returns the update policy to be used for updating the model when the
	 * target has changed
	 * 
	 * @return the update policy, or <code>null</code> if unspecified
	 * 
	 * @see BindSpec#POLICY_AUTOMATIC
	 * @see BindSpec#POLICY_EXPLICIT
	 */
	public Integer getModelUpdatePolicy() {
		return modelUpdatePolicy;
	}

	/**
	 * Returns the converter to be used, or <code>null</code> if a default
	 * converter should be used.
	 * 
	 * @return the converter, or <code>null</code>
	 */
	public IConverter getTargetToModelConverter() {
		return targetToModelConverter;
	}

	/**
	 * Returns the update policy to be used for updating the target when the
	 * model has changed
	 * 
	 * @return the update policy, or <code>null</code> if unspecified
	 * 
	 * @see BindSpec#POLICY_AUTOMATIC
	 * @see BindSpec#POLICY_EXPLICIT
	 */
	public Integer getTargetUpdatePolicy() {
		return targetUpdatePolicy;
	}

	/**
	 * @return true if the model should be updated by the binding
	 */
	public boolean isUpdateModel() {
		return updateModel;
	}

	/**
	 * @return true if the target should be updated by the binding
	 */
	public boolean isUpdateTarget() {
		return updateTarget;
	}

	/**
	 * Sets the model to target converter.
	 * 
	 * @param converter
	 *            <code>null</code> allowed and will remove all existing
	 *            converters.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setModelToTargetConverter(IConverter converter) {
		modelToTargetConverter = converter;
		return this;
	}

	/**
	 * @param modelUpdatePolicy
	 *            The modelUpdatePolicy to set.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setModelUpdatePolicy(Integer modelUpdatePolicy) {
		this.modelUpdatePolicy = modelUpdatePolicy;
		return this;
	}

	/**
	 * Sets the target to model converter.
	 * 
	 * @param converter
	 *            <code>null</code> allowed and will remove all existing
	 *            converters.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setTargetToModelConverter(IConverter converter) {
		targetToModelConverter = converter;
		return this;
	}

	/**
	 * @param targetUpdatePolicy
	 *            The targetUpdatePolicy to set.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setTargetUpdatePolicy(Integer targetUpdatePolicy) {
		this.targetUpdatePolicy = targetUpdatePolicy;
		return this;
	}

	/**
	 * @param updateModel
	 *            The updateModel to set.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setUpdateModel(boolean updateModel) {
		this.updateModel = updateModel;
		return this;
	}

	/**
	 * @param updateTarget
	 *            The updateTarget to set.
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setUpdateTarget(boolean updateTarget) {
		this.updateTarget = updateTarget;
		return this;
	}

	/**
	 * Fills any values not explicitly set with defaults. This implementation of
	 * {@link #fillBindSpecDefaults(IObservableValue, IObservableValue)} creates
	 * validators that always return {@link Status#OK_STATUS}, and converters
	 * that perform no conversion.
	 * 
	 * @param target
	 * @param model
	 */
	protected void fillBindSpecDefaults(IObservable target, IObservable model) {
		if (getModelToTargetConverter() == null) {
			setModelToTargetConverter(new DefaultConverter(Object.class,
					Object.class));
		}
		if (getTargetToModelConverter() == null) {
			setTargetToModelConverter(new DefaultConverter(Object.class,
					Object.class));
		}

		for (Iterator it = BindingEvent.PIPELINE_CONSTANTS.keySet().iterator(); it
				.hasNext();) {
			Integer position = (Integer) it.next();
			addTargetValidator(position.intValue(), new DefaultValidator());
			addModelValidator(position.intValue(), new DefaultValidator());
		}
	}

	/**
	 * @return pipeline position to automatically validate up to on every target
	 *         change when {@link #getTargetUpdatePolicy()} is
	 *         {@link BindSpec#POLICY_EXPLICIT}, can be <code>null</code>
	 */
	public Integer getTargetValidatePolicy() {
		return targetValidatePosition;
	}

	/**
	 * Sets the position to validate to automatically on every target change
	 * when the {@link #getTargetUpdatePolicy()} is
	 * {@link BindSpec#POLICY_EXPLICIT}.
	 * 
	 * @param pipelinePosition
	 *            BindingEvent.PIPELINE_* constant, <code>null</code> for
	 *            default
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec setTargetValidatePolicy(Integer pipelinePosition) {
		this.targetValidatePosition = pipelinePosition;
		return this;
	}

	/**
	 * Registers a target validator to be run after the provided
	 * <code>pipelinePosition</code>. Multiple instances of the same
	 * validator can be registered for the same position.
	 * 
	 * @param pipelinePosition
	 *            BindingEvent.PIPELINE_* constant
	 * @param validator
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec addTargetValidator(int pipelinePosition,
			IValidator validator) {
		if (validator == null) {
			throw new IllegalArgumentException("Argument 'validator' was null"); //$NON-NLS-1$
		}

		if (targetValidatorsContainers == null) {
			targetValidatorsContainers = new HashMap();
		}

		addValidator(pipelinePosition, validator, targetValidatorsContainers);

		return this;
	}

	/**
	 * Retrieves the target validators to be executed at the provided
	 * <code>pipelinePosition</code>.
	 * 
	 * @param pipelinePosition
	 *            BindingEvent.PIPELINE_* constant
	 * @return validators, empty array if none are registered
	 */
	public IValidator[] getTargetValidators(int pipelinePosition) {
		return getValidators(pipelinePosition, targetValidatorsContainers);
	}

	/**
	 * Registers a model validator to be run after the provided
	 * <code>pipelinePosition</code>. Multiple instances of the same
	 * validator can be registered for the same position.
	 * 
	 * @param pipelinePosition
	 *            BindingEvent.PIPELINE_* constant
	 * @param validator
	 * @return this BindSpec, to enable chaining of method calls
	 */
	public BindSpec addModelValidator(int pipelinePosition, IValidator validator) {
		if (validator == null) {
			throw new IllegalArgumentException("Argument 'validator' was null"); //$NON-NLS-1$
		}

		if (modelValidatorsContainers == null) {
			modelValidatorsContainers = new HashMap();
		}

		addValidator(pipelinePosition, validator, modelValidatorsContainers);

		return this;
	}

	/**
	 * Retrieves the model validators to be executed at the provided
	 * <code>pipelinePosition</code>.
	 * 
	 * @param pipelinePosition
	 *            BindingEvent.PIPELINE_* constant
	 * @return validators, empty array if none are registered
	 */
	public IValidator[] getModelValidators(int pipelinePosition) {
		return getValidators(pipelinePosition, modelValidatorsContainers);
	}

	/**
	 * Registers a validator.
	 * 
	 * @param position
	 * @param validator
	 * @param validatorsContainers
	 */
	private static void addValidator(int position, IValidator validator,
			Map validatorsContainers) {
		Assert.isNotNull(validator);
		Assert.isNotNull(validatorsContainers);

		Integer positionInteger = new Integer(position);
		ValidatorsContainer container = null;
		container = (ValidatorsContainer) validatorsContainers
				.get(positionInteger);

		if (container == null) {
			validatorsContainers.put(positionInteger, new ValidatorsContainer(
					position, validator));
		} else {
			container.addValidator(validator);
		}
	}

	/**
	 * @param position
	 * @param validatorsContainers
	 *            can be <code>null</code>
	 * @return validators, empty array if none found
	 */
	private static IValidator[] getValidators(int position,
			Map validatorsContainers) {
		IValidator[] result = NO_VALIDATORS;

		if (validatorsContainers != null) {
			ValidatorsContainer container = (ValidatorsContainer) validatorsContainers
					.get(new Integer(position));

			if (container != null) {
				result = container.getValidators();
			}
		}

		return result;
	}

	/**
	 * Stores validators along with the pipeline position to validate.
	 * 
	 */
	private static class ValidatorsContainer {
		final int pipelinePosition;
		final LinkedList validators = new LinkedList();

		ValidatorsContainer(int pipelinePosition, IValidator validator) {
			this.pipelinePosition = pipelinePosition;
			addValidator(validator);
		}

		void addValidator(IValidator validator) {
			validators.add(validator);
		}

		IValidator[] getValidators() {
			IValidator[] result = NO_VALIDATORS;

			if (validators != null) {
				result = (IValidator[]) validators
						.toArray(new IValidator[validators.size()]);
			}

			return result;
		}
	}
}
