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
package org.eclipse.jface.databinding.internal;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.IUpdatableTree;

/**
 * TreeBinding facts:
 *    o No Conversion, Validation on various tree elements
 *    o On a bind, only root elements will be pushed from the model to the target
 *    o <code>ChangeEvent.VIRTUAL</code> event will request children as needed
 * 
 */
public class TreeBinding extends Binding implements IChangeListener {

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
		
		// some sanity check
		Class[] targetTypes=target.getTypes();
		Class[] modelTypes=model.getTypes();
		if (targetTypes==null || modelTypes==null || targetTypes.length!=modelTypes.length)
			throw new BindingException("Target and Model do not have the same Types"); //$NON-NLS-1$
		List targetList = Arrays.asList(targetTypes);
		for (int i = 0; i < model.getTypes().length; i++) {
			if (!targetList.contains(modelTypes[i]))
				throw new BindingException("Model Type does not apply to target: " + modelTypes[i]); //$NON-NLS-1$
		}
	}

	public void handleChange(ChangeEvent changeEvent) {
		if (updating==0 || changeEvent.getChangeType()==ChangeEvent.VIRTUAL) {
			IUpdatable notifier = changeEvent.getUpdatable();
			if (notifier == target) {
				if (changeEvent.getChangeType() == ChangeEvent.VERIFY) {
					// No Conversion on the object itself
				} else {
					update(model, target, changeEvent);
				}
			} else if (notifier == model) {				
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
	private void update(IUpdatableTree needsUpdate, IUpdatableTree source, ChangeEvent event) {
		int index = event.getPosition();
		Object parent = event.getParent();
			try {
				updating ++;
				if (event.getChangeType() == ChangeEvent.VIRTUAL)
					source.setElements(parent, needsUpdate.getElements(parent));
				else if (event.getChangeType() == ChangeEvent.CHANGE)
					needsUpdate.setElement(parent, index, event.getNewValue());
				else if (event.getChangeType() == ChangeEvent.ADD)
					needsUpdate.addElement(parent, index, event.getNewValue());
				else if (event.getChangeType() == ChangeEvent.REMOVE)
					needsUpdate.removeElement(parent, index);
			} finally {
				updating --;				
			}
	}

	/**
	 * Copy model's root elements into the target
	 * It is up to the target to fire a Virtual request event to get
	 * the rest of the children
	 */
	public void updateTargetFromModel() {
		copyRootContents(target, model);
	}

	private void copyRootContents(IUpdatableTree destination,
			IUpdatableTree source) {
		try {
			updating ++;
			destination.setElements(null, source.getElements(null));
		} finally {
			updating --;
		}
	}

}