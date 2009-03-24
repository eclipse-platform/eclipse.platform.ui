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

import java.util.Hashtable;

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
 * <p>
 * When parsing from the registry, this state understands two parameters:
 * <code>default</code>, which is the default value for this item; and
 * <code>persisted</code>, which is whether the state should be persisted
 * between sessions. The <code>default</code> parameter has no default value and
 * must be specified in one of its forms, and the <code>persisted</code>
 * parameter defaults to <code>true</code>. If only one parameter is passed
 * (i.e., using the class name followed by a colon), then it is assumed to be
 * the <code>default</code> parameter.
 * </p>
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

		boolean shouldPersist = true; // persist by default
		if (data instanceof String) {
			setValue(data);
		} else if (data instanceof Hashtable) {
			final Hashtable parameters = (Hashtable) data;
			final Object defaultObject = parameters.get("default"); //$NON-NLS-1$
			if (defaultObject instanceof String) {
				setValue(defaultObject);
			}

			final Object persistedObject = parameters.get("persisted"); //$NON-NLS-1$
			if (persistedObject instanceof String
					&& "false".equalsIgnoreCase(((String) persistedObject))) //$NON-NLS-1$
				shouldPersist = false;
		}
		setShouldPersist(shouldPersist);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.commands.PersistentState#load(org.eclipse.jface.preference
	 * .IPreferenceStore, java.lang.String)
	 */
	public void load(IPreferenceStore store, String preferenceKey) {
		if (!shouldPersist())
			return;
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
		if (!shouldPersist())
			return;
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
