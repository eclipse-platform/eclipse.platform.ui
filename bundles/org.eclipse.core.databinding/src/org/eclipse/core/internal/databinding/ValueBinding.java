/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 152543, 159768
 *******************************************************************************/
package org.eclipse.core.internal.databinding;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.BindSpec;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.BindingEvent;
import org.eclipse.core.databinding.BindingException;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.IValueChangingListener;
import org.eclipse.core.databinding.observable.value.IVetoableValue;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.ValueChangingEvent;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 1.0
 * 
 */
public class ValueBinding extends Binding {
	private final IObservableValue target;

	private final IObservableValue model;

	private IConverter targetToModelConverter;

	private IConverter modelToTargetConverter;

	private boolean updatingTarget = false;
	private boolean updatingModel = false;

	private WritableValue partialValidationErrorObservable;

	private WritableValue validationErrorObservable;

	private IValueChangeListener targetChangeListener;

	private Map targetValidators = new HashMap();
	private Map modelValidators = new HashMap();

	private static final Integer[] VALIDATION_POSITIONS = new Integer[] {
			new Integer(BindingEvent.PIPELINE_VALUE_CHANGING),
			new Integer(BindingEvent.PIPELINE_AFTER_GET),
			new Integer(BindingEvent.PIPELINE_AFTER_CONVERT),
			new Integer(BindingEvent.PIPELINE_BEFORE_CHANGE) };

	/**
	 * @param target
	 * @param model
	 * @param bindSpec
	 */
	public ValueBinding(IObservableValue target,
			IObservableValue model, BindSpec bindSpec) {
		super(target, model);
		
		this.target = target;
		this.model = model;
		fillBindSpecDefaults(bindSpec, target, model);

		for (int i = 0; i < VALIDATION_POSITIONS.length; i++) {
			Integer position = VALIDATION_POSITIONS[i];

			targetValidators.put(position, bindSpec
					.getTargetValidators(position.intValue()));
			modelValidators.put(position, bindSpec.getModelValidators(position
					.intValue()));
		}

		if (bindSpec.isUpdateTarget()) {
			modelToTargetConverter = bindSpec.getModelToTargetConverter();
			if (modelToTargetConverter == null) {
				throw new BindingException(
						"Missing model to target converter from " + model.getValueType() + " to " + target.getValueType()); //$NON-NLS-1$ //$NON-NLS-2$
			}

			int pipelineStop = getValidationPolicy(bindSpec
					.getTargetUpdatePolicy(), bindSpec.getModelValidatePolicy());

			model
					.addValueChangeListener(modelChangeListener = new ModelChangeListener(
							pipelineStop));
		}
		if (bindSpec.isUpdateModel()) {
			targetToModelConverter = bindSpec.getTargetToModelConverter();
			if (targetToModelConverter == null) {
				throw new BindingException(
						"Missing target to model converter from " + target.getValueType() + " to " + model.getValueType()); //$NON-NLS-1$ //$NON-NLS-2$
			}

			int pipelineStop = getValidationPolicy(bindSpec
					.getModelUpdatePolicy(), bindSpec.getTargetValidatePolicy());

			target
					.addValueChangeListener(targetChangeListener = new TargetChangeListener(
							pipelineStop));

			if (target instanceof IVetoableValue) {
				((IVetoableValue) target)
						.addValueChangingListener(targetChangingListener);
			}
		}
		this.updateTarget = bindSpec.isUpdateTarget();
	}

	public void dispose() {
		target.removeValueChangeListener(targetChangeListener);
		if (target instanceof IVetoableValue) {
			((IVetoableValue) target)
					.removeValueChangingListener(targetChangingListener);
		}
		model.removeValueChangeListener(modelChangeListener);
		super.dispose();
	}

	private final IValueChangingListener targetChangingListener = new IValueChangingListener() {
		public void handleValueChanging(ValueChangingEvent event) {
			if (updatingTarget)
				return;
			// we are notified of a pending change, do validation
			// and veto the change if it is not valid
			Object value = event.diff.getNewValue();

			BindingEvent e = createBindingEvent(event.diff,
					BindingEvent.EVENT_COPY_TO_MODEL,
					BindingEvent.PIPELINE_VALUE_CHANGING);

			if (!performPosition(value, BindingEvent.PIPELINE_VALUE_CHANGING,
					e, BindingEvent.PIPELINE_AFTER_CHANGE)) {
				event.veto = true;
			}
		}
	};

