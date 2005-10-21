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
package org.eclipse.jface.binding.internal;

import org.eclipse.jface.binding.DatabindingContext;
import org.eclipse.jface.binding.IChangeEvent;
import org.eclipse.jface.binding.IConverter;
import org.eclipse.jface.binding.IUpdatableValue;
import org.eclipse.jface.binding.IValidator;

/**
 * @since 3.2
 * 
 */
public class ValueBinding extends Binding {

	private final IUpdatableValue target;

	private final IUpdatableValue model;

	private final IValidator validator;

	private final IConverter converter;

	/**
	 * @param context
	 * @param target
	 * @param model
	 * @param converter
	 * @param validator
	 */
	public ValueBinding(DatabindingContext context, IUpdatableValue target,
			IUpdatableValue model, IConverter converter, IValidator validator) {
		super(context);
		this.target = target;
		this.model = model;
		this.converter = converter;
		this.validator = validator;
	}

	public void handleChange(IChangeEvent changeEvent) {
		if (changeEvent.getUpdatable() == target) {
			if (changeEvent.getChangeType() == IChangeEvent.VERIFY) {
				// we are notified of a pending change, do validation
				// and
				// veto the change if it is not valid
				Object value = changeEvent.getNewValue();
				String partialValidationError = validator
						.isPartiallyValid(value);
				context.updatePartialValidationError(this,
						partialValidationError);
				if (partialValidationError != null) {
					changeEvent.setVeto(true);
				}
			} else {
				// the target (usually a widget) has changed, validate
				// the
				// value and update the source
				updateModelFromTarget();
			}
		} else {
			updateTargetFromModel();
		}
	}

	/**
	 * This also does validation.
	 */
	public void updateModelFromTarget() {
		Object value = target.getValue();
		String validationError = doValidateTarget(value);
		if (validationError == null) {
			try {
				model.setValue(converter.convertTargetToModel(value), this);
			} catch (Exception ex) {
				context.updateValidationError(this,
						BindingMessages.getString("ValueBinding_ErrorWhileSettingValue")); //$NON-NLS-1$
			}
		}
	}

	/**
	 * 
	 */
	public void validateTarget() {
		Object value = target.getValue();
		doValidateTarget(value);
	}

	private String doValidateTarget(Object value) {
		String validationError = validator.isValid(value);
		context.updatePartialValidationError(this, null);
		context.updateValidationError(this, validationError);
		return validationError;
	}

	/**
	 * 
	 */
	public void updateTargetFromModel() {
		target.setValue(converter.convertModelToTarget(model.getValue()), this);
		validateTarget();
	}
}