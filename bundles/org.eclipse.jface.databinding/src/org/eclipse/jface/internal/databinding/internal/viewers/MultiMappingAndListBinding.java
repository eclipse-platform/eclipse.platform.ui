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
import org.eclipse.jface.internal.databinding.provisional.observable.list.IListChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.provisional.observable.list.ListDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.list.ListDiffEntry;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.IMappingChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.IMultiMapping;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.IObservableMultiMappingWithDomain;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.MappingDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.provisional.observable.value.WritableValue;
import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;
import org.eclipse.jface.internal.databinding.provisional.viewers.IObservableCollectionWithLabels;

/**
 * 
 * 
 */
public class MultiMappingAndListBinding extends Binding {

	private boolean updating = false;

	private IObservableCollectionWithLabels target;

	private IObservableMultiMappingWithDomain model;

	private IObservableList modelList;

	private final IObservableList targetList;
	
	/**
	 * @param context
	 * @param targetList
	 * @param target
	 * @param modelList
	 * @param model
	 * @param bindSpec
	 */
	public MultiMappingAndListBinding(DataBindingContext context,
			IObservableList targetList, IObservableCollectionWithLabels target,
			IObservableList modelList, IObservableMultiMappingWithDomain model,
			BindSpec bindSpec) {
		super(context);
		this.targetList = targetList;
		this.target = target;
		this.model = model;
		this.modelList = modelList;
		// TODO validation/conversion as specified by the bindSpec
		targetList.addListChangeListener(targetChangeListener);
		model.addMappingChangeListener(cellsChangeListener);
		modelList.addListChangeListener(modelChangeListener);
		updateTargetFromModel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.Binding#dispose()
	 */
	public void dispose() {
		target.dispose();
		targetList.dispose();
		model.dispose();
		modelList.dispose();
		
		disposed = true;
	}
	
	private final IListChangeListener targetChangeListener = new IListChangeListener() {
		public void handleListChange(IObservableList source, ListDiff diff) {
		}
	};

	private IMappingChangeListener cellsChangeListener = new IMappingChangeListener() {
		public void handleMappingValueChange(IObservable source,
				MappingDiff diff) {
			target.updateElements(diff.getElements().toArray());
		}
	};

	private IListChangeListener modelChangeListener = new IListChangeListener() {
		public void handleListChange(IObservableList source, ListDiff diff) {
			if (updating) {
				return;
			}
			// TODO validation
			BindingEvent e = new BindingEvent(model, targetList, diff,
					org.eclipse.jface.databinding.BindingEvent.EVENT_COPY_TO_TARGET,
					org.eclipse.jface.databinding.BindingEvent.PIPELINE_AFTER_GET);
			if (failure(errMsg(fireBindingEvent(e)))) {
				return;
			}
			try {
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
				e.pipelinePosition = org.eclipse.jface.databinding.BindingEvent.PIPELINE_AFTER_CHANGE;
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
		targetList.clear();
		targetList.addAll(modelList);
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

	public void updateModelFromTarget() {
		// no-op since the target is never out of sync with the model
	}
}