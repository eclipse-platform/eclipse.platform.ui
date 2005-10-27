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
import org.eclipse.jface.binding.IBindSpec;
import org.eclipse.jface.binding.IChangeEvent;
import org.eclipse.jface.binding.IChangeListener;
import org.eclipse.jface.binding.IConverter;
import org.eclipse.jface.binding.IUpdatable;
import org.eclipse.jface.binding.IUpdatableCollection;
import org.eclipse.jface.binding.IValidator;

/**
 * 
 * 
 */
public class CollectionBinding extends Binding implements IChangeListener {

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
	public CollectionBinding(DatabindingContext context,
			IUpdatableCollection target, IUpdatableCollection model,
			IBindSpec bindSpec) {
		super(context);
		this.target = target;
		this.model = model;
		this.converter = bindSpec == null ? null : bindSpec.getConverter();
		this.validator = bindSpec == null ? null : bindSpec.getValidator();
		if (this.validator == null) {
			this.validator = context.getValidator(converter);
		}
	}

	public void handleChange(IChangeEvent changeEvent) {
		if (!updating) {
			IUpdatable notifier = changeEvent.getUpdatable();
			if (notifier == target) {
				if (changeEvent.getChangeType() == IChangeEvent.VERIFY) {
					// we are notified of a pending change, do validation
					// and
					// veto the change if it is not valid
					// TODO verify
				} else {
					// the target (usually a widget) has changed, validate
					// the
					// value and update the source
					// TODO validation
					update(model, target, changeEvent);
				}
			} else if (notifier == model) {
				// TODO validation
				update(target, model, changeEvent);
			}
		}
	}

	/**
	 * Update the collection from the event.
	 * 
	 * @param needsUpdate
	 *            IUpdatable to be updated
	 * @param event
	 */
	private void update(IUpdatableCollection needsUpdate, IUpdatableCollection source, IChangeEvent event) {
		int row = event.getPosition();
		if (row == -1) {
			// full update
			copyContents(needsUpdate, source);
		} else {
			try {
				updating = true;
				if (event.getChangeType() == IChangeEvent.CHANGE)
					needsUpdate.setElement(row, event.getNewValue());
				else if (event.getChangeType() == IChangeEvent.ADD)
					needsUpdate.addElement(event.getNewValue(), row);
				else if (event.getChangeType() == IChangeEvent.REMOVE)
					needsUpdate.removeElement(row);
			} finally {
				updating = false;
			}
		}
	}

	/**
	 * Copy model's element into the target
	 */
	public void updateTargetFromModel() {
		copyContents(target, model);
	}

	private void copyContents(IUpdatableCollection destination,
			IUpdatableCollection source) {
		try {
			updating = true;
			// Remove old, if any
			while (destination.getSize() > 0)
				destination.removeElement(0);

			// Set the target List with the content of the Model List
			for (int i = 0; i < source.getSize(); i++) {
				destination.addElement(source.getElement(i), i);
			}
		} finally {
			updating = false;
		}
	}

}