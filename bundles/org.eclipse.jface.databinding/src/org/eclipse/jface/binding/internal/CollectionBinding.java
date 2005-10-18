package org.eclipse.jface.binding.internal;


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
/*
 *  Created Oct 18, 2005 by Gili Mendel
 * 
 *  $RCSfile: $
 *  $Revision: $  $Date: $ 
 */
 


import org.eclipse.jface.binding.*;
 

/**
 * 
 *
 */
public class CollectionBinding implements IChangeListener {
	
	private final DatabindingService context;

	private final IUpdatableCollection target;

	private final IUpdatableCollection model;

	private final IConverter converter;

	private final IValidator validator;
	

	/**
	 * @param context
	 * @param target
	 * @param model
	 * @param converter
	 * @param validator
	 */
	public CollectionBinding(DatabindingService context, 
			IUpdatableCollection target,IUpdatableCollection model, 
			IConverter converter, IValidator validator) {
		super();
		this.context = context;
		this.target = target;
		this.model = model;
		this.converter = converter;
		this.validator = validator;
		
		initialize();
	}
	
	private void initialize() {
		target.addChangeListener(this);
		model.addChangeListener(this);		
	}


	public void handleChange(IChangeEvent changeEvent) {
		IUpdatable notifier =  changeEvent.getUpdatable();
		if (notifier == target) {
			if (changeEvent.getChangeType() == IChangeEvent.VERIFY) {
				// we are notified of a pending change, do validation
				// and
				// veto the change if it is not valid
	            //TODO verify
			} else {
				// the target (usually a widget) has changed, validate
				// the
				// value and update the source
				//TODO validation
				update(model, changeEvent);
			}
		} else if (notifier == model){
			//TODO validation
			update(target, changeEvent);
		}			
	}

	/**
	 * Update the collection from the event.
	 * @param needsUpdate IUpdatable to be updated
	 * @param event 
	 */
	public void update(IUpdatableCollection needsUpdate, IChangeEvent event) {
		int row = event.getPosition();	
		if (event.getChangeType() == IChangeEvent.CHANGE)
			needsUpdate.setElement(row, event.getNewValue());
		else if (event.getChangeType() == IChangeEvent.ADD)
			needsUpdate.addElement(event.getNewValue(), row);
		else if (event.getChangeType() == IChangeEvent.REMOVE)
			needsUpdate.removeElement(row);
	}
	

	/**
	 * Copy model's element into the target
	 */
	public void updateTargetFromModel() {
		// Remove old, if any
		while (target.getSize() > 0)
			target.removeElement(0);
		
		// Set the target List with the content of the Model List
		for (int i = 0; i < model.getSize(); i++) {
			target.addElement(model.getElement(i), i);
		}
	}


}