/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.preference;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class BooleanPropertyAction extends Action {

	private IPreferenceStore preferenceStore;
	private String property;	

	public BooleanPropertyAction(String title, IPreferenceStore preferenceStore, String property)
		throws IllegalArgumentException {
		super(title, AS_CHECK_BOX);
		
		if (preferenceStore == null || property == null)
			throw new IllegalArgumentException();
		
		this.preferenceStore = preferenceStore;
		this.property = property;
		final String finalProprety = property;
		
		preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (finalProprety.equals(event.getProperty()))
					setChecked(Boolean.TRUE.equals(event.getNewValue())); 
			}
		});

		setChecked(preferenceStore.getBoolean(property));		
	}

	public void run() {
		preferenceStore.setValue(property, isChecked());
	}
}