	private class TargetChangeListener implements IValueChangeListener {
		private int pipelinePosition;

		TargetChangeListener(int pipelinePosition) {
			this.pipelinePosition = pipelinePosition;
		}

		public void handleValueChange(ValueChangeEvent event) {
			final ValueDiff diff = event.diff;
			if (updatingTarget)
				return;
			// the target (usually a widget) has changed, validate
			// the value and update the source
			model.getRealm().exec(new Runnable() {
				public void run() {
					doUpdateModelFromTarget(diff, pipelinePosition);
				}
			});
		}
	}

	private IValueChangeListener modelChangeListener;

	private class ModelChangeListener implements IValueChangeListener {
		private int pipelinePosition;

		ModelChangeListener(int pipelinePosition) {
			this.pipelinePosition = pipelinePosition;
		}

		public void handleValueChange(ValueChangeEvent event) {
			final ValueDiff diff = event.diff;
			if (updatingModel)
				return;
			// The model has changed so we must update the target
			model.getRealm().exec(new Runnable() {
				public void run() {
					doUpdateTargetFromModel(diff, pipelinePosition);
				}
			});
		}
	}

	private boolean updateTarget;

	/**
	 * Perform the target to model process up to and including the
	 * <code>lastPosition</code>.
	 * 
	 * @param diff
	 * @param lastPosition
	 *            BindingEvent.PIPELINE_* constant
	 */
	private void doUpdateModelFromTarget(ValueDiff diff, int lastPosition) {
		Assert.isTrue(model.getRealm().isCurrent());
		BindingEvent e = createBindingEvent(diff,
				BindingEvent.EVENT_COPY_TO_MODEL, BindingEvent.PIPELINE_AFTER_GET);
		e.originalValue = diff.getNewValue();
		if (!performPosition(e.originalValue, BindingEvent.PIPELINE_AFTER_GET,
				e, lastPosition)) {
			return;
		}

		try {
			updatingModel = true;

			e.convertedValue = targetToModelConverter.convert(e.originalValue);
			if (!performPosition(e.convertedValue,
					BindingEvent.PIPELINE_AFTER_CONVERT, e, lastPosition)) {
				return;
			}

			if (!performPosition(e.convertedValue,
					BindingEvent.PIPELINE_BEFORE_CHANGE, e, lastPosition)) {
				return;
			}

			model.setValue(e.convertedValue);
			performPosition(e.convertedValue,
					BindingEvent.PIPELINE_AFTER_CHANGE, e, lastPosition);
		} catch (Exception ex) {
			IStatus error = ValidationStatus.error(BindingMessages
					.getString("ValueBinding_ErrorWhileSettingValue"), //$NON-NLS-1$
					ex);
			validationErrorObservable.setValue(error);
		} finally {
			updatingModel = false;
		}
	}

	/**
	 * Performs the necessary processing for the position.
	 * 
	 * @param value
	 * @param position
	 * @param e
	 * @param lastPosition
	 * @return <code>true</code> if should proceed to the next position
	 */
	private boolean performPosition(Object value, int position, BindingEvent e,
			int lastPosition) {
		Map validatorMap = null;

		if (e.copyType == BindingEvent.EVENT_COPY_TO_MODEL) {
			validatorMap = targetValidators;
		} else if (e.copyType == BindingEvent.EVENT_COPY_TO_TARGET) {
			validatorMap = modelValidators;
		}

		IStatus status = Status.OK_STATUS;

		if (validatorMap != null) {
			IValidator[] validators = (IValidator[]) validatorMap
					.get(new Integer(position));
			if (validators != null) {
				for (int i = 0; status.isOK() && i < validators.length; i++) {
					status = validators[i].validate(value);
				}
			}
		}

		if (status.isOK()) {
			// only notify listeners if validation passed
			e.pipelinePosition = position;
			status = fireBindingEvent(e);
		}

		final IStatus finalStatus = status;

		if (position == BindingEvent.PIPELINE_VALUE_CHANGING) {
			partialValidationErrorObservable.getRealm().exec(new Runnable() {
				public void run() {
					partialValidationErrorObservable.setValue(finalStatus);
				}
			});
		} else {
			Assert.isTrue(partialValidationErrorObservable.getRealm().equals(
					validationErrorObservable.getRealm()));
			partialValidationErrorObservable.getRealm().exec(new Runnable() {
				public void run() {
					partialValidationErrorObservable.setValue(Status.OK_STATUS);
					validationErrorObservable.setValue(finalStatus);
				}
			});
		}

		return (status.isOK() && position != lastPosition);
	}

