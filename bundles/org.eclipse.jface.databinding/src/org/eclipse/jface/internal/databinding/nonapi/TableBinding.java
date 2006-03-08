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

import org.eclipse.jface.internal.databinding.api.BindingEvent;
import org.eclipse.jface.internal.databinding.api.BindSpec;
import org.eclipse.jface.internal.databinding.api.observable.IObservable;
import org.eclipse.jface.internal.databinding.api.observable.mapping.IMappingChangeListener;
import org.eclipse.jface.internal.databinding.api.observable.mapping.MappingDiff;
import org.eclipse.jface.internal.databinding.api.observable.mapping.IMultiMapping;
import org.eclipse.jface.internal.databinding.api.observable.mapping.IObservableMultiMappingWithDomain;
import org.eclipse.jface.internal.databinding.api.observable.set.IObservableSet;
import org.eclipse.jface.internal.databinding.api.observable.set.IObservableSetWithLabels;
import org.eclipse.jface.internal.databinding.api.observable.set.ISetChangeListener;
import org.eclipse.jface.internal.databinding.api.observable.set.SetDiff;
import org.eclipse.jface.internal.databinding.api.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.api.observable.value.WritableValue;
import org.eclipse.jface.internal.databinding.api.validation.ValidationError;

/**
 * 
 * 
 */
public class TableBinding extends Binding {

	private boolean updating = false;

	private IObservableSetWithLabels target;

	private IObservableMultiMappingWithDomain model;

	private IObservableSet modelDomain;

	/**
	 * @param context
	 * @param target
	 * @param model
	 * @param bindSpec
	 */
	public TableBinding(DataBindingContext context,
			IObservableSetWithLabels target,
			IObservableMultiMappingWithDomain model, BindSpec bindSpec) {
		super(context);
		this.target = target;
		this.model = model;
		this.modelDomain = model.getDomain();
		// TODO validation/conversion as specified by the bindSpec
		target.addSetChangeListener(targetChangeListener);
		model.addMappingChangeListener(cellsChangeListener);
		modelDomain.addSetChangeListener(modelChangeListener);
	}

	private final ISetChangeListener targetChangeListener = new ISetChangeListener() {
		public void handleSetChange(IObservableSet source, SetDiff diff) {
		}
	};

	private IMappingChangeListener cellsChangeListener = new IMappingChangeListener() {
		public void handleMappingValueChange(IObservable source,
				MappingDiff diff) {
			target.updateElements(diff.getElements().toArray());
		}
	};

	private ISetChangeListener modelChangeListener = new ISetChangeListener() {
		public void handleSetChange(IObservableSet source, SetDiff diff) {
			if (updating) {
				return;
			}
			// TODO validation
			BindingEvent e = new BindingEvent(model, target, diff,
					BindingEvent.EVENT_COPY_TO_TARGET,
					BindingEvent.PIPELINE_AFTER_GET);
			if (failure(errMsg(fireBindingEvent(e)))) {
				return;
			}
			try {
				// get setDiff from event object - might have been modified by a
				// listener
				SetDiff setDiff = (SetDiff) e.diff;
				target.addAll(setDiff.getAdditions());
				target.removeAll(setDiff.getRemovals());
				e.pipelinePosition = BindingEvent.PIPELINE_AFTER_CHANGE;
				if (failure(errMsg(fireBindingEvent(e)))) {
					return;
				}
			} finally {
				updating = false;
			}
		}
	};

	private WritableValue partialValidationErrorObservable = new WritableValue(
			null);

	private WritableValue validationErrorObservable = new WritableValue(null);

	private ValidationError errMsg(ValidationError validationError) {
		partialValidationErrorObservable.setValue(null);
		validationErrorObservable.setValue(validationError);
		return validationError;
	}

	private boolean failure(ValidationError errorMessage) {
		// FIXME: Need to fire a BindingEvent here
		if (errorMessage != null
				&& errorMessage.status == ValidationError.ERROR) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.internal.databinding.Binding#updateTargetFromModel(org.eclipse.jface.internal.provisional.databinding.ChangeEvent)
	 */
	public void updateTargetFromModel() {
		target.clear();
		target.addAll(modelDomain);
		target.init(new IMultiMapping() {
			public Object[] getMappingValues(Object element, int[] indices) {
				return model.getMappingValues(element, indices);
			}

			public void setMappingValues(Object element, int[] indices,
					Object[] values) {
				model.setMappingValues(element, indices, values);
			}
		});
	}

	public IObservableValue getValidationError() {
		return validationErrorObservable;
	}

	public IObservableValue getPartialValidationError() {
		return partialValidationErrorObservable;
	}
}
