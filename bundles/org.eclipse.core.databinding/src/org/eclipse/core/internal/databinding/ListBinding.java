/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 159768
 *******************************************************************************/
package org.eclipse.core.internal.databinding;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.BindSpec;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.BindingEvent;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IDiff;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * 
 */
public class ListBinding extends Binding {

	private boolean updating = false;

	private IObservableList modelList;

	private final IObservableList targetList;

	private Map targetValidators = new HashMap();
	private Map modelValidators = new HashMap();

	/**
	 * Positions that validation will be performed.
	 */
	private static final Integer[] VALIDATION_POSITIONS = new Integer[] {
			new Integer(BindingEvent.PIPELINE_AFTER_GET),
			new Integer(BindingEvent.PIPELINE_BEFORE_CHANGE) };

	/**
	 * @param targetList
	 * @param target
	 * @param modelList
	 * @param model
	 * @param bindSpec
	 */
	public ListBinding(IObservableList targetList,
			IObservableList modelList, BindSpec bindSpec) {
		this.targetList = targetList;
		this.modelList = modelList;

		for (int i = 0; i < VALIDATION_POSITIONS.length; i++) {
			Integer positionInteger = VALIDATION_POSITIONS[i];

			targetValidators.put(positionInteger, bindSpec
					.getTargetValidators(positionInteger.intValue()));
			modelValidators.put(positionInteger, bindSpec
					.getModelValidators(positionInteger.intValue()));
		}

		fillBindSpecDefaults(bindSpec, targetList, modelList);

		if (bindSpec.isUpdateModel()) {
			int stopPosition = getValidationPolicy(bindSpec
					.getModelUpdatePolicy(), bindSpec.getTargetValidatePolicy());
			targetList
					.addListChangeListener(targetChangeListener = new ChangeListener(
							BindingEvent.EVENT_COPY_TO_MODEL, stopPosition));
		} else {
			targetChangeListener = null;
		}

		if (bindSpec.isUpdateTarget()) {
			 int stopPosition = getValidationPolicy(
					bindSpec.getTargetUpdatePolicy(), bindSpec
							.getModelValidatePolicy());
			modelList
					.addListChangeListener(modelChangeListener = new ChangeListener(
							BindingEvent.EVENT_COPY_TO_TARGET, stopPosition));
		} else {
			modelChangeListener = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.internal.databinding.provisional.Binding#dispose()
	 */
	public void dispose() {
		targetList.removeListChangeListener(targetChangeListener);
		modelList.removeListChangeListener(modelChangeListener);
		super.dispose();
	}

	private final IListChangeListener targetChangeListener;
	private final IListChangeListener modelChangeListener;

	private class ChangeListener implements IListChangeListener {
		private final int pipelinePosition;
		private final int copyType;

		ChangeListener(int copyType, int pipelinePosition) {
			this.pipelinePosition = pipelinePosition;
			this.copyType = copyType;
		}

		public void handleListChange(ListChangeEvent event) {
			if (copyType == BindingEvent.EVENT_COPY_TO_MODEL) {
				doUpdateModelFromTarget(event.diff, pipelinePosition);
			} else {
				doUpdateTargetFromModel(event.diff, pipelinePosition);
			}
		}
	}

	/**
	 * Perform the target to model process up to and including the
	 * <code>lastPosition</code>.
	 * 
	 * @param diff
	 * @param lastPosition
	 *            BindingEvent.PIPELINE_* constant
	 */
	private void doUpdateModelFromTarget(IDiff diff, int lastPosition) {
		if (updating) {
			return;
		}

		BindingEvent e = createBindingEvent(diff,
				BindingEvent.EVENT_COPY_TO_MODEL,
				BindingEvent.PIPELINE_AFTER_GET);

		if (!performPosition(BindingEvent.PIPELINE_AFTER_GET, e, lastPosition)) {
			return;
		}

		updating = true;
		try {
			if (!performPosition(BindingEvent.PIPELINE_BEFORE_CHANGE, e,
					lastPosition)) {
				return;
			}

			// get setDiff from event object - might have been modified by a
			// listener
			ListDiff setDiff = (ListDiff) e.diff;
			ListDiffEntry[] differences = setDiff.getDifferences();
			for (int i = 0; i < differences.length; i++) {
				ListDiffEntry entry = differences[i];
				if (entry.isAddition()) {
					modelList.add(entry.getPosition(), entry.getElement());
				} else {
					modelList.remove(entry.getPosition());
				}
			}

			performPosition(BindingEvent.PIPELINE_AFTER_CHANGE, e, lastPosition);
		} finally {
			updating = false;
		}
	}

	/**
	 * Performs the model to target process up to and including the
	 * <code>lastPosition</code>.
	 * 
	 * @param diff
	 * @param lastPosition
	 *            BindingEvent.PIPELINE_* constant
	 */
	private void doUpdateTargetFromModel(IDiff diff, int lastPosition) {
		if (updating) {
			return;
		}
		BindingEvent e = createBindingEvent(diff,
				BindingEvent.EVENT_COPY_TO_TARGET, BindingEvent.PIPELINE_AFTER_GET);
		if (!performPosition(BindingEvent.PIPELINE_AFTER_GET, e, lastPosition)) {
			return;
		}

		updating = true;
		try {
			if (!performPosition(BindingEvent.PIPELINE_BEFORE_CHANGE, e,
					lastPosition)) {
				return;
			}

			// get setDiff from event object - might have been modified by a
			// listener
			ListDiff setDiff = (ListDiff) e.diff;
			ListDiffEntry[] differences = setDiff.getDifferences();
			for (int i = 0; i < differences.length; i++) {
				ListDiffEntry entry = differences[i];
				if (entry.isAddition()) {
					targetList.add(entry.getPosition(), entry.getElement());
				} else {
					targetList.remove(entry.getPosition());
				}
			}

			performPosition(BindingEvent.PIPELINE_AFTER_CHANGE, e, lastPosition);
		} finally {
			updating = false;
		}
	}

	/**
	 * Performs the necessary processing for the position.
	 * 
	 * @param value
	 * @param pipelinePosition
	 * @param e
	 * @param lastPosition
	 * @return <code>true</code> if should proceed to the next position
	 */
	private boolean performPosition(int pipelinePosition, BindingEvent e,
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
					.get(new Integer(pipelinePosition));
			if (validators != null) {
				for (int i = 0; status.isOK() && i < validators.length; i++) {
					status = validators[i].validate(e.diff);
				}
			}
		}

		if (status.isOK()) {
			// Only notify listeners if validation passed.
			e.pipelinePosition = pipelinePosition;
			status = fireBindingEvent(e);
		}

		partialValidationErrorObservable.setValue(null);
		validationErrorObservable.setValue(status);

		return (status.isOK() && pipelinePosition != lastPosition);
	}

	private WritableValue partialValidationErrorObservable;

	private WritableValue validationErrorObservable;

	public void updateTargetFromModel() {
		updateTargetFromModel(BindingEvent.PIPELINE_AFTER_CHANGE);
	}

	public IObservableValue getValidationStatus() {
		return validationErrorObservable;
	}

	public IObservableValue getPartialValidationStatus() {
		return partialValidationErrorObservable;
	}

	public void updateModelFromTarget() {
		updateModelFromTarget(BindingEvent.PIPELINE_AFTER_CHANGE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.databinding.Binding#performModelToTarget(int)
	 */
	public void updateTargetFromModel(final int phase) {
		modelList.getRealm().exec(new Runnable() {
			public void run() {
				final ListDiff listDiff = Diffs.computeListDiff(targetList,
						modelList);
				targetList.getRealm().exec(new Runnable() {
					public void run() {
						doUpdateTargetFromModel(listDiff, phase);
					}
				});
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.databinding.Binding#performTargetToModel(int)
	 */
	public void updateModelFromTarget(final int phase) {
		targetList.getRealm().exec(new Runnable() {
			public void run() {
				final ListDiff listDiff = Diffs.computeListDiff(modelList,
						targetList);
				modelList.getRealm().exec(new Runnable() {
					public void run() {
						doUpdateModelFromTarget(listDiff, phase);
					}
				});
			}
		});
	}

	protected void preInit() {
		partialValidationErrorObservable = new WritableValue(context
				.getValidationRealm(), Status.OK_STATUS, IStatus.class);
		validationErrorObservable = new WritableValue(context
				.getValidationRealm(), Status.OK_STATUS, IStatus.class);
	}

	protected void postInit() {
		updateTargetFromModel();
	}

	private static int getValidationPolicy(Integer updatePolicy,
			Integer validationPolicy) {
		int pipelineStop = BindingEvent.PIPELINE_AFTER_CHANGE;

		if (BindSpec.POLICY_EXPLICIT.equals(updatePolicy)) {
			pipelineStop = (validationPolicy == null) ? BindingEvent.PIPELINE_AFTER_GET
					: validationPolicy.intValue();
		}

		return pipelineStop;
	}
}