	/**
	 * Can be called from any thread/realm.
	 */
	public void updateTargetFromModel() {
		updateTargetFromModel(BindingEvent.PIPELINE_AFTER_CHANGE);
	}

	/**
	 * Perform the model to target process up to and including the
	 * <code>lastPosition</code>.
	 * 
	 * @param diff
	 * @param lastPosition
	 *            BindingEvent.PIPELINE_* constant
	 */
	private void doUpdateTargetFromModel(ValueDiff diff, int lastPosition) {
		Assert.isTrue(target.getRealm().isCurrent());
		try {
			updatingTarget = true;
			BindingEvent e = createBindingEvent(diff,
					BindingEvent.EVENT_COPY_TO_TARGET, BindingEvent.PIPELINE_AFTER_GET);
			e.originalValue = diff.getNewValue();
			if (!performPosition(e.originalValue,
					BindingEvent.PIPELINE_AFTER_GET, e, lastPosition)) {
				return;
			}

			e.convertedValue = modelToTargetConverter.convert(e.originalValue);
			if (!performPosition(e.convertedValue,
					BindingEvent.PIPELINE_AFTER_CONVERT, e, lastPosition)) {
				return;
			}

			if (!performPosition(e.convertedValue,
					BindingEvent.PIPELINE_BEFORE_CHANGE, e, lastPosition)) {
				return;
			}

			target.setValue(e.convertedValue);
			performPosition(e.convertedValue,
					BindingEvent.PIPELINE_AFTER_CHANGE, e, lastPosition);
		} catch (Exception ex) {
			final IStatus error = ValidationStatus.error(BindingMessages
					.getString("ValueBinding_ErrorWhileSettingValue"), //$NON-NLS-1$
					ex);
			validationErrorObservable.getRealm().exec(new Runnable() {
				public void run() {
					validationErrorObservable.setValue(error);
				}
			});
		} finally {
			updatingTarget = false;
		}
	}

	public IObservableValue getValidationStatus() {
		return validationErrorObservable;
	}

	public IObservableValue getPartialValidationStatus() {
		return partialValidationErrorObservable;
	}

	/**
	 * Can be called from any thread/realm.
	 */
	public void updateModelFromTarget() {
		updateModelFromTarget(BindingEvent.PIPELINE_AFTER_CHANGE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.databinding.Binding#performTarget(int)
	 */
	public void updateModelFromTarget(final int pipelinePosition) {
		target.getRealm().exec(new Runnable() {
			public void run() {
				final ValueDiff valueDiff = Diffs.createValueDiff(target
						.getValue(), target.getValue());
				model.getRealm().exec(new Runnable() {
					public void run() {
						doUpdateModelFromTarget(valueDiff, pipelinePosition);
					}
				});
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.databinding.Binding#performModelToTarget(int)
	 */
	public void updateTargetFromModel(final int pipelinePosition) {
		model.getRealm().exec(new Runnable() {
			public void run() {
				final ValueDiff valueDiff = Diffs.createValueDiff(null, model
						.getValue());
				target.getRealm().exec(new Runnable() {
					public void run() {
						doUpdateTargetFromModel(valueDiff, pipelinePosition);
					}
				});
			}
		});
	}

	protected void preInit() {
		validationErrorObservable = new WritableValue(context
				.getValidationRealm(), Status.OK_STATUS, IStatus.class);
		partialValidationErrorObservable = new WritableValue(context
				.getValidationRealm(), Status.OK_STATUS, IStatus.class);
	}

	protected void postInit() {
		if (updateTarget) {
			updateTargetFromModel();
		}
	}

	private static int getValidationPolicy(Integer updatePolicy,
			Integer validationPolicy) {
		int pipelineStop = BindingEvent.PIPELINE_AFTER_CHANGE;

		if (BindSpec.POLICY_EXPLICIT.equals(updatePolicy)) {
			pipelineStop = (validationPolicy == null) ? BindingEvent.PIPELINE_AFTER_CONVERT
					: validationPolicy.intValue();
		}

		return pipelineStop;
	}
}
