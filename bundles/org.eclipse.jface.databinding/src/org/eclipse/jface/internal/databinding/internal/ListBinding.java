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
package org.eclipse.jface.internal.databinding.internal;

import org.eclipse.jface.internal.databinding.provisional.BindSpec;
import org.eclipse.jface.internal.databinding.provisional.Binding;
import org.eclipse.jface.internal.databinding.provisional.BindingEvent;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.observable.list.IListChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.provisional.observable.list.ListDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.list.ListDiffEntry;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.provisional.observable.value.WritableValue;
import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;

/**
 * 
 * 
 */
public class ListBinding extends Binding {

	private boolean updating = false;

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
	public ListBinding(DataBindingContext context, IObservableList targetList,
			IObservableList modelList, BindSpec bindSpec) {
		super(context);
		this.targetList = targetList;
		this.modelList = modelList;
		// TODO validation/conversion as specified by the bindSpec
		targetList.addListChangeListener(targetChangeListener);
		modelList.addListChangeListener(modelChangeListener);
		updateTargetFromModel();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.Binding#dispose()
	 */
	public void dispose() {
		targetList.removeListChangeListener(targetChangeListener);
		modelList.removeListChangeListener(modelChangeListener);
		targetList.dispose();
		modelList.dispose();
		
		disposed = true;
	}
	
	private final IListChangeListener targetChangeListener = new IListChangeListener() {
		public void handleListChange(IObservableList source, ListDiff diff) {
			if (updating) {
				return;
			}
			// TODO validation
			BindingEvent e = new BindingEvent(modelList, targetList, diff,
					BindingEvent.EVENT_COPY_TO_MODEL,
					BindingEvent.PIPELINE_AFTER_GET);
			if (failure(errMsg(fireBindingEvent(e)))) {
				return;
			}
			updating = true;
			try {
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
				e.pipelinePosition = BindingEvent.PIPELINE_AFTER_CHANGE;
				if (failure(errMsg(fireBindingEvent(e)))) {
					return;
				}
			} finally {
				updating = false;
			}
		}
	};

	private IListChangeListener modelChangeListener = new IListChangeListener() {
		public void handleListChange(IObservableList source, ListDiff diff) {
			if (updating) {
				return;
			}
			// TODO validation
			BindingEvent e = new BindingEvent(modelList, targetList, diff,
					BindingEvent.EVENT_COPY_TO_TARGET,
					BindingEvent.PIPELINE_AFTER_GET);
			if (failure(errMsg(fireBindingEvent(e)))) {
				return;
			}
			updating = true;
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

	public void updateTargetFromModel() {
		updating = true;
		try {
			targetList.clear();
			targetList.addAll(modelList);
		} finally {
			updating = false;
		}
	}

	public IObservableValue getValidationError() {
		return validationErrorObservable;
	}

	public IObservableValue getPartialValidationError() {
		return partialValidationErrorObservable;
	}

	public void updateModelFromTarget() {
		updating = true;
		try {
			modelList.clear();
			modelList.addAll(targetList);
		} finally {
			updating = false;
		}
	}
}