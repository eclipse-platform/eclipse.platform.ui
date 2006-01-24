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

import java.util.List;

import org.eclipse.jface.databinding.BindingEvent;
import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IBindSpec;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IUpdatableCollection;
import org.eclipse.jface.databinding.converter.IConverter;
import org.eclipse.jface.databinding.validator.IValidator;

/**
 * 
 * 
 */
public class CollectionBinding extends Binding {

	private final IUpdatableCollection target;

	private final IUpdatableCollection model;

	private IConverter converter;

	private IValidator validator;

	private boolean updating = false;

	/**
	 * @param context
	 * @param target
	 * @param model
	 * @param bindSpec 
	 */
	public CollectionBinding(DataBindingContext context,
			IUpdatableCollection target, IUpdatableCollection model,
			IBindSpec bindSpec) {
		super(context);
		this.target = target;
		this.model = model;
		this.converter = bindSpec.getConverter();
		if (converter == null) {
			throw new BindingException("Missing converter from " + target.getElementType() + " to " + model.getElementType()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if(!converter.getModelType().equals(model.getElementType())) {
			throw new BindingException("Converter does not apply to model type. Expected: " + model.getElementType() + ", actual: " + converter.getModelType()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if(!converter.getTargetType().equals(target.getElementType())) {
			throw new BindingException("Converter does not apply to target type. Expected: " + target.getElementType() + ", actual: " + converter.getTargetType()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		this.validator = bindSpec.getValidator();
		if (validator == null) {
			throw new BindingException("Missing validator"); //$NON-NLS-1$
		}
		target.addChangeListener(targetChangeListener);
		model.addChangeListener(modelChangeListener);
	}
	
	private final IChangeListener targetChangeListener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {
			if (updating)
				return;

			if (changeEvent.getChangeType() == ChangeEvent.VERIFY) {
				// we are notified of a pending change, do validation
				// and veto the change if it is not valid
				Object value = changeEvent.getNewValue();
				String partialValidationError = validator
						.isPartiallyValid(value);
				context.updatePartialValidationError(this,
						partialValidationError);
				if (partialValidationError != null) {
					changeEvent.setVeto(true);
				}

				BindingEvent e = new BindingEvent(changeEvent, BindingEvent.EVENT_PARTIAL_VALIDATE, BindingEvent.PIPELINE_AFTER_VALIDATE);
				e.originalValue = changeEvent.getNewValue();
				partialValidationError = fireBindingEvent(e);
				context.updatePartialValidationError(this,
						partialValidationError);
				if (partialValidationError != null) {
					changeEvent.setVeto(true);
				}
			} else {
				// Update	
				// TODO, at this time we validate only the "value/conversion" not the index (add/remove)
				String validationError = null;
				if (changeEvent.getChangeType() != ChangeEvent.REMOVE) {
					Object value = changeEvent.getNewValue();
					validationError = validator.isValid(value);
					context.updatePartialValidationError(this, null);
					context.updateValidationError(this, validationError);
				}
				if (validationError == null) 
				     update(model, target, changeEvent);
			}
		}
	};

	private IChangeListener modelChangeListener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {
			if (updating)
				return;
			// TODO validation
			update(target, model, changeEvent);
		}
	};

	/**
	 * Update the collection from the event.
	 * 
	 * @param needsUpdate
	 *            IUpdatable to be updated
	 * @param changeEvent
	 */
	private void update(IUpdatableCollection needsUpdate, IUpdatableCollection source, ChangeEvent changeEvent) {
		int row = changeEvent.getPosition();
		if (row == -1) {
			// full update
			copyContents(changeEvent, needsUpdate, source);
		} else {
			try {
				updating = true;
				int copyType = BindingEvent.EVENT_COPY_TO_MODEL;
				if (needsUpdate == target) {
					copyType = BindingEvent.EVENT_COPY_TO_TARGET;
				}
				BindingEvent e = new BindingEvent(changeEvent, copyType, BindingEvent.PIPELINE_AFTER_GET);
				e.originalValue = changeEvent.getNewValue();
				if (failure(errMsg(fireBindingEvent(e)))) {
					return;
				}

				if (changeEvent.getChangeType() == ChangeEvent.CHANGE) {
					needsUpdate.setElement(row, changeEvent.getNewValue());
				}
				else if (changeEvent.getChangeType() == ChangeEvent.ADD) {
					needsUpdate.addElement(changeEvent.getNewValue(), row);
				}
				else if (changeEvent.getChangeType() == ChangeEvent.REMOVE) {
					needsUpdate.removeElement(row);
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

	/**
	 * Copy model's element into the target
	 */
	public void updateTargetFromModel(ChangeEvent changeEvent) {
		copyContents(changeEvent, target, model);
	}

	private void copyContents(ChangeEvent changeEvent,
			IUpdatableCollection destination, IUpdatableCollection source) {
		try {
		   updating = true;
		   
		   int copyType = BindingEvent.EVENT_COPY_TO_MODEL;
		   if (destination == target) {
			   copyType = BindingEvent.EVENT_COPY_TO_TARGET;
		   }
		   BindingEvent e = new BindingEvent(changeEvent, copyType, BindingEvent.PIPELINE_AFTER_GET);
		   e.originalValue = source.getElements();
		   if (failure(errMsg(fireBindingEvent(e)))) {
			   return;
		   }
		   
           destination.setElements((List)e.originalValue);
           e.pipelinePosition = BindingEvent.PIPELINE_AFTER_CHANGE;
		   if (failure(errMsg(fireBindingEvent(e)))) {
			   return;
		   }
		} finally {
			updating = false;
		}
	}

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
	}}

