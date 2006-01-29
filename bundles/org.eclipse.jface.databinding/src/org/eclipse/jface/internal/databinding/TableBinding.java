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
package org.eclipse.jface.internal.databinding;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.databinding.BindingEvent;
import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IBindSpec;
import org.eclipse.jface.databinding.ICellProvider;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IReadableSet;
import org.eclipse.jface.databinding.IUpdatableTable;
import org.eclipse.jface.databinding.IUpdatableCellProvider;

/**
 * 
 * 
 */
public class TableBinding extends Binding {

	private final IUpdatableTable target;

	private final IUpdatableCellProvider model;
	
	private /*IConverter*/ List columnConverters;

	private boolean updating = false;

	private IReadableSet modelSet;

	/**
	 * @param context
	 * @param target
	 * @param model
	 * @param bindSpec 
	 */
	public TableBinding(DataBindingContext context,
			IUpdatableTable target, IUpdatableCellProvider model,
			IBindSpec bindSpec) {
		super(context);
		this.target = target;
		this.model = model;
		this.modelSet = model.getReadableSet();
		target.addChangeListener(targetChangeListener);
		model.addChangeListener(cellsChangeListener);
		modelSet.addChangeListener(modelChangeListener);
	}
	
	private final IChangeListener targetChangeListener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {}
	};

	private IChangeListener cellsChangeListener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {
			if (changeEvent.getChangeType() == ChangeEvent.FUNCTION_CHANGED) {
				Collection changedElements = (Collection) changeEvent.getNewValue();
				target.updateElements(changedElements.toArray());
			}
		}
	};

	private IChangeListener modelChangeListener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {
			if (updating)
				return;
			// TODO validation
			if ((changeEvent.getChangeType() &  (ChangeEvent.ADD_MANY | ChangeEvent.REMOVE_MANY | ChangeEvent.STALE)) != 0) {
				BindingEvent e = new BindingEvent(changeEvent, BindingEvent.EVENT_COPY_TO_TARGET, BindingEvent.PIPELINE_AFTER_GET);
				e.originalValue = changeEvent.getNewValue();
				if (failure(errMsg(fireBindingEvent(e)))) {
					return;
				}
				try {
					if (changeEvent.getChangeType() == ChangeEvent.ADD_MANY) {
						target.addAll((Collection) e.originalValue);
					} else if (changeEvent.getChangeType() == ChangeEvent.REMOVE_MANY) {
						target.removeAll((Collection) e.originalValue);
					} else if (changeEvent.getChangeType() == ChangeEvent.STALE) {
						// TODO not implemented (mark target as stale if model is stale, otherwise update target to look like model)
					}
					e.pipelinePosition = BindingEvent.PIPELINE_AFTER_CHANGE;
					if (failure(errMsg(fireBindingEvent(e)))) {
						return;
					}
				} finally {
					updating = false;
				}
			}
		}
	};

	private String errMsg(String validationError) {
		context.updatePartialValidationError(targetChangeListener, null);
		context.updateValidationError(targetChangeListener, validationError);
		return validationError;
	}
	
	private boolean failure(String errorMessage) {
		if (errorMessage != null) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.Binding#updateTargetFromModel(org.eclipse.jface.databinding.ChangeEvent)
	 */
	public void updateTargetFromModel(ChangeEvent changeEvent) {
		target.clear();
		target.addAll(modelSet.toCollection());
		target.setCellProvider(new ICellProvider() {

			public Object getCellValue(Object element, int index) {
				// TODO conversion
				Object cellValue = model.getCellValue(element,index);
				return cellValue == null ? "" : cellValue; //$NON-NLS-1$
			}

			public void setCellValue(Object element, int index, Object value) {
				// TODO conversion, validation
				model.setCellValue(element,index,value);
			}});
	}
}

