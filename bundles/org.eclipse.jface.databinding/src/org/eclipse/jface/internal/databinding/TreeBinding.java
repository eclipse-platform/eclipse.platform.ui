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

import org.eclipse.jface.databinding.BindingEvent;
import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IUpdatableTree;

/**
 * TreeBinding facts:
 *    o No Conversion, Validation on various tree elements
 *    o On a bind, only root elements will be pushed from the model to the target
 *    o <code>ChangeEvent.VIRTUAL</code> event will request children as needed
 */
public class TreeBinding extends Binding {

	private final IUpdatableTree target;

	private final IUpdatableTree model;

	private int updating = 0;  // Virtual requests are allowed while an update is in progress

	/**
	 * @param context
	 * @param target
	 * @param model 
	 */
	public TreeBinding(DataBindingContext context,
			IUpdatableTree target, IUpdatableTree model) {
		super(context);
		this.target = target;
		this.model = model;
		
		// some sanity check, target should support all declared model elements
		Class[] targetTypes=target.getTypes();
		Class[] modelTypes=model.getTypes();
		if (targetTypes==null || modelTypes==null)
			throw new BindingException("Tree type is not set"); //$NON-NLS-1$
						
		
		for (int i = 0; i < modelTypes.length; i++) {
			boolean canHandle=false;
			for (int j = 0; j < targetTypes.length; j++) 
				if (targetTypes[j].isAssignableFrom(modelTypes[i])) {
					canHandle=true;
					break;
				}
			if (!canHandle)
				throw new BindingException("Target does not supports type: " + modelTypes[i]); //$NON-NLS-1$
		}
		target.addChangeListener(targetChangeListener);
		model.addChangeListener(modelChangeListener);
	}

	private IChangeListener targetChangeListener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {
			if (updating != 0  && changeEvent.getChangeType()!=ChangeEvent.VIRTUAL)
				return;

			if (changeEvent.getChangeType() == ChangeEvent.VERIFY) {
				// No Conversion on the object itself
			} else {
				update(model, target, changeEvent);
			}
		}
	};

	private IChangeListener modelChangeListener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {
			if (updating != 0  && changeEvent.getChangeType()!=ChangeEvent.VIRTUAL)
				return;

			update(target, model, changeEvent);
		}
	};

	/**
	 * Update the collection from the event.
	 * 
	 * @param needsUpdate
	 *            IUpdatable to be updated
	 * @param event
	 */
	private void update(IUpdatableTree needsUpdate, IUpdatableTree source, ChangeEvent event) {
		int index = event.getPosition();
		Object parent = event.getParent();
			try {
				updating ++;
				int copyType = BindingEvent.EVENT_COPY_TO_MODEL;
				if (needsUpdate == target) {
					copyType = BindingEvent.EVENT_COPY_TO_TARGET;
				}
				BindingEvent bindingEvent = new BindingEvent(event, copyType, BindingEvent.PIPELINE_AFTER_GET);
				bindingEvent.originalValue = event.getNewValue();
				
				switch (event.getChangeType()) {
					case ChangeEvent.VIRTUAL:
						bindingEvent.originalValue = needsUpdate.getElements(parent);
						source.setElements(parent, (Object[])bindingEvent.originalValue);
						break;
						
					case ChangeEvent.CHANGE:
						needsUpdate.setElement(parent, index, event.getNewValue());
						break;
						
					case ChangeEvent.ADD:
						needsUpdate.addElement(parent, index, event.getNewValue());
						break;
						
					case ChangeEvent.REMOVE:
						needsUpdate.removeElement(parent, index);
						break;
						
					case ChangeEvent.REPLACE:
						Object val = event.getNewValue();
						if (val.getClass().isArray())
						   needsUpdate.setElements(parent, (Object[]) val);
						else if (val instanceof Collection)
							needsUpdate.setElements(parent, ((Collection)val).toArray());
						else
							throw new BindingException ("Invalid REPLACE event"); //$NON-NLS-1$
					    break;
				}

				if (failure(errMsg(fireBindingEvent(bindingEvent)))) {
					return;
				}
				
			} finally {
				updating --;				
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
	}
	
	/**
	 * Copy model's root elements into the target
	 * It is up to the target to fire a Virtual request event to get
	 * the rest of the children
	 */
	public void updateTargetFromModel(ChangeEvent changeEvent) {
		copyRootContents(target, model);
	}

	private void copyRootContents(IUpdatableTree destination,
			IUpdatableTree source) {
		try {
			updating ++;
			BindingEvent bindingEvent = new BindingEvent(null, BindingEvent.PIPELINE_AFTER_CHANGE, BindingEvent.PIPELINE_AFTER_GET);
			bindingEvent.originalValue = source.getElements(null);
			destination.setElements(null, (Object[]) bindingEvent.originalValue);
			fireBindingEvent(bindingEvent);
		} finally {
			updating --;
		}
	}

}