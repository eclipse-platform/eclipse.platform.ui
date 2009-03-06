/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.handlers;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.commands.PersistentState;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This state supports a radio-button like command, where the value of the
 * parameterized command is stored as state. The command must define a state
 * using the {@link #STATE_ID} id and a string commandParameter using the
 * {@link #PARAMETER_ID} id. Menu contributions supplied by
 * <code>org.eclipse.ui.menus</code> can then set the {@link #PARAMETER_ID}.
 * 
 * @see HandlerUtil#updateRadioState(org.eclipse.core.commands.Command, String)
 * @see HandlerUtil#matchesRadioState(org.eclipse.core.commands.ExecutionEvent)
 * @since 3.5
 */
public final class RadioState extends PersistentState implements
		IExecutableExtension {

	/**
	 * The state ID for a radio state understood by the system.
	 */
	public final static String STATE_ID = "org.eclipse.ui.commands.radioState"; //$NON-NLS-1$

	/**
	 * The parameter ID for a radio state understood by the system.
	 */
	public final static String PARAMETER_ID = "org.eclipse.ui.commands.radioStateParameter"; //$NON-NLS-1$

	public RadioState() {
		setShouldPersist(true);
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) {
		if (data instanceof String)
			setValue(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.commands.PersistentState#load(org.eclipse.jface.preference
	 * .IPreferenceStore, java.lang.String)
	 */
	public void load(IPreferenceStore store, String preferenceKey) {
		final String value = store.getString(preferenceKey);
		if (value.length() != 0)
			setValue(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.commands.PersistentState#save(org.eclipse.jface.preference
	 * .IPreferenceStore, java.lang.String)
	 */
	public void save(IPreferenceStore store, String preferenceKey) {
		final Object value = getValue();
		if (value instanceof String) {
			store.setValue(preferenceKey, (String) value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.State#setValue(java.lang.Object)
	 */
	public void setValue(Object value) {
		if (!(value instanceof String))
			return; // we set only String values
		super.setValue(value);
	}

}
