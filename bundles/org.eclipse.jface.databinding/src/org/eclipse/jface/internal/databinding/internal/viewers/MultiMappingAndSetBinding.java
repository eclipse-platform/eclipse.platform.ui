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
package org.eclipse.jface.internal.databinding.internal.viewers;

import org.eclipse.jface.internal.databinding.provisional.BindSpec;
import org.eclipse.jface.internal.databinding.provisional.Binding;
import org.eclipse.jface.internal.databinding.provisional.BindingEvent;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.IMappingChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.IMultiMapping;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.IObservableMultiMappingWithDomain;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.MappingDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet;
import org.eclipse.jface.internal.databinding.provisional.observable.set.ISetChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.set.SetDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.provisional.observable.value.WritableValue;
import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;
import org.eclipse.jface.internal.databinding.provisional.viewers.IObservableCollectionWithLabels;

/**
 * 
 * 
 */
public class MultiMappingAndSetBinding extends Binding {

	private boolean updating = false;

	private IObservableCollectionWithLabels target;

	private IObservableMultiMappingWithDomain model;

	private IObservableSet modelSet;

	private final IObservableSet targetSet;

	
	/**
	 * @param context
	 * @param targetSet
	 * @param target
	 * @param modelSet
	 * @param model
	 * @param bindSpec
	 */
	public MultiMappingAndSetBinding(DataBindingContext context,
			IObservableSet targetSet, IObservableCollectionWithLabels target,
			IObservableSet modelSet, IObservableMultiMappingWithDomain model,
			BindSpec bindSpec) {
		super(context);
		this.targetSet = targetSet;
		this.target = target;
		this.model = model;
		this.modelSet = modelSet;
		// TODO validation/conversion as specified by the bindSpec
		targetSet.addSetChangeListener(targetChangeListener);
		model.addMappingChangeListener(cellsChangeListener);
		modelSet.addSetChangeListener(modelChangeListener);
		updateTargetFromModel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.Binding#dispose()
	 */
	public void dispose() {
		target.dispose();
		targetSet.dispose();
		model.dispose();
		modelSet.dispose();
		
		disposed = true;
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
			BindingEvent e = new BindingEvent(model, targetSet, diff,
					BindingEvent.EVENT_COPY_TO_TARGET,
					BindingEvent.PIPELINE_AFTER_GET);
			if (failure(errMsg(fireBindingEvent(e)))) {
				return;
			}
			try {
				// get setDiff from event object - might have been modified by a
				// listener
				SetDiff setDiff = (SetDiff) e.diff;
				targetSet.addAll(setDiff.getAdditions());
				targetSet.removeAll(setDiff.getRemovals());
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
		targetSet.clear();
		targetSet.addAll(modelSet);
		target.init(new IMultiMapping() {
			public Object[] getMappingValues(Object element, int[] columnIndices) {
				return model.getMappingValues(element, columnIndices);
			}

			public void setMappingValues(Object element, int[] columnIndices,
					Object[] values) {
				model.setMappingValues(element, columnIndices, values);
			}
		});
	}

	public IObservableValue getValidationError() {
		return validationErrorObservable;
	}

	public IObservableValue getPartialValidationError() {
		return partialValidationErrorObservable;
	}

	public void updateModelFromTarget() {
		// no-op since the target is never out of sync with the model
	}
